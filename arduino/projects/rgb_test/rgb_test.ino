unsigned long int mic  =0;

#define LEDSIZE 8
typedef struct {
  unsigned char pin;
  unsigned char wypelnienie;	// 8 bitów		0 - 256
  uint16_t on_time;	        // 16bitów		0 - 65536 ms max
  uint16_t off_time;	        // 16bitów		0 - 65536 ms max
} LED;

LED leds[LEDSIZE] = {
  { 6, 10, 100, 100 },
  { 7, 20, 100, 100 },
  { 8, 30, 100, 100 },
  { 9, 50, 100, 100 },
  { 10, 70, 100, 100 },
  { 11, 100, 100, 100 },
  { 12, 150, 100, 100 },
  { 13, 255, 100, 100 }
};

unsigned char i8  = LEDSIZE;

void setup(){
  while(i8--){
    pinMode(leds[i8].pin, OUTPUT);  
  }
  
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
          bigcycle     = bigcycle_max;
          wypelnienie  = map( analogRead(A0), 0, 1024, 0, cycle_max );
          czas_on      = map( analogRead(A1), 0, 1024, 0, on_max );
          czas_off     = map( analogRead(A2), 0, 1024, 0, off_max );
       //   Serial.println( String(wypelnienie) );
      //    Serial.println( String(micros() - mic) + " " + String(wypelnienie) );
       //   mic = micros();
          
          for(i8=LEDSIZE;i8>0;i8--){
            leds[i8-1].wypelnienie = wypelnienie;
          }
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
  if( sig ){
    Serial.println(  "a " + String(micros() - mic)  );
    mic = micros();
    sig = false;  
  } */ 
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
*/
ISR(TIMER1_COMPA_vect){
  sig = true;
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
