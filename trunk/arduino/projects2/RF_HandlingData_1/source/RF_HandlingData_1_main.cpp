
/*
* Getting Started example sketch for nRF24L01+ radios
* This is an example of how to send data from one node to another using data structures
* Updated: Dec 2014 by TMRh20
*/

#include <SPI.h>
#include "RF24.h"

//byte addresses[][6] = {"1Node","2Node"};
const uint64_t addresses[2] = { 0xF0F0F0F0E1LL, 0xF0F0F0F0D2LL };

/****************** User Config ***************************/
/***      Set this radio as radio number 0 or 1         ***/
bool radioNumber	= 0;
bool role 			= !radioNumber;


/* Hardware configuration: Set up nRF24L01 radio on SPI bus plus pins 7 & 8 */
RF24 radio(9,10);

// uint8_t _cepin, uint8_t _cspin)

/**********************************************************/

// Used to control whether this node is sending or receiving


/**
* Create a data structure for transmitting and receiving data
* This allows many variables to be easily sent and received in a single transmission
* See http://www.cplusplus.com/doc/tutorial/structures/
*/
struct dataStruct{
  unsigned long _micros;
  int value;
};

dataStruct din	= {0,0};
dataStruct out	= {0,0};


void setup() {
  Serial.begin(115200);
  if(role){
		Serial.println(F("*** ROLE 1. CHANGING TO TRANSMIT ROLE -- PRESS '0' TO SWITCH BACK TO ROLE 0"));	
	}else{
		Serial.println(F("*** ROLE 0. CHANGING TO RECEIVE ROLE -- PRESS '1' TO SWITCH BACK TO ROLE 1"));     
	}
  
  radio.begin();
  radio.setRetries(15,15);
  radio.setDataRate(RF24_250KBPS);
  radio.setChannel(76);
    
  // Set the PA Level low to prevent power supply related issues since this is a
 // getting_started sketch, and the likelihood of close proximity of the devices. RF24_PA_MAX is default.
  radio.setPALevel(RF24_PA_LOW);
  
  // Open a writing and reading pipe on each radio, with opposite addresses
  if(radioNumber){
    radio.openWritingPipe(addresses[1]);
    radio.openReadingPipe(1,addresses[0]);
  }else{
    radio.openWritingPipe(addresses[0]);
    radio.openReadingPipe(1,addresses[1]);
  }

  
  
  
  
  // Start the radio listening for data
  radio.startListening();
  radio.printDetails();
  
  int pl = radio.getPayloadSize();
  
  Serial.print(F("Payload: "));
  Serial.println(pl); 
}


void loop() {

/****************** Ping Out Role ***************************/  
if (role == 1)  {
    unsigned long time = micros();
    out._micros = time;
    out.value++;
    Serial.print(F("1 - Now sending value: "));
    Serial.println(out.value);
  
    radio.stopListening();  
    if (!radio.write( &out, sizeof(out) )){
       Serial.println(F("1 - Write failed"));
   //    radio.startListening(); 
    }
  
//	Serial.println(F("1 - Write OK"));
	radio.startListening();                                    // Now, continue listening
	
	unsigned long started_waiting_at = micros();               // Set up a timeout period, get the current microseconds
	boolean timeout = false;                                   // Set up a variable to indicate if a response was received or not

	while ( ! radio.available() ){                             // While nothing is received
	  if (micros() - started_waiting_at > 1900000 ){            // If waited longer than 900ms, indicate timeout and exit while loop
		  timeout = true;
		  break;
	  }
	}
		
	if ( timeout ){                                             // Describe the results
		Serial.println(F("1 - Failed, response timed out. stop"));
	}else{                                                      // Grab the response, compare, and send to debugging spew
		radio.read( &din, sizeof(din) );
		// Spew it
		Serial.print(F("1 !!! Recieved in time:"));
	//    Serial.print(time);
	//    Serial.print(F(", Got response micros: "));
	 //   Serial.print(myData._micros);
	//    Serial.print(F(", Round-trip delay "));
		Serial.print(time-din._micros);
		Serial.print(F(" new value "));
		Serial.println(din.value);
	}

    // Try again 1s later
    delay(1000);
  }

  if ( role == 0 )
  {
    if( radio.available()){
                                                           // Variable for the received timestamp
      while (radio.available()) {                          // While there is data ready
        radio.read( &din, sizeof(din) );             // Get the payload
      }
      Serial.print(F("0 - Recieved micros "));
      Serial.print(din._micros);  
      Serial.print(" value : ");
      Serial.println(din.value);
	  
      din.value += 100;                                 // Increment the float value

      Serial.print(F("0 - Sending micros "));
      Serial.print(din._micros);  
      Serial.print(" value : ");
      Serial.println(din.value);

      radio.stopListening();                               	// First, stop listening so we can talk  
      bool res = radio.write( &din, sizeof(din) );              // Send the final one back.      
		if (!res){
			   Serial.println(F("0 - send failed"));
		}

      radio.startListening();                              // Now, resume listening so we catch the next packets.    

   }
 }


/****************** Change Roles via Serial Commands ***************************/
  if ( Serial.available() )
  {
    char c = toupper(Serial.read());
    if ( c == '1' && role == 0 ){      
      Serial.println(F("*** ROLE 1. CHANGING TO TRANSMIT ROLE -- PRESS '0' TO SWITCH BACK TO ROLE 0"));
      role = 1;                  // Become the primary transmitter (ping out)
      radio.startListening();
   }else
    if ( c == '0' && role == 1 ){
      Serial.println(F("*** ROLE 0. CHANGING TO RECEIVE ROLE -- PRESS '1' TO SWITCH BACK TO ROLE 1"));      
       role = 0;                // Become the primary receiver (pong back)
       radio.startListening();
    }
  }


} // Loop
