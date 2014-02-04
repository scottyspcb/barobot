#include "barobot_mainboard_main.h" 
#define IS_MAINBOARD true
#define IS_PROGRAMMER true
#include <WSWire.h>
#include <barobot_common.h>
#include <i2c_helpers.h>
#include <AccelStepper.h>
#include <FlexiTimer2.h>
#include <avr/io.h>
#include <avr/wdt.h>
byte in_buffer[7];
volatile uint8_t input_buffer[MAINBOARD_BUFFER_LENGTH][7] = {{0,0,0,0,0,0,0},{0,0,0,0,0,0,0},{0,0,0,0,0,0,0}};
volatile uint8_t buff_length[MAINBOARD_BUFFER_LENGTH] = {0,0,0};

void (*spi_init)();
uint8_t (*spi_send)(uint8_t);

byte out_buffer[7];
uint8_t serialBuff[130];
uint8_t serialBuff_pos   = 0;

boolean error=0;
volatile boolean stepper_now = false;
int here;
uint8_t hbval            = 128;

boolean prog_mode        = false;
String serial0Buffer     = "";
boolean Console0Complete = false;   // This will be set to true once we have a full string
boolean last_i2c_read_error = false;

byte reprogramm_index	= 0;
byte reprogramm_address = 0;
long unsigned milis100	= 0;

void disableWd(){
	// Disable the WDT
	//    WDTCSR |= _BV(WDCE) | _BV(WDE);
	//    WDTCSR = 0;
	//wdt_disable();
}
void setup(){
	//disableWd();
	DEBUGINIT();
	DEBUGLN("-MSTART");
	pinMode(PIN_PROGRAMMER_LED_ACTIVE, OUTPUT);
	pinMode(PIN_PROGRAMMER_LED_ERROR, OUTPUT);
	pinMode(PIN_PROGRAMMER_LED_STATE, OUTPUT);
	pinMode(PIN_MAINBOARD_SCK, INPUT );
	pinMode(PIN_MAINBOARD_MISO, INPUT );
	pinMode(PIN_MAINBOARD_MOSI, INPUT );

	digitalWrite(PIN_PROGRAMMER_LED_ACTIVE, LOW);
	digitalWrite(PIN_PROGRAMMER_LED_ERROR, LOW);
	digitalWrite(PIN_PROGRAMMER_LED_STATE, LOW);
	 
	digitalWrite(PIN_PROGRAMMER_LED_ERROR, LOW);
	digitalWrite(PIN_PROGRAMMER_LED_STATE, LOW);

	Wire.begin(I2C_ADR_MAINBOARD);
	digitalWrite(SDA, 1);
	digitalWrite(SCL, 1);
	Wire.onReceive(receiveEvent);
	setupStepper();
	i2c_device_found(I2C_ADR_MAINBOARD, MAINBOARD_DEVICE_TYPE,MAINBOARD_VERSION);
}

#if MAINBOARD_SERVO_4PIN==true
AccelStepper stepperX(8, PIN_MAINBOARD_STEPPER_STEP0, PIN_MAINBOARD_STEPPER_STEP1, PIN_MAINBOARD_STEPPER_STEP2, PIN_MAINBOARD_STEPPER_STEP3 );
#else
AccelStepper stepperX(1, PIN_MAINBOARD_STEPPER_STEP, PIN_MAINBOARD_STEPPER_DIR);      // Step, DIR
#endif
 
void setupStepper(){
	stepperX.disable_on_ready = true;
	stepperX.setEnablePin(PIN_MAINBOARD_STEPPER_ENABLE);
	stepperX.setPinsInverted( false, false, true ); // enable pin invert
	stepperX.setAcceleration(MAINBOARD_ACCELERX);
	stepperX.setMaxSpeed(MAINBOARD_SPEEDX);

	pinMode(PIN_MAINBOARD_STEPPER_MS1, OUTPUT);
	pinMode(PIN_MAINBOARD_STEPPER_MS2, OUTPUT);
	pinMode(PIN_MAINBOARD_STEPPER_MS3, OUTPUT);
	digitalWrite(PIN_MAINBOARD_STEPPER_MS1, HIGH );
	digitalWrite(PIN_MAINBOARD_STEPPER_MS2, HIGH );
	digitalWrite(PIN_MAINBOARD_STEPPER_MS3, LOW );
	stepperX.onReady(stepperReady);
	FlexiTimer2::set(1, 1.0/3000, timer);
	FlexiTimer2::start();
	milis100 = millis() + 3000;    // za 3 sek zaczij sprawdzanie magistrali
}

void timer(){
	stepper_now = true;
}

