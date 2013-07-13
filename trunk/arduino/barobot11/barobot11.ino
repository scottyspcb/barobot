// numer 10
#include <AccelStepper.h>
#include "config.cpp";
#include <NewPing.h>
#include <SPI.h>
#include <Adb.h>
#include <Servo.h>

// stan uC
byte status = STATE_INIT;      // na pocztku jest w trakcje inicjacji
// koniec stanu uC

// obsluga zrodel wejscia
Connection * connection;        // Adb connection.
boolean adb_ready = false;
String serial0Buffer = "";
String serial3Buffer = "";
boolean Console0Complete = false;   // This will be set to true once we have a full string
boolean Console3Complete = false;   // This will be set to true once we have a full string
// koniec obsluga zrodel wejscia

// http://code.google.com/p/arduino-new-ping/wiki/Using_NewPing_Syntax
NewPing ultrasonic0(PIN_ULTRA0_TRIG, PIN_ULTRA0_ECHO, MAX_DISTANCE);
NewPing ultrasonic1(PIN_ULTRA1_TRIG, PIN_ULTRA1_ECHO, MAX_DISTANCE);

String stos = "";
int stos_length = 0;

void setupSerial(){      // uzyte w setup()
  if (DEBUG_OVER_SERIAL){
    Serial.begin(SERIAL0_BOUND);
    send2debuger("INIT", "START init");
  }
  if( DEBUG_OVER_BT || USE_BT ){
    Serial3.begin( BT_BOUND, SERIAL_8N1 ); //BT
    delay(1000);
    Serial3.print( "AT" );
    delay(1000);
    Serial3.print( "AT+PIN0000" );
    delay(1000);
    Serial3.print( "AT+NAME__BT_NAME__" );
  }
}

#if SERVOX4PIN==true
AccelStepper stepperX(4, STEPPER_X_STEP0, STEPPER_X_STEP1, STEPPER_X_STEP2, STEPPER_X_STEP3 );
#else
AccelStepper stepperX(1, STEPPER_X_STEP, STEPPER_X_DIR);      // Step, DIR
#endif

#if SERVOY4PIN==true
AccelStepper stepperY(4, STEPPER_Y_STEP0, STEPPER_Y_STEP1, STEPPER_Y_STEP2, STEPPER_Y_STEP3 );
#else
AccelStepper stepperY(1, STEPPER_Y_STEP, STEPPER_Y_DIR);// Step, DIR
#endif

Servo servoZ;
long margin_x_min = 0;
long margin_y_min = 0;
boolean lock_system = false;
boolean send_ret = true;        // czy wysyłać odpowiedzi po wykonaniu komend
boolean last_stepper_operation = false;

#define LOS_MAX 20

void setupOutputs(){      // uzyte w setup()
  pinMode( STATUS_LED01, OUTPUT);      // LEDY podświetlenia
  pinMode( STATUS_LED02, OUTPUT);
  pinMode( STATUS_LED03, OUTPUT);
  pinMode( STATUS_LED04, OUTPUT);
  pinMode( STATUS_LED05, OUTPUT);
  pinMode( STATUS_LED06, OUTPUT);
  pinMode( STATUS_LED07, OUTPUT);
  pinMode( STATUS_LED08, OUTPUT);
  pinMode( STATUS_LED09, OUTPUT);

  pinMode( IRRX_PIN, INPUT);
  pinMode( IRRY_PIN, INPUT);
  pinMode( IRRZ_PIN, INPUT);


  pinMode( PIN_MADDR0, OUTPUT);      // adresowanie wyjscia w multiplekserze
  pinMode( PIN_MADDR1, OUTPUT);
  pinMode( PIN_MADDR2, OUTPUT);
  pinMode( PIN_MADDR3, OUTPUT);

//  pinMode( STEPPER_Z_ENABLE, OUTPUT);
  pinMode( STEPPER_Z_PWM, OUTPUT);

  pinMode( STATUS_LED_PIN, OUTPUT);           // zapal diode statusu (w trakcje inicjacji)
  digitalWrite( STATUS_LED_PIN, HIGH );
  digitalWrite( STATUS_LED01, LOW );    // zgas ledy na początku
  digitalWrite( STATUS_LED02, LOW );
  digitalWrite( STATUS_LED03, LOW );
  digitalWrite( STATUS_LED04, LOW );
  digitalWrite( STATUS_LED05, LOW );
  digitalWrite( STATUS_LED06, LOW );
  digitalWrite( STATUS_LED07, LOW );
  digitalWrite( STATUS_LED08, LOW );
  digitalWrite( STATUS_LED09, LOW );
}

long unsigned acc_x = ACCELERX;
long unsigned acc_y = ACCELERY;

