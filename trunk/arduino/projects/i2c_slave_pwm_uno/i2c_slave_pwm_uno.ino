#include <Wire.h>
int x = 1;

// to jest slave
#define MY_ADDR 0x04
#define SLAVE_ADDR 0x04
#define MASTER_ADDR 0x02

#define VERSION 0x01
#define DEVICE_TYPE 0x10

unsigned long int mic  =0;

#define LEDSIZE 8
typedef struct {
  byte pin;
  byte wypelnienie;	// 8 bitów		0 - 256
  uint16_t on_time;	        // 16bitów		0 - 65536 ms max
  uint16_t off_time;	        // 16bitów		0 - 65536 ms max
} 
LED;

LED leds[LEDSIZE] = {
  {6, 10, 100, 100   },
  {7, 20, 100, 100   },
  {8, 30, 100, 100   },
  {9, 50, 100, 100   } ,
  {10, 70, 100, 100   },
  {11, 100, 100, 100   },
  {12, 150, 100, 100   },
  {13, 255, 100, 100   }
};

byte i8  = LEDSIZE;

void setup(){
  while(i8--){
    pinMode(leds[i8].pin, OUTPUT);  
  }
  Wire.begin(MY_ADDR);
  Wire.onReceive(receiveEvent);
  Wire.onRequest(requestEvent);
  Serial.println("START SLAVE"); 
  Serial.begin(115200,SERIAL_8N1);
  pinMode(A0, INPUT);
  pinMode(A1, INPUT);
  pinMode(A2, INPUT);
}

//unsigned int cycle   = 0;         // 2^16 = 65535
uint8_t cycle          = 0;         // 2^8  = 256
uint8_t mediumcycle    = 0;         // 2^8  = 256
uint8_t bigcycle       = 0;         // 2^8  = 256

#define cycle_max 126
#define on_max 256
#define off_max 256
#define mediumcycle_max 16    // ok 80 ms
#define bigcycle_max 20       // ok 80 ms

bool was_change = false;
volatile bool sig = false;
volatile bool use_local = false;
unsigned int wypelnienie= 50;
byte in_buffer1[5];
byte in_buffer2[5];
byte super1 = 1;
byte super2 = 2;

void loop(){  
  if( cycle == 0 ){
    for(i8=LEDSIZE;i8>0;i8--){
      if(leds[i8-1].wypelnienie >0 ){
        digitalWrite(leds[i8-1].pin, HIGH);
      }
    }
    if( mediumcycle == 0 ){    // przekręcił się duży licznik
      mediumcycle     = mediumcycle_max;
      if( bigcycle == 0 ){    // przekręcił się duży licznik
   //     sig = true;
          
      }
      bigcycle--;    // gdy = 0 to rownowazne: bigcycle     = bigcycle_max;
    }
    mediumcycle--;
  }else{    // nie trzeba dla 0 bo juz sprawdzam ten warunek
    for(i8=LEDSIZE;i8>0;i8--){
      if(leds[i8-1].wypelnienie == cycle ){
        digitalWrite(leds[i8-1].pin, LOW  );
      }
    }
  }
  //  }
  cycle++;

  if( use_local&& in_buffer1[0] ){          // komendy bez odpowiedzi tutaj:
    byte command = in_buffer1[0];
//    Serial.print("komenda loop: " );
 //   printHex(in_buffer1[0]);
    if( command == 0x11 ){                // PWM     3 bajty
          Serial.print("Odebralem pin: ");
          printHex(in_buffer1[1]);
          Serial.print("Odebralem level: ");
          printHex(in_buffer1[2]);
          setPWM(in_buffer1[1],in_buffer1[2]);
          in_buffer1[0] = 0;

    }else if( command == 0x10 ){          // reset
          in_buffer1[0] = 0;
    }else if( command == 0x12 ){          // set time
          in_buffer1[0] = 0;
    }else if( command == 0x13 ){          // fade
          in_buffer1[0] = 0;
    }else if( command == 0x14 ){          // set dir
          in_buffer1[0] = 0;
    }else if( command == 0x15 ){          // set output
          in_buffer1[0] = 0; 
    }else if( command == 0x1E ){          // zmien address
          in_buffer1[0] = 0;  
    }else if( command == 0xEE ){          // STOP       3 bajty
          in_buffer1[0] = 0;    
    }
    use_local = false;
   }

}

void setPWM(byte pin, byte level){
  leds[pin].wypelnienie = level;
}

void receiveEvent(int howMany){
  byte cntr = 0;
  byte aa = 0;

  if(in_buffer1[0] || use_local ){
    Serial.print("Niedokonczona komenda ");
    printHex(in_buffer1[0]);
  }

  while( Wire.available()){ // loop through all but the last  
    aa = Wire.read(); // receive byte as a character
  //  Serial.print("Odbieram" + String(cntr) + ":");
  //  printHex(aa);
    in_buffer1[cntr] = aa;
    cntr++;
  }
  if(in_buffer1[0] == 0x11){
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
 //   Serial.print("komenda: " );
 //   printHex(in_buffer1[0]);

    if( command == 0x16 ){  // analog value
        byte ttt[2] = {0x44,0x55};
        Serial.print("Wysylam  analog");
        Wire.write(ttt,2);
        in_buffer1[0] =0;
    }else if( command == 0x18 ){  // digital value
        byte ttt[1] = {0xff};
        Serial.print("Wysylam  digital");
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
    }
}

void printHex(byte val){
  int temp =  val;
  Serial.println(temp,HEX);
}


/*
  for(i8=LEDSIZE;i8>0;i8--){
     leds[i8-1].wypelnienie = level;
  }
*/

//    Serial.println("++++++++++");
  //  Serial.println("<==bylo:" + String(cc) + " " + String(howMany));  
  //  while(howMany--){
  //    Wire.write(0x33);
  //  }
  /*
  //  Wire.beginTransmission(MASTER_ADDR);
    String a = "slave x is " + String(x);
    const char* aa = (const char *) a.c_str();

  //  Wire.endTransmission();
    x++;

 */

/*
    //    for(i8  = 0;i8<LEDSIZE;i8++){
 void reload(){
 analogWrite(leds[2].pin, wypelnienie);
 
 }*/
  /*
  if( sig2 ){
   Serial.println( "b " + String(micros() - mic)  );
   mic = micros();  
   sig2 = false; 
   }  
   Serial.println(  "a " + String(micros() - mic)  );
   mic = micros();
   sig = false; 
   }*/
/*
   if( sig ){
        wypelnienie  = map( analogRead(A0), 0, 1024, 0, cycle_max );
        unsigned int czas_on      = map( analogRead(A1), 0, 1024, 0, on_max );
        unsigned int czas_off     = map( analogRead(A2), 0, 1024, 0, off_max );
        Serial.println( "-------------------"+String(wypelnienie) );
        setPWM(3,wypelnienie);
        sig= false;
   }*/
