#define IS_CARRET true
#define HAS_LEDS true
#include <barobot_common.h>
#include <avr/io.h>
//#include <stdint.h>       // needed for uint8_t

#define ANALOGS  9
#define ANALOG_TRIES  2

volatile uint16_t checks = 0;
volatile int8_t ADCport[ANALOGS] = {2,3,4,5,6,7,8,0,1};
volatile int16_t ADCvalue[ANALOG_TRIES][ANALOGS] = {{0,0,0,0,0,0},{0,0,0,0,0,0}};
volatile uint8_t channel = 0;
volatile uint8_t row = 0;

void setup(){
  pinMode(A0, INPUT);
  pinMode(A1, INPUT);
  pinMode(A2, INPUT);
  pinMode(A3, INPUT);
  pinMode(A4, INPUT);
  pinMode(A5, INPUT);
  pinMode(A6, INPUT);
  pinMode(A7, INPUT);
  DEBUGINIT();
  DEBUGLN("-start"); 
  init_analogs();
}

ISR(ADC_vect){
  uint8_t tmp  = ADMUX;            // read the value of ADMUX register
  tmp          &= 0xF0;            // starsze bity
  channel      = (channel + 1)%ANALOGS;
  ADMUX        = (tmp | ADCport[channel]);
  ADCvalue[ row ][ channel ] = ADCL | (ADCH << 8);  //  read low first
  if( channel == 0 ){
    //row          = ((row+1) % ANALOG_TRIES);
  }
  checks++;
}

unsigned long milisAnalog = 0;
unsigned long int mil = 0;
long int milis1000 = 0;
long int milis2000 = 0;
byte iii = 0;
void loop() {
  mil = millis();
  	if( mil > milis1000 ){    // debug, mrygaj co 1 sek
       //   DEBUG( "-analog " );
          DEBUG( checks );
          DEBUG(":\t\t");
          DEBUG( ADCvalue[0][0] );          DEBUG(" ");
          DEBUG( ADCvalue[0][1] );          DEBUG(" ");
          DEBUG( ADCvalue[0][2] );          DEBUG(" ");
          DEBUG( ADCvalue[0][3] );          DEBUG(" ");
          DEBUG( ADCvalue[0][4] );          DEBUG(" ");
          DEBUG( ADCvalue[0][5] );          DEBUG(" ");
          DEBUG( ADCvalue[0][6] );          DEBUG(" ");
          DEBUG( ADCvalue[0][7] );          DEBUG(" ");
          DEBUG( ADCvalue[0][8] );          DEBUG(" ");
          DEBUGLN( );
          milis1000 = mil + 100;
          checks = 0;
         // Serial.println(GetTemp());
      }
}

void init_analogs(){
    ADMUX = 0;                // use ADC0
    ADMUX |= _BV(REFS1);      
    ADMUX |= (1 << REFS0);    // REFS1 + REFS0 = Internal 1.1V (ATmega168/328) or  2.56V on (ATmega8)
    ADCSRA |= (1 << ADPS0);  // 128 prescale  
    ADCSRA |= (1 << ADPS1);
    ADCSRA |= (1 << ADPS2);
    ADCSRA |= (1 << ADATE);   // Set ADC Auto Trigger Enable
    ADCSRB = 0;               // 0 for free running mode
    ADCSRA |= (1 << ADEN);    // Enable the ADC
    ADCSRA |= (1 << ADIE);    // Enable Interrupts 
    ADCSRA |= (1 << ADSC);    // Start the ADC conversion
    sei();
}