void setupSteppers(){                  // uzyte w setup()
  stepperX.setAcceleration(ACCELERX);    // lewo prawo
  stepperX.setMaxSpeed(SPEEDX); 
  stepperY.setAcceleration(ACCELERY);    // wgląb
  stepperY.setMaxSpeed(SPEEDY);
  //  stepperY.setMinPulseWidth(20000); 
}

void setupUltrasonic(){      // uzyte w setup()
}

void setupADB(){      // uzyte w setup()
  if (USE_ADB ){
    //    delay(500);
    ADB::init();
    delay(500);
    connection = ADB::addConnection("tcp:ADB_PORT", true, adbEventHandler);  // Open an ADB stream to the phone's shell. Auto-reconnect. Use any unused port number eg:4568
    delay(500);
  }
}
// Ustawienia Przerwań
void setupInts(){      // uzyte w setup()

  pinMode(INT5, INPUT); 
  //pinMode(INT4, INPUT);

  digitalWrite(INT5, HIGH); //turn pullup resistor on
  //    digitalWrite(INT4, HIGH); //turn pullup resistor on

  //  attachInterrupt( INT0, on_int0R, RISING);    // nasłuchuj zmiany PIN 2
  //  attachInterrupt( INT0, on_int0F, FALLING);    // nasłuchuj zmiany PIN 2

  //  attachInterrupt( INT2, on_int2R, RISING);    // nasłuchuj zmiany PIN 21    // Krańcowy Y
  //  attachInterrupt( INT2, on_int2F, FALLING);    // nasłuchuj zmiany PIN 21   // Krańcowy Y

  //  attachInterrupt( INT3, on_int3R, RISING);     // nasłuchuj zmiany PIN 20   // Krańcowy X
  //  attachInterrupt( INT3, on_int3F, FALLING);    // nasłuchuj zmiany PIN 20  // Krańcowy X

  //  attachInterrupt( INT4, on_int4R, CHANGE);    // nasłuchuj zmiany PIN 19  // Enkoder X
  //  attachInterrupt( INT4, on_int4F, FALLING);    // nasłuchuj zmiany PIN 19 // Enkoder X

  attachInterrupt( INT5, on_int5R, CHANGE);    // nasłuchuj zmiany PIN 18 // Enkoder Y
  //  attachInterrupt( INT5, on_int5F, FALLING);    // nasłuchuj zmiany PIN 18// Enkoder Y
}

volatile bool was_interrupt = LOW;    // przerwania połączona spójnikiem OR
volatile bool int0_value = LOW;
volatile bool int1_value = LOW;
volatile bool int2_value = LOW;
volatile bool int3_value = LOW;
//volatile bool int4_value = LOW;
//volatile bool int5_value = LOW;

volatile unsigned long encoder_x = 0;    // pozycja enkodera (nieminusowa, volatile bo uzywana w pezrwaniach)
volatile unsigned long encoder_y = 0;    // pozycja enkodera (nieminusowa, volatile bo uzywana w pezrwaniach)

void on_int0R(){  
  int0_value = HIGH;
  was_interrupt = HIGH;
}    // pin 2
void on_int0F(){  
  int0_value = HIGH;
  was_interrupt = HIGH;
}    // pin 2
void on_int2R(){  
  int2_value = HIGH;
  was_interrupt = HIGH;
}    // pin 21       // Krańcowy Y rise
void on_int2F(){  
  int2_value = HIGH;
  was_interrupt = HIGH;
}    // pin 21       // Krańcowy Y fall
void on_int3R(){  
  int3_value = HIGH;
  was_interrupt = HIGH;
}    // pin 20       // Krańcowy X rise
void on_int3F(){  
  int3_value = HIGH;
  was_interrupt = HIGH;
}    // pin 20       // Krańcowy X fall
void on_int4R(){  
  encoder_x++;
}          // pin 19       // Enkoder X
//void on_int4F(){  }    // pin 19                       // Enkoder X
void on_int5R(){  
  encoder_y++;
}          // pin 18       // Enkoder Y
//void on_int5F(){  }    // pin 18                       // Enkoder Y
// Koniec Ustawienia Przerwań

void afterSetup(){ 
  digitalWrite( STATUS_LED_PIN, 0 );      // koniec inicjacji
  send2debuger("INIT", "START ready");

  //NewPing::timer_ms(20, in_20ms);   // Create a Timer2 interrupt that calls func
  //NewPing::timer_ms(40, in_40ms);   // Create a Timer2 interrupt that calls func
  // NewPing::timer_ms(100, in_100ms);   // Create a Timer2 interrupt that calls func
  //NewPing::timer_ms(1000, in_1000ms);   // Create a Timer2 interrupt that calls func
}

