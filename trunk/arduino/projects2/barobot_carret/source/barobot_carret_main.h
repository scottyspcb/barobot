//-------------------------------------------------------------------
#ifndef __barobot_carret_main_H__
#define __barobot_carret_main_H__
//-------------------------------------------------------------------
 
#include <arduino.h>
#include <twi.h>
#include <WSWire.h>
 
//-------------------------------------------------------------------
 
//-------------------------------------------------------------------
 
// Put yout declarations here
 
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
void enable_pin( byte pin );
void disable_pin( byte pin );
void init_analogs();
byte localToGlobal( byte ind );
byte globalToLocal( byte ind );
void send( byte buffer[], byte length );
void send_here_i_am();
void send_y_pos( byte reason);
void send_x_pos( byte reason, boolean is_up, boolean is_down );
void send_servo( boolean error, byte servo, uint16_t pos );
void requestEvent();
void receiveEvent(int howMany);
void run_to(byte index, byte sspeed, uint16_t target);
void proceed( volatile byte buffer[5] );
void timer();
void reload_servo( byte index );
void update_servo( byte index );
void loop();
void init_leds();
void setup();
//---- DO NOT DELETE THIS LINE -- @MM_DECLA_END@---------------------
// -> ...AND HERE. This space is reserved for automated code generation!
//===================================================================
 
 
//-------------------------------------------------------------------
#endif
//-------------------------------------------------------------------
 