void loop(){
	long unsigned mil = millis();
	if( mil > milis100 ){    // co 7 sek
		check_i2c();    // czy linia jest drozna?
		hbval++;
		if(hbval>=10){
			//  2c_analog(I2C_ADR_CARRET, 0);
			hbval=0;
		}
		milis100 = mil + 12000;
		DEBUG("-HELLO ");
		DEBUGLN(String(mil));
	}
	if (Console0Complete) {
		parseInput( serial0Buffer );				      // parsuj wejscie
		Console0Complete = false;
		serialBuff_pos = 0;
		serial0Buffer = "";
	}
	for( byte i=0;i<MAINBOARD_BUFFER_LENGTH;i++){
		stepperX.run();
		if( input_buffer[i][0] ){
			proceed( buff_length[i],input_buffer[i] );
			input_buffer[i][0] = 0;
		}
	}
	stepperX.run();
}
/*
void scann_i2c(){
  byte nDevices=0;
  byte error=0;
  for(byte addr2 = 1; addr2 < 20; addr2++ )   {
    stepperX.run();
    Wire.beginTransmission(addr2);
    error = Wire.endTransmission();
    if (error == 0){
      DEBUG("-dev @");
      printHex(addr2, false );
      uint16_t readed = i2c_getVersion(addr2);
      DEBUG(" type: ");
      printHex( readed>>8, false );        // starsze bity = typ
      DEBUG(" ver: ");
      printHex( readed & 0xff, false );    // młodsze bity = ver
      DEBUGLN("");
      nDevices++;
    }else{
//     DEBUGLN("RET: "+String(addr2)+" / "+String(error));
    }
  }
}*/
void check_i2c(){
	Wire.beginTransmission(I2C_ADR_RESERVED);
	byte ee = Wire.endTransmission();     // czy linia jest drozna
	if(ee == 6 ){    // niedrozna - resetuj i2c
		reset_wire();
	}
}
void reset_wire(){
	sendln2android("RWIRE");
	//    pinMode(PIN_MAINBOARD_SDA, INPUT );
	//    pinMode(PIN_MAINBOARD_SCL, INPUT );
	Wire.begin(I2C_ADR_MAINBOARD);
	tri_state( PIN_PROGRAMMER_RESET_CARRET, false );		// pin w stanie niskim = reset
	tri_state( PIN_PROGRAMMER_RESET_UPANEL_FRONT, false );	// pin w stanie niskim = reset
	tri_state( PIN_PROGRAMMER_RESET_UPANEL_BACK, false );	// pin w stanie niskim = reset

	tri_state( PIN_PROGRAMMER_RESET_CARRET, true );			// pin w stanie niskim = reset
	tri_state( PIN_PROGRAMMER_RESET_UPANEL_FRONT, true );   // pin w stanie niskim = reset
	tri_state( PIN_PROGRAMMER_RESET_UPANEL_BACK, true );	// pin w stanie niskim = reset
}

void proceed( byte length,volatile uint8_t buffer[7] ){ // zrozum co przyszlo po i2c
	if(prog_mode){
		return;
	}
	if(buffer[0] == METHOD_HERE_I_AM){         //  here_i_am {METHOD_HERE_I_AM,my_address,type,ver}
		i2c_device_found( buffer[1], buffer[2], buffer[3] );
	}else if(buffer[0] == METHOD_IMPORTANT_ANALOG){      // wyslij do androida pozycje bo trafiono na górkę hallem
		if( buffer[1] == INNER_HALL_X){
			boolean stop_moving = false;// is moving up or down
			send2android("TX,");    // trigger
			send2android( String(buffer[2]) );                // reason
			send2android( "," );
			long int dis = stepperX.distanceToGo();
			if( dis < 0 && buffer[2] == HALL_GLOBAL_MIN ){    // moving down, min found
				stop_moving = true;
				stepperX.stopNow();
			}else if( dis > 0 && buffer[2] == HALL_GLOBAL_MAX){    // moving up, max found
				stop_moving = true;
				stepperX.stopNow();
			}
			if( dis > 0 ){
				send2android( String(DRIVER_DIR_FORWARD) );
			}else if( dis < 0 ){
				send2android( String(DRIVER_DIR_BACKWARD) );
			}else{
				send2android( String(DRIVER_DIR_STOP) );
			}
			send2android( "," );
			send2android( String(stepperX.currentPosition()) );
			send2androidEnd();
			if(stop_moving){
				send2android("Rx");
				send2android( String(stepperX.currentPosition()) );
				send2androidEnd();
			}
		}else{
			send2android(buffer,length);
			send2androidEnd();		
		}

	}else if(buffer[0] == METHOD_CAN_FILL ){
		byte res = read_can_fill();          // if 0 = ready
		if( res == 0 ){
			sendln2android("RCAN_FILL" );
		}else{                      // ERROR
			sendln2android("ERCAN_FILL" );
		}
	}else if(buffer[0] == METHOD_I2C_SLAVEMSG || buffer[0] == METHOD_EXEC_ERROR ){      // wyslij do androida
		send2android(buffer,length);
		send2androidEnd();
	}else{
		DEBUG("- no idea ");
		DEBUG(buffer[0]);
		DEBUG(" ");
		DEBUG(buffer[1]);
		DEBUG(" ");
		DEBUG(buffer[2]);
		DEBUG(" ");
		DEBUG(buffer[3]);
		DEBUG(" ");
		DEBUG(buffer[4]);
		DEBUG(" ");
		DEBUGLN(buffer[5]);
	}
	buffer[0] = 0;  //ready
}
 
void i2c_device_found( byte addr,byte type,byte ver ){
	byte ttt[4] = {METHOD_DEVICE_FOUND,addr,type,ver};
	send2android(ttt,4);
	send2androidEnd();
}

