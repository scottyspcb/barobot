#define IS_MAINBOARD true
#define IS_PROGRAMMER true
#include <WSWire.h>
#include <barobot_common.h>
#include <i2c_helpers.h>


volatile byte in_buffer[5];
volatile byte input_buffer[MAINBOARD_BUFFER_LENGTH][5] = {{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0}};      // 6 buforow po 5 bajtów
volatile byte out_buffer[5];


byte nextpos = 0;
boolean scann_order = false;
byte order[COUNT_UPANEL] = {0,0,0,0,0,0,0,0,0,0,0,0};

byte x = 0;
byte y = 10;

void setup(){
  Serial.begin(115200);
  Wire.begin(I2C_ADR_MAINBOARD);
  Wire.onReceive(receiveEvent);
}

byte pin = 0;
//byte address = 0x04;
byte nDevices=0;

void check_i2c(){  
  Wire.beginTransmission(I2C_ADR_RESERVED);
  byte ee = Wire.endTransmission();     // czy linia jest drozna
  if(ee == 6 ){    // niedrozna - resetuj i2c
    Serial.println("RESET WIRE");
    Wire.begin(I2C_ADR_MAINBOARD);
/*
    for(byte sa = 0x05; sa <= 110; sa++ ) {       // wyslij wszystkim ze reset
      out_buffer[0]  = 0x16;
      writeRegisters(sa, 1, true );
    }*/
    pinMode(PIN_PROGRAMMER_RESET_UPANEL, OUTPUT); 
    digitalWrite(PIN_PROGRAMMER_RESET_UPANEL, LOW);       // pin w stanie niskim
    
    pinMode(PIN_PROGRAMMER_RESET_IPANEL, OUTPUT); 
    digitalWrite(PIN_PROGRAMMER_RESET_IPANEL, LOW);       // pin w stanie niskim

    asm("nop");    // at least 1,5 us
    // end reset = start first slave
    pinMode(PIN_PROGRAMMER_RESET_UPANEL, INPUT);          // set pin to input
    digitalWrite(PIN_PROGRAMMER_RESET_UPANEL, LOW);       // turn OFF pullup resistors
    
    pinMode(PIN_PROGRAMMER_RESET_IPANEL, INPUT);          // set pin to input
    digitalWrite(PIN_PROGRAMMER_RESET_IPANEL, LOW);       // turn OFF pullup resistors 
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
  byte error=0;
  for(byte addr2 = 1; addr2 < 20; addr2++ )   {
    Wire.beginTransmission(addr2);
    error = Wire.endTransmission();
    if (error == 0){
      Serial.print("device found at:");
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
  if(kk>=7){
    get_order();
    kk=0;
  }
  delay(2000);
  for( byte i=0;i<MAINBOARD_BUFFER_LENGTH;i++){
    if( input_buffer[i][0] ){
      proceed( input_buffer[i] );
      input_buffer[i][0] = 0;
    }
  }
}

void scann(){
  byte error;
  int nDevices= 0;
  for(byte aaa = I2C_ADR_MAINBOARD; aaa < 20; aaa++ ) {
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
        Serial.print("- !!! to nie jest urzadzenie i2c: ret 0x");
        printHex( (readed>>8), false );
        printHex( (readed & 0xff) );
      }
    } else if (error==4){
      Serial.print("!!!Unknow error at address 0x");
      printHex(aaa );
    }else{
//      Serial.print("error " +String(error)  +" at address 0x");
//      printHex(aaa);
    }
  }
}

void proceed( volatile byte buffer[5] ){
  if(buffer[0] == 0x21){    // slave input pin value
//    printHex(buffer[1], false );
  String ss = "- IN " + String(buffer[2]) + ": " + String(buffer[3]);
  Serial.println(ss);
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
      Serial.println("Resetuje");

      pinMode(PIN_PROGRAMMER_RESET_UPANEL, OUTPUT); 
      digitalWrite(PIN_PROGRAMMER_RESET_UPANEL, LOW);       // pin w stanie niskim
      asm("nop");    // at least 1,5 us
      // end reset = start first slave
      pinMode(PIN_PROGRAMMER_RESET_UPANEL, INPUT);          // set pin to input
      digitalWrite(PIN_PROGRAMMER_RESET_UPANEL, LOW);       // turn OFF pullup resistors        
      // wait for  here_i_am {0x23,my_address,type,ver}
      nextpos = 0;
      Serial.println("get_order");
      scann_order = true;
}

void i2c_setOutput( byte slave_address, byte new_addr ){			// Zmień adres I2c, musi być podane co najmniej 4 razy zeby zadziałało. (2 bajty)
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
  for( byte a = 0; a < MAINBOARD_BUFFER_LENGTH; a++ ){
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






/*
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


#define HWVER 2
#define SWMAJ 1
#define SWMIN 18
#define EECHUNK (32)

#define PTIME       30
#define STK_OK      0x10
#define STK_FAILED  0x11
#define STK_UNKNOWN 0x12
#define STK_INSYNC  0x14
#define STK_NOSYNC  0x15
#define CRC_EOP     0x20
#define beget16(addr) (*addr * 256 + *(addr+1) )

typedef struct param {
  uint8_t devicecode;
  uint8_t revision;
  uint8_t progtype;
  uint8_t parmode;
  uint8_t polling;
  uint8_t selftimed;
  uint8_t lockbytes;
  uint8_t fusebytes;
  int flashpoll;
  int eeprompoll;
  int pagesize;
  int eepromsize;
  int flashsize;
} parameter;

parameter param;
int error=0;
int pmode=0;
// address for reading and writing, set by 'U' command
int here;
uint8_t buff[256]; // global block storage
uint8_t hbval=128;
int8_t hbdelta=8;



void pulse(int pin, int times) {
  do {
    digitalWrite(pin, HIGH);
    delay(PTIME);
    digitalWrite(pin, LOW);
    delay(PTIME);
  } 
  while (times--);
}
void heartbeat() {    // this provides a heartbeat on pin 9, so you can tell the software is running.
  if (hbval > 192) hbdelta = -hbdelta;
  if (hbval < 32) hbdelta = -hbdelta;
  hbval += hbdelta;
  analogWrite(PIN_PROGRAMMER_LED_STATE, hbval);
  delay(20);
}

void start_isp() {
  Serial.begin(19200);

  pinMode(PIN_PROGRAMMER_LED_ACTIVE, OUTPUT);
  pinMode(PIN_PROGRAMMER_LED_ERROR, OUTPUT);  
  pinMode(PIN_PROGRAMMER_LED_STATE, OUTPUT);
 
  pulse(PIN_PROGRAMMER_LED_ACTIVE, 2);
  pulse(PIN_PROGRAMMER_LED_ERROR, 2);
  pulse(PIN_PROGRAMMER_LED_STATE, 2);
  
  while(true){
    loop_isp();
  }
}

void loop_isp() {
  // is pmode active?
  if (pmode) {
    digitalWrite(PIN_PROGRAMMER_LED_ACTIVE, HIGH); 
  }
  else digitalWrite(PIN_PROGRAMMER_LED_ACTIVE, LOW);
  // is there an error?
  if (error){
    digitalWrite(PIN_PROGRAMMER_LED_ERROR, HIGH); 
  }
  else {
    digitalWrite(PIN_PROGRAMMER_LED_ERROR, LOW);
  }

  // light the heartbeat LED
  heartbeat();
  if (Serial.available()) {
    simulateisp();
  }
}

uint8_t getch() {
  while(!Serial.available());
  return Serial.read();
}
void fill(int n) {
  for (int x = 0; x < n; x++) {
    buff[x] = getch();
  }
}

void prog_lamp(int state) {
  if (PROG_FLICKER)
    digitalWrite(PIN_PROGRAMMER_LED_ACTIVE, state);
}

void spi_init() {
  uint8_t x;
  SPCR = 0x53;
  x=SPSR;
  x=SPDR;
}

void spi_wait() {
  do {
  } while (!(SPSR & (1 << SPIF)));
}

uint8_t spi_send(uint8_t b) {
  uint8_t reply;
  SPDR=b;
  spi_wait();
  reply = SPDR;
  return reply;
}

uint8_t spi_transaction(uint8_t a, uint8_t b, uint8_t c, uint8_t d) {
  uint8_t n;
  spi_send(a); 
  n=spi_send(b);
  //if (n != a) error = -1;
  n=spi_send(c);
  return spi_send(d);
}

void empty_reply() {
  if (CRC_EOP == getch()) {
    Serial.print((char)STK_INSYNC);
    Serial.print((char)STK_OK);
  } else {
    error++;
    Serial.print((char)STK_NOSYNC);
  }
}

void breply(uint8_t b) {
  if (CRC_EOP == getch()) {
    Serial.print((char)STK_INSYNC);
    Serial.print((char)b);
    Serial.print((char)STK_OK);
  } else {
    error++;
    Serial.print((char)STK_NOSYNC);
  }
}

void get_version(uint8_t c) {
  switch(c) {
  case 0x80:
    breply(HWVER);
    break;
  case 0x81:
    breply(SWMAJ);
    break;
  case 0x82:
    breply(SWMIN);
    break;
  case 0x93:
    breply('S'); // serial programmer
    break;
  default:
    breply(0);
  }
}

void set_parameters() {
  // call this after reading paramter packet into buff[]
  param.devicecode = buff[0];
  param.revision   = buff[1];
  param.progtype   = buff[2];
  param.parmode    = buff[3];
  param.polling    = buff[4];
  param.selftimed  = buff[5];
  param.lockbytes  = buff[6];
  param.fusebytes  = buff[7];
  param.flashpoll  = buff[8]; 
  // ignore buff[9] (= buff[8])
  // following are 16 bits (big endian)
  param.eeprompoll = beget16(&buff[10]);
  param.pagesize   = beget16(&buff[12]);
  param.eepromsize = beget16(&buff[14]);

  // 32 bits flashsize (big endian)
  param.flashsize = buff[16] * 0x01000000+ buff[17] * 0x00010000 + buff[18] * 0x00000100 + buff[19];
}

void start_pmode() {
  spi_init();
  // following delays may not work on all targets...
  byte reset_pin  = PIN_PROGRAMMER_RESET_UPANEL;
  pinMode(reset_pin, OUTPUT);
  digitalWrite(reset_pin, HIGH);
  pinMode(SCK, OUTPUT);
  digitalWrite(SCK, LOW);
  delay(50);
  digitalWrite(reset_pin, LOW);
  delay(50);
  pinMode(MISO, INPUT);
  pinMode(MOSI, OUTPUT);
  spi_transaction(0xAC, 0x53, 0x00, 0x00);
  pmode = 1;
}

void end_pmode() {
  pinMode(MISO, INPUT);
  pinMode(MOSI, INPUT);
  pinMode(SCK, INPUT);
  byte reset_pin  = PIN_PROGRAMMER_RESET_UPANEL;
  pinMode(reset_pin, INPUT);    // ten jako ostati
  pmode = 0;
}

void universal() {
  int w;
  uint8_t ch;
  fill(4);
  ch = spi_transaction(buff[0], buff[1], buff[2], buff[3]);
  breply(ch);
}

void flash(uint8_t hilo, int addr, uint8_t data) {
  spi_transaction(0x40+8*hilo, 
  addr>>8 & 0xFF, 
  addr & 0xFF,
  data);
}
void commit(int addr) {
  if (PROG_FLICKER) prog_lamp(LOW);
  spi_transaction(0x4C, (addr >> 8) & 0xFF, addr & 0xFF, 0);
  if (PROG_FLICKER) {
    delay(PTIME);
    prog_lamp(HIGH);
  }
}

//#define _current_page(x) (here & 0xFFFFE0)
int current_page(int addr) {
  if (param.pagesize == 32)  return here & 0xFFFFFFF0;
  if (param.pagesize == 64)  return here & 0xFFFFFFE0;
  if (param.pagesize == 128) return here & 0xFFFFFFC0;
  if (param.pagesize == 256) return here & 0xFFFFFF80;
  return here;
}

void write_flash(int length) {
  fill(length);
  if (CRC_EOP == getch()) {
    Serial.print((char) STK_INSYNC);
    Serial.print((char) write_flash_pages(length));
  }else {
    error++;
    Serial.print((char) STK_NOSYNC);
  }
}

uint8_t write_flash_pages(int length) {
  int x = 0;
  int page = current_page(here);
  while (x < length) {
    if (page != current_page(here)) {
      commit(page);
      page = current_page(here);
    }
    flash(LOW, here, buff[x++]);
    flash(HIGH, here, buff[x++]);
    here++;
  }

  commit(page);
  return STK_OK;
}

void program_page() {
  char result = (char) STK_FAILED;
  int length = 256 * getch();
  length += getch();
  char memtype = getch();
  // flash memory @here, (length) bytes
  if (memtype == 'F') {
    write_flash(length);
    return;
  }
  if (memtype == 'E') {
    result = STK_OK;
    if (CRC_EOP == getch()) {
      Serial.print((char) STK_INSYNC);
      Serial.print(result);
    } else {
      error++;
      Serial.print((char) STK_NOSYNC);
    }
    return;
  }
  Serial.print((char)STK_FAILED);
  return;
}

uint8_t flash_read(uint8_t hilo, int addr) {
  return spi_transaction(0x20 + hilo * 8,
  (addr >> 8) & 0xFF,
  addr & 0xFF,
  0);
}

char flash_read_page(int length) {
  for (int x = 0; x < length; x+=2) {
    uint8_t low = flash_read(LOW, here);
    Serial.print((char) low);
    uint8_t high = flash_read(HIGH, here);
    Serial.print((char) high);
    here++;
  }
  return STK_OK;
}

void read_page() {
  char result = (char)STK_FAILED;
  int length = 256 * getch();
  length += getch();
  char memtype = getch();
  if (CRC_EOP != getch()) {
    error++;
    Serial.print((char) STK_NOSYNC);
    return;
  }
  Serial.print((char) STK_INSYNC);
  if (memtype == 'F') {
    result = flash_read_page(length);
  }

  Serial.print(result);
  return;
}

void read_signature() {
  if (CRC_EOP != getch()) {
    error++;
    Serial.print((char) STK_NOSYNC);
    return;
  }
  Serial.print((char) STK_INSYNC);
  uint8_t high = spi_transaction(0x30, 0x00, 0x00, 0x00);
  Serial.print((char) high);
  uint8_t middle = spi_transaction(0x30, 0x00, 0x01, 0x00);
  Serial.print((char) middle);
  uint8_t low = spi_transaction(0x30, 0x00, 0x02, 0x00);
  Serial.print((char) low);
  Serial.print((char) STK_OK);
}

int simulateisp() { 
  uint8_t data, low, high;
  uint8_t ch = getch();
  switch (ch) {
  case '0': // signon
    error = 0;
    empty_reply();
    break;
  case '1':
    if (getch() == CRC_EOP) {
      Serial.print((char) STK_INSYNC);
      Serial.print("AVR ISP");
      Serial.print((char) STK_OK);
    }
    break;
  case 'A':
    get_version(getch());
    break;
  case 'B':
    fill(20);
    set_parameters();
    empty_reply();
    break;
  case 'E': // extended parameters - ignore for now
    fill(5);
    empty_reply();
    break;
  case 'P':
    start_pmode();
    empty_reply();
    break;
  case 'U': // set address (word)
    here = getch();
    here += 256 * getch();
    empty_reply();
    break;
  case 'Q': //0x51
    error=0;
    end_pmode();
    empty_reply();
    break;
  case 'V': //0x56
    universal();
    break;
  case 0x60: //STK_PROG_FLASH
    low = getch();
    high = getch();
    empty_reply();
    break;
  case 0x61: //STK_PROG_DATA
    data = getch();
    empty_reply();
    break;
  case 0x64: //STK_PROG_PAGE
    program_page();
    break;
  case 0x74: //STK_READ_PAGE 't'
    read_page();    
    break;
  case 0x75: //STK_READ_SIGN 'u'
    read_signature();
    break;
    // expecting a command, not CRC_EOP
    // this is how we can get back in sync
  case CRC_EOP:
    error++;
    Serial.print((char) STK_NOSYNC);
    break;
  default:
    error++;
    if (CRC_EOP == getch()) 
      Serial.print((char)STK_UNKNOWN);
    else
      Serial.print((char)STK_NOSYNC);
  }
}


