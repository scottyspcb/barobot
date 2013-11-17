#define IS_UPANEL true
#define HAS_LEDS true
#include <barobot_common.h>
#include <WSWire.h>
#include <i2c_helpers.h>

volatile bool use_local = false;
volatile byte in_buffer1[5];
long int milis1 = 0;
long int milis4 = 0;
boolean diddd = false;

void setup(){
  pinMode(PIN_UPANEL_SCK, INPUT );         // stan wysokiej impedancji
  pinMode(PIN_UPANEL_MISO, INPUT );        // stan wysokiej impedancji
  pinMode(PIN_UPANEL_MOSI, INPUT );        // stan wysokiej impedancji
  pinMode(PIN_UPANEL_LEFT_RESET, INPUT);   // stan wysokiej impedancji
  pinMode(PIN_UPANEL_POKE, INPUT);

  digitalWrite(PIN_UPANEL_POKE, LOW);      // enable pullup

  // pootwieraj porty:
  DDRC |= _BV(PC2) | _BV(PC3);      // wyjscie
  DDRB |= _BV(PB0) | _BV(PB1);
  DDRD |= _BV(PD4) | _BV(PD5) | _BV(PD6) | _BV(PD7);

  digitalWrite(PIN_UPANEL_LED7_NUM, LOW );      // oczekiwanie na adres

//  Serial.begin(UPANEL_SERIAL0_BOUND);
  if(!init_i2c()){
    {
      check_i2c_valid();
    } while( !init_i2c() );
  }
//    show_error(5 );
  Wire.onReceive(receiveEvent);
  Wire.onRequest(requestEvent);
  send_here_i_am();  // wyslij ze oto jestem

  digitalWrite(PIN_UPANEL_LED0_NUM, HIGH );
  digitalWrite(PIN_UPANEL_LED1_NUM, HIGH );
  digitalWrite(PIN_UPANEL_LED2_NUM, LOW );
  digitalWrite(PIN_UPANEL_LED3_NUM, LOW );
  digitalWrite(PIN_UPANEL_LED4_NUM, HIGH );
  digitalWrite(PIN_UPANEL_LED5_NUM, HIGH );
  digitalWrite(PIN_UPANEL_LED6_NUM, HIGH );
  digitalWrite(PIN_UPANEL_LED7_NUM, HIGH );
//  attachInterrupt(0, button_down, FALLING);
}


byte button_down = 0;

