#include <Wire.h>
#include <arduino.h>
#include <i2c_helpers.h>
#include <avr/eeprom.h>

// to jest slave
#define MASTER_ADDR 0x01
#define VERSION 0x01
#define DEVICE_TYPE 0x11

byte my_address = 0x00;
volatile byte in_buffer1[5];

void setup(){
  Serial.begin(38400);
  init_i2c();
  Wire.onReceive(receiveEvent);
  Wire.onRequest(requestEvent);
  Serial.println("gotowe");
}

void loop(){
  /*
  byte error, address;
  for(address = 1; address < 127; address++ )   {
    Wire.beginTransmission(address);
    error = Wire.endTransmission();
    if (error == 0){
      Serial.print("I2C device found at address: ");
      Serial.print(address,HEX);
      Serial.println("  !");
    }
    else if (error==4){
      Serial.print("Unknow error at address: ");
      Serial.println(address,HEX);
    }
  }*/
  delay(500);           // wait 5 seconds for next scan
}

void requestEvent(){ 
    byte command = in_buffer1[0];
    if( command == 0x19 ){          // TEPE + VERSION       3 bajty
        byte ttt[2] = {VERSION,DEVICE_TYPE};
        Serial.println("send type");
        Wire.write(ttt,2);
        in_buffer1[0] =0;
    }else if( command == 0x1A ){    // return xor
        byte res = in_buffer1[1] ^ in_buffer1[2];
        Wire.write(res);
        in_buffer1[0] =0;
    }
}

void receiveEvent(int howMany){
  byte cntr = 0;
  while( Wire.available()){ // loop through all but the last  
    in_buffer1[cntr] = Wire.read(); // receive byte as a character
    cntr++;
  }
}
