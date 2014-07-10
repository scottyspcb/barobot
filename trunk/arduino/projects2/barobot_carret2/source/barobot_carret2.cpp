#include "barobot_carret2_main.h"
#include "constants.h"
#include <Adafruit_NeoPixel.h>
#include <Arduino.h>
#include <barobot_common.h>
#include <Servo.h>
#include <constants.h>
#include <AsyncDriver.h>
#include <FlexiTimer2.h>

// PINS
#define PIN_B2_STEPPER_ENABLE PPD3	// 3
#define PIN_B2_STEPPER_STEP PPD4	// 4
#define PIN_B2_STEPPER_DIR PPD5		// 5
#define PIN_B2_SERVO_Y PPB2			// 10
#define PIN_B2_SERVO_Z PPB1			// 9
#define PIN_B2_LED_TOP PPD7			// 7
#define PIN_B2_LED_BOTTOM PPB0		// 8

#define PIN_B2_HALL_X A3			// PPC3, 17
#define PIN_B2_HALL_Y A2			// PPC2, 16
#define PIN_B2_WEIGHT A0			// PPC0, 14
#define PIN_B2_TABLET_PWR A1		// 15


// Config
#define ANALOGS  6
#define ANALOG_TRIES  4
#define B2_ACCELERX 9000
#define B2_SPEEDX 2500


volatile boolean stepperIsReady = false;
volatile uint16_t checks = 0;
//									  {0,1,2,3,4,5}
volatile int8_t ADCport[ANALOGS]	= {3,5,6,7,0,2};
volatile int16_t ADCvalue[ANALOG_TRIES][ANALOGS] = {{0,0,0,0,0,0},{0,0,0,0,0,0},{0,0,0,0,0,0},{0,0,0,0,0,0}};
volatile uint8_t channel = 0;
volatile uint8_t row = 0;
volatile unsigned int timer_counter = 1;
byteint bytepos;
#define MIN_DELTA  20

volatile unsigned int ticks  = 0;

struct ServoChannel {
	uint8_t pin;
	int16_t delta_pos;
	int16_t target_pos;
	int16_t last_pos;
	int16_t last_distance;
	volatile boolean pos_changed;
	volatile boolean enabled;
	uint8_t moving;
};

Servo servo_lib[2];
volatile ServoChannel servos[2]= {
	{PIN_B2_SERVO_Y,0,0,0,0,false,false,DRIVER_DIR_STOP },
	{PIN_B2_SERVO_Z,0,0,0,0,false,false,DRIVER_DIR_STOP },
};

unsigned long mil = 0;

uint16_t cc = 0;
byte state_id = 0xff;
int16_t up_level = 0;
int16_t down_level = 0;

volatile int16_t hallx_state[HXSTATES][4] = {
	//{CODE, MIN, MAX }
	{HX_STATE_0,	HX_NEODYM_UP_BELOW,			1024,						0x0f},		// ERROR
	{HX_STATE_1,	HX_NEODYM_UP_START,			HX_NEODYM_UP_BELOW-1,		0x08},		// to jest neodym max
	{HX_STATE_2,	HX_FERRITE_UP_IS_BELOW,		HX_NEODYM_UP_START-1,		0x04},		// wznosi siê neodym
	{HX_STATE_3,	HX_LOCAL_UP_MAX_OVER,		HX_FERRITE_UP_IS_BELOW-1,	0x02},		// czubek lokalnego max
	{HX_STATE_4,	HX_NOISE_BELOW,				HX_LOCAL_UP_MAX_OVER-1,		0x01},		// wznosi siê

	{HX_STATE_5,	HX_NOISE_OVER,				HX_NOISE_BELOW-1,			0x00},		// neutralne

	{HX_STATE_6,	HX_LOCAL_DOWN_IS_BELOW,		HX_NOISE_OVER-1,			0x10},		// opada
	{HX_STATE_7,	HX_FERRITE_DOWN_IS_BELOW,	HX_LOCAL_DOWN_IS_BELOW-1,	0x20},		// czubek lokalnego min
	{HX_STATE_8,	HX_NEODYM_DOWN_START,		HX_FERRITE_DOWN_IS_BELOW-1,	0x40},		// opada neodym
	{HX_STATE_9,	HX_NEODYM_DOWN_OVER,		HX_NEODYM_DOWN_START-1,		0x80},		// to jest neodym min	
	{HX_STATE_10,	0,							HX_NEODYM_DOWN_OVER-1,		0xf0}		// NOT CONNECTED	
};

