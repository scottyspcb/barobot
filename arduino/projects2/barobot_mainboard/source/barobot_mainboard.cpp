#include "barobot_mainboard_main.h" 
#define IS_MAINBOARD true
#define IS_PROGRAMMER true
#include <WSWire.h>
#include <barobot_common.h>
#include <constants.h>
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
volatile boolean timer_now = false;
volatile byte timer_counter = 1;
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
	pinMode(PIN_PROGRAMMER_RESET_UPANEL_FRONT, INPUT);
	pinMode(PIN_PROGRAMMER_RESET_UPANEL_BACK, INPUT);
	pinMode(PIN_PROGRAMMER_RESET_CARRET, INPUT);
	pinMode(PIN_PROGRAMMER_RESET_MASTER, INPUT);

	pinMode(PIN_MAINBOARD_SCK, INPUT );
	pinMode(PIN_MAINBOARD_MISO, INPUT );
	pinMode(PIN_MAINBOARD_MOSI, INPUT );

	pinMode(PIN_MAINBOARD_FREE_PIN, INPUT );
	pinMode(PIN_MAINBOARD_TABLET_PWR, INPUT );	

	pinMode(PIN_MAINBOARD_SDA, INPUT );
	pinMode(PIN_MAINBOARD_SCL, INPUT );
	pinMode(SS, OUTPUT );	// needed by SPI do enable ISP

	//disableWd();
	DEBUGINIT();
	DEBUGLN("-MSTART");

	setupStepper();

	pinMode(PIN_PROGRAMMER_LED_ACTIVE, OUTPUT);
	pinMode(PIN_PROGRAMMER_LED_ERROR, OUTPUT);
	pinMode(PIN_PROGRAMMER_LED_STATE, OUTPUT);

	digitalWrite( PIN_PROGRAMMER_LED_ACTIVE, LOW);
	digitalWrite( PIN_PROGRAMMER_LED_ERROR, LOW);
	digitalWrite( PIN_PROGRAMMER_LED_STATE, LOW);

	my_address	= I2C_ADR_MAINBOARD;
	Wire.begin(I2C_ADR_MAINBOARD);
	Wire.onReceive(receiveEvent);

	i2c_device_found(I2C_ADR_MAINBOARD, MAINBOARD_DEVICE_TYPE,MAINBOARD_VERSION);
}

#if MAINBOARD_SERVO_4PIN==true
AccelStepper stepperX(AccelStepper::HALF4WIRE, PIN_MAINBOARD_STEPPER_STEP0, PIN_MAINBOARD_STEPPER_STEP1, PIN_MAINBOARD_STEPPER_STEP2, PIN_MAINBOARD_STEPPER_STEP3 );
#else
AccelStepper stepperX(AccelStepper::DRIVER, PIN_MAINBOARD_STEPPER_STEP, PIN_MAINBOARD_STEPPER_DIR);      // Step, DIR
#endif

void setupStepper(){
	stepperX.disable_on_ready = true;
	stepperX.setDisablePin(PIN_MAINBOARD_STEPPER_ENABLE);
	stepperX.disableOutputs();
	stepperX.setAcceleration(MAINBOARD_ACCELERX);
	stepperX.setMaxSpeed(MAINBOARD_SPEEDX);
	stepperX.setOnReady(stepperReady);
	FlexiTimer2::set(1, 1.0/3000, timer);
	FlexiTimer2::start();
}

void timer(){
	timer_counter++;
	timer_now = true;
}

uint16_t divisor = 500;

void loop(){
	if(!timer_counter){
		divisor--;
		if(!divisor){
			timer_now	= false;
			divisor 	= 500;
	//		check_i2c();    // czy linia jest drozna?
			DEBUGLN("-HELLO");
	//		DEBUGLN(String(mil));


			// check tablet exists
			uint16_t tablet = analogRead( PIN_MAINBOARD_TABLET_PWR );
			if(tablet < 150 ){		// <1,5V = no tablet = disable drivers
				out_buffer[0]  = METHOD_DRIVER_DISABLE;
				out_buffer[1]  = DRIVER_Y;
				writeRegisters(I2C_ADR_CARRET, 2, true );
				DEBUGLN("-NO TABLET ");
				stepperX.stop();
				stepperX.disableOutputs();
				out_buffer[0]  = METHOD_DRIVER_DISABLE;
				out_buffer[1]  = DRIVER_Z;
				writeRegisters(I2C_ADR_CARRET, 2, true );
			}
		}
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
		DEBUGLN(buffer[4]);
	}
	buffer[0] = 0;  //ready
}

