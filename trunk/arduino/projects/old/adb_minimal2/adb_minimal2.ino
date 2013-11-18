#include <SPI.h>
#include <Adb.h>
#define MAX_RESET 8

// Adb connection.
Connection * connection;

// Elapsed time for ADC sampling
long lastTime;
boolean r = true;

void adbEventHandler(Connection * connection, adb_eventType event, uint16_t length, uint8_t * data){
  if (event == ADB_CONNECTION_RECEIVE) {
    Serial.println(  "ADB_RECEIVE");
  }else if (event == ADB_CONNECTION_FAILED) {
     Serial.println(  "ADB_CONNECTION_FAILED");
  }else if (event == ADB_CONNECTION_OPEN) {
     Serial.println(  "ADB_CONNECTION_OPEN");
  }else if (event == ADB_CONNECTION_CLOSE) {
     Serial.println(  "ADB_CONNECTION_CLOSE");
  }else if (event == ADB_CONNECT) {
     Serial.println(  "ADB_CONNECT");
  }else if (event == ADB_DISCONNECT) {
     Serial.println( "ADB_DISCONNECT");
  }else{
     Serial.println( "SOME EVENT");  
  }
}

void setup(){
  pinMode(11, OUTPUT);

  // Initialise serial port
  Serial.begin(115200);
  
  // Note start time
  lastTime = millis();

  // Initialise the ADB subsystem.  
  ADB::init();

  // Open an ADB stream to the phone's shell. Auto-reconnect
  connection = ADB::addConnection("tcp:4567", true, adbEventHandler);  
  Serial.println("start...");
}

int send2android3( String output2, int bb ){      // wyslij string do androida
    String output = output2 + "\n";
    unsigned int len = output.length();  // długosc komunikatu
    char *cstr = new char[output.length() + 1];
    strcpy(cstr, output.c_str());
    cstr[output.length()] = '\0';
    int aaa = connection->writeString( cstr );
    delete [] cstr;
    ADB::poll();
    if( aaa == -2 && bb < 3 ){
      return send2android3(output2, bb++ );
    }
    return aaa;
}
long mil = 0;
long czestotliwosc = 1000;

void loop(){
  mil = millis();
  if( mil > lastTime + czestotliwosc ){
    int aaa = send2android3("aaaaaaaaaaaaaaaa" + String(random()), 1);
    Serial.println("jade1 " + String(aaa) );

    aaa = send2android3("GLASS 6" + String(random()),1);
    Serial.println("jade2 " + String(aaa) );

    aaa = send2android3("READY AT 360,0,100" + String(random()),1 );
    Serial.println("jade3 " + String(aaa) );  
    
    aaa = send2android3("ENCODERS [0/2] [0/2]" + String(random()),1);
    Serial.println("jade4 " + String(aaa)); 
    
    lastTime = mil;
    czestotliwosc = analogRead(A0) * 4;  // 0 - 1000
  }
}