#define HYSTERESIS  2
#define SEPARATOR_CHAR '\n'
#define TRIES  10

volatile int16_t hally_state[HYSTATES][4] = {
	//{CODE, MIN, MAX  }
	{'E',	1024,	1024,		0x0f},		// ERROR
	{'R',	550,	1024-1,		0x04},		// neodym +
	{'A',	450,	550-1,		0x02},		// normal
	{'B',	1,		450-1,		0x01},		// neodym -
	{'N',	0,		0,			0xf0}		// NOT CONNECTED	
};

String serial0Buffer = "";
unsigned long int when_next = 0;
unsigned long int sending = 0b00000000;
unsigned long int time = 5000;
unsigned long int sum = 0;
unsigned long int repeat = 0;

AsyncDriver stepperX( PIN_B2_STEPPER_STEP, PIN_B2_STEPPER_DIR, PIN_B2_STEPPER_ENABLE );      // Step, DIR

Adafruit_NeoPixel top_panels = Adafruit_NeoPixel(50, PIN_B2_LED_BOTTOM, NEO_GRB + NEO_KHZ800);
Adafruit_NeoPixel bottom_panels = Adafruit_NeoPixel(1, PIN_B2_LED_TOP,  NEO_GRB + NEO_KHZ800);

inline void  setupStepper(){
	stepperX.disable_on_ready = true;
	stepperX.disableOutputs();
	stepperX.setAcceleration(B2_ACCELERX);
	stepperX.setMaxSpeed(B2_SPEEDX);
	stepperX.setOnReady(stepperReady);
	FlexiTimer2::set(1, 1.0/10000, timer);
	FlexiTimer2::start();
}

void setup(){
	pinMode(PIN_B2_TABLET_PWR, INPUT );	

	pinMode(PIN_B2_SERVO_Y, INPUT );      // nie pozwalaj na przypadkowe machanie na starcie
	pinMode(PIN_B2_SERVO_Z, INPUT );      // nie pozwalaj na przypadkowe machanie na starcie

	pinMode(PIN_B2_HALL_X, INPUT);
	pinMode(PIN_B2_HALL_Y, INPUT);
	pinMode(PIN_B2_WEIGHT, INPUT);

	//pinMode(PIN_CARRET_CURRENTY, INPUT);
	//pinMode(PIN_CARRET_CURRENTZ, INPUT);

	init_leds();
	serial0Buffer = "";

	//init_analogs();
	init_hallx();
	//sendstats();

	//pinMode(SS, OUTPUT );	// needed by SPI do enable ISP
	setupStepper();
}

void init_leds(){
	top_panels.begin();
	top_panels.show(); // Initialize all pixels to 'off'
	bottom_panels.begin();
	bottom_panels.show(); // Initialize all pixels to 'off'
}

void sendVal( byte n ) {
  /*
  unsigned int value = ADCvalue[0][n];
  if( value > 0 ){
    for( byte i=1; i<=sum;i++){
      value+= ADCvalue[i][n];
    }
  } */
  int value =  analogRead( A0 + n ); 
  Serial.print( value );  
  Serial.print(",");  
}

