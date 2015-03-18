#define B2_VERSION 0x00011
#define PCB_TYPE 3					// 1 - upanels, 2 = servos, 3 = figrelli actuators

#include "barobot_carret2_main.h"
#include <Adafruit_NeoPixel.h>
#include <Arduino.h>
#include <barobot_common.h>
#include <Servo.h>
#include <avr/eeprom.h>
#include <constants.h>
#include <AsyncDriver.h>
#include <FlexiTimer2.h>

// PWM: 3, 5, 6, 9, 10, 11
// PINS
// common settings
#define PIN_B2_SELF_RESET 2			// 
#define YZ_INPUT_ON_DISABLE false
#define YZ_VALUE_ON_DISABLE HIGH

// config for PCB with steppers
#define PIN_B2_STEPPER_ENABLE 3		// 
#define PIN_B2_STEPPER_STEP 12		// 
#define PIN_B2_STEPPER_DIR 13		// 
#define PIN_B2_SERVO_Y 9			// 
#define PIN_B2_SERVO_Z 10			// 
#define PIN_B2_LED_TOP 11			// 
#define PIN_B2_LED_BOTTOM 8			// 
#define PIN_B2_SERVOS_ENABLE_PIN 7	// 
#define PIN_B2_SERVY_ENABLE_PIN 6	// 
#define PIN_B2_SERVZ_ENABLE_PIN 5	// 

#define PIN_B2_HALL_Y A0			// 
#define PIN_B2_HALL_Z A1			// 
#define PIN_B2_HALL_X A2			// 
#define PIN_B2_WEIGHT A3			// 

// config for PCB with Figrelli L 16
#define PIN_B3_OUT_Y1 9
#define PIN_B3_OUT_Y2 5
#define PIN_B3_OUT_Z1 7
#define PIN_B3_OUT_Z2 10
#define PIN_B3_POWER_SENSOR 6		// 

#define PIN_B3_IN_Y A0			// 
#define PIN_B3_IN_Z A1			// 
#define PIN_B3_IN_X A2			// 
#define PIN_B3_WEIGHT A3		// 
#define PIN_B3_LIGHT A5			// 
#define PIN_B3_LED_BOTTOM 8		// using PIN_B2_LED_BOTTOM instead

#define Z_MAX_VAL 720		// 
#define Z_MIN_VAL 120		//
#define Y_MAX_VAL 1023		// 
#define Y_MIN_VAL 1			//

// Config
#define ANALOG_TRIES  4
#define B2_ACCELERX 10000
#define B2_SPEEDX 2500
#define B2_SERIAL0_BOUND 57600

#define MAGIC_LEDS 20
#define SERVO_MAX_TIME 10000				// in milis = 10s
#define MAX_TIME_WITHOUT_ANDROID 10000		// in milis = 20s
#define POKE_ANDROID_TIME 5000				// in milis = 20s

volatile boolean stepperIsReady = false;
unsigned long int mil = 0;
unsigned long int servo_start_time = 0;
unsigned long int last_android = 0;
unsigned long int last_poke = 0;

byteint bytepos;
#define MIN_DELTA  15
#define SERVO_PRESCALER  200

volatile uint16_t prescaler = 0;			// step Y
volatile long int delta = 0;
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
	{PIN_B2_SERVO_Y,0, 770, 770,0,false,false,DRIVER_DIR_STOP },
	{PIN_B2_SERVO_Z,0,2400,2400,0,false,false,DRIVER_DIR_STOP },
};

byte state_id = 0xff;
int16_t up_level = 0;
int16_t down_level = 0;

// HALL X VALUES 

// neodym	max  654	
// 			max  548	561		574
//			zero 507	
//			min  465	451		435
// neodym 	min	 357

#define HX_NEODYM_UP_BELOW  850
// neodym	max  654
#define HX_NEODYM_UP_START  640

#define HX_FERRITE_UP_IS_BELOW  610
// 			max  548	561		574
#define HX_LOCAL_UP_MAX_OVER  540

#define HX_NOISE_BELOW 531
//			zero 507	
#define HX_NOISE_OVER  500

#define HX_LOCAL_DOWN_IS_BELOW  495
//			min  465	451		435
#define HX_FERRITE_DOWN_IS_BELOW  420

#define HX_NEODYM_DOWN_START  370
// neodym 	min	 357
#define HX_NEODYM_DOWN_OVER  150

// end HALL X VALUES 

volatile int16_t hallx_state[HXSTATES][3] = {
	//{CODE, MIN, MAX }
	{HX_STATE_0,	HX_NEODYM_UP_BELOW,			1024						},		// 11		ERROR
	{HX_STATE_1,	HX_NEODYM_UP_START,			HX_NEODYM_UP_BELOW-1		},		// 22		to jest neodym max
	{HX_STATE_2,	HX_FERRITE_UP_IS_BELOW,		HX_NEODYM_UP_START-1		},		// 33		wznosi siê neodym
	{HX_STATE_3,	HX_LOCAL_UP_MAX_OVER,		HX_FERRITE_UP_IS_BELOW-1	},		// 44		czubek lokalnego max
	{HX_STATE_4,	HX_NOISE_BELOW,				HX_LOCAL_UP_MAX_OVER-1		},		// 55		wznosi siê

	{HX_STATE_5,	HX_NOISE_OVER,				HX_NOISE_BELOW-1			},		// 66		neutralne

	{HX_STATE_6,	HX_LOCAL_DOWN_IS_BELOW,		HX_NOISE_OVER-1				},		// 77		opada
	{HX_STATE_7,	HX_FERRITE_DOWN_IS_BELOW,	HX_LOCAL_DOWN_IS_BELOW-1	},		// 88		czubek lokalnego min
	{HX_STATE_8,	HX_NEODYM_DOWN_START,		HX_FERRITE_DOWN_IS_BELOW-1	},		// 99		opada neodym
	{HX_STATE_9,	HX_NEODYM_DOWN_OVER,		HX_NEODYM_DOWN_START-1		},		// 100		to jest neodym min	
	{HX_STATE_10,	0,							HX_NEODYM_DOWN_OVER-1		}		// 111		NOT CONNECTED	
};

