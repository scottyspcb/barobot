#define IS_MAINBOARD true
#include <WSWire.h>
#include <barobot_common.h>

int led = 13;

#define BUFFER_LENGTH 10
volatile byte in_buffer[5];
volatile byte input_buffer[BUFFER_LENGTH][5] = {{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0}};      // 6 buforow po 5 bajtów
volatile byte out_buffer[5];
volatile boolean was_event = false;

byte nextpos = 0;
boolean scann_order = false;
byte order[COUNT_UPANEL] = {0,0,0,0,0,0,0,0,0,0,0};

byte x = 0;
byte y = 10;

void setup(){
  Serial.begin(115200);
  Wire.begin(I2C_ADR_MAINBOARD);
  Wire.onReceive(receiveEvent);
  pinMode(led, OUTPUT);  
}

byte pin = 0;
//byte address = 0x04;
byte nDevices=0;
byte error=0;

void check_i2c(){  
  Wire.beginTransmission(I2C_ADR_RESERVED);
  byte ee = Wire.endTransmission();     // czy linia jest drozna
  if(ee == 6 ){    // niedrozna - resetuj i2c
    Serial.println("RESET WIRE");
    Wire.begin(I2C_ADR_MAINBOARD);
    for(byte sa = 0x05; sa <= 110; sa++ ) {       // wyslij wszystkim ze reset
      out_buffer[0]  = 0x16;
      writeRegisters(sa, 1, true );
    }
    pinMode(PIN_MAINBOARD_LEFT_RESET, OUTPUT); 
    digitalWrite(PIN_MAINBOARD_LEFT_RESET, LOW);       // pin w stanie niskim
    asm("nop");    // at least 1,5 us
    // end reset = start first slave
    pinMode(PIN_MAINBOARD_LEFT_RESET, INPUT);          // set pin to input
    digitalWrite(PIN_MAINBOARD_LEFT_RESET, LOW);       // turn OFF pullup resistors
  }
}
int kk  =1;
void loop(){
  Serial.println("----------LOOP------");
  check_i2c();
  x = x + 30;
  if( x == 0 ){
     x = 100;
  }
  pin++;
  if( pin > 8 ){
      pin = 0;
  }
  nDevices = 0;
  for(byte addr2 = 1; addr2 < 20; addr2++ )   {
    Wire.beginTransmission(addr2);
    error = Wire.endTransmission();
    if (error == 0){
      Serial.print("I2C device found at address: ");
      printHex(addr2, false );
      uint16_t readed = i2c_getVersion(addr2);
      Serial.print(" type: ");
      printHex( readed>>8, false );
      Serial.print(" ver: ");
      printHex( readed & 0xff, false );
      Serial.print(" pos: ");
      byte pos = getOrder(addr2);
      printHex( pos, false );   
      Serial.println("");
      nDevices++;
    }else{
     Serial.println("RET: "+String(addr2)+" / "+String(error));
    }
  }
  Serial.println("Devices:" + String(nDevices));
  scann();
  kk++;
  if(kk>=5){
    get_order();
    kk=0;
  }
  digitalWrite(led, HIGH); 
  delay(100);
  digitalWrite(led, LOW);
  delay(100);
  for( byte i=0;i<BUFFER_LENGTH;i++){
    if( input_buffer[i][0] ){
      proceed( input_buffer[i] );
      input_buffer[i][0] = 0;
    }
  }
}

void scann(){
  byte error;
  int nDevices= 0;

  for(byte aaa = 1; aaa < 127; aaa++ ) {
    Wire.beginTransmission(aaa);
    error = Wire.endTransmission();
    if (error == 0){
      uint16_t readed = i2c_getVersion(aaa);
      if( (readed>>8) > 0 && (readed & 0xff >0)){
        i2c_setPWM( aaa, pin, x );
        unsigned int errors= test_slave( aaa );
        nDevices++;
      }else{
        printHex( aaa, false );
        Serial.println("- !!! to nie jest urzadzenie i2c");
      }
    } else if (error==4){
      Serial.print("!!!Unknow error at address 0x");
      printHex(aaa );
    }else{
    //  Serial.print("no dev at address 0x");
    //  printHex(aaa );
    //  printHex(error );
    }
  }
}