void loop() {
	mil = millis();

  if( mil > when_next ){    // debug, mrygaj co 1 sek
		if( bitRead(sending, 0 ) ){  sendVal(0);}
		if( bitRead(sending, 1 ) ){  sendVal(1);}
		if( bitRead(sending, 2 ) ){  sendVal(2);}
		if( bitRead(sending, 3 ) ){  sendVal(3);}
		if( bitRead(sending, 4 ) ){  sendVal(4);}
		if( bitRead(sending, 5 ) ){  sendVal(5);}
		if( bitRead(sending, 6 ) ){  sendVal(6);}
		if( bitRead(sending, 7 ) ){  sendVal(7);}
		if( bitRead(sending, 8 ) ){  sendVal(8);}
		if(sending > 0){
			Serial.println();
		}
		when_next = mil + time;
	}
	readHall();
	update_servo( INNER_SERVOY );
	update_servo( INNER_SERVOZ );
	if(stepperIsReady){
		long int pos = stepperX.currentPosition();
		sendStepperReady(pos);
		stepperIsReady = false;
	}
//	Serial.println("tutaj8");
}

void stepperReady( long int pos ){		// in interrupt
	stepperIsReady = true;
	//sendStepperReady(pos);
}

//uint16_t divisor = 500;

void sendStepperReady( long int pos ){		// in interrupt
	Serial.print("RRx");
	Serial.println(String(pos));
}

int16_t readValue() {           // synchroniczne
/*
	cli();;
	int16_t val1 = ADCvalue[0][INNER_CODE_HALL_X];      // copy, look at the ISR
	val1 += ADCvalue[1][INNER_CODE_HALL_X];
	val1 += ADCvalue[2][INNER_CODE_HALL_X];
	val1 += ADCvalue[3][INNER_CODE_HALL_X];
	sei();
	val1 = val1 >>2;    // div 4
	return val1;
	*/
	return analogRead(PIN_B2_HALL_X );
}

byte get_hx_state_id( int16_t value){
	for(byte i=0;i<HXSTATES;i++){
		if(hallx_state[i][1] <= value && hallx_state[i][2] >= value ){
			return i;
		}
	}
	return 0xff;
}
byte get_hy_state_id( int16_t value){
	for(byte i=0;i<HYSTATES;i++){
		if(hally_state[i][1] <= value && hally_state[i][2] >= value ){
			return i;
		}
	}
	return 0xff;
}
void init_hallx() {           // synchroniczne
	int16_t val1 = readValue();
	val1 += readValue();
	val1 += readValue();
	val1 += readValue();
	val1 = val1>>2;		//  div 4
	byte new_state_id = get_hx_state_id( val1 );
	change_state( state_id, new_state_id, val1 );
}
void change_state( byte oldStateId, byte newStateId, int16_t value ) {           // synchroniczne
	if( newStateId != 0xff ){
		state_id		= newStateId;
		up_level		= hallx_state[newStateId][2] + HYSTERESIS;		// max is a limit
		down_level		= hallx_state[newStateId][1] - HYSTERESIS;		// min is a limit
		send_hx_pos( newStateId, value );	// send to mainboard
	}
}
void readHall() {           // synchroniczne
	if( stepperX.distanceToGo() != 0
		&& servos[INNER_SERVOY].moving == DRIVER_DIR_STOP 
		&& servos[INNER_SERVOZ].moving == DRIVER_DIR_STOP ){
		if( cc>HX_SPEED){
			cc   = 0;
			int16_t val1 = readValue();
			if( val1 >= up_level || val1 <= down_level ){
				byte new_state_id = get_hx_state_id( val1 );
				change_state( state_id, new_state_id, val1 );
			}
		}
		cc++;
	}
}

void update_servo( byte index ) {           // synchroniczne
	if( servos[index].pos_changed == true ){  // mam byc gdzie indziej
	//	Serial.println("teraz");
	//	Serial.flush();
		//    DEBUG( "-przesuwam Y " );
		//    DEBUGLN( String(servos[index].last_pos) );
		servo_lib[index].writeMicroseconds(servos[index].last_pos);
	//	Serial.println("po");
	//	Serial.flush();
		servos[index].pos_changed = false;
		if( servos[index].last_pos == servos[index].target_pos){
			DEBUGLN( "-gotowe servo" );
			/*
			if( index == INNER_SERVOY ){
				uint16_t margin = servos[index].last_pos;    // odwrotnie do ostatniej komendy
				if( servos[index].delta_pos > 0 ){      // jechalem w gore
					DEBUGLN( "- -100" );
					margin -= 20;
				}else if(  servos[index].delta_pos < 0){  // jechalem w dol
					DEBUGLN( "- +100" );
					margin += 20;
				}
				servo_lib[index].writeMicroseconds(margin);				
			}*/
			send_servo(false, localToGlobal(index), servos[index].target_pos );
		}
	//	Serial.println("po2");
	//	Serial.flush();
	}
}