void setup(){ 
  // noInterrupts();
  status= STATE_BUSY;      // w inicie jest w stanie zajętym
  setupOutputs();
  setupSerial();
  setupInts();
  setupUltrasonic();
  setupSteppers();
  setupADB();
  afterSetup();
  status= STATE_READY;      // po wykonaniu operacji jest w stanie gotowym
  send2debuger("INIT", "Koniec init");
  //interrupts();
}

unsigned long counter20 = 0, counter40 = 0, counter100 = 0, counter1000 = 0;
unsigned long milis20 = 0,  milis40 = 0, milis100 = 0, milis1000 = 0;
unsigned long mil = 0;

void loop(){
  run_steppers();
  if(lock_system){    // jade silnikiem, nie rob nic wiecej
    return;
  }
  if( int0_value == HIGH ){
    send2android( "INT 0" );
    int0_value = LOW;
  }
  mil = millis();
  if( mil > milis20 + 20 ){      // 50 razy na sek
    //   in_20ms();
    //    counter20++;
    //    milis20 = mil;
  }
  if( mil > milis40 + 40 ){      // 25 razy na sek
    in_40ms();
    counter40++;
    milis40 = mil;
  }
  if( mil > milis100 + 100 ){      // 10 razy na sek
    in_100ms();
    counter100++;
    milis100 = mil;
  }
  if( mil > milis1000 + 1000 ){      // 1 razy na sek
    in_1000ms();
    counter1000++;
    milis1000 = mil;
  }
  if( stos_length ){// gdy odblokuje sie lock_system to wykonaj nastepne polecenie
    String runcmd = stosPop();
    parseInput( runcmd );                      // parsuj wejscie
  }
  if (Console0Complete) {
    readSerial0Input( serial0Buffer );          // parsuj wejscie
    Console0Complete = false;
    serial0Buffer = "";
  }
  if( USE_BT || DEBUG_OVER_BT ){
    if (Console3Complete) {
      readSerial3Input( serial3Buffer );          // parsuj wejscie
      Console3Complete = false;
      serial3Buffer = "";
    }
  }
  if( USE_ADB ){
    ADB::poll();      // Poll the ADB subsystem.
  }
}

void in_20ms(){
}
void in_40ms(){
  digitalWrite( STATUS_LED04, counter40%2 );  
}
void in_100ms(){
}

void in_1000ms( ){
  //  digitalWrite( STATUS_LED03, counter1000%2 );
  if(counter100 % 4 == 1){
    unsigned int srednia = readDistance0();
    //send2android( String("RET VAL DISTANCE0 ") +srednia );
  }else if(counter100 % 4 == 2){    
    unsigned int ble = analogRead(A0);
    send2android( String("RET VAL ANALOG0 ") +ble);
  }else if(counter100 % 4 == 3){
    unsigned int srednia = readDistance1();
    //    send2android( String("RET VAL DISTANCE1 ") +srednia);
  }
}

unsigned int readDistance0(){
  return ultrasonic0.ping_median( DIST_REPEAT ); // Do multiple pings (default=5), discard out of range pings and return median in microseconds. 
}
unsigned int readDistance1(){
  return ultrasonic1.ping_median( DIST_REPEAT ); // Do multiple pings (default=5), discard out of range pings and return median in microseconds. 
}

long decodePosition( int axis, String input, int odetnij ){
  long pos = 0;
  if(odetnij>0){
    input = input.substring(odetnij);    // obetnij SET X lub SET Y, lub SEY Z, zostaje np "343" lub "+432"
  }
  if( input.startsWith("+") || input.startsWith("-")){                     // relatywnie + lub - czyli oddal o pozana pozycje od aktualnej
    long diff = input.substring(1).toInt();          // +
    if( input.startsWith("-")){
      diff = - diff;                                 // -
    }
    if( axis == AXIS_X){
      pos = stepperX.targetPosition() + diff * STEPPER_X_MUL;
    }
    else if( axis == AXIS_Y){
      pos = stepperY.targetPosition() + diff * STEPPER_Y_MUL;
    }
    else if( axis == AXIS_Z){
      // generalnie niemozliwe
    }
  }
  else{                                 // dokladna pozycja 
    pos = input.toInt();    // pozycja sprzętowa jest oddalona o margin_x_min od pozycji programowej
    if( axis == AXIS_X){
      pos = margin_x_min + pos * STEPPER_X_MUL;
    }
    else if( axis == AXIS_Y){
      pos = margin_y_min + pos * STEPPER_Y_MUL;
    }
    else if( axis == AXIS_Z){
      pos = pos;
    }    
  }
  return pos;
}
long decodeInt(String input, int odetnij ){
  long pos = 0;
  if(odetnij>0){
    input = input.substring(odetnij);    // obetnij SET SPEEDX lub SET SPEEDY, lub SEY SPEEDZ, zostaje np "343" lub "+432"
  }
  pos = input.toInt();
  return pos;
}

