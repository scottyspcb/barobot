#define MAXCOMMAND 	10
#define IS_MAINBOARD true

#include "barobot_mainboard2_main.h" 
#include <barobot_common.h>
#include <Arduino.h>
#include <WSWire.h>
#include <constants.h>
#include <AsyncDriver.h>
#include <FlexiTimer2.h>

#define INBFLENGTH 	5
#define I2CDELAY	10

byte my_address = 0x00;
volatile uint8_t input_buffer[MAINBOARD_BUFFER_LENGTH][MAXCOMMAND] = {{0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0}};
volatile uint8_t buff_length[MAINBOARD_BUFFER_LENGTH] = {0,0,0};

byte out_buffer[7];

volatile boolean timer_now = false;
volatile unsigned int timer_counter = 1;

String serial0Buffer     = "";
volatile boolean stepperIsReady = false;
AsyncDriver stepperX( PIN_MAINBOARD_STEPPER_STEP, PIN_MAINBOARD_STEPPER_DIR, PIN_MAINBOARD_STEPPER_ENABLE );      // Step, DIR

inline void  setupStepper(){
	stepperX.disable_on_ready = true;
	stepperX.disableOutputs();
	stepperX.setAcceleration(MAINBOARD_ACCELERX);
	stepperX.setMaxSpeed(MAINBOARD_SPEEDX);
	stepperX.setOnReady(stepperReady);
	FlexiTimer2::set(1, 1.0/10000, timer);
	FlexiTimer2::start();
}
void setup(){
	pinMode(PIN_MAINBOARD_SCK, INPUT );
	pinMode(PIN_MAINBOARD_MISO, INPUT );
	pinMode(PIN_MAINBOARD_MOSI, INPUT );
	pinMode(PIN_MAINBOARD_TABLET_PWR, INPUT );	
	pinMode(SS, OUTPUT );	// needed by SPI do enable ISP
	setupStepper();
}


void timer(){
	timer_counter++;
	//timer_now = true;
	stepperX.run();
}

//uint16_t divisor = 500;
byteint bytepos;
long int next_time = 0;

void loop(){
/*
	long int b = millis();
	if( b > next_time){
		Serial.print("timer: ");
		Serial.println(timer_counter);
		next_time	= next_time + 1000;
		timer_counter = 0;
	}
*/
	//if(!timer_counter){
	//	divisor--;
	//	if(!divisor){
	//		timer_now	= false;
	//		divisor 	= 500;
	//		check_i2c();    // czy linia jest drozna?
	//		DEBUGLN("-HELLO");
	//		DEBUGLN(String(mil));
	//	}
	//}

	for( byte i=0;i<MAINBOARD_BUFFER_LENGTH;i++){
		if( input_buffer[i][0] ){
		//	proceed( buff_length[i],input_buffer[i] );
			input_buffer[i][0] = 0;
		}
	}
	if(stepperIsReady){
		long int pos = stepperX.currentPosition();
		sendStepperReady(pos);
		stepperIsReady = false;
	}
}

void parseInput( String input ){   // zrozum co przyszlo po serialu
//	Serial.println("-input1: " + input );
	input.trim();
	boolean defaultResult = true;
	byte command = input.charAt(0);
	byte il = input.length();

	if( command == METHOD_MSET_TOP_COLOR || command == METHOD_MSET_BOTTOM_COLOR ) {    // CAaRrGgBb		// set TOP /BOTTOM color for Aa to Rr Gg Bb
		String digits    	= input.substring( 1 );
		char charBuf[10];
		digits.toCharArray(charBuf,12);
		uint16_t address	= 0;
		uint16_t red		= 0;
		uint16_t green		= 0;
		uint16_t blue		= 0;
		sscanf(charBuf,"%2x%2x%2x%2x", &address, &red, &green, &blue );
		/*
		DEBUG("-adr: ");
		DEBUG(String(address));
		DEBUG(" r: ");	DEBUG(String(red));
		DEBUG(" g: ");	DEBUG(String(green));
		DEBUG(" b: ");	DEBUG(String(blue));
		DEBUG(" w: ");	DEBUG(String(white));
		DEBUGLN();*/
		out_buffer[0]  = ((command == METHOD_MSET_TOP_COLOR) ? METHOD_SET_TOP_COLOR : METHOD_SET_BOTTOM_COLOR);
		out_buffer[1]  = red;
		out_buffer[2]  = green;
		out_buffer[3]  = blue;
		writeRegisters(address, 5, false );
		delayMicroseconds(100);

	}else if(command == METHOD_MSET_LED || command == METHOD_M_ONECOLOR ) {    // L12,ff,211 or  B12,ff,211		// zgaœ wszystkie na upanelu 0x0C OR set color and disable other leds
		String digits     = input.substring( 1 );
		char charBuf[10];
		digits.toCharArray(charBuf,10);
		unsigned int num    = 0;
		unsigned int leds 	= 0;
		unsigned int power  = 0;
		sscanf(charBuf,"%i,%2x,%i", &num, &leds, &power );
		out_buffer[0]  = (command == METHOD_M_ONECOLOR) ? METHOD_ONECOLOR : METHOD_SETLEDS;
		out_buffer[1]  = leds;
		out_buffer[2]  = power;
		writeRegisters(num, 3, false );
		delayMicroseconds(100);

	}else if( command == 'x') {	//METHOD_GET_X_POS
		long int pos = stepperX.currentPosition();
		Serial.print("Rx"); 
		Serial.print(String(pos)); 
		Serial.println();
		Serial.flush();
		defaultResult = false;
	}else if( input.startsWith(METHOD_SET_X_ACCELERATION)) {    // AX10                  // ACCELERATION
		String ss 	  = input.substring( 2 );		// 10
		unsigned int val =ss.toInt();
		val = val * 100;
		stepperX.setAcceleration(val);
		DEBUGLN("-setAcceleration: " + String(val) );
	}else if(command == 'X' ) {    // X10,10              // TARGET,MAXSPEED
		String ss 		= input.substring( 1 );
		paserDeriver(ss);
		defaultResult = false;
	}else if( input.equals("EX") ) {    // enable
		stepperX.enableOutputs();
	}else if( input.equals("DX") ) {    // disable
		stepperX.disableOutputs();
	}else if( input.equals( "PING2ANDROID") ){      // nic nie rob
		defaultResult = false;
	}else if( input.equals( "PING") ){
		defaultResult = false;
		Serial.println("PONG");	

	}else if( command == METHOD_GET_TEMP ){  
		uint8_t tt = GetTemp();
		defaultResult = false;
		Serial.print("RT");
		Serial.println(String(tt));
		Serial.flush();
	}else if( input.equals( "WR") ){      // wait for return - tylko zwrÃ³c zwrotke
	}else{
		Serial.println("NO_CMD [" + input +"]");
		defaultResult = false;
	}
	if(defaultResult ){
		Serial.println("RR" + input );
		Serial.flush();
	}
}

