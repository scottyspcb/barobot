//-------------------------------------------------------------------
#ifndef __barobot_carret_main_H__
#define __barobot_carret_main_H__
//-------------------------------------------------------------------
 
#include <arduino.h>
uint8_t GetTemp();

int16_t readValue(byte pin) ;


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
void sendln( volatile byte buffer[], byte length );
void send_hx_pos2( byte state_name, byte dir, int16_t value );
void send_hx_pos( byte stateId, int16_t value );
void timer3();
void timer2();
void reload_servo();
void serialEvent();
void paserDriver( byte driver, String input2 );
void send_error( String input);
void setColor(byte num, unsigned long int color);
byte sensor2analogPin(byte sensorNum);
void parseInput( String input );
void send_servoYisReady();
void sendZpos();
void sendYpos();
void enableYZ();
void disableYZ();
void disableServosNow();
void stopY( boolean addDelay );
void stopZ( boolean addDelay );
void moveZto( unsigned int target );
void moveYto(byte sspeed, uint16_t target);
void step_servoZ();
void step_servoY();
void readHall();
byte get_hx_state_id( int16_t value);
void sendStepperReady();
void set_all_leds(unsigned long int color);
void loop();
void init_hallx(byte pin);
void sendStats( boolean isStart );
void setup();
//---- DO NOT DELETE THIS LINE -- @MM_DECLA_END@---------------------
// -> ...AND HERE. This space is reserved for automated code generation!
//===================================================================
 
 
//-------------------------------------------------------------------
#endif
//-------------------------------------------------------------------
 