void proceed( volatile byte buffer[5] ){
  if(buffer[0] == 0x21){    // slave input pin value
//    printHex(buffer[1], false );
  //  String ss = "- IN " + String(buffer[2]) + ": " + String(buffer[3]);
  //  Serial.println(ss);
  }else if(buffer[0] == 0x22){    // poke - wcisnieto przycisk
  }else if(buffer[0] == 0x23){    // here_i_am
    // sprawdz czy ten adres nie jest uzywany
    boolean used = false;
    if(scann_order){
      byte pos = getOrder(buffer[1]);
      if( pos !=0xFF || (nextpos >= COUNT_UPANEL) ){
        scann_order  =  false;
        Serial.println("koniec order");
      }else{
        printHex( buffer[1], false );
        Serial.println("- na pozycji " + String(nextpos) + "- resetuje nastepny");
        order[nextpos++]  = buffer[1];     // na tm miejscu slave o tym adresie
        i2c_reset_next( buffer[1] );       // reset next (next to slave)
        i2c_run_next( buffer[1] );
      }
    }
  }else{
    Serial.println("recieve unknown - ");
    printHex(buffer[0]);
  }
  buffer[0] = 0;  //ready
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
void i2c_reset_next( byte slave_address ){			            // Resetuj urządzenie obok urządzenia adresowego, stan niski na wyjściu resetuje tego obok
  out_buffer[0]  = 0x16;
  writeRegisters(slave_address, 1, true );
}
void i2c_run_next( byte slave_address ){			            // Koniec resetu urządzenia obok urządzenia adresowego, stan wysokiej impedancji na wyjściu
  out_buffer[0]  = 0x17;
  writeRegisters(slave_address, 1, true );
}

void get_order(){      // pobierz kolejnosc elementów
      pinMode(PIN_MAINBOARD_LEFT_RESET, OUTPUT); 
      digitalWrite(PIN_MAINBOARD_LEFT_RESET, LOW);       // pin w stanie niskim
      asm("nop");    // at least 1,5 us
      asm("nop");    // at least 1,5 us 
      // end reset = start first slave
      pinMode(PIN_MAINBOARD_LEFT_RESET, INPUT);          // set pin to input
      digitalWrite(PIN_MAINBOARD_LEFT_RESET, LOW);       // turn OFF pullup resistors        
      // wait for  here_i_am {0x23,my_address}
      nextpos = 0;
      Serial.println("get_order");
      scann_order = true;
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
  out_buffer[0]  = 0x29;
  byte error = writeRegisters(slave_address, 1, true );
  if( error ){
   Serial.println("!!!i2c_getVersion error1");  
  }
  if( !error ){
    readRegisters( slave_address, 2 );
    uint16_t res = in_buffer[1];    // = wersja
    res = (res<<8) | in_buffer[0];    // = typ
    return res;
  }
  if( error ){
   Serial.println("!!!i2c_getVersion error1");  
  }
  return 0xFF;
}

void i2c_stop(  byte slave_address ){      // zgaś wszystko
  out_buffer[0]  = 0xEE;
  writeRegisters(slave_address, 1, true );
  writeRegisters(slave_address, 1, true );
}


byte i2c_test_slave( byte slave_address, byte num1, byte num2 ){      // testuj
  out_buffer[0]  = 0x2A;
  out_buffer[1]  = num1;
  out_buffer[2]  = num2;  
  byte error = writeRegisters(slave_address, 3, true );
  if( error != 0 ){
    Serial.println("!!!Test error1" + String(error));  
  }
  if( error == 0 ){
    readRegisters( slave_address, 1 );
    return in_buffer[0];
  }
  if( error ){
   Serial.println("!!!Test error2");  
  }
  return 0xFF;
}

unsigned int test_slave(byte slave_address){
//  printHex( slave_address, false );
//  Serial.println("- Test_slave start" );
  byte cntr1 = 5;
  const byte c2_max = 10;
  unsigned int cc = cntr1 * c2_max;
  byte res = 0;
  unsigned int errors= 0;
  while(--cntr1){
    byte cntr2 = c2_max;  
    while(--cntr2){
      res = i2c_test_slave(slave_address, cntr1, cntr2);
      byte valid = cntr1 ^ cntr2;
      if(res !=valid){
        errors++;
        printHex( slave_address, false );
        Serial.println("- !!! zle "+ String(res) + " != " + String( valid ) );
      }
   //  delay(10);
    }    
  }
  printHex( slave_address, false );
  Serial.println("- Test_slave (" + String(cc) + "): " + String(errors));
  return errors;
}

boolean i2c_getValue(  byte slave_address, byte pin ){      // zwraca 2 bajty. typ na młodszych bitach, versja na starszych
  out_buffer[0]  = 0x28;
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
  out_buffer[0]  = 0x26;
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
  if(!howMany){
     return;
  }
  byte cnt = 0;
  volatile byte (*buffer) = 0;
  Serial.print("input " );
  for( byte a = 0; a < BUFFER_LENGTH; a++ ){
    if(input_buffer[a][0] == 0 ){
      buffer = (&input_buffer[a][0]); 
      while( Wire.available()){ // loop through all but the last
        byte w =  Wire.read(); // receive byte as a character
        *(buffer +(cnt++)) = w;
        printHex(w, false ); 
      }
      Serial.println(""); 
      return;
    }
  }
  Serial.println(" - pelno"); 
}

byte getOrder( byte address ){
    for(byte i =0; i<nextpos;i++){
      if(order[i] == address){      // powtórzono
        return i;
      }
    }
    return 0xFF;
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
    Serial.println("!!!Odebralem liczb:" + String(count) );
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
      Serial.println("!!! writeRegisters error: " + String(error));
      delay(100);
      return error;
    }
    if(wait){
      delay(20);
    }
  //  Serial.println("end writeRegisters");
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
