#define IS_MAINBOARD true
#define IS_PROGRAMMER true
#include <WSWire.h>
#include <barobot_common.h>
#include <i2c_helpers.h>
#include <AccelStepper.h>

volatile byte in_buffer[5];
volatile byte input_buffer[MAINBOARD_BUFFER_LENGTH][5] = {{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0}};
volatile byte out_buffer[5];
uint8_t serialBuff[130];
uint8_t serialBuff_pos   = 0;

boolean error=0;
int here;
uint8_t hbval=128;

byte nextpos             = 0;
boolean scann_order      = false;
byte order[COUNT_UPANEL] = {0,0,0,0,0,0,0,0,0,0,0,0};
boolean prog_mode        = false;
String serial0Buffer     = "";
boolean Console0Complete = false;   // This will be set to true once we have a full string
boolean last_i2c_read_error = false;

byte reprogramm_index = 0;


long int milis100 = 0;
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

void setup(){
  DEBUGINIT();
  DEBUGLN("MASTER START");
  pinMode(PIN_PROGRAMMER_LED_ACTIVE, OUTPUT);
  pinMode(PIN_PROGRAMMER_LED_ERROR, OUTPUT);  
  pinMode(PIN_PROGRAMMER_LED_STATE, OUTPUT);

  pinMode(PIN_MAINBOARD_SCK, INPUT );
  pinMode(PIN_MAINBOARD_MISO, INPUT );  
  pinMode(PIN_MAINBOARD_MOSI, INPUT );

  digitalWrite(PIN_PROGRAMMER_LED_ACTIVE, HIGH);
  digitalWrite(PIN_PROGRAMMER_LED_ERROR, HIGH);
  digitalWrite(PIN_PROGRAMMER_LED_STATE, HIGH);

  Wire.begin(I2C_ADR_MAINBOARD);
  digitalWrite(SDA, 1);    // disable pullups
  digitalWrite(SCL, 1);    // disable pullups
  Wire.onReceive(receiveEvent);  
//  Serial.println("master start");
}

#if MAINBOARD_SERVO_4PIN==true
AccelStepper stepperX(8, PIN_MAINBOARD_STEPPER_STEP0, PIN_MAINBOARD_STEPPER_STEP1, PIN_MAINBOARD_STEPPER_STEP2, PIN_MAINBOARD_STEPPER_STEP3 );
#else
AccelStepper stepperX(1, PIN_MAINBOARD_STEPPER_STEP, PIN_MAINBOARD_STEPPER_DIR);      // Step, DIR
#endif

void check_i2c(){  
  Wire.beginTransmission(I2C_ADR_RESERVED);
  byte ee = Wire.endTransmission();     // czy linia jest drozna
  if(ee == 6 ){    // niedrozna - resetuj i2c
    Serial.println("RWIRE");
//    pinMode(PIN_MAINBOARD_SDA, INPUT );
//    pinMode(PIN_MAINBOARD_SCL, INPUT );
    Wire.begin(I2C_ADR_MAINBOARD);
/*
    for(byte sa = 0x05; sa <= 110; sa++ ) {       // wyslij wszystkim ze reset
      out_buffer[0]  = 0x16;
      writeRegisters(sa, 1, true );
    }*/
    tri_state( PIN_PROGRAMMER_RESET_UPANEL, false );       // pin w stanie niskim = reset
    tri_state( PIN_PROGRAMMER_RESET_IPANEL, false );       // pin w stanie niskim = reset
    asm("nop");    // at least 1,5 us, can be more
    tri_state( PIN_PROGRAMMER_RESET_UPANEL, true );       // pin w stanie niskim = reset
    tri_state( PIN_PROGRAMMER_RESET_IPANEL, true );       // pin w stanie niskim = reset
  }
}

