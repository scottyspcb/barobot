#define IS_TROLLEY true
/*
Komponenty:

HALL_X
HALL_Y

SERVO_Y
SERVO_Z

WEIGHT_SENSOR
NEXT_RESET
I2C

*/
// pin01  arduino --  PC6	RESET           - CONN1
// pin02  arduino 00  PD0	RX		- CONN2
// pin03  arduino 01  PD1	TX		- CONN2
// pin04  arduino 02  PD2	INT0		- SWITCH
// pin05  arduino 03  PD3	INT1		- CONN1
// pin06  arduino 04  PD4		        -
// pin07  arduino --  VCC
// pin08  arduino --  GND
// pin09  arduino ??  PB6	XTAL1		- 
// pin10  arduino ??  PB7	XTAL2		- 
// pin11  arduino 05  PD5                       - 
// pin12  arduino 06  PD6                       -
// pin13  arduino 07  PD7                       -
// pin14  arduino 08  PB0                       - 

// pin15  arduino 09  PB1			- SERVO_X
// pin16  arduino 10  PB2	SS		- SERVO_Y
// pin17  arduino 11  PB3	MOSI		- CONN1
// pin18  arduino 12  PB4	MISO		- CONN1
// pin19  arduino 13  PB5	SCK		- CONN1
// pin20  arduino --  AVCC
// pin21  arduino --  AREF
// pin22  arduino --  GND
// pin23  arduino A0/D14  PC0	ADC0		- HALL_X 
// pin24  arduino A1/D15  PC1	ADC1		- HALL_Y 
// pin25  arduino A2/D16  PC2	ADC2		- WEIGHT
// pin26  arduino A3/D17  PC3	ADC3		- 
// pin27  arduino A4/D18  PC4	ADC4	SDA	- CONN1
// pin28  arduino A5/D19  PC5	ADC5	SCL	- CONN1

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

unsigned int servo_y_last = 0;
unsigned int servo_z_last = 0;

unsigned int servo_y_max_pos = 0;
unsigned int servo_y_min_pos = 0;

unsigned int servo_z_max_pos = 0;
unsigned int servo_z_min_pos = 0;


volatile bool use_local = false;
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

    if( use_local&& in_buffer1[0] ){          // komendy bez odpowiedzi tutaj:
      byte command = in_buffer1[0];
      if( command == 0x11 ){                // PWM     3 bajty
           // setPWM(in_buffer1[1],in_buffer1[2]);
 //           leds[in_buffer1[1]].wypelnienie = in_buffer1[2];
      }else if( command == 0x10 ){          // reset
      }
      in_buffer1[0] = 0;
      use_local = false;
   }
}

void receiveEvent(int howMany){
  byte cntr = 0;
  byte aa = 0;
  while( Wire.available()){ // loop through all but the last  
    aa = Wire.read(); // receive byte as a character
    in_buffer1[cntr] = aa;
    cntr++;
  }
  byte sss = (in_buffer1[0] >> 4);
  if ( sss == 1 ){      // najstarsze 8 bitów RÓWNE 1 to wykonaj w głównym wątku
      use_local = true;  
  }
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
  byte ttt[2] = {0x23,my_address};
  send(ttt,2);
//  Serial.println("hello "+ String( my_address ));  
}
void send( byte buffer[], byte ss ){
  Wire.beginTransmission(I2C_ADR_MAINBOARD);  
  Wire.write(buffer,ss);
  byte error = Wire.endTransmission();
//  Serial.println("out "+ String( my_address ) +" / "+ String( pin ) +"/"+ String(value)+ "/e:" + String(error));
}



