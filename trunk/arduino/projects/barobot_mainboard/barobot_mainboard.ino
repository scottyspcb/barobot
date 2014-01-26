//#include "barobot_mainboard_main.h"

#define IS_MAINBOARD true
#define IS_PROGRAMMER true
#include <WSWire.h>
#include <barobot_common.h>
#include <i2c_helpers.h>
#include <AccelStepper.h>

#include <avr/io.h>
#include <avr/wdt.h>
byte in_buffer[7];
volatile uint8_t input_buffer[MAINBOARD_BUFFER_LENGTH][7] = {{0,0,0,0,0,0,0},{0,0,0,0,0,0,0},{0,0,0,0,0,0,0}};
volatile uint8_t buff_length[MAINBOARD_BUFFER_LENGTH] = {0,0,0};

void (*spi_init)();
uint8_t (*spi_send)(uint8_t);

byte out_buffer[7];
uint8_t serialBuff[130];
uint8_t serialBuff_pos   = 0;

boolean error=0;
volatile boolean stepper_now = false;
int here;
uint8_t hbval            = 128;
byte nextpos             = 0;
boolean scann_order      = false;
byte order[COUNT_UPANEL] = {0,0,0,0,0,0,0,0,0,0,0,0};


boolean prog_mode        = false;
String serial0Buffer     = "";
boolean Console0Complete = false;   // This will be set to true once we have a full string
boolean last_i2c_read_error = false;

byte reprogramm_index = 0;
long unsigned milis100 = 0;
boolean diddd = false;

/*
PROG, RESET  - programm specific device by i2c address
PROG 0       - first upanel
PROG 0x0A    - carret
PROG 0x0E    - upanel...
PROG 0xFF    - after last known device

PROGN, RESETN    - programm after number

00  reset by master     reset master
01  reset by master     programm carret

05  reset by master     programm upanel 0 (index 0)
06  reset by upanel-0   programm upanel 1 (index 1)
07  reset by upanel-1   programm upanel 2 (index 2)
08  reset by upanel-2   programm upanel 3 (index 3)
09  reset by upanel-3   programm upanel 4 (index 4)
0A  reset by upanel-4   programm upanel 5 (index 5)
0B  reset by upanel-5   programm upanel 6 (index 6)
0C  reset by upanel-6   programm upanel 7 (index 7)
0D  reset by upanel-7   programm upanel 8 (index 8)
0E  reset by upanel-8   programm upanel 9 (index 9)
0F  reset by upanel-9   programm upanel 10 (index 10)
10  reset by upanel-10  programm upanel 11 (index 11)
...

SPECIAL
fe  send to last known device
ff  send to device AFTER last known device
*/
void disableWd(){
    // Disable the WDT
//    WDTCSR |= _BV(WDCE) | _BV(WDE); 
//    WDTCSR = 0;
      wdt_disable();

}
void setup(){
  DEBUGINIT();
  DEBUGLN("-MASTER START");
  disableWd();


  pinMode(PIN_PROGRAMMER_LED_ACTIVE, OUTPUT);
  pinMode(PIN_PROGRAMMER_LED_ERROR, OUTPUT);  
  pinMode(PIN_PROGRAMMER_LED_STATE, OUTPUT);

  pinMode(PIN_MAINBOARD_SCK, INPUT );
  pinMode(PIN_MAINBOARD_MISO, INPUT );
  pinMode(PIN_MAINBOARD_MOSI, INPUT );

  digitalWrite(PIN_PROGRAMMER_LED_ACTIVE, LOW);
  digitalWrite(PIN_PROGRAMMER_LED_ERROR, LOW);
  digitalWrite(PIN_PROGRAMMER_LED_STATE, LOW);

  Wire.begin(I2C_ADR_MAINBOARD);
  digitalWrite(SDA, 1);    // disable pullups
  digitalWrite(SCL, 1);    // disable pullups
  Wire.onReceive(receiveEvent);  
  setupStepper();
  send2android("RREBOOT");
  send2androidEnd();
}

