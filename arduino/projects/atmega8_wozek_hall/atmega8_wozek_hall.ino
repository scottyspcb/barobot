#define IS_IPANEL true
#define HAS_LEDS true
#include <WSWire.h>
#include <i2c_helpers.h>
#include <barobot_common.h>
#include <avr/eeprom.h>
#include <Servo.h>
#include <FlexiTimer2.h>
#define UNCONNECTED_LEVEL  3
#define MIN_DELTA  20
//unsigned int typical_zero = 512;
//unsigned int last_max = 0;
//unsigned int last_min = 0;
volatile byte input_buffer[IPANEL_BUFFER_LENGTH][5] = {{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0}};      // 6 buforow po 5 bajtów
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
	} ;

        Servo servo_lib[2];
	volatile ServoChannel servos[2]= {
		{PIN_IPANEL_SERVO_Y,0,0,0,0,false,false},
		{PIN_IPANEL_SERVO_Z,0,0,0,0,false,false},
	};

volatile boolean read_hallx  = false;
volatile boolean read_hally  = false;
volatile boolean read_weight = false;

// oscyloskop
boolean analog_reading = false;
byte analog_num = 0;
unsigned int analog_speed = 0;
byte analog_repeat = 0;	
byte analog_pos = 0;
unsigned long analog_sum = 0;
// koniec oscyloskop

boolean diddd = false;

void setup(){
  pinMode(PIN_IPANEL_SCK, INPUT );         // stan wysokiej impedancji
  pinMode(PIN_IPANEL_MISO, INPUT );        // stan wysokiej impedancji
  pinMode(PIN_IPANEL_MOSI, INPUT );        // stan wysokiej impedancji

  pinMode(PIN_IPANEL_SERVO_Y, OUTPUT);
  pinMode(PIN_IPANEL_SERVO_Z, OUTPUT);

  pinMode(PIN_IPANEL_HALL_X, INPUT);
  pinMode(PIN_IPANEL_HALL_Y, INPUT);
  pinMode(PIN_IPANEL_WEIGHT, INPUT); 

  init_leds();

  DEBUGINIT();
  DEBUGLN("-wozek start"); 
  my_address = I2C_ADR_IPANEL;
  Wire.begin(I2C_ADR_IPANEL);
  Wire.onReceive(receiveEvent);
  Wire.onRequest(requestEvent);
  send_here_i_am();  // wyslij ze oto jestem
  diddd = !diddd;
  FlexiTimer2::set(40, 1.0/100, timer);
  FlexiTimer2::start();
}

void init_leds(){
  for (uint8_t i = 0; i < COUNT_IPANEL_ONBOARD_LED; ++i){
    uint8_t pin = _pwm_channels[i].pin;
    _pwm_channels[i].outport = portOutputRegister(digitalPinToPort(pin));
    _pwm_channels[i].pinmask = digitalPinToBitMask(pin);
    pinMode(pin, OUTPUT);
    #if UPANEL_COMMON_ANODE
     *_pwm_channels[i].outport |= _pwm_channels[i].pinmask;                // turn off the channel (set GND)
    #else
     *_pwm_channels[i].outport &= ~(_pwm_channels[i].pinmask);             // turn off the channel (set VCC)
    #endif
  }
  digitalWrite(PIN_PANEL_LED0_NUM, HIGH);
}

unsigned long milisAnalog = 0;
unsigned long int mil = 0;
long int milis100 = 0;

