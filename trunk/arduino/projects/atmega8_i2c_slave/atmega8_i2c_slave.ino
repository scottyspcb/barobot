#include <WSWire.h>
#include <arduino.h>
#include <i2c_helpers.h>
#include <avr/eeprom.h>

// to jest slave
#define VERSION 0x01
#define DEVICE_TYPE 0x10
#define MASTER_ADDR 0x01

#define LEDSIZE 2
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
};

volatile bool sig = false;
volatile bool use_local = false;
volatile byte in_buffer1[5];
byte i8  = LEDSIZE;

void setup(){
//  pinMode(A0, INPUT);
//  Serial.begin(38400,SERIAL_8N1);
  Serial.begin(38400);
  init_i2c();
  pinMode(13, OUTPUT);
  Wire.onReceive(receiveEvent);
  Wire.onRequest(requestEvent);
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
/*
  if( use_local&& in_buffer1[0] ){          // komendy bez odpowiedzi tutaj:
    byte command = in_buffer1[0];
    Serial.print("komenda loop: " );
    printHex(in_buffer1[0]);
    if( command == 0x11 ){                // PWM     3 bajty
         // setPWM(in_buffer1[1],in_buffer1[2]);
          leds[in_buffer1[1]].wypelnienie = in_buffer1[2];
    }else if( command == 0x10 ){          // reset
    }else if( command == 0x12 ){          // set time
    }else if( command == 0x13 ){          // fade
    }else if( command == 0x14 ){          // set dir
    }else if( command == 0x15 ){          // set output
    }else if( command == 0x1E ){          // zmien address
    }else if( command == 0xEE ){          // STOP       3 bajty
    }
    in_buffer1[0] = 0;
    use_local = false;
   }
   */
}

void serialEvent(){				       // FUNKCJA WBUDOWANA - zbieraj dane z serial0
	while (Serial.available()) {    // odczytuj gdy istnieja dane i poprzednie zostaly odczytane
		char inChar = (char)Serial.read(); 
                Serial.write(inChar);
	}
}
/*
void setPWM(byte pin, byte level){
    leds[pin].wypelnienie = level;
    Serial.println("setPWM:" + String(pin) + "/" + String(level) );
}
*/
void receiveEvent(int howMany){
  if(in_buffer1[0] || use_local ){
    //Serial.print("NKomenda");
    //printHex(in_buffer1[0]);
  }
  byte cntr = 0;
  byte aa = 0;
  while( Wire.available()){ // loop through all but the last  
    aa = Wire.read(); // receive byte as a character
    in_buffer1[cntr] = aa;
    cntr++;
  }
  if( in_buffer1[0] == 0x11 || in_buffer1[0] == 0x10 || in_buffer1[0] == 0x12 || 
      in_buffer1[0] == 0x13|| in_buffer1[0] == 0x14|| in_buffer1[0] == 0x15 ||
      in_buffer1[0] == 0x1E|| in_buffer1[0] == 0xEE ){
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
    if( command == 0x16 ){  // analog value
        byte ttt[2] = {0x44,0x55};
      //  Serial.print("Wysylam  analog");
        Wire.write(ttt,2);
        in_buffer1[0] =0;
    }else if( command == 0x18 ){  // digital value
        byte ttt[1] = {0xff};
   //     Serial.print("Wysylam  digital");
        Wire.write(ttt,1);
        in_buffer1[0] =0;
    }else if( command == 0x19 ){          // TEPE + VERSION       3 bajty
        byte ttt[2] = {VERSION,DEVICE_TYPE};
        Wire.write(ttt,2);
        in_buffer1[0] =0;
    }else if( command == 0x1A ){    // return xor
        byte res = in_buffer1[1] ^ in_buffer1[2];
        Wire.write(res);
        in_buffer1[0] =0;
        if( res & 1 ){    // ustawiony najmlodzzy bit
          diddd = !diddd;
          digitalWrite(13, diddd);
        }
    }
}

static void send_pin_value( byte pin, byte value ){
  Serial.println("out p "+ String( my_address ) +" / "+ String( pin ) +"/"+ String(value));
  Wire.beginTransmission(MASTER_ADDR);  
  byte ttt[4] = {0x21,my_address,pin,value};
  Wire.write(ttt,4);
  Wire.endTransmission();
 // Serial.println("outpin");
}

