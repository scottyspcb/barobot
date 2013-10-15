#include <Arduino.h>
#include <Wire.h>
#include <avr/eeprom.h>
#define MASTER_ADDR 0x01
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
*/
byte check_free( byte address){    // true jesli wolne
  //Serial.println("szukam:" + String(address)  );
  Wire.beginTransmission(address);
  return (Wire.endTransmission() == 2) ? true : false;    // 2 - wolne, inne = zajete;
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
    while( --ww ){
      int32_t wait = 50000;
      while(--wait ){
        asm("nop");
      }
    }
}
byte my_address = 0x00;
void init_i2c(){
  byte ad1 = eeprom_read_byte((unsigned char *) 0x00);
  byte ad2 = eeprom_read_byte((unsigned char *) 0x01);  
  byte ad3 = eeprom_read_byte((unsigned char *) 0x02);  

  if(ad1 == ad2 && ad1 == ad3 ){
    my_address   = ad1;
  }else{    // znajdz ten co sie nei zgadza i go zadpisz
    if( ad1 == ad2 ){        // ad3 sie nie zgadza
    	eeprom_write_byte((unsigned char *) 0x03, ad1);
    }else if( ad2 == ad3 ){  // ad1 sie nie zgadza
    	eeprom_write_byte((unsigned char *) 0x01, ad1);
    }else if( ad1 == ad3 ){  // ad2 sie nie zgadza
    	eeprom_write_byte((unsigned char *) 0x02, ad1);
    }
  }
  Serial.println("old" + String(my_address) );
  Wire.begin();    // chwilowo jako master
  pinMode(5,INPUT_PULLUP);
  pinMode(6,INPUT_PULLUP);
  while(check_free( MASTER_ADDR )){		// jeœli jest wolne to nie ma mastera = b³¹d i czekaj na mastera
	Serial.println("!master" );
	delay2(100);
  }
  
  if( my_address < 0x03 || my_address > 110 || !check_free(my_address)){    // zajety - sprawdzaj inne...
    for(my_address = 5; my_address <= 110; my_address++ ) {  // zarezerwowane adresy 0x00 - 0x03
        if(check_free(my_address)){    // gotowe, jest adres
              break;
        }
    }
	delay2(my_address);
  }
  save_i2c_address( 0x00, my_address, ad1 );    // zapisuje gdy my_address != oa
  Serial.println("na" + String(my_address) );
  Wire.begin(my_address);
}