#define HYSTERESIS  3
#define SEPARATOR_CHAR '\n'
#define TRIES  10
/*
volatile int16_t hally_state[HYSTATES][3] = {
	//{CODE, MIN, MAX  }
	{'E',	800,	1024		},		// ERROR
	{'R',	550,	800-1		},		// neodym +
	{'A',	450,	550-1		},		// normal
	{'B',	300,	450-1		},		// neodym -
	{'N',	0,		300			}		// NOT CONNECTED	
};
*/
String serial0Buffer = "";
uint8_t pcb_type	= PCB_TYPE;

//unsigned long int when_next = 0;
//unsigned long int sending = 0b00000000;
//unsigned long int time = 5000;
//unsigned long int sum = 0;
//unsigned long int repeat = 0;

AsyncDriver stepperX( PIN_B2_STEPPER_STEP, PIN_B2_STEPPER_DIR, PIN_B2_STEPPER_ENABLE );      // Step, DIR
Adafruit_NeoPixel top_panels	= Adafruit_NeoPixel(MAGIC_LEDS, PIN_B2_LED_TOP, NEO_GRB + NEO_KHZ800);
Adafruit_NeoPixel bottom_panels = Adafruit_NeoPixel(2, PIN_B2_LED_BOTTOM,  NEO_RGB + NEO_KHZ800);
// NEO_RBG added in Adafruit_NeoPixel: rOffset = 1;gOffset = 2;bOffset = 0;

static EEMEM uint16_t eeprom_starts = 0x02; 
boolean enabled6 = true;		// before setup();

void setup(){
	Serial.begin(B2_SERIAL0_BOUND);
	serial0Buffer = "";
	Serial.println("");	// remove dust
	Serial.flush();

	// read pcb type	
	pinMode( PIN_B3_POWER_SENSOR, INPUT );
	boolean power = digitalRead( PIN_B3_POWER_SENSOR );
	if( power == LOW ){
		Serial.println("-chyba 2");
	}else{
		Serial.println("-chyba 3");
	}

	//pcb_type
	disableYZ();
	if(pcb_type == 2 ){		// servos + magicled
		//pinMode(PIN_B2_SERVO_Y, INPUT+INPUT_PULLUP );      // nie pozwalaj na przypadkowe machanie na starcie
		//pinMode(PIN_B2_SERVO_Z, INPUT+INPUT_PULLUP );      // nie pozwalaj na przypadkowe machanie na starcie
		pinMode(PIN_B2_SELF_RESET, INPUT );	
		pinMode(PIN_B2_HALL_X, INPUT + INPUT_PULLUP );
		pinMode(PIN_B2_HALL_Y, INPUT + INPUT_PULLUP );
		pinMode(PIN_B2_WEIGHT, INPUT);

	}else if(pcb_type == 3 ){		// actuators + magicled
		if(YZ_INPUT_ON_DISABLE){
			pinMode(PIN_B3_OUT_Y1, INPUT );		// stop Y
			pinMode(PIN_B3_OUT_Y2, INPUT );		// stop Y		
			pinMode(PIN_B3_OUT_Z1, INPUT );		// stop Z	
			pinMode(PIN_B3_OUT_Z2, INPUT );		// stop Z
		}else{
			pinMode(PIN_B3_OUT_Y1, OUTPUT);
			pinMode(PIN_B3_OUT_Y2, OUTPUT);
			pinMode(PIN_B3_OUT_Z1, OUTPUT);
			pinMode(PIN_B3_OUT_Z2, OUTPUT);
			pinMode(PIN_B3_LIGHT, INPUT );

			digitalWrite(PIN_B3_OUT_Y1, YZ_VALUE_ON_DISABLE);		// stop Y
			digitalWrite(PIN_B3_OUT_Y2, YZ_VALUE_ON_DISABLE);
			digitalWrite(PIN_B3_OUT_Z1, YZ_VALUE_ON_DISABLE);		// stop Z
			digitalWrite(PIN_B3_OUT_Z2, YZ_VALUE_ON_DISABLE);	

		}
		//pinMode(PIN_B3_POWER_SENSOR, INPUT + INPUT_PULLUP );
		pinMode(PIN_B3_POWER_SENSOR, INPUT );	
	}

	// blink led	
	pinMode(PIN_B2_STEPPER_DIR, OUTPUT );
	for(byte i =0; i<pcb_type;i++){
		digitalWrite( PIN_B2_STEPPER_DIR, HIGH );
		delay(100);
		digitalWrite( PIN_B2_STEPPER_DIR, LOW );
		delay(200);
	}
	init_leds();
	setupStepper();

	unsigned long int color = bottom_panels.Color(0,  0,  20 );	
	set_all_leds(color);
	Serial.println("");	// remove dust
	Serial.flush();
	Serial.println("BSTART");

	if(pcb_type == 2 ){		// servos + magicled
		init_hallx(PIN_B2_HALL_X);
	}else if(pcb_type == 3 ){
		init_hallx(PIN_B3_IN_X);
	}

	sendStats( true );
	Serial.flush();
}

void sendStats( boolean isStart ) {
	uint8_t tt			= GetTemp();
	uint16_t starts		= eeprom_read_word(&eeprom_starts);
	uint8_t rid_low		= eeprom_read_byte((unsigned char *)(EEPROM_ROBOT_ID_LOW*2));
	uint8_t rid_high	= eeprom_read_byte((unsigned char *)(EEPROM_ROBOT_ID_HIGH*2));
	// RRS,VERSION,TEMP,STARTS,ROBOT_ID
	// RRS,4,60,3222,40,0,3		= version 4, TEMP = 60 somethings (not celsius or fahrenheits), starts 3222, robot_id_low = 40,  robot_id_high = 0, pcb_type = 3
	Serial.print("RRS,");
	Serial.print(B2_VERSION, DEC );
	Serial.print(",");
	Serial.print(tt, DEC);
	Serial.print(",");
	Serial.print(starts, DEC);
	Serial.print(",");
	Serial.print(rid_low, DEC);
	Serial.print(",");
	Serial.print(rid_high, DEC);
	Serial.print(",");
	Serial.print(pcb_type, DEC);			// pcb type
	Serial.println();
	if(isStart){
		starts++;
		eeprom_write_word(&eeprom_starts, starts);
	}
}