void loop() {
   unsigned long mil = millis();
  	if( mil > milis1 + 1000 ){    // co 4 sek
                diddd = !diddd;
  		milis1 = mil;
                digitalWrite(PIN_UPANEL_LED3_NUM, diddd);
  	}
  	if( mil > milis4 + 4000 ){    // co 4 sek
                send_pin_value( PIN_UPANEL_POKE, diddd ? 1 : 0 );
  		milis4 = mil;
  	}
    if( use_local&& in_buffer1[0] ){          // komendy bez odpowiedzi tutaj:
      byte command = in_buffer1[0];
      if( command == 0x11 ){                // PWM     3 bajty
           // setPWM(in_buffer1[1],in_buffer1[2]);
 //           leds[in_buffer1[1]].wypelnienie = in_buffer1[2];
      }else if( command == 0x10 ){         // reset
      }else if( command == 0x12 ){         // set time
      }else if( command == 0x13 ){         // fadein out
      }else if( command == 0x14 ){         // set dir
      }else if( command == 0x15 ){         // set output
      }else if( command == 0x1C ){         // prog mode on
        digitalWrite(LED_TOP_RED, LOW);
        if(in_buffer1[1] == my_address){
          prog_me = true;
        }else{
          prog_me = false;
        }
        prog_mode = true;
      }else if( command == 0x1B ){         // prog mode off
        digitalWrite(LED_TOP_RED, HIGH);
        prog_mode = false;
      }else if( command == 0x16 ){         // Resetuj urządzenie obok
        reset_next( LOW );
      }else if( command == 0x17 ){          // Koniec resetu urządzenia obok, ustaw pin w stan wysokiej impedancji
        reset_next( HIGH );
      }else if( command == 0x1E ){          // zmien address
      }
      in_buffer1[0] = 0;
      use_local = false;
   }

   if( digitalRead(PIN_UPANEL_POKE) ){
     if( button_down > 5  ){    // wciskam juz jakis czas
        // stop counting
        send_pin_value( PIN_UPANEL_POKE, 1 );
     }else{
        button_down++;
     }
   }else if( button_down > 2 ){
       if( button_down == 0 ){
         send_pin_value( PIN_UPANEL_POKE, 0 );    // puscilem
         button_down = 0;
       }else{
         button_down--;
       }
   }




   if( digitalRead(PIN_UPANEL_POKE) ){
     button_down++;

   }else if( button_down > 2 ){    // filter errors
     if( button_down > 200 ){
        // long press
        send_pin_value( PIN_UPANEL_POKE, 1 );
     }else if( button_down > 100 ){  // short press
        send_pin_value( PIN_UPANEL_POKE, 0 );
     }
     button_down = 0;
   }
}
void reset_next(boolean value){
  if( value == HIGH){    // run device
    digitalWrite(PIN_UPANEL_LEFT_RESET, HIGH);     // set pin to input
    pinMode(PIN_UPANEL_LEFT_RESET, INPUT);
    digitalWrite(PIN_UPANEL_LEFT_RESET, LOW);      // disable pullup
  }else if( value == LOW ){    // reset device
    pinMode(PIN_UPANEL_LEFT_RESET, OUTPUT); 
    digitalWrite(PIN_UPANEL_LEFT_RESET, LOW );
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
  if ( bit_is_set( in_buffer1[0], 4 ) ){      // IF like: xxx0 xxxx - run in main loop, else in requestEvent
      use_local = true;
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
  //  }else if( command == 0x28 ){  // get digital value
      /*  boolean value  = digitalRead(in_buffer1[1]);
        byte ttt[1]    = {value ? 0xff:0xff};
        Wire.write(ttt,1);*/
    }else if( command == 0x29 ){          // TEPE + VERSION       3 bajty
        byte ttt[2] = {UPANEL_DEVICE_TYPE,UPANEL_VERSION};
        Wire.write(ttt,2);
    }else if( command == 0x2A ){    // return xor
        byte res = in_buffer1[1] ^ in_buffer1[2];
        Wire.write(res);
        if( res & 0x01 ){    // ustawiony najmlodzzy bit
          diddd = !diddd;
          digitalWrite(PIN_UPANEL_LED4_NUM, diddd);
        }
    }
}

static void send_pin_value( byte pin, byte value ){
  byte ttt[4] = {0x21,my_address,pin,value};
  send(ttt,4);
 // Serial.println("out "+ String( my_address ) +" / "+ String( pin ) +"/"+ String(value));
}

static void send_here_i_am(){
  byte ttt[4] = {0x23,my_address,UPANEL_DEVICE_TYPE,UPANEL_VERSION};
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
//    Serial.print("send"+String(licznik) +": " + String( my_address ) +": ");
//    printHex(buffer[0], false ); 
//    Serial.print(", ");
//    printHex(buffer[1], false ); 
//    Serial.println(" / "+ String(ret) );  
  }
}

void check_i2c_valid(){  
  Wire.beginTransmission(I2C_ADR_RESERVED);
  byte ee = Wire.endTransmission();     // czy linia jest drozna
  if( ee == 6 ){    // niedrozna - resetuj i2c
    reset_next(LOW);
    reset_next(HIGH);
  }
}











/*
void show_error( byte error_code ){    // mrygaj czerwonym tyle razy
  while(true){
    while(--error_code){
      digitalWrite(PIN_UPANEL_LED_TEST, 1);
      delay2(100);    
      digitalWrite(PIN_UPANEL_LED_TEST, 0);
      delay2(100);
    }
  }
}*/



