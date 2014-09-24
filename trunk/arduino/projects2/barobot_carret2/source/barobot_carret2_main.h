//-------------------------------------------------------------------
#ifndef __barobot_carret_main_H__
#define __barobot_carret_main_H__
//-------------------------------------------------------------------
 
#include <arduino.h>
uint8_t GetTemp();
//-------------------------------------------------------------------
 
//-------------------------------------------------------------------
 
// Put yout declarations here
 long unsigned int decodeInt(String input, byte odetnij );
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
byte localToGlobal( byte ind );
void sendln( volatile byte buffer[], byte length );
void send_y_pos( byte stateId, int16_t value);
void send_hx_pos( byte stateId, int16_t value );
void send_servo( boolean error, byte servo, uint16_t pos );
void run_to(byte index, byte sspeed, uint16_t target);
void timer();
void reload_servo( byte index );
void serialEvent();
void paserDeriver( byte driver, String input2 );
void send_error( String input);
void setColor(byte num, unsigned long int color);
void parseInput( String input );
void update_servo( byte index );
void readHall();
void change_state( byte oldStateId, byte newStateId, int16_t value );
void init_hallx();
byte get_hy_state_id( int16_t value);
byte get_hx_state_id( int16_t value);
void sendStepperReady();
void stepperReady( long int pos );
void loop();
void sendVal( byte n );
void set_all_leds(unsigned long int color);
void init_leds();
void setup();
//---- DO NOT DELETE THIS LINE -- @MM_DECLA_END@---------------------
// -> ...AND HERE. This space is reserved for automated code generation!
//===================================================================
 
 
//-------------------------------------------------------------------
#endif
//-------------------------------------------------------------------
 
