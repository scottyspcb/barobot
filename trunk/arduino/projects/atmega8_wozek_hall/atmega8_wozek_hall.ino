#define IS_IPANEL true
#define HAS_LEDS true
#include <WSWire.h>
#include <i2c_helpers.h>
#include <barobot_common.h>
#include <avr/eeprom.h>
#include <Servo.h>
#include <FlexiTimer2.h>

#define UNCONNECTED_LEVEL  3

//unsigned int typical_zero = 512;
//unsigned int last_max = 0;
//unsigned int last_min = 0;

volatile byte input_buffer[IPANEL_BUFFER_LENGTH][5] = {{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0}};      // 6 buforow po 5 bajtów
volatile byte last_index     = 0;
volatile unsigned int ticks  = 0;
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

Servo servoY;
Servo servoZ;

uint16_t servo_y_last = 0;
uint16_t servo_z_last = 0;
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

  pinMode(PIN_IPANEL_LED0_NUM, OUTPUT);
  pinMode(PIN_IPANEL_LED1_NUM, OUTPUT);
  pinMode(PIN_IPANEL_LED2_NUM, OUTPUT);
  pinMode(PIN_IPANEL_LED3_NUM, OUTPUT);
  pinMode(PIN_IPANEL_LED4_NUM, OUTPUT);
  pinMode(PIN_IPANEL_LED5_NUM, OUTPUT);
  pinMode(PIN_IPANEL_LED6_NUM, OUTPUT);
  pinMode(PIN_IPANEL_LED7_NUM, OUTPUT);

  digitalWrite(PIN_IPANEL_LED0_NUM, LOW);
  digitalWrite(PIN_IPANEL_LED1_NUM, HIGH);
  digitalWrite(PIN_IPANEL_LED2_NUM, HIGH);
  digitalWrite(PIN_IPANEL_LED3_NUM, HIGH);
  digitalWrite(PIN_IPANEL_LED4_NUM, HIGH);
  digitalWrite(PIN_IPANEL_LED5_NUM, HIGH);
  digitalWrite(PIN_IPANEL_LED6_NUM, HIGH);
  digitalWrite(PIN_IPANEL_LED7_NUM, HIGH);

  DEBUGINIT();
  DEBUGLN("wozek start"); 
  my_address = I2C_ADR_IPANEL;
  Wire.begin(I2C_ADR_IPANEL);
  Wire.onReceive(receiveEvent);
  Wire.onRequest(requestEvent);
  send_here_i_am();  // wyslij ze oto jestem
  diddd = !diddd;
  FlexiTimer2::set(40, 1.0/100, timer);
  FlexiTimer2::start();
}
unsigned long milisAnalog = 0;
long int mil = 0;
//long int milis100 = 0;

void loop() {
  mil = millis();
//  analogRead(PIN_IPANEL_HALL_Y );    // very often
//  analogRead(PIN_IPANEL_HALL_X );    // often
//  analogRead(PIN_IPANEL_WEIGHT );    // sometimes
 
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

  // analizuj bufor wejsciowy i2c
  for( byte i=0;i<IPANEL_BUFFER_LENGTH;i++){
    if( input_buffer[i][0] >0 && bit_is_clear(input_buffer[i][0], 0 )){    // bez xxxx xxx1 b
      proceed( input_buffer[i] );
      input_buffer[i][0] = 0;
    }
  }
}

void timer(){
  ticks++;
  digitalWrite(PIN_IPANEL_LED7_NUM,  !digitalRead(PIN_IPANEL_LED7_NUM));    // Toggle led. Read from register (not from pin)
  
  
  
}