inline void change_state( byte oldStateId, byte newStateId, int16_t value ) {           // synchroniczne
	if( newStateId != 0xff ){
		state_id	= newStateId;
		up_level	= hallx_state[newStateId][2] + HYSTERESIS;		// max is a limit
		down_level	= hallx_state[newStateId][1] - HYSTERESIS;		// min is a limit
		send_hx_pos( newStateId, value );	// send to mainboard
	}
}

inline void setupStepper(){
	stepperX.disable_on_ready = true;
	stepperX.disableOutputs();
	stepperX.setAcceleration(B2_ACCELERX);
	stepperX.setMaxSpeed(B2_SPEEDX);
	stepperX.setOnReady(stepperReady);
	if(pcb_type == 2 ){		// servos + magicled
		FlexiTimer2::set(1, 1.0/10000, timer2);
	}else if(pcb_type == 3 ){		// actuators + magicled
		FlexiTimer2::set(1, 1.0/10000, timer3);
	}
	FlexiTimer2::start();
}

inline void init_leds(){
	top_panels.begin();
	bottom_panels.begin();
	unsigned long int color = bottom_panels.Color(50,  0,  0 );
	set_all_leds(color);
}

void init_hallx(byte pin) {           // synchroniczne
	int16_t val1 = readValue(pin);
	byte new_state_id = get_hx_state_id( val1 );
	change_state( state_id, new_state_id, val1 );
}


int16_t agv = 0;

#define PWR_SENSOR_DIV 5
int16_t divider = PWR_SENSOR_DIV;
int8_t low_for_sure = 0;

void loop() {
	mil = millis();
/*
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
	}*/
	if( pcb_type == 3 && divider-- == 0 ){
		boolean power = digitalRead( PIN_B3_POWER_SENSOR );
		if( power == LOW ){
			low_for_sure++;
			divider = PWR_SENSOR_DIV/2;		// high speed checking
			if( low_for_sure > 90 ){
				Serial.println("F0");
				Serial.flush();
			}
		}else{
			low_for_sure = 0;
			divider = PWR_SENSOR_DIV;
		}
	}
	
	readHall();
	step_servoY();
	step_servoZ();
	
	if(stepperIsReady){
		sendStepperReady();
		stepperIsReady = false;
	}
	/*
	if(pre--){
		int16_t val1 = analogRead( PIN_B2_WEIGHT );
		agv = (val1 + agv * 3)>>2;
		if( val1 - agv > 2 ){
			byte r = abs(val1 - agv);
			bottom_panels.setPixelColor(0, r,0,0 );
			bottom_panels.show();

		}else if( val1 - agv < 2 ){
			byte g = abs(val1 - agv);
			bottom_panels.setPixelColor(1, 0,g,0 );
			bottom_panels.show();
		}
	}*/

	if( mil - last_android > MAX_TIME_WITHOUT_ANDROID ){			// no android
		last_android	= mil;
		disableServosNow();
		stepperX.disableOutputs();
	//	unsigned long int color = bottom_panels.Color(20,  0,  0 );	
	//	set_all_leds(color);
		Serial.println("RRNOMASTER");
		Serial.flush();
	}
	if( servo_start_time != 0 ){
		unsigned long period = millis() - servo_start_time;
		if( period > SERVO_MAX_TIME ){		// emergency stop
			disableServosNow();
			servo_start_time = 0;
			unsigned long int color = bottom_panels.Color(255,  0,  0 );
			set_all_leds(color);
			Serial.println("RRSERVOOFF");
			Serial.flush();
		}
	}
	if( mil - last_poke > POKE_ANDROID_TIME ){			// no android
		Serial.print("POKE ");
		Serial.println(String(mil));
		Serial.flush();
		last_poke = mil;
	}
}

void set_all_leds(unsigned long int color){
	for(byte i=0;i<MAGIC_LEDS;i++){
		top_panels.setPixelColor(i, color );
	}
	bottom_panels.setPixelColor(0, color );
	bottom_panels.setPixelColor(1, color );	
	top_panels.show();
	bottom_panels.show();
}

void sendStepperReady(){
	long int pos = stepperX.currentPosition();
	bytepos.i= pos;
	byte ttt[8] = {
		METHOD_I2C_SLAVEMSG,
		1, 
		RETURN_DRIVER_READY, 
		DRIVER_X, 
		bytepos.bytes[3],		// bits 0-7
		bytepos.bytes[2],		// bits 8-15
		bytepos.bytes[1],		// bits 16-23
		bytepos.bytes[0]		// bits 24-32
	};
	Serial.println("Rx" + String(pos));
	Serial.flush();
	sendln(ttt,8);
	Serial.println();
	Serial.flush();
}

byte get_hx_state_id( int16_t value){
	for(byte i=0;i<HXSTATES;i++){
		if(hallx_state[i][1] <= value && hallx_state[i][2] >= value ){
			return i;
		}
	}
	return 0xff;
}

uint16_t cc = 0;
void readHall() {           // synchroniczne
	if(
		servos[INNER_SERVOY].moving == DRIVER_DIR_STOP && servos[INNER_SERVOZ].moving == DRIVER_DIR_STOP ){
		if( cc>HX_SPEED){
			cc   = 0;
			byte pin = sensor2analogPin( INNER_HALL_X );
			int16_t val1 = readValue(pin);
			if( val1 >= up_level || val1 <= down_level ){
				byte new_state_id = get_hx_state_id( val1 );
				change_state( state_id, new_state_id, val1 );	// and STOP if needed
			}
		}
		cc++;
	}
}

