#include <AccelStepper.h>
#include "config.cpp";
#include <NewPing.h>
#include <SPI.h>
#include <Adb.h>
#include <Servo.h>

// stan uC
byte status = STATE_INIT;      // na pocztku jest w trakcje inicjacji
// koniec stanu uC
unsigned int waga_zero = 10;        // przy tej wadze uznaje ze nic nie stoi na wadze
byte version = 1;

volatile bool was_interrupt = LOW;    // przerwania połączona spójnikiem OR
volatile bool int0_value = LOW;
volatile bool int1_value = LOW;
volatile bool int2_value = LOW;
volatile bool int3_value = LOW;
//volatile bool int4_value = LOW;
//volatile bool int5_value = LOW;

volatile unsigned long encoder_diff_x = 0;    // przejechane przez enkoder
volatile unsigned long encoder_x = 0;         // pozycja enkodera (nieminusowa, volatile bo uzywana w pezrwaniach)
volatile unsigned long encoder_diff_y = 0;    // przejechane przez enkoder
volatile unsigned long encoder_y = 0;         // pozycja enkodera (nieminusowa, volatile bo uzywana w pezrwaniach)
boolean read_waga =false;

// obsluga zrodel wejscia
Connection * connection;        // Adb connection.
boolean adb_ready = false;
boolean bt_ready = true;
String serial0Buffer = "";
String serial3Buffer = "";
boolean Console0Complete = false;   // This will be set to true once we have a full string
boolean Console3Complete = false;   // This will be set to true once we have a full string
// koniec obsluga zrodel wejscia

// http://code.google.com/p/arduino-new-ping/wiki/Using_NewPing_Syntax
NewPing ultrasonic0(PIN_ULTRA0_TRIG, PIN_ULTRA0_ECHO, MAX_DISTANCE);
NewPing ultrasonic1(PIN_ULTRA1_TRIG, PIN_ULTRA1_ECHO, MAX_DISTANCE);

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
    String btdn = "AT+NAME" + String(BT_DEV_NAME);
    Serial3.print( btdn );
    delay(1000);
    Serial3.println( "" );    // reset jesli nie idzie do bufora...
    Serial3.println( "PING" );    // reset jesli nie idzie do bufora...
    Serial3.print( "\n" + String(SEPARATOR_CHAR) );
  }
}

#if SERVOX4PIN==true
AccelStepper stepperX(8, STEPPER_X_STEP0, STEPPER_X_STEP1, STEPPER_X_STEP2, STEPPER_X_STEP3 );
#else
AccelStepper stepperX(1, STEPPER_X_STEP, STEPPER_X_DIR);      // Step, DIR
#endif

#if SERVOY4PIN==true
AccelStepper stepperY(8, STEPPER_Y_STEP0, STEPPER_Y_STEP1, STEPPER_Y_STEP2, STEPPER_Y_STEP3 );
#else
AccelStepper stepperY(1, STEPPER_Y_STEP, STEPPER_Y_DIR);// Step, DIR
#endif

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

void setupSteppers(){                  // uzyte w setup()
  stepperX.setPinsInverted( false, false, true ); // enable pin invert
  stepperY.setPinsInverted( false, false, true ); // enable pin invert
  stepperX.disable_on_ready = STEPPERX_READY_DISABLE;
  stepperY.disable_on_ready = STEPPERY_READY_DISABLE;
  #if SERVOX4PIN==true
  #else
    stepperX.setEnablePin(STEPPER_X_ENABLE);
  #endif

  #if SERVOY4PIN==true
  #else
    stepperY.setEnablePin(STEPPER_Y_ENABLE);
  #endif

  if(STEPPERX_READY_DISABLE){
    stepperX.disableOutputs();
  }
  if(STEPPERY_READY_DISABLE){
    stepperY.disableOutputs();
  }

  stepperX.setAcceleration(ACCELERX);    // lewo prawo
  stepperX.setMaxSpeed(SPEEDX); 

  stepperY.setAcceleration(ACCELERY);    // wgląb
  stepperY.setMaxSpeed(SPEEDY);
  
  
  // zjedz na dół stepperem Z
  
  
}

void setupUltrasonic(){      // uzyte w setup()
}

