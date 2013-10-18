#include <WSWire.h>
#include <arduino.h>
#include <i2c_helpers.h>
#include <avr/eeprom.h>

// ATMEL ATMEGA8 & 168 / ARDUINO
//
//                  +-\/-+
//            PC6  1|    |28  PC5 (AI 5/ D19)
//      (D 0) PD0  2|    |27  PC4 (AI 4/ D18)
//      (D 1) PD1  3|    |26  PC3 (AI 3/ D17)
//      (D 2) PD2  4|    |25  PC2 (AI 2/ D16)
// PWM+ (D 3) PD3  5|    |24  PC1 (AI 1/ D15)
//      (D 4) PD4  6|    |23  PC0 (AI 0/ D14)
//            VCC  7|    |22  GND
//            GND  8|    |21  AREF
//            PB6  9|    |20  AVCC
//            PB7 10|    |19  PB5 (D 13)
// PWM+ (D 5) PD5 11|    |18  PB4 (D 12)
// PWM+ (D 6) PD6 12|    |17  PB3 (D 11) PWM
//      (D 7) PD7 13|    |16  PB2 (D 10) PWM
//      (D 8) PB0 14|    |15  PB1 (D 9) PWM
//                  +----+

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
// pin11  arduino 05  PD5                       - LED R TOP
// pin12  arduino 06  PD6                       - LED G TOP
// pin13  arduino 07  PD7                       - LED B TOP
// pin14  arduino 08  PB0                       - LED W TOP

// pin15  arduino 09  PB1			- 
// pin16  arduino 10  PB2	SS		- 
// pin17  arduino 11  PB3	MOSI		- CONN1
// pin18  arduino 12  PB4	MISO		- CONN1
// pin19  arduino 13  PB5	SCK		- CONN1
// pin20  arduino --  AVCC
// pin21  arduino --  AREF
// pin22  arduino --  GND
// pin23  arduino A0  PC0	ADC0		- 
// pin24  arduino A1  PC1	ADC1		- 
// pin25  arduino A2  PC2	ADC2		- 
// pin26  arduino A3  PC3	ADC3		- 
// pin27  arduino A4  PC4	ADC4	SDA	- CONN1
// pin28  arduino A5  PC5	ADC5	SCL	- CONN1

#define LEFT_RESET_PIN 14

// to jest slave
#define VERSION 0x01
#define DEVICE_TYPE 0x10
#define MASTER_ADDR 0x01
#define LEFT_RESET_PIN 4
#define MY_POKE_PIN 4

#define LEDSIZE 8
typedef struct {
  byte pin;
  byte wypelnienie;	        // 8 bitów		0 - 256
  uint16_t on_time;	        // 16bitów		0 - 65536 ms max
  uint16_t off_time;	        // 16bitów		0 - 65536 ms max
}
LED;

LED leds[LEDSIZE] = {
  {8, 10, 100, 100   },
  {9, 10, 100, 100   },
  {8, 10, 100, 100   },
  {9, 10, 100, 100   },
  {8, 10, 100, 100   },
  {9, 10, 100, 100   },
  {8, 10, 100, 100   },
  {9, 10, 100, 100   }
};

volatile bool use_local = false;
volatile byte in_buffer1[5];
byte i8  = LEDSIZE;

void setup(){
  pinMode(MY_POKE_PIN, INPUT);
//  Serial.begin(38400);
  Serial.begin(115200);
  Serial.println("start" );
  if(!init_i2c()){
//    show_error(5 );
  }
  pinMode(13, OUTPUT);
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
                send_pin_value( A0, diddd ? 1 : 0 );
                diddd = !diddd;
  		milis100 = mil;
                digitalWrite(13, diddd);
  	}
  /*
  int sw = analogRead( A0 );
  boolean nval = ( sw > 1000 ) ? HIGH : LOW;
  if(last_send_value != nval){
    send_pin_value( A0, 1 );
    last_send_value = nval;
  }  
  */