void parseInput( String input ){   // zrozum co przyszlo po serialu
	input.trim();
	boolean defaultResult = true;
	byte command = serialBuff[0];
	if( command == METHOD_SEND2SLAVE ){    // send over i2c to slave
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
			byte error			= writeRegisters(slave_address, count, false );
			if( error ){
				delay(20);
				writeRegisters(slave_address, count, false );	// powt�rz w razie czego
				delay(20);
			}
		}

	}else if( input.startsWith("W")) {
		String digits     = input.substring( 2 );
		char charBuf[16];
		digits.toCharArray(charBuf,16);
		unsigned int num  	= 0;
		unsigned int led  	= 0;
		unsigned int power  = 0;
		//sscanf(charBuf,"%i,%x,%i", &num, &led, &power );
		sscanf(charBuf,"%i", &num );
		DEBUG("-num: ");
		DEBUG(String(num));
		DEBUG(" led: ");
		DEBUG(String(led));
		DEBUG(" power: ");
		DEBUG(String(power));
		DEBUGLN();
		byte error =checkAddress(num);
		if(error != 0 ){
			out_buffer[0]  = METHOD_SETPWM;
			writeRegisters(num, 1, false );
		}

	}else if(command == METHOD_MSET_LED ) {    // L12,ff,211		// zga� wszystkie na upanelu 0x0C
		String digits     = input.substring( 1 );
		char charBuf[10];
		digits.toCharArray(charBuf,10);
		unsigned int num    = 0;
		unsigned int leds 	= 0;
		unsigned int power  = 0;
		sscanf(charBuf,"%i,%x,%i", &num, &leds, &power );
		out_buffer[0]  = METHOD_SETLEDS;
		out_buffer[1]  = leds;
		out_buffer[2]  = power;
		writeRegisters(num, 3, false );
		delay(2);

	}else if( command == 'x') {
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
	}else if(command == 'X' ) {    // X10,10,10              // TARGET,MAXSPEED
		String ss 		= input.substring( 1 );
		paserDeriver(DRIVER_X,ss);
		defaultResult = false;
	}else if( command == 'Y' ) {    // Y10,10                 // TARGET,ACCELERATION
		String ss 		= input.substring( 1 );		// 10,10
		paserDeriver(DRIVER_Y,ss);
		defaultResult = false;
	}else if(command == 'Z') {    // Z10,10                 // TARGET,ACCELERATION
		String ss 		= input.substring( 1 );		// 10,10
		paserDeriver(DRIVER_Z,ss);
		defaultResult = false;
	}else if(command == 'y' ) {    // pobierz pozycje
		out_buffer[0]  = METHOD_GET_Y_POS;
		writeRegisters(I2C_ADR_CARRET, 1, false );
		defaultResult = false;
	}else if( command == 'z' ) {    // pobierz pozycje
		out_buffer[0]  = METHOD_GET_Z_POS;
		writeRegisters(I2C_ADR_CARRET, 1, false );
		defaultResult = false;

	}else if( command == 'E' ) {    // enable
		byte command2 = serialBuff[1];
		defaultResult = false;
		if( command2 == 'X' ){
			stepperX.enableOutputs();
			byte ttt[4] = {METHOD_I2C_SLAVEMSG, my_address, METHOD_DRIVER_ENABLE, DRIVER_X };
			send2android(ttt,4);
			send2androidEnd();
		}else if( command2 == 'Y' ){
			out_buffer[0]  = METHOD_DRIVER_ENABLE;
			out_buffer[1]  = DRIVER_Y;
			writeRegisters(I2C_ADR_CARRET, 2, false );
		}else if( command2 == 'Z' ){
			out_buffer[0]  = METHOD_DRIVER_ENABLE;
			out_buffer[1]  = DRIVER_Z;
			writeRegisters(I2C_ADR_CARRET, 2, false );
		}
	}else if( command == 'D' ) {    // disable
		byte command2 = serialBuff[1];
		defaultResult = false;
		if( command2 == 'X' ){
			stepperX.disableOutputs();
			byte ttt[4] = {METHOD_I2C_SLAVEMSG, my_address, METHOD_DRIVER_ENABLE, DRIVER_X };
			send2android(ttt,4);
			send2androidEnd();
		}else if( command2 == 'Y' ){
			out_buffer[0]  = METHOD_DRIVER_DISABLE;
			out_buffer[1]  = DRIVER_Y;
			writeRegisters(I2C_ADR_CARRET, 2, false );
		}else if( command2 == 'Z' ){
			out_buffer[0]  = METHOD_DRIVER_DISABLE;
			out_buffer[1]  = DRIVER_Z;
			writeRegisters(I2C_ADR_CARRET, 2, false );
		}

	}else if( input.equals( "PING2ANDROID") ){      // nic nie rob
		defaultResult = false;
		
	}else if( input.equals( "PING") ){
		defaultResult = false;
		sendln2android("PONG");	
	}else if( command == 'p' ) {    // p10,0,0   (prog next to)- programuj urzadzenie podlaczone resetem do urzadzenia o adresie 10
		read_prog_settings(input, 2 );
		defaultResult = false;
	}else if(  command == 'P' ) {    	// P1; P2,0; P3,1; P4,1;   - programuj urzadzenie 0x0A z predkosca 19200, PROG 0,0 - force first, PROG 0A,0 - wozek
		read_prog_settings(input, 1 );
		defaultResult = false;
	}else if( input.startsWith("RESET_NEXT")) {			// RESET12
		String digits     = input.substring( 10 );
		char charBuf[4];
		digits.toCharArray(charBuf,4);
		unsigned int num    = 0;
		sscanf(charBuf,"%i", &num );
		byte error =reset_device_next_to(num, LOW);
		if (error == 0){
			delay(20);
			reset_device_next_to(num, HIGH);
		}else{  //error
			defaultResult = false;
			send_error(input);
		}
	}else if( input.startsWith("RESET")) {    // RESET0; RESET1; RESET2 
		String digits     = input.substring( 5 );
		char charBuf[10];
		digits.toCharArray(charBuf,10);
		unsigned int num    = 0;
		sscanf(charBuf,"%i", &num );
		boolean ret = reset_device_num(num, LOW);
		if(ret){
			delay(500);
			reset_device_num(num, HIGH);
		}else{  //error
			defaultResult = false;
			send_error(input);
		}
	}else if( command == 'H' ){		// HAS NEXT, H2,H3,H4
		String digits         		  = input.substring( 1 );
		unsigned int target           = 0;
		char charBuf[3];
		digits.toCharArray(charBuf, 3);
		sscanf(charBuf,"%i", &target );
		defaultResult = false;
		byte pin		= get_local_pin(target);
		if(pin == 0xff){
			send_error(input);
		}else{
			boolean value	= digitalRead( pin);			// is sht is connected
			byte ttt[4]		= {METHOD_I2C_SLAVEMSG, my_address, METHOD_CHECK_NEXT, value };
			send2android(ttt,4);
			send2androidEnd();
		}

	}else if( command == 'h' ){			// h10,h11,h12,13   - id selected i2c device has next node
		String digits         		  = input.substring( 1 );
		unsigned int target           = 0;
		char charBuf[3];
		digits.toCharArray(charBuf, 3);
		sscanf(charBuf,"%i", &target );
		defaultResult = false;
		byte adr 		= (byte) target;
		out_buffer[0] 	= METHOD_CHECK_NEXT;
		byte error		= writeRegisters(adr, 1, true );
		if (error != 0){
			send_error(input);
			return;
		}

	}else if( command == 'I' ){
		byte nDevices=0;
		byte error=0;
		for(byte addr2 = 1; addr2 < I2C_ADR_UEND; addr2++ )   {
			error =checkAddress(addr2);
			if (error == 0){
				delay(20);
				uint16_t readed = i2c_getVersion(addr2);
				i2c_device_found( addr2,(readed & 0xff),(readed>>8) );
				nDevices++;
			}
		}
		if( nDevices == 0 ){
			send_error(input);
			defaultResult = false;
		}
/*
		send2debuger( "I2C PARAMS", ss );
		char const *c	= ss.c_str();				// lista parametrow, pierwszy to komenda
	}else if( input.equals("PING2ARDUINO") ){        // odeslij PONG
	}else if( input.equals( "PONG" )){			// nic, to byla odpowiedz na moje PING
		*/
	}else if( input.equals("RB")) {	// resetuj magistral� i2c
		reset_wire();
	}else if( input.equals("RB2")) {	// resetuj magistral� i2c
		reset_wire2();
	}else if( input.equals( "TEST") ){
		i2c_test_slaves();		
	}else if( command == METHOD_GET_TEMP ){  
		uint8_t tt = GetTemp();
		defaultResult = false;
		send2android("RT");
		sendln2android(String(tt));
	}else if( input.equals( "WR") ){      // wait for return - tylko zwróc zwrotke
	}else{
		sendln2android("NO_CMD [" + input +"]");
		defaultResult = false;
	}
	if(defaultResult ){
		sendln2android("R" + input );
	}
}

