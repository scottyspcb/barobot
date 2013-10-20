
#include <stdint.h>
#include <avr/io.h>
#include <avr/interrupt.h>
#include <Arduino.h>

// sterowanie plusem? false gdy sterowaniem minusem
#define COMMON_ANODE false


#define sbi(x,y) x |= _BV(y) //set bit - using bitwise OR operator 
#define cbi(x,y) x &= ~(_BV(y)) //clear bit - using bitwise AND operator
#define tbi(x,y) x ^= _BV(y) //toggle bit - using bitwise XOR operator
#define is_high(x,y) (x & _BV(y) == _BV(y)) //check if the y'th bit of register 'x' is high ... test if its AND with 1 is 1

void SoftPWMSet(int8_t pin, uint8_t value, uint8_t hardset = 0);
void SoftPWMEnd(int8_t pin);
void SoftPWMSetFadeTime(int8_t pin, uint16_t fadeUpTime, uint16_t fadeDownTime);

#define SOFTPWM_TIMER_SET(val)     (TCNT2 = (val))
#define SOFTPWM_MAXCHANNELS 8
#define ALL -1

#define SOFTPWM_FREQ 60UL
#define SOFTPWM_OCR (F_CPU/(8UL*256UL*SOFTPWM_FREQ))

typedef struct{ 
  int8_t pin;      // hardware I/O port and pin for this channel
  volatile uint8_t *outport;
  uint8_t pinmask;
  uint8_t pwmvalue;
  uint8_t checkval;
  uint8_t fadeuprate;
  uint8_t fadedownrate;
} softPWMChannel;

volatile softPWMChannel _softpwm_channels[SOFTPWM_MAXCHANNELS];
volatile uint8_t _isr_softcount = 0xff;
volatile unsigned long nanotime = 0;
volatile unsigned long timertime = 100000;
volatile boolean pwmnow= false;
volatile uint8_t checktime= false;

ISR(TIMER2_COMP_vect){
  if(pwmnow){
    pwmnow=false;
    uint8_t i;
    if(++_isr_softcount == 0){
      int16_t newvalue;
      int16_t direction;
      if(++checktime==0){
//        unsigned long time = micros();
//        timertime = time - nanotime;
//        nanotime = time;
      }
      // set all channels high - let's start again
      // and accept new checkvals
      for (i = 0; i < SOFTPWM_MAXCHANNELS; i++){
        if (_softpwm_channels[i].fadeuprate > 0 || _softpwm_channels[i].fadedownrate > 0){
          // we want to fade to the new value
          direction = _softpwm_channels[i].pwmvalue - _softpwm_channels[i].checkval;

          // we will default to jumping to the new value
          newvalue = _softpwm_channels[i].pwmvalue;

          if (direction > 0 && _softpwm_channels[i].fadeuprate > 0){
            newvalue = _softpwm_channels[i].checkval + _softpwm_channels[i].fadeuprate;
            if (newvalue > _softpwm_channels[i].pwmvalue)
              newvalue = _softpwm_channels[i].pwmvalue;
          } else if (direction < 0 && _softpwm_channels[i].fadedownrate > 0){
            newvalue = _softpwm_channels[i].checkval - _softpwm_channels[i].fadedownrate;
            if (newvalue < _softpwm_channels[i].pwmvalue)
              newvalue = _softpwm_channels[i].pwmvalue;
          }
          _softpwm_channels[i].checkval = newvalue;
        }else{  // just set the channel to the new value
          _softpwm_channels[i].checkval = _softpwm_channels[i].pwmvalue;
        }
        // now set the pin high (if not 0)
        if (_softpwm_channels[i].checkval > 0){  // don't set if checkval == 0
          #if COMMON_ANODE
           *_softpwm_channels[i].outport |= _softpwm_channels[i].pinmask;            // turn on the channel (set VCC)
         #else
           *_softpwm_channels[i].outport &= ~(_softpwm_channels[i].pinmask);          // turn on the channel (set GND) 
         #endif
        }
      }
    }
    for (i = 0; i < SOFTPWM_MAXCHANNELS; i++) {
      if (_softpwm_channels[i].pin >= 0 && (_softpwm_channels[i].checkval == _isr_softcount)) { // if it's a valid pin // if we have hit the width
          #if COMMON_ANODE
           *_softpwm_channels[i].outport &= ~(_softpwm_channels[i].pinmask);          // turn off the channel (set GND)
         #else
           *_softpwm_channels[i].outport |= _softpwm_channels[i].pinmask;              // turn off the channel (set VCC)
         #endif
      }
    }
  }else{
    pwmnow=true;
  }
}