void parseInput( String input ){   // zrozum co sie dzieje
  input.trim();
  send2debuger( "COMMAND", input );
  bool defaultResult = true;

  if ( input.startsWith("SET") ) {      // tutaj niektore beda synchroniczne inne asynchroniczne wiec czasem zwracaj RET, a czasem dopiero po zakonczeniu
    if( input.startsWith("SET LED") ){    // zapal LEDa o numerze 1-9
      unsigned int pin = input.substring(7).toInt();      // "SET LED4 ON" na "4 ON"
      if( pin == 1 ){          
        pin = STATUS_LED01;
      }else if( pin == 2 ){    
        pin = STATUS_LED02;
      }else if( pin == 3 ){    
        pin = STATUS_LED03;
      }else if( pin == 4 ){    
        pin = STATUS_LED04;
      }else if( pin == 5 ){    
        pin = STATUS_LED05;
      }else if( pin == 6 ){    
        pin = STATUS_LED06;
      }else if( pin == 7 ){    
        pin = STATUS_LED07;
      }else if( pin == 8 ){    
        pin = STATUS_LED08;
      }else if( pin == 9 ){    
        pin = STATUS_LED09;
      }else{
        pin = 0;
      }
      if(pin != 0 ){
        if( input.endsWith("ON") ){
          digitalWrite(pin, HIGH );      
        }
        else{
          digitalWrite(pin, LOW );
        }
      }
    }else if( input.startsWith("SET X") ){
      long pos = decodePosition( AXIS_X, input, 6 );
      last_stepper_operation = true;
      lock_system = true;
      encoder_x = 0;
      stepperX.moveTo(pos);
      defaultResult = false;
    } else if( input.startsWith("SET Y") ){
      long pos = decodePosition( AXIS_Y, input, 6 );
      lock_system = true;
      encoder_y = 0;
      last_stepper_operation = true;
      stepperY.moveTo(pos);
      defaultResult = false;
    }else if( input.startsWith("SET SPEEDX") ){
      long val = decodeInt( input, 11 );  // 10 znakow i spacja
      send2android("SPEED " + String(val));
      stepperX.setMaxSpeed(val);    // lewo prawo
    }else if( input.startsWith("SET SPEEDY") ){
      long val = decodeInt( input, 11 );
      stepperY.setMaxSpeed(val);      // wgląb

      //    }else if( input.startsWith("SET SPEEDZ") ){
      //      long val = decodeInt( input, 11 );
      //?      stepperZ.setMaxSpeed(val);    // góra dol

    } else if( input.startsWith("SET ACCX") ){
      long val = decodeInt( input, 9 );    // SET ACCX i spacja
      send2android("ACC " + String(val));
      stepperX.setAcceleration(val);
      acc_x = val;
    }else if( input.startsWith("SET ACCY") ){      
      long val = decodeInt( input, 9 );    // SET ACCY i spacja
      stepperY.setAcceleration(val);

      //    }else if( input.startsWith("SET ACCZ") ){
      //      long val = decodeInt( input, 9 );    // SET ACCZ i spacja
      //      stepperZ.setAcceleration(val);      
    }else if( input.startsWith("MIX ") ){      // MIX 245
      long czas = decodeInt( input, 4 );        // SET MIX i spacja
      mieszaj(czas);
    }else{
      send2android("NO COMMAND" );
      defaultResult = false;  
    }
  }else if( input.startsWith("GET")) {
    if( input == "GET STATUS" ){
      if( status == STATE_BUSY ){
        send2android("VAL BUSY");
      }
      else if( status == STATE_READY ){
        send2android("VAL READY");
      }
      else if( status == STATE_ERROR ){
        send2android("VAL ERROR");
      }
      else if( status == STATE_INIT ){
        send2android("VAL INIT");
      }
    }else if( input == "GET CARRET" ){      // pozycja karetki x,y
      ret_current_position();
    }else if( input == "GET GLASS" ){       // waga szklanki
      long unsigned int waga = read_szklanka();
      send2android("GLASS " + String(waga));
    }else if( input == "GET WEIGHT" ){       // waga butelek (do 15 liczb nieujemnych)
      String res = "";
      for (byte count=0; count<16; count++) {              // 16 razy
        long unsigned waga = read_butelka(count);
        res = res + String(waga);    // odczytaj wartosc
        if(count<15){
          res = res + ',';        
        }
      }
      send2android( "WEIGHT " +res);    
    }else{
      send2android("NO COMMAND" );
      defaultResult = false;  
    }
    defaultResult = false;
  }
  else if( input.startsWith("IRR") ){
    if( input.startsWith("IRR STOP") ){    // zatrzymaj wszystko, ale zamknij dozownik plynow, zatrzymaj silniki
      defaultResult = false;
      status = STATE_BUSY; 
      status = STATE_READY;               // a po zakonczeniu zatrzymywania
    }
  } else if( input == "READY" ){      // odeślij READY
    send_ret = true;
    defaultResult = false;
    ret_current_position();   
  }else if( input == "NORET" ){
    send_ret = false;
  }else if( input == "PING" ){      // odeslij PONG
    send2android("PONG");
    defaultResult = false;
  }else if( input.startsWith("ANDROID ") ){    // zwrotka, nic nie rób
    defaultResult = false;
  }else if( input == "PONG" ){     // nic, to byla odpowiedz na moje PING
    defaultResult = false;
  }else if( input.startsWith("FILL ") ){
    long ilosc = decodeInt( input, 5 );  // FILL i spacja
    nalej( ilosc );    
  }else if( input == "KALIBRUJX" ){
    stosPush("NORET");
    stosPush("SET X " + String( - XLENGTH));
    stosPush("SET X " + String( XLENGTH ));
    stosPush("SET X 0");
    stosPush("READY");
  }else if( input == "KALIBRUJY" ){
    stosPush("NORET");
    stosPush("SET Y " + String( - 700));
    stosPush("SET Y " + String( 700 ));
    stosPush("SET Y 0");
    stosPush("READY");
  }else if( input == "KALIBRUJZ" ){  
    z_down();
  }else if( input == "MACHAJX" ){       // wykonaj 10 ruchów 300 - 1000
    stosPush("NORET");
    for(byte f = 0;f<10;){
      stosPush("SET X 300");
      stosPush("SET X 1000");
      f=f+2;
    }
    stosPush("READY" );
  }else if( input == "MACHAJY" ){       // wykonaj 10 ruchów 300 - 1000
    stosPush("NORET");
    for(byte f = 0;f<LOS_MAX;){
      stosPush("SET Y 50");
      stosPush("SET Y 400");
      f=f+2;
    }
    stosPush("READY");
  }else if( input == "LOSUJX" ){       // wykonaj 10 losowych ruchów po X
    stosPush("NORET");
    for(byte f = 0;f<LOS_MAX;){
      stosPush("SET X " + String(random(0,7) * 100));// zawsze po przeciwnych stronach
      stosPush("SET X " + String(random(8,16) * 100));
      f=f+2;
    }
    stosPush("READY");
  }else if( input == "LOSUJY" ){       // wykonaj 10 losowych ruchów po X
    stosPush("NORET");
    for(byte f = 0;f<LOS_MAX;){
      stosPush("SET Y " + String(random(0,3) * 100));// zawsze po przeciwnych stronach
      stosPush("SET Y " + String(random(3,6) * 100));
      f=f+2;
    }
    stosPush("READY");
  }else{
    send2android("NO COMMAND" );
    defaultResult = false;  
  }
  if(defaultResult && send_ret ){
    send2android("RET " + input );
  }
}

