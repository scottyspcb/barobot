// sterowanie plusem? false gdy sterowaniem minusem
#define COMMON_ANODE true

void PWMSet(int8_t pin, uint8_t value);
void PWMEnd(int8_t pin);

typedef struct{ 
  int8_t pin;      // hardware I/O port and pin for this channel
  volatile uint8_t *outport;
  uint8_t pinmask;
  uint8_t pwmvalue;
  uint8_t fadeuprate;
  uint8_t fadedownrate;
  uint8_t checkval;
} PWMChannel;

#define LEDS 8
volatile PWMChannel _pwm_channels[LEDS]= {{4,0,0,0,0,0},{5,0,0,0,0,0},{6,0,0,0,0,0},{7,0,0,0,0,0},{8,0,0,0,0,0},{9,0,0,0,0,0},{16,0,0,0,0,0},{17,0,0,0,0,0}};
volatile uint8_t _isr_count = 0xff;
volatile boolean pwmnow= false;

volatile uint16_t timertime = 1000;
volatile unsigned long _nanotime = 0;
volatile uint8_t _timediv= 0;
volatile uint8_t checktime= 0;

ISR(TIMER2_COMP_vect){
  uint8_t i=LEDS;
  if(pwmnow){
    pwmnow=false;
    if(++_isr_count == 0){
      if( checktime /* && ++_timediv==0*/ ){
        if(checktime == 1){    // zmierzylem raz
          _nanotime =  micros();
          checktime = 2;
        }else if(checktime == 2){    // zmierzylem 2 raz, to starczy
          timertime  = micros() - _nanotime;
          checktime = 0;
        }
      }
      // set all channels high - let's start again
      // and accept new checkvals
      int16_t newvalue;
      int16_t direction;
      volatile PWMChannel *led;
      while(i--){
        led = &_pwm_channels[i];
        direction = (led->pwmvalue - led->checkval);            // we want to fade to the new value
        if (direction > 0 && led->fadeuprate > 0){
          newvalue = led->checkval + led->fadeuprate;
          if (newvalue > led->pwmvalue){
            newvalue = led->pwmvalue;
          }
        } else if (direction < 0 && led->fadedownrate > 0){
          newvalue = led->checkval - led->fadedownrate;
          if (newvalue < led->pwmvalue){
            newvalue = led->pwmvalue;
          }
        }else{
          newvalue = led->pwmvalue;          // we will default to jumping to the new value
        }
        led->checkval = newvalue;
        if (newvalue > 0){                  // now set the pin high (if not 0) // don't set if checkval == 0
          #if COMMON_ANODE
           *(led->outport) |= led->pinmask;            // turn on the channel (set VCC)
         #else
           *(led->outport) &= ~(led->pinmask);          // turn on the channel (set GND) 
         #endif
        }
      }
    }
  }else{
    pwmnow=true;
    while(i--){
     if( _pwm_channels[i].checkval == _isr_count) {                          // if it's a valid pin // if we have hit the width
          #if COMMON_ANODE
           *_pwm_channels[i].outport &= ~(_pwm_channels[i].pinmask);          // turn off the channel (set GND)
         #else
           *_pwm_channels[i].outport |= _pwm_channels[i].pinmask;              // turn off the channel (set VCC)
         #endif
      }
    }
  }
}

void PWMSet(int8_t pin, uint8_t value){
  if(pin == -1 ){
    uint8_t i=LEDS;
    while(i--){
      _pwm_channels[i].pwmvalue = value;
    }
  }else{
     _pwm_channels[pin].pwmvalue = value;        // set the pin (and exit, if individual pin)
  }
}


void PWMSetFadeTime(int8_t pin, uint8_t fadeUpTime, uint8_t fadeDownTime){
  if(pin == -1 ){
    uint8_t i=LEDS;
    while(i--){
      _pwm_channels[i].fadeuprate = fadeUpTime;
      _pwm_channels[i].fadedownrate = fadeDownTime;
    }
  }else{
      _pwm_channels[pin].fadeuprate = fadeUpTime;
      _pwm_channels[pin].fadedownrate = fadeDownTime;
  }
}


void setup(){
  Serial.begin(38400);
  
  // pootwieraj porty:
  DDRC |= _BV(PC2) | _BV(PC3);
  DDRB |= _BV(PB0) | _BV(PB1);
  DDRD |= _BV(PD4) | _BV(PD5) | _BV(PD6) | _BV(PD7);

  for (uint8_t i = 0; i < LEDS; i++){
    uint8_t pin = _pwm_channels[i].pin;
    _pwm_channels[i].outport = portOutputRegister(digitalPinToPort(pin));
    _pwm_channels[i].pinmask = digitalPinToBitMask(pin);
//  pinMode(pin, OUTPUT);
    #if COMMON_ANODE
     *_pwm_channels[i].outport &= ~(_pwm_channels[i].pinmask);          // turn off the channel (set GND)
    #else
     *_pwm_channels[i].outport |= _pwm_channels[i].pinmask;              // turn off the channel (set VCC)
    #endif
  }

    OCR2 = 100;
    TCCR2 &= ~(1 << CS20);
    TCCR2 &= ~(1 << CS21);
    TCCR2 &= ~(1 << CS22); 
//    TCCR2 |= (1 << CS21);
    TCCR2 |= (1 << CS20);
//    TCCR2 |= (1 << CS22);  
//    20        = 8640   us
//    21        = 65792  us
//    21,20     = 262144  us
//    22        = 524288  us
//    20,22     = 1048576  us
//    21,22     = 2097152  us
//    22,21,20  = 8388608  us

  Serial.println("s3");

  TCCR2 |= (1 << WGM21);    // Set to CTC Mode
  TIMSK |= (1 << OCIE2);    // Set interrupt on compare match

  PWMSetFadeTime(0, 1, 1);
  PWMSetFadeTime(1, 2, 2);
  PWMSetFadeTime(2, 3, 3);
  PWMSetFadeTime(3, 4, 4);

  PWMSetFadeTime(4, 5, 5);
  PWMSetFadeTime(5, 6, 6);
  PWMSetFadeTime(6, 7, 7);
  PWMSetFadeTime(7, 0, 0);

  sei();    // enable interrupts
}

void loop() {
  checktime  =0;
  Serial.println("-on-");

  PWMSet(0, 255);    //zapal
  PWMSet(1, 255);
  PWMSet(2, 255);
  PWMSet(3, 255);
  PWMSet(4, 255);
  PWMSet(5, 255);
  PWMSet(6, 255);
  PWMSet(7, 255);

 // Serial.println(timertime);
  
  delay(5200);

  Serial.println("soff");
  PWMSet(0, 2);
  PWMSet(1, 2);
  PWMSet(2, 2);
  PWMSet(3, 2);
  PWMSet(4, 2);
  PWMSet(5, 2);
  PWMSet(6, 2);
  PWMSet(7, 2);

  delay(5200);
}

/*

TCNT2 = 0;
 _isr_count = 0xff;

void PWMEnd(int8_t pin){
  if(pin == -1 ){
    uint8_t i=LEDS;
    while(i--){
      pinMode(_pwm_channels[i].pin, INPUT);
    }
  }else{
     pinMode(_pwm_channels[pin].pin, INPUT);
  }
}

uint8_t fadeToSteps( uint16_t fadeTime ){
  if (fadeTime){    // > 0
    uint8_t t = 255UL * 8000UL /(8UL*60UL) / fadeTime;
    Serial.println("fadeTime "+ String(t));   
    return t;
  }
  return 0;
}
*/