void send_error( String input){
	Serial.print("E" );	
	Serial.println( input );
	Serial.flush();
}

void stepperReady( long int pos ){		// in interrupt
	stepperIsReady = true;
	//sendStepperReady(pos);
}

void sendStepperReady( long int pos ){		// in interrupt
	bytepos.i= pos;
	out_buffer[0]  = METHOD_STEPPER_MOVING;           // wyslij do wozka ze jade
	out_buffer[1]  = DRIVER_X;
	out_buffer[2]  = DRIVER_DIR_STOP;
	writeRegisters(I2C_ADR_CARRET, 3, false );        // send to carret
	Serial.print("RRx");
	Serial.println(String(pos));
}

void paserDeriver( String input ){   // odczytaj komende silnika
	int comma      = input.indexOf(',');
	long maxspeed  = 0;
	long target    = 0;
	if( comma == -1 ){      // tylko jedna komenda
		target          = input.toInt();
	//	unsigned int target           = 0;
	//	char charBuf[3];
	//	digits.toCharArray(charBuf, 3);
	//	sscanf(charBuf,"%i", &target );
	}else{
		String current  = input.substring(0, comma);
		target 			= current.toInt();
		input           = input.substring(comma + 1 );    // wytnij od tego znaku

	//	Serial.println("-current: " + current );
	//	Serial.println("-input2: " + input );
		if( input.length() > 0 ){
			maxspeed       = input.toInt();
	//		DEBUGLN("-setMaxSpeed: " + String(maxspeed) );
		}
	}
//	Serial.println("-target: " + String(target) );

	if(maxspeed > 0){
		stepperX.setMaxSpeed(maxspeed);
	}
	//long int tar = stepperX.targetPosition();	
	stepperX.moveTo(target);
//	Serial.println("-moveTo: " + String(stepperX.targetPosition()) );
	long int dis = stepperX.distanceToGo();
//	Serial.println("-distanceToGo: " + String(dis) );
	//tar = stepperX.targetPosition();
	//Serial.println("-tar2: " + String(tar) );
	if( dis != 0 ){
		out_buffer[0]  = METHOD_STEPPER_MOVING;
		out_buffer[1]  = DRIVER_X;
		out_buffer[2]  = ( dis > 0 ) ? DRIVER_DIR_FORWARD : DRIVER_DIR_BACKWARD;        // forward?
		writeRegisters(I2C_ADR_CARRET, 3, false );        // send to carret
	}
}

void writeRegisters(int deviceAddress, byte length, boolean wait) {
	Wire.beginTransmission(deviceAddress); // start transmission to device
	Wire.write(out_buffer, length);         // send value to write
	byte error = Wire.endTransmission();     // end transmission
	if( error ){
		Serial.print("ES,");
		Serial.print(String(deviceAddress));
		Serial.print(",");
		Serial.print(String(error));
		Serial.print(",");
		Serial.print(String(out_buffer[0]));
		Serial.flush();
	}else if(wait){
		delay(I2CDELAY);
	}
}

void serialEvent(){				             // Runs after every LOOP (means don't run if loop hangs)
	while (Serial.available()) {    // odczytuj gdy istnieja dane i poprzednie zostaly odczytane
		char inChar = (char)Serial.read();
		serial0Buffer += String(inChar);

//		String s = "["+ String(inChar)+"] ";
//		Serial.print(s);

		if (inChar == '\n') {
//			Serial.println();
		//	Console0Complete = true;
			parseInput( serial0Buffer );				      // parsuj wejscie
		//	Console0Complete = false;
			serial0Buffer = "";
		}
	}
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
