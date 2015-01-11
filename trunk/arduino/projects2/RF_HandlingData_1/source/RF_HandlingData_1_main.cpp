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

int role = 1;

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
  Serial.println("Listening..."); 
}

void loop(){

 // byte data[Mirf.payload];
  unsigned long time = 0;
  
  /*
   * If a packet has been recived.
   *
   * isSending also restores listening mode when it 
   * transitions from true to false.
   */
   
  if(!Mirf.isSending() && Mirf.dataReady()){
    Serial.println("Got packet");
    
    /*
     * Get load the packet into the buffer.
     */
     
    Mirf.getData((byte *) &time);
    Serial.print("get: ");
    Serial.println(time);
    /*
     * Set the send address.
     */
     
     
    Mirf.setTADDR((byte *)"clie1");
    delay(100);
    /*
     * Send the data back to the client.
     */
     
      Serial.print("send: ");
      Serial.print(time);

    Mirf.send((byte *) &time);
    
    /*
     * Wait untill sending has finished
     *
     * NB: isSending returns the chip to receving after returning true.
     */
      
    Serial.println("Reply sent.");
  }
}