void parseInput( String input ){   // zrozum co przyszlo po serialu
	input.trim();
	boolean defaultResult = true;
	byte command = serialBuff[0];
	if( command == METHOD_SEND2SLAVE ){    // wyślij przez i2c do slave i spodziewaj się wyniku
		if(input.length() < 3 ){
			return;
		}
		byte count          = input.length() - 2;		// tyle do wysłania (COMAND + PARAMS)
		byte slave_address  = input.charAt( 1 );		// 0 = command, 1 = address, 2 = needs
		//byte command        = input.charAt( 2 );		// komenda do uzycia przez slave
		if(slave_address == I2C_ADR_MAINBOARD ){		// to do mnie
			volatile byte (*buffer) = 0;
		//	DEBUG("-input " );
			for( byte a = 0; a < MAINBOARD_BUFFER_LENGTH; a++ ){
				if(input_buffer[a][0] == 0 ){
					buffer = (&input_buffer[a][0]);
					for( byte b = 0; b < count; b++ ){
						*(buffer +b) = input.charAt( b + 2 );
					}
					buff_length[a] = count;
					return;
				}
			}
		}else{
			for( byte a = 0; a < count; a++ ){
				out_buffer[ a ]  = input.charAt( a + 2 );
			}
			byte error			= writeRegisters(slave_address, count, true );
			if( error ){
				writeRegisters(slave_address, count, true );	// powt�rz w razie czego
			}
		}

	}else if( input.startsWith("PROG_NEXT ")) {    // PROGN 0x0C,0,0   - programuj urzadzenie podlaczone resetem do 0x0C
		read_prog_settings(input, 2 );
		defaultResult = false;
		return;
	}else if( input.startsWith("PROG ")) {    	// PROG 1; PROG 2,0; PROG 3,1; PROG 4,1;   - programuj urzadzenie 0x0A z prędkosca 19200, PROG 0,0 - force first, PROG 0A,0 - wozek
		read_prog_settings(input, 1);
		defaultResult = false;
		return;

	}else if( input.startsWith("RESET ")) {    // RESET 0, RESET 1, RESET 2 
		String digits     = input.substring( 6 );
		char charBuf[10];
		digits.toCharArray(charBuf,10);
		unsigned int num    = 0;
		sscanf(charBuf,"%x", &num );
		delay(200);
		boolean ret = reset_device_num(num, LOW);
		if(ret){
			delay(1000);
			reset_device_num(num, HIGH);
		}else{  //error
			defaultResult = false;
			sendln2android("E " + input );
		}
	}else if( input.equals("RB")) {	// resetuj magistral� i2c
		reset_wire();

	}else if( input.startsWith("x")) {
		long int pos = stepperX.currentPosition();
		send2android("Rx"); 
		send2android(String(pos)); 
		send2androidEnd();
		defaultResult = false;
		
		//METHOD_GET_X_POS
		
	}else if( input.startsWith(METHOD_SET_X_ACCELERATION)) {    // AX10                  // ACCELERATION
		String ss 	  = input.substring( 2 );		// 10
		long unsigned val = decodeInt( ss, 0 );
		val = val * 100;
		stepperX.setAcceleration(val);
		DEBUGLN("-setAcceleration: " + String(val) );
	}else if( input.startsWith("X")) {    // X10,10,10              // TARGET,MAXSPEED
		String ss 		= input.substring( 1 );
		paserDeriver(DRIVER_X,ss);
		defaultResult = false;
	}else if( input.startsWith("Y")) {    // Y10,10                 // TARGET,ACCELERATION
		String ss 		= input.substring( 1 );		// 10,10
		paserDeriver(DRIVER_Y,ss);
		defaultResult = false;
	}else if( input.startsWith("Z")) {    // Z10,10                 // TARGET,ACCELERATION
		String ss 		= input.substring( 1 );		// 10,10
		paserDeriver(DRIVER_Z,ss);
		defaultResult = false;
	}else if( input.equals("y")) {    // pobierz pozycje
		out_buffer[0]  = METHOD_GET_Y_POS;
		writeRegisters(I2C_ADR_CARRET, 1, false );
		defaultResult = false;
	}else if( input.equals("z")) {    // pobierz pozycje
		out_buffer[0]  = METHOD_GET_Z_POS;
		writeRegisters(I2C_ADR_CARRET, 1, false );
		defaultResult = false;
	}else if( input.equals("EX") ){
		stepperX.enableOutputs();
		byte ttt[4] = {METHOD_I2C_SLAVEMSG, my_address, METHOD_DRIVER_ENABLE, DRIVER_X };
		send2android(ttt,4);
		send2androidEnd();
		defaultResult = false;	
	}else if( input.equals("DX") ){
		stepperX.disableOutputs();
		byte ttt[4] = {METHOD_I2C_SLAVEMSG, my_address, METHOD_DRIVER_ENABLE, DRIVER_X };
		send2android(ttt,4);
		send2androidEnd();
		defaultResult = false;
	}else if( input.equals("EY") ){
		out_buffer[0]  = METHOD_DRIVER_ENABLE;
		out_buffer[1]  = DRIVER_Y;
		writeRegisters(I2C_ADR_CARRET, 2, false );
		defaultResult = false;
	}else if( input.equals("EZ") ){
		out_buffer[0]  = METHOD_DRIVER_ENABLE;
		out_buffer[1]  = DRIVER_Z;
		writeRegisters(I2C_ADR_CARRET, 2, false );
		defaultResult = false;
	}else if( input.equals("DY") ){
		out_buffer[0]  = METHOD_DRIVER_DISABLE;
		out_buffer[1]  = DRIVER_Y;
		writeRegisters(I2C_ADR_CARRET, 2, false );
		defaultResult = false;
	}else if( input.equals("DZ") ){
		out_buffer[0]  = METHOD_DRIVER_DISABLE;
		out_buffer[1]  = DRIVER_Z;
		writeRegisters(I2C_ADR_CARRET, 2, false );
		defaultResult = false;
//	}else if( input.equals(METHOD_RESET_BUS) ){    // reset bus
//		get_order();
	}else if( input.equals("I2C") ){
		byte nDevices=0;
		byte error=0;
		for(byte addr2 = 1; addr2 < 20; addr2++ )   {
			Wire.beginTransmission(addr2);
			error = Wire.endTransmission();
			if (error == 0){
				uint16_t readed = i2c_getVersion(addr2);
				i2c_device_found(  addr2,(readed & 0xff),(readed>>8) );
				nDevices++;
			}
		}
		if( nDevices == 0 ){
			sendln2android("EI2C");
			defaultResult = false;
		}
/*
		send2debuger( "I2C PARAMS", ss );
		char const *c	= ss.c_str();				// lista parametrow, pierwszy to komenda
	}else if( input.equals("PING2ARDUINO") ){        // odeslij PONG
	}else if( input.equals( "PONG" )){			// nic, to byla odpowiedz na moje PING
		*/
	}else if( input.equals( "PING2ANDROID") ){      // nic nie rob
		defaultResult = false;
	}else if( input.equals( "WR") ){      // wait for return - tylko zwróc zwrotke
	}else{
		sendln2android("NO_CMD [" + input +"]");
		defaultResult = false;
	}
	if(defaultResult ){
		sendln2android("R" + input );
	}
}
byte read_can_fill(){
	out_buffer[0]  = METHOD_CAN_FILL;
	byte error = writeRegisters(I2C_ADR_CARRET, 1, true );
	if( error ){
		return error;
	}
	readRegisters( I2C_ADR_CARRET, 1 );
	return in_buffer[0]; // 0 jesli mozna, inna liczba jesli blad
}

