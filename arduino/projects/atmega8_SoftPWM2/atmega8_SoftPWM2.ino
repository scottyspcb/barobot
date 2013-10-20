
#include <stdint.h>
#include <avr/io.h>
#include <avr/interrupt.h>
#include <Arduino.h>

#define SOFTPWM_MAXCHANNELS 8
#define SOFTPWM_PWMDEFAULT 0x00

#define SOFTPWM_NORMAL 0
#define SOFTPWM_INVERTED 1

#define ALL -1


#ifndef cbi
#define cbi(sfr, bit) (_SFR_BYTE(sfr) &= ~_BV(bit))
#endif
#ifndef sbi
#define sbi(sfr, bit) (_SFR_BYTE(sfr) |= _BV(bit))
#endif 

void SoftPWMBegin();
void SoftPWMSet(int8_t pin, uint8_t value, uint8_t hardset = 0);
void SoftPWMSetPercent(int8_t pin, uint8_t percent, uint8_t hardset = 0);
void SoftPWMEnd(int8_t pin);
void SoftPWMSetFadeTime(int8_t pin, uint16_t fadeUpTime, uint16_t fadeDownTime);
void SoftPWMSetPolarity(int8_t pin, uint8_t polarity);


#define SOFTPWM_NORMAL 0
#define SOFTPWM_INVERTED 1

#define SOFTPWM_FREQ 60UL
#define SOFTPWM_OCR (F_CPU/(8UL*256UL*SOFTPWM_FREQ))

volatile uint8_t _isr_softcount = 0xff;
volatile unsigned long nanotime = 0;;

uint8_t _softpwm_defaultPolarity = SOFTPWM_NORMAL;

typedef struct{
  // hardware I/O port and pin for this channel
  int8_t pin;
  uint8_t polarity;
  volatile uint8_t *outport;
  uint8_t pinmask;
  uint8_t pwmvalue;
  uint8_t checkval;
  uint8_t fadeuprate;
  uint8_t fadedownrate;
} softPWMChannel;

volatile softPWMChannel _softpwm_channels[SOFTPWM_MAXCHANNELS];


ISR(TIMER2_COMP_vect){
  uint8_t i;
  int16_t newvalue;
  int16_t direction;

  if(++_isr_softcount == 0){
    // 8672
        unsigned long time = micros();
	Serial.println(time - nanotime);
	nanotime = time;

    // set all channels high - let's start again
    // and accept new checkvals
    for (i = 0; i < SOFTPWM_MAXCHANNELS; i++){
      if (_softpwm_channels[i].fadeuprate > 0 || _softpwm_channels[i].fadedownrate > 0)
      {
        // we want to fade to the new value
        direction = _softpwm_channels[i].pwmvalue - _softpwm_channels[i].checkval;

        // we will default to jumping to the new value
        newvalue = _softpwm_channels[i].pwmvalue;

        if (direction > 0 && _softpwm_channels[i].fadeuprate > 0)
        {
          newvalue = _softpwm_channels[i].checkval + _softpwm_channels[i].fadeuprate;
          if (newvalue > _softpwm_channels[i].pwmvalue)
            newvalue = _softpwm_channels[i].pwmvalue;
        }
        else if (direction < 0 && _softpwm_channels[i].fadedownrate > 0)
        {
          newvalue = _softpwm_channels[i].checkval - _softpwm_channels[i].fadedownrate;
          if (newvalue < _softpwm_channels[i].pwmvalue)
            newvalue = _softpwm_channels[i].pwmvalue;
        }

        _softpwm_channels[i].checkval = newvalue;
      }
      else  // just set the channel to the new value
        _softpwm_channels[i].checkval = _softpwm_channels[i].pwmvalue;

      // now set the pin high (if not 0)
      if (_softpwm_channels[i].checkval > 0)  // don't set if checkval == 0
      {
        if (_softpwm_channels[i].polarity == SOFTPWM_NORMAL)
          *_softpwm_channels[i].outport |= _softpwm_channels[i].pinmask;
        else
          *_softpwm_channels[i].outport &= ~(_softpwm_channels[i].pinmask);
      }
    }
  }

  for (i = 0; i < SOFTPWM_MAXCHANNELS; i++) {
    if (_softpwm_channels[i].pin >= 0)  // if it's a valid pin
    {
      if (_softpwm_channels[i].checkval == _isr_softcount)  // if we have hit the width
      {
        // turn off the channel
        if (_softpwm_channels[i].polarity == SOFTPWM_NORMAL)
          *_softpwm_channels[i].outport &= ~(_softpwm_channels[i].pinmask);
        else
          *_softpwm_channels[i].outport |= _softpwm_channels[i].pinmask;
      }
    }
  }
}




void SoftPWMSetPolarity(int8_t pin, uint8_t polarity){
  uint8_t i;

  if (polarity != SOFTPWM_NORMAL)
    polarity = SOFTPWM_INVERTED;

  for (i = 0; i < SOFTPWM_MAXCHANNELS; i++)
  {
    if ((pin < 0 && _softpwm_channels[i].pin >= 0) ||  // ALL pins
       (pin >= 0 && _softpwm_channels[i].pin == pin))  // individual pin
    {
      _softpwm_channels[i].polarity = polarity;
    }
  }
}


void SoftPWMSetPercent(int8_t pin, uint8_t percent, uint8_t hardset){
  SoftPWMSet(pin, ((uint16_t)percent * 255) / 100, hardset);
}

