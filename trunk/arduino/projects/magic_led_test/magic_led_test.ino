#include <Adafruit_NeoPixel.h>
#include <EEPROM.h>

#define PIN 8
/*
#define NEO_RGB 0x00
#define NEO_GRB 0x01 
#define NEO_BRG 0x04
#define NEO_RBG 0x08
#define NEO_GBR 0x10 
#define NEO_BGR 0x20
*/

byte tryb =1;
byte next =1;
Adafruit_NeoPixel strip = Adafruit_NeoPixel(2, PIN,  NEO_RGB + NEO_KHZ800);

void setup() {
  Serial.begin(57600); 
  Serial.println("HELLO");
  strip.begin();
  strip.show();
}

void loop() {
   Serial.println(String(tryb));
   pokaz_rgb(tryb,1000);
/*
   Serial.println("2. NEO_GRB"); 
   pokaz_rgb(NEO_GRB,1000);

   Serial.println("3. NEO_BRG");
   pokaz_rgb(NEO_BRG,1000);
   
   Serial.println("4. NEO_RGB");
   pokaz_rgb(NEO_RBG,1000);	// NEO_RBG added in Adafruit_NeoPixel: rOffset = 1;gOffset = 2;bOffset = 0;
   */
}

void pokaz_rgb( byte mode,int time ){
  Serial.println("\tCREATE");

  Serial.println("\tRED");
  set_color( strip.Color( 255, 0,  0 ));
  delay(time);

  Serial.println("\tGREEN");
  set_color( strip.Color( 0, 255,  0 ));
  delay(time);

  Serial.println("\tBLUE");
  set_color( strip.Color( 0, 0,  255 ));
  delay(time);

  Serial.println("\t Off");
  set_color( 0);  //zgas
  delay(time);
}


void set_color( uint32_t color ){
  int num = strip.numPixels();
   Serial.print("\t pixels:");
   Serial.println(String(num));
   for(int8_t i = 0; i < num; i++) {
      Serial.println(String(i));
      strip.setPixelColor(i, color );
   }
   Serial.println("show");
   strip.show();
}