union byteint{
    byte bytes[4];
    int i;
};

void stepperReady( long int pos ){
	sendln2android("Rx" + String(pos));
	out_buffer[0]  = METHOD_STEPPER_MOVING;           // wyslij do wozka ze jade
	out_buffer[1]  = DRIVER_X;
	out_buffer[2]  = DRIVER_DIR_STOP;
	writeRegisters(I2C_ADR_CARRET, 3, false );        // send to carret*/

	byteint value;
	value.i= pos;

	byte ttt[8] = {
		METHOD_I2C_SLAVEMSG,
		my_address, 
		RETURN_DRIVER_READY, 
		DRIVER_X, 
		value.bytes[3],				// bits 0-7
		value.bytes[2],				// bits 8-15
		value.bytes[1],				// bits 16-23
		value.bytes[0]				// bits 24-32
	};
	send2android(ttt,8);
	send2androidEnd();
}
void paserDeriver( byte driver, String input ){   // odczytaj komende silnika
	input.trim();
	int comma      = input.indexOf(',');
	long maxspeed  = 0;
	long target    = 0;
	if( comma == -1 ){      // tylko jedna komenda
		target          = decodeInt( input, 0 );
	}else{
		String current  = input.substring(0, comma);
		input           = input.substring(comma + 1 );    // wytnij od tego znaku
		target          = decodeInt( current, 0 );
		//DEBUGLN("-moveTo: " + String(target) );
		if( input.length() > 0 ){
			maxspeed       = decodeInt( input, 0 );
			//DEBUGLN("-setMaxSpeed: " + String(maxspeed) );
		}
	}
	 
	if( driver == DRIVER_X){
		if(maxspeed > 0){
			stepperX.setMaxSpeed(maxspeed);
		}
		stepperX.moveTo(target);
		stepperX.run();
		long int dis = stepperX.distanceToGo();
		if( dis != 0 ){
			out_buffer[0]  = METHOD_STEPPER_MOVING;
			out_buffer[1]  = DRIVER_X;
			out_buffer[2]  = ( dis > 0 ) ? DRIVER_DIR_FORWARD : DRIVER_DIR_BACKWARD;        // forward?
			writeRegisters(I2C_ADR_CARRET, 3, false );        // send to carret
		}
	}else if( maxspeed > 0 && driver == DRIVER_Y ){            // stepper Y
		out_buffer[0]  = METHOD_SET_Y_POS;
		out_buffer[1]  = target & 0xff;            // low byte
		out_buffer[2]  = (target >> 8) & 0xff;     // high byte
		out_buffer[3]  = maxspeed & 0xff;
		writeRegisters(I2C_ADR_CARRET, 4, false );
	}else if( maxspeed > 0 && driver == DRIVER_Z ){            // stepper Z
		out_buffer[0]  = METHOD_SET_Z_POS;
		out_buffer[1]  = target & 0xff;
		out_buffer[2]  = (target >> 8) & 0xff;
		out_buffer[3]  = maxspeed & 0xff;
		writeRegisters(I2C_ADR_CARRET, 4, false );
	}
}
 
long unsigned decodeInt(String input, int odetnij ){
	long pos = 0;
	if(odetnij>0){
		input = input.substring(odetnij);    // obetnij znaki z przodu
	}
	pos = input.toInt();
	return pos;
}
 
void tri_state( byte pin_num, boolean pin_value ){
	if( pin_value ){
		digitalWrite(pin_num, HIGH);       // HIGH value = run
		pinMode(pin_num, INPUT);
		digitalWrite( pin_num, 1);    // disable pullups
	}else{
		pinMode(pin_num, OUTPUT);
		digitalWrite(pin_num, LOW);        // LOW value = reset
	}
}
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
void i2c_test_slaves(){
	byte error;
	for(byte aaa = I2C_ADR_MAINBOARD; aaa < 20; aaa++ ) {
		Wire.beginTransmission(aaa);
		error = Wire.endTransmission();
		if (error == 0){
			uint16_t readed = i2c_getVersion(aaa);
			if( (readed>>8) > 0 && ((readed & 0xff) >0)){
				test_slave( aaa );
			}else{
				printHex( aaa, false );
				DEBUG("-! to nie jest urzadzenie i2c: ret 0x");
				printHex( (readed>>8), false );
				printHex( (readed & 0xff) );
			}
		} else if (error==4){
			DEBUG("-!Unknow error at address 0x");
			printHex(aaa );
		}else{
			//      DEBUG("-!error " +String(error)  +" at address 0x");
			//      printHex(aaa);
		}
	}
}

boolean reset_device_num( byte num, boolean pin_value ){
	if( num == 0x01 ){                        // master
		//tri_state( PIN_PROGRAMMER_RESET_MASTER, pin_value );		// to generalnie przerywa prace i resetuje procesor
		wdt_enable(WDTO_8S);
		while(1){
			pulse(PIN_PROGRAMMER_LED_ACTIVE, 2);
		};
	}else if( num == 0x02 ){                        // wozek
		tri_state( PIN_PROGRAMMER_RESET_CARRET, pin_value );
	}else if( num == 0x03 ){                  // pierwszy upanel
		tri_state( PIN_PROGRAMMER_RESET_UPANEL_BACK, pin_value );
	}else if( num == 0x04 ){                  // pierwszy upanel
		tri_state( PIN_PROGRAMMER_RESET_UPANEL_FRONT, pin_value );
	}else if( num == 0xff ){          // reset after last
		return false;
	}
	return false;
}
 
