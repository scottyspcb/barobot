#define IS_IPANEL true
#include <WSWire.h>
#include <i2c_helpers.h>
#include <barobot_common.h>
#include <avr/eeprom.h>
#include <Servo.h>

unsigned int typical_zero = 512;
unsigned int last_max = 0;
unsigned int last_min = 0;

volatile byte input_buffer[IPANEL_BUFFER_LENGTH][5] = {{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0}};      // 6 buforow po 5 bajtów
volatile byte out_buffer[5];
volatile boolean was_event = false;

Servo servoY;
Servo servoZ;

uint16_t servo_y_last = 0;
uint16_t servo_z_last = 0;
boolean diddd = false;
volatile byte in_buffer1[5];

void setup(){
//  Serial.begin(38400);
  Serial.begin(115200);
  Serial.println("wozek start"); 
  my_address = I2C_ADR_IPANEL;
  Wire.begin(I2C_ADR_IPANEL);
  Wire.onReceive(receiveEvent);
  Wire.onRequest(requestEvent);
  send_here_i_am();  // wyslij ze oto jestem
  pinMode(PIN_IPANEL_LED_TEST, OUTPUT);
  diddd = !diddd;
  digitalWrite(PIN_IPANEL_LED_TEST, true);
}

long int mil = 0;
long int milis100 = 0;


void loop() {
  mil = millis();
  if( mil > milis100 + 4000 ){    // co 4 sek
        milis100 = mil;
  }
/*
  for( byte i=0;i<BUFFER_LENGTH;i++){
    if( input_buffer[i][0] ){
      proceed( input_buffer[i] );
      input_buffer[i][0] = 0;
    }
  }*/ 
}

void proceed( volatile byte buffer[5] ){
  if(buffer[0] == 0x21){    // slave input pin value
//    printHex(buffer[1], false );
  String ss = "- IN " + String(buffer[2]) + ": " + String(buffer[3]);
  Serial.println(ss);
  }else if(buffer[0] == 0x22){    // poke - wcisnieto przycisk
  }else if( buffer[0]  == 0x29 ){          // TEPE + VERSION       3 bajty
      byte ttt[2] = {UPANEL_DEVICE_TYPE,UPANEL_VERSION};
      Wire.write(ttt,2);
  }else if( buffer[0]  == 0x2A ){    // return xor
      byte res = in_buffer1[1] ^ in_buffer1[2];
      Wire.write(res);
      if( res & 1 ){    // ustawiony najmlodzzy bit
        diddd = !diddd;
        digitalWrite(PIN_IPANEL_LED_TEST, diddd);
      }
  }else{
    Serial.print("recieve unknown - ");
    printHex(buffer[0]);
  }
  buffer[0] = 0;  //ready
}



void receiveEvent(int howMany){
  if(!howMany){
     return;
  }
  byte cnt = 0;
  volatile byte (*buffer) = 0;
  Serial.print("input " );
  for( byte a = 0; a < IPANEL_BUFFER_LENGTH; a++ ){
    if(input_buffer[a][0] == 0 ){
      buffer = (&input_buffer[a][0]); 
      while( Wire.available()){ // loop through all but the last
        byte w =  Wire.read(); // receive byte as a character
        *(buffer +(cnt++)) = w;
        printHex(w, false ); 
      }
      Serial.println(""); 
      return;
    }
  }
  Serial.println(" - pelno"); 
}

void requestEvent(){ 
  // w in_buffer jest polecenie
    byte command = in_buffer1[0];
    if( command == 0x29 ){          // TEPE + VERSION       3 bajty
        byte ttt[2] = {IPANEL_DEVICE_TYPE,IPANEL_VERSION};
        Wire.write(ttt,2);
    }else if( command == 0x2A ){    // return xor
        byte res = in_buffer1[1] ^ in_buffer1[2];
        Wire.write(res);
        if( res & 1 ){    // ustawiony najmlodzzy bit
          diddd = !diddd;
          digitalWrite(PIN_IPANEL_LED_TEST, diddd);
        }
    }
}

static void send_pin_value( byte pin, byte value ){
  byte ttt[4] = {0x21,my_address,pin,value};
  send(ttt,4);
 // Serial.println("out "+ String( my_address ) +" / "+ String( pin ) +"/"+ String(value));
}

static void send_here_i_am(){
  byte ttt[4] = {0x23,my_address,TROLLEY_DEVICE_TYPE,TROLLEY_VERSION};
  Serial.println("hello "+ String( my_address ));  
  send(ttt,4);
}
byte send( byte buffer[], byte ss ){
  Wire.beginTransmission(I2C_ADR_MAINBOARD);  
  Wire.write(buffer,ss);
  byte error = Wire.endTransmission();
  Serial.println("out "+ String( my_address ) +": ("+ String( buffer[0] ) +","+ String(buffer[1])+ ") e: " + String(error));
  return error;
}