uint16_t y_last_val = 0;
uint16_t y_repeat = 0;
void step_servoY() {       		    // synchroniczne
	volatile ServoChannel &ser = servos[INNER_SERVOY];
	if( pcb_type == 2 ){			// servos + magicled
		if( ser.pos_changed == true ){  // mam byc gdzie indziej
		//	Serial.print("-pos= " + String(servos[index].last_pos ));
		//	Serial.print("\t tar= " + String(servos[index].target_pos) );
		//	Serial.println("\t d= " + String(delta) );
			servo_lib[INNER_SERVOY].writeMicroseconds(ser.last_pos);
			ser.pos_changed = false;
			if(ser.last_pos == ser.target_pos){		// on target pos
				ser.moving= DRIVER_DIR_STOP;
				send_servoYisReady();
			}
		}
	}else if(pcb_type == 3 ){		// actuators + magicled
		if( ser.moving != DRIVER_DIR_STOP ){
			ser.last_pos = analogRead(PIN_B3_IN_Y);
			if( ser.last_pos == y_last_val || abs(ser.last_pos - y_last_val) <= 3){
			/*
				if( y_repeat %30 == 1 ){
					Serial.print("-");
					Serial.print(ser.last_pos);
					Serial.print("-");
					Serial.println(y_repeat);
				}*/
				y_repeat++;
				if( y_repeat == 700 ){
					Serial.print("RRYSTOPS,");
					Serial.println(ser.last_pos);
					Serial.flush();
					stopY( false );
					sendYpos();
					y_repeat = 0;
				}
			}else{
			/*
				if(y_repeat>60){
					Serial.print("-reset-");
					Serial.print(ser.last_pos);
					Serial.print("-");
					Serial.println(y_repeat);
				}*/
				y_repeat	=0;
				y_last_val	= ser.last_pos;
			}
			
			if( ser.moving == DRIVER_DIR_FORWARD ){
				if( ser.last_pos >= Y_MAX_VAL ){			// stop
					stopY( false );
					sendYpos();
		//			Serial.println("-step, STOP Z max");
				}else if( ser.last_pos >= ser.target_pos ){
					stopY( false );
					sendYpos();
				}
			}else if( ser.moving == DRIVER_DIR_BACKWARD ){
				if(  ser.last_pos <= Y_MIN_VAL ){			// stop
		//			Serial.println("-step, STOP Z min");
					stopY( false );
					sendYpos();
				}else if( ser.last_pos <= ser.target_pos ){
					stopY( false );
					sendYpos();
				}
			}
		}
	}
}

uint16_t z_last_val = 0;
uint16_t z_repeat = 0;

void step_servoZ() {       		    // synchroniczne
	volatile ServoChannel &ser = servos[INNER_SERVOZ];

	if( pcb_type == 2 ){			// servos + magicled
	}else if(pcb_type == 3 ){		// actuators + magicled
		if( ser.moving != DRIVER_DIR_STOP ){
			ser.last_pos = analogRead(PIN_B3_IN_Z);
		//	Serial.println(ser.last_pos);
			if( ser.last_pos == z_last_val || (ser.last_pos - z_last_val) == 1){
				z_repeat++;
				if( z_repeat == 500 ){
					Serial.print("RRZSTOPS,");
					Serial.println(ser.last_pos);
					Serial.flush();
				}
			}else{
				z_repeat	=0;
				z_last_val	= ser.last_pos;
			}

			if( ser.moving == DRIVER_DIR_FORWARD ){
				if( ser.last_pos >= Z_MAX_VAL ){			// stop
					stopZ( false );
					sendZpos();
		//			Serial.println("-step, STOP Z max");
				}else if( ser.last_pos >= ser.target_pos ){
					stopZ( false );
					sendZpos();
				}
			}else if( ser.moving == DRIVER_DIR_BACKWARD ){
				if(  ser.last_pos <= Z_MIN_VAL ){			// stop
		//			Serial.println("-step, STOP Z min");
					stopZ( false );
					sendZpos();
				}else if( ser.last_pos <= ser.target_pos ){
					stopZ( false );
					sendZpos();
				}
			}
		}
	}
}

void moveYto(byte sspeed, uint16_t target){
	volatile ServoChannel &ser = servos[INNER_SERVOY];

	if(pcb_type == 2 ){		// servos + magicled
		//Serial.print("-SERVO speed ");
		//Serial.print(String(sspeed));
		//Serial.print(" target:");
		//Serial.println(String(target));
		if(!servo_lib[INNER_SERVOY].attached()){            //  turn on even if the same target pos
			servo_lib[INNER_SERVOY].attach(ser.pin);
		}
		if( ser.target_pos  == target && ser.last_pos == target ){      // the same pos
			servo_lib[INNER_SERVOY].attach(ser.pin);
			servo_lib[INNER_SERVOY].writeMicroseconds(ser.last_pos);
			ser.moving		= DRIVER_DIR_STOP;
			y_repeat		= 0;
			send_servoYisReady();
			enableYZ();
		}else{
			if( target < ser.last_pos ){    // jedz w dol
				ser.delta_pos = -sspeed;
				ser.last_distance = ser.last_pos - target;
				ser.moving	= DRIVER_DIR_BACKWARD;
				y_repeat		= 0;
			}else if( target > ser.last_pos ){    // jedz w gore
				ser.delta_pos = sspeed;
				ser.last_distance = target - ser.last_pos;
				ser.moving	= DRIVER_DIR_FORWARD;
				y_repeat		= 0;
			}
			if(!enabled6){
				enableYZ();
				delay(150);		// wait for stabilization of servo
				servo_lib[INNER_SERVOY].writeMicroseconds(ser.last_pos);
		//		delay(500);
			}
			servos[INNER_SERVOY].target_pos    = target;
			prescaler					= SERVO_PRESCALER;
		}
	}else if(pcb_type == 3 ){		// actuators + magicled
		ser.last_pos = analogRead(PIN_B3_IN_Y);

		if( target < ser.last_pos ){    // jedz w dol
		//	ser.last_distance	= ser.last_pos - target;
			ser.moving			= DRIVER_DIR_BACKWARD;
			y_repeat			= 0;
		}else if( target > ser.last_pos ){    // jedz w gore
		//	ser.last_distance	= target - ser.last_pos;
			ser.moving			= DRIVER_DIR_FORWARD;
			y_repeat			= 0;
		}else if( target == ser.last_pos ){   				   // current pos
			if( ser.last_pos == target ){
				ser.moving		= DRIVER_DIR_STOP;
				send_servoYisReady();
			}
		}
		if( ser.moving	!= DRIVER_DIR_STOP ){
			ser.target_pos	= target;
			if( ser.moving		== DRIVER_DIR_BACKWARD ){

				if(YZ_INPUT_ON_DISABLE){
					pinMode(PIN_B3_OUT_Y1, OUTPUT );
					pinMode(PIN_B3_OUT_Y2, OUTPUT );
				}
				
				digitalWrite(PIN_B3_OUT_Y1, LOW);
				//digitalWrite(PIN_B3_OUT_Y2, HIGH);
				analogWrite( PIN_B3_OUT_Y2, sspeed );		// enable on HIGH, 255 = max speed
			}else{											// DRIVER_DIR_FORWARD
				digitalWrite(PIN_B3_OUT_Y1, HIGH);
				//digitalWrite(PIN_B3_OUT_Y2, LOW);
				analogWrite( PIN_B3_OUT_Y2, 255 - sspeed );			// enable on LOW so PWM is upside down, 0 = max speed
			}
		}
	}/*
	Serial.print("-moveYto: ");
	Serial.print(String(target));
	Serial.print(" lastpos: ");
	Serial.print(String(ser.last_pos));
	Serial.print(" moving: ");
	Serial.println(String(ser.moving));*/
}

