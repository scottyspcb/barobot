#include <Wire.h>

// to jest master
#define MY_ADDR 0x01
#define MASTER_ADDR 0x01
#define VERSION 0x01
#define DEVICE_TYPE 0x12
int led = 13;

volatile byte in_buffer[10];
volatile byte out_buffer[10];
volatile byte x = 0;
volatile byte y = 10;


void setup(){
  Wire.begin();
  pinMode(led, OUTPUT); 
  Serial.begin(115200);
  Wire.onReceive(receiveEvent);
  Serial.println("START MASTER");
}

byte pin = 0;

void loop(){
  byte error = 0;
  byte address = 0x04;
  Serial.println("---------------------------------------------LOOP-------------------------------------");
//  Wire.beginTransmission(address);
//  Serial.println("LOOP2");
//  error = Wire.endTransmission();
  if (error == 0){
      unsigned int errors= test_slave( address );
      i2c_setPWM( address, pin, x );
      x = x + 30;
      if( x == 0 ){
         x = 100;
      }
      pin++;
      if( pin > 8 ){
          pin = 0;
      }

      uint16_t readed = i2c_getVersion(address);
      Serial.println("-----i2c_getVersion" );
      printHex( readed>>8 );
      printHex( readed & 0xff );
 
//   }  
  }else{
      Serial.println("Nie ma urzadzenia");
  }
/*
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
  delay(100);*/
}


void i2c_resetCycles( byte slave_address ){
  out_buffer[0]  = 0x10;
  writeRegisters(slave_address, 1, true );
}
void i2c_setPWM( byte slave_address, byte pin, byte level ){		                              // Wpisz wypełnienie PWM do LEDa ( 3 bajty )
  out_buffer[0]  = 0x11;
  out_buffer[1]  = pin;
  out_buffer[2]  = level;
  writeRegisters(slave_address, 3, true );
}
void i2c_setTime( byte slave_address, byte pin, byte on_time, byte off_time ){			            // Czas pomiędzy kolejnym zapaleniem i Czas od zapalenia do zgaszenia ( 4 bajty )
  out_buffer[0]  = 0x12;
  out_buffer[1]  = pin;
  out_buffer[2]  = on_time;
  out_buffer[3]  = off_time;
  writeRegisters(slave_address, 3, true );
}
void i2c_setFading( byte slave_address,  byte pin, byte level ){				                // Czas i kierunek zanikania PWMa	( 3 bajty )
  out_buffer[0]  = 0x13;
  out_buffer[1]  = pin;
  out_buffer[2]  = level;
  writeRegisters(slave_address, 2, true );
}
void i2c_setDir( byte slave_address, byte pin, byte dir ){						        // Ustaw kierunek dla PINU o numerze ( 3 bajty )
  out_buffer[0]  = 0x14;
  out_buffer[1]  = pin;
  out_buffer[2]  = dir;
  writeRegisters(slave_address, 3, true );
}
void i2c_setOutput( byte slave_address, byte pin, byte value ){			                	// Wpisz cyfrowo PIN o numerze	 ( 3 bajty )
  out_buffer[0]  = 0x15;
  out_buffer[1]  = pin;
  out_buffer[2]  = value;
  writeRegisters(slave_address, 3, true );
}
void i2c_setOutput( byte slave_address, byte new_addr ){			                	// Zmień adres I2c, musi być podane co najmniej 4 razy zeby zadziałało. (2 bajty)
  out_buffer[0]  = 0x1E;
  out_buffer[1]  = new_addr;
  writeRegisters(slave_address, 2, true );    // powtarzaj
  writeRegisters(slave_address, 2, true );
  writeRegisters(slave_address, 2, true );
  writeRegisters(slave_address, 2, true );
  writeRegisters(slave_address, 2, true );
}

uint16_t i2c_getVersion( byte slave_address ){      // zwraca 2 bajty. typ na młodszych bitach, versja na starszych
  out_buffer[0]  = 0x19;
  byte error = writeRegisters(slave_address, 1, true );
  if( error ){
   Serial.println("i2c_getVersion error1");  
  }
  if( !error ){
    readRegisters( slave_address, 2 );
    uint16_t res = in_buffer[1];    // = wersja
    res = (res<<8) | in_buffer[0];    // = typ
    return res;
  }
  if( error ){
   Serial.println("i2c_getVersion error1");  
  }
  return 0xFF;
}

void i2c_stop(  byte slave_address ){      // zgaś wszystko
  out_buffer[0]  = 0xEE;
  writeRegisters(slave_address, 1, true );
  writeRegisters(slave_address, 1, true );
}