void SoftPWMSet(int8_t pin, uint8_t value, uint8_t hardset){
  uint8_t i;
  if (hardset){
    Serial.println("SOFTPWM_TIMER_SET");
    SOFTPWM_TIMER_SET(0);
    _isr_softcount = 0xff;
  }
  for (i = 0; i < SOFTPWM_MAXCHANNELS; i++){
    if ((pin < 0 && _softpwm_channels[i].pin >= 0) ||  // ALL pins
       (pin >= 0 && _softpwm_channels[i].pin == pin))  // individual pin
    {
      // set the pin (and exit, if individual pin)
      _softpwm_channels[i].pwmvalue = value;

      if (pin >= 0) // we've set the individual pin
        return;
    }
  }
}

void initPWM(int8_t pin, int8_t firstfree){
  // we have a free pin we can use
  _softpwm_channels[firstfree].pin = pin;
  _softpwm_channels[firstfree].outport = portOutputRegister(digitalPinToPort(pin));
  _softpwm_channels[firstfree].pinmask = digitalPinToBitMask(pin);
//    _softpwm_channels[firstfree].checkval = 0;\

  // now prepare the pin for output
  // turn it off to start (no glitch)
  pinMode(pin, OUTPUT);

  #if COMMON_ANODE
   *_softpwm_channels[firstfree].outport &= ~(_softpwm_channels[firstfree].pinmask);          // turn off the channel (set GND)
  #else
   *_softpwm_channels[firstfree].outport |= _softpwm_channels[firstfree].pinmask;              // turn off the channel (set VCC)
  #endif
}


void SoftPWMEnd(int8_t pin){
  uint8_t i;
  for (i = 0; i < SOFTPWM_MAXCHANNELS; i++)  {
    if ((pin < 0 && _softpwm_channels[i].pin >= 0) ||  // ALL pins
       (pin >= 0 && _softpwm_channels[i].pin == pin))  // individual pin
    {
      // now disable the pin (put it into INPUT mode)
      digitalWrite(_softpwm_channels[i].pin, 1);
      pinMode(_softpwm_channels[i].pin, INPUT);
      _softpwm_channels[i].pin = -1;      // remove the pin
    }
  }
}


void SoftPWMSetFadeTime(int8_t pin, uint16_t fadeUpTime, uint16_t fadeDownTime)
{
  int16_t fadeAmount;
  uint8_t i;

  for (i = 0; i < SOFTPWM_MAXCHANNELS; i++){
    if ((pin < 0 && _softpwm_channels[i].pin >= 0) ||  // ALL pins
       (pin >= 0 && _softpwm_channels[i].pin == pin))  // individual pin
    {
      fadeAmount = 0;
      if (fadeUpTime > 0){
        fadeAmount = 255UL * (SOFTPWM_OCR * 256UL / (F_CPU / 8000UL)) / fadeUpTime *2U;
        Serial.println("fadeUpTime " +String(pin)+"/"+ String(fadeAmount) + "/ " + String(SOFTPWM_OCR));

      }
      _softpwm_channels[i].fadeuprate = fadeAmount;

      fadeAmount = 0;
      if (fadeDownTime > 0){
        fadeAmount = 255UL * (SOFTPWM_OCR * 256UL / (F_CPU / 8000UL)) / fadeDownTime *2U;
        Serial.println("fadeDownTime " +String(pin)+"/"+ String(fadeAmount) + "/ " + String(SOFTPWM_OCR));
      }
      _softpwm_channels[i].fadedownrate = fadeAmount;

      if (pin >= 0) { // we've set individual pin
        break;
      }
    }
  }
}

