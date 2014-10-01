#include <Adafruit_NeoPixel.h>
#include <EEPROM.h>

#define PIN 2

// Parameter 1 = number of pixels in strip
// Parameter 2 = Arduino pin number (most are valid)
// Parameter 3 = pixel type flags, add together as needed:
//   NEO_KHZ800  800 KHz bitstream (most NeoPixel products w/WS2812 LEDs)
//   NEO_KHZ400  400 KHz (classic 'v1' (not v2) FLORA pixels, WS2811 drivers)
//   NEO_GRB     Pixels are wired for GRB bitstream (most NeoPixel products)
//   NEO_RGB     Pixels are wired for RGB bitstream (v1 FLORA pixels, not v2)
Adafruit_NeoPixel strip = Adafruit_NeoPixel(150, PIN,  NEO_GRB + NEO_KHZ800);

// IMPORTANT: To reduce NeoPixel burnout risk, add 1000 uF capacitor across
// pixel power leads, add 300 - 500 Ohm resistor on first pixel's data input
// and minimize distance between Arduino and first pixel.  Avoid connecting
//on a live circuit...if you must, connect GND first.

byte tryb =1;
int num = strip.numPixels();
void setup() {
  num = strip.numPixels();
  tryb =EEPROM.read( 0x10 );
  if( tryb > 6){
    tryb = 0;
  }
  EEPROM.write( 0x10, tryb+1 );
  strip.begin();
  strip.show();
  if(tryb == 0){
 //   nightrider( strip.Color(100, 0, 0), 3, 100 );
  }
//tryb = 5;
}

void loop() {
  if( tryb == 0 ){
    roadrunner2(10);
    roadrunner();
    loader(strip.Color(0, 255, 0), strip.Color(0, 0, 255), 50);
    // Some example procedures showing how to display to the pixels:
    colorWipe(strip.Color(255, 0, 0), 30); // Red
    colorWipe(strip.Color(0, 255, 0), 30); // Green
    colorWipe(strip.Color(0, 0, 255), 30); // Blue
  
    theaterChase(strip.Color(127, 127, 127), 30); // White
    theaterChase(strip.Color(127,   0,   0), 30); // Red
    theaterChase(strip.Color(  0,   0, 127), 30); // Blue
  
    rainbow(20);
    rainbowCycle( 5, 20 );
    theaterChaseRainbow(50);
    
    for( byte i=0; i< 10; i++){
      fader( 20 ); 
    }
    for( byte i=0; i< 10; i++){
      wyliczanka(); 
    }
    
  }else if( tryb == 1 ){
   int num = strip.numPixels();
     for(int16_t i = 0; i < num; i = i + 1) {
        strip.setPixelColor(i, strip.Color(255, 255, 255) );
        strip.show();
     }  
  }else if( tryb == 2 ){
   int num = strip.numPixels();
     for(int16_t i = 0; i < num; i = i + 1) {
        strip.setPixelColor(i, strip.Color( 255, 0, 0) );
        strip.show();
     } 
  }else if( tryb == 3 ){
   int num = strip.numPixels();
     for(int16_t i = 0; i < num; i = i + 1) {
        strip.setPixelColor(i, strip.Color(0, 255, 0) );
        strip.show();
     } 
  }else if( tryb == 4 ){
   int num = strip.numPixels();
     for(int16_t i = 0; i < num; i = i + 1) {
        strip.setPixelColor(i, strip.Color(0, 0, 255 ) );
        strip.show();
     } 
  }else if( tryb == 5 ){
    for(int i=0; i<360;i+=10){
      byte time = 10 + (float) 30 * abs(sin(i * 2 / PI));  // 10 - 40
      toggle(time, time, (45 - time)/2 );
    } 
  }else if( tryb == 6 ){
    rainbowCycle(5, 20); 
  }  
}

void fader( int time ){
   int r = random(0, 200);   
  // int g = random(0, 255-r);   
  // int b = random(0, 255-g); 

   fadein( 30, 10, r, 0, 0 );
   fadeout( 40, 10, r, 0, 0 );

   delay(100);

   r = random(128, 255);   
   fadein( 30, 10, r, 0, 0 );
   fadeout( 30, 10, r, 0, 0 );

   delay(300);
}
void fadein( byte tempo, byte time, byte r,  byte g,  byte b ){
   int16_t i = 0;
   for(; i < 255; i = i + tempo) {
     if(i>255){
       i = 255;
     }
     fade( r, g, b, (float)i/255);
     delay(time);
   }
   fade( r, g, b, 1);
}
void fadeout( byte tempo, byte time, byte r,  byte g,  byte b){
   int16_t i = 255;
   for(; i >= 0; i = i - tempo) {
      fade( r, g, b, (float)i/255);
      delay(time);
    }
    fade( r, g, b, 0);
}

void fade( byte r,  byte g,  byte b, float scale ){
   uint32_t color = strip.Color( r * scale, g * scale,  b * scale );
   for(int8_t i = 0; i < num; i = i + 1) {
      strip.setPixelColor(i, color  );
   }
   strip.show();
}


void roadrunner() {
      int num = strip.numPixels();
	for(int16_t i = 0; i < num; i++) {
		// Set the i'th led to red 

                strip.setPixelColor(i, strip.Color(255, 0, 0) );
                strip.show();

		// now that we've shown the leds, reset the i'th led to black
		strip.setPixelColor(i, 0 );
                delay(10);

                strip.show();
		// Wait a little bit before we loop around and do it again
		delay(10);
	}

	// Now go in the other direction.  
	for(int16_t i = num-1; i >= 0; i--) {
		// Set the i'th led to red 
                strip.setPixelColor(i, strip.Color(0, 255, 0) );
                strip.show();
                delay(10);
		// now that we've shown the leds, reset the i'th led to black
		strip.setPixelColor(i, 0 );
                strip.show();
		// Wait a little bit before we loop around and do it again
		delay(10);
	}
}

