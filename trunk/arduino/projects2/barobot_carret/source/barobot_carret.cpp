#define MAXCOMMAND_CARRET 	6
#define IS_CARRET true
#define HAS_LEDS true

#include "barobot_carret_main.h"
#include <WSWire.h>
#include <i2c_helpers.h>
#include <barobot_common.h>
#include "constants.h"
#include <Servo.h>
#include <FlexiTimer2.h>
#include <avr/io.h>
//#include <stdint.h>       // needed for uint8_t
#include <avr/interrupt.h>
#include <avr/io.h>

#define ANALOGS  6
#define ANALOG_TRIES  4

volatile uint16_t checks = 0;
//									  {0,1,2,3,4,5}
volatile int8_t ADCport[ANALOGS]	= {3,5,6,7,0,2};
volatile int16_t ADCvalue[ANALOG_TRIES][ANALOGS] = {{0,0,0,0,0,0},{0,0,0,0,0,0},{0,0,0,0,0,0},{0,0,0,0,0,0}};
volatile uint8_t channel = 0;
volatile uint8_t row = 0;

byte moving_x = DRIVER_DIR_STOP;
//byte moving_y = DRIVER_DIR_STOP;
//byte moving_z = DRIVER_DIR_STOP;

#define MIN_DELTA  20

//unsigned int typical_zero = 512;
//unsigned int last_max = 0;
//unsigned int last_min = 0;
volatile byte input_buffer[CARRET_BUFFER_LENGTH][MAXCOMMAND_CARRET] = {{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0}};      // 6 buforow po 5 bajtów
volatile byte last_index     = 0;
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
	{PIN_CARRET_SERVO_Y,0,0,0,0,false,false,DRIVER_DIR_STOP },
	{PIN_CARRET_SERVO_Z,0,0,0,0,false,false,DRIVER_DIR_STOP },
};

// oscyloskop
boolean analog_reading = false;
byte analog_num = 0;
unsigned int analog_speed = 0;
byte analog_repeat = 0;
byte analog_pos = 0;
byteint analog_sum;
// koniec oscyloskop

boolean diddd = false;
unsigned long milisAnalog = 0;
unsigned long mil = 0;

uint16_t cc = 0;
byte state_id = 0xff;
int16_t up_level = 0;
int16_t down_level = 0;

#define HISTORY_LENGTH  7

volatile int16_t hallx_state[HXSTATES][4] = {
	//{CODE, MIN, MAX }
	{HX_STATE_0,	HX_NEODYM_UP_BELOW,			1024,						0x0f},		// 11		ERROR
	{HX_STATE_1,	HX_NEODYM_UP_START,			HX_NEODYM_UP_BELOW-1,		0x08},		// 22		to jest neodym max
	{HX_STATE_2,	HX_FERRITE_UP_IS_BELOW,		HX_NEODYM_UP_START-1,		0x04},		// 33		wznosi si� neodym
	{HX_STATE_3,	HX_LOCAL_UP_MAX_OVER,		HX_FERRITE_UP_IS_BELOW-1,	0x02},		// 44		czubek lokalnego max
	{HX_STATE_4,	HX_NOISE_BELOW,				HX_LOCAL_UP_MAX_OVER-1,		0x01},		// 55		wznosi si�

	{HX_STATE_5,	HX_NOISE_OVER,				HX_NOISE_BELOW-1,			0x00},		// 66		neutralne

	{HX_STATE_6,	HX_LOCAL_DOWN_IS_BELOW,		HX_NOISE_OVER-1,			0x10},		// 77		opada
	{HX_STATE_7,	HX_FERRITE_DOWN_IS_BELOW,	HX_LOCAL_DOWN_IS_BELOW-1,	0x20},		// 88		czubek lokalnego min
	{HX_STATE_8,	HX_NEODYM_DOWN_START,		HX_FERRITE_DOWN_IS_BELOW-1,	0x40},		// 99		opada neodym
	{HX_STATE_9,	HX_NEODYM_DOWN_OVER,		HX_NEODYM_DOWN_START-1,		0x80},		// 100		to jest neodym min	
	{HX_STATE_10,	0,							HX_NEODYM_DOWN_OVER-1,		0xf0}		// 111		NOT CONNECTED	
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
	{'N',	0,		0,		0xf0}		// NOT CONNECTED	
};

String serial0Buffer = "";
boolean Console0Complete = false;   // This will be set to true once we have a full string

unsigned long int when_next = 0;
unsigned long int sending = 0b00000000;
unsigned long int time = 5000;
unsigned long int sum = 0;
unsigned long int repeat = 0;