void loop(){
        long int mil = millis();
  	if( mil > milis100 + 2000 ){    // co 4 sek
                check_i2c();
                byte nDevices=0;
                error=0;
                for(byte addr2 = 1; addr2 < 20; addr2++ )   {
                  Wire.beginTransmission(addr2);
                  error = Wire.endTransmission();
                  if (error == 0){
                    Serial.print("-dev @");
                    printHex(addr2, false );
                    uint16_t readed = i2c_getVersion(addr2);
                    Serial.print(" type: ");
                    printHex( readed>>8, false );    // starsze bity = typ
                    Serial.print(" ver: ");
                    printHex( readed & 0xff, false );    // młodsze bity = ver
                    Serial.print(" pos: ");
                    byte pos = getResetOrder(addr2);
                    printHex( pos, false );   
                    Serial.println("");
                    nDevices++;
                  }else{
              //     Serial.println("RET: "+String(addr2)+" / "+String(error));
                  }
                }
              //  Serial.println("Devices:" + String(nDevices));
              //  scann();
                hbval++;
                if(hbval>=10){
                  get_order();
                  hbval=0;
                }
  		milis100 = mil;
  	}
		if (Console0Complete) {
			parseInput( serial0Buffer );				      // parsuj wejscie
			Console0Complete = false;
                        serialBuff_pos = 0;
			serial0Buffer = "";
		}

    for( byte i=0;i<MAINBOARD_BUFFER_LENGTH;i++){
      if( input_buffer[i][0] ){
        proceed( input_buffer[i] );
        input_buffer[i][0] = 0;
      }
    }
}