void parseInput( String input ){
//	Serial.println("-input1: " + input );
	input.trim();
	boolean defaultResult = true;
	byte command = input.charAt(0);

	if( command == METHOD_MSET_TOP_COLOR && command == METHOD_MSET_BOTTOM_COLOR ) {    // CAaColor		// set TOP /BOTTOM color for Aa to Rr Gg Bb
		// C|03|4294967295
		String digits    	= input.substring( 1 );
		char charBuf[15];
		digits.toCharArray(charBuf,15);
		uint16_t address	= 0;
		unsigned long int color	= 0;
		sscanf(charBuf,"%2x%lx", &address, &color );
		setColor(address, color);

	}else if(command == METHOD_MSET_LED || command == METHOD_M_ONECOLOR ) {    // L12,ff,211 or  B12,ff,211
		String digits     = input.substring( 1 );
		char charBuf[10];
		digits.toCharArray(charBuf,10);
		unsigned int num    = 0;
		unsigned int leds 	= 0;
		unsigned int power  = 0;
		sscanf(charBuf,"%i,%2x,%i", &num, &leds, &power );
		byte r =  bitRead(leds, 0) ? power : 0;
		byte g =  bitRead(leds, 1) ? power : 0;
		byte b =  bitRead(leds, 2) ? power : 0;
		unsigned long int color = bottom_panels.Color(r,  g,  b );
		setColor(num, color);

/*
		byte i = COUNT_CARRET_ONBOARD_LED;
		while(i--){
			if( bitRead(leds, i) ){
				strip.setPixelColor(i, power);
			}else if( command == METHOD_M_ONECOLOR ){
				strip.setPixelColor(i, 0);
			}
		}*/

	}else if( input.startsWith(METHOD_SET_X_ACCELERATION)) {    // AX10                  // ACCELERATION
		unsigned int val = decodeInt(input, 2);
		val = val * 100;
		stepperX.setAcceleration(val);
		DEBUGLN("-setAcceleration: " + String(val) );
	}else if( input.equals("EX") ) {    // enable
		stepperX.enableOutputs();

	}else if( input.equals("EY") ) {    // enable
		byte index = 0;
		servo_lib[index].attach(servos[index].pin);
		servos[index].enabled= true;

	}else if( input.equals("EZ") ) {    // enable
		byte index = 1;
		servo_lib[index].attach(servos[index].pin);
		servos[index].enabled= true;

	}else if( command == 'D' ) {    // disable
		byte command2 = input.charAt(1);
		defaultResult = false;
		byte index = (command2 == 'Y') ? 0 : 1;
		if( command2 == 'X' ){
			stepperX.disableOutputs();
		}else{
			servos[index].enabled= false;
			servo_lib[index].detach();
			if( servos[index].target_pos != servos[index].last_pos ){    //  wylaczylem w trakcie jechania
				 send_servo(false, localToGlobal(index), servos[index].target_pos );
			}
			stepperX.fastWrite(servos[index].pin, HIGH);		// set to 1
			//    pinMode(servos[index].pin, INPUT);
			servos[index].pos_changed = false;
		}
	}else if(command == 'A' ) {    // A
		defaultResult = false;
		byte source = input.charAt(1) -48;		// ascii to num ( '0' = 48 )

		if( source == INNER_HALL_X ){
			int16_t val1 = readValue();
			byte newStateId = get_hx_state_id( val1 );
			send_hx_pos(newStateId, val1 );

		}else if( source ==  INNER_HALL_Y ){ 
			int16_t val1 = analogRead(PIN_B2_HALL_Y );
			byte newStateId = get_hy_state_id( val1 );
			send_y_pos(newStateId, val1 );

		}else if( source ==  INNER_WEIGHT ){ 
			int16_t val1 = analogRead(PIN_B2_WEIGHT );
			byte ttt[4] = {
				METHOD_IMPORTANT_ANALOG, 
				INNER_WEIGHT,
				(val1 & 0xFF),
				(val1 >>8),
			};
			send(ttt,4); 
		}else if( source ==  INNER_CURRENTY ){
		}else if( source ==  INNER_CURRENTZ ){
		}else if( source ==  INNER_MB_TEMP ){
		}else if( source ==  INNER_TABLET ){
		}

	//}else if( command == METHOD_GETVALUE ){
   // }else if( command == METHOD_EEPROM_WRITE_I2C ){
   // }else if( command == METHOD_EEPROM_READ_I2C ){
	}else if( input.equals( "WR") ){      // wait for return - tylko zwrÃ³c zwrotke
	}else{
		defaultResult = false;
		if( input.equals( "PING2ANDROID") ){      // nic nie rob

		}else if(command == 'X' ) {    // X10,10              // TARGET,MAXSPEED
			paserDeriver(DRIVER_X, input);
		}else if( command == 'Y' ) {    // Y10,10             // TARGET,ACCELERATION
			paserDeriver(DRIVER_Y,input);
		}else if(command == 'Z') {    // Z10,10               // TARGET,ACCELERATION
			paserDeriver(DRIVER_Z,input);
		}else if( input.equals( "PING") ){
			Serial.println("PONG");	
		}else if( command == METHOD_GET_TEMP ){  
			uint8_t tt = GetTemp();
			Serial.print("RT");
			Serial.println(String(tt));
			Serial.flush();

		}else if( command == 'x') {	//METHOD_GET_X_POS
			long int pos = stepperX.currentPosition();
			Serial.print("Rx"); 
			Serial.print(String(pos)); 
			Serial.println();
			Serial.flush();
			
		}else if(command == 'y' ) {    // pobierz pozycje
			byte ttt[4] = {METHOD_I2C_SLAVEMSG, METHOD_GET_Y_POS, (servos[INNER_SERVOY].last_pos & 0xFF),(servos[INNER_SERVOY].last_pos >>8) };
			send(ttt,4);
		
		}else if( command == 'z' ) {    // pobierz pozycje
			byte ttt[4] = {METHOD_I2C_SLAVEMSG, METHOD_GET_Z_POS, (servos[INNER_SERVOZ].last_pos & 0xFF),(servos[INNER_SERVOZ].last_pos >>8) };
			send(ttt,4);

		}else{
			Serial.println("NO_CMD [" + input +"]");
		}
	}
	if(defaultResult ){
		Serial.println("RR" + input );
		Serial.flush();
	}

  long unsigned int value= decodeInt( input, 1 );    // po komendzie zawsze jest liczba
  if( command == '+'){
    if(value < 10){
      bitSet(sending,  value);
    }
    sendstats();
  }else if( command == '-'){
    if(value < 10){
      bitClear(sending,  value);
    }
    sendstats();
  }else if( command == 'r'){
    repeat = value;
    sendstats();
  }else if( command == 't'){
    time = value;
    sendstats();
  }else if( command == 's'){
    sum = value % TRIES;
    sendstats();
  }else if( command == 'c'){
    sendstats();
  }
}


