/**
 * Pins:
 * Hardware SPI:
 * MISO -> 12
 * MOSI -> 11
 * SCK -> 13
 */

#include <SPI.h>
#include <Mirf.h>
#include <nRF24L01.h>
#include <MirfHardwareSpiDriver.h>

int role = 0;

struct command{
	uint8_t module;
	uint8_t command;
	uint8_t red;
	uint8_t green;
	uint8_t blue;
	unsigned long _micros;
};
byte cmdsize=sizeof(command);

command din	= {0,0,0,0,0};
command out	= {0,0,0,0,0};

void setup(){
  Serial.begin(115200);
  Serial.println("start..."); 
  Mirf.spi = &MirfHardwareSpi;
  Mirf.csnPin = 10; //(This is optional to change the chip select pin)
  Mirf.cePin = 9; //(This is optional to change the enable pin)
  Mirf.init();
  if( role == 0 ){
	Mirf.setRADDR((byte *)"clie1");
  }else{
	Mirf.setRADDR((byte *)"serv1");
  }
  Mirf.payload = sizeof(unsigned long);
  //Mirf.channel = 10;
  Mirf.config();
  Serial.println("Beginning ... "); 
}

boolean error = false;

void loop(){
  unsigned long time = millis();
  
  Mirf.setTADDR((byte *)"serv1");
  
  Mirf.send((byte *)&time);
  
  while(Mirf.isSending()){
  }

  Serial.println("Finished sending");
  delay(10);
  error = false;
  while(!Mirf.dataReady()){
    //Serial.println("Waiting");
    if ( ( millis() - time ) > 4000 ) {
      Serial.println("Timeout on response from server!");
      error = true;
      break;
    }
  }
	if(error){
	 Serial.print("error");
  
	}else{
		time = 0;
	  Mirf.getData((byte *) &time);
	//  Serial.print("Ping: ");
	//  Serial.println((millis() - time));
	  Serial.print("time: ");
	  Serial.println(time); 
  }
  delay(1000);
} 
  
  
  