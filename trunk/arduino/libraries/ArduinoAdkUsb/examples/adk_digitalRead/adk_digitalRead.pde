import cc.arduino.*;

ArduinoAdkUsb arduino;

int rectcolor = color(0, 0, 0);

void setup() {
  arduino = new ArduinoAdkUsb( this );

  if ( arduino.list() != null )
    arduino.connect( arduino.list()[0] );

  /* Lock PORTRAIT view */
  orientation( PORTRAIT );

  rectMode( CENTER );
}

void draw() {
  if ( arduino.isConnected() ) {
    if ( arduino.available() > 0 ) {  
      if ( arduino.readChar() == 'a' )
        rectcolor = color( 0, 255, 0 );
      else 
        rectcolor = color( 0, 0, 0 );
    }

    fill( rectcolor );
    rect( sketchWidth()/2, sketchHeight()/2, 300, 300 );
  }

  /* Just draws a red/green rect in the corner based on the state of the connection */
  connected( arduino.isConnected() );
}

void onStop() {
  finish();
}

void connected( boolean state ) {
  pushMatrix();
  translate( 20, 20 );
  if ( state )
    fill( 0, 255, 0 );
  else
    fill( 255, 0, 0 );
  rect( 0, 0, 30, 30 );
  popMatrix();
}

/*
 // ARDUINO CODE, _01_digitalRead_adk.ino
 
 #include <Max3421e.h>
 #include <Usb.h>
 #include <AndroidAccessory.h>
 
 // accessory descriptor. It's how Arduino identifies itself to Android
 char applicationName[] = "Mega_ADK"; // the app on your phone
 char accessoryName[] = "Mega_ADK"; // your Arduino board
 char companyName[] = "Arduino SA";
 
 // make up anything you want for these
 char versionNumber[] = "1.0";
 char serialNumber[] = "1";
 char url[] = "http://labs.arduino.cc/adk/ADK_digitalRead"; // the URL of your app online
 
 // button variables
 int buttonPin = A1;
 int buttonState = 0;
 char letter = 'a';
 
 // counters
 long timer = millis();
 
 
 // initialize the accessory:
 AndroidAccessory usb(companyName, applicationName, 
 accessoryName, versionNumber, url, serialNumber);
 
 void setup() {
 // start the connection to the device over the USB host:
 usb.powerOn();
 
 pinMode(buttonPin, INPUT);
 }
 
 void loop() {
 // Read button state 
 buttonState = digitalRead(buttonPin);
 
 // Print to usb
 if (millis()-timer>100) { // sending 10 times per second
 if (usb.isConnected()) { // isConnected makes sure the USB connection is ope
 if (buttonState == HIGH) {
 usb.write( 'a' );
 }
 else {
 usb.write( ' ' );
 }
 }
 timer = millis();
 }
 }
 */