void read_prog_settings( String input, byte ns ){
	String digits         = input.substring( 5 );
	unsigned int target           = 0x00;
	unsigned int slow_sck         = 0;
	unsigned int serial_baud_num  = 0;
	char charBuf[10];
	digits.toCharArray(charBuf, 10);
	sscanf(charBuf,"%x,%x,%i", &target, &serial_baud_num, &slow_sck );
	if(ns == 1){            // PROG - podany numer 0,1,2 lub 3
		reprogramm_index    = target;
		reprogramm_address  = 0;
	}else if(ns == 2){     // PROG_NEXT, parametr to adres poprzedniego
		reprogramm_index    = 0;
		reprogramm_address  = target;
	}
	DEBUG("-ISP PROG START");
	DEBUGLN(reprogramm_index);
	programmer_mode(true, serial_baud_num, slow_sck );
}
 
void reset_device_next_to( byte slave_address, boolean pin_value ){
	if( pin_value ){                  // Koniec resetu urządzenia obok urządzenia adresowego, stan wysokiej impedancji na wyjściu
		out_buffer[0]  = METHOD_RUN_NEXT;
		writeRegisters(slave_address, 1, true );
	}else{	                    // Resetuj urządzenie obok urządzenia adresowego, stan niski na wyjściu resetuje tego obok
		out_buffer[0]  = METHOD_RESET_NEXT;
		writeRegisters(slave_address, 1, true );
	}
}

void i2c_analog( byte slave_address, byte analog ){
	out_buffer[0]  = METHOD_LIVE_ANALOG;
	out_buffer[1]  = analog;
	out_buffer[2]  = 15;
	out_buffer[3]  = 1;
	writeRegisters(slave_address, 4, false );
}
void i2c_analog_off( byte slave_address, byte analog ){
	out_buffer[0]  = METHOD_LIVE_OFF;
	writeRegisters(slave_address, 1, false );
}

uint16_t i2c_getVersion( byte slave_address ){      // zwraca 2 bajty. typ na młodszych bitach, versja na starszych
	out_buffer[0]  = METHOD_GETVERSION;
	byte error = writeRegisters(slave_address, 1, true );
	if( !error ){
		readRegisters( slave_address, 2 );
		uint16_t res = in_buffer[0];    // = typ na starszych bitach
		res = (res<<8) | in_buffer[1];    // = wersja na młodszych bitach
		return res;
	}
	return 0xFF;
}
 
void i2c_stop( byte slave_address ){      // zgaś wszystko
	out_buffer[0]  = 0xEE;
	writeRegisters(slave_address, 1, true );
	writeRegisters(slave_address, 1, true );
}
 
byte i2c_test_slave( byte slave_address, byte num1, byte num2 ){      // testuj
	out_buffer[0]  = METHOD_TEST_SLAVE;
	out_buffer[1]  = num1;
	out_buffer[2]  = num2;
	byte error = writeRegisters(slave_address, 3, true );
	if( error == 0 ){
		readRegisters( slave_address, 1 );
		return in_buffer[0];
	}
	return 0xFF;
}
 
unsigned int test_slave(byte slave_address){
	//  printHex( slave_address, false );
	//  DEBUGLN("- Test_slave start" );
	byte cntr1 = 5;
	const byte c2_max = 10;
	unsigned int cc = cntr1 * c2_max;
	byte res = 0;
	unsigned int errors= 0;
	while(--cntr1){
		byte cntr2 = c2_max;
		while(--cntr2){
			res = i2c_test_slave(slave_address, cntr1, cntr2);
			byte valid = cntr1 ^ cntr2;
			if(res !=valid){
				errors++;
				printHex( slave_address, false );
				DEBUGLN("- !!! zle "+ String(res) + " != " + String( valid ) );
			}
			//  delay(10);
		}
	}
	printHex( slave_address, false );
	DEBUGLN("- Test_slave (" + String(cc) + "): " + String(errors));
	return errors;
}
 
 
uint16_t i2c_getAnalogValue( byte slave_address, byte pin ){ // Pobierz analogowo wartość PIN o numerze ( 2 bajty )
	out_buffer[0]  = METHOD_GETANALOGVALUE;
	out_buffer[1]  = pin;
	byte error = writeRegisters( slave_address, 2, true );
	if(!error){
		readRegisters( slave_address, 2 );
		// number: 0xA1 0xB4
		// send order: 0xB4, 0xA1  (little-endian)
		// buffer[0]  = 0xB4
		// buffer[1]  = 0xA1
		uint16_t res = in_buffer[1];
		res = (res<<8) + in_buffer[0];
		return res;
	}
	return 0xFF;
}
 
// this is event handler, all vars should be volatile
void receiveEvent(int howMany){
	if(!howMany){
		return;
	}
	byte cnt = 0;
	volatile byte (*buffer) = 0;
	DEBUG("-input " );
	for( byte a = 0; a < MAINBOARD_BUFFER_LENGTH; a++ ){
		if(input_buffer[a][0] == 0 ){
			buffer = (&input_buffer[a][0]);
			while( Wire.available()){ // loop through all but the last
				byte w =  Wire.read(); // receive byte as a character
				*(buffer +(cnt++)) = w;
				DEBUG(w );
			}
			buff_length[a] = howMany;
			DEBUGLN("");
			return;
		}
	}
}
 
