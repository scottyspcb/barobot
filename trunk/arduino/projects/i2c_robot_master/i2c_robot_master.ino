#include <Wire.h>

// to jest master
#define MY_ADDR 0x02
#define MASTER_ADDR 0x02
#define VERSION 0x01
#define DEVICE_TYPE 0x12
int led = 13;
void setup(){
  Wire.begin();
    pinMode(led, OUTPUT); 
  Serial.begin(115200);
  Serial.println("\nI2C Scanner");

//  Wire.onReceive(receiveEvent);
  Serial.println("START MASTER");
}

void writeRegister(int deviceAddress, byte command, byte val, byte val2) {
    Wire.beginTransmission(deviceAddress); // start transmission to device 
    Wire.write(command);       // send register address
    Wire.write(val);           // send value to write
    Wire.write(val2);          // send value to write
    Wire.endTransmission();    // end transmission
    delay(10);
}

int readRegister(int deviceAddress, byte command){
    Wire.beginTransmission(deviceAddress);
    Wire.write(command); // register to read
    Wire.endTransmission();
    Wire.requestFrom(deviceAddress, 1); // read a byte
    while(!Wire.available()) {}
    return Wire.read();
}
byte in_buffer[10];
byte out_buffer[5];

// Funkcja odczytywania N rejestrow
byte twiGetRegisters(int deviceAddress, byte command, byte length){
  byte count = 0;
  Wire.beginTransmission(deviceAddress);
  Wire.write(command);
  Wire.endTransmission();

  Wire.requestFrom(deviceAddress, (int) length);
  while (Wire.available()){
    in_buffer[count] = Wire.read();
    count++;
  }
  return count;
}



void writeRegisters(int deviceAddress, byte length, boolean wait) {
    byte c = 0;
    Wire.beginTransmission(deviceAddress); // start transmission to device 
    while( c < length){
      Wire.write(out_buffer[c++]);         // send value to write
      c++;
    }
    Wire.endTransmission();     // end transmission
    if(wait){
      delay(10);
    }
}




byte x = 0;
byte y = 10;

void loop(){
  byte error, address;
  int nDevices;

  nDevices = 0;
  address = 0x04;
//  for(address = 1; address < 127; address++ ){
    Wire.beginTransmission(address);
    error = Wire.endTransmission();
    if (error == 0){
//      Serial.print("address 0x");
//      Serial.println(address,HEX);

      writeRegister( address, 0x11, x, y );
      Serial.println("Wpisalem x " + String(x));
      Serial.println("Wpisalem y " + String(y));
      x++;
      y--;

      byte res = readRegister( address, 0x66 );
      Serial.println("A=====66(====");
      Serial.println(res,HEX);
      Serial.println("A=====)===");      
/*
      res = readRegister( address, 0x56 );
      Serial.println("B=====56(====");
      Serial.println(res,HEX);
      Serial.println("B=====)===");
*/
/*
      byte readed = twiGetRegisters(address, 0x99, 3);
      Serial.println("B=====99(====" + String(readed));
      printHex(in_buffer[0]);
      printHex(in_buffer[1]);
      printHex(in_buffer[2]);
      printHex(in_buffer[3]);
      printHex(in_buffer[4]);
      Serial.println("B=====)===");


      byte readed2 = twiGetRegisters(address, 0x88, 2);
      Serial.println("C=====88(====" + String(readed2));
      printHex(in_buffer[0]);
      printHex(in_buffer[1]);
      printHex(in_buffer[2]);
      printHex(in_buffer[3]);
      printHex(in_buffer[4]);
      Serial.println("C=====)===");
*/
/*
      Wire.beginTransmission(address); // transmit to device #4
      String a = "master x is " + String(x);
      const char* aa = (const char *) a.c_str();
      Wire.write(aa);
      Wire.endTransmission(); // stop transmitting
  */      
//   }  
  }

  digitalWrite(led, HIGH);   // turn the LED on (HIGH is the voltage level)
  delay(1000);               // wait for a second
  digitalWrite(led, LOW);    // turn the LED off by making the voltage LOW
  delay(1000);               // wait for a second

  digitalWrite(led, HIGH);   // turn the LED on (HIGH is the voltage level)
  delay(1000);               // wait for a second
  digitalWrite(led, LOW);    // turn the LED off by making the voltage LOW
  delay(1000);               // wait for a second

}
void printHex(byte val){
  int temp =  val;
  Serial.println(temp,HEX);
}

/*
void receiveEvent(int howMany){
  while( Wire.available()){ // loop through all but the last  
    char aa = Wire.read(); // receive byte as a character
    Serial.println(aa,HEX);
  }
}*/