void setupADB(){      // uzyte w setup()
  if( USE_ADB ){    
    delay(500);
    ADB::init();
    delay(500);
    connection = ADB::addConnection("tcp:4567", true, adbEventHandler);
    delay(500);
    send2debuger( "ADB", "Po ADB" );
  }
}
// Ustawienia Przerwań
void setupInts(){      // uzyte w setup()
//  pinMode(INT5, INPUT); 
  //pinMode(INT4, INPUT);

//  digitalWrite(INT5, HIGH); //turn pullup resistor on
  //    digitalWrite(INT4, HIGH); //turn pullup resistor on

  //  attachInterrupt( INT0, on_int0R, RISING);    // nasłuchuj zmiany PIN 2
  //  attachInterrupt( INT0, on_int0F, FALLING);    // nasłuchuj zmiany PIN 2

  //  attachInterrupt( INT2, on_int2R, RISING);    // nasłuchuj zmiany PIN 21    // Krańcowy Y
  //  attachInterrupt( INT2, on_int2F, FALLING);    // nasłuchuj zmiany PIN 21   // Krańcowy Y

  //  attachInterrupt( INT3, on_int3R, RISING);     // nasłuchuj zmiany PIN 20   // Krańcowy X
  //  attachInterrupt( INT3, on_int3F, FALLING);    // nasłuchuj zmiany PIN 20  // Krańcowy X

//  attachInterrupt( INT4, on_int4R, CHANGE);    // nasłuchuj zmiany PIN 19  // Enkoder X
  //  attachInterrupt( INT4, on_int4F, FALLING);    // nasłuchuj zmiany PIN 19 // Enkoder X

//  attachInterrupt( INT5, on_int5R, CHANGE);    // nasłuchuj zmiany PIN 18 // Enkoder Y
  //  attachInterrupt( INT5, on_int5F, FALLING);    // nasłuchuj zmiany PIN 18// Enkoder Y
}

void on_int0R(){    int0_value = HIGH;  was_interrupt = HIGH;}    // pin 2
void on_int0F(){    int0_value = HIGH;  was_interrupt = HIGH;}    // pin 2
void on_int2R(){    int2_value = HIGH;  was_interrupt = HIGH;}    // pin 21       // Krańcowy Y rise
void on_int2F(){    int2_value = HIGH;  was_interrupt = HIGH;}    // pin 21       // Krańcowy Y fall
void on_int3R(){    int3_value = HIGH;  was_interrupt = HIGH;}    // pin 20       // Krańcowy X rise
void on_int3F(){    int3_value = HIGH;  was_interrupt = HIGH;}    // pin 20       // Krańcowy X fall
void on_int4R(){    encoder_x++;   encoder_diff_x++;digitalWrite( STATUS_LED01, encoder_x%2);} 
// pin 19       // Enkoder X
//void on_int4F(){  }    // pin 19                       // Enkoder X
void on_int5R(){    encoder_y++;  encoder_diff_y++;}
// pin 18       // Enkoder Y
//void on_int5F(){  }    // pin 18                       // Enkoder Y
// Koniec Ustawienia Przerwań

void afterSetup(){ 
  send2debuger("INIT", "START ready");
  //NewPing::timer_ms(20, in_20ms);   // Create a Timer2 interrupt that calls func
  //NewPing::timer_ms(40, in_40ms);   // Create a Timer2 interrupt that calls func
  //NewPing::timer_ms(100, in_100ms);   // Create a Timer2 interrupt that calls func
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

Servo servoZ;
long int margin_x = 0;          // odstępstwo od technicznej pozycji X
long int margin_y = 0;          // odstępstwo od technicznej pozycji Y
long int dlugosc_x = 0;
long int dlugosc_y = 0;
int dlugosc_z = 100;
boolean lock_system = false;    // czy wykonywać tylko ruch silnikami
boolean send_ret = true;        // czy wysyłać odpowiedzi po wykonaniu komend
boolean last_stepper_operation = false; 
boolean irr_handled_x = true;
boolean irr_min_x = false;          //czy jestem na dole?
boolean irr_max_x = false;
boolean irr_handled_y = true;
boolean irr_min_y = false;          //czy jestem na dole?
boolean irr_max_y = false;
boolean irr_min_z = false;          //czy jestem na dole?
boolean irr_max_z = false;
boolean irr_x  = HIGH;              // czy dotykam przerwania
boolean irr_y  = HIGH;              // czy dotykam przerwania

unsigned int acc_x = ACCELERX;
unsigned int acc_y = ACCELERY;
unsigned int max_speed_x = SPEEDX;
unsigned int max_speed_y = SPEEDY;

unsigned int last_max_x = XLENGTH;
unsigned int last_max_y = YLENGTH;
unsigned long counter100 = 0, counter1000 = 0;
unsigned long milis40 = 0, milis100 = 0, milis1000 = 0;
unsigned long mil = 0;
String stos = "";
unsigned int stos_length = 0;
boolean is_connected = false;

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
  /*
  if(  mil > milis40 + 40 ){      // 25 razy na sek
    in_40ms();
    milis40 = mil;
  }*/
  if( read_waga && mil > milis100 + 50 ){      // 20 razy na sek
    in_100ms();
    milis100 = mil;
  }
  if( mil > milis1000 + 3000 ){      // 1 razy na sek
    in_3000ms();
    counter1000++;
    milis1000 = mil;
  }
  if( stos_length ){// gdy odblokuje sie lock_system to wykonaj nastepne polecenie
    String runcmd = stosPop();
    send_current_position( false );                   // pewnie przemieszczałem się wiec wyslij do androida gdzie jestem
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
      is_connected      = true;
      Console3Complete = false;
      serial3Buffer = "";
    }
  }
  if( USE_ADB ){
    ADB::poll();      // Poll the ADB subsystem.
  }
}