#if MAINBOARD_SERVO_4PIN==true
AccelStepper stepperX(8, PIN_MAINBOARD_STEPPER_STEP0, PIN_MAINBOARD_STEPPER_STEP1, PIN_MAINBOARD_STEPPER_STEP2, PIN_MAINBOARD_STEPPER_STEP3 );
#else
AccelStepper stepperX(1, PIN_MAINBOARD_STEPPER_STEP, PIN_MAINBOARD_STEPPER_DIR);      // Step, DIR
#endif

void setupStepper(){
  stepperX.disable_on_ready = true;
  stepperX.setEnablePin(PIN_MAINBOARD_STEPPER_ENABLE);
  stepperX.setPinsInverted( false, false, true ); // enable pin invert
  stepperX.setAcceleration(MAINBOARD_ACCELERX); 
  stepperX.setMaxSpeed(MAINBOARD_SPEEDX);  

  pinMode(PIN_MAINBOARD_STEPPER_MS1, OUTPUT);
  pinMode(PIN_MAINBOARD_STEPPER_MS2, OUTPUT);
  pinMode(PIN_MAINBOARD_STEPPER_MS3, OUTPUT);
  digitalWrite(PIN_MAINBOARD_STEPPER_MS1, HIGH );
  digitalWrite(PIN_MAINBOARD_STEPPER_MS2, HIGH );
  digitalWrite(PIN_MAINBOARD_STEPPER_MS3, LOW );

  milis100 = millis() + 3000;    // za 3 sek zaczij sprawdzanie magistrali
}

void timer(){
  stepper_now = true;
}

void loop(){
        long unsigned mil = millis();
  	if( mil > milis100 ){    // co 7 sek
                check_i2c();    // czy linia jest drozna?
                hbval++;
                if(hbval>=10){
              //  2c_analog(I2C_ADR_IPANEL, 0);
                  hbval=0;
                }
  		milis100 = mil + 12000;
                DEBUG("-HELLO android ");
                DEBUGLN(String(mil));
  	}
		if (Console0Complete) {
			parseInput( serial0Buffer );				      // parsuj wejscie
			Console0Complete = false;
                        serialBuff_pos = 0;
			serial0Buffer = "";
		}
    for( byte i=0;i<MAINBOARD_BUFFER_LENGTH;i++){
      stepperX.run();
      if( input_buffer[i][0] ){
        proceed( buff_length[i],input_buffer[i] );
        input_buffer[i][0] = 0;
      }
    }
    stepperX.run();
}
/*
void scann_i2c(){
  byte nDevices=0;
  byte error=0;
  for(byte addr2 = 1; addr2 < 20; addr2++ )   {
    stepperX.run();
    Wire.beginTransmission(addr2);
    error = Wire.endTransmission();
    if (error == 0){
      DEBUG("-dev @");
      printHex(addr2, false );
      uint16_t readed = i2c_getVersion(addr2);
      DEBUG(" type: ");
      printHex( readed>>8, false );        // starsze bity = typ
      DEBUG(" ver: ");
      printHex( readed & 0xff, false );    // mĹ‚odsze bity = ver
      DEBUG(" pos: ");
      byte pos = getResetOrder(addr2);
      printHex( pos, false );   
      DEBUGLN("");
      nDevices++;
    }else{
//     DEBUGLN("RET: "+String(addr2)+" / "+String(error));
    }
  }
}*/

void check_i2c(){
  Wire.beginTransmission(I2C_ADR_RESERVED);
  byte ee = Wire.endTransmission();     // czy linia jest drozna
  if(ee == 6 ){    // niedrozna - resetuj i2c
    sendln2android("RWIRE");
//    pinMode(PIN_MAINBOARD_SDA, INPUT );
//    pinMode(PIN_MAINBOARD_SCL, INPUT );
    Wire.begin(I2C_ADR_MAINBOARD);
    tri_state( PIN_PROGRAMMER_RESET_IPANEL, false );       // pin w stanie niskim = reset
    tri_state( PIN_PROGRAMMER_RESET_UPANEL, false );       // pin w stanie niskim = reset
    tri_state( PIN_PROGRAMMER_RESET_IPANEL, true );       // pin w stanie niskim = reset
    tri_state( PIN_PROGRAMMER_RESET_UPANEL, true );       // pin w stanie niskim = reset
  }
}