//send_poke();
    if( use_local&& in_buffer1[0] ){          // komendy bez odpowiedzi tutaj:
      byte command = in_buffer1[0];
      if( command == 0x11 ){                // PWM     3 bajty
           // setPWM(in_buffer1[1],in_buffer1[2]);
            leds[in_buffer1[1]].wypelnienie = in_buffer1[2];
      }else if( command == 0x10 ){          // reset
      }else if( command == 0x12 ){          // set time
      }else if( command == 0x13 ){          // fade
      }else if( command == 0x14 ){          // set dir
      }else if( command == 0x15 ){          // set output
      }else if( command == 0x16 ){          // Resetuj urządzenie obok
         Serial.println("R-" );
        pinMode(LEFT_RESET_PIN, OUTPUT); 
        digitalWrite(LEFT_RESET_PIN, LOW);       // pin w stanie niskim
        
      }else if( command == 0x17 ){         // Koniec resetu urządzenia obok, ustaw pin w stan wysokiej impedancji
        Serial.println("R0" );
        pinMode(LEFT_RESET_PIN, INPUT);          // set pin to input
        digitalWrite(LEFT_RESET_PIN, LOW);       // turn OFF pullup resistors
      }else if( command == 0x1E ){          // zmien address
      }
      use_local = false;
   }
}

void receiveEvent(int howMany){
  /*if(in_buffer1[0] || use_local ){
    Serial.print("NKomenda");
    printHex(in_buffer1[0]);
  }*/
  byte cntr = 0;
  byte aa = 0;
  while( Wire.available()){ // loop through all but the last  
    aa = Wire.read(); // receive byte as a character
    in_buffer1[cntr] = aa;
    cntr++;
  }
  byte sss = (in_buffer1[0] >> 4);
  if ( sss == 1 ){      // najstarsze 8 bitów = 1 to wykonaj w głównym wątku
      use_local = true;  
  }
}

void requestEvent(){ 
  // w in_buffer jest polecenie
  /*
    while( Wire.available()){ // loop through all but the last  
      aa = Wire.read(); // receive byte as a character
      Serial.print("IN: " );
      printHex(aa);
    }*/
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
        byte ttt[2] = {VERSION,DEVICE_TYPE};
        Wire.write(ttt,2);
    }else if( command == 0x2A ){    // return xor
        byte res = in_buffer1[1] ^ in_buffer1[2];
        Wire.write(res);
        if( res & 1 ){    // ustawiony najmlodzzy bit
          diddd = !diddd;
          digitalWrite(13, diddd);
        }
    }
}

static void send_pin_value( byte pin, byte value ){
  byte ttt[4] = {0x21,my_address,pin,value};
  send(ttt,4);
//  Serial.println("out "+ String( my_address ) +" / "+ String( pin ) +"/"+ String(value)+ "/e:" + String(error));
}
void send_poke(){
  byte ttt[2] = {0x22,my_address};
  send(ttt,2);
}
static void send_here_i_am(){
  byte ttt[2] = {0x23,my_address};
  send(ttt,2);
}
void send( byte buffer[], byte ss ){
  Wire.beginTransmission(MASTER_ADDR);  
  Wire.write(buffer,ss);
  byte error = Wire.endTransmission();
//  Serial.println("out "+ String( my_address ) +" / "+ String( pin ) +"/"+ String(value)+ "/e:" + String(error));
}

/*
void show_error( byte error_code ){    // mrygaj czerwonym tyle razy
  while(true){
    while(--error_code){
      digitalWrite(13, 1);
      delay2(100);    
      digitalWrite(13, 0);
      delay2(100);
    }
  }
}*/

/*
void serialEvent(){				       // FUNKCJA WBUDOWANA - zbieraj dane z serial0
	while (Serial.available()) {    // odczytuj gdy istnieja dane i poprzednie zostaly odczytane
		char inChar = (char)Serial.read(); 
                Serial.write(inChar);
	}
}*/

/*
void setPWM(byte pin, byte level){
    leds[pin].wypelnienie = level;
    Serial.println("setPWM:" + String(pin) + "/" + String(level) );
}
*/

