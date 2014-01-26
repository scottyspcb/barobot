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

volatile uint8_t _isr_count = 0xff;
volatile boolean pwmnow= false;
volatile uint16_t timertime = 100;
volatile unsigned long _nanotime = 0;
volatile uint8_t _timediv= 0;
volatile uint8_t checktime= 0;

void setup(){
  DEBUGINIT();
  pinMode(PIN_UPANEL_SCK, INPUT );         // stan wysokiej impedancji
  pinMode(PIN_UPANEL_MISO, INPUT );        // stan wysokiej impedancji
  pinMode(PIN_UPANEL_MOSI, INPUT );        // stan wysokiej impedancji
  pinMode(PIN_UPANEL_LEFT_RESET, INPUT);   // stan wysokiej impedancji
  pinMode(PIN_UPANEL_POKE, INPUT);

  digitalWrite(PIN_UPANEL_POKE, LOW);      // enable pullup, poke-switch

  // pootwieraj porty:
  DDRC |= _BV(PC2) | _BV(PC3);      // wyjscie
  DDRB |= _BV(PB0) | _BV(PB1);
  DDRD |= _BV(PD4) | _BV(PD5) | _BV(PD6) | _BV(PD7);
  digitalWrite(PIN_PANEL_LED7_NUM, HIGH );      // debug, oczekiwanie na adres
  if(!init_i2c()){
    {
      check_i2c_valid();
    } while( !init_i2c() );
  }
//    show_error(5 );
  Wire.onReceive(receiveEvent);
  Wire.onRequest(requestEvent);
  send_here_i_am();  // wyslij po i2c ze oto jestem

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

  digitalWrite(PIN_PANEL_LED2_NUM, HIGH );
  digitalWrite(PIN_PANEL_LED3_NUM, HIGH );

//  attachInterrupt(0, button_down, FALLING);
}

byte button_down = 0;

void loop() {
  /*
   unsigned long mil = millis();
    // debug:
  	if( mil > milis1 + 1000 ){    // debug, mrygaj co 1 sek
                diddd = !diddd;
  		milis1 = mil;
                digitalWrite(PIN_PANEL_LED3_NUM, diddd);
  	}
  	if( mil > milis4 + 4000 ){    // co 4 sek
                send_pin_value( PIN_UPANEL_POKE, diddd ? 1 : 0 );
  		milis4 = mil;
  	}
*/
    if( use_local&& in_buffer1[0] ){          // komendy bez wymaganej odpowiedzi do mastera obsluguj tutaj:
      byte command = in_buffer1[0];
      if( command == METHOD_SETPWM ){                // PWM     3 bajty
           // setPWM(in_buffer1[1],in_buffer1[2]);
 //           leds[in_buffer1[1]].wypelnienie = in_buffer1[2];

      }else if( command ==  METHOD_SETLED ){         // 
        byte settings = in_buffer1[1];    // 8 bits = 8 leds
        
        
        boolean a0 = bitRead(in_buffer1[0], 0);
        boolean a1 = bitRead(in_buffer1[1], 0);
        boolean a2 = bitRead(in_buffer1[2], 0);
        boolean a3 = bitRead(in_buffer1[3], 0);
        boolean a4 = bitRead(in_buffer1[5], 0);
        boolean a5 = bitRead(in_buffer1[6], 0);
        boolean a6 = bitRead(in_buffer1[7], 0);
        


//      }else if( command == METHOD_RESETCYCLES ){         // reset
//      }else if( command == METHOD_SETTIME ){         // set time
//      }else if( command == METHOD_SETFADING ){         // fadein out
      }else if( command == METHOD_PROG_MODE_ON ){         // i2c in prog mode (master programuje jakis slave, ale nie mnie)
        digitalWrite(LED_TOP_RED, HIGH);
        if(in_buffer1[1] == my_address){
          prog_me = true;
          digitalWrite(LED_TOP_RED, HIGH);
          digitalWrite(LED_BOTTOM_RED, HIGH);
        }else{
          prog_me = false;
        }
        prog_mode = true;
//      }else if( command == METHOD_GETANALOGVALUE ){  // get analog value
    /*
        uint16_t value = analogRead(in_buffer1[1]);
        byte ttt[2]    = {value>>8, value & 0xff };
        Wire.write(ttt,2);*/
  //  }else if( command == 0x28 ){  // get digital value
      /*  boolean value  = digitalRead(in_buffer1[1]);
        byte ttt[1]    = {value ? 0xff:0xff};
        Wire.write(ttt,1);*/
      }else if( command == METHOD_PROG_MODE_OFF ){         // i2c in prog mode off
        digitalWrite(LED_TOP_RED, LOW);
        prog_mode = false;
      }else if( command == METHOD_RESET_NEXT ){         // Resetuj urządzenie obok
        reset_next( LOW );
      }else if( command == METHOD_RUN_NEXT ){          // Koniec resetu urządzenia obok, ustaw pin w stan wysokiej impedancji
        reset_next( HIGH );
      }else if( command == METHOD_RESETSLAVEADDRESS ){          // zmien address
      }
      in_buffer1[0] = 0;
      use_local = false;
   }

   // todo, zrobić jakiś prosty debouncing (zeby drganie styków przycisku nie miało aż takiego wpływu)
   if( digitalRead(PIN_UPANEL_POKE) ){    // wcisnieto poke-switch
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
  //if ( bit_is_clear( in_buffer1[0], 0 ) ){      // IF like: xxxx xxx1 - run in main loop, else in requestEvent
   if( in_buffer1[0] != METHOD_GETVERSION &&  in_buffer1[0] != METHOD_TEST_SLAVE ){
      use_local = true;
  }
  // w tym miejscu jednynie bardzo proste komendy nie wymagające zwrotek pracujące w funkcji obsługi przerwania, pamietac o volatile
}

void requestEvent(){ 
  // w in_buffer jest polecenie
    if( in_buffer1[0] == METHOD_GETVERSION ){          // TEPE + VERSION       3 bajty
        byte ttt[2] = {UPANEL_DEVICE_TYPE,UPANEL_VERSION};
        Wire.write(ttt,2);
    }else if( in_buffer1[0] == METHOD_TEST_SLAVE ){    // return xor
        byte res = in_buffer1[1] ^ in_buffer1[2];
        Wire.write(res);
        /*
        if( res & 0x01 ){    // ustawiony najmlodzzy bit
          diddd = !diddd;
          digitalWrite(PIN_PANEL_LED4_NUM, diddd);
        }*/
    }
}

static void send_pin_value( byte pin, byte value ){
  byte ttt[5] = {METHOD_I2C_SLAVEMSG,RETURN_PIN_VALUE,my_address,pin,value};
  send(ttt,5);
 // Serial.println("out "+ String( my_address ) +" / "+ String( pin ) +"/"+ String(value));
}

static void send_here_i_am(){
  byte ttt[4] = {METHOD_HERE_I_AM,my_address,UPANEL_DEVICE_TYPE,UPANEL_VERSION};
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

void digitalWrite2(uint8_t pin, uint8_t val){
        uint8_t bit = digitalPinToBitMask(pin);
        uint8_t port = digitalPinToPort(pin);
        if (port == NOT_A_PIN){
          return;
      }
      volatile uint8_t *out;
      out = portOutputRegister(port);
      if (val == LOW) {
              *out &= ~bit;
      } else {
              *out |= bit;
      }
}



































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

/*
void swiec(){
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
*/

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