void proceed( byte length,volatile uint8_t buffer[7] ){ // zrozum co przyszlo po i2c
  if(prog_mode){
    return;
  }
  if(buffer[0] == METHOD_HERE_I_AM){         //  here_i_am {METHOD_HERE_I_AM,my_address,type,ver}
    byte pos = getResetOrder(buffer[1]);
    if( buffer[2] == IPANEL_DEVICE_TYPE ){
        DEBUGLN("-Wozek! ");  
    }else if( scann_order ){


        if( pos == 0xff ){        // nie ma na liscie?
   //       DEBUGLN("-Upanel pod " + String(nextpos));
          order[nextpos++]  = buffer[1];            // na tm miejscu slave o tym adresie
          i2c_reset_next( buffer[1], false );       // reset next (next to slave)
          i2c_reset_next( buffer[1], true );
          pos = getResetOrder(buffer[1]);
      }else{    //      if(nextpos >= COUNT_UPANEL ){


          scann_order  =  false;
          DEBUGLN("-koniec order");
      }
    }
    i2c_device_found( buffer[1], buffer[2], buffer[3], pos );

  }else if(buffer[0] == METHOD_IMPORTANT_ANALOG){      // wyslij do androida pozycje bo trafiono na gĂłrkÄ™ hallem
   
/*
    byte ttt[5] = {
      METHOD_IMPORTANT_ANALOG,
      buffer[1],                        // analog num
      buffer[2],                        // reason
      buffer[2],                        // pos low byte
      buffer[2]                         // pos high byte
    };
    send2android(ttt,5);
    send2androidEnd();*/

  }else if(buffer[0] == METHOD_I2C_SLAVEMSG){      // wyslij do androida
        if( buffer[1] == RETURN_DRIVER_READY ){
          uint16_t pos = buffer[4];     // little endian
          pos = (pos<<8) | buffer[3];
          if( buffer[2] == DRIVER_Y){
            send2android("Ry");
          }else if( buffer[2] == DRIVER_Z){
            send2android("Rz");
          }
          send2android( String(pos) );
          send2androidEnd();
          /*
        }else if( buffer[1] == RETURN_DRIVER_ERROR ){
          uint16_t pos = buffer[4];     // little endian
          pos = (pos<<8) | buffer[3];
          if( buffer[2] == DRIVER_Y){
            send2android("EY");
          }else if( buffer[2] == DRIVER_Z){
            send2android("EZ");
          }
          send2android( String(pos));
          send2androidEnd();*/
        }else{
          byte b2[length];
          DEBUG("-METHOD_I2C_SLAVEMSG ");
          for(byte i=0;i<length;i++){
            b2[i] = buffer[i];
            DEBUG(" ");
            printHex(b2[i], false);
          }
          DEBUGLN(" "); 
          send2android(b2, length);
          send2androidEnd(); 
        } 
  }else{
    DEBUG("- METHOD_I2C_SLAVEMSG ");
    DEBUG(buffer[0]);
    DEBUG(" ");
    DEBUG(buffer[1]);
    DEBUG(" ");
    DEBUG(buffer[2]);
    DEBUG(" ");
    DEBUG(buffer[3]);
    DEBUG(" ");
    DEBUG(buffer[4]);
    DEBUG(" ");
    DEBUGLN(buffer[5]);
  }
  buffer[0] = 0;  //ready
}

void i2c_device_found( byte addr,byte type,byte ver, byte pos ){
 // byte ttt[5] = {METHOD_DEVICE_FOUND,addr,type,ver,pos};
//  send2android(ttt,5);
//  send2androidEnd();
}

