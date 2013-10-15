#include <Wire.h>
#include <Arduino.h>

template <typename T> byte I2C_writeAnything (const T& value){
	const byte * k = (const byte*) &value;
	byte i;
	for (i = 0; i < sizeof value; i++){
		Wire.write(*k++);
	}
	return i;
}

template <typename T> byte I2C_readAnything(T& value){
	byte * k = (byte*) &value;
	byte i;
	for (i = 0; i < sizeof value; i++){
		*k++ = Wire.read();
	}
	return i;
}