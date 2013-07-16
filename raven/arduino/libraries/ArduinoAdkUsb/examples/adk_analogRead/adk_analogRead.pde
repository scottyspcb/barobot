import cc.arduino.*;

ArduinoAdkUsb arduino;

int val;
float rotation;

void setup() {
  arduino = new ArduinoAdkUsb( this );

  if ( arduino.list() != null )
    arduino.connect( arduino.list()[0] );

  /* Lock PORTRAIT view */
  orientation( PORTRAIT );

  rectMode( CENTER );
}

void draw() {
  background( 255 );

  if ( arduino.isConnected() ) {
    /* Try to read from arduino */
    if ( arduino.available() > 0 ) {
      val = arduino.readByte() & 0xFF; // 0-255

      /* Get a rotational value from the read byte */
      rotation = map( val, 0, 255, 0, HALF_PI );
    }

    /* Draw a simple rectangle, rotates based on read value */
    pushMatrix();
    translate( sketchWidth()/2, sketchHeight()/2 );
    rotate( rotation );
    fill( 0 );
    rect( 0, 0, 300, 300 );
    popMatrix();
  }

  /* Draws a filled rect based on arduino connection state */
  connected( arduino.isConnected() );
}

void onStop() {
  /* Because of an issue in processing... */
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
 // ARDUINO CODE, _03_analogRead_adk.ino
 
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
 char url[] = "http://labs.arduino.cc/adk/ADK_count"; // the URL of your app online
 
 // button variables
 int sliderPin = A1;
 int val;
 
 // counters
 long timer = millis();
 
 
 // initialize the accessory:
 AndroidAccessory usb(companyName, applicationName,
 accessoryName,versionNumber,url,serialNumber);
 
 void setup() {
 // start the connection to the device over the USB host:
 usb.powerOn();
 
 pinMode(sliderPin, INPUT);   
 }
 
 void loop() {
 // Read button state
 val = analogRead(sliderPin);
 val /= 4;
 
 // Print to usb 
 if(millis()-timer>100) { // sending 10 times per second
 if (usb.isConnected()) { // isConnected makes sure the USB connection is ope
 usb.write(val);
 }
 timer = millis();
 }
 }
 */