void in_100ms(){
  long unsigned int waga = read_szklanka();
  send2android("GLASS " + String(waga));
}

void in_3000ms(){
  //  digitalWrite( STATUS_LED03, counter1000%2 );
//  send2android( "PING2ANDROID" );
  if(counter100 % 4 == 1){
    unsigned int srednia = readDistance0();
    //send2android( String("RET VAL DISTANCE0 ") +srednia );
  }else if(counter100 % 4 == 2){    
    unsigned int ble = analogRead(A0);
   // send2android( String("VAL ANALOG0 ") +ble);
  }else if(counter100 % 4 == 3){
    unsigned int srednia = readDistance1();
    //    send2android( String("VAL DISTANCE1 ") +srednia);
  }
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
      pos = target_posx() + diff;
//      send2debuger( "decodePosition X", "target(" + String(target_posx()) + ") T(" + String(stepperX.currentPosition()) + ") diff:" + String(diff)+ ") L(" + String(pos)+")");
    }else if( axis == AXIS_Y){
      pos = target_posy() + diff;
    }else if( axis == AXIS_Z){
      // generalnie niemozliwe
    }
  }else{                    // dokladna pozycja 
    pos = input.toInt();    // pozycja sprzętowa jest oddalona o margin_x od pozycji programowej  
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
      if( pin == 1 ){                  pin = STATUS_LED01;
      }else if( pin == 2 ){            pin = STATUS_LED02;
      }else if( pin == 3 ){            pin = STATUS_LED03;
      }else if( pin == 4 ){            pin = STATUS_LED04;
      }else if( pin == 5 ){            pin = STATUS_LED05;
      }else if( pin == 6 ){            pin = STATUS_LED06;
      }else if( pin == 7 ){            pin = STATUS_LED07;
      }else if( pin == 8 ){            pin = STATUS_LED08;
      }else if( pin == 9 ){            pin = STATUS_LED09;
      }else{                           pin = 0;
      }
      if(pin != 0 ){
        if( input.endsWith("ON") ){
          digitalWrite(pin, HIGH );      
        } else{
          digitalWrite(pin, LOW );
        }
      }
    }else if( input.startsWith("SET X") ){
      long pos = decodePosition( AXIS_X, input, 6 );
      last_stepper_operation = true;
      posx(pos);
      defaultResult = false;
    } else if( input.startsWith("SET Y") ){
      long pos = decodePosition( AXIS_Y, input, 6 );
      last_stepper_operation = true;
      posy(pos);
      defaultResult = false;
    }else if( input.startsWith("SET SPEEDX") ){
      max_speed_x = decodeInt( input, 11 );    // 10 znakow i spacja
      stepperX.setMaxSpeed(max_speed_x);    // lewo prawo
    }else if( input.startsWith("SET SPEEDY") ){
      max_speed_y = decodeInt( input, 11 );
      stepperY.setMaxSpeed(max_speed_y);      // wgląb
    } else if( input.startsWith("SET ACCX") ){
      acc_x = decodeInt( input, 9 );    // SET ACCX i spacja
      stepperX.setAcceleration(acc_x);
    }else if( input.startsWith("SET ACCY") ){      
      acc_y = decodeInt( input, 9 );    // SET ACCY i spacja
      stepperY.setAcceleration(acc_y);

    }else if( input.equals("SET Z MAX") ){
       if(!irr_max_z){
          int up_pos = SERVOZ_UP_POS;
          servoZ.writeMicroseconds(up_pos);             // na doł
          delay(SERVOZ_UP_TIME);
          irr_max_z = true;
          irr_min_z = false;
       } 
       send_current_position(true);
     }else if( input.startsWith("SET Z MIN") ){
       if(!irr_min_z){
          int down_pos = SERVOZ_DOWN_POS;
          servoZ.writeMicroseconds(down_pos);         // na doł
          delay(SERVOZ_DOWN_TIME);
          irr_max_z = false;
          irr_min_z = true;
       }
       send2android("LENGTHZ "+ String(dlugosc_z) );
       send_current_position(true);
     }else if( input.equals("SET Z ") ){
      int msec = decodeInt( input, 6 );    // 10 znakow i spacja
      send2debuger( "msec", "w gore1" );
      for(int keep = 0; keep < 1000; keep += 1)  {
        servoZ.writeMicroseconds(msec);
        delay(100);
      }

    }else{
      send2android("NO COMMAND [" + input +"]");
      defaultResult = false;
    }
  }else if( input.equals( "WAIT READY") ){      // tylko zwróc zwrotkę
 
  }else if( input.startsWith( "WAIT TIME ") ){      // odczekaj i jedz dalej RET WAIT TIME
    long wait = decodeInt( input, 10 );        // WAIT TIME i spacja
    delay(wait); 

  }else if( input.startsWith("WAIT GLASS ") ){
    long min_diff = decodeInt( input, 11 );        // WAIT GLASS i spacja
    byte res = wait4glass(min_diff);
    if(res == 0){    // nie ma szklanki i nie będzie
      send2android("ERROR "+ input);
      defaultResult = false;
    }
  }else if( input.startsWith("MIX ") ){       // MIX 245
    long czas = decodeInt( input, 4 );        // SET MIX i spacja
    mieszadlo_enable(czas);
    defaultResult = false;
  }else if( input.equals("LIVE WEIGHT ON") ){      // RET LIVE WEIGHT ON
    read_waga = true;
  }else if( input.equals("LIVE WEIGHT OFF") ){    // RET LIVE WEIGHT OFF
    read_waga = false;
  }else if( input.startsWith("GET")) {
    if( input == "GET SPEED" ){      // podaje prędkosci i akceleracje silnikow X i Y
        send2android("VAL SPEEDX " + String(max_speed_y) + ","+ String(acc_x) );
        send2android("VAL SPEEDY " + String(max_speed_x) + ","+ String(acc_y) );
    }else if( input.equals( "GET VERSION" )){
      send2android( "VAL VERSION " + String(version));
    }else if( input.equals( "GET CARRET" )){      // pozycja karetki x,y
      send_current_position( false );
    }else if( input == "GET GLASS" ){       // waga szklanki
      long unsigned int waga = read_szklanka();
      send2android("GLASS " + String(waga));
    }else if( input.equals( "GET WEIGHT") ){       // waga butelek (do 15 liczb nieujemnych)
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
      send2android("NO COMMAND [" + input +"]");
    }
    defaultResult = false;
  }else if( input.startsWith("IRR") ){
    if( input.startsWith("IRR STOP") ){    // zatrzymaj wszystko, ale zamknij dozownik plynow, zatrzymaj silniki
      defaultResult = false;
      status = STATE_BUSY; 
      status = STATE_READY;               // a po zakonczeniu zatrzymywania
    }
  } else if( input.equals( "READY") ){      // odeślij READY AS POS
    send_ret = true;
    defaultResult = false;
    send_current_position(true);
  }else if( input == "NORET" ){
    send_ret = false;
  }else if( input.equals("ENABLEX") ){
    stepperX.enableOutputs();
  }else if( input.equals("DISABLEX") ){
    if(STEPPERX_READY_DISABLE){
      stepperX.disableOutputs();
    }
  }else if( input.equals("ENABLEY") ){
    stepperY.enableOutputs();
  }else if( input.equals("DISABLEY") ){
    if(STEPPERY_READY_DISABLE){
      stepperY.disableOutputs();
    }
  }else if( input.equals("ENABLEZ") ){
    servoZ.attach(STEPPER_Z_PWM);                  // przypisz do pinu, uruchamia PWMa 
  }else if( input.equals("DISABLEZ") ){
    servoZ.detach();                               // odetnij sterowanie
  }else if( input.equals("PING2ARDUINO") ){      // odeslij PONG
    send2android("PONG");
    defaultResult = false;
  }else if( input.startsWith("ANDROID ") ){    // zwrotka, nic nie rób
    defaultResult = false;
  }else if( input.equals( "PONG" )){           // nic, to byla odpowiedz na moje PING
    defaultResult = false;
  }else if( input.startsWith("PACPAC") ){
    delay(SERVOZ_PAC_TIME_WAIT);
    send2debuger( "fill", "pac pac gora" );
    servoZ.writeMicroseconds(SERVOZ_PAC_POS);              // do góry
    delay(SERVOZ_PAC_TIME_UP);
    send2debuger( "fill", "pac pac dol" );
    servoZ.writeMicroseconds(SERVOZ_DOWN_POS);             // na doł
    delay(SERVOZ_PAC_TIME_DOWN);
    irr_min_y  = true;
    irr_max_y  = false;
    send_current_position(false);
  }else if( input.equals ("KALIBRUJ WAGA" )){       // wykonaj 10 losowych ruchów po X    
    waga_zero = read_szklanka();
    send2android("GLASS " + String(waga_zero));
    defaultResult = false;

  }else if( input.equals( "PING2ANDROID") ){      // nic nie rob
    defaultResult = false;
    

  }else if( input.equals("KALIBRUJX") ){
    // kaliberacja wagi razem z kalubracją X
    waga_zero = read_szklanka();    // TODO - wydzielić
    send2android("GLASS " + String(waga_zero));
    stosPush("NORET");
    stosPush("SET X " + String( - XLENGTH));
    stosPush("SET X " + String( XLENGTH ));
    stosPush("SET X 0");
    stosPush("READY");
  }else if( input.equals( "KALIBRUJY") ){
    stosPush("NORET");
    stosPush("SET Y " + String( - YLENGTH ));
    stosPush("SET Y " + String( YLENGTH ));
    stosPush("SET Y 0");
    stosPush("READY");
  }else{
    send2android("NO COMMAND [" + input +"]");
    defaultResult = false;  
  }
  if(defaultResult && send_ret ){
    send2android("RET " + input );
  }
}

