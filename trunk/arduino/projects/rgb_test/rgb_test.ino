#include <Wire.h>
int x = 1;

// to jest slave
#define MY_ADDR 0x04
#define SLAVE_ADDR 0x04
#define MASTER_ADDR 0x02

#define VERSION 0x01
#define DEVICE_TYPE 0x41

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
  { 10, 70, 100, 100   },
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
  Wire.onReceive(receiveEvent); // register event 
  Wire.onRequest(requestEvent); // register event   
  
  Serial.println("START SLAVE"); 

  Serial.begin(115200,SERIAL_8N1);
  /*
    Serial.println( "--------" );
   Serial.println( "--------" );
   Serial.println( "--1-----" );
   i8= LEDSIZE;
   while(i8--){
   Serial.println( String(i8) );
   }
   
   Serial.println( "----2----" );
   for(i8=LEDSIZE-1;i8>0;i8--){
   Serial.println( String(i8) );
   }
   Serial.println( "----3----" );
   for(i8  = 0;i8<LEDSIZE;i8++){
   Serial.println( String(i8) );
   }*/

  pinMode(A0, INPUT);
  pinMode(A1, INPUT);
  pinMode(A2, INPUT);

  //  analogWrite(leds[4].pin, 255);
  //  digitalWrite(leds[5].pin, 255);
  // mic = micros();
/*
  // initialize timer1 
  noInterrupts();           // disable all interrupts
  TCCR1A = 0;
  TCCR1B = 0;
  TCNT1  = 0;

  OCR1A = 8192;            // compare match register 16MHz/256/2Hz
  TCCR1B |= (1 << WGM12);   // CTC mode
  TCCR1B |= (1 << CS12);    // 256 prescaler 
  TIMSK1 |= (1 << OCIE1A);  // enable timer compare interrupt
  interrupts();             // enable all interrupts
  */
}

unsigned int wypelnienie  = 0;
unsigned int czas_on      = 0;
unsigned int czas_off     = 0;
//unsigned int wypelnij   = 10;
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
volatile bool sig2 = false;

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
        sig = true;
      }
      bigcycle--;
    }
    mediumcycle--;
  }
  //else{    // nie trzeba dla 0 bo juz sprawdzam ten warunek
  for(i8=LEDSIZE;i8>0;i8--){
    if(leds[i8-1].wypelnienie == cycle ){
      digitalWrite(leds[i8-1].pin, LOW  );
    }
  }
  //  }
  cycle++;
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

   if( sig ){
        bigcycle     = bigcycle_max;
        wypelnienie  = map( analogRead(A0), 0, 1024, 0, cycle_max );
        czas_on      = map( analogRead(A1), 0, 1024, 0, on_max );
        czas_off     = map( analogRead(A2), 0, 1024, 0, off_max );
        //   Serial.println( String(wypelnienie) );
     //   Serial.println( String(micros() - mic) + " " + String(wypelnienie) );
     //   mic = micros();
        for(i8=LEDSIZE;i8>0;i8--){
           leds[i8-1].wypelnienie = wypelnienie;
        }
        sig= false;
   }
}

//volatile byte buffer[10];
byte in_buffer[10];
byte super1 = 1;
byte super2 = 2;

void receiveEvent(int howMany){
  byte cc = 0;
  byte aa = 0;
  while( Wire.available()){ // loop through all but the last  
    aa = Wire.read(); // receive byte as a character
    Serial.print("Odbieram" + String(cc) + ":");
    printHex(aa);
    in_buffer[cc] = aa;
    cc++;
  }
  // komendy bez odpowiedzi tutaj:
   byte command = in_buffer[0];
   if( command == 0x11 ){                // PWM     3 bajty
        super1  = in_buffer[1];
        super2  = in_buffer[2];
  
      //  Serial.print("Odebralem X: ");
      //  printHex(super1);
     //   Serial.print("Odebralem Y: ");
     //   printHex(super2);
   }
}

void requestEvent(){ 
  
    Serial.println("-------");

//  byte cc = 0;
//  byte aa = 0;
  /*
  
    while( Wire.available()){ // loop through all but the last  
      aa = Wire.read(); // receive byte as a character
      Serial.print("IN: " );
      printHex(aa);
    }*/

    byte command = in_buffer[0];
    Serial.print("komenda: " );
    printHex(in_buffer[0]);

    if( command == 0x11 ){                // PWM     3 bajty

    }else if( command == 0x22 ){          // BLINK   4 bajty
    }else if( command == 0x44 ){          // FADE    3 bajty
    }else if( command == 0x55 ){          // SYNCHRO 1 bajt
    }else if( command == 0x66 ){          // GET VALUE  2 bajty
       byte ttt[1] = {0x39};
       Wire.write(ttt,1);
        
       Serial.print("Wysylam: ");
       printHex(ttt[0]);
  
    }else if( command == 0x77 ){          // SET VALUE  3 bajty
    }else if( command == 0x88 ){          // DIR        3 bajty
      byte ttt[5] = {0x5A,super1,super2,super1,super2};
      Wire.write(ttt,5);


      Serial.print("Wysylam X: ");
      printHex(super1);
      Serial.print("Wysylam Y: ");      
      printHex(super2);

    }else if( command == 0x99 ){          // TEPE + VERSION       3 bajty
        byte ttt = 0x87;
        Wire.write(ttt); 

        ttt = VERSION;
        Wire.write(ttt);  
      
        Serial.print("Wysylam: ");
        printHex(ttt);
        
        ttt = DEVICE_TYPE;
        Wire.write(ttt);

        Serial.print("Wysylam: ");
        printHex(ttt); 
        Serial.print("Wysylam: ");
        printHex(VERSION);
        Serial.print("Wysylam: ");
        printHex(DEVICE_TYPE);
        

    }else if( command == 0xEE ){          // STOP       3 bajty
        Wire.write(0x32);
    }
}

void printHex(byte val){
  int temp =  val;
  Serial.println(temp,HEX);
}

volatile uint8_t ISRcounter = 0; /* Count the number of times the ISR has run */
/*
ISR( SIG_OVERFLOW1 ){
 sig = true;
 } 
 ISR( SIG_OVERFLOW0 ){
 sig = true;
 } 
 ISR( SIG_OVERFLOW2 ){
 sig = true;
 } 
 ISR(TIM0_OVF_vect){
 sig = true;
 } 
 ISR(TIM333333_OVF_veffct){
 sig = true;
 } 


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
ISR(TIMER1_COMPA_vect){
//  sig = true;
}

ISR(TIMER1_OVF_vect)        // interrupt service routine that wraps a user defined function supplied by attachInterrupt
{
  sig2 = true;
  TCNT1 = 34286;            // preload timer
}

/*
    //    for(i8  = 0;i8<LEDSIZE;i8++){
 void reload(){
 analogWrite(leds[2].pin, wypelnienie);
 
 }*/