void proceed( volatile byte buffer[5] ){
  DEBUG("proceed - ");
  printHex(buffer[0], false);
  DEBUG(" ");
  printHex(buffer[1], false);
  DEBUG(" ");
  printHex(buffer[2]);

  if( buffer[0] == METHOD_PROG_MODE_ON ){         // prog mode on
    digitalWrite(PIN_IPANEL_LED1_NUM, LOW);
    if(buffer[1] == my_address){
      prog_me = true;
      digitalWrite(LED_TOP_GREEN, LOW);
      digitalWrite(LED_BOTTOM_GREEN, LOW);  
      digitalWrite(LED_TOP_RED, LOW);
      digitalWrite(LED_BOTTOM_RED, LOW);
    }else{
      prog_me = false;
    }
    prog_mode = true;
  }else if( buffer[0] == METHOD_PROG_MODE_OFF ){         // prog mode off
    digitalWrite(PIN_IPANEL_LED1_NUM, HIGH);
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
    if(buffer[1] == 1){
      servoZ.attach(PIN_IPANEL_SERVO_Y);
    }else if(buffer[1] == 2){
      servoZ.attach(PIN_IPANEL_SERVO_Z);
    }
  }else if( buffer[0] == METHOD_DRIVER_DISABLE ){
    if(buffer[1] == 1){
      servoZ.detach();
    }else if(buffer[1] == 2){
      servoZ.detach();
    }
  }else if( buffer[0] == METHOD_GOTOSERVOYPOS ){
    // on wire: low_byte, high_byte
    // in memory: low_byte, high_byte
    byte speed    = buffer[3];
    uint16_t t = buffer[2] << 8 + buffer[1];    // little endian

  }else if( buffer[0] == METHOD_GOTOSERVOZPOS ){
    uint16_t t = buffer[2] << 8 + buffer[1];    // little endian

  }else if( buffer[0] == METHOD_SETPWM ){
    byte led    = buffer[1];
    byte level  = buffer[2];
    
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
    DEBUG("proceed unknown - ");
    printHex(buffer[0], false);
    DEBUG(" ");
    printHex(buffer[1], false);
    DEBUG(" ");
    printHex(buffer[2]);
  }
  buffer[0] = 0;  //ready
}

void receiveEvent(int howMany){
  if(!howMany){
     return;
  }
  byte cnt = 0;
  volatile byte (*buffer) = 0;
  DEBUG("input " );
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
        byte ttt[2] = {servo_y_last >>8,servo_y_last && 0xFF};
        Wire.write(ttt,2);

    }else if( command == METHOD_GETSERVOZPOS ){         // getServoZPos  
        byte ttt[2] = {servo_z_last>>8,servo_z_last && 0xFF};
        Wire.write(ttt,2);

    }else if( command == METHOD_TEST_SLAVE ){    // return xor
        byte res = input_buffer[last_index][1] ^ input_buffer[last_index][2];
        Wire.write(res);
        if( res & 1 ){    // ustawiony najmlodzzy bit
          diddd = !diddd;
          digitalWrite(PIN_IPANEL_LED1_NUM, diddd);
        }      
    }else if( command == METHOD_GETANALOGVALUE ){    
    }else if( command == METHOD_GETVALUE ){    
    }else if( command == METHOD_RESET_NEXT ){
    }else if( command == METHOD_RUN_NEXT ){
    }else if(!prog_mode){
      DEBUG("requestEvent unknown - ");
      printHex(input_buffer[last_index][0], false);
      DEBUG(" ");
      printHex(input_buffer[last_index][1], false);
      DEBUG(" ");
      printHex(input_buffer[last_index][2]);
    }
    input_buffer[last_index][0] = 0;
}

static void send_pin_value( byte pin, byte value ){
  byte ttt[5] = {METHOD_I2C_SLAVEMSG,0x21,my_address,pin,value};
  send(ttt,5);
 // DEBUGLN("out "+ String( my_address ) +" / "+ String( pin ) +"/"+ String(value));
}

static void send_here_i_am(){
  byte ttt[4] = {METHOD_HERE_I_AM,my_address,IPANEL_DEVICE_TYPE,IPANEL_VERSION};
  DEBUGLN("hello "+ String( my_address ));  
  send(ttt,4);
}

void send( byte buffer[], byte ss ){
  if(prog_mode){
    return;
  }
  byte ret = 1;
  byte licznik = 250;
  while( ret && licznik++ ){    // prubuj 5 razy, zakoncz gdy error = 0;
    Wire.beginTransmission(I2C_ADR_MAINBOARD);  
    Wire.write(buffer,ss);
    ret = Wire.endTransmission();
//    DEBUG("send"+String(licznik) +": " + String( my_address ) +": ");
//    printHex(buffer[0], false ); 
//    DEBUG(", ");
//    printHex(buffer[1], false ); 
//    DEBUGLN(" / "+ String(ret) );

      DEBUG("out "+ String( my_address ) +": (" );
      printHex( buffer[0], false ); 
      DEBUG(" ");
      printHex( buffer[1], false ); 
      DEBUG(" ");
      printHex( buffer[2], false ); 
      DEBUG(" ");
      printHex( buffer[3], false ); 
      DEBUGLN( ") e: " + String(ret));
  }
//  return ret;
}