void setColor(byte num, unsigned long int color){
	if( num < 10 ){ // 0..9
		bottom_panels.setPixelColor(num, color );
		bottom_panels.show();
	}else{	//	<= 10
		top_panels.setPixelColor(num, color );
		top_panels.show();
	}
}


void send_error( String input){
	Serial.print("E" );	
	Serial.println( input );
	Serial.flush();
}

void paserDeriver( byte driver, String input2 ){   // odczytaj komende silnika
	String input   = input2.substring( 1 );
	int comma      = input.indexOf(',');
	long maxspeed  = 0;
	long target    = 0;

//	Serial.println("-input: " + input );

	if( comma == -1 ){      // tylko jedna komenda
		target          = input.toInt();
	//	unsigned int target           = 0;
	//	char charBuf[3];
	//	digits.toCharArray(charBuf, 3);
	//	sscanf(charBuf,"%i", &target );
	}else{
		input           = input.substring(comma + 1 );    // wytnij od tego znaku
		String current  = input.substring(0, comma);
		target          = decodeInt( current, 0 );
	//	Serial.println("-current: " + current );
	//	Serial.println("-input2: " + input );
		if( input.length() > 0 ){
			maxspeed       = input.toInt();
	//		DEBUGLN("-setMaxSpeed: " + String(maxspeed) );
		}
	}
	if( driver == DRIVER_X){
		if(maxspeed > 0){
			stepperX.setMaxSpeed(maxspeed);
		}
		stepperX.moveTo(target);
	}else if( maxspeed > 0 && driver == DRIVER_Y ){            // stepper Y
		// on wire: low_byte, high_byte, speed
		// in memory: 1=low_byte, 2=high_byte, 3=speed
		DEBUG("SERVO Y speed ");
		DEBUG(String(maxspeed));
		DEBUG(" target:");
		DEBUGLN(String(target));
		run_to(INNER_SERVOY,maxspeed,target);
	}else if( maxspeed > 0 && driver == DRIVER_Z ){            // stepper Z
		run_to(INNER_SERVOZ,maxspeed,target);
	}
}