void scann(){
  byte error;
  for(byte aaa = I2C_ADR_MAINBOARD; aaa < 20; aaa++ ) {
    Wire.beginTransmission(aaa);
    error = Wire.endTransmission();
    if (error == 0){
      uint16_t readed = i2c_getVersion(aaa);
      if( (readed>>8) > 0 && ((readed & 0xff) >0)){
        i2c_setPWM( aaa, 44, 12 );
        unsigned int errors= test_slave( aaa );
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

void parseInput( String input ){   // zrozum co sie dzieje
	input.trim();
	send2debuger( "COMMAND", input );
	boolean defaultResult = true;

	if( input.charAt( 0 ) == METHOD_SEND2SLAVE ){      // wyślij przez i2c do slave i spodziewaj się wyniku
          Serial.println("K14");
          // np 0x14 0x0A 0x02 0x11 0x22 0x33       // wyślij po i2c do slave numer 0x0A bajty: 0x11 0x22 0x33  i spodziewaj się 2 bajtów wyniku
          byte count          = input.length() - 3;    // tyle do wysłania
          byte slave_address  = input.charAt( 1 ); 
          byte needs          = input.charAt( 2 );
          if( count > 0){
            for(byte i =0; i<count;i++){
              out_buffer[i]      = input.charAt( i - 3 );      // piewszy bajt komendy jest w czwartym bajcie (na pozycji 3 licząc od 0)
            }
            byte error = writeRegisters(slave_address, count, true );
            if( error ){
              Serial.println("!!!i2c_getVersion error1"); 
              // ERROR command
            }else{
              if( needs > 0){                // moge chcieć 0 bajtów
                readRegisters( slave_address, needs );
                // wynik w in_buffer;
              }else{
              }
            }
          }else{  // nic nie wysyłaj bo nie ma co
            // wrong_call++
          }
//	}else if( input.charAt( 0 ) == 0x11 ){      // wyślij przez i2c do slave
          // np 0x11 0x0A 0x11 0x22 0x33      // wyślij po i2c do slave numer 0x0A bajty: 0x11 0x22 0x33

	}else if( input.startsWith("PROG ")) {    // PROG 0A,1    // PROG 0A,0   - programuj urzadzenie 0x0A z prędkosca 19200, PROG 0,0 - force first, PROG 0A,0 - wozek
                read_prog_settings(input, 1);
                defaultResult = false;
                return;
	}else if( input.startsWith("PROGN ")) {    // PROGN 0,0,0   - programuj urzadzenie podłączone do 0x0C (number 0 )
                read_prog_settings(input, 2 );
                defaultResult = false;
                return;
	}else if( input.startsWith("RESET ")) {    // RESET 0A - resetuj urzadzenie 0x0A
                String digits     = input.substring( 6 );
                char charBuf[20];
                digits.toCharArray(charBuf,20);
                uint8_t i2c_address    = 0x00;
                sscanf(charBuf,"%x", &i2c_address );
                byte num = getResetOrder(i2c_address);
                delay(200);
                boolean ret = reset_device_num(num, LOW);
                if(ret){
                  delay(1000);
                  reset_device_num(num, HIGH);
                  defaultResult = false;
                }else{  //error
                }
	}else if( input.startsWith("RESETN ")) {    // RESETN n - resetuj upanel pod numerem n, 0 - pierwszy, ff- po ostatnim wykrytym (np gdy nie da się wykryć)
                String digits     = input.substring( 6 );
                char charBuf[20];
                digits.toCharArray(charBuf,20);
                uint8_t target    = 0x00;
                sscanf(charBuf,"%x", &target );
                delay(200);
                reset_device_num(target, LOW);   
                boolean ret = reset_device_num(target, LOW);
                if(ret){
                  delay(1000);
                  reset_device_num(target, HIGH);
                  defaultResult = false;
                }else{

                }

/*
        }else if ( input.startsWith("SET") ) {      // tutaj niektore beda synchroniczne inne asynchroniczne wiec czasem zwracaj R, a czasem dopiero po zakonczeniu
		if( false){
		}else{
			sendln2android("ARDUINO NO COMMAND [" + input +"]");
			defaultResult = false;
		}
	}else if( input.startsWith("GET")) {
		defaultResult = false;

	}else if( input.equals("EX") ){
		stepperX.enableOutputs();
	}else if( input.equals("DX") ){
		if(MAINBOARD_STEPPER_READY_DISABLE){
			stepperX.disableOutputs();
		}
	}else if( input.equals("I2C SCANN") ){
//		scann_i2c();
//		defaultResult = false;

	}else if( input.startsWith("I2C ") ){			// wyślij komendę do urządzenia i2c o podanym numerze
		byte address	= input.charAt( 4 );		// adres i2c
		String ss 		= input.substring( 5 );		// "I2C " 4 znaki, 5 to adres więc podaj od szóstego

		send2debuger( "I2C ADR", ""+String(address) );
		send2debuger( "I2C LEN", ""+String(ss.length()) );
		send2debuger( "I2C PARAMS", ss );

		char const *c	= ss.c_str();				// lista parametrow, pierwszy to komenda
		for(byte a; a<ss.length(); a++){
			send2debuger( "I2C PARAM", ""+String(c[a]) );
		}	
		defaultResult = false; 

	}else if( input.equals("PING2ARDUINO") ){        // odeslij PONG
		sendln2android("PONG");
		defaultResult = false;
	}else if( input.startsWith("ANDROID ") ){    // zwrotka, nic nie rób
		defaultResult = false;
	}else if( input.equals( "PONG" )){			// nic, to byla odpowiedz na moje PING
		defaultResult = false;
	}else if( input.equals( "PING2ANDROID") ){      // nic nie rob
		defaultResult = false;

	}else if( input.equals( "WAIT READY") ){      // tylko zwróc zwrotke
*/
	}else{
                // nie rozumiem
		sendln2android("ARDUINO NO COMMAND [" + input +"]");
		defaultResult = false;  
	}
	if(defaultResult ){
		sendln2android("R " + input );
	}
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
//    tri_state( PIN_PROGRAMMER_RESET_MASTER, pin_value );
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
    byte target           = 0x00;
    byte slow_sck         = 0;
    byte serial_baud_num  = 0;
    char charBuf[10];
    digits.toCharArray(charBuf, 10);
    sscanf(charBuf,"%x,%x,%i", &target, &serial_baud_num, &slow_sck );
    if(ns == 1){            // PROG - podanu adres, znajdz numer
      reprogramm_index    = getResetOrder(target);
    }else if(ns == 2){     // PROGN
      reprogramm_index    = target;
    }
    programmer_mode(true, serial_baud_num, slow_sck );
}

void proceed( volatile byte buffer[5] ){
  if(prog_mode){
    return;
  }
  if(buffer[0] == METHOD_HERE_I_AM){    // here_i_am
    // sprawdz czy ten adres nie jest uzywany
    if(scann_order){
      byte pos = getResetOrder(buffer[1]);
      if( pos !=0xFF || (nextpos >= COUNT_UPANEL) ){
        scann_order  =  false;
        Serial.println("koniec order");
      }else if( buffer[2] == IPANEL_DEVICE_TYPE ){
        Serial.println("Wozek! ");  
      }else{
        printHex( buffer[1], false );
        Serial.println("- Upanel pod " + String(nextpos));
        /*
        printHex(buffer[1], false);
        Serial.print(" ");
        printHex(buffer[2], false);
        Serial.print(" ");
        printHex(buffer[3], false);
        Serial.print(" ");
        printHex(buffer[4], false);
        Serial.print(" ");
        printHex(buffer[5]);
*/
        order[nextpos++]  = buffer[1];     // na tm miejscu slave o tym adresie
        i2c_reset_next( buffer[1], false );       // reset next (next to slave)
        i2c_reset_next( buffer[1], true );
      }
    }
  }else{
    Serial.println("recieve unknown - ");
    printHex(buffer[0]);
  }
  buffer[0] = 0;  //ready
}

void i2c_resetCycles( byte slave_address ){
  out_buffer[0]  = METHOD_RESETCYCLES;
  writeRegisters(slave_address, 1, true );
}
void i2c_setPWM( byte slave_address, byte pin, byte level ){		                              // Wpisz wypełnienie PWM do LEDa ( 3 bajty )
  out_buffer[0]  = METHOD_SETPWM;
  out_buffer[1]  = pin;
  out_buffer[2]  = level;
  writeRegisters(slave_address, 3, true );
}
void i2c_setTime( byte slave_address, byte pin, byte on_time, byte off_time ){			            // Czas pomiędzy kolejnym zapaleniem i Czas od zapalenia do zgaszenia ( 4 bajty )
  out_buffer[0]  = METHOD_SETTIME;
  out_buffer[1]  = pin;
  out_buffer[2]  = on_time;
  out_buffer[3]  = off_time;
  writeRegisters(slave_address, 3, true );
}
void i2c_setFading( byte slave_address,  byte pin, byte level_in, byte level_out ){			       // Czas i kierunek zanikania PWMa	( 4 bajty )
  out_buffer[0]  = METHOD_SETFADING;
  out_buffer[1]  = pin;
  out_buffer[2]  = level_in;
  out_buffer[2]  = level_out;
  writeRegisters(slave_address, 2, true );
}
/*
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
}*/

void i2c_reset_next( byte slave_address, boolean pin_value ){
  if( pin_value ){                  // Koniec resetu urządzenia obok urządzenia adresowego, stan wysokiej impedancji na wyjściu
    out_buffer[0]  = METHOD_RUN_NEXT;
    writeRegisters(slave_address, 1, true );  
  }else{	                    // Resetuj urządzenie obok urządzenia adresowego, stan niski na wyjściu resetuje tego obok
    out_buffer[0]  = METHOD_RESET_NEXT;
    writeRegisters(slave_address, 1, true );
  }
}

void i2c_prog_mode( boolean active, byte slave_address ){                  // Toggle programm mode in slave
  if(active){
    out_buffer[0]  = METHOD_PROG_MODE_ON;    // ON
    out_buffer[1]  = slave_address;
  }else{
    out_buffer[0]  = METHOD_PROG_MODE_OFF;    // OFF
  }
  writeRegisters(slave_address, 1, true );
}

void get_order(){      // pobierz kolejnosc elementów
      Serial.println("Resetuje");
      pinMode(PIN_PROGRAMMER_RESET_UPANEL, OUTPUT); 
      digitalWrite(PIN_PROGRAMMER_RESET_UPANEL, LOW);
      tri_state( PIN_PROGRAMMER_RESET_UPANEL, false );       // pin w stanie niskim = reset
     // end reset = start first slave
      tri_state( PIN_PROGRAMMER_RESET_UPANEL, true );       // run slave

      // wait for  here_i_am {METHOD_HERE_I_AM,my_address,type,ver}
      nextpos = 0;
      scann_order = true;
}

void i2c_reloadAddress( byte slave_address, byte new_addr ){			// Zmień adres I2c, musi być podane co najmniej 4 razy zeby zadziałało. (2 bajty)
  out_buffer[0]  = METHOD_RESETSLAVEADDRESS;
  out_buffer[1]  = new_addr;
  writeRegisters(slave_address, 2, true );    // powtarzaj
  writeRegisters(slave_address, 2, true );
  writeRegisters(slave_address, 2, true );
  writeRegisters(slave_address, 2, true );
  writeRegisters(slave_address, 2, true );
}

uint16_t i2c_getVersion( byte slave_address ){      // zwraca 2 bajty. typ na młodszych bitach, versja na starszych
  out_buffer[0]  = METHOD_GETVERSION;
  byte error = writeRegisters(slave_address, 1, true );
  if( error && !prog_mode ){
   Serial.println("!!!i2c_getVersion error1");  
  }
  if( !error ){
    readRegisters( slave_address, 2 );
    uint16_t res = in_buffer[0];    // = typ na starszych bitach
    res = (res<<8) | in_buffer[1];    // = wersja na młodszych bitach
    return res;
  }
  return 0xFF;
}

void i2c_stop( byte slave_address ){      // zgaś wszystko
  out_buffer[0]  = 0xEE;
  writeRegisters(slave_address, 1, true );
  writeRegisters(slave_address, 1, true );
}

byte i2c_test_slave( byte slave_address, byte num1, byte num2 ){      // testuj
  out_buffer[0]  = METHOD_TEST_SLAVE;
  out_buffer[1]  = num1;
  out_buffer[2]  = num2;  
  byte error = writeRegisters(slave_address, 3, true );
  if( error != 0 && !prog_mode ){
    Serial.println("!!!Test error1" + String(error));  
  }
  if( error == 0 ){
    readRegisters( slave_address, 1 );
    return in_buffer[0];
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
  out_buffer[0]  = METHOD_GETVALUE;
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

void receiveEvent(int howMany){
  if(!howMany){
     return;
  }
  byte cnt = 0;
  volatile byte (*buffer) = 0;
//  Serial.print("input " );
  for( byte a = 0; a < MAINBOARD_BUFFER_LENGTH; a++ ){
    if(input_buffer[a][0] == 0 ){
      buffer = (&input_buffer[a][0]); 
      while( Wire.available()){ // loop through all but the last
        byte w =  Wire.read(); // receive byte as a character
        *(buffer +(cnt++)) = w;
//        printHex(w, false ); 
      }
//      Serial.println(""); 
      return;
    }
  }
//  Serial.println(" - pelno"); 
}

// znajdz kod resetu
byte getResetOrder( byte i2c_address ){
  if( i2c_address == 0 ){    // reset mainboard = error
    return 0xff;
  }
  if( i2c_address == I2C_ADR_IPANEL ){       // wozek 
    return 0;
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
      Serial.print("niedoczekanie...");
    }
    return 0x32;
  }
  while (Wire.available()){
    byte d = Wire.read();
//    Serial.print("READ:");
//    printHex(d);
    in_buffer[count] = d;//Wire.read();
    count++;
  }
  if( count == length){
    last_i2c_read_error = false;
  }else{
    last_i2c_read_error = true;
    if(!prog_mode){
      Serial.println("!!!Odebralem liczb:" + String(count) );
    }
  }
  return count;
}

// wysyla dowolną ilosc liczb na kanal
byte writeRegisters(int deviceAddress, byte length, boolean wait) {
    byte c = 0;
    Wire.beginTransmission(deviceAddress); // start transmission to device 
//    Wire.write(out_buffer, length);         // send value to write
    while( c < length ){
      Wire.write(out_buffer[c]);         // send value to write
//      Serial.print("Wysylam do " + String(deviceAddress) + ": ");
//      printHex(out_buffer[c]);
      c++;
    }
    byte error = Wire.endTransmission();     // end transmission
    if( error && !prog_mode ){
      Serial.println("!!! writeRegisters error: " + String(error) +"/"+ String(deviceAddress));
      delay(100);
      return error;
    }
    if(wait){
      delay(20);
    }
  //  Serial.println("end writeRegisters");
    return 0;
}

void send2debuger( String ns, String logstr ){
  if(!prog_mode){
    Serial.print(ns);
    Serial.print(": [");
    Serial.print(logstr);
    Serial.println("]");
  }
}

void sendln2android(){      // wyslij string do androida
	Serial.println();
}
void sendln2android( String output2 ){      // wyslij string do androida
	Serial.println( output2 );
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



// These set the correct timing delays for programming a target with clk < 500KHz.
// Be sure to specify the lowest clock frequency of any target you plan to burn.
// The rule is the programing clock (on SCK) must be less than 1/4 the clock
// speed of the target (or 1/6 for speeds above 12MHz).  For example, if using
// the 128KHz RC oscillator, with a prescaler of 8, the target's clock frequency
// would be 16KHz, and the maximum programming clock would be 4KHz, or a clock
// period of 250uS.  The algorithm uses a quarter of the clock period for sync
// purposes, so QUARTER_PERIOD would be set to 63uS.  Be aware that internal RC
// oscillators can be off by as much as 10%, so you might have to force a slower
// clock speed.
#define MINIMUM_TARGET_CLOCK_SPEED 128000
#define SCK_FREQUENCY (MINIMUM_TARGET_CLOCK_SPEED/4)
//#define QUARTER_PERIOD ((1000000/SCK_FREQUENCY/4)+1)
#define QUARTER_PERIOD 50

typedef struct param {
  uint8_t devicecode;
  uint8_t revision;
  uint8_t progtype;
  uint8_t parmode;
  uint8_t polling;
  uint8_t selftimed;
  uint8_t lockbytes;
  uint8_t fusebytes;
//  int flashpoll;
//  int eeprompoll;
  int pagesize;
//  int eepromsize;
//  int flashsize;
} parameter;
parameter param;

void (*spi_init)();
uint8_t (*spi_send)(uint8_t);

void programmer_mode( boolean active, byte serial_baud_num, boolean slow_sck ) {
  if(active){
    byte error;
    prog_mode = true;
    for(byte aaa = I2C_ADR_MAINBOARD; aaa < 20; aaa++ ) {
      Wire.beginTransmission(aaa);
      error = Wire.endTransmission();
      if (error == 0){
        i2c_prog_mode(true, reprogramm_index );  
      }
    }
    if( serial_baud_num ){
      Serial.begin(115200);
    }else{
      Serial.begin(PROGRAMMER_SERIAL0_BOUND);
    }
    if (slow_sck){
      spi_init = sw_spi_init;
      spi_send = sw_spi_send;
    } else {
      spi_init = hw_spi_init;
      spi_send = hw_spi_send;
    }
    pulse(PIN_PROGRAMMER_LED_ACTIVE, 2);
    pulse(PIN_PROGRAMMER_LED_ERROR, 2);
    pulse(PIN_PROGRAMMER_LED_STATE, 2);
    while(prog_mode){
      digitalWrite(PIN_PROGRAMMER_LED_ERROR, !error); 
      heartbeat();
      if (Serial.available()) {
        simulateisp();
      }
    }
  }else{
    digitalWrite(PIN_PROGRAMMER_LED_ACTIVE, HIGH);
    digitalWrite(PIN_PROGRAMMER_LED_ERROR, HIGH);
    digitalWrite(PIN_PROGRAMMER_LED_STATE, HIGH);
    pinMode(MISO, INPUT);
    pinMode(MOSI, INPUT);
    pinMode(SCK, INPUT);
    reset_device_num( reprogramm_index, HIGH);
    prog_mode = false;
    delay(2000);
    Serial.begin(MAINBOARD_SERIAL0_BOUND);

  }
}
void start_pmode() {
  spi_init();
  error = false;
  digitalWrite(PIN_PROGRAMMER_LED_ACTIVE, LOW);
  reset_device_num( reprogramm_index, LOW);
  reset_device_num( reprogramm_index, HIGH);
  reset_device_num( reprogramm_index, LOW);
  pinMode(SCK, OUTPUT);
  digitalWrite(SCK, LOW);
  delay(50);
  reset_device_num( reprogramm_index, LOW);
  delay(50);
  pinMode(MISO, INPUT);
  pinMode(MOSI, OUTPUT);
  spi_transaction(0xAC, 0x53, 0x00, 0x00);
}

void pulse(int pin, int times) {
  do {
    digitalWrite(pin, LOW);
    delay(PTIME);
    digitalWrite(pin, HIGH);
    delay(PTIME);
  } 
  while (times--);
}
void heartbeat() {
  hbval += ( hbval + 12 % 150) + 35;
  analogWrite(PIN_PROGRAMMER_LED_STATE, hbval);
  delay(20);
}

uint8_t getch() {
  while(!Serial.available());
  return Serial.read();
}
void fill(int n) {
  for (int x = 0; x < n; x++) {
    serialBuff[x] = getch();
  }
}

uint8_t hw_spi_send(uint8_t b) {
  uint8_t reply;
  SPDR=b;
  spi_wait();
  reply = SPDR;
  return reply;
}

void hw_spi_init() {
  uint8_t x;
  SPCR = 0x53;
  x=SPSR;
  x=SPDR;
}

void spi_wait() {
  do {
  } while (!(SPSR & (1 << SPIF)));
}

uint8_t spi_transaction(uint8_t a, uint8_t b, uint8_t c, uint8_t d) {
  uint8_t n;
  spi_send(a); 
  n=spi_send(b);
  n=spi_send(c);
  return spi_send(d);
}

void empty_reply() {
  if (CRC_EOP == getch()) {
    Serial.print((char)STK_INSYNC);
    Serial.print((char)STK_OK);
  } else {
    error = true;
    Serial.print((char)STK_NOSYNC);
  }
}

void breply(uint8_t b) {
  if (CRC_EOP == getch()) {
    Serial.print((char)STK_INSYNC);
    Serial.print((char)b);
    Serial.print((char)STK_OK);
  } else {
    error = true;
    Serial.print((char)STK_NOSYNC);
  }
}

uint8_t sw_spi_send(uint8_t b) {
  unsigned static char msk[] = { 0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01 };
  uint8_t reply=0;
  char bits[8] = { 0, 0, 0, 0, 0, 0, 0, 0 };
  for(uint8_t _bit = 0;_bit < 8;_bit++){
    digitalWrite(MOSI, !!(b & msk[_bit]));
    delayMicroseconds(QUARTER_PERIOD);
    digitalWrite(SCK, HIGH);
    delayMicroseconds(QUARTER_PERIOD);
    bits[_bit] = digitalRead(MISO);
    delayMicroseconds(QUARTER_PERIOD);
    digitalWrite(SCK, LOW);
    delayMicroseconds(QUARTER_PERIOD);
  }
  reply = bits[0] << 7 | bits[1] << 6 | bits[2] << 5 | bits[3] << 4 | bits[4] << 3 | bits[5] << 2 | bits[6] << 1 | bits[7];
  return reply;
}
void sw_spi_init() {
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
  // call this after reading paramter packet into serialBuff[]
  param.devicecode = serialBuff[0];
  param.revision   = serialBuff[1];
  param.progtype   = serialBuff[2];
  param.parmode    = serialBuff[3];
  param.polling    = serialBuff[4];
  param.selftimed  = serialBuff[5];
  param.lockbytes  = serialBuff[6];
  param.fusebytes  = serialBuff[7];
//  param.flashpoll  = serialBuff[8]; 
  // ignore serialBuff[9] (= serialBuff[8])
//  param.eeprompoll = beget16(&serialBuff[10]);
  param.pagesize   = beget16(&serialBuff[12]);
//  param.eepromsize = beget16(&serialBuff[14]);

  // 32 bits flashsize (big endian)
//  param.flashsize = serialBuff[16] * 0x01000000+ serialBuff[17] * 0x00010000 + serialBuff[18] * 0x00000100 + serialBuff[19];
}

void universal(){
  uint8_t ch;
  fill(4);
  ch = spi_transaction(serialBuff[0], serialBuff[1], serialBuff[2], serialBuff[3]);
  breply(ch);
}

void flash(uint8_t hilo, int addr, uint8_t data) {
  spi_transaction(0x40+8*hilo, 
  addr>>8 & 0xFF, 
  addr & 0xFF,
  data);
}

void commit(int addr) {
  digitalWrite(PIN_PROGRAMMER_LED_ACTIVE,  !digitalRead(PIN_PROGRAMMER_LED_ACTIVE));    // Toggle led. Read from register (not from pin)
  spi_transaction(0x4C, (addr >> 8) & 0xFF, addr & 0xFF, 0);
}

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
    error = true;
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
    flash(LOW, here, serialBuff[x++]);
    flash(HIGH, here, serialBuff[x++]);
    here++;
  }
  commit(page);
  return STK_OK;
}

uint8_t flash_read(uint8_t hilo, int addr) {
  return spi_transaction(0x20 + hilo * 8,
  (addr >> 8) & 0xFF,
  addr & 0xFF,
  0);
}

char flash_read_page(int length) {
  digitalWrite(PIN_PROGRAMMER_LED_ACTIVE,  !digitalRead(PIN_PROGRAMMER_LED_ACTIVE));    // Toggle led. Read from register (not from pin)
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
    error=true;
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

void program_page() {
  char result = (char) STK_FAILED;
  int length = 256 * getch();
  length += getch();
  char memtype = getch();
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
      Serial.print((char) STK_NOSYNC);
      error = true;
    }
    return;
  }
  Serial.print((char)STK_FAILED);
  return;
}

void read_signature() {
  if (CRC_EOP != getch()) {
    error=true;
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

void simulateisp() { 
  uint8_t data, low, high;
  uint8_t ch = getch();
  switch (ch) {
  case '0': // signon
    error = false;
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
  case 'E':
    fill(5);
    empty_reply();
    break;
  case 'P':      //0x50
    start_pmode();
    empty_reply();
    break;
  case 'U': // set address (word)
    here = getch();
    here += 256 * getch();
    empty_reply();
    break;
  case 'Q': //0x51
    error=false;
    if (CRC_EOP == getch()) {
      Serial.print((char)STK_INSYNC);
      Serial.print((char)STK_OK);
    } else {
      error = true;
      Serial.print((char)STK_NOSYNC);
    }
//    Serial.print((char) STK_INSYNC);  // 0x14
//    Serial.print((char) STK_OK);  // 0x10
    programmer_mode(false, 0, false );
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
  case CRC_EOP:
    Serial.print((char) STK_NOSYNC);
    break;
  default:
    char r = STK_NOSYNC;
    if (CRC_EOP == getch()) {
        r = STK_UNKNOWN;
    }
    Serial.print(r);
  }
/*        
  default:
    error=true;
    char r = STK_NOSYNC;
    if (CRC_EOP == getch()) {
        r = STK_UNKNOWN;
    }
    Serial.print(r);
  }*/
}

