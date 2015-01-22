/**
	master
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

String serial0Buffer = "";
int role			= 0;				// master
uint8_t dataPin		= 8;
uint8_t clockPin	= 7;

Adafruit_WS2801 strip = Adafruit_WS2801( 2, dataPin, clockPin );

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

command din	= {0,0,0,0,0,0,0,0};
command out	= {0,0,0,0,0,0,0,0};

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
	Mirf.setRADDR((byte *)"clie1");
  }else if( role == 2 ){
	Mirf.setRADDR((byte *)"clie2");
  }
  Mirf.payload = cmdsize;
  Mirf.channel = 10;
  Mirf.config();
	Serial.println("SERVER START"); 
	Serial.print("ROLE ");
	Serial.println(role);
  strip.begin();
  strip.show();
	serial0Buffer = "";
}

boolean error = false;

void sentTo(byte address, command out ){
	if(address == 1 ){
		Mirf.setTADDR((byte *)"1clie");
	}else if(address == 2 ){
		Mirf.setTADDR((byte *)"2clie");
	}
	out.dest	= address;
	out.sender	= role;

  Serial.print("-send to:\t");
  Serial.print(out.dest);
  Serial.print(", command:  "); 
  Serial.print(out.command);
  Serial.print(" = "); 
  Serial.print(out.red);
  Serial.print("/");
  Serial.print(out.green);
  Serial.print("/");
  Serial.println(out.blue);
  Mirf.send((byte *)&out);
}

void sentWithRet(byte address, command out ){
  unsigned long time = millis();

  sentTo(address, out);
  while(Mirf.isSending()){
  }
  delay(10);
  error = false;
  while(!Mirf.dataReady()){
    //Serial.println("Waiting");
    if ( ( millis() - time ) > 2000 ) {
      Serial.println("Timeout on response from server!");
      error = true;
      break;
    }
  }
  /*
	if(error){
	 Serial.println("recieve error");
	}else{
	  Mirf.getData((byte *) &din);
	//  Serial.print("Ping: ");
	//  Serial.println((millis() - time));
	  Serial.print("confirmation command: ");
	  Serial.print(din.command);
	  Serial.print(", from: ");  
	  Serial.print(din.dest);  
	  Serial.print(" = "); 
	  Serial.print(din.red);
	  Serial.print("/");
	  Serial.print(din.green);
	  Serial.print("/");
	  Serial.println(din.blue);
	  delay(10);
  }  */
}

boolean adr = false;
unsigned long lastTimeOn = 0;
unsigned long lastTimeOff = 0;

void loop(){
 //byte address = random(1,3);		// 1 or 2
 // byte address = adr ? 1 : 2;		// 1 or 2
 //adr = !adr;
	unsigned long time = millis();
	if( time > lastTimeOn){
		lastTimeOn	= time + 1000;
		byte r		= random(0,3);
		out.red		= 0;//random(255);
		out.green	= 0;//random(255);
		out.blue	= 0;//random(255);
		if(r == 0 ){
			out.red = 200;
		}else if(r == 1 ){
			out.green = 200;
		}else{
			out.blue = 200;
		}
		out.module	= 2;	// random(0,3);		// 0, 1 or 2
		out.command	= 1;	// on

		sentWithRet( 1, out );
		sentWithRet( 2, out );
	}
	/*
	if( time > lastTimeOff){
		lastTimeOff	= lastTimeOn + 500;
		out.red		= 0;
		out.green	= 0;
		out.blue	= 0;
		out.command	= 1;// on color
		sentWithRet( 1, out );
		sentWithRet( 2, out );
	}*/
}

void parseInput( String input ){
	input.trim();
	boolean defaultResult = true;
	byte command	= input.charAt(0);
	//byte il			= input.length();

	/*
		uint8_t sender;
		uint8_t dest;
		uint8_t module;
		uint8_t command;
		uint8_t red;
		uint8_t green;
		uint8_t blue;
	*/
	if( command == 'C' ) {    // command: C,100,100,100,100,100
		char charBuf[30];
		input.toCharArray(charBuf,30);
		
		// C,1,2,1,red,green,blue
		// C,1,2,1,255,0,0

		sscanf(charBuf,"C,%2hhx,%2hhx,%2hhx,%2hhx,%2hhx,%2hhx", &din.dest, &din.module , &din.command , &din.red , &din.green , &din.blue );
		sentWithRet( din.dest, din );
		
		
	}else if( input.equals( "RESET") ){
	}else{
		Serial.println("NO_CMD [" + input +"]");
	}
	if(defaultResult ){
		Serial.println("RR" + input );
		Serial.flush();
	}
}

void serialEvent(){				    // Runs after every LOOP (means don't run if loop hangs)
	while (Serial.available()) {    // odczytuj gdy istnieja dane i poprzednie zostaly odczytane
		char inChar = (char)Serial.read();
		serial0Buffer += String(inChar);
		if (inChar == '\n') {
			parseInput( serial0Buffer );				      // parsuj wejscie
			serial0Buffer = "";
		}
	}
}

/*
command
0- disable
1- on pernamently
2- show once and disable
3- blink fast (to black)

*/