void moveZto( unsigned int target ){
	volatile ServoChannel &ser = servos[INNER_SERVOZ];

	if( pcb_type == 3 ){
		ser.last_pos = analogRead(PIN_B3_IN_Z);
	}

	ser.target_pos	= target;
	if( ser.target_pos == ser.last_pos ){		// the same pos
		ser.moving		= DRIVER_DIR_STOP;
	}else if( ser.target_pos < ser.last_pos){
		ser.moving		= DRIVER_DIR_BACKWARD;
		z_repeat		= 0;
	}else{
		ser.moving		= DRIVER_DIR_FORWARD;
		z_repeat		= 0;
	}

	if( ser.moving != DRIVER_DIR_STOP ){
		ser.enabled		= true;
		if(pcb_type == 2 ){		// servos + magicled
			ser.last_pos	= target;
			servo_lib[INNER_SERVOZ].attach(servos[INNER_SERVOZ].pin);
			servo_lib[INNER_SERVOZ].writeMicroseconds(target);
		}else if(pcb_type == 3 ){
			if(YZ_INPUT_ON_DISABLE){
				pinMode(PIN_B3_OUT_Z1, OUTPUT );	
				pinMode(PIN_B3_OUT_Z2, OUTPUT );
			}
			if( ser.moving		== DRIVER_DIR_BACKWARD ){
				digitalWrite(PIN_B3_OUT_Z1, LOW);
				digitalWrite(PIN_B3_OUT_Z2, HIGH);
			}else{											// DRIVER_DIR_FORWARD
				digitalWrite(PIN_B3_OUT_Z1, HIGH);
				digitalWrite(PIN_B3_OUT_Z2, LOW);
			}
		}
	}
	enableYZ();
}

void stopZ( boolean addDelay ){
	volatile ServoChannel &ser = servos[INNER_SERVOZ];
	
	if( pcb_type == 2 && (ser.moving || ser.moving )){		// run disableYZ if Y is ON even if Z is OFF 
		disableYZ();
		if(addDelay){
			delay(300);		// to be sure
		}
	}
	if( ser.moving ){
		ser.pos_changed	= false;
		ser.moving			= DRIVER_DIR_STOP;
		if(pcb_type == 2 ){		// servos + magicled
		}else if(pcb_type == 3 ){		// actuators + magicled
			if(YZ_INPUT_ON_DISABLE){
				pinMode(PIN_B3_OUT_Z1, INPUT );		// stop Z	
				pinMode(PIN_B3_OUT_Z2, INPUT );		// stop Z
			}else{
				digitalWrite(PIN_B3_OUT_Z1, LOW);		// stop Y
				digitalWrite(PIN_B3_OUT_Z2, LOW);
			}
			servo_start_time	= 0;
		}
	}
}

void stopY( boolean addDelay ){
	volatile ServoChannel &ser = servos[INNER_SERVOY];
	if( pcb_type == 2 && (ser.moving || ser.moving )){		// run disableYZ if Z is ON even if Y is OFF 
		disableYZ();
		if(addDelay){
			delay(300);		// to be sure
		}
	}
	if( ser.moving ){
		ser.pos_changed	= false;
		ser.moving			= DRIVER_DIR_STOP;
		if(pcb_type == 2 ){		// servos + magicled
		}else if(pcb_type == 3 ){		// actuators + magicled
			if(YZ_INPUT_ON_DISABLE){
				pinMode(PIN_B3_OUT_Y1, INPUT );		// stop Y
				pinMode(PIN_B3_OUT_Y2, INPUT );		// stop Y
			}else{
				digitalWrite(PIN_B3_OUT_Y1, YZ_VALUE_ON_DISABLE);		// stop Y
				digitalWrite(PIN_B3_OUT_Y2, YZ_VALUE_ON_DISABLE);
			}
			servo_start_time	= 0;
		}
	}
}

void disableServosNow(){
	stopY( false );
	stopZ( false );
}