void parseInput( String input ){   // zrozum co przyszlo po serialu
	input.trim();
	boolean defaultResult = true;
        byte command = serialBuff[0];
//        byte command2 = serialBuff[1];
	if( command == METHOD_SEND2SLAVE ){    // wyĹ›lij przez i2c do slave i spodziewaj siÄ™ wyniku
      //    DEBUGLN("-A2S");
          // np 0x14 0x0A 0x02 0x11 0x22 0x33            // wyĹ›lij po i2c do slave numer 0x0A bajty: 0x11 0x22 0x33  i spodziewaj siÄ™ 2 bajtĂłw wyniku
          byte count          = input.length() - 3;      // tyle do wysĹ‚ania
          byte slave_address  = input.charAt( 1 );       // 0 = command, 1 = address, 2 = needs
          byte needs          = input.charAt( 2 );
          if( count > 0){
            for(byte i =0; i<count;i++){
              out_buffer[i]      = input.charAt( i + 3 );      // piewszy bajt komendy jest w czwartym bajcie (na pozycji 3 liczÄ…c od 0)
            }
            if(slave_address == I2C_ADR_MAINBOARD ){          // to jest komunikat do mnie?
              proceed( count,out_buffer );                    // analizuj komende i wyĹ›lij odpowiedĹş
            }else{
              byte error = writeRegisters(slave_address, count, true );
              if( error ){
                DEBUGLN("-!A2S"); 
                byte ttt[3] = {METHOD_I2C_ERROR, slave_address, error};              // ERROR command
                send2android(ttt,3);
                send2androidEnd();
              }else{
                if( needs > 0){                // moge chcieÄ‡ 0 bajtĂłw
                  readRegisters( slave_address, needs );
                  // wynik w in_buffer;
                  byte ttt[2] = {METHOD_RET_FROM_SLAVE,slave_address}; 
                  send2android(ttt,2);
                  send2android(in_buffer,needs);
                  send2androidEnd();
                }else{
                  //koniec komendy
                }
              }
            }
          }else{  // nic nie wysyĹ‚aj bo nie ma co
            // wrong_call++
          }
//	}else if( input.charAt( 0 ) == 0x11 ){      // wyĹ›lij przez i2c do slave
          // np 0x11 0x0A 0x11 0x22 0x33      // wyĹ›lij po i2c do slave numer 0x0A bajty: 0x11 0x22 0x33

	}else if( input.startsWith("PROG ")) {    // PROG 0A,1    // PROG 0A,0   - programuj urzadzenie 0x0A z prÄ™dkosca 19200, PROG 0,0 - force first, PROG 0A,0 - wozek
                read_prog_settings(input, 1);
                defaultResult = false;
                return;
	}else if( input.startsWith("PROGN ")) {    // PROGN 0,0,0   - programuj urzadzenie podĹ‚Ä…czone do 0x0C (number 0 )
                read_prog_settings(input, 2 );
                defaultResult = false;
                return;
	}else if( input.startsWith("RESET ")) {    // RESET 0A - resetuj urzadzenie 0x0A
                String digits     = input.substring( 6 );
                char charBuf[20];
                digits.toCharArray(charBuf,20);
                unsigned int i2c_address    = 0x00;
                sscanf(charBuf,"%x", &i2c_address );
                byte num = getResetOrder(i2c_address);
                delay(200);
                boolean ret = reset_device_num(num, LOW);
                if(ret){
                  delay(1000);
                  reset_device_num(num, HIGH);
                }else{  //error
                  defaultResult = false;
					sendln2android("E " + input );
                }
	}else if( input.startsWith("RESETN ")) {    // RESETN n - resetuj upanel pod numerem n, 0 - pierwszy, ff- po ostatnim wykrytym (np gdy nie da siÄ™ wykryÄ‡)
                String digits     = input.substring( 6 );
                char charBuf[20];
                digits.toCharArray(charBuf,20);
                unsigned int target    = 0x00;
                sscanf(charBuf,"%x", &target );
                delay(200);
                reset_device_num(target, LOW);   
                boolean ret = reset_device_num(target, LOW);
                if(ret){
                  delay(1000);
                  reset_device_num(target, HIGH);
                }else{
                  defaultResult = false;
       		  sendln2android("E " + input );
                }
                
	
	}else{
                // nie rozumiem
		sendln2android("ARDUINO NO COMMAND [" + input +"]");
		defaultResult = false;  
	}
	if(defaultResult ){
		sendln2android("R" + input );
	}
}