// newPos = pozucja logiczna (zamieniana jest na tecniczną wewnątrz)
void posx( long newPos ){
//  send2debuger("osX","jade do:L(" + String(newPos)+") czyli T(" + String(newPos * STEPPER_X_MUL + margin_x)+ "). margines: " + String(margin_x));
  lock_system = true;
  encoder_diff_x = 0;
  stepperX.moveTo( newPos * STEPPER_X_MUL + margin_x );
}
void posy( long newPos ){
//  send2debuger("osY","jade do:L(" + String(newPos)+") czyli: T(" + String(newPos * STEPPER_Y_MUL + margin_x)+ "). margines: " + String(margin_y));
  lock_system = true;
  encoder_diff_y = 0;
  stepperY.moveTo( newPos * STEPPER_Y_MUL + margin_y );
}

long int posx(){
   return (stepperX.currentPosition() - margin_x) /STEPPER_X_MUL;
}
long int posy(){
   return (stepperY.currentPosition() - margin_y) /STEPPER_Y_MUL;
}
long int target_posx(){
   return (stepperX.targetPosition() - margin_x) /STEPPER_X_MUL;
}
long int target_posy(){
   return (stepperY.targetPosition() - margin_y) /STEPPER_Y_MUL;
}

long int posz(){
  if(irr_min_z){
   return 0;
  }else{
   return dlugosc_z;
  }
}