void disableYZ(){
	if(enabled6){
		if(pcb_type == 2 ){		// servos + magicled
			pinMode(PIN_B2_SERVOS_ENABLE_PIN, OUTPUT );
			digitalWrite(PIN_B2_SERVOS_ENABLE_PIN, HIGH );	// disable power

			servo_lib[INNER_SERVOY].detach();
			pinMode(PIN_B2_SERVO_Y, OUTPUT );
			digitalWrite( PIN_B2_SERVO_Y, HIGH );

			servo_lib[INNER_SERVOZ].detach();
			pinMode(PIN_B2_SERVO_Z, OUTPUT );
			digitalWrite( PIN_B2_SERVO_Z, HIGH );

		}else if(pcb_type == 3 ){		// actuators + magicled
			if(YZ_INPUT_ON_DISABLE){
				pinMode(PIN_B3_OUT_Y1, INPUT );		// stop Y
				pinMode(PIN_B3_OUT_Y2, INPUT );		// stop Y
				pinMode(PIN_B3_OUT_Z1, INPUT );		// stop Z
				pinMode(PIN_B3_OUT_Z2, INPUT );		// stop Z
			}else{
				digitalWrite(PIN_B3_OUT_Y1, YZ_VALUE_ON_DISABLE);		// stop Y
				digitalWrite(PIN_B3_OUT_Y2, YZ_VALUE_ON_DISABLE);

				digitalWrite(PIN_B3_OUT_Z1, YZ_VALUE_ON_DISABLE);		// stop Z
				digitalWrite(PIN_B3_OUT_Z2, YZ_VALUE_ON_DISABLE);
			}
		}
		enabled6 = false;
	}
	servo_start_time	= 0;
}

void enableYZ(){
//	Serial.print("-enableYZ enabled6:");
//	Serial.println(String(enabled6));
	if(!enabled6){
		if(pcb_type == 2 ){		// servos + magicled
		//	pinMode(PIN_B2_SERVOS_ENABLE_PIN, INPUT );		// make pin low (with pull-down)
			pinMode(PIN_B2_SERVOS_ENABLE_PIN, OUTPUT );		// make pin low (with pull-down)
			digitalWrite(PIN_B2_SERVOS_ENABLE_PIN, LOW );	// enable power
		}else if(pcb_type == 3 ){							// actuators + magicled
			// nothing special...
		}	
		enabled6 = true;
	}
	servo_start_time	= millis();
}

void sendYpos(){
	if(pcb_type == 2 ){		// servos + magicled
		uint16_t pos	= servos[INNER_SERVOY].last_pos;
		byte ttt[5] = {METHOD_I2C_SLAVEMSG,  1, METHOD_GET_Y_POS, (pos & 0xFF),(pos >>8) };
		sendln(ttt,5);
	}else if(pcb_type == 3 ){		// actuators + magicled
		int16_t val1	= readValue( PIN_B3_IN_Y );
		Serial.print("RRy");
		Serial.print(String(val1));
		Serial.print(",");
		Serial.println(String(servos[INNER_SERVOY].moving));
		Serial.flush();
	}
}

void sendZpos(){
	if(pcb_type == 2 ){		// servos + magicled
		byte ttt[5] = {METHOD_I2C_SLAVEMSG,  1, METHOD_GET_Z_POS, (servos[INNER_SERVOZ].last_pos & 0xFF),(servos[INNER_SERVOZ].last_pos >>8) };
		sendln(ttt,5);
	}else if(pcb_type == 3 ){		// actuators + magicled
		int16_t val1	= readValue( PIN_B3_IN_Z );
		Serial.print("RRz");
		Serial.print(String(val1));
		Serial.print(",");
		Serial.println(String(servos[INNER_SERVOZ].moving));			// RRz330,1	 or // RRz330,0
		Serial.flush();
	}
}

void send_servoYisReady(){
	uint16_t pos	= servos[INNER_SERVOY].last_pos;
	byte ttt[6]		= {METHOD_I2C_SLAVEMSG, 1, RETURN_DRIVER_READY, DRIVER_Y, (pos & 0xFF), (pos >>8) };
	sendln(ttt,6);
	ttt[2]			= RETURN_DRIVER_READY_REPEAT;
	sendln(ttt,6);
}

