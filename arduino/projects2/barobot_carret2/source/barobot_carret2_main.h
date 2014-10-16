//-------------------------------------------------------------------
#ifndef __barobot_carret_main_H__
#define __barobot_carret_main_H__
//-------------------------------------------------------------------
 
#include <arduino.h>
uint8_t GetTemp();
//-------------------------------------------------------------------
 
//-------------------------------------------------------------------
 
// Put yout declarations here
inline void  setupStepper();
 long unsigned int decodeInt(String input, byte odetnij );
 inline void init_leds();
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
void stepperReady( long int pos );
byte localToGlobal( byte ind );
void sendln( volatile byte buffer[], byte length );
void send_y_pos( byte stateId, int16_t value);
void send_hx_pos2( byte state_name, byte dir, int16_t value );
void send_hx_pos( byte stateId, int16_t value );
void send_servo( boolean error, byte servo, uint16_t pos );
void run_to(byte index, byte sspeed, uint16_t target);
void timer();
void reload_servo( byte index );
void serialEvent();
void paserDeriver( byte driver, String input2 );
void disable6v();
void enable6v();
void send_error( String input);
void setColor(byte num, unsigned long int color);
void parseInput( String input );
void update_servo( byte index );
void readHall();
void init_hallx();
byte get_hy_state_id( int16_t value);
byte get_hx_state_id( int16_t value);
void sendStepperReady();
void set_all_leds(unsigned long int color);
void disableServeNow(byte index);
void loop();
void sendVal( byte n );
void sendStats();
void setup();
//---- DO NOT DELETE THIS LINE -- @MM_DECLA_END@---------------------
// -> ...AND HERE. This space is reserved for automated code generation!
//===================================================================
 
 
//-------------------------------------------------------------------
#endif
//-------------------------------------------------------------------
 