void sendstats(){ 
    Serial.print("state ");
    if( bitRead(sending, 0 ) ){  Serial.print("0");   }
    if( bitRead(sending, 1 ) ){  Serial.print("1");   }
    if( bitRead(sending, 2 ) ){  Serial.print("2");   }
    if( bitRead(sending, 3 ) ){  Serial.print("3");   }
    if( bitRead(sending, 4 ) ){  Serial.print("4");   }
    if( bitRead(sending, 5 ) ){  Serial.print("5");   }
    if( bitRead(sending, 6 ) ){  Serial.print("6");   }
    if( bitRead(sending, 7 ) ){  Serial.print("7");   }
    if( bitRead(sending, 8 ) ){  Serial.print("8");   }
    Serial.println();
    Serial.print("t" + String(time ));
    Serial.print("s" + String(sum ));
    Serial.print("r" + String(repeat) );
}

void serialEvent(){				    // Runs after every LOOP (means don't run if loop hangs)
	while (Serial.available()) {    // odczytuj gdy istnieja dane i poprzednie zostaly odczytane
		char inChar = (char)Serial.read();
		serial0Buffer += String(inChar);
		if (inChar == '\n') {
			parseInput( serial0Buffer );				      // parsuj wejscie
			serial0Buffer = "";
		}
	}
}

unsigned int decodeInt(String input, int odetnij ){
  long pos = 0;
  if(odetnij>0){
    input = input.substring(odetnij);    // obetnij znaki z przodu
  }
  pos = input.toInt();
  return pos;
}

