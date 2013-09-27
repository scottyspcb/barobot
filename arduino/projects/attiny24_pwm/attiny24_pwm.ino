volatile uint8_t licznik = 0;
#define I2C_SLAVE_ADDRESS 0x4 
#include <TinyWireS.h>

// The default buffer size, Can't recall the scope of defines right now
#ifndef TWI_RX_BUFFER_SIZE
#define TWI_RX_BUFFER_SIZE ( 16 )
#endif
volatile uint8_t i2c_regs[] ={
    0xDE, 
    0xAD, 
    0xBE, 
    0xEF, 
};

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






volatile byte reg_position;
void requestEvent(){  
    TinyWireS.send(i2c_regs[reg_position]);
    // Increment the reg position on each read, and loop back to zero
    reg_position = (reg_position+1) % sizeof(i2c_regs);
}

void blinkn(uint8_t blinks){
    digitalWrite(3, HIGH);
    while(blinks--)  {
        digitalWrite(3, LOW);
        delay(50);
        digitalWrite(3, HIGH);
        delay(100);
    }
}

/**
 * The I2C data received -handler
 *
 * This needs to complete before the next incoming transaction (start, data, restart/stop) does 
 */
void receiveEvent(uint8_t howMany)
{
    if (howMany < 1)
    {
        // Sanity-check
        return;
    }
    if (howMany > TWI_RX_BUFFER_SIZE)
    {
        // Also insane number
        return;
    }

    reg_position = TinyWireS.receive();
    howMany--;
    if (!howMany)
    {
        // This write was only to set the buffer for next read
        return;
    }
    while(howMany--)
    {
        i2c_regs[reg_position%sizeof(i2c_regs)] = TinyWireS.receive();
        reg_position++;
    }
}





// to jest master
#define MY_ADDR 0x02
#define MASTER_ADDR 0x02
#define VERSION 0x01
#define DEVICE_TYPE 0x12
int led = 13;
byte x = 0;
byte y = 10;
  byte address = 0x04;
byte in_buffer[10];
byte out_buffer[5];




unsigned int last_max_x = 655555;
long int margin_x = 0;          // odstępstwo od technicznej pozycji X
long int dlugosc_x = 0;
int dlugosc_z = 100;
boolean send_ret = true;        // czy wysyłać odpowiedzi po wykonaniu komend
boolean last_stepper_operation = false; 
boolean irr_handled_x = true;
boolean irr_min_x = false;          //czy jestem na dole?
boolean irr_max_x = false;
boolean irr_x  = HIGH;			// czy dotykam przerwania



const byte MY_ADDRESS = 42;
volatile boolean haveData = false;
volatile long fnum;
volatile long foo;


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




byte i8  = LEDSIZE;
void setup()
{
    // TODO: Tri-state this and wait for input voltage to stabilize 
    pinMode(3, OUTPUT); // OC1B-, Arduino pin 3, ADC
    digitalWrite(3, LOW); // Note that this makes the led turn on, it's wire this way to allow for the voltage sensing above.

    pinMode(1, OUTPUT); // OC1A, also The only HW-PWM -pin supported by the tiny core analogWrite

    /**
     * Reminder: taking care of pull-ups is the masters job
     */

    while(i8--){
      pinMode(leds[i8].pin, OUTPUT);  
    }

    TinyWireS.begin(I2C_SLAVE_ADDRESS);
    TinyWireS.onReceive(receiveEvent);
    TinyWireS.onRequest(requestEvent);

    // Whatever other setup routines ?
    
    digitalWrite(3, HIGH);
}

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
  


  TinyWireS_stop_check();
}