void setup(){
  Serial.begin(38400);
  delay(1000);
  Serial.println("start");
  Serial.println(F_CPU);
  Serial.println(8UL);
  Serial.println(256UL);
  Serial.println(SOFTPWM_FREQ);
  Serial.println(SOFTPWM_OCR);
/*
16000000
8
256
60
130
 */
 
  uint8_t leds[SOFTPWM_MAXCHANNELS] = {4, 5, 6, 7, 8, 9, 16, 17};
  
  for (uint8_t i = 0; i < SOFTPWM_MAXCHANNELS; i++)
  {
    _softpwm_channels[i].pin = -1;
    _softpwm_channels[i].outport = 0;
    _softpwm_channels[i].fadeuprate = 0;
    _softpwm_channels[i].fadedownrate = 0;
  }

  for (int8_t i = 0; i < SOFTPWM_MAXCHANNELS; i++)  {
    uint8_t pin = leds[i];
    // we have a free pin we can use
    _softpwm_channels[i].pin = pin;
    _softpwm_channels[i].outport = portOutputRegister(digitalPinToPort(pin));
    _softpwm_channels[i].pinmask = digitalPinToBitMask(pin);
  //    _softpwm_channels[i].checkval = 0;\
  
    // now prepare the pin for output
    // turn it off to start (no glitch)
    pinMode(pin, OUTPUT);
  
    #if COMMON_ANODE
     *_softpwm_channels[i].outport &= ~(_softpwm_channels[i].pinmask);          // turn off the channel (set GND)
    #else
     *_softpwm_channels[i].outport |= _softpwm_channels[i].pinmask;              // turn off the channel (set VCC)
    #endif
  }


  Serial.println("s2");
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

  Serial.println("s4");

  SoftPWMSet(4, 255);
  SoftPWMSet(5, 255);
  SoftPWMSet(6, 255);
  SoftPWMSet(7, 255);
  SoftPWMSet(8, 255);
  SoftPWMSet(9, 255);
  SoftPWMSet(16, 255);
  SoftPWMSet(17, 255);

  SoftPWMSetFadeTime(4,  3000, 3000);
  SoftPWMSetFadeTime(5,  3000, 3000);
  SoftPWMSetFadeTime(6,  3000, 3000);
  SoftPWMSetFadeTime(7,  3000, 3000);
  SoftPWMSetFadeTime(8,  1000, 1000);
  SoftPWMSetFadeTime(9,  1000, 1000);
  SoftPWMSetFadeTime(16, 1000, 1000);
  SoftPWMSetFadeTime(17, 1000, 1000);

  Serial.println("ready");
  sei();    // enable interrupts
}

void loop() {
  Serial.println(timertime);
  Serial.println("-on-");

  SoftPWMSet(4, 255);      // zgas
  SoftPWMSet(5, 255);
  SoftPWMSet(6, 255);
  SoftPWMSet(7, 255);
  SoftPWMSet(8, 255);
  SoftPWMSet(9, 255);
  SoftPWMSet(16, 255);
  SoftPWMSet(17, 255);

  delay(3000);

  Serial.println("soff");
  SoftPWMSet(4, 0);
  SoftPWMSet(5, 0);
  SoftPWMSet(6, 0);
  SoftPWMSet(7, 0);
  SoftPWMSet(8, 0);
  SoftPWMSet(9, 0);
  SoftPWMSet(16, 0);
  SoftPWMSet(17, 0);

  delay(3000);
}

/*
void toggle( boolean toggle, uint8_t i){
  if (toggle){
    *_softpwm_channels[i].outport &= ~(_softpwm_channels[i].pinmask);
  }else{
    *_softpwm_channels[i].outport |= _softpwm_channels[i].pinmask;
  } 
}
 */