void parseInput( String input ){
//	Serial.println("-input1: " + input );
	input.trim();
	boolean defaultResult = true;
	byte command	= input.charAt(0);
	byte il			= input.length();
	last_android	= millis();

	if( command == 'Q' ) {    // QCOLOR, change all leds, color in hex
		String digits    	= input.substring( 1 );
		char charBuf[12];
		digits.toCharArray(charBuf,12);
		unsigned long int color	= 0;
		sscanf(charBuf,"%lx", &color );
		set_all_leds(color);
	}else if(command == 'l') {    // lnn,color i.e:   l01FFFFFF
		String digits     = input.substring( 1 );
		char charBuf[10];
		digits.toCharArray(charBuf,10);
		uint8_t num    = 0;
		unsigned long int color	= 0;
		sscanf(charBuf,"%hhi,%lx", &num, &color );
		setColor(num, color);

	}else if( command == 'T' ) {    // T90                  // ACCELERATION * 100 (90 * 100= 9000)
		unsigned int val = decodeInt(input, 2);
		val = val * 100;
		stepperX.setAcceleration(val);

	}else if( input.equals("EX") ) {    // enable motor
		stepperX.enableOutputs();
	}else if( command == 'D' ) {    // disable motor
		byte command2	= input.charAt(1);
		byte index		= (command2 == 'Y') ? 0 : 1;
		if( command2 == 'X' ){
			stepperX.disableOutputs();
		}else if( index == INNER_SERVOY ){

			if( servos[INNER_SERVOY].target_pos != servos[INNER_SERVOY].last_pos ){    //  wylaczylem w trakcie jechania
				send_servoYisReady();
			}
			stopY( true );
		}else{					// INNER_SERVOZ
			stopZ( true );
		}
	}else if( input.equals( "WR") ){   	  		// wait for return
	}else if( command == 'M' && il >= 4 ){		// save 1 char to eeprom in 2 cells. address in HEX!!! ie.: M0FF3 = write F3 into addresses: 0F*2 and 0F*2+1
		char charBuf[6];
		input.toCharArray(charBuf,6);
		unsigned char ad    = 0;
		unsigned char value = 0;
		sscanf(charBuf,"M%2hhx%2hhx", &ad, &value );
		byte ad1	= ad*2;
		byte ad2	= ad*2+1;
		while (!eeprom_is_ready());
		eeprom_write_byte( (uint8_t*)ad1, value);
		while (!eeprom_is_ready());
		eeprom_write_byte( (uint8_t*)ad2, value);

	}else if(command == 'K') {  // K1900               // move Z with max speed TARGET,SPEED(int,decimal)
		unsigned int pos		= decodeInt(input, 1);
		moveZto( pos );
	}else{
		defaultResult = false;
		if( input.equals( "AA") ){    	// android active	// no result

		}else if(command == 'A' ) {    // A
			byte source		= input.charAt(1) -48;		// ascii to num ( '0' = 48 )
			byte pin		= sensor2analogPin( source );
			int16_t val1	= (pin == 255) ? 0 : readValue( pin );

			if( source == INNER_HALL_X ){
				byte newStateId = get_hx_state_id( val1 );
				send_hx_pos( newStateId, val1 );
			}else if( source ==  INNER_HALL_Y ){ 				// A1			= Y pos or Y hall
				if(pcb_type == 2 ){		// servos + magicled
					// todo - remove
					uint16_t pos = servos[INNER_SERVOY].last_pos;
					byte ttt[10] = {
						METHOD_IMPORTANT_ANALOG, 
						INNER_HALL_Y, 
						0, 
						0,						// last dir
						0,						// pos
						0,						// pos
						(pos & 0xFF),			// position
						(pos >>8),				// position
						(val1 & 0xFF),
						(val1 >>8),
					};
					sendln(ttt,10);
				}else if(pcb_type == 3 ){		// actuators + magicled
					Serial.print("RRA1,");
					Serial.println(String(val1));
					Serial.flush();
				}
			}else{
				Serial.print("RRA");
				Serial.print(String(source));
				Serial.print(",");
				Serial.println(String(val1));
				Serial.flush();
			}
		}else if(command == 'a' ) {    					// a2  - read analog value
			byte source		= input.charAt(1) -48;		// ascii to num ( '0' = 48 )
			int16_t val1	= readValue( A0 + source );	// 0 = analog 0
			Serial.print("RRa");
			Serial.print(source, DEC);
			Serial.print(",");	
			Serial.println(String(val1));
			Serial.flush();

		}else if( input.equals( "IH") ){						// is home, reset X pos to 0
			stepperX.setCurrentPosition(0);
			Serial.print("RRx0");
			Serial.println();
			Serial.println("RRIH");
			Serial.flush();
		}else if(command == 'X' ) {    // X10,10              // TARGET,MAXSPEED
			paserDriver(DRIVER_X, input);
		}else if( command == 'Y' ) {    // Y10,10             // TARGET,SPEED
			paserDriver(DRIVER_Y,input);
		}else if(command == 'Z') {    // Z10,10               // TARGET,SPEED
			paserDriver(DRIVER_Z,input);
		}else if( input.equals( "PING") ){
			Serial.println("RRPONG");
			Serial.flush();
		}else if( command == 'x') {	//METHOD_GET_X_POS
			long int pos = stepperX.currentPosition();
			Serial.print("RRx"); 
			Serial.print(String(pos));
			Serial.println();
			Serial.flush();
		}else if(command == 'y' ) {    // pobierz pozycje
			sendYpos();
		}else if( command == 'z' ) {    // pobierz pozycje
			sendZpos();
		}else if( command == 'm' && il == 3 ){		// read 2 chars from eeprom. ie.: m15, address in HEX, value in dec
			char charBuf[5];
			unsigned char ad    = 0;
			input.toCharArray(charBuf,5);
			sscanf(charBuf,"m%2hhx", &ad );
			byte ad1	= ad*2;
			byte ad2	= ad*2+1;
			byte val1	= eeprom_read_byte((unsigned char *) ad1);
			byte val2	= eeprom_read_byte((unsigned char *) ad2);
			Serial.print("RRm");
			Serial.print(ad, HEX);
			Serial.print(',');
			Serial.print(val1, DEC);
			Serial.print(',');
			Serial.println(val2, DEC);
			Serial.flush();
		}else if( command == 'S' ){
			defaultResult = false;
			sendStats( false );
		}else if( input.equals( "RESET") ){
			Serial.println("RRRESET");		// R R RESET
			delay(3000);
			Serial.println("RRESET_START");
			Serial.flush();
			pinMode(PIN_B2_SELF_RESET, OUTPUT );	
			digitalWrite(PIN_B2_SELF_RESET, LOW );	
		}else{
			Serial.println("NO_CMD [" + input +"]");
			Serial.flush();
		}
	}
	if(defaultResult ){
		Serial.println("RR" + input );
		Serial.flush();
	}
/*
	// oscyloskop
	String ss 		= input.substring( 1 );
	byte value		= ss.toInt();

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
  }*/
}

byte sensor2analogPin(byte sensorNum){
	if(sensorNum == INNER_HALL_X ){					// 0
		return (pcb_type == 2 ) ? PIN_B2_HALL_X : PIN_B3_IN_X;
	}else if(sensorNum == INNER_HALL_Y ){			// 1
		return (pcb_type == 2 ) ? PIN_B2_HALL_Y : PIN_B3_IN_Y; 
	}else if(sensorNum == INNER_WEIGHT ){			// 2
		return (pcb_type == 2 ) ? PIN_B2_WEIGHT : PIN_B3_WEIGHT;
	}else if(sensorNum == INNER_POS_Z ){			// 3
		return (pcb_type == 2 ) ? PIN_B2_HALL_Z : PIN_B3_IN_Z;
	}else if(sensorNum == INNER_LIGHT ){			// 4
		return (pcb_type == 2 ) ? PIN_B3_LIGHT : PIN_B3_LIGHT;
	}
	return 255;
}

void setColor(byte num, unsigned long int color){
	if( num < 10 ){ // 0..9
		bottom_panels.setPixelColor(num, color );
		bottom_panels.show();
	}else{	//	<= 10
		top_panels.setPixelColor(num - 10, color );		// 10 - 20 (0-19)
		top_panels.show();
	}
}
void send_error( String input){
	Serial.print("E" );	
	Serial.println( input );
	Serial.flush();
}