void reload_servo( byte index ){      // in interrupt
	volatile ServoChannel &ser = servos[index];
	if( servo_lib[index].attached() && ser.last_pos != ser.target_pos ){
		long int this_distance =0;
		long int delta = 0;
		if( ser.last_pos > ser.target_pos ){
			this_distance  = ser.last_pos - ser.target_pos;
		}else if( ser.last_pos < ser.target_pos ){
			this_distance  = ser.target_pos - ser.last_pos;
		}
		int quoter = (ser.last_distance >> 2);                // this_distance zawsze sie zmiejsza
		if( this_distance < quoter){                      // ostatnia cwiatrka = zwalniaj
			delta = (ser.delta_pos * this_distance);
			delta = delta /quoter;
			//      DEBUG("delta4 = " );
		}else if( this_distance > (ser.last_distance - quoter)){        // pierwsza cwiatrka = przyspieszaj. tu zawsze this_distance > 3/4 * last_distance
			delta = (ser.delta_pos * (ser.last_distance - this_distance ) );      // tu zawsze (last_distance - this_distance ) < quoter
			delta = delta /quoter;
			//      DEBUG("delta1 = " );
		}else{  // na maxa
			//      DEBUG("delta2 = " );
			delta = ser.delta_pos;
		}
		if(ser.delta_pos > 0){
			if( delta < MIN_DELTA){
				delta = MIN_DELTA;
			}
		}else{
			if( delta > -MIN_DELTA){
				delta = -MIN_DELTA;
			}
		}
		ser.last_pos = ser.last_pos + delta;
		if( ser.delta_pos > 0 && ser.last_pos > ser.target_pos ){        // nie przekraczaj docelowej pozycji
			ser.last_pos = ser.target_pos;
			//     DEBUGLN("gotowe1");
		}else if( ser.delta_pos < 0 && ser.last_pos < ser.target_pos ){
			//      DEBUGLN("gotowe2");
			ser.last_pos = ser.target_pos;
		}
		ser.pos_changed = true;
    /*
    if(ser.pos_changed){
      DEBUG(String(delta));
      DEBUG(" ");
      DEBUG(String(ser.delta_pos));
      DEBUG(" ");
      DEBUG(String(ser.target_pos));
      DEBUG(" ");
      DEBUGLN(String(ser.last_pos));
		}*/
	}
}

void timer(){
	timer_counter++;
	//timer_now = true;
	stepperX.run();
	ticks++;
	//DW(PIN_PANEL_LED1_NUM,  !digitalRead(PIN_PANEL_LED1_NUM));    // Toggle led. Read from register (not from pin)
	reload_servo(INNER_SERVOY);
	reload_servo(INNER_SERVOZ);
}

void run_to(byte index, byte sspeed, uint16_t target){
	if( servos[index].target_pos  == target && servos[index].last_pos == target ){      // the same pos
		servo_lib[index].attach(servos[index].pin);
		servo_lib[index].writeMicroseconds(servos[index].last_pos);
		send_servo(false, localToGlobal(index), target );
	}else{
		servos[index].target_pos     = target;
		if( servos[index].target_pos < servos[index].last_pos ){    // jedz w dol
			servos[index].delta_pos = -sspeed;
			servos[index].last_distance = servos[index].last_pos - servos[index].target_pos;
			servos[index].moving	= DRIVER_DIR_FORWARD;
		}else if( servos[index].target_pos > servos[index].last_pos ){    // jedz w gore
			servos[index].delta_pos = sspeed;
			servos[index].last_distance = servos[index].target_pos - servos[index].last_pos;
			servos[index].moving	= DRIVER_DIR_BACKWARD;
		}
	}
	if(!servo_lib[index].attached()){            //  turn on even if the same target pos
		servo_lib[index].attach(servos[index].pin);
		servos[index].enabled = true;
	}
}

void send_servo( boolean error, byte servo, uint16_t pos ){
	if(error){
		byte ttt[5] = {METHOD_I2C_SLAVEMSG,  RETURN_DRIVER_READY, servo, (pos & 0xFF), (pos >>8) };
		send(ttt,5);
		byte ttt2[3] = {METHOD_EXEC_ERROR,  RETURN_DRIVER_ERROR, servo};
		send(ttt2,3);
	}else{
		byte ttt[5] = {METHOD_I2C_SLAVEMSG,  RETURN_DRIVER_READY, servo, (pos & 0xFF), (pos >>8) };
		send(ttt,5);
		if(servo == DRIVER_Y ){
			servos[INNER_SERVOY].moving= DRIVER_DIR_STOP;
		}else if(servo == DRIVER_Z ){
			servos[INNER_SERVOZ].moving= DRIVER_DIR_STOP;
		}
		ttt[2] = RETURN_DRIVER_READY_REPEAT;
		send(ttt,6);
	}
}