// Funkcja odczytywania N rejestrow
byte readRegisters(byte deviceAddress, byte length){
	Wire.requestFrom(deviceAddress, length);
	byte count = 0;
	byte waits = 100;
	while(Wire.available() == 0 && waits--) {
	}
	if(waits==0){
		if(!prog_mode){
			DEBUG("-niedoczekanie...");
		}
		return 0x32;
	}
	while (Wire.available()){
		byte d = Wire.read();
		//    DEBUG("READ:");
		//    printHex(d);
		in_buffer[count] = d;//Wire.read();
		count++;
	}
	if( count == length){
		last_i2c_read_error = false;
	}else{
		last_i2c_read_error = true;
		if(!prog_mode){
			DEBUGLN("-!Odebralem liczb:" + String(count) );
		}
	}
	return count;
}
 
// wysyla dowolną ilosc liczb na kanal
byte writeRegisters(int deviceAddress, byte length, boolean wait) {
	byte c = 0;
	Wire.beginTransmission(deviceAddress); // start transmission to device
	//    Wire.write(out_buffer, length);         // send value to write
	while( c < length ){
		Wire.write(out_buffer[c]);         // send value to write
		//      DEBUG("-Wysylam do " + String(deviceAddress) + ": ");
		//      printHex(out_buffer[c]);
		c++;
	}
	byte error = Wire.endTransmission();     // end transmission
	if( error && !prog_mode ){
	//	DEBUGLN("-! writeRegisters error: " + String(error) +"/"+ String(deviceAddress));
		byte ttt[5] = {RETURN_I2C_ERROR,my_address,deviceAddress, length, out_buffer[0]};
		send2android(ttt,5);
		delay(100);
		return error;
	}
	if(wait){
		delay(20);
	}
	//  DEBUGLN("-end writeRegisters");
	return 0;
}
void send2debuger( String ns, String logstr ){
	if(!prog_mode){
		DEBUG("-");
		DEBUG(ns);
		DEBUG(": [");
		DEBUG(logstr);
		DEBUGLN("]");
	}
}
void send2androidEnd(){      // wyslij string do androida
	Serial.println();
}
void sendln2android( String output2 ){      // wyslij string do androida
	Serial.println( output2 );
}
void send2android( uint8_t bits8 ){
	Serial.write(bits8);
}
void send2android( volatile uint8_t buffer[], int length ){
	//Serial.write(buf, len);
	Serial.print(buffer[0]);
	for (int i=1; i<length; i++) { 
		Serial.print(",");	
		Serial.print(buffer[i]); 
	}
	Serial.println();
}
void send2android( String output2 ){      // wyslij string do androida
	Serial.print( output2 );
}
void serialEvent(){				             // Runs after every LOOP (means don't run if loop hangs)
	if(!prog_mode){
		while (Serial.available() && !Console0Complete) {    // odczytuj gdy istnieja dane i poprzednie zostaly odczytane
			char inChar = (char)Serial.read();
			serial0Buffer += String(inChar);
			serialBuff[ serialBuff_pos++ ] = inChar;
			if (inChar == '\n') {
				Console0Complete = true;
			}
		}
	}
}
 
 
// These set the correct timing delays for programming a target with clk < 500KHz.
// Be sure to specify the lowest clock frequency of any target you plan to burn.
// The rule is the programing clock (on SCK) must be less than 1/4 the clock
// speed of the target (or 1/6 for speeds above 12MHz).  For example, if using
// the 128KHz RC oscillator, with a prescaler of 8, the target's clock frequency
// would be 16KHz, and the maximum programming clock would be 4KHz, or a clock
// period of 250uS.  The algorithm uses a quarter of the clock period for sync
// purposes, so QUARTER_PERIOD would be set to 63uS.  Be aware that internal RC
// oscillators can be off by as much as 10%, so you might have to force a slower
// clock speed.
#define MINIMUM_TARGET_CLOCK_SPEED 128000
#define SCK_FREQUENCY (MINIMUM_TARGET_CLOCK_SPEED/4)
//#define QUARTER_PERIOD ((1000000/SCK_FREQUENCY/4)+1)
#define QUARTER_PERIOD 50
 
typedef struct param {
	uint8_t devicecode;
	uint8_t revision;
	uint8_t progtype;
	uint8_t parmode;
	uint8_t polling;
	uint8_t selftimed;
	uint8_t lockbytes;
	uint8_t fusebytes;
	//  int flashpoll;
	//  int eeprompoll;
	int pagesize;
	//  int eepromsize;
	//  int flashsize;
} parameter;
parameter param;
 
void programmer_mode( boolean active, byte serial_baud_num, boolean slow_sck ) {
	if(active){
		prog_mode = true;
		// disable stepper
		stepperX.disableOutputs();

		if( serial_baud_num ){
			Serial.begin(115200);
		}else{
			Serial.begin(PROGRAMMER_SERIAL0_BOUND);
		}
		if (slow_sck){
			spi_init = sw_spi_init;
			spi_send = sw_spi_send;
		} else {
			spi_init = hw_spi_init;
			spi_send = hw_spi_send;
		}
		pulse(PIN_PROGRAMMER_LED_ACTIVE, 2);
		pulse(PIN_PROGRAMMER_LED_ERROR, 2);
		pulse(PIN_PROGRAMMER_LED_STATE, 2);
		while(prog_mode){
			digitalWrite(PIN_PROGRAMMER_LED_ERROR, !error);
			heartbeat();
			if (Serial.available()) {
				simulateisp();
			}
		}
	}else{
		digitalWrite(PIN_PROGRAMMER_LED_ACTIVE, HIGH);
		digitalWrite(PIN_PROGRAMMER_LED_ERROR, HIGH);
		digitalWrite(PIN_PROGRAMMER_LED_STATE, HIGH);
		pinMode(MISO, INPUT);
		pinMode(MOSI, INPUT);
		pinMode(SCK, INPUT);
		if(reprogramm_address){
			reset_device_next_to( reprogramm_address, HIGH);		
		}else if(reprogramm_index){
			reset_device_num( reprogramm_index, HIGH);
		}
		prog_mode = false;
		delay(2000);
		Serial.begin(MAINBOARD_SERIAL0_BOUND);
	}
}
void start_pmode() {
	spi_init();
	error = false;
	digitalWrite(PIN_PROGRAMMER_LED_ACTIVE, LOW);
	if(reprogramm_address){
		reset_device_next_to( reprogramm_address, LOW);
		reset_device_next_to( reprogramm_address, HIGH);
		reset_device_next_to( reprogramm_address, LOW);
	}else if(reprogramm_index){
		reset_device_num( reprogramm_index, LOW);
		reset_device_num( reprogramm_index, HIGH);
		reset_device_num( reprogramm_index, LOW);
	}
	pinMode(SCK, OUTPUT);
	digitalWrite(SCK, LOW);
	delay(50);
	if(reprogramm_address){
		reset_device_next_to( reprogramm_address, LOW);		
	}else if(reprogramm_index){
		reset_device_num( reprogramm_index, LOW);
	}
	
	delay(50);
	pinMode(MISO, INPUT);
	pinMode(MOSI, OUTPUT);
	spi_transaction(0xAC, 0x53, 0x00, 0x00);
}
 