long unsigned decodeInt(String input, int odetnij ){
  long pos = 0;
  if(odetnij>0){
    input = input.substring(odetnij);    // obetnij znaki z przodu
  }
  pos = input.toInt();
  return pos;
}

void tri_state( byte pin_num, boolean pin_value ){
    if( pin_value ){
      digitalWrite(pin_num, HIGH);       // HIGH value = run
      pinMode(pin_num, INPUT);
      digitalWrite( pin_num, 1);    // disable pullups
    }else{
      pinMode(pin_num, OUTPUT); 
      digitalWrite(pin_num, LOW);        // LOW value = reset
    }
}







boolean reset_device_num( byte num, boolean pin_value ){
  if( num == 0x00 ){                        // master 
    DEBUGLN("-reset_device_num");
    wdt_enable(WDTO_8S);
    DEBUGLN("-WDTO_8S");
    while(1){
      DEBUGLN("-JADE");
    };    
  }else if( num == 0x01 ){                        // wozek 
    tri_state( PIN_PROGRAMMER_RESET_IPANEL, pin_value );
  }else if( num == 0x05 ){                  // pierwszy upanel
      tri_state( PIN_PROGRAMMER_RESET_UPANEL, pin_value );
  }else if( num == 0xff ){          // reset after last
      if(nextpos == 0 || order[ nextpos - 1] == 0 ){    // nie ma zadnego urzadzenia lub przedostatni nie istenieje
        return false;
      }
      i2c_reset_next( order[ nextpos - 1], pin_value );              // HIGH value = run
  }else{    // other position, num >= 6
      if( num == 0xfe ){          // reset last known
        num = nextpos;
      }
      num = num -6;
      if( nextpos == 0 || num >= nextpos || order[ num ] ==0 ){   // nie ma urzadzenia
        return false;
      }
      i2c_reset_next( order[ num ], pin_value );
  }
  return true;
}

void read_prog_settings( String input, byte ns ){
    String digits         = input.substring( 5 );
    unsigned int target           = 0x00;
    unsigned int slow_sck         = 0;
    unsigned int serial_baud_num  = 0;
    char charBuf[10];
    digits.toCharArray(charBuf, 10);
    sscanf(charBuf,"%x,%x,%i", &target, &serial_baud_num, &slow_sck );
    if(ns == 1){            // PROG - podanu adres, znajdz numer
      reprogramm_index    = getResetOrder(target);
    }else if(ns == 2){     // PROGN
      reprogramm_index    = target;
    }
    DEBUG("-ISP PROG START");
    DEBUGLN(reprogramm_index);
}

void i2c_reset_next( byte slave_address, boolean pin_value ){
  if( pin_value ){                  // Koniec resetu urzÄ…dzenia obok urzÄ…dzenia adresowego, stan wysokiej impedancji na wyjĹ›ciu
    out_buffer[0]  = METHOD_RUN_NEXT;
    writeRegisters(slave_address, 1, true );  
  }else{	                    // Resetuj urzÄ…dzenie obok urzÄ…dzenia adresowego, stan niski na wyjĹ›ciu resetuje tego obok
    out_buffer[0]  = METHOD_RESET_NEXT;
    writeRegisters(slave_address, 1, true );
  }
}



void get_order(){      // pobierz kolejnosc elementĂłw
      DEBUGLN("-Resetuje");
      pinMode(PIN_PROGRAMMER_RESET_UPANEL, OUTPUT); 
      digitalWrite(PIN_PROGRAMMER_RESET_UPANEL, LOW);
      tri_state( PIN_PROGRAMMER_RESET_IPANEL, false );       // pin w stanie niskim = reset
      tri_state( PIN_PROGRAMMER_RESET_IPANEL, true );        // end reset = start first slave
      tri_state( PIN_PROGRAMMER_RESET_UPANEL, false );       // pin w stanie niskim = reset
      tri_state( PIN_PROGRAMMER_RESET_UPANEL, true );        // end reset = start first slave
      nextpos = 0;
      scann_order = true;
}

