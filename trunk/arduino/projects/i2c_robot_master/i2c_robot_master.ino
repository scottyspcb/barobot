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
//  Wire.onReceive(receiveEvent);
  Serial.println("START MASTER");
}

byte in_buffer[10];
byte out_buffer[5];

void writeRegister(int deviceAddress, byte command, byte val, byte val2) {
    Wire.beginTransmission(deviceAddress); // start transmission to device 
    Wire.write(command); 
    Wire.write(val);
    Wire.write(val2);

    Serial.print("Wysylam: ");
    printHex(command);
    Serial.print("Wysylam: ");
    printHex(val);
    Serial.print("Wysylam: ");
    printHex(val2);
    
    int error = Wire.endTransmission();    // end transmission
    delay(10);
}

byte readRegister(int deviceAddress, byte command){
//    Serial.println("------ sendRegister START--------");

    Wire.beginTransmission(deviceAddress);
    Wire.write(command); // register to read
    int error = Wire.endTransmission();
    delay(10);

//    Serial.println("------ sendRegister END--------"+ String(error));
//    Serial.println("------ readRegister START--------");

    Wire.requestFrom(deviceAddress, 1); // read a byte
    while(Wire.available() == 0) {
      Serial.println("-czekam-");
    }
    return Wire.read();
}


byte GetRegisters(byte deviceAddress, byte command, byte length){
  Wire.beginTransmission(deviceAddress);
  Wire.write(command);
  int error = Wire.endTransmission();
  delay(10);
 // Serial.println("Czekam. Error:" + String(error));
  byte counter = 0;
  Wire.requestFrom(deviceAddress, length);
  delay(10);
  byte waits = 100;
  while(Wire.available() == 0 && waits--) {
    Serial.println("-czekam-");
  }
  if(waits==0){
    return 0x32;
  }
  while(Wire.available()){    // slave may send less than requested
    in_buffer[counter++] = Wire.read(); 
  };
  if(counter <length){
    Serial.println("-za malo: " + String(counter));  
  }

  /*
  int maxtry = 0;
  for (counter = 0; counter < length; counter++){
    if (Wire.available ()) {
      in_buffer[counter] = Wire.read();
      Serial.print("Czytam " + String(counter)+ " :" );
      printHex(in_buffer[counter]);
    }else{
      Serial.print("Nie ma wejscia:");
      return 0xFE;  // did not get all bytes
    }

  }*/
/*
  while(Wire.available() < length) {
    maxtry++;
  }
  while (Wire.available()){
    in_buffer[count] = Wire.read();
    Serial.print("Czytam:");
    printHex(in_buffer[count]);
    count++;
  }*/
 // Serial.println("Odebralem liczb:" + String(count) + ". Prob: " + String(maxtry));
  return 1;
}



// Funkcja odczytywania N rejestrow
byte readRegisters(int deviceAddress, byte length){
  Wire.requestFrom(deviceAddress, (int) length);
  int maxtry = 200;
  byte count = 0;
  /*
  while(Wire.available() < length ) {
    maxtry++;
  }*/
  while (Wire.available()){
    byte d = Wire.read();
    Serial.print("READ:");
    printHex(d);
    in_buffer[count] = d;//Wire.read();
    count++;
  }
  Serial.println("Odebralem liczb:" + String(count) + ". Prób: " + String(maxtry));
  return count;
}


// wysyla dowolną ilosc liczb na kanal
void writeRegisters(int deviceAddress, byte length, boolean wait) {
    byte c = 0;
    Wire.beginTransmission(deviceAddress); // start transmission to device 
    while( c < length ){
      Wire.write(out_buffer[c]);         // send value to write

      Serial.print("Wysylam: ");
      printHex(out_buffer[c]);

      c++;
    }
    int error = Wire.endTransmission();     // end transmission
    Serial.println("writeRegisters" + String(error));
    if(wait){
      delay(10);
    }
}

byte x = 0;
byte y = 10;

void loop(){
  byte error = 0;
  byte address = 0x04;
  Serial.println("---------------------------------------------LOOP-------------------------------------");
//  Wire.beginTransmission(address);
//  Serial.println("LOOP2");
//  error = Wire.endTransmission();
  Serial.println("LOOP3");

  if (error == 0){
//      Serial.print("address 0x");
//      Serial.println(address,HEX);
      writeRegister( address, 0x11, x, y );
      x++;
      y--;

      byte res = readRegister( address, 0x66 );
      Serial.print("ADDR 0x66: ");
      printHex(res);
      
      byte readed = GetRegisters(address, 0x66, 1);
      Serial.println("=====66(====" + String(readed));
      printHex(in_buffer[0]);
      printHex(in_buffer[1]);
      printHex(in_buffer[2]);
      Serial.println("=====)===");
/*
      res = readRegister( address, 0x56 );
      Serial.println("B=====56(====");
      Serial.println(res,HEX);
      Serial.println("B=====)===");
*/

      readed = GetRegisters(address, 0x88, 5);
      Serial.println("=====88(====" + String(readed));
      printHex(in_buffer[0]);
      printHex(in_buffer[1]);
      printHex(in_buffer[2]);
      printHex(in_buffer[3]);
      printHex(in_buffer[4]);
      printHex(in_buffer[5]);
      printHex(in_buffer[6]);
      printHex(in_buffer[7]);
      printHex(in_buffer[8]);
      Serial.println("=====)===");
      
/*

      byte readed2 = GetRegisters(address, 0x88, 2);
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
  }else{
      Serial.println("Nie ma urzadzenia");
  }

  digitalWrite(led, HIGH); 
  delay(100);
  digitalWrite(led, LOW);
  delay(100);

  digitalWrite(led, HIGH); 
  delay(100);
  digitalWrite(led, LOW);
  delay(100);

  digitalWrite(led, HIGH); 
  delay(100);
  digitalWrite(led, LOW);
  delay(100);

  digitalWrite(led, HIGH); 
  delay(100);
  digitalWrite(led, LOW);
  delay(100);

  digitalWrite(led, HIGH); 
  delay(100);
  digitalWrite(led, LOW);
  delay(100);

  digitalWrite(led, HIGH); 
  delay(100);
  digitalWrite(led, LOW);
  delay(100);

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