void send_current_position( boolean isReady ){ 
  if(isReady){
    send2android("RET READY AT " + String(posx()) + "," + String(posy())+ "," + String(posz()));
  }else{
    send2android("RET POS " + String(posx()) + "," + String(posy())+ "," + String(posz()));
  }
//  send2android("ENCODERS [" + String(encoder_diff_x) + "/" + String(encoder_x)+ "] [" + String(encoder_diff_y)+ "/" + String(encoder_y));
}

//void on_int1R(){  int1_value = HIGH;}   // pin 3      // Krańcowy Z na wcisniecie
void on_int1F(){ 
  int1_value = HIGH;
}   // pin 3    // Krańcowy Z na puszczenie
// attachInterrupt( INT1, on_int1R, FALLING);   // nasłuchuj zmiany PIN 3    // Krańcowy Z na puszczenie

byte wait4glass( int min_diff ){
  long unsigned waga = read_szklanka();
  send2android("GLASS " + String(waga));
  if( waga < waga_zero + min_diff ){          // rozni się o mniej niż WAGA_MIN_DIFF
    unsigned int repeat = 0;
    while( repeat < WAGA_REPEAT_COUNT && (waga < waga_zero + min_diff) ){          // powtarzaj WAGA_REPEAT_CONUT razy
      digitalWrite( STATUS_GLASS_LED, HIGH );        // zapal ze chce szklankę
      delay(400);
      digitalWrite( STATUS_GLASS_LED, LOW );         // zgaś
      delay(300);
      waga = read_szklanka();
      send2android("GLASS " + String(waga));
    }
    //jak juz jest ok to i tak poczekaj sekundę na usunięcie ręki wkłądającej szklankę
    delay(2000);
    if(waga < waga_zero + min_diff ){      //nadal jest za mało
      return 0;
    }
  }
  return 1;
}

void mieszadlo_enable( long czas ){   // mieszaj tyle czasu
  // zajedź mieszadełkiem
  // mieszaj
  delay(czas);
  // wyjedz mieszadełkiem
}