void pulse(int pin, int times) {
	do {
		digitalWrite(pin, LOW);
		delay(PTIME);
		digitalWrite(pin, HIGH);
		delay(PTIME);
	}
	while (times--);
}
void heartbeat() {
	hbval += ( hbval + 12 % 150) + 35;
	analogWrite(PIN_PROGRAMMER_LED_STATE, hbval);
	delay(20);
}
 
uint8_t getch() {
	while(!Serial.available());
	return Serial.read();
}
void fill(int n) {
	for (int x = 0; x < n; x++) {
		serialBuff[x] = getch();
	}
}
 
uint8_t hw_spi_send(uint8_t b) {
	uint8_t reply;
	SPDR=b;
	spi_wait();
	reply = SPDR;
	return reply;
}
 
void hw_spi_init() {
	uint8_t x;
	SPCR = 0x53;
	x=SPSR;
	x=SPDR;
}
 
void spi_wait() {
	do {
	} while (!(SPSR & (1 << SPIF)));
}
 
uint8_t spi_transaction(uint8_t a, uint8_t b, uint8_t c, uint8_t d) {
	uint8_t n;
	spi_send(a);
	n=spi_send(b);
	n=spi_send(c);
	return spi_send(d);
}
 
void empty_reply() {
	if (CRC_EOP == getch()) {
		Serial.print((char)STK_INSYNC);
		Serial.print((char)STK_OK);
	} else {
		error = true;
		Serial.print((char)STK_NOSYNC);
	}
}
 
void breply(uint8_t b) {
	if (CRC_EOP == getch()) {
		Serial.print((char)STK_INSYNC);
		Serial.print((char)b);
		Serial.print((char)STK_OK);
	} else {
		error = true;
		Serial.print((char)STK_NOSYNC);
	}
}
 
uint8_t sw_spi_send(uint8_t b) {
	unsigned static char msk[] = { 0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01 };
	 
	 
	uint8_t reply=0;
	char bits[8] = { 0, 0, 0, 0, 0, 0, 0, 0 };
	 
	 
	for(uint8_t _bit = 0;_bit < 8;_bit++){
		digitalWrite(MOSI, !!(b & msk[_bit]));
		delayMicroseconds(QUARTER_PERIOD);
		digitalWrite(SCK, HIGH);
		delayMicroseconds(QUARTER_PERIOD);
		bits[_bit] = digitalRead(MISO);
		delayMicroseconds(QUARTER_PERIOD);
		digitalWrite(SCK, LOW);
		delayMicroseconds(QUARTER_PERIOD);
	}
	reply = bits[0] << 7 | bits[1] << 6 | bits[2] << 5 | bits[3] << 4 | bits[4] << 3 | bits[5] << 2 | bits[6] << 1 | bits[7];
	return reply;
}
void sw_spi_init() {
}
void get_version(uint8_t c) {
	switch(c) {
		case 0x80:
		breply(HWVER);
		break;
		case 0x81:
		breply(SWMAJ);
		break;
		case 0x82:
		breply(SWMIN);
		break;
		case 0x93:
		breply('S'); // serial programmer
		break;
		default:
		breply(0);
	}
}
 
void set_parameters() {
	// call this after reading paramter packet into serialBuff[]
	param.devicecode = serialBuff[0];
	param.revision   = serialBuff[1];
	param.progtype   = serialBuff[2];
	param.parmode    = serialBuff[3];
	param.polling    = serialBuff[4];
	param.selftimed  = serialBuff[5];
	param.lockbytes  = serialBuff[6];
	param.fusebytes  = serialBuff[7];
	//  param.flashpoll  = serialBuff[8];
	// ignore serialBuff[9] (= serialBuff[8])
	//  param.eeprompoll = beget16(&serialBuff[10]);
	param.pagesize   = beget16(&serialBuff[12]);
	//  param.eepromsize = beget16(&serialBuff[14]);
	 
	// 32 bits flashsize (big endian)
	//  param.flashsize = serialBuff[16] * 0x01000000+ serialBuff[17] * 0x00010000 + serialBuff[18] * 0x00000100 + serialBuff[19];
}
 
void universal(){
	uint8_t ch;
	fill(4);
	ch = spi_transaction(serialBuff[0], serialBuff[1], serialBuff[2], serialBuff[3]);
	breply(ch);
}
 
void flash(uint8_t hilo, int addr, uint8_t data) {
	spi_transaction(0x40+8*hilo,
	addr>>8 & 0xFF,
	addr & 0xFF,
	data);
}
 