void i2c_stop( byte slave_address ){      // zgaĹ› wszystko
  out_buffer[0]  = 0xEE;
  writeRegisters(slave_address, 1, true );
  writeRegisters(slave_address, 1, true );
}



uint16_t i2c_getAnalogValue( byte slave_address, byte pin ){ // Pobierz analogowo wartoĹ›Ä‡ PIN o numerze ( 2 bajty )
  out_buffer[0]  = METHOD_GETANALOGVALUE;
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

// this is event handler, all vars should be volatile
void receiveEvent(int howMany){
  if(!howMany){
     return;
  }
  byte cnt = 0;
  volatile byte (*buffer) = 0;
  DEBUG("-input " );
  for( byte a = 0; a < MAINBOARD_BUFFER_LENGTH; a++ ){
    if(input_buffer[a][0] == 0 ){
      buffer = (&input_buffer[a][0]);
      while( Wire.available()){ // loop through all but the last
        byte w =  Wire.read(); // receive byte as a character
        *(buffer +(cnt++)) = w;
        DEBUG(w ); 
      }
      buff_length[a] = howMany;
      DEBUGLN(""); 
      return;
    }
  }
}

// znajdz kod resetu
byte getResetOrder( byte i2c_address ){
  if( i2c_address == 0 ){
    return 0;
  }
  if( i2c_address == I2C_ADR_IPANEL ){       // wozek 
    return 1;
  }
  for(byte i =0; i<nextpos;i++){
    if(order[i] == i2c_address){
      return i + 5;                  // na pozcji 0 zwracaj 5, na pozycji 1 zwracaj 6
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
    if(!prog_mode){
      DEBUG("-niedoczekanie...");
    }
    return 0x32;
  }
  while (Wire.available()){
    byte d = Wire.read();
//    DEBUG("READ:");
//    printHex(d);
    in_buffer[count] = d;//Wire.read();
    count++;
  }
  if( count == length){
    last_i2c_read_error = false;
  }else{
    last_i2c_read_error = true;
    if(!prog_mode){
      DEBUGLN("-!Odebralem liczb:" + String(count) );
    }
  }
  return count;
}

// wysyla dowolnÄ… ilosc liczb na kanal
byte writeRegisters(int deviceAddress, byte length, boolean wait) {
    byte c = 0;
    Wire.beginTransmission(deviceAddress); // start transmission to device 
//    Wire.write(out_buffer, length);         // send value to write
    while( c < length ){
      Wire.write(out_buffer[c]);         // send value to write
//      DEBUG("-Wysylam do " + String(deviceAddress) + ": ");
//      printHex(out_buffer[c]);
      c++;
    }
    byte error = Wire.endTransmission();     // end transmission
    if( error && !prog_mode ){
      DEBUGLN("-! writeRegisters error: " + String(error) +"/"+ String(deviceAddress));
      delay(100);
      return error;
    }
    if(wait){
      delay(20);
    }
  //  DEBUGLN("-end writeRegisters");
    return 0;
}
void send2debuger( String ns, String logstr ){
  if(!prog_mode){
    DEBUG("-");
    DEBUG(ns);
    DEBUG(": [");
    DEBUG(logstr);
    DEBUGLN("]");
  }
}
void send2androidEnd(){      // wyslij string do androida
	Serial.println();
}
void sendln2android( String output2 ){      // wyslij string do androida
	Serial.println( output2 );
}
void send2android( uint8_t bits8 ){
        Serial.write(bits8);
}
void send2android( uint8_t buf[], int len ){
        Serial.write(buf, len);
}
void send2android( String output2 ){      // wyslij string do androida
	Serial.print( output2 );
}
void serialEvent(){				             // Runs after every LOOP (means don't run if loop hangs)
    if(!prog_mode){
	while (Serial.available() && !Console0Complete) {    // odczytuj gdy istnieja dane i poprzednie zostaly odczytane
		char inChar = (char)Serial.read(); 
		serial0Buffer += String(inChar);
                serialBuff[ serialBuff_pos++ ] = inChar;
		if (inChar == '\n') {
			Console0Complete = true;
		}
	}
    }
}


