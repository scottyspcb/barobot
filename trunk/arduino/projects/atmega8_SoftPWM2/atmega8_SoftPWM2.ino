#define IS_UPANEL true
#define HAS_LEDS true
#include <barobot_common.h>

volatile uint8_t _isr_count = 0xff;
volatile boolean pwmnow= false;

volatile uint16_t timertime = 100;
volatile unsigned long _nanotime = 0;
volatile uint8_t _timediv= 0;
volatile uint8_t checktime= 0;

ISR(TIMER2_COMP_vect){
  if(pwmnow){
    uint8_t i=COUNT_UPANEL_ONBOARD_LED;
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
      int16_t newvalue;
      int16_t direction;
      volatile PWMChannel *led;
      while(i--){
        led = &_pwm_channels[i];
        direction = (led->pwmup - led->current_pwm);            // we want to fade to the new value
        if (direction > 0 && led->fadeup > 0){
          newvalue = led->current_pwm + led->fadeup;
          if (newvalue > led->pwmup){
            newvalue = led->pwmup;
          }
        } else if (direction < 0 && led->fadedown > 0){
          newvalue = led->current_pwm - led->fadedown;
          if (newvalue < led->pwmup){
            newvalue = led->pwmup;
          }
        }else{
          newvalue = led->pwmup;          //  default: new value
        }
        led->current_pwm = newvalue;
        if (newvalue > 0){                  // set the pin high (if not 0) // don't set if current_pwm == 0
          #if UPANEL_COMMON_ANODE
           *(led->outport) |= led->pinmask;            // turn on the channel (set VCC)
         #else
           *(led->outport) &= ~(led->pinmask);          // turn on the channel (set GND) 
         #endif
        }
      }
    }else{
      while(i--){
       if( _pwm_channels[i].current_pwm == _isr_count) {                          // if it's a valid pin // if we have hit the width
            #if UPANEL_COMMON_ANODE
             *_pwm_channels[i].outport &= ~(_pwm_channels[i].pinmask);          // turn off the channel (set GND)
           #else
             *_pwm_channels[i].outport |= _pwm_channels[i].pinmask;              // turn off the channel (set VCC)
           #endif
        }
      }
    }
  }else{
    pwmnow=true;
  }
}

void PWM(uint8_t pin, uint8_t pwmup, uint8_t pwmdown, uint8_t timeup, uint8_t timedown, uint8_t fadeup, uint8_t fadedown){
  if(pin == 0xff ){
    uint8_t i=COUNT_UPANEL_ONBOARD_LED;
    while(i--){    
     _pwm_channels[i].fadeup =  fadeup; 
     _pwm_channels[i].fadedown =  fadedown;
     _pwm_channels[i].pwmup = pwmup;
     _pwm_channels[i].pwmdown =  pwmdown; 
     _pwm_channels[i].timedown =  timedown;
     _pwm_channels[i].timeup = timeup;   
    }
  }else{
     _pwm_channels[pin].fadeup =  fadeup; 
     _pwm_channels[pin].fadedown =  fadedown;
     _pwm_channels[pin].pwmup = pwmup;
     _pwm_channels[pin].pwmdown =  pwmdown; 
     _pwm_channels[pin].timedown =  timedown;
     _pwm_channels[pin].timeup = timeup;   
  }
}

void PWMSet(uint8_t pin, uint8_t up){
  if(pin == 0xff ){
    uint8_t i=COUNT_UPANEL_ONBOARD_LED;
    while(i--){
      _pwm_channels[i].pwmup =  up;
    }
  }else{
     _pwm_channels[pin].pwmup =  up;        // set the pin (and exit, if individual pin)
  }
}

void PWMSetFadeTime(uint8_t pin, uint8_t up, uint8_t down){
  if(pin == 0xff ){
    uint8_t i=COUNT_UPANEL_ONBOARD_LED;
    while(i--){
      _pwm_channels[i].fadeup =  up;
      _pwm_channels[i].fadedown = down;
    }
  }else{
      _pwm_channels[pin].fadeup =  up;
      _pwm_channels[pin].fadedown = down;
  }
}

void setup(){
//  Serial.begin(38400);
  // pootwieraj porty:
  DDRC |= _BV(PC2) | _BV(PC3);
  DDRB |= _BV(PB0) | _BV(PB1);
  DDRD |= _BV(PD4) | _BV(PD5) | _BV(PD6) | _BV(PD7);
/*
  uint8_t i=LEDS;
  while(i--){
    uint8_t pin = _pwm_channels[i].pin;
    _pwm_channels[i].outport = portOutputRegister(digitalPinToPort(pin));
    _pwm_channels[i].pinmask = digitalPinToBitMask(pin);
//  pinMode(pin, OUTPUT);
    #if UPANEL_COMMON_ANODE
     *_pwm_channels[i].outport &= ~(_pwm_channels[i].pinmask);          // turn off the channel (set GND)
    #else
     *_pwm_channels[i].outport |= _pwm_channels[i].pinmask;              // turn off the channel (set VCC)
    #endif
  }
     */ 

  for (uint8_t i = 0; i < COUNT_UPANEL_ONBOARD_LED; ++i){
    uint8_t pin = _pwm_channels[i].pin;
    _pwm_channels[i].outport = portOutputRegister(digitalPinToPort(pin));
    _pwm_channels[i].pinmask = digitalPinToBitMask(pin);
//  pinMode(pin, OUTPUT);
    #if UPANEL_COMMON_ANODE
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

  TCCR2 |= (1 << WGM21);    // Set to CTC Mode
  TIMSK |= (1 << OCIE2);    // Set interrupt on compare match

  PWM( 1, 0,0,0,0,0,0);
  PWM( -1, 0,0,0,0,0,0);
 
  PWMSetFadeTime(-1, 1, 1);
  
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
//  checktime  =0;

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

  PWMSet(0, 1);
  PWMSet(1, 1);
  PWMSet(2, 1);
  PWMSet(3, 1);
  PWMSet(4, 0);
  PWMSet(5, 0);
  PWMSet(6, 0);
  PWMSet(7, 0);

  delay(5200);
}


/*

void h_to_rgb(byte h, color* c){
    byte hd = h / 42;   // 42 == 252/6,  252 == H_MAX
    byte hi = hd % 6;   // gives 0-5
    byte f = h % 42; 
    byte fs = f * 6;
    switch( hi ) {
        case 0:
            c->r = 252;     c->g = fs;      c->b = 0;
           break;
        case 1:
            c->r = 252-fs;  c->g = 252;     c->b = 0;
            break;
        case 2:
            c->r = 0;       c->g = 252;     c->b = fs;
            break;
        case 3:
            c->r = 0;       c->g = 252-fs;  c->b = 252;
            break;
        case 4:
            c->r = fs;      c->g = 0;       c->b = 252;
            break;
        case 5:
            c->r = 252;     c->g = 0;       c->b = 252-fs;
            break;
    }
}


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