void run_steppers(){    // robione w każdym przebiegu loop
  long int dist_x = stepperX.distanceToGo();
  long int dist_y = stepperY.distanceToGo();

  if( dist_x == 0 && dist_y == 0 ){    //  zajechalem 
    lock_system = false;
    if( last_stepper_operation ){    // byla komenda
      stepperX.stopNow();
      stepperY.stopNow();
      if(send_ret){
        send_current_position(true);    // wyslij ze skonczylem
      }
//      send2debuger("steppers","koniec X:" + String(stepperX.distanceToGo()) + " Y:" + String(stepperY.distanceToGo()) + String(" margines_x ") + String(margin_x) );
      last_stepper_operation = false;
    }
  }else{
    irr_x  = digitalRead( IRRX_PIN );
    if( irr_x == LOW && dist_x != 0 ){
      if( irr_min_x && dist_x < 0 ){    // stoje na dole a mam jechac w dół
        stepperX.stopNow();
        send2debuger("osX MIN","jestem na dole i nie jade w dol roznica:" + String(dist_x)+" / R(" + String(stepperX.distanceToGo())+ ") / " + String(margin_x));
        delay(100);
        dist_x = 0;
      }
      if( irr_max_x && dist_x > 0 ){    // stoje na górze a mam jechac w góre
        stepperX.stopNow();
        send2debuger("osX MAX","jestem na górze i nie jade w gore roznica:" + String(dist_x)+" / R(" + String(stepperX.distanceToGo())+ ") / " + String(margin_x));
        dist_x = 0;
      }
    }
    if( irr_x == LOW && !irr_handled_x) {      // wciśnięte = bylo przerwanie X i jeszcze nie jest obsluzone
      if( dist_x < 0 ){                       // jechalem w dół
        margin_x = stepperX.currentPosition();                      // to jest pozycja skrajna
        stepperX.stopNow();
        irr_min_x = true;      // to jest minimum
        dlugosc_x = last_max_x;
        send2debuger("osX MIN", "dlugosc:" + String(dlugosc_x)+ " margines: " + String(margin_x)+ " jade: " + String(stepperX.distanceToGo()));
        send2android("LENGTHX " +  String(dlugosc_x) );
      }else if( dist_x > 0 ){             // jechalem w gore
        stepperX.stopNow();
        last_max_x = posx();   // to jest pozycja skrajna
        irr_max_x = true;
        dlugosc_x = last_max_x;
        send2debuger("osX MAX", "dlugosc:" + String(dlugosc_x)+ " margines: " + String(margin_x));
        send2android("LENGTHX " +  String(dlugosc_x) );
      }else{
        send2debuger("osX", "dist_x :" + String(dist_x)+ " margines: " + String(margin_x) +  " distanceToGo: " + String(stepperX.distanceToGo()));
      }
      irr_handled_x = true;
    }else if(dist_x != 0){
      stepperX.run();
    }

    if(irr_x == HIGH && irr_handled_x ){    // czyli koniec obslugi stanu LOW
      send2debuger("osX","puszczam IRR X");
      irr_handled_x  = false;
      irr_min_x  = false;
      irr_max_x  = false;
    }

// -------------oś Y--------------------

    irr_y  = digitalRead( IRRY_PIN );
    if( irr_y == LOW && dist_y != 0 ){
      if( irr_min_y && dist_y < 0 ){    // stoje na dole a mam jechac w dół
        stepperY.stopNow();
        send2debuger("osY MIN","jestem na dole i nie jade w dol roznica:" + String(dist_y)+" / R(" + String(stepperY.distanceToGo())+ ") / " + String(margin_y));
        delay(100);
        dist_y = 0;
      }
      if( irr_max_y && dist_y > 0 ){    // stoje na górze a mam jechac w góre
        stepperY.stopNow();
        send2debuger("osY MAX","jestem na górze i nie jade w gore roznica:" + String(dist_y)+" / R(" + String(stepperY.distanceToGo())+ ") / " + String(margin_y));
        dist_y = 0;
      }
    }
    if( irr_y == LOW && !irr_handled_y) {      // wciśnięte = bylo przerwanie Y i jeszcze nie jest obsluzone
      if( dist_y < 0 ){                       // jechalem w dół
        stepperY.stopNow();           // tu jest 0, reset adresowania
        margin_y = stepperY.currentPosition();                      // to jest pozycja skrajna
        irr_min_y = true;      // to jest minimum
        dlugosc_y = last_max_y;
        send2debuger("osY MIN", "dlugosc:" + String(dlugosc_y)+ " margines: " + String(margin_y)+ " jade: " + String(stepperY.distanceToGo()));
        send2android("LENGTHY " +  String(dlugosc_y) );
      }else if( dist_y > 0 ){             // jechalem w gore
        stepperY.stopNow();
        last_max_y = posy();   // to jest pozycja skrajna
        irr_max_y = true;
        dlugosc_y = last_max_y;
        send2debuger("osY MAX", "dlugosc:" + String(dlugosc_y)+ " margines: " + String(margin_y));
        send2android("LENGTHY " +  String(dlugosc_y) );
      }else{
        send2debuger("osY", "dist_y :" + String(dist_y)+ " margines: " + String(margin_y) +  " distanceToGo: " + String(stepperY.distanceToGo()));
      }
      irr_handled_y = true;
    }else if(dist_y != 0){
      stepperY.run();
    }
    if(irr_y == HIGH && irr_handled_y ){    // czyli koniec obslugi stanu LOW
      send2debuger("osY","puszczam IRR Y");
      irr_handled_y  = false;
      irr_min_y  = false;
      irr_max_y  = false;
    }
  }
}

void irr_zrozum(){
 irr_y  = digitalRead( IRRY_PIN );
 irr_x  = digitalRead( IRRX_PIN );
}

// obluga wag
unsigned int read_szklanka(){
  digitalWrite( PIN_MADDR0, 0 );      //   // Ustaw numer na muxach, wystaw adres
  digitalWrite( PIN_MADDR1, 0 );
  digitalWrite( PIN_MADDR2, 0 );
  digitalWrite( PIN_MADDR3, 0 );
  unsigned long rr = 0;
  delay(MULTI_ADDR_TIME);
  unsigned int j=0;
  unsigned int count = 1<<WAGA_READ_COUNT;
  for( j=0; j<count ;j++){
    rr+= analogRead(PIN_WAGA_ANALOG);
    delay(2);
  }
  return rr >>WAGA_READ_COUNT;
}