void commit(int addr) {
	digitalWrite(PIN_PROGRAMMER_LED_ACTIVE,  !digitalRead(PIN_PROGRAMMER_LED_ACTIVE));    // Toggle led. Read from register (not from pin)
	spi_transaction(0x4C, (addr >> 8) & 0xFF, addr & 0xFF, 0);
}
 
int current_page(int addr) {
	if (param.pagesize == 32)  return here & 0xFFFFFFF0;
	if (param.pagesize == 64)  return here & 0xFFFFFFE0;
	if (param.pagesize == 128) return here & 0xFFFFFFC0;
	if (param.pagesize == 256) return here & 0xFFFFFF80;
	return here;
}
 
void write_flash(int length) {
	fill(length);
	if (CRC_EOP == getch()) {
		Serial.print((char) STK_INSYNC);
		Serial.print((char) write_flash_pages(length));
	}else {
		 
		 
		error = true;
		Serial.print((char) STK_NOSYNC);
	}
}
 
uint8_t write_flash_pages(int length) {
	int x = 0;
	int page = current_page(here);
	while (x < length) {
		if (page != current_page(here)) {
			commit(page);
			page = current_page(here);
		}
		flash(LOW, here, serialBuff[x++]);
		flash(HIGH, here, serialBuff[x++]);
		here++;
	}
	commit(page);
	return STK_OK;
}
 
uint8_t flash_read(uint8_t hilo, int addr) {
	return spi_transaction(0x20 + hilo * 8,
	(addr >> 8) & 0xFF,
	addr & 0xFF,
	0);
}
 
char flash_read_page(int length) {
	digitalWrite(PIN_PROGRAMMER_LED_ACTIVE,  !digitalRead(PIN_PROGRAMMER_LED_ACTIVE));    // Toggle led. Read from register (not from pin)
	for (int x = 0; x < length; x+=2) {
		uint8_t low = flash_read(LOW, here);
		Serial.print((char) low);
		uint8_t high = flash_read(HIGH, here);
		Serial.print((char) high);
		here++;
	}
	return STK_OK;
}
 
void read_page() {
	char result = (char)STK_FAILED;
	int length = 256 * getch();
	length += getch();
	char memtype = getch();
	if (CRC_EOP != getch()) {
		error=true;
		Serial.print((char) STK_NOSYNC);
		return;
	}
	Serial.print((char) STK_INSYNC);
	if (memtype == 'F') {
		result = flash_read_page(length);
	}
	Serial.print(result);
	return;
}
 
void program_page() {
	char result = (char) STK_FAILED;
	int length = 256 * getch();
	length += getch();
	char memtype = getch();
	if (memtype == 'F') {
		write_flash(length);
		return;
	}
	if (memtype == 'E') {
		result = STK_OK;
		if (CRC_EOP == getch()) {
			Serial.print((char) STK_INSYNC);
			Serial.print(result);
		} else {
			Serial.print((char) STK_NOSYNC);
			error = true;
		}
		return;
	}
	Serial.print((char)STK_FAILED);
	return;
}
 
void read_signature() {
	if (CRC_EOP != getch()) {
		error=true;
		Serial.print((char) STK_NOSYNC);
		return;
	}
	Serial.print((char) STK_INSYNC);
	uint8_t high = spi_transaction(0x30, 0x00, 0x00, 0x00);
	Serial.print((char) high);
	uint8_t middle = spi_transaction(0x30, 0x00, 0x01, 0x00);
	Serial.print((char) middle);
	uint8_t low = spi_transaction(0x30, 0x00, 0x02, 0x00);
	Serial.print((char) low);
	Serial.print((char) STK_OK);
}
 
void simulateisp() {
	uint8_t data, low, high;
	uint8_t ch = getch();
	switch (ch) {
		case '0': // signon
		error = false;
		empty_reply();
		break;
		case '1':
		if (getch() == CRC_EOP) {
			Serial.print((char) STK_INSYNC);
			Serial.print("AVR ISP");
			Serial.print((char) STK_OK);
		}
		break;
		case 'A':
		get_version(getch());
		break;
		case 'B':
		fill(20);
		set_parameters();
		empty_reply();
		break;
		case 'E':
		fill(5);
		empty_reply();
		break;
		case 'P':      //0x50
		start_pmode();
		empty_reply();
		break;
		case 'U': // set address (word)
		here = getch();
		here += 256 * getch();
		empty_reply();
		break;
		case 'Q': //0x51
		error=false;
		if (CRC_EOP == getch()) {
			Serial.print((char)STK_INSYNC);
			Serial.print((char)STK_OK);
		} else {
			error = true;
			Serial.print((char)STK_NOSYNC);
		}
		//    Serial.print((char) STK_INSYNC);  // 0x14
		//    Serial.print((char) STK_OK);  // 0x10
		programmer_mode(false, 0, false );
		break;
		case 'V': //0x56
		universal();
		break;
		case 0x60: //STK_PROG_FLASH
		low = getch();
		high = getch();
		empty_reply();
		break;
		case 0x61: //STK_PROG_DATA
		data = getch();
		empty_reply();
		break;
		case 0x64: //STK_PROG_PAGE
		program_page();
		break;
		case 0x74: //STK_READ_PAGE 't'
		read_page();
		break;
		case 0x75: //STK_READ_SIGN 'u'
		read_signature();
		break;
		case CRC_EOP:
		Serial.print((char) STK_NOSYNC);
		break;
		default:
		char r = STK_NOSYNC;
		if (CRC_EOP == getch()) {
			r = STK_UNKNOWN;
		}
		Serial.print(r);
	}
/*
  default:
    error=true;
    char r = STK_NOSYNC;
    if (CRC_EOP == getch()) {
        r = STK_UNKNOWN;
    }
    Serial.print(r);
}*/
}
 