void i2c_device_found( byte addr,byte type,byte ver ){
	byte ttt[4] = {METHOD_DEVICE_FOUND,addr,type,ver};
	send2android(ttt,4);
	send2androidEnd();
}

void send_error( String input){
	send2android("E" );	
	sendln2android( input );
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

void stepperReady( long int pos ){
	//sendln2android("Rx" + String(pos));
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
	for(byte aaa = I2C_ADR_MAINBOARD; aaa < I2C_ADR_UEND; aaa++ ) {
		error =checkAddress(aaa);
		if (error == 0){
			delay(20);
			uint16_t readed = i2c_getVersion(aaa);
			if( (readed>>8) > 0 && ((readed & 0xff) >0)){
			}else{
				printHex( aaa, false );
				DEBUG("-! to nie jest urzadzenie i2c: ret 0x");
				printHex( (readed>>8), false );
				printHex( (readed & 0xff) );
			}
			test_slave( aaa, 15 );
		} else if (error==4){
			DEBUG("-!Unknow error at address 0x");
			printHex(aaa );
		}else{
			//      DEBUG("-!error " +String(error)  +" at address 0x");
			//      printHex(aaa);
		}
	}
}

byte get_local_pin( byte index ){
	if( index == 0x01 ){
		return PIN_PROGRAMMER_RESET_MASTER;
	}else if( index == 0x02 ){
		return PIN_PROGRAMMER_RESET_CARRET;
	}else if( index == 0x03 ){ 
		return PIN_PROGRAMMER_RESET_UPANEL_BACK;
	}else if( index == 0x04 ){
		return PIN_PROGRAMMER_RESET_UPANEL_FRONT;
	}
	return 0xff;
}

boolean reset_device_num( byte num, boolean pin_value ){
	byte pin = get_local_pin(num);
	if( pin == 0xff ){
		return false;
	}
	tri_state( pin, pin_value );
	return true;
}

void read_prog_settings( String input, byte ns ){
	String digits         		  = input.substring( 1 );
	unsigned int target           = 0x00;
	unsigned int slow_sck         = 0;
	long unsigned int serial_baud_num  = 0;
	char charBuf[10];
	digits.toCharArray(charBuf, 10);
	sscanf(charBuf,"%i,%li,%i", &target, &serial_baud_num, &slow_sck );

	if(ns == 1){            // PROG - podany numer 1,2 lub 3
		DEBUGLN("SISP");
		reprogramm_index    = target;
		reprogramm_address  = 0;
	}else if(ns == 2){     // PROG_NEXT, parametr to adres poprzedniego
		DEBUGLN("SISP");
		reprogramm_index    = 0;
		reprogramm_address  = (byte) target;
		byte error =checkAddress(reprogramm_address);
		if (error != 0){
			send_error(input);
			return;
		}
	}
	//DEBUG(" BAUD: ");
	//DEBUG(String( serial_baud_num));
	//DEBUG(" SSCK: " );
	//DEBUG(String( slow_sck));
	//DEBUGLN();
	Serial.flush();

	send_prog_mode(METHOD_PROG_MODE_ON);
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
}

void send_prog_mode( byte command ){
	byte ee =checkAddress(I2C_ADR_RESERVED);
	if(ee == 6 ){
	}else{		// drozna
		out_buffer[0]  = command;
		out_buffer[1]  = reprogramm_address;
		for(byte addr2 = 2; addr2 < I2C_ADR_UEND; addr2++ )   {
			ee =checkAddress(addr2);
			if(ee == 0){
				writeRegisters(addr2, 2, true );	
			}
		}
	}
} 
 
byte checkAddress( byte address ){
	Wire.beginTransmission(address);
	return Wire.endTransmission();
}

byte reset_device_next_to( byte slave_address, boolean pin_value ){
	if( pin_value ){                  // Koniec resetu urządzenia obok urządzenia adresowego, stan wysokiej impedancji na wyjściu
		out_buffer[0]  = METHOD_RUN_NEXT;
	}else{	                    // Resetuj urządzenie obok urządzenia adresowego, stan niski na wyjściu resetuje tego obok
		out_buffer[0]  = METHOD_RESET_NEXT;
	}
	return writeRegisters(slave_address, 1, false );
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
 
void i2c_stop( byte slave_address ){      // zgas wszystko
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

void test_slave(byte slave_address, byte tests){
	const byte c2_max = 10;
	byte res = 0;
	byteint cc, errors;
	cc.i= tests * c2_max;
	errors.i= 0;
	while(--tests){
		byte cntr2 = c2_max;
		while(--cntr2){
			res = i2c_test_slave(slave_address, tests, cntr2);
			byte valid = tests ^ cntr2;
			if(res !=valid){
				errors.i++;
				DEBUG( slave_address );
				DEBUGLN("- !!! zle "+ String(res) + " != " + String( valid ) );
			}
			//  delay(10);
		}
	}
	byte ttt[7] = {
		METHOD_I2C_SLAVEMSG,
		my_address,
		slave_address,
		errors.bytes[1],
		errors.bytes[0],
		cc.bytes[1],
		cc.bytes[0],
	};
	send2android(ttt,7);
	send2androidEnd();
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
#define DEBUG_SEND2	false

void receiveEvent(int howMany){
	if(!howMany){
		return;
	}
	byte cnt = 0;
	volatile byte (*buffer) = 0;
	#if DEBUG_SEND2
	if(!prog_mode){
		DEBUG("-input " );	
	}
	#endif
	for( byte a = 0; a < MAINBOARD_BUFFER_LENGTH; a++ ){
		if(input_buffer[a][0] == 0 ){
			buffer = (&input_buffer[a][0]);
			while( Wire.available()){ // loop through all but the last
				byte w =  Wire.read(); // receive byte as a character
				*(buffer +(cnt++)) = w;
				#if DEBUG_SEND2
					if(!prog_mode){
						DEBUG(w );
					}
				#endif
			}
			buff_length[a] = howMany;
			#if DEBUG_SEND2
				if(!prog_mode){
					DEBUGLN();
				}
			#endif
			return;
		}
	}
}
// Funkcja odczytywania N rejestrow
byte readRegisters(byte deviceAddress, byte length){
	Wire.requestFrom(deviceAddress, length);
	byte count = 0;
	/*
	byte waits = 100;
	while(Wire.available() == 0 && waits--) {
	}
	if(waits==0){
		if(!prog_mode){
			DEBUG("-niedoczekanie...");
		}
		return 0x32;
	}*/
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
#define DEBUG_SEND	false

byte writeRegisters(int deviceAddress, byte length, boolean wait) {
	Wire.beginTransmission(deviceAddress); // start transmission to device

	#if DEBUG_SEND
		if(!prog_mode){
			DEBUG("-Sending to " + String(deviceAddress) + ": ");
		}
		byte c = 0;
		while( c < length ){
			Wire.write(out_buffer[c]);         // send value to write
			if(!prog_mode){
				printHex(out_buffer[c]);
			}
			c++;
		}
		//if(!prog_mode){
		//	DEBUGLN("-end writeRegisters");
		//}
    #else
		Wire.write(out_buffer, length);         // send value to write
    #endif

	byte error = Wire.endTransmission();     // end transmission
	if( error && !prog_mode ){
		DEBUGLN("-! writeRegisters error: " + String(error) +"/"+ String(deviceAddress));
		byte ttt[6] = {RETURN_I2C_ERROR,my_address,deviceAddress, error, length, out_buffer[0]};
		send2android(ttt,6);
		send2androidEnd();
		return error;
	}
	if(wait){
		delay(20);
	}
	return 0;
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

void check_i2c(){ // czy linia jest drozna
	byte ee =checkAddress(I2C_ADR_RESERVED);
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
void reset_wire2(){
	sendln2android("RWIRE2");

	pinMode(PIN_MAINBOARD_SCK, OUTPUT);
	pinMode(PIN_MAINBOARD_MISO, OUTPUT);
	pinMode(PIN_MAINBOARD_MOSI, OUTPUT);
	digitalWrite( PIN_MAINBOARD_SCK, HIGH );
	digitalWrite( PIN_MAINBOARD_MISO, HIGH );
	digitalWrite( PIN_MAINBOARD_MOSI, HIGH );
	delay(100);

	tri_state( PIN_PROGRAMMER_RESET_CARRET, false );		// pin w stanie niskim = reset
	tri_state( PIN_PROGRAMMER_RESET_UPANEL_FRONT, false );	// pin w stanie niskim = reset
	tri_state( PIN_PROGRAMMER_RESET_UPANEL_BACK, false );	// pin w stanie niskim = reset
	tri_state( PIN_PROGRAMMER_RESET_MASTER, false );		// resetuje maszyn�, odbiera sterowanie
	
}

uint8_t GetTemp(){
  // The internal temperature has to be used
  // with the internal reference of 1.1V.
  // Channel 8 can not be selected with
  // the analogRead function yet.
  // Set the internal reference and mux.
//  ADMUX = (_BV(REFS1) | _BV(REFS0) | _BV(MUX3));
  ADMUX = 0;
  ADMUX |= _BV(REFS1);
  ADMUX |= _BV(REFS0);
  ADMUX |= 8;
  ADCSRA |= _BV(ADEN);  // enable the ADC
  delay(20);            // wait for voltages to become stable.
  ADCSRA |= _BV(ADSC);  // Start the ADC
  // Detect end-of-conversion
  while (bit_is_set(ADCSRA,ADSC));
  // Reading register "ADCW" takes care of how to read ADCL and ADCH.
  return ADCW;
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

void end_programmer_mode(){
	digitalWrite(PIN_PROGRAMMER_LED_ACTIVE, LOW );
	digitalWrite(PIN_PROGRAMMER_LED_ERROR, LOW );
	digitalWrite(PIN_PROGRAMMER_LED_STATE, LOW );

	pinMode(MISO, INPUT);
	pinMode(MOSI, INPUT);
	pinMode(SCK, INPUT);
	prog_mode = false;
	Serial.flush();
	Serial.begin(MAINBOARD_SERIAL0_BOUND);

	send_prog_mode(METHOD_PROG_MODE_OFF);

	if(reprogramm_address){		// unlock device
		reset_device_next_to( reprogramm_address, HIGH);
	}else if(reprogramm_index){
		reset_device_num( reprogramm_index, HIGH);
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
	}else{
		Serial.print("!!!");
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
		Serial.print( (char)STK_INSYNC );
		Serial.print( (char)STK_OK );
	} else {
		error = true;
		Serial.print( (char)STK_NOSYNC );
	}
}
 
void breply(uint8_t b) {
	if (CRC_EOP == getch()) {
		Serial.print( (char)STK_INSYNC );
		Serial.print( (char)b );
		Serial.print( (char)STK_OK );
	} else {
		error = true;
		Serial.print( (char)STK_NOSYNC );
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
	spi_transaction(0x40+8*hilo,addr>>8 & 0xFF,addr & 0xFF,data);
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
			end_programmer_mode();
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
 
/*
void scann_i2c(){
  byte nDevices=0;
  byte error=0;
  for(byte addr2 = 1; addr2 < 20; addr2++ )   {
    stepperX.run();
   error =checkAddress(I2C_ADR_RESERVED);
    delay(20);
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