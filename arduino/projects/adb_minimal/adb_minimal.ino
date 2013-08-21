#include <AccelStepper.h>
#include "config.cpp";
#include <SPI.h>
#include <Adb.h>

int status = STATE_INIT;      // na pocztku jest w trakcje inicjacji

// obsluga zrodel wejscia
Connection * connection;        // Adb connection.
boolean adb_ready = false;
String serial0Buffer = "";
String serial3Buffer = "";
boolean Console0Complete = false;   // This will be set to true once we have a full string
boolean Console3Complete = false;   // This will be set to true once we have a full string
// koniec obsluga zrodel wejscia
void setupOutputs(){      // uzyte w setup()
  pinMode( STATUS_LED_PIN, OUTPUT);           // zapal diode statusu (w trakcje inicjacji)
  digitalWrite( STATUS_LED_PIN, HIGH );
}

void setupADB(){      // uzyte w setup()
  if (USE_ADB ){
    delay(500);
    ADB::init();
    delay(500);
    connection = ADB::addConnection("tcp:14568", true, adbEventHandler);  // Open an ADB stream to the phone's shell. Auto-reconnect. Use any unused port number eg:4568
    delay(500);
    send2debuger("INIT", "ADB READY");
  }
}

void setup(){ 
  if (DEBUG_OVER_SERIAL){
    Serial.begin(SERIAL0_BOUND);
  }
  setupOutputs();
  setupADB();
  status= STATE_BUSY;      // po inicie jest w stanie zajętym

  digitalWrite( STATUS_LED_PIN, 0 );      // koniec inicjacji
  send2debuger("INIT", "START ready");
  status= STATE_READY;      // po wykonaniu operacji jest w stanie gotowym
  send2debuger("INIT", "Koniec init");
}

unsigned long counter1000 = 0;
unsigned long milis = 0;
unsigned long mil = 0;
unsigned long int czestotliwosc = 0;

void loop(){
  mil = millis();
  if( mil > milis + czestotliwosc ){
    unsigned int rand = random(1,1000);
    String send = "RET "+ String(rand);
    counter1000++;
    milis = mil;
    czestotliwosc = analogRead(A0) * 4;  // 0 - 1000
    send2android(send);
//    digitalWrite( STATUS_LED_PIN, counter1000%2 );
  }
  if (Console0Complete) {
    readSerial0Input( serial0Buffer );          // parsuj wejscie
    Console0Complete = false;
    serial0Buffer = "";
  }
  if( USE_ADB ){
    ADB::poll();      // Poll the ADB subsystem.
  }
}

void readSerial0Input( String input ){      // odbierz wejscie z serial0
  if(DEBUG_SERIAL_INPUT){
    send2debuger( "READ0", input );
  }
  send2android(input);
}

void readAndroidInput( String input ){    // odbierz przez ADB z androida
  if(DEBUG_ADB_INPUT){
    send2debuger("READ_ADB",input);
  }
}

// BILBIOTEKI i obsługi EVENTow
void send2android( String output2 ){      // wyslij string do androida
    String output = output2 + "\n";
    unsigned int len = output.length();  // długosc komunikatu
    if( USE_ADB && adb_ready ){
//      uint8_t* sendstr = stringToUint8( output );
//      connection->write( len, sendstr );
        char *cstr = new char[output.length() + 1];
        strcpy(cstr, output.c_str());
        cstr[output.length()] = '\0';
        connection->writeString( cstr );
        delete [] cstr;

//      free(sendstr);// bez tego sie zapcha
      if(DEBUG_ADB2ANDROID){      
        send2debuger( "2ANDROID ADBSEND", output2 );
      }
    }
    if(DEBUG_OUTPUT2ANDROID){
      if( !adb_ready || !USE_ADB){
          send2debuger( "2ANDROID NOSEND", output2 );
      }
    }
}

uint8_t* stringToUint8( String in){    // pamietac o zwolnieniu pamieci po tym!!! 
  //  uint8_t line_buf[liczba.length()+1] = data;
  //  char charBuf[liczba.length()+1];
  //  liczba.toCharArray(charBuf, liczba.length()+1) ;
  //   uint16_t s = liczba.length()+1;
  //    char test[data+1] = "";
  //   uint16_t length, uint8_t * data){
  //   String res = "";
  //   for (int i=0; i<length; i++) {
  //     res += (char) data[i];
  //   }
  //    connection->writeString(charBuf);
  //  Serial.println(charBuf);
  int len = in.length();  // długosc komunikatu
 
 // char charBuf[50];
 // uint8_t charBuf2[50];
 // in.toCharArray(charBuf, len);
  
  uint8_t* line_buf;
  line_buf = (uint8_t *) malloc(len+1);
  int i=0;
  for (; i<len; i++) {
    line_buf[i]  = (uint8_t) in[i];
  }
  line_buf[len] = 0;
//  line_buf[len+1] = 0;
  return line_buf;
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
  if(DEBUG_OVER_SERIAL){
    Serial.println("DEBUG "+ ns +": [" + logstr + "]");
  }
}

void serialEvent(){                       // FUNKCJA WBUDOWANA - zbieraj dane z serial0 i serial3 i skadaj w komendy
  while (Serial.available() && !Console0Complete) {    // odczytuj gdy istnieja dane i poprzednie zostaly odczytane
    char inChar = (char)Serial.read(); 
    serial0Buffer += String(inChar);
    if (inChar == '\n') {
      Console0Complete = true;
    }
  }
}