unsigned long read_butelka(byte numer){
  digitalWrite(PIN_MADDR0, bitRead(numer,0) );      //   // Ustaw numer na muxach, wystaw adres
  digitalWrite(PIN_MADDR1, bitRead(numer,1) );
  digitalWrite(PIN_MADDR2, bitRead(numer,2) );
  digitalWrite(PIN_MADDR3, bitRead(numer,3) );
  unsigned long rr = 0;
  delay(MULTI_ADDR_TIME);
  unsigned int j=0;
  unsigned int count = 1<<MULTI_READ_COUNT;
  for( j=0; j<count ;j++){
    rr+= analogRead(PIN_WAGA_ANALOG);
    delay(2);
  }
  delay(MULTI_READ_TIME);
  return rr >>MULTI_READ_COUNT;
}


void readSerial0Input( String input ){      // odbierz wejscie z serial0
  parseInput( input );                      // parsuj wejscie
  if(DEBUG_SERIAL_INPUT){
    send2debuger( "READ0", input );
  }
  send2android(input);
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

long int maplong(unsigned long x, unsigned long in_min, unsigned long in_max, unsigned long out_min, unsigned long out_max){
  return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
}

unsigned int readDistance0(){
  return 0;//ultrasonic0.ping_median( DIST_REPEAT ); // Do multiple pings (default=5), discard out of range pings and return median in microseconds. 
}
unsigned int readDistance1(){
  return 0;//ultrasonic1.ping_median( DIST_REPEAT ); // Do multiple pings (default=5), discard out of range pings and return median in microseconds. 
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
//    send2debuger( "ADB_RES", "ADB_RECEIVE");
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

// BILBIOTEKI i obsługi EVENTow
String stosPop(){        // pobierz ze stosu
  // delimiter to nowa linia: SEPARATOR_CHAR
  int c            = stos.indexOf( SEPARATOR_CHAR );
  if(c >0 ){
    String res     = stos.substring(0,c);
    stos           = stos.substring(c+1);
    stos_length--;
    return res;
  }else if(stos_length){
    stos_length = 0;
    return stos;  
  }else{
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

int send2adb( String output){      // wyslij string do androida
    int res = -2;
    byte cnt = 0;
    while( res == -2 && cnt < 10 ){        // powtarzaj 4 razy
      char *cstr = new char[output.length() + 1];
      strcpy(cstr, output.c_str());
      cstr[output.length()] = '\0';
      res = connection->writeString( cstr );
      if( res == -2 ){
        if( cnt > 2 ){
          send2debuger( "ADB", "nie idzie: " + output + "/" + String(cnt) );
          delay(10);
          ADB::poll();      // Poll the ADB subsystem.
        }
        cnt++;
      }
      delete [] cstr;
    }
    return res;
}

int send2android( String output2 ){      // wyslij string do androida
  String output = output2 + SEPARATOR_CHAR;
  int res = -1;
  unsigned int len = output.length();  // długosc komunikatu
  if( USE_ADB && adb_ready ){
    res = send2adb(output);
    if(DEBUG_ADB2ANDROID){
      send2debuger( "2ANDROID ADBSEND: " + String(res), output2 );
    }
  }
  if( USE_BT ){
      if(DEBUG_OUTPUT2ANDROID){
        send2debuger( "2ANDROID BTSEND", output2 );
      }
      Serial3.print( output );    // nie println bo juz dodaj separator
      res = 0;
  }else if(DEBUG_OUTPUT2ANDROID){
    if( !adb_ready ){
      send2debuger( "2ANDROID NOSEND", output2 );
    }
  }
  return res;
}
void readSerial3Input( String input ){       // odebralem z BT od androida
  if( USE_BT ){ 
    parseInput( input );          // parsuj wejscie
  }
  if(DEBUG_BT_INPUT){
    send2debuger( "READ3", input);
  }
}

void readAndroidInput( String input ){    // odbierz przez ADB z androida
  if(DEBUG_ADB_INPUT){
    send2debuger("READ_ADB",input);  
  }
  parseInput( input );          // parsuj wejscie
}




/*
  int1_value = LOW;
  pinMode(PIN3, INPUT);
  servoZ.attach(STEPPER_Z_PWM);                   // przypisz do pinu, uruchamia PWMa
  servoZ.writeMicroseconds(SERVOZ_UP);            // jedź aż na gore
  attachInterrupt( INT1, on_int1F, FALLING);    // nasłuchuj zmiany PIN 3    // Krańcowy Z (gorny) na wcisniecie daje zero
  int i =0;
  while( int1_value == HIGH ){                   // az na górze
    send2debuger( "fill", "czekam na gore" );
    delay(20);                                    // todo zabezpieczenie gdy po 5 sek nadal nie jest na górze
    i++;
    if(i>1000){    // bez przesady
      break;
    }
  }
  detachInterrupt(INT1);
  servoZ.writeMicroseconds(SERVOZ_STAYUP);     // trzymaj na górze z tą mocą
  irr_min_z = false;
  irr_max_z = true;
  send_current_position(false);  
  send2debuger( "fill", "czekam " + String(czas) );
  delay(czas);                                 // trzymaj na górze tyle czasu 
  servoZ.writeMicroseconds(SERVOZ_DOWN);       // a potem jedz w dol
  send2debuger( "fill", "w dół" );
//  attachInterrupt( INT1, on_int1F, FALLING);   // nasłuchuj zmiany PIN 3    // Krańcowy Z (dolny) na wcisniecie daje zero
//  delay(100);                                  // zjedz kawałek zanim znow podlacze przerwania
//  int1_value = LOW;
///  int aaa = 0;
  delay( 1000 );
  */
/*
  while(int1_value == LOW){                    // az dotknie krańcówki dolnej
    send2debuger( "irr", "test " + String(aaa) );
    delay(20);
    aaa++;
  }
  detachInterrupt(INT1);
 */


/*  int1_value = LOW;
  attachInterrupt( INT1, on_int1F, FALLING);   // nasłuchuj zmiany PIN 3    // Krańcowy Z (dolny) na wcisniecie daje zero
  delay(100);                                  // zjedz kawałek zanim znow podlacze przerwania
  int1_value = LOW;
  while(int1_value == LOW){                    // az dotknie krańcówki dolnej
    //      send2debuger( "irr", "test " + String(aaa) );
    delay(20);
  }
  detachInterrupt(INT1);                      // odlacz przerwanie, jestem na dole
  int1_value = LOW;
  servoZ.attach(STEPPER_Z_PWM);                // przypisz do pinu, uruchamia PWMa
  attachInterrupt( INT1, on_int1F, FALLING);    // nasłuchuj zmiany PIN 3    // Krańcowy Z (gorny) na wcisniecie daje zero
  servoZ.writeMicroseconds(SERVOZ_UP);         // jedź w gore
  int i =0;
  while( int1_value == HIGH ){                   // az na górze
    send2debuger( "fill", "czekam na gore" );
    delay(20);                                    // todo zabezpieczenie gdy po 5 sek nadal nie jest na górze
    i++;
    if(i>1000){    // bez przesady
      break;
    }
  }
  
  
  
//  ile losowych losowac
#define LOS_MAX 20
  
  
  }else if( input.equals( "LOSUJX" )){       // wykonaj 10 losowych ruchów po X
    stosPush("NORET");
    for(byte f = 0;f<LOS_MAX;){
      stosPush("SET X " + String(random(0,XLENGTH/100 / 2) * 100));// zawsze po przeciwnych stronach
      stosPush("SET X " + String(random(XLENGTH/100 / 2,XLENGTH/100) * 100));
      f=f+2;
    }
    stosPush("READY");
  }else if( input.equals("LOSUJY" )){       // wykonaj 10 losowych ruchów po X
    stosPush("NORET");
    for(byte f = 0;f<LOS_MAX;){
      stosPush("SET Y " + String(random(0,3) * 100));// zawsze po przeciwnych stronach
      stosPush("SET Y " + String(random(3,6) * 100));
      f=f+2;
    }
    stosPush("READY");
  
  }else if( input.equals( "MACHAJX") ){       // wykonaj 10 ruchów 300 - 1000
    stosPush("NORET");
    for(byte f = 0;f<10;){
      stosPush("SET X " + String( XLENGTH / 3 ) );
      stosPush("SET X " + String( XLENGTH / 3 * 2) );
      f=f+2;
    }
    stosPush("READY" );

  }else if( input.equals( "MACHAJY") ){       // wykonaj 10 ruchów 300 - 1000
    stosPush("NORET");
    for(byte f = 0;f<LOS_MAX;){
      stosPush("SET Y 50");
      stosPush("SET Y 400");
      f=f+2;
    }
    stosPush("READY");
  }else if( input.equals( "MACHAJZ") ){       // wykonaj 10 ruchów MAX - MIN
    stosPush("NORET");
    for(byte f = 0;f<LOS_MAX;){
      stosPush("SET Z MAX");
      stosPush("SET Y MIN");
      f=f+2;
    }
    stosPush("READY");


// pozycja trzymająca i mnożnik trzymania
#define SERVOZ_STAYUP_TIME 256

#define SERVOZ_STAYUP_POS 2020
  }else if( input.startsWith("STAYZ") ){
    unsigned long int stayz = decodeInt( input, 6 );    // 5 znakow i spacja
    for(int keep = 0; keep < (stayz>>8); keep += 1)  {
      servoZ.writeMicroseconds(SERVOZ_STAYUP_POS);
      delay(SERVOZ_STAYUP_TIME);
    }
    send_current_position(false);
  
  
  */