void ret_current_position(){ 
    long int posx = (stepperX.currentPosition() +  margin_x_min) /STEPPER_X_MUL;
    long int posy = (stepperY.currentPosition() +  margin_y_min) /STEPPER_Y_MUL;    // to jest pozycja skrajna
    long int posz = 0;
    String ret ="READY AT " + String(posx) + "," + String(posy)+ "," + String(posz); 
    send2android(ret);
}

//void on_int1R(){  int1_value = HIGH;}   // pin 3      // Krańcowy Z na wcisniecie
void on_int1F(){ 
  int1_value = HIGH;
}   // pin 3    // Krańcowy Z na puszczenie
// attachInterrupt( INT1, on_int1R, FALLING);   // nasłuchuj zmiany PIN 3    // Krańcowy Z na puszczenie

void z_down(){   // zajedz silnikiem Z na dół
  int1_value = LOW;
  servoZ.attach(STEPPER_Z_PWM);                  // przypisz do pinu, uruchamia PWMa
  servoZ.write(SERVOZ_UP);                       // jedź troche w górę
  delay(100);
  servoZ.write(SERVOZ_DOWN);       // a potem jedz w dol
  attachInterrupt( INT1, on_int1F, FALLING);   // nasłuchuj zmiany PIN 3    // Krańcowy Z (dolny) na wcisniecie daje zero
  delay(100);                                  // zjedz kawałek zanim znow podlacze przerwania
  int1_value = LOW;
  while(int1_value == LOW){                    // az dotknie krańcówki dolnej
    //      send2debuger( "irr", "test " + String(aaa) );
    delay(20);
  }
  servoZ.detach();                             // odetnij sterowanie
  detachInterrupt(INT1);                      // odlacz przerwanie, jestem na dole
}
void nalej( long czas ){   // nalej cieczy przez tyle czasu
  int1_value = LOW;
  pinMode(PIN3, INPUT);
  servoZ.attach(STEPPER_Z_PWM);                   // przypisz do pinu, uruchamia PWMa
  servoZ.write(SERVOZ_UP);            // jedź aż na górę
  attachInterrupt( INT1, on_int1F, FALLING);    // nasłuchuj zmiany PIN 3    // Krańcowy Z (gorny) na wcisniecie daje zero
  while( int1_value == LOW ){                   // az na górze
    delay(20);                                    // todo zabezpieczenie gdy po 5 sek nadal nie jest na górze
  }
  detachInterrupt(INT1);
  servoZ.write(SERVOZ_STAYUP);     // trzymaj na górze w tą mocą
  send2debuger( "fill", "czekam " + String(czas) );
  delay(czas);                                 // trzymaj na górze tyle czasu 
  servoZ.write(SERVOZ_DOWN);       // a potem jedz w dol
//  attachInterrupt( INT1, on_int1F, FALLING);   // nasłuchuj zmiany PIN 3    // Krańcowy Z (dolny) na wcisniecie daje zero
//  delay(100);                                  // zjedz kawałek zanim znow podlacze przerwania
//  int1_value = LOW;
///  int aaa = 0;

  delay( 1000 );
  /*
  while(int1_value == LOW){                    // az dotknie krańcówki dolnej
    send2debuger( "irr", "test " + String(aaa) );
    delay(20);
    aaa++;
  }
  detachInterrupt(INT1);
    */
  servoZ.detach();                             // odetnij sterowanie
}