byte iii = 0;
void loop() {
  mil = millis();
	if( analog_reading &&  mil > milisAnalog ){
		milisAnalog = mil+ analog_speed;
		if( analog_pos == analog_repeat ){			// wyślij
                        Serial.print( "A" );
                        Serial.print( analog_num );
                        Serial.print( " " );
                        Serial.println( analog_sum );
			analog_pos = 0;
			analog_sum = 0;
		}
		analog_pos++;
		analog_sum	+= analogRead(analog_num);
	}

  update_servo( INNER_SERVOY );
  update_servo( INNER_SERVOZ );

  	if( mil > milis100 ){    // debug, mrygaj co 1 sek
          uint8_t pin = _pwm_channels[iii].pin; 
          DEBUG( "-pin " );
          DEBUG( iii );
          DEBUG( "/" );
          DEBUGLN( pin );
          digitalWrite(_pwm_channels[0].pin, false);
          digitalWrite(_pwm_channels[1].pin, false);
          digitalWrite(_pwm_channels[2].pin, false);
          digitalWrite(_pwm_channels[3].pin, false);
          digitalWrite(_pwm_channels[4].pin, false);
          digitalWrite(_pwm_channels[5].pin, false);       
          digitalWrite(_pwm_channels[6].pin, false);
          digitalWrite(_pwm_channels[7].pin, false);
          digitalWrite(pin, true);
          milis100 = mil + 2000;
          iii++;
          iii = iii %COUNT_IPANEL_ONBOARD_LED;
  	}


  // analizuj bufor wejsciowy i2c
  for( byte i=0;i<IPANEL_BUFFER_LENGTH;i++){
    if( input_buffer[i][0] >0 && bit_is_clear(input_buffer[i][0], 0 )){    // bez xxxx xxx1 b
      proceed( input_buffer[i] );
      input_buffer[i][0] = 0;
    }
  }
}

void update_servo( byte index ) {           // synchroniczne
  if( servos[index].pos_changed == true ){  // mam byc gdzie indziej
    DEBUG( "-przesuwam Y " );
    DEBUGLN( String(servos[index].last_pos) );
    servo_lib[index].writeMicroseconds(servos[index].last_pos);
    servos[index].pos_changed = false;
    if( servos[index].last_pos == servos[index].target_pos){
      DEBUGLN( "-gotowe servo" );
      send_servo(false, localToGlobal(index) );
    }
  }
}