void send_hx_pos( byte stateId, int16_t value ) {
	//Serial.println("new state: " + String(stateId) + " @ " + String(value) );
	byte i = COUNT_CARRET_ONBOARD_LED;
	while(i--){
		if( bitRead( hallx_state[stateId][3], i) ){
			top_panels.setPixelColor(i, 100);
		}else{
			top_panels.setPixelColor(i, 0 );
		}
		top_panels.show();
	}
	byte state_name	= hallx_state[stateId][0];
	byte dir = 0;
	boolean stop_moving = false;
	long int dis = stepperX.distanceToGo();
	if( dis > 0 ){
		dir = DRIVER_DIR_FORWARD;
		if( state_name == HX_STATE_1 ){		// moving up, max found
			stop_moving = true;
		}
	}else if( dis < 0 ){
		dir = DRIVER_DIR_BACKWARD;
		if( state_name == HX_STATE_9 ){   		// moving down, min found
			stop_moving = true;
		}
	}else{
		dir = DRIVER_DIR_STOP;
	}
	if(stop_moving){
		stepperX.stopNow();
		dir = 0;
	}
	byte ttt[10] = {
		METHOD_IMPORTANT_ANALOG, 	// 0
		INNER_HALL_X, 		// 1	
		state_name,  		// 2	// STATE
		dir,				// 3	// dir
		bytepos.bytes[3], 	// 4	// bits 0-7
		bytepos.bytes[2],  	// 5	// bits 8-15
		bytepos.bytes[1], 	// 6	// bits 16-23
		bytepos.bytes[0],  	// 7	// bits 24-32
		(value & 0xFF),
		(value >>8),
	};	
	send(ttt,10);
}
void send_y_pos( byte stateId, int16_t value) {
	byte state_name	= hally_state[stateId][0];
	uint16_t pos = servos[INNER_SERVOY].last_pos;
	byte ttt[10] = {
		METHOD_IMPORTANT_ANALOG, 
		INNER_HALL_Y, 
		state_name, 
		0,						// last dir
		0,						// pos
		0,						// pos
		(pos & 0xFF),			// position
		(pos >>8),				// position
		(value & 0xFF),
		(value >>8),
	}; 
	send(ttt,10);
}

void send( volatile byte buffer[], byte length ){
	//Serial.write(buf, len);
	Serial.print(buffer[0]);
	for (int i=1; i<length; i++) { 
		Serial.print(",");	
		Serial.print(buffer[i]);
	}
}

byte localToGlobal( byte ind ){      // get global device index used in android
	if( ind == INNER_SERVOY ){
		return DRIVER_Y;
	}
	return DRIVER_Z;  // INNER_SERVOZ
}
void init_analogs(){
    ADMUX = 0;                // use ADC0
    ADMUX |= _BV(REFS1);      
    ADMUX |= (1 << REFS0);    // REFS1 + REFS0 = Internal 1.1V (ATmega168/328) or  2.56V on (ATmega8)
    ADCSRA |= (1 << ADPS0);  // 128 prescale  
    ADCSRA |= (1 << ADPS1);
    ADCSRA |= (1 << ADPS2);
    ADCSRA |= (1 << ADATE);   // Set ADC Auto Trigger Enable
    ADCSRB = 0;               // 0 for free running mode
    ADCSRA |= (1 << ADEN);    // Enable the ADC
    ADCSRA |= (1 << ADIE);    // Enable Interrupts 
    ADCSRA |= (1 << ADSC);    // Start the ADC conversion
    sei();
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

/*
ISR(ADC_vect){
  uint8_t tmp  = ADMUX;            // read the value of ADMUX register
  tmp          &= 0xF0;            // starsze bity
  channel      = (channel + 1)%ANALOGS;
  ADMUX        = (tmp | ADCport[channel]);
  ADCvalue[ row ][ channel ] = ADCL | (ADCH << 8);  //  read low first
  if( channel == 0 ){
    row          = ((row+1) % ANALOG_TRIES);
  }
  //checks++;
}
*/