void mieszaj2( long czas ){
  servoZ.attach(STEPPER_Z_PWM);  // attaches the servo on pin  to the servo object 
  servoZ.writeMicroseconds(SERVOZ_UP);
  delay(200);
  int1_value = 0;
  int licznik = 0;
  while( int1_value != 2 && licznik < 200){                   // az na górze  
    Serial.println("gora");
    licznik++;
    delay(20);
  }
  servoZ.writeMicroseconds(SERVOZ_STAYUP);  // trzymaj
  Serial.println("czekam");
  delay(5000);
  servoZ.writeMicroseconds(SERVOZ_DOWN);  // dół
  int1_value = 0;
  Serial.println("dol");
  licznik = 0;
  while( int1_value != 2 && licznik < 200){                   // az na górze  
    Serial.println("dol++");
    licznik++;
    delay(20);
  }
  servoZ.detach();
}

void mieszaj( long czas ){   // mieszaj tyle czasu
  // zjedź karetką
  // zajedź mieszadełkiem
  // mieszaj
  delay(czas);
  // wyjedz mieszadełkiem
}

void readSerial0Input( String input ){      // odbierz wejscie z serial0
  parseInput( input );                      // parsuj wejscie
  if(DEBUG_SERIAL_INPUT){
    send2debuger( "READ0", input );
  }
  send2android(input);
}

void readSerial3Input( String input ){       // odebralem z BT od androida
  if( USE_BT ){ 
    parseInput( input );          // parsuj wejscie
  }
  if(DEBUG_BT_INPUT){
    send2debuger( "READ3", input);
  }
}
long int max_x_pos = -1;
long int max_y_pos = -1;

void readAndroidInput( String input ){    // odbierz przez ADB z androida
  if(DEBUG_ADB_INPUT){
    send2debuger("READ_ADB",input);  
  }
  parseInput( input );          // parsuj wejscie
}
boolean irrx_handled = true;
boolean irrx_max = false;
boolean irrx_min = false;
boolean irry_handled = true;
int dlugoscx = 0;
int dlugoscy = 0;
int lastx_min = 0;
int lastx_max = 1;
int lasty_min = 0;
int lasty_max = 1;

int irry_min = 0;
int irry_max = 1;

boolean irrx  = HIGH;
boolean irry  = HIGH;