byte i2c_test_slave( byte slave_address, byte num1, byte num2 ){      // testuj
  out_buffer[0]  = 0x1A;
  out_buffer[1]  = num1;
  out_buffer[2]  = num2;  
  byte error = writeRegisters(slave_address, 3, true );
  if( error != 0 ){
   Serial.println("Test error1");  
  }
  if( error == 0 ){
    readRegisters( slave_address, 1 );
    return in_buffer[0];
  }
  if( error ){
   Serial.println("Test error2");  
  }
  return 0xFF;
}

unsigned int test_slave(byte slave_address){
  byte cntr1 = 20;
  const byte c2_max = 10;
  byte res = 0;
  unsigned int errors= 0;
  while(cntr1--){
    byte cntr2 = c2_max;  
    while(cntr2--){
      res = i2c_test_slave(slave_address, cntr1, cntr2);
      byte valid = cntr1 ^ cntr2;
      if(res !=valid){
        errors++;
        Serial.println("zle "+ String(res) + " != " + String( valid ) );
      }
   //  delay(10);
    }    
  }
  unsigned int cc = cntr1 * c2_max;
  Serial.println("------------Test_slave (" + String(cc) + "): " + String(errors));
  return errors;
}

boolean i2c_getValue(  byte slave_address, byte pin ){      // zwraca 2 bajty. typ na młodszych bitach, versja na starszych
  out_buffer[0]  = 0x18;
  out_buffer[1]  = pin;
  byte error = writeRegisters(slave_address, 2, true );
  if(!error){
    readRegisters( slave_address, 1 );
    byte res = in_buffer[0];    // = wersja
    if( res == 0 ){
      return false;
    }
    if( res == 0xFF ){
       return true;
    }
    // todo. zwróc true ale raportuj warning
    return true;
  }
  return false;
}

uint16_t i2c_getAnalogValue( byte slave_address, byte pin ){ // Pobierz analogowo wartość PIN o numerze ( 2 bajty )
  out_buffer[0]  = 0x16;
  out_buffer[1]  = pin;
  byte error = writeRegisters( slave_address, 2, true );
  if(!error){
    readRegisters( slave_address, 2 );
    // number: 0xA1 0xB4
    // send order: 0xB4, 0xA1  (little-endian)
    // buffer[0]  = 0xB4
    // buffer[1]  = 0xA1
    uint16_t res = in_buffer[1];
    res = (res<<8) + in_buffer[0];
    return res;
  }
  return 0xFF;
}

void receiveEvent(int howMany){
  Serial.println("receiveEvent");
  while( Wire.available()){ // loop through all but the last  
    char aa = Wire.read(); // receive byte as a character
    printHex(aa);
  }
}

void printHex(byte val){
  int temp =  val;
  Serial.println(temp,HEX);
}


// Funkcja odczytywania N rejestrow
byte readRegisters(byte deviceAddress, byte length){
  Wire.requestFrom(deviceAddress, length);
  byte count = 0;
  byte waits = 100;
  while(Wire.available() == 0 && waits--) {
  }
  if(waits==0){
    Serial.print("niedoczekanie...");
    return 0x32;
  }

  while (Wire.available()){
    byte d = Wire.read();
//    Serial.print("READ:");
//    printHex(d);
    in_buffer[count] = d;//Wire.read();
    count++;
  }
  if( count != length){
    Serial.println("!Odebralem liczb:" + String(count) );
  }
  return count;
}

// wysyla dowolną ilosc liczb na kanal
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
      Serial.println("writeRegisters error: " + String(error));
      delay(100);
      return error;
    }
    if(wait){
      delay(20);
    }
  //  Serial.println("end writeRegisters");
    return 0;
}









/*
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
    Serial.println("-czekam: " + String(waits));
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

  for (counter = 0; counter < length; counter++){
    if (Wire.available ()) {
      in_buffer[counter] = Wire.read();
      Serial.print("Czytam " + String(counter)+ " :" );
      printHex(in_buffer[counter]);
    }else{
      Serial.print("Nie ma wejscia");
      return 0xFE;  // did not get all bytes
    }
  }
  
  return 1;
}


byte readRegisterTemp(int deviceAddress, byte command){
    Wire.beginTransmission(deviceAddress);
    Wire.write(command); // register to read
    int error = Wire.endTransmission();
    delay(10);
    if(error){
      Serial.print("readRegisterTemp error: ");
      printHex(error);
    }
    byte waits = 50;

    Wire.requestFrom(deviceAddress, 1); // read 1 byte
    
    while(Wire.available() == 0 && waits--) {
      Serial.println("-czekam---: " + String(waits));
    }
    return Wire.read();
}


*/