void setup(){
	pinMode(PIN_CARRET_SCK, INPUT );         // stan wysokiej impedancji
	pinMode(PIN_CARRET_MISO, INPUT );        // stan wysokiej impedancji
	pinMode(PIN_CARRET_MOSI, INPUT );        // stan wysokiej impedancji

	pinMode(PIN_CARRET_SERVO_Y, INPUT );      // nie pozwalaj na przypadkowe machanie na starcie
	pinMode(PIN_CARRET_SERVO_Z, INPUT );      // nie pozwalaj na przypadkowe machanie na starcie

	pinMode(PIN_CARRET_HALL_X, INPUT);
	pinMode(PIN_CARRET_HALL_Y, INPUT);
	pinMode(PIN_CARRET_WEIGHT, INPUT);

	pinMode(PIN_CARRET_CURRENTY, INPUT);
	pinMode(PIN_CARRET_CURRENTZ, INPUT);

	init_leds();
	serial0Buffer = "";
	DEBUGINIT();
	DEBUGLN("-wozek start");
	my_address = I2C_ADR_CARRET;
	Wire.begin(I2C_ADR_CARRET);
	Wire.onReceive(receiveEvent);
	Wire.onRequest(requestEvent);
	send_here_i_am();  // wyslij ze oto jestem
	FlexiTimer2::set(40, 1.0/100, timer);
	FlexiTimer2::start();
	//init_analogs();
	init_hallx();
	sendstats();
}

void init_leds(){
	for (uint8_t i = 0; i < COUNT_CARRET_ONBOARD_LED; ++i){
		uint8_t pin = _pwm_channels[i].pin;
		_pwm_channels[i].outport = portOutputRegister(digitalPinToPort(pin));
		_pwm_channels[i].pinmask = digitalPinToBitMask(pin);
		pinMode(pin, OUTPUT);
		set_pin(i, false);
	}
}

byte common_anode = PIN_PANEL_LED_OFF_WHEN;
void set_pin( byte pin, boolean value ){
	boolean off_when = bitRead( common_anode, pin);
	if( (value ^ off_when) == true ){
	  *_pwm_channels[pin].outport |= _pwm_channels[pin].pinmask;
	}else{
	 *_pwm_channels[pin].outport &= ~(_pwm_channels[pin].pinmask);
	}
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

  if (Console0Complete) {
    parseInput( serial0Buffer );                      // parsuj wejscie
    Console0Complete = false;
    serial0Buffer = "";
  }

  if( mil > when_next ){    // debug, mrygaj co 1 sek
		boolean sendsth = false;
      if( bitRead(sending, 0 ) ){  sendVal(0); sendsth=true;  }
      if( bitRead(sending, 1 ) ){  sendVal(1); sendsth=true;   }
      if( bitRead(sending, 2 ) ){  sendVal(2); sendsth=true;   }
      if( bitRead(sending, 3 ) ){  sendVal(3); sendsth=true;   }
      if( bitRead(sending, 4 ) ){  sendVal(4); sendsth=true;   }
      if( bitRead(sending, 5 ) ){  sendVal(5); sendsth=true;   }
      if( bitRead(sending, 6 ) ){  sendVal(6); sendsth=true;   }
      if( bitRead(sending, 7 ) ){  sendVal(7); sendsth=true;   }
      if( bitRead(sending, 8 ) ){  sendVal(8); sendsth=true;   }
      if(sendsth){
		Serial.println();
      }
      when_next = mil + time;
  }
	//sendanalog();
	readHall();

	update_servo( INNER_SERVOY );
	update_servo( INNER_SERVOZ );

	// analizuj bufor wejsciowy i2c
	for( byte i=0;i<CARRET_BUFFER_LENGTH;i++){
	//    if( input_buffer[i][0] >0 && bit_is_clear(input_buffer[i][0], 0 )){    // bez xxxx xxx1 b
		if( input_buffer[i][0] >0 ){    // bez xxxx xxx1 b
			if( input_buffer[i][0] != METHOD_GETVERSION &&  input_buffer[i][0] != METHOD_TEST_SLAVE ){
				proceed( input_buffer[i] );
				input_buffer[i][0] = 0;
			}
		}
	}
//	Serial.println("tutaj8");
}

