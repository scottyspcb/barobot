//-------------------------------------------------------------------
#ifndef __barobot_upanel_main_H__
#define __barobot_upanel_main_H__
//-------------------------------------------------------------------

#include <arduino.h>

//-------------------------------------------------------------------

//-------------------------------------------------------------------

// Put yout declarations here


void send_pin_value( byte pin, byte value );
void send_here_i_am();
void send( byte buffer[], byte ss );
//-------------------------------------------------------------------

//===================================================================
// -> DO NOT WRITE ANYTHING BETWEEN HERE...
// 		This section is reserved for automated code generation
// 		This process tries to detect all user-created
// 		functions in main_sketch.cpp, and inject their  
// 		declarations into this file.
// 		If you do not want to use this automated process,  
//		simply delete the lines below, with "&MM_DECLA" text 
//===================================================================
//---- DO NOT DELETE THIS LINE -- @MM_DECLA_BEG@---------------------
void PWMSetFadeTime(uint8_t pin, uint8_t up, uint8_t down);
void PWMSet(uint8_t pin, uint8_t up);
void PWM(uint8_t pin, uint8_t pwmup, uint8_t pwmdown, uint8_t timeup, uint8_t timedown, uint8_t fadeup, uint8_t fadedown);
void check_i2c_valid();
void send( byte buffer[], byte ss );
void send_here_i_am();
void send_pin_value( byte pin, byte value );
void requestEvent();
void receiveEvent(int howMany);
void reset_next(boolean value);
void loop();
void setup();
//---- DO NOT DELETE THIS LINE -- @MM_DECLA_END@---------------------
// -> ...AND HERE. This space is reserved for automated code generation!
//===================================================================


//-------------------------------------------------------------------
#endif
//-------------------------------------------------------------------
