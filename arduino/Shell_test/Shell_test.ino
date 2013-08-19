#include <SPI.h>
#include <Adb.h>

Connection * shell;

// Event handler for the shell connection. 
void adbEventHandler(Connection * connection, adb_eventType event, uint16_t length, uint8_t * data){
   send2debuger( "ADB_RES", "EVENT");
  if (event == ADB_CONNECTION_RECEIVE) {
    String res = "";
    for (int i=0; i<length; i++) {
      res += (char) data[i];
    }
    Serial.println(res);
  }else if (event == ADB_CONNECTION_FAILED) {
     send2debuger( "ADB_RES", "ADB_CONNECTION_FAILED");

  }else if (event == ADB_CONNECTION_OPEN) {
     send2debuger( "ADB_RES", "ADB_CONNECTION_OPEN");
  }else if (event == ADB_CONNECTION_CLOSE) {
     send2debuger( "ADB_RES", "ADB_CONNECTION_CLOSE");
  }else if (event == ADB_CONNECT) {
     send2debuger( "ADB_RES", "ADB_CONNECT");
  }else if (event == ADB_DISCONNECT) {
     send2debuger( "ADB_RES", "ADB_DISCONNECT");
  }
}


void send2debuger( String in, String in2 ){
     Serial.println(in + " " + in2);
}

void setup()
{
  Serial.begin(9600);
  delay(500);
  // Initialise the ADB subsystem.  
  ADB::init();
  delay(500);
  // Open an ADB stream to the phone's shell. Auto-reconnect
  shell = ADB::addConnection("shell:", true, adbEventHandler);  
  delay(500);
  Serial.println("start");
}

void loop(){
  byte ch;
  // Check for incoming serial data
  if (Serial.available() > 0){
    // read the incoming byte:
    ch = Serial.read();
    // Write to shell
    if (shell->status == ADB_OPEN)
      shell->write(1, &ch);
    else
      Serial.println("Shell not open");
  }
  // Poll the ADB subsystem.
  ADB::poll();
}