void paserDriver( byte driver, String input2 ){   // odczytaj komende silnika
	String input   = input2.substring( 1 );
	int comma      = input.indexOf(',');
	long maxspeed  = 0;
	long target    = 0;
//	Serial.println("-input: " + input );
	if( comma == -1 ){      // tylko jedna komenda
		target          = input.toInt();
	}else{
		String current  = input.substring(0, comma);
		input           = input.substring(comma + 1 );    // wytnij od tego znaku
		target          = decodeInt( current, 0 );
		if( input.length() > 0 ){
			maxspeed       = input.toInt();
	//		Serial.println("-setMaxSpeed: " + String(maxspeed) );
		}
	}
	if( driver == DRIVER_X){
		if(maxspeed > 0){
			stepperX.setMaxSpeed(maxspeed);
		}
		int16_t value	= readValue(PIN_B2_HALL_X);
		byte stateId	= get_hx_state_id( value );
		byte state_name	= hallx_state[stateId][0];
		byte dir		= 0;
		boolean stop_moving = false;
		long int dis = target - stepperX.currentPosition();	// distance
		if( dis > 0 ){	// DRIVER_DIR_FORWARD
			dir = DRIVER_DIR_FORWARD;
			if( state_name == HX_STATE_1 ){			// moving up, max found
				stop_moving = true;
			}
		}else if( dis < 0 ){	// DRIVER_DIR_BACKWARD
			dir = DRIVER_DIR_BACKWARD;
			if( state_name == HX_STATE_9 ){   		// moving down, min found
				stop_moving = true;
			}
		}else{
			dir = DRIVER_DIR_STOP;
		}
		if(stop_moving){
	//		Serial.print("-stop_moving " );
	//		Serial.println(String(dis));
			send_hx_pos2(state_name, dir, value);				// send hall value to mainboard
			stepperIsReady = true;		//
		}else{
			stepperX.moveTo(target);
		}
	}else if(  driver == DRIVER_Y ){            // stepper Y
		// on wire: low_byte, high_byte, speed
		// in memory: 1=low_byte, 2=high_byte, 3=speed
		if(maxspeed == 0 ){
			maxspeed = 255;
		}
		moveYto(maxspeed,target);
	}else if( driver == DRIVER_Z ){            // stepper Z
		moveZto(target);
	}
}
/*
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
*/
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

long unsigned int decodeInt(String input, byte odetnij ){
  if(odetnij>0){
    input = input.substring(odetnij);    // obetnij znaki z przodu
  }
  return input.toInt();
}

void reload_servo(){      // in interrupt
	volatile ServoChannel &ser = servos[INNER_SERVOY];
	if( servo_lib[INNER_SERVOY].attached() && ser.last_pos != ser.target_pos ){
		long int this_distance =0;
		delta = 0;
		if( ser.last_pos > ser.target_pos ){
			this_distance  = ser.last_pos - ser.target_pos;
		}else if( ser.last_pos < ser.target_pos ){
			this_distance  = ser.target_pos - ser.last_pos;
		}
		int quoter = (ser.last_distance >> 2);            // this_distance zawsze sie zmiejsza
		if( this_distance < quoter){                      // ostatnia cwiatrka = zwalniaj
			delta = (ser.delta_pos * this_distance);
			delta = delta /quoter;
			//      Serial.print("delta4 = " );
		}else if( this_distance > (ser.last_distance - quoter)){        // pierwsza cwiatrka = przyspieszaj. tu zawsze this_distance > 3/4 * last_distance
			delta = (ser.delta_pos * (ser.last_distance - this_distance ) );      // tu zawsze (last_distance - this_distance ) < quoter
			delta = delta /quoter;
			//      Serial.print("delta1 = " );
		}else{  // na maxa
			//      Serial.print("delta2 = " );
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
			//     Serial.println("gotowe1");
		}else if( ser.delta_pos < 0 && ser.last_pos < ser.target_pos ){
			//      Serial.println("gotowe2");
			ser.last_pos = ser.target_pos;
		}
		ser.pos_changed = true;
/*
		if(ser.pos_changed){
		  Serial.print(String(delta));
		  Serial.print(" ");
		  Serial.print(String(ser.delta_pos));
		  Serial.print(" ");
		  Serial.print(String(ser.target_pos));
		  Serial.print(" ");
		  Serial.println(String(ser.last_pos));
		}*/
	}
}


void timer2(){
//	timer_counter++;
	//timer_now = true;
	stepperX.run();
//	ticks++;
	if((--prescaler) == 0 ){
		reload_servo();
		prescaler = SERVO_PRESCALER;
	}
}

void timer3(){
	stepperX.run();
	//if((--prescaler) == 0 ){
	//	prescaler = SERVO_PRESCALER * 2;		// 2 times slower
	//}
}

void send_hx_pos( byte stateId, int16_t value ) {
	//Serial.println("new state: " + String(stateId) + " - " + String(value) );
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
	send_hx_pos2(state_name, dir, value);
}

void send_hx_pos2( byte state_name, byte dir, int16_t value ) {
	bytepos.i		= stepperX.currentPosition();
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
	//Serial.print("-A0,");
	//Serial.println(String(value));
	sendln(ttt,10);
}

void sendln( volatile byte buffer[], byte length ){
	//Serial.write(buf, len);
	Serial.print(buffer[0]);
	for (int i=1; i<length; i++) { 
		Serial.print(",");	
		Serial.print(buffer[i]);
	}
	Serial.println();
	Serial.flush();
}
/*
void sendVal( byte n ) {
  int value =  analogRead( A0 + n ); 
  Serial.print( value );  
  Serial.print(",");  
}*/

int16_t readValue(byte pin) {           // synchroniczne
	int16_t val1 = analogRead( pin );
	val1 += analogRead( pin );
	val1 += analogRead( pin );
	val1 += analogRead( pin );
	val1 = val1 >>2;		// divide by 4 (2^2)
	return val1;
}

void stepperReady( long int pos ){		// in interrupt
	stepperIsReady = true;
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
