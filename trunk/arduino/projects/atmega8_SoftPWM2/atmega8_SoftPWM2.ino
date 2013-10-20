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

volatile uint16_t timertime = 100000;
volatile unsigned long _nanotime = 0;
volatile uint8_t _timediv= 0;
volatile uint8_t checktime= 0;

ISR(TIMER2_COMP_vect){
  if(pwmnow){
    pwmnow=false;
    uint8_t i;
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
      for (i = 0; i < LEDS; i++){
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
        if (newvalue > 0){          // now set the pin high (if not 0) // don't set if checkval == 0
          #if COMMON_ANODE
           *(led->outport) |= led->pinmask;            // turn on the channel (set VCC)
         #else
           *(led->outport) &= ~(led->pinmask);          // turn on the channel (set GND) 
         #endif
        }
      }
    }
    for (i = 0; i < LEDS; i++) {
      if(_pwm_channels[i].checkval == _isr_count) { // if it's a valid pin // if we have hit the width
          #if COMMON_ANODE
           *_pwm_channels[i].outport &= ~(_pwm_channels[i].pinmask);          // turn off the channel (set GND)
         #else
           *_pwm_channels[i].outport |= _pwm_channels[i].pinmask;              // turn off the channel (set VCC)
         #endif
      }
    }
  }else{
    pwmnow=true;
  }
}

void PWMSet(int8_t pin, uint8_t value){
  uint8_t i;
  for (i = 0; i < LEDS; i++){
    if ((pin < 0 && _pwm_channels[i].pin >= 0) ||  // ALL pins
       (pin >= 0 && _pwm_channels[i].pin == pin))  // individual pin
    {
      _pwm_channels[i].pwmvalue = value;        // set the pin (and exit, if individual pin)
      if (pin >= 0){ // we've set the individual pin
        return;
      }
    }
  }
}

void PWMSetFadeTime(int8_t pin, uint8_t fadeUpTime, uint8_t fadeDownTime){
  for (uint8_t i = 0; i < LEDS; i++){
    if ((pin < 0 && _pwm_channels[i].pin >= 0) ||  // ALL pins
       (pin >= 0 && _pwm_channels[i].pin == pin))  // individual pin
    {
      _pwm_channels[i].fadeuprate = fadeUpTime;
      _pwm_channels[i].fadedownrate = fadeDownTime;
      if (pin >= 0){ // we've set the individual pin
        return;
      }
    }
  }
}

void setup(){
  Serial.begin(38400);
  for (uint8_t i = 0; i < LEDS; i++){
    uint8_t pin = _pwm_channels[i].pin;
    _pwm_channels[i].outport =portOutputRegister(digitalPinToPort(pin));
    _pwm_channels[i].pinmask = digitalPinToBitMask(pin);
    pinMode(pin, OUTPUT);
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

  PWMSetFadeTime(4, 0, 5);
  PWMSetFadeTime(5, 3, 5);
  PWMSetFadeTime(6, 5, 5);
  PWMSetFadeTime(7, 6, 5);

  PWMSetFadeTime(8, 8, 5);
  PWMSetFadeTime(9, 9, 5);
  PWMSetFadeTime(16,10, 5);
  PWMSetFadeTime(17,11, 5);

  sei();    // enable interrupts
}

void loop() {
  checktime  =1;
  Serial.println("-on-");

  PWMSet(4, 255);    //zapal
  PWMSet(5, 255);
  PWMSet(6, 255);
  PWMSet(7, 255);
  PWMSet(8, 255);
  PWMSet(9, 255);
  PWMSet(16, 255);
  PWMSet(17, 255);

  Serial.println(timertime);
  
  delay(1500);

  Serial.println("soff");
  PWMSet(4, 10);
  PWMSet(5, 10);
  PWMSet(6, 10);
  PWMSet(7, 10);
  PWMSet(8, 10);
  PWMSet(9, 10);
  PWMSet(16, 10);
  PWMSet(17, 10);

  delay(1500);
}

/*
  if (hardset){
    TCNT2 = 0;
    _isr_count = 0xff;
  }


void toggle( boolean toggle, uint8_t i){
  if (toggle){
    *_pwm_channels[i].outport &= ~(_pwm_channels[i].pinmask);
  }else{
    *_pwm_channels[i].outport |= _pwm_channels[i].pinmask;
  } 
}

void initPWM(int8_t pin, int8_t firstfree){
  // we have a free pin we can use
  _pwm_channels[firstfree].pin = pin;
  _pwm_channels[firstfree].outport = portOutputRegister(digitalPinToPort(pin));
  _pwm_channels[firstfree].pinmask = digitalPinToBitMask(pin);
//    _pwm_channels[firstfree].checkval = 0;\

  // now prepare the pin for output
  // turn it off to start (no glitch)
  pinMode(pin, OUTPUT);

  #if COMMON_ANODE
   *_pwm_channels[firstfree].outport &= ~(_pwm_channels[firstfree].pinmask);          // turn off the channel (set GND)
  #else
   *_pwm_channels[firstfree].outport |= _pwm_channels[firstfree].pinmask;              // turn off the channel (set VCC)
  #endif
}

void PWMEnd(int8_t pin){
  uint8_t i;
  for (i = 0; i < PWM_MAXCHANNELS; i++)  {
    if ((pin < 0 && _pwm_channels[i].pin >= 0) ||  // ALL pins
       (pin >= 0 && _pwm_channels[i].pin == pin))  // individual pin
    {
      // now disable the pin (put it into INPUT mode)
      digitalWrite(_pwm_channels[i].pin, 1);
      pinMode(_pwm_channels[i].pin, INPUT);
      _pwm_channels[i].pin = -1;      // remove the pin
    }
  }
}

uint8_t fadeToSteps( uint16_t fadeTime ){
  if (fadeTime){    // > 0
  //  uint8_t t = 255UL * 8000UL /(8UL*60UL) / fadeTime;
    uint8_t t = 255UL * (PWM_OCR * 256UL / (F_CPU / 8000UL)) / fadeTime;
    uint8_t t = 
    Serial.println("fadeTime "+ String(t));   
    return t;
  }
  return 0;
}
*/
