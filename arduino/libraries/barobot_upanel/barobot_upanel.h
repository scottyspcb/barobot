#ifndef I2C_HELPERS
	#define I2C_HELPERS	
	#include <Arduino.h>
	#include <WSWire.h>
	#include <avr/eeprom.h>

	byte readRegisters( byte deviceAddress, byte length);
	byte writeRegisters( int deviceAddress, byte length, boolean wait);
	void printHex(byte val);
	void printHex(byte val, boolean newline);
	void DW(uint8_t pin, uint8_t val);
	byte addr_is_used( byte address);    // true jesli wolne
	void save_i2c_address( int epromaddress, byte new_address, byte old_address);
	boolean init_i2c();
	void delay2(word time);
	extern byte my_address;
#endif

