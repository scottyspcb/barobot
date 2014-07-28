#include <i2c_helpers.h>
#include <barobot_common.h>
/*
byte in_buffer[5];
byte out_buffer[5];

// Funkcja odczytywania N rejestrow
byte readRegisters(byte deviceAddress, byte length){
  Wire.requestFrom(deviceAddress, length);
  byte count = 0;
  byte waits = 100;
  while(Wire.available() == 0 && waits--) {
  }
  if(waits==0){
   // Serial.print("niedoczekanie...");
    return 0x32;
  }

  while (Wire.available()){
    in_buffer[count] = Wire.read();
    count++;
  }
  if( count != length){
    Serial.println("!!!malo liczb:" + String(count)+"/"+ String(length) );
  }
  return count;
}

// wysyla dowoln¹ ilosc liczb na kanal
byte writeRegisters(int deviceAddress, byte length, boolean wait) {
    byte c = 0;
    Wire.beginTransmission(deviceAddress); // start transmission to device 
    while( c < length ){
      Wire.write(out_buffer[c]);         // send value to write
//      Serial.print("Wysylam: ");
//      printHex(out_buffer[c]);
      c++;
    }
    byte error = Wire.endTransmission();     // end transmission
    if( error ){
      Serial.println("writeRegisters er:" + String(error));
      delay(100);
      return error;
    }
    if(wait){
      delay(20);
    }
    return 0;
}
*/
/*
void printHex2(volatile uint8_t data){
	if (data<0x10) {
		Serial.print("0");
	}
	Serial.print(data,HEX); 
	Serial.print(" "); 

}

void printHex8(uint8_t *data, uint8_t length){ // prints 8-bit data in hex with leading zeroes
	for (int i=0; i<length; i++) { 
	  printHex2(data[i]);
	}
}

void printHex8(volatile uint8_t *data, uint8_t length){ // prints 8-bit data in hex with leading zeroes
	for (int i=0; i<length; i++) { 
		printHex2(data[i]);
	}
}
*/
void printHex(byte val){
  int temp =  val;
  Serial.println(temp,HEX);
}

void printHex(byte val, boolean newline){
	int temp =  val;
	if(newline){
		Serial.println(temp,HEX);
	}else{
		Serial.print(temp,HEX);
	}
}
byte addr_is_used( byte address){    // true jesli wolne
	Wire.beginTransmission(address);
	byte ee = Wire.endTransmission();
	Serial.println("szukam:" +String(address)+"/"+ String(ee) );
	if( ee == 2 || ee == 6 ){		// kody b³êdów
		return 0;
	}
	if( ee == 0){
		return 1;
	}
	return 6;

	/*
		*	0 .. USED
		*	1 .. not possible
		*	2 .. FREE
		*	3 .. not possible
		*	4 .. ERROR
		*	5 .. timed out while trying to become Bus Master
		*	6 .. timed out while waiting for data to be sent
	*/

	/*
		RETURNS
		*	0 .. NOT USED,	FREE		(2)
		*	1 .. USED,		NOT FREE	(0)
		*	6 .. ERROR,		TIMEOUT		(6)
	*/
  //boolean bb = (ee == 2) ? true : false;    // 2 - wolne, inne = zajete;
 // return bb;
}

void save_i2c_address( int epromaddress, byte new_address, byte old_address){
	if( new_address != old_address ){
		eeprom_write_byte((unsigned char *) 0x00, new_address);
		eeprom_write_byte((unsigned char *) 0x01, new_address);
		eeprom_write_byte((unsigned char *) 0x02, new_address);
	}
}

void delay2( word ww ){
//    uint32_t wait = 0xFFFFFF;
//    uint32_t wait = 4294967295;
    //word ww = my_address;
	++ww;	// jesli wstawilem zero to masakra
    while( --ww ){
		// F_CPU =  8000000L or 16000000L
		//uint16_t wait = 50000;		//65536max
		uint16_t wait = F_CPU/8000;		//65536max, 1000
		while(--wait ){
			asm("nop");
		}
    }
}

void DW(uint8_t pin, uint8_t val){
      uint8_t bit  = digitalPinToBitMask(pin);
      uint8_t port = digitalPinToPort(pin);
      if (port == NOT_A_PIN){
        return;
      }
      volatile uint8_t *out;
      out = portOutputRegister(port);
      if (val == LOW) {
              *out &= ~bit;
      } else {
              *out |= bit;
      }
}

byte my_address = 0x00;
boolean init_i2c(){

	Wire.begin();    // chwilowo jako master

	// DEactivate internal pullups for twi.
	DW(SDA, 0);
	DW(SCL, 0);

	byte ad1 = eeprom_read_byte((unsigned char *) 0x00);
	byte ad2 = eeprom_read_byte((unsigned char *) 0x01);  
//	byte ad3 = eeprom_read_byte((unsigned char *) 0x02);  

	if(ad1 == ad2 ){		// && ad1 == ad3
		my_address   = ad1;
	}else{    // znajdz ten co sie nei zgadza i go zapisz
		if( ad1 == ad2 ){        // ad3 sie nie zgadza
			eeprom_write_byte((unsigned char *) 0x03, ad1);
	//	}else if( ad2 == ad3 ){  // ad1 sie nie zgadza
	//		eeprom_write_byte((unsigned char *) 0x01, ad1);
	//	}else if( ad1 == ad3 ){  // ad2 sie nie zgadza
	//		eeprom_write_byte((unsigned char *) 0x02, ad1);
		}
	}
	delay2(my_address);

	//Serial.println("old" + String(my_address) );
	while(!addr_is_used( I2C_ADR_MAINBOARD )){		// jeœli jest wolne to nie ma mastera = b³¹d i czekaj na mastera
		delay2(100);
		return false;
	}

	//Serial.println( "+m" );
//	my_address = 0;
	if( my_address < I2C_ADR_USTART || my_address > I2C_ADR_UEND || addr_is_used(my_address)){    // zajety - sprawdzaj inne...
	//	Serial.println("-");
		my_address = I2C_ADR_USTART;
		while( (++my_address )<=110 && addr_is_used(my_address) ){		// a¿ do znalezienia wolnego lub konca listy
		//	asm("nop");
		}
	}
	if( my_address == I2C_ADR_UEND ){		// czyli przeskanowalem cala magistrale i nie bylo zadnego wolnego = magistrala wisi
		return false;
	}
	Wire.begin(my_address);
	//Serial.println("na" + String(my_address) );
	save_i2c_address( 0x00, my_address, ad1 );    // zapisuje gdy my_address != oa
	return true;
}

/*

LEDS 12,0xff,255
LEDS 12,0x00,100

LEDS 12,0x00,0
LEDS 12,0x00,4
LEDS 12,0x00,99
LEDS 12,0x00,99
LEDS 12,0x00,255
LEDS 12,0x0a,100

LEDS 12,0x0E,255

LEDS 12,0x0e,255


 
UPANEL 
0xf1
11110001






CARRET


LEDS 12,0xf1,0




Bity:
76543210

0	
1	
2	
3	
4	
5	
6	
7	






*/




/*
    * Output   0 .. success
    *          1 .. length to long for buffer
    *          2 .. address send, NACK received
    *          3 .. data send, NACK received
    *          4 .. other twi error (lost bus arbitration, bus error, ..)
    *          5 .. timed out while trying to become Bus Master
    *          6 .. timed out while waiting for data to be sent
*/



/*
void delay_ms(uint16_t ms) {
while (ms) {
_delay_ms(1);
ms--;
}
}
 
*/