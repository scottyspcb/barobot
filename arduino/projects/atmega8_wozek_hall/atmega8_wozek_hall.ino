#define IS_TROLLEY true
#include <WSWire.h>
#include <i2c_helpers.h>
#include <barobot_common.h>
#include <avr/eeprom.h>
#include <Servo.h>

unsigned int typical_zero = 512;
unsigned int last_max = 0;
unsigned int last_min = 0;

Servo servoY;
Servo servoZ;

uint16_t servo_y_last = 0;
uint16_t servo_z_last = 0;

volatile bool read_local = false;
volatile byte in_buffer1[5];

void setup(){
//  Serial.begin(38400);
//  Serial.begin(115200);
  my_address = I2C_ADR_TROLLEY;
  Wire.begin(I2C_ADR_TROLLEY);
  Wire.onReceive(receiveEvent);
  Wire.onRequest(requestEvent);
  send_here_i_am();  // wyslij ze oto jestem
}

long int mil = 0;
long int milis100 = 0;
boolean diddd = false;

void loop() {
  mil = millis();

    if( mil > milis100 + 4000 ){    // co 4 sek
          milis100 = mil;
    }

    if( read_local&& in_buffer1[0] ){          // komendy bez odpowiedzi tutaj:
      byte command = in_buffer1[0];
      if( command == 0x11 ){                // PWM     3 bajty
           // setPWM(in_buffer1[1],in_buffer1[2]);
 //           leds[in_buffer1[1]].wypelnienie = in_buffer1[2];
      }else if( command == 0x10 ){          // reset
      
       // servoY.writeMicroseconds(up_pos);			 // na doł
      
      }
      in_buffer1[0] = 0;
      read_local = false;
   }
}

void receiveEvent(int howMany){
  byte cntr   = 0;
  byte aa     = 0;
  while( Wire.available()){ // loop through all but the last  
    aa = Wire.read(); // receive byte as a character
    in_buffer1[cntr] = aa;
    cntr++;
  }
  byte sss = (in_buffer1[0] >> 4);
  if ( sss == 1 ){      // najstarsze 8 bitów RÓWNE 1 to wykonaj w głównym wątku
      read_local = true;  
  }
  // w tym miejscu jednynie proste komendy nie wymagające zwrotek
}

void requestEvent(){ 
  // w in_buffer jest polecenie
    byte command = in_buffer1[0];
    if( command == 0x26 ){  // get analog value
    /*
        uint16_t value = analogRead(in_buffer1[1]);
        byte ttt[2]    = {value>>8, value & 0xff };
        Wire.write(ttt,2);*/
    }else if( command == 0x28 ){  // get digital value
      /*  boolean value  = digitalRead(in_buffer1[1]);
        byte ttt[1]    = {value ? 0xff:0xff};
        Wire.write(ttt,1);*/
    }else if( command == 0x29 ){          // TEPE + VERSION       3 bajty
        byte ttt[2] = {TROLLEY_DEVICE_TYPE,TROLLEY_VERSION};
        Wire.write(ttt,2);
    }else if( command == 0x2A ){    // return xor
        byte res = in_buffer1[1] ^ in_buffer1[2];
        Wire.write(res);
        if( res & 1 ){    // ustawiony najmlodzzy bit
//          diddd = !diddd;
//          digitalWrite(13, diddd);
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
  send(ttt,4);
//  Serial.println("hello "+ String( my_address ));  
}
void send( byte buffer[], byte ss ){
  Wire.beginTransmission(I2C_ADR_MAINBOARD);  
  Wire.write(buffer,ss);
  byte error = Wire.endTransmission();
//  Serial.println("out "+ String( my_address ) +" / "+ String( pin ) +"/"+ String(value)+ "/e:" + String(error));
}



