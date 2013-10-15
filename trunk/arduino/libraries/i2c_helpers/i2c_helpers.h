#ifndef I2C_HELPERS
#define I2C_HELPERS


byte readRegisters( byte deviceAddress, byte length);
byte writeRegisters( int deviceAddress, byte length, boolean wait);
void printHex(byte val);
void printHex(byte val, boolean newline);


byte check_free( byte address);    // true jesli wolne
void save_i2c_address( int epromaddress, byte new_address, byte old_address);
void init_i2c();
extern byte my_address;
#endif