#define SOFTPWM_TIMER_SET(val)     (TCNT2 = (val))
void SoftPWMSet(int8_t pin, uint8_t value, uint8_t hardset){
  int8_t firstfree = -1;  // first free index
  uint8_t i;

  if (hardset){
    Serial.println("SOFTPWM_TIMER_SET");
    SOFTPWM_TIMER_SET(0);
    _isr_softcount = 0xff;
  }

  // If the pin isn't already set, add it
  for (i = 0; i < SOFTPWM_MAXCHANNELS; i++)
  {
    if ((pin < 0 && _softpwm_channels[i].pin >= 0) ||  // ALL pins
       (pin >= 0 && _softpwm_channels[i].pin == pin))  // individual pin
    {
      // set the pin (and exit, if individual pin)
      _softpwm_channels[i].pwmvalue = value;

      if (pin >= 0) // we've set the individual pin
        return;
    }
    // get the first free pin if available
    if (firstfree < 0 && _softpwm_channels[i].pin < 0)
      firstfree = i;
  }

  if (pin >= 0 && firstfree >= 0)
  {
    // we have a free pin we can use
    _softpwm_channels[firstfree].pin = pin;
    _softpwm_channels[firstfree].polarity = _softpwm_defaultPolarity;
    _softpwm_channels[firstfree].outport = portOutputRegister(digitalPinToPort(pin));
    _softpwm_channels[firstfree].pinmask = digitalPinToBitMask(pin);
    _softpwm_channels[firstfree].pwmvalue = value;
//    _softpwm_channels[firstfree].checkval = 0;
    
    // now prepare the pin for output
    // turn it off to start (no glitch)
    if (_softpwm_defaultPolarity == SOFTPWM_NORMAL)
      digitalWrite(pin, LOW);
    else
      digitalWrite(pin, HIGH);
    pinMode(pin, OUTPUT);
  }
}

void SoftPWMEnd(int8_t pin)
{
  uint8_t i;

  for (i = 0; i < SOFTPWM_MAXCHANNELS; i++)
  {
    if ((pin < 0 && _softpwm_channels[i].pin >= 0) ||  // ALL pins
       (pin >= 0 && _softpwm_channels[i].pin == pin))  // individual pin
    {
      // now disable the pin (put it into INPUT mode)
      digitalWrite(_softpwm_channels[i].pin, 1);
      pinMode(_softpwm_channels[i].pin, INPUT);

      // remove the pin
      _softpwm_channels[i].pin = -1;
    }
  }
}


void SoftPWMSetFadeTime(int8_t pin, uint16_t fadeUpTime, uint16_t fadeDownTime)
{
  int16_t fadeAmount;
  uint8_t i;

  for (i = 0; i < SOFTPWM_MAXCHANNELS; i++)
  {
    if ((pin < 0 && _softpwm_channels[i].pin >= 0) ||  // ALL pins
       (pin >= 0 && _softpwm_channels[i].pin == pin))  // individual pin
    {

      fadeAmount = 0;
      if (fadeUpTime > 0)
        fadeAmount = 255UL * (SOFTPWM_OCR * 256UL / (F_CPU / 8000UL)) / fadeUpTime;

      _softpwm_channels[i].fadeuprate = fadeAmount;

      fadeAmount = 0;
      if (fadeDownTime > 0)
        fadeAmount = 255UL * (SOFTPWM_OCR * 256UL / (F_CPU / 8000UL)) / fadeDownTime;

      _softpwm_channels[i].fadedownrate = fadeAmount;

      if (pin >= 0)  // we've set individual pin
        break;
    }
  }
}



void setup(){
  Serial.begin(38400);
  delay(1000);
  Serial.println("start");
  delay(1000);
  
  Serial.println("s2");  
  /*
  Serial.println(F_CPU);
  Serial.println(8UL);
  Serial.println(256UL);
  Serial.println(SOFTPWM_FREQ);
  Serial.println(SOFTPWM_OCR);*/
/*
16000000
8
256
60
130
 */
  for (byte i = 0; i < SOFTPWM_MAXCHANNELS; i++) {
    _softpwm_channels[i].pin = -1;
    _softpwm_channels[i].polarity = SOFTPWM_NORMAL;
    _softpwm_channels[i].outport = 0;
    _softpwm_channels[i].fadeuprate = 0;
    _softpwm_channels[i].fadedownrate = 0;
  }

    OCR2 = 100;
    TCCR2 |= (1 << WGM21);    // Set to CTC Mode
    TIMSK |= (1 << OCIE2);    // Set interrupt on compare match

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


  SoftPWMSet(13, 0);
  SoftPWMSetFadeTime(13, 3000, 1000);
  SoftPWMSetFadeTime(8, 3000, 3000);
  Serial.println("ready");
  sei();    // enable interrupts
}

void loop() 
{
  Serial.println("on");

  SoftPWMSet(13, 255);
  SoftPWMSet(8, 255);

  delay(3050);

  Serial.println("s128");
  SoftPWMSet(13, 0);
  
  delay(3000);

  SoftPWMSet(8, 0 );

  Serial.println("s64"); 
  SoftPWMSet(13, 128);
  delay(3000);

  Serial.println("soff");
  SoftPWMSet(13, 0);

  delay(3000);
}