void reload_servo( byte index ){      // in interrupt
  volatile ServoChannel &ser = servos[index];
  if( ser.enabled && ser.last_pos != ser.target_pos ){
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
//  digitalWrite(PIN_PANEL_LED7_NUM,  !digitalRead(PIN_PANEL_LED7_NUM));    // Toggle led. Read from register (not from pin)
  reload_servo(INNER_SERVOY);
  reload_servo(INNER_SERVOZ);  
}

// czytaj komendy i2c
void proceed( volatile byte buffer[5] ){
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

  if( buffer[0] == METHOD_PROG_MODE_ON ){         // prog mode on
    digitalWrite(PIN_PANEL_LED1_NUM, HIGH);
    if(buffer[1] == my_address){
      prog_me = true;
      digitalWrite(LED_TOP_RED, HIGH);
      digitalWrite(LED_BOTTOM_RED, HIGH);
    }else{
      prog_me = false;
    }
    prog_mode = true;
  }else if( buffer[0] == METHOD_PROG_MODE_OFF ){         // prog mode off
    digitalWrite(PIN_PANEL_LED1_NUM, LOW);
    prog_mode = false;
    prog_me = false;

  }else if( buffer[0] == METHOD_LIVE_OFF ){         // LIVE A OFF
	analog_reading	= false;

  }else if( buffer[0] == METHOD_LIVE_ANALOG ){         // LIVE A 3,100,5 // TODO method byte
      if(buffer[1] < 8){
        analog_num = buffer[1];      // analog num
        analog_speed = buffer[2];    // speed
        analog_repeat = buffer[3];   // repeat
        analog_pos = 0;
        analog_sum = 0;
        pinMode( A0 + analog_num, INPUT);      // numer portu analoga to nie numer pinu (w mega A0 to 54)
        analog_reading	= true;
      }
  }else if( buffer[0] == METHOD_DRIVER_ENABLE ){
    byte index = globalToLocal( buffer[1] );
    servo_lib[index].attach(servos[index].pin);
    servos[index].enabled= true;

  }else if( buffer[0] == METHOD_DRIVER_DISABLE ){
    byte index = globalToLocal( buffer[1] );
    servos[index].enabled= false;
    servo_lib[index].detach();
    servos[index].pos_changed = false;
  }else if( buffer[0] == METHOD_GOTOSERVOYPOS ){
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
  }else if( buffer[0] == METHOD_GOTOSERVOZPOS ){
    byte sspeed    = buffer[3];
    uint16_t target= buffer[2];           // little endian
    target= (target << 8);
    target+= buffer[1];    // little endian
    run_to(INNER_SERVOZ,sspeed,target);
  }else if( buffer[0] == METHOD_SETPWM ){
    byte led    = buffer[1];
    byte level  = buffer[2];
    if( level > 127){
      digitalWrite(led, HIGH);
    }else{
      digitalWrite(led, LOW);
    }
  }else if( buffer[0] == METHOD_SETTIME ){
    byte led   = buffer[1];
    byte on    = buffer[2];
    byte off   = buffer[2];
    
  }else if( buffer[0] == METHOD_SETFADING ){
    byte led   = buffer[1];
    byte on    = buffer[2];
    byte off   = buffer[2];

  }else if( buffer[0] == METHOD_RESETCYCLES ){
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
    servos[index].target_pos     = target;
    if( servos[index].target_pos < servos[index].last_pos ){    // jedz w dol
      servos[index].delta_pos = -sspeed;
      servos[index].last_distance = servos[index].last_pos - servos[index].target_pos;
    }else if( servos[index].target_pos > servos[index].last_pos ){    // jedz w gore
      servos[index].delta_pos = sspeed;
      servos[index].last_distance = servos[index].target_pos - servos[index].last_pos;
    }
    if(!servos[index].enabled){
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
  DEBUG("-input " );
  for( byte a = 0; a < IPANEL_BUFFER_LENGTH; a++ ){
    if(input_buffer[a][0] == 0 ){
      buffer = (&input_buffer[a][0]); 
      while( Wire.available()){ // loop through all but the last
        byte w =  Wire.read(); // receive byte as a character
        *(buffer +(cnt++)) = w;
        printHex(w, false ); 
      }
      DEBUGLN(""); 
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
        byte ttt[2] = {IPANEL_DEVICE_TYPE,IPANEL_VERSION};
        Wire.write(ttt,2);
        
    }else if( command == METHOD_GETSERVOYPOS ){         // getServoYPos
        byte ttt[2] = {servos[INNER_SERVOY].last_pos >>8,servos[INNER_SERVOY].last_pos && 0xFF};
        Wire.write(ttt,2);

    }else if( command == METHOD_GETSERVOZPOS ){         // getServoZPos  
        byte ttt[2] = {servos[INNER_SERVOZ].last_pos >>8,servos[INNER_SERVOZ].last_pos && 0xFF};
        Wire.write(ttt,2);

    }else if( command == METHOD_TEST_SLAVE ){    // return xor
        byte res = input_buffer[last_index][1] ^ input_buffer[last_index][2];
        Wire.write(res);
        if( res & 1 ){    // ustawiony najmlodzzy bit
          diddd = !diddd;
          digitalWrite(PIN_PANEL_LED1_NUM, diddd);
        }      
    }else if( command == METHOD_GETANALOGVALUE ){
    }else if( command == METHOD_GETVALUE ){
    }else if( command == METHOD_RESET_NEXT ){
    }else if( command == METHOD_RUN_NEXT ){
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

static void send_pin_value( byte pin, byte value ){
  byte ttt[5] = {METHOD_I2C_SLAVEMSG,RETURN_PIN_VALUE,my_address,pin,value};
  send(ttt,5);
 // DEBUGLN("-out "+ String( my_address ) +" / "+ String( pin ) +"/"+ String(value));
}
static void send_servo( boolean error, byte servo ){
  byte ttt[3] = {METHOD_I2C_SLAVEMSG, error ? RETURN_DRIVER_ERROR : RETURN_DRIVER_READY, servo};
  send(ttt,3);
 // DEBUGLN("-out "+ String( my_address ) +" / "+ String( pin ) +"/"+ String(value));
}

static void send_here_i_am(){
  byte ttt[4] = {METHOD_HERE_I_AM,my_address,IPANEL_DEVICE_TYPE,IPANEL_VERSION};
  DEBUGLN("-hello "+ String( my_address ));
  send(ttt,4);
}

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
//  analogRead(PIN_IPANEL_HALL_Y );    // very often
//  analogRead(PIN_IPANEL_HALL_X );    // often
//  analogRead(PIN_IPANEL_WEIGHT );    // sometimes
 
