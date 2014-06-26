//-------------------------------------------------------------------
#ifndef __barobot_mainboard_main_H__
#define __barobot_mainboard_main_H__
//-------------------------------------------------------------------
 
#include <arduino.h>


 
uint8_t GetTemp();
 
//-------------------------------------------------------------------
 
//-------------------------------------------------------------------
 
// Put yout declarations here
 long unsigned decodeInt(String input, int odetnij );
boolean reset_device_num( byte num, boolean pin_value );
boolean reset_device_num2( byte num, boolean pin_value );
uint16_t i2c_getVersion( byte slave_address );
uint8_t spi_transaction(uint8_t a, uint8_t b, uint8_t c, uint8_t d);
uint8_t write_flash_pages(int length);

uint8_t hw_spi_send(uint8_t b);
void spi_wait();

void hw_spi_init();
void printHex8(uint8_t *data, uint8_t length);
void printHex8(volatile uint8_t *data, uint8_t length);
uint8_t sw_spi_send(uint8_t b);

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
void serialEvent();
void writeRegisters(int deviceAddress, byte length, boolean wait);
void paserDeriver( String input );
void sendStepperReady( long int pos );
void stepperReady( long int pos );
void send_error( String input);
void parseInput( String input );
void loop();
void timer();
void setup();
//---- DO NOT DELETE THIS LINE -- @MM_DECLA_END@---------------------
// -> ...AND HERE. This space is reserved for automated code generation!
//===================================================================
 

 
//-------------------------------------------------------------------
#endif
//-------------------------------------------------------------------
 