void sendanalog() {           // synchroniczne
	if( analog_reading &&  mil > milisAnalog ){
		milisAnalog = mil+ analog_speed;
		if( analog_pos == analog_repeat ){			// wysllij
		/*
			Serial.print( "A" );
			Serial.print( analog_num );
			Serial.print( " " );
			Serial.println( analog_sum );
		*/
			byte ttt[8] = {
				METHOD_I2C_SLAVEMSG,
				my_address, 
				METHOD_LIVE_ANALOG, 
				analog_num, 
				analog_sum.bytes[3],		// bits 0-7
				analog_sum.bytes[2],		// bits 8-15
				analog_sum.bytes[1],		// bits 16-23
				analog_sum.bytes[0]			// bits 24-32
			};
			send(ttt,8);
			analog_pos = 0;
			analog_sum.i = 0;
		}
		analog_pos++;
		analog_sum.i	+= ADCvalue[0][analog_num];
	}
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
	return analogRead(PIN_CARRET_HALL_X );
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
	if( moving_x != DRIVER_DIR_STOP 
		&& servos[INNER_SERVOY].moving == DRIVER_DIR_STOP 
		&& servos[INNER_SERVOZ].moving == DRIVER_DIR_STOP )
		{
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
//	Serial.println("moze");
//	Serial.flush();

	if( servos[index].pos_changed == true && !prog_mode){  // mam byc gdzie indziej
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
  byte command= input.charAt( 0 );
  unsigned int value= decodeInt( input, 1 );    // po komendzie zawsze jest liczba
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
void serialEvent(){                       // FUNKCJA WBUDOWANA - zbieraj dane z serial0 i serial3 i skadaj w komendy
  while (Serial.available() && !Console0Complete) {    // odczytuj gdy istnieja dane i poprzednie zostaly odczytane
    char inChar = (char)Serial.read(); 
    serial0Buffer += String(inChar);
    if (inChar == SEPARATOR_CHAR) {
      Console0Complete = true;
    }
  }
}
long unsigned decodeInt(String input, byte odetnij ){
  if(odetnij>0){
    input = input.substring(odetnij);    // obetnij znaki z przodu
  }
  return input.toInt();
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

void timer(){  // in interrupt
	ticks++;
	//DW(PIN_PANEL_LED1_NUM,  !digitalRead(PIN_PANEL_LED1_NUM));    // Toggle led. Read from register (not from pin)
	reload_servo(INNER_SERVOY);
	reload_servo(INNER_SERVOZ);
}

// czytaj komendy i2c
void proceed( volatile byte buffer[MAXCOMMAND_CARRET] ){
	//Serial.println("proceed1");
/*
	DEBUG("-proceed - ");
	DEBUG(String(buffer[0]));
	DEBUG(" ");
	printHex(buffer[1], false);
	DEBUG(" ");
	printHex(buffer[2], false);
	DEBUG(" ");
	printHex(buffer[3], false);
	DEBUG(" ");
	printHex(buffer[4]);
*/
	byte command = buffer[0];	 
	if( command == METHOD_PROG_MODE_ON ){         // prog mode on
		DW(PIN_PANEL_LED1_NUM, HIGH);
		if(buffer[1] == my_address){
			prog_me = true;
			DW(LED_TOP_RED, HIGH);
			DW(LED_BOTTOM_RED, HIGH);
		}else{
			prog_me = false;
		}
		prog_mode = true;
	}else if( command == METHOD_PROG_MODE_OFF ){         // prog mode off
		DW(PIN_PANEL_LED1_NUM, LOW);
		prog_mode = false;
		prog_me = false;

	}else if( command == METHOD_LIVE_OFF ){         // LIVE A OFF
		analog_reading	= false;

	}else if( command == METHOD_STEPPER_MOVING ){
		if( buffer[1] == DRIVER_X ){
			moving_x = buffer[2];
		}
		//DEBUG("-driver X moving:");
		//DEBUGLN(String(buffer[2]));

	}else if( command == METHOD_CHECK_NEXT ){
		byte ttt[4] = {METHOD_I2C_SLAVEMSG, my_address, METHOD_CHECK_NEXT, 0 };		// 0 = no device found (cant have device)
		send(ttt,4);

	}else if( command == METHOD_LIVE_ANALOG ){         // LIVE A 3,100,5 // TODO method byte
		if(buffer[1] < 8){
			analog_num		= buffer[1];   // analog num
			analog_speed	= buffer[2];   // speed
			analog_repeat	= buffer[3];   // repeat
			analog_pos		= 0;
			analog_sum.i	= 0;
		//	if( analog_num < 8){
		//		pinMode( A0 + analog_num, INPUT);      // numer portu analoga to nie numer pinu (w mega A0 to 54)
		//	}
			analog_reading	= true;
		}
	}else if( command == METHOD_DRIVER_ENABLE ){
		byte index = globalToLocal( buffer[1] );
		servo_lib[index].attach(servos[index].pin);
		servos[index].enabled= true;

		byte ttt[4] = {METHOD_I2C_SLAVEMSG, my_address, METHOD_DRIVER_ENABLE, buffer[1] };
		send(ttt,4);

	}else if( command == METHOD_DRIVER_DISABLE ){
		byte index = globalToLocal( buffer[1] );
		servos[index].enabled= false;
		servo_lib[index].detach();

		if( servos[index].target_pos != servos[index].last_pos ){    //  wyłączyłem w trakcie jechania
			 send_servo(false, localToGlobal(index), servos[index].target_pos );
		}
		DW(servos[index].pin, HIGH);
		//    pinMode(servos[index].pin, INPUT);
		servos[index].pos_changed = false;

		byte ttt[4] = {METHOD_I2C_SLAVEMSG, my_address, METHOD_DRIVER_DISABLE, localToGlobal(index) };
		send(ttt,4);


	}else if( command == METHOD_CAN_FILL ){
		boolean value = false;
		byte ttt[4] = {METHOD_I2C_SLAVEMSG, my_address, METHOD_CAN_FILL, value ? 1 : 0 };
		send(ttt,4);

	}else if( command == METHOD_SETLEDS ){
		byte i = COUNT_CARRET_ONBOARD_LED;
		while(i--){
			if( bitRead(buffer[1], i) ){
				_pwm_channels[i].pwmup = buffer[2];
				set_pin(i, (buffer[2] > 0));
			}
		}
    }else if( command == METHOD_ONECOLOR ){
		byte i = COUNT_UPANEL_ONBOARD_LED;
		while(i--){
			if( bitRead(buffer[1], i) ){
			  _pwm_channels[i].pwmup = buffer[2];
			  set_pin(i, (buffer[2] > 0));
			}else{
			  _pwm_channels[i].pwmup = 0;
			  set_pin(i, 0 );
			}
		}
	}else if( command == METHOD_SET_TOP_COLOR ){
		set_pin(0, (buffer[0] > 0));
		set_pin(1, (buffer[1] > 0));
		set_pin(2, (buffer[2] > 0));
		set_pin(3, (buffer[3] > 0));
	}else if( command == METHOD_SET_BOTTOM_COLOR ){
		set_pin(4, (buffer[0] > 0));
		set_pin(5, (buffer[1] > 0));
		set_pin(6, (buffer[2] > 0));
		set_pin(7, (buffer[3] > 0));
	
	}else if( command == METHOD_SET_Y_POS ){
		// on wire: low_byte, high_byte, speed
		// in memory: 1=low_byte, 2=high_byte, 3=speed
		byte sspeed    = buffer[3];
		uint16_t target= buffer[2];           // little endian
		target= (target << 8);
		target+= buffer[1];    // little endian
		DEBUG("SERVO Y speed ");
		DEBUG(String(sspeed));
		DEBUG(" target:");
		DEBUGLN(String(target));
		run_to(INNER_SERVOY,sspeed,target);

	}else if( command == METHOD_SET_Z_POS ){
		byte sspeed    = buffer[3];
		uint16_t target= buffer[2];           // little endian
		target= (target << 8);
		target+= buffer[1];    // little endian
		run_to(INNER_SERVOZ,sspeed,target);

	}else if( command == METHOD_GET_Y_POS ){
		byte ttt[5] = {METHOD_I2C_SLAVEMSG, my_address, METHOD_GET_Y_POS, (servos[INNER_SERVOY].last_pos & 0xFF),(servos[INNER_SERVOY].last_pos >>8) };
		send(ttt,5);

	}else if( command == METHOD_GET_Z_POS ){
		byte ttt[5] = {METHOD_I2C_SLAVEMSG, my_address, METHOD_GET_Z_POS, (servos[INNER_SERVOZ].last_pos & 0xFF),(servos[INNER_SERVOZ].last_pos >>8) };
		send(ttt,5);

	}else if( command == METHOD_GETANALOGVALUE ){
		byte source = buffer[1];
		if( source == INNER_HALL_X ){	
			int16_t val1 = readValue();
			byte newStateId = get_hx_state_id( val1 );
			byte state_name	= hallx_state[newStateId][0];
			byte ttt[10] = {
				METHOD_IMPORTANT_ANALOG, 
				INNER_HALL_X, 
				state_name,  		// STATE
				0,					// dir is unknown on carret
				0, 					// position is unknown on carret
				0,  				// position is unknown on carret
				0, 					// position is unknown on carret
				0,  				// position is unknown on carret
				(val1 & 0xFF),
				(val1 >>8),
			};
			send(ttt,10);

		}else if( source ==  INNER_HALL_Y ){ 
			int16_t val1 = analogRead(PIN_CARRET_HALL_X );
			byte newStateId = get_hy_state_id( val1 );
			byte state_name	= hally_state[newStateId][0];
			byte ttt[10] = {
				METHOD_IMPORTANT_ANALOG, 
				INNER_HALL_Y, 
				state_name, 		// STATE 
				0,					// dir is unknown on carret
				0, 					// position
				0,  				// position
				0, 					// position
				0,  				// position
				(val1 & 0xFF),
				(val1 >>8),
			};
			send(ttt,10); 
		}else if( source ==  INNER_WEIGHT ){ 
			int16_t val1 = analogRead(PIN_CARRET_WEIGHT );
			byte ttt[10] = {
				METHOD_IMPORTANT_ANALOG, 
				INNER_WEIGHT,
				0, 					// STATE
				0,					// dir is unknown on carret
				0, 					// position is unknown on carret
				0,  				// position is unknown on carret
				0, 					// position is unknown on carret
				0,  				// position is unknown on carret
				(val1 & 0xFF),
				(val1 >>8),
			};
			send(ttt,10); 

		}else if( source ==  INNER_CURRENTY ){
		
		}else if( source ==  INNER_CURRENTZ ){
		
		}else if( source ==  INNER_CARRET_TEMP ){
		
		}

	}else if( command == METHOD_GETVALUE ){
	//}else if( buffer[0] == METHOD_RESET_NEXT ){
	//}else if( buffer[0] == METHOD_RUN_NEXT ){
	}else if( command == METHOD_SETTIME ){
		//  byte led   = buffer[1];
		//  byte on    = buffer[2];
		//   byte off   = buffer[2];
		 
	}else if( command == METHOD_SETFADING ){
		//  byte led   = buffer[1];
		// byte on    = buffer[2];
		// byte off   = buffer[2];
		 
	}else if( command == METHOD_RESETCYCLES ){
		// resetuj cykl petli pwm



	}else{
		DEBUG("-proceed unknown - ");
		printHex(buffer[0], false);
		DEBUG(" ");
		printHex(buffer[1], false);
		DEBUG(" ");
		printHex(buffer[2]);
	}
	buffer[0] = 0;  //ready
}
 
void run_to(byte index, byte sspeed, uint16_t target){
	if(prog_mode){
		return;
	}
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
 
void receiveEvent(int howMany){
	if(!howMany){
		return;
	}
	byte cnt = 0;
	volatile byte (*buffer) = 0;
	//  DEBUG("-input " );
	for( byte a = 0; a < CARRET_BUFFER_LENGTH; a++ ){
		if(input_buffer[a][0] == 0 ){
			buffer = (&input_buffer[a][0]);
			while( Wire.available()){ // loop through all but the last
				byte w =  Wire.read(); // receive byte as a character
				*(buffer +(cnt++)) = w;
				//        printHex(w, false );
			}
			//      DEBUGLN("");
			last_index = a;
			return;
		}
	}
	DEBUGLN(" - pelno");
}

void requestEvent(){
	// w in_buffer jest polecenie
	byte command = input_buffer[last_index][0];
	if( command == METHOD_GETVERSION ){          // getVersion       3 bajty
		byte ttt[2] = {CARRET_DEVICE_TYPE,CARRET_VERSION};
		Wire.write(ttt,2);
		 
	}else if( command == METHOD_TEST_SLAVE ){    // return xor
		byte res = input_buffer[last_index][1] ^ input_buffer[last_index][2];
		Wire.write(res);
		if( res & 1 ){    // ustawiony najmlodzszy bit
			diddd = !diddd;
			DW(PIN_PANEL_LED1_NUM, diddd);
		}
	}else if(!prog_mode){
		DEBUG("-requestEvent unknown - ");
		printHex(input_buffer[last_index][0], false);
		DEBUG(" ");
		printHex(input_buffer[last_index][1], false);
		DEBUG(" ");
		printHex(input_buffer[last_index][2]);
	}
	input_buffer[last_index][0] = 0;
}

/*
static void send_pin_value( byte pin, byte value ){
  byte ttt[5] = {METHOD_I2C_SLAVEMSG,my_address, RETURN_PIN_VALUE,my_address,pin,value};
  send(ttt,5);
 // DEBUGLN("-out "+ String( my_address ) +" / "+ String( pin ) +"/"+ String(value));
}*/

void send_servo( boolean error, byte servo, uint16_t pos ){
	if(error){
		byte ttt[6] = {METHOD_I2C_SLAVEMSG, my_address, RETURN_DRIVER_READY, servo, (pos & 0xFF), (pos >>8) };
		send(ttt,6);
		byte ttt2[4] = {METHOD_EXEC_ERROR, my_address, RETURN_DRIVER_ERROR, servo};
		send(ttt2,4);
	}else{
		byte ttt[6] = {METHOD_I2C_SLAVEMSG, my_address, RETURN_DRIVER_READY, servo, (pos & 0xFF), (pos >>8) };
		send(ttt,6);
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
	Serial.println("new state: " + String(stateId) + " @ " + String(value) );
	byte i = COUNT_CARRET_ONBOARD_LED;
	while(i--){
		if( bitRead( hallx_state[stateId][3], i) ){
			set_pin(i, 1);
		}else{
			set_pin(i, 0);
		}
	}	
	byte state_name	= hallx_state[stateId][0];
	byte ttt[10] = {
		METHOD_IMPORTANT_ANALOG, 
		INNER_HALL_X, 
		state_name, 
		0,					// dir is unknown on carret
		0, 					// position is unknown on carret
		0,  				// position is unknown on carret
		0, 					// position is unknown on carret
		0,  				// position is unknown on carret
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
		(pos & 0xFF),			// position unknown on carret
		(pos >>8),				// position unknown on carret
		(value & 0xFF),
		(value >>8),
	}; 
	send(ttt,10);
}

void send_here_i_am(){
	byte ttt[4] = {METHOD_HERE_I_AM,my_address,CARRET_DEVICE_TYPE,CARRET_VERSION};
	//DEBUGLN("-hello "+ String( my_address ));
	send(ttt,4);
}
#define DEBUG_SEND	false
void send( byte buffer[], byte length ){
	if(prog_mode){
		return;
	}
	byte ret = 1;
	byte licznik = 250;
	while( ret && licznik++ ){    // prubuj 5 razy, zakoncz gdy error = 0;
		Wire.beginTransmission(I2C_ADR_MAINBOARD);
		Wire.write(buffer,length);
		ret = Wire.endTransmission();
		if(DEBUG_SEND){
			DEBUG("-send try:"+String(licznik) +", myadr: " + String( my_address ) +": ");
			DEBUG(buffer[0]);
			DEBUG(", ");
			DEBUG(buffer[1] );
			if(length > 2){
				DEBUG(", ");
				DEBUG(buffer[2] );
				if(length > 3){
					DEBUG(", ");
					DEBUG(buffer[3] );
				}
			}
			DEBUGLN(" ret: "+ String(ret) );
		}
	}
}

byte globalToLocal( byte ind ){      // get global device index used in android
	if( ind == DRIVER_Y ){
		return INNER_SERVOY;
	}
	return INNER_SERVOZ;  // DRIVER_Z
}
byte localToGlobal( byte ind ){      // get global device index used in android
	if( ind == INNER_SERVOY ){
		return DRIVER_Y;
	}
	return DRIVER_Z;  // INNER_SERVOZ
}
//  analogRead(PIN_CARRET_HALL_Y );    // very often
//  analogRead(PIN_CARRET_HALL_X );    // often
//  analogRead(PIN_CARRET_WEIGHT );    // sometimes

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

/*
  	if( mil > milis2000 ){    // debug, mrygaj co 1 sek
          uint8_t pin = _pwm_channels[iii].pin;
          DEBUG( "-pin " );
          DEBUG( iii );
          DEBUG( "/" );
          DEBUGLN( pin );
          DW(_pwm_channels[0].pin, false);
          DW(_pwm_channels[1].pin, false);
          DW(_pwm_channels[2].pin, false);
          DW(_pwm_channels[3].pin, false);
          DW(_pwm_channels[4].pin, false);
          DW(_pwm_channels[5].pin, false);
          DW(_pwm_channels[6].pin, false);
          DW(_pwm_channels[7].pin, false);
          DW(pin, true);
          milis2000 = mil + 500;
          iii++;
          iii = iii %COUNT_CARRET_ONBOARD_LED;
  	}
*/
 
 
