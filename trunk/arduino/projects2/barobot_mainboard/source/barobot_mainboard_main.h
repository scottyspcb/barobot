//-------------------------------------------------------------------
#ifndef __barobot_mainboard_main_H__
#define __barobot_mainboard_main_H__
//-------------------------------------------------------------------
 
#include <arduino.h>
#include <AccelStepper.h>
 
 
uint8_t GetTemp();
 
//-------------------------------------------------------------------
 
//-------------------------------------------------------------------
 
// Put yout declarations here
 long unsigned decodeInt(String input, int odetnij );
boolean reset_device_num( byte num, boolean pin_value );

boolean reset_device_num2( byte num, boolean pin_value );
uint16_t i2c_getVersion( byte slave_address );
unsigned int test_slave(byte slave_address, byte tests);
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
void simulateisp();
void read_signature();
void program_page();
void read_page();
void write_flash(int length);
int current_page(int addr);
void commit(int addr);
void flash(uint8_t hilo, int addr, uint8_t data);
void universal();
void set_parameters();
void get_version(uint8_t c);
void sw_spi_init();
void breply(uint8_t b);
void empty_reply();
void spi_wait();
void hw_spi_init();
void fill(int n);
void heartbeat();
void pulse(int pin, int times);
void start_pmode();
void programmer_mode();
void reset_wire2();
void reset_wire();
void check_i2c();
void serialEvent();
void send2android( String output2 );
void send2android( volatile uint8_t buffer[], int length );
void send2android( uint8_t bits8 );
void sendln2android( String output2 );
void send2androidEnd();
byte writeRegisters(int deviceAddress, byte length, boolean wait);
byte readRegisters(byte deviceAddress, byte length);
void receiveEvent(int howMany);
byte i2c_test_slave( byte slave_address, byte num1, byte num2 );
void i2c_stop( byte slave_address );
void i2c_analog_off( byte slave_address, byte analog );
void i2c_analog( byte slave_address, byte analog );
void reset_device_next_to( byte slave_address, boolean pin_value );
byte checkAddress( byte address );
void read_prog_settings( String input, byte ns );
byte get_local_pin( byte index );
void i2c_test_slaves();
void tri_state( byte pin_num, boolean pin_value );
void paserDeriver( byte driver, String input );
void stepperReady( long int pos );
byte read_can_fill();
void send_error( String input);
void i2c_device_found( byte addr,byte type,byte ver );
void parseInput( String input );
void proceed( byte length,volatile uint8_t buffer[7] );
void loop();
void timer();
void setupStepper();
void setup();
void disableWd();
//---- DO NOT DELETE THIS LINE -- @MM_DECLA_END@---------------------
// -> ...AND HERE. This space is reserved for automated code generation!
//===================================================================
 

 
//-------------------------------------------------------------------
#endif
//-------------------------------------------------------------------
 