void run_steppers(){    // robione w każdym przebiegu loop
  long int distx = stepperX.distanceToGo();
  long int disty = stepperY.distanceToGo();

  if( distx == 0 && disty == 0 ){    //  zajechalem 
    lock_system = false;
    if( last_stepper_operation ){    // byla komenda
      stepperX.run();
      stepperY.run();
      stepperX.stop();
      stepperY.stop();
      stepperX.disableOutputs();
      stepperY.disableOutputs();
      if(send_ret){
        ret_current_position();    // wyslij ze skonczylem
      }
      last_stepper_operation = false;
    }
  }
  else{
    irrx  = digitalRead( IRRX_PIN );
    if( irrx == LOW ){
      if( irrx_min && distx < 0 ){    // stoje na dole a mam jechac w dół
        stepperX.stop();
        stepperX.disableOutputs();
        send2debuger("irrx-","nigdzie nie jade: " + String(distx));
      }
      if( irrx_max && distx > 0 ){    // stoje na górze a mam jechac w góre
        stepperX.stop();
        stepperX.disableOutputs();
        send2debuger("irrx+","nigdzie nie jade: " + String(distx));
      }
    }
    if( irrx == LOW && !irrx_handled) {      // bylo przerwanie X
      if( distx < 0 ){           // jechalem w dół
        stepperX.setCurrentPosition(10);            // tu jest 0, reset adresowania
        lastx_min = stepperX.currentPosition();    // to jest pozycja skrajna
        stepperX.stop();
        stepperX.disableOutputs();
        irrx_min = true;
        dlugoscx = lastx_max - lastx_min;
        send2debuger("irrx MIN", String(lastx_min)+ " dlugosc:" + String(dlugoscx));
        send2android("LENGTHX " +  String(dlugoscx) );
      }else{
        max_x_pos = stepperX.currentPosition();    // to jest pozycja skrajna
        irrx_max = true;
        lastx_max = max_x_pos;
        dlugoscx = lastx_max - lastx_min;
        send2debuger("irrx MAX",String(max_x_pos)+ " dlugosc:" + String(dlugoscx) );
        send2android("LENGTHX " +  String(dlugoscx) );
        stepperX.setAcceleration(100000);
        stepperX.moveTo(max_x_pos);
        stepperX.stop();
        stepperX.setAcceleration(acc_x);        
        stepperX.disableOutputs();
      }
      irrx_handled = true;
    }else if(distx != 0){
      stepperX.run();
    }

    if(irrx == HIGH && irrx_handled ){    // czyli koniec obslugi stanu LOW
      send2debuger("irrx","HIGH-----------");
      irrx_handled  = false;
      irrx_min  = false;
      irrx_max  = false;
    }

    irry  = digitalRead( IRRY_PIN );
    if( irry == LOW ){
      if( irry_min && disty < 0 ){    // stoje na dole a mam jechac w dół
        stepperY.stop();
        stepperY.disableOutputs();
        send2debuger("irry-","nigdzie nie jade: " + String(disty));
      }
      if( irry_max && disty > 0 ){    // stoje na górze a mam jechac w góre
        stepperY.stop();
        stepperY.disableOutputs();
        send2debuger("irry+","nigdzie nie jade: " + String(disty));
      }
    }
    if( irry == LOW && !irry_handled) {      // bylo przerwanie X
      if( disty < 0 ){           // jechalem w dół
        stepperY.setCurrentPosition(10);            // tu jest 0, reset adresowania
        lasty_min = stepperY.currentPosition();    // to jest pozycja skrajna
        stepperY.stop();
        stepperY.disableOutputs();
        irry_min = true;
        dlugoscy = lasty_max - lasty_min;
        send2debuger("irry MIN", String(lasty_min)+ " dlugosc:" + String(dlugoscy));
        send2android("LENGTHY " +  String(dlugoscy) );
      }else{
        max_y_pos = stepperY.currentPosition();    // to jest pozycja skrajna
        irry_max = true;
        lasty_max = max_y_pos;
        dlugoscy = lasty_max - lasty_min;
        send2debuger("irry MAX", String(max_y_pos)+ " dlugosc:" + String(dlugoscy) );
        send2android("LENGTHY " +  String(dlugoscy) );
        stepperY.setAcceleration(100000);
        stepperY.moveTo(max_y_pos);
        stepperY.stop();
        stepperY.setAcceleration(acc_y);        
        stepperY.disableOutputs();
      }
      send2android("IRRY");
      irry_handled = true;
    }else if(disty != 0){
      stepperY.run();
    }
    if(irry == HIGH && irry_handled ){   // czyli koniec obslugi stanu LOW
      send2debuger("irry","HIGH-----------");
      irry_handled  = false;
      irry_min  = false;
      irry_max  = false;
    }
  }
}

// BILBIOTEKI i obsługi EVENTow
String stosPop(){        // pobierz ze stosu
  // delimiter to nowa linia: SEPARATOR_CHAR
  int c          = stos.indexOf( SEPARATOR_CHAR );
  if(c >0 ){
    String res     = stos.substring(0,c);
    stos           = stos.substring(c+1);
    stos_length--;
    return res;
  }
  else if(stos_length){
    send2debuger( "NOENTER", stos );
    stos_length = 0;
    return stos;  
  }
  else{
    stos_length = 0;
    return ""; 
  }
}
void stosPush( String in ){       // dodaj do stosu
  stos  = stos + in + String(SEPARATOR_CHAR);
  stos_length++;
}
void stosClear(){        // pobierz ze stosu
  stos  = "";
  stos_length = 0;
}

