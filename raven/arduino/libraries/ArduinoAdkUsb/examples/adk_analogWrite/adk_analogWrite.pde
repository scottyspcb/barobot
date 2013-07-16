import cc.arduino.*;

ArduinoAdkUsb arduino;

boolean locked = false;

void setup() {
  arduino = new ArduinoAdkUsb( this );

  if ( arduino.list() != null )
    arduino.connect( arduino.list()[0] );

  /* Lock PORTRAIT view */
  orientation( PORTRAIT );
}

void draw() {
}

public boolean surfaceTouchEvent(MotionEvent event) {
  if ( arduino.isConnected() ) {
    if ( event.getAction() == MotionEvent.ACTION_MOVE ) {
      int val = (int)map( event.getX(), 0, sketchWidth(), 0, 255 );
      arduino.write( (byte)val );
    }
  }

  // if you want the variables for motionX/motionY, mouseX/mouseY etc.
  // to work properly, you'll need to call super.surfaceTouchEvent().
  return super.surfaceTouchEvent(event);
}

void onStop() {
  super.onStop();
  finish();
}

/*
 // ARDUINO CODE, _04_analogWrite_adk.ino
 
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
 
 // led variables
 int ledPin = 10;
 
 // counters
 long timer = millis();
 
 // initialize the accessory:
 AndroidAccessory usb(companyName, applicationName,
 accessoryName,versionNumber,url,serialNumber);
 
 void setup() {
 Serial.begin( 9600 );
 // start the connection to the device over the USB host:
 usb.powerOn();
 
 pinMode(ledPin, OUTPUT);   
 }
 
 void loop() {
 // Print to usb 
 if(millis()-timer>100) { // sending 10 times per second
 if (usb.isConnected()) { // isConnected makes sure the USB connection is ope
 char val = usb.read();
 Serial.print( val );
 if( val == 'a' )
 digitalWrite( ledPin, HIGH );
 else if( val == 'b' )
 digitalWrite( ledPin, LOW );
 }
 timer = millis();
 }
 }
 */