void whiteTest(uint32_t color, uint32_t color2, int  time ) {
   int num = strip.numPixels();
   for(int16_t i = 0; i < num; i = i + 1) {
      strip.setPixelColor(i, color );
      strip.show();
      delay(time);
      strip.setPixelColor(i, 0 );
   }
}

void wyliczanka(){
    byte colors[num+1][3];
    int leds = num * 4;
    int time = 50;
    for(byte l= 0; l < leds; l++){
      int led = random(0, num);   
      int color = random(0, 3);    // 0, 1, 2   
      int ratio = random(0, 255);       
      colors[led][color] = ratio;
      
      uint32_t c = strip.Color(colors[led][0], colors[led][1], colors[led][2]);
      strip.setPixelColor(led, c );
      strip.show();
      delay(time);
      time = max(10, time-1); 
    }
    for(byte l= 0; l < num; l++){
      strip.setPixelColor(l, 0 );
    }
    strip.show();
    delay(1000);
}
void roadrunner2(byte count){
  for(int i =0;i<count;i++){
    int r = random(0, 250);   
    int g = random(0, 255-r);   
    int b = random(0, 255-g); 
    int time = random(0, 20); 
    whiteTest(strip.Color(r, g, b), strip.Color(r, g, b),time);
  }
}

void toggle(int time1, int time2, int count){
  uint32_t color = strip.Color(  255,   255, 255);
  for(int i =0;i<count;i++){
    for(int16_t i = 0; i < num; i++) {
      strip.setPixelColor(i, color );
    }
    strip.show();
    delay(time1);
    for(int16_t i = 0; i < num; i++) {
      strip.setPixelColor(i, 0 );
    }
    strip.show();
    delay(time2);
  }
}

// Fill the dots one after the other with a color
void colorWipe(uint32_t c, uint8_t wait) {
  for(uint16_t i=0; i<strip.numPixels(); i++) {
      strip.setPixelColor(i, c);
      strip.show();
      delay(wait);
  }
}

void rainbow(uint8_t wait) {
  uint16_t i, j;
  for(j=0; j<256; j++) {
    for(i=0; i<strip.numPixels(); i++) {
      strip.setPixelColor(i, Wheel((i+j) & 255));
    }
    strip.show();
    delay(wait);
  }
}

// Slightly different, this makes the rainbow equally distributed throughout
void rainbowCycle(uint8_t wait, byte cycles) {
  uint16_t i, j;
  for(j=0; j<256*cycles; j++) { // 5 cycles of all colors on wheel
    for(i=0; i< strip.numPixels(); i++) {
      strip.setPixelColor(i, Wheel(((i * 256 / strip.numPixels()) + j) & 255));
    }
    strip.show();
    delay(wait);
  }
}

//Theatre-style crawling lights.
void theaterChase(uint32_t c, uint8_t wait) {
  for (int j=0; j<10; j++) {  //do 10 cycles of chasing
    for (int q=0; q < 3; q++) {
      for (int i=0; i < strip.numPixels(); i=i+3) {
        strip.setPixelColor(i+q, c);    //turn every third pixel on
      }
      strip.show();
      delay(wait);
      for (int i=0; i < strip.numPixels(); i=i+3) {
        strip.setPixelColor(i+q, 0);        //turn every third pixel off
      }
    }
  }
}

//Theatre-style crawling lights with rainbow effect
void theaterChaseRainbow(uint8_t wait) {
  for (int j=0; j < 256; j++) {     // cycle all 256 colors in the wheel
    for (int q=0; q < 3; q++) {
        for (int i=0; i < strip.numPixels(); i=i+3) {
          strip.setPixelColor(i+q, Wheel( (i+j) % 255));    //turn every third pixel on
        }
        strip.show();
        delay(wait);
        for (int i=0; i < strip.numPixels(); i=i+3) {
          strip.setPixelColor(i+q, 0);        //turn every third pixel off
        }
    }
  }
}

// Input a value 0 to 255 to get a color value.
// The colours are a transition r - g - b - back to r.
uint32_t Wheel(byte WheelPos) {
  if(WheelPos < 85) {
   return strip.Color(WheelPos * 3, 255 - WheelPos * 3, 0);
  } else if(WheelPos < 170) {
   WheelPos -= 85;
   return strip.Color(255 - WheelPos * 3, 0, WheelPos * 3);
  } else {
   WheelPos -= 170;
   return strip.Color(0, WheelPos * 3, 255 - WheelPos * 3);
  }
}

void loader(uint32_t color, uint32_t color2, int  time ) {
   int num = strip.numPixels();
   for(int16_t i = 0; i < num; i = i + 1) {
     byte first = i;
     byte last = num-i;
     strip.setPixelColor(first, color );
     strip.setPixelColor(last, color2 );
     strip.show();
     delay(time);
   }
}

void nightrider(uint32_t color, byte count, int time ) {
   int num = strip.numPixels();
   boolean dir = true;
    for(byte c = 0; c < count; c++) {
     for(byte i = 0; i < num; i = i + 1) {
       int curr = i;
       if(!dir){
        curr = num - i;
       }
       strip.setPixelColor(curr, color );
       strip.show();
       delay(time);
       strip.setPixelColor(curr, 0 );
     }
     dir  =!dir;
   }  
}



