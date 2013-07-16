/*
  AnalogReadGraph for ADK
 
 Reads input from an Android device using the Android Accessory
 library, graphs it. 
 
 created 16 Jun 2011
 by Tom Igoe
 
 This example code is in the public domain.
 http://labs.arduino.cc/en/Tutorial/AnalogReadGraph
 
 */
 import cc.arduino.*;

ArduinoAdkUsb arduino;   // instance of the USB library

int positionY = 0;       // height of a vertical bar on the screen
int previousY = 0;       //   previous Y value
int xpos = 0;            // x position of the graph

void setup() {
  // initialize the USB host:
  arduino = new ArduinoAdkUsb( this );

  // if there is a USB host, open a connection to it:

  if ( arduino.list() != null ) {
    arduino.connect( arduino.list()[0] );
  }

  // Lock PORTRAIT view:
  orientation( PORTRAIT );

  // initialize a font to draw on the screen:
  String thisFont = PFont.list()[0];
  PFont myFont = createFont(thisFont, 96);
  textFont(myFont, 96);
  background(0);
  smooth();
  strokeWeight(3);
}

void draw() {
  // white background, nice blue text:
  background(255);
  fill(#2389F6);
  // Try to read from arduino
  if (arduino.isConnected()) {
    if ( arduino.available() > 0 ) {
      int inChar = arduino.read();
      // map it to the screen height:
      positionY = int(map(inChar, 0, 255, 0, height));
      drawSegment(positionY);
    }
  }
}

void drawSegment(int thisValue) {
  // map the incoming values (0 to  1023) to an appropriate
  // graphing range (0 to window height/number of values):
  int ypos = int(map(thisValue, 0, 1023, 0, height))     ;

  // change colors to draw the graph line in light blue  :
  stroke(#34A3EC);
  line(xpos, previousY, xpos+1, ypos);
  // save the current value to be the next time's previous value:
  previousY = ypos;

  // if you've drawn to the edge of the window, start at the beginning again:
  if (xpos >= width) {
    xpos = 0;
    background(0);
  } 
  else {
    xpos++;
  }
}

// clean up nicely after the app closes:
void onStop() {
  super.onStop();
  finish();
}

