#define IS_UPANEL true
#include <WSWire.h>
#include <i2c_helpers.h>
#include <barobot_common.h>

volatile bool use_local = false;
volatile byte in_buffer1[5];

void setup(){
  pinMode(8, OUTPUT);
  digitalWrite(8, 1); 
//  Serial.begin(38400);
//  Serial.begin(115200);
  if(!init_i2c()){
//    show_error(5 );
  }
  pinMode(13, OUTPUT);
//  pinMode(MY_POKE_PIN, INPUT);
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
                send_pin_value( PIN_UPANEL_POKE, diddd ? 1 : 0 );
                diddd = !diddd;
  		milis100 = mil;
                digitalWrite(8, diddd);
  	}
  /*
  int sw = analogRead( A0 );
  boolean nval = ( sw > 1000 ) ? HIGH : LOW;
  if(last_send_value != nval){
    send_pin_value( A0, 1 );
    last_send_value = nval;
  }  
  */
    if( use_local&& in_buffer1[0] ){          // komendy bez odpowiedzi tutaj:
      byte command = in_buffer1[0];
      if( command == 0x11 ){                // PWM     3 bajty
           // setPWM(in_buffer1[1],in_buffer1[2]);
 //           leds[in_buffer1[1]].wypelnienie = in_buffer1[2];
      }else if( command == 0x10 ){          // reset
      }else if( command == 0x12 ){          // set time
      }else if( command == 0x13 ){          // fade
      }else if( command == 0x14 ){          // set dir
      }else if( command == 0x15 ){          // set output
      }else if( command == 0x16 ){          // Resetuj urządzenie obok
        pinMode(PIN_UPANEL_LEFT_RESET, OUTPUT); 
        digitalWrite(PIN_UPANEL_LEFT_RESET, LOW);  // pin w stanie niskim
      }else if( command == 0x17 ){          // Koniec resetu urządzenia obok, ustaw pin w stan wysokiej impedancji
        pinMode(PIN_UPANEL_LEFT_RESET, INPUT);     // set pin to input
        digitalWrite(PIN_UPANEL_LEFT_RESET, LOW);  // turn OFF pullup resistors
      }else if( command == 0x1E ){          // zmien address
      }
      in_buffer1[0] = 0;
      use_local = false;
   }
}

void receiveEvent(int howMany){
  if(!howMany){
     return;
  }
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
        byte ttt[2] = {UPANEL_DEVICE_TYPE,UPANEL_VERSION};
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
 // Serial.println("out "+ String( my_address ) +" / "+ String( pin ) +"/"+ String(value));
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
  byte ret = 1;
  byte licznik = 250;
  while( ret && licznik++ ){    // prubuj 5 razy, zakoncz gdy error = 0;
    Wire.beginTransmission(I2C_ADR_MAINBOARD);  
    Wire.write(buffer,ss);
    ret = Wire.endTransmission();
//    Serial.print("send"+String(licznik) +": " + String( my_address ) +": ");
//    printHex(buffer[0], false ); 
//    Serial.print(", ");
//    printHex(buffer[1], false ); 
//    Serial.println(" / "+ String(ret) );  
  }
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