void send2android( String output ){      // wyslij string do androida
  String output2 = output + SEPARATOR_CHAR;
  unsigned int len = output2.length();  // długosc komunikatu
  if( USE_ADB && adb_ready ){
    char *cstr = new char[output.length() + 1];
    strcpy(cstr, output.c_str());
    cstr[output.length()] = '\0';
    connection->writeString( cstr );
    delete [] cstr;
    if(DEBUG_ADB2ANDROID){
      send2debuger( "2ANDROID ADBSEND", output );
    }
  }
  if( USE_BT ){
    if(DEBUG_OUTPUT2ANDROID){
      send2debuger( "2ANDROID BTSEND", output );
    }
    Serial3.print( output2 );    // nie println bo juz dodaj separator
  }else if(DEBUG_OUTPUT2ANDROID){
    if( !adb_ready || !USE_ADB ){
      send2debuger( "2ANDROID NOSEND", output );
    }
  }
}
// Event handler for the shell connection. 
// This event handler is called whenever data is sent from Android Device to Seeeduino ADK.
// Any data / command to be sent to I/O of ADK has to be handled here.

void adbEventHandler(Connection * connection, adb_eventType event, uint16_t length, uint8_t * data){
  if (event == ADB_CONNECTION_RECEIVE) {
    String res = "";
    for (int i=0; i<length; i++) {
      res += (char) data[i];
    }
    send2debuger( "ADB_RES", "ADB_RECEIVE");
    readAndroidInput(res);
  }else if (event == ADB_CONNECTION_FAILED) {
    send2debuger( "ADB_RES", "ADB_CONNECTION_FAILED");
    adb_ready = false;
  }else if (event == ADB_CONNECTION_OPEN) {
    send2debuger( "ADB_RES", "ADB_CONNECTION_OPEN");
    adb_ready = true;
  }else if (event == ADB_CONNECTION_CLOSE) {
    send2debuger( "ADB_RES", "ADB_CONNECTION_CLOSE");
    adb_ready = false;
  }else if (event == ADB_CONNECT) {
    send2debuger( "ADB_RES", "ADB_CONNECT");
  }else if (event == ADB_DISCONNECT) {
    send2debuger( "ADB_RES", "ADB_DISCONNECT");
    adb_ready = false;
  }else{
    send2debuger( "ADB_RES", "SOME EVENT");  
  }
}

void send2debuger( String ns, String logstr ){
  if( DEBUG_OVER_BT ){
    Serial3.println("DEBUG "+ ns +": [" + logstr + "]");
  }
  if(DEBUG_OVER_SERIAL){
    Serial.println("DEBUG "+ ns +": [" + logstr + "]");
  }
}

void serialEvent(){                       // FUNKCJA WBUDOWANA - zbieraj dane z serial0 i serial3 i skadaj w komendy
  while (Serial.available() && !Console0Complete) {    // odczytuj gdy istnieja dane i poprzednie zostaly odczytane
    char inChar = (char)Serial.read(); 
    serial0Buffer += String(inChar);
    if (inChar == SEPARATOR_CHAR) {
      Console0Complete = true;
    }
  }
}

void serialEvent3(){                       // FUNKCJA WBUDOWANA - zbieraj dane z serial0 i serial3 i skadaj w komendy
  if( USE_BT || DEBUG_OVER_BT ){
    while (Serial3.available() && !Console3Complete){   // odczytuj gdy istnieja dane i poprzednie zostaly odczytane
      char inChar = (char)Serial3.read(); 
      serial3Buffer += String(inChar);
      if (inChar == SEPARATOR_CHAR || inChar == 13 ) {
        Console3Complete = true;
      }  
    }
  }
}

// obluga wag
float read_szklanka(){
  return read_butelka( 0 );    // kieliszek jest jako waga 0
}

unsigned long read_butelka(byte numer){
  digitalWrite(PIN_MADDR0, bitRead(numer,0) );      //   // Ustaw numer na muxach, wystaw adres
  digitalWrite(PIN_MADDR1, bitRead(numer,1) );
  digitalWrite(PIN_MADDR2, bitRead(numer,2) );
  digitalWrite(PIN_MADDR3, bitRead(numer,3) );
  unsigned long rr = 0;
  delay(MULTI_ADDR_TIME);
  long unsigned int j=0;
  int count = 1<<MULTI_READ_COUNT;
  for( j=0; j<count ;j++){
    rr+= analogRead(PIN_WAGA_ANALOG);
  }
  delay(MULTI_READ_TIME);
  return rr >>MULTI_READ_COUNT;
}

unsigned long int maplong(unsigned long x, unsigned long in_min, unsigned long in_max, unsigned long out_min, unsigned long out_max){
  return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
}

