volatile uint8_t licznik = 0;

#include <I2C_Send.h>
#include <AccelStepperMini.h>
#include <Wire.h>
// to jest master
#define MY_ADDR 0x02
#define MASTER_ADDR 0x02
#define VERSION 0x01
#define DEVICE_TYPE 0x12
int led = 13;
byte x = 0;
byte y = 10;
  byte address = 0x04;
byte in_buffer[10];
byte out_buffer[5];


void printHex(byte val){
  int temp =  val;
  
}



byte GetRegisters(byte deviceAddress, byte command, byte length){
  Wire.beginTransmission(deviceAddress);
  Wire.write(command);
  int error = Wire.endTransmission();
  delay(10);

  byte counter = 0;
  Wire.requestFrom(deviceAddress, length);
  delay(10);
  byte waits = 100;
 
  if(waits==0){
    return 0x32;
  }
  while(Wire.available()){    // slave may send less than requested
    in_buffer[counter++] = Wire.read(); 
  };
  if(counter <length){

  }

  return 1;
}


void writeRegister(int deviceAddress, byte command, byte val, byte val2) {
    Wire.beginTransmission(deviceAddress); // start transmission to device 
    Wire.write(command); 
    Wire.write(val);
    Wire.write(val2);


    printHex(command);
    printHex(val);
    printHex(val2);
    
    int error = Wire.endTransmission();    // end transmission
    delay(10);
}


// wysyla dowolną ilosc liczb na kanal
void writeRegisters(int deviceAddress, byte length, boolean wait) {
    byte c = 0;
    Wire.beginTransmission(deviceAddress); // start transmission to device 
    while( c < length ){
      Wire.write(out_buffer[c]);         // send value to write

      printHex(out_buffer[c]);

      c++;
    }
    int error = Wire.endTransmission();     // end transmission
    if(wait){
      delay(10);
    }
}
byte readRegister(int deviceAddress, byte command){

    Wire.beginTransmission(deviceAddress);
    Wire.write(command); // register to read
    int error = Wire.endTransmission();
    delay(10);


    Wire.requestFrom(deviceAddress, 1); // read a byte
    return Wire.read();
}

AccelStepperMini stepperX(1, 9, 10);      // Step, DIR

unsigned int last_max_x = 655555;

long int margin_x = 0;          // odstępstwo od technicznej pozycji X

long int dlugosc_x = 0;

int dlugosc_z = 100;

boolean send_ret = true;        // czy wysyłać odpowiedzi po wykonaniu komend
boolean last_stepper_operation = false; 
boolean irr_handled_x = true;
boolean irr_min_x = false;          //czy jestem na dole?
boolean irr_max_x = false;


boolean irr_x  = HIGH;			// czy dotykam przerwania




const byte MY_ADDRESS = 42;
volatile boolean haveData = false;
volatile long fnum;
volatile long foo;


void setup(){


	stepperX.setAcceleration(300);    // lewo prawo
	stepperX.setMaxSpeed(200);

	Wire.begin (MY_ADDRESS);
	Wire.onReceive (receiveEvent);

}
long int posx(){
	return (stepperX.currentPosition() - margin_x) /7;
}


void loop(){
	run_steppers();
	 if (haveData){
  
    haveData = false;  
    } 
 writeRegister( address, 0x11, x, y );
      x++;
      y--;

      byte res = readRegister( address, 0x66 );
      printHex(res);
      
      byte readed = GetRegisters(address, 0x66, 1);

      printHex(in_buffer[0]);
      printHex(in_buffer[1]);
      printHex(in_buffer[2]);
  

      readed = GetRegisters(address, 0x88, 5);
  
      printHex(in_buffer[0]);
      printHex(in_buffer[1]);
      printHex(in_buffer[2]);
      printHex(in_buffer[3]);
      printHex(in_buffer[4]);
      printHex(in_buffer[5]);
      printHex(in_buffer[6]);
      printHex(in_buffer[7]);
      printHex(in_buffer[8]);


}

// called by interrupt service routine when incoming data arrives
void receiveEvent (int howMany) {
 if (howMany >= (sizeof fnum) + (sizeof foo))
   {
   I2C_readAnything (fnum);   
   I2C_readAnything (foo);   
   haveData = true;     
   }  // end if have enough data
 }  // end of receiveEvent

void run_steppers(){    // robione w każdym przebiegu loop
	long int dist_x = stepperX.distanceToGo();


	if( dist_x == 0 ){    // zajechalem 

		if( last_stepper_operation ){    // byla komenda
			stepperX.stopNow();
			if(send_ret){
				return;
			}
			last_stepper_operation = false;
		}
	}else{
		irr_x  = digitalRead( 8 );
		if( irr_x == LOW && dist_x != 0 ){		// wcisniete a mam gdzies jechac
			if( irr_min_x && dist_x < 0 ){    	// stoje na dole a mam jechac w dół
			  stepperX.stopNow();
			  dist_x = 0;
			}else if( irr_max_x && dist_x > 0 ){    // stoje na górze a mam jechac w góre
			  stepperX.stopNow();
			  dist_x = 0;
			}
		}
		if( irr_x == LOW && !irr_handled_x ) {      // wciśnięte = bylo przerwanie X i jeszcze nie jest obsluzone
			if( dist_x < 0 ){				       // jechalem w dół
			  margin_x = stepperX.currentPosition();				      // to jest pozycja skrajna
			  stepperX.stopNow();
			  irr_min_x = true;      // to jest minimum
			  dlugosc_x = last_max_x;

			}else if( dist_x > 0 ){			 // jechalem w gore
			  stepperX.stopNow();
			  irr_max_x = true;
			  dlugosc_x = last_max_x;

			}else{
			}
			irr_handled_x = true;
		}else if(dist_x != 0){
			stepperX.run();
		}
		
		if( dist_x != 0 ){			// mam jechac
			// skrajny puszczony lub wcisniety ale jade w inną strone
			if(irr_x == HIGH || ( irr_min_x && dist_x < 0 ) || ( irr_max_x && dist_x > 0 ) ){	
				stepperX.run();
			}
		}
		

	}
}
