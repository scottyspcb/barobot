/**
	SLAVE
 * Pins:
 * Hardware SPI:
 * MISO -> 12
 * MOSI -> 11
 * SCK -> 13
 */

#include <SPI.h>
#include <Mirf.h>
#include <nRF24L01.h>
#include "Adafruit_WS2801.h"
#include <MirfHardwareSpiDriver.h>

int role = 1;				// slave
uint8_t dataPin  = 8;
uint8_t clockPin = 7;

Adafruit_WS2801 strip = Adafruit_WS2801(2, dataPin, clockPin);

struct command{
	uint8_t sender;
	uint8_t dest;
	uint8_t module;
	uint8_t command;
	uint8_t red;
	uint8_t green;
	uint8_t blue;
	uint8_t speed;
};
byte cmdsize=sizeof(command);

command din	= {0,0,0,0,0};
//command out	= {0,0,0,0,0};

uint32_t Color(byte r, byte g, byte b)
{
  uint32_t c;
  c = g;
  c <<= 8;
  c |= b;
  c <<= 8;
  c |= r;
  return c;
}

void setup(){
  Serial.begin(115200);
  Serial.println("start..."); 
  Mirf.spi = &MirfHardwareSpi;
  Mirf.csnPin = 10; //(This is optional to change the chip select pin)
  Mirf.cePin = 9; //(This is optional to change the enable pin)
  Mirf.init();
  if( role == 0 ){
	Mirf.setRADDR((byte *)"serv1");
  }else if( role == 1 ){
	Mirf.setRADDR((byte *)"1clie");
  }else if( role == 2 ){
	Mirf.setRADDR((byte *)"2clie");
  }
  Mirf.payload = cmdsize;
  Mirf.channel = 10;
  Mirf.config();
  Serial.println("SLAVE START"); 

	Serial.print("ROLE ");
	Serial.println(role);
 
  strip.begin();
  strip.show();
  strip.setPixelColor(0, Color(200, 0, 0) );
  strip.setPixelColor(1, Color(200, 0, 0) );
  strip.show();
 
}

byte last_command = -1;

void use_command( command in ){
	if( in.command == 0 ){					// turn off all
		strip.setPixelColor( 0, 0 );
		strip.setPixelColor( 1, 0 );	
		strip.show();
	}else if( in.command == 1 || in.command == 2 || in.command == 3 ){
		if( in.module == 0 || in.module == 1 ){
			strip.setPixelColor(in.module, Color(in.red, in.green, in.blue) );
			strip.show();
		}else{
			strip.setPixelColor(0, Color(in.red, in.green, in.blue) );
			strip.setPixelColor(1, Color(in.red, in.green, in.blue) );
			strip.show();
		}
		last_command = in.command;

	}else if( in.command == 3 ){	
	}
}

// SLAVE
void loop(){
  if(!Mirf.isSending() && Mirf.dataReady()){
    Mirf.getData((byte *) &din);
	  Serial.print("show command:\t");
	  //Serial.print(din.module);
	  //Serial.print(", command:  "); 
	  Serial.print(din.command);
	  Serial.print(" = "); 
	  Serial.print(din.red);
	  Serial.print("/");
	  Serial.print(din.green);
	  Serial.print("/");
	  Serial.print(din.blue);
	  Serial.print(" to ");
	  Serial.println(din.dest); 

    if(din.dest != role && din.dest != 255 ){		// 255 = all
		return;
	}

	use_command( din );

    Mirf.setTADDR((byte *)"serv1");
    din.dest	= 0;
    din.sender	= role;
    delay(100);
      Serial.print("send command:\t");
	  Serial.println(din.command);
      Mirf.send((byte *) &din);
	  while(Mirf.isSending()){
	  }
	delay(10);

  }
 
}