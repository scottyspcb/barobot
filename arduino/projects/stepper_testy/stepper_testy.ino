#include <AccelStepper.h>

#define SEPARATOR_CHAR '\n'
//AccelStepper stepper(28,29,30,31); // Defaults to AccelStepper::FULL4WIRE (4 pins) on 2, 3, 4, 5
//AccelStepper stepper(4, 28,29,30,31 );
//AccelStepper stepper(4, 46,47,48,49 );
//AccelStepper stepper(4, 40,41,42,43 );
//AccelStepper stepper(1, 8,9,10,11);
AccelStepper stepper(1, 4, 5 );

String serial0Buffer = "";
boolean Console0Complete = false;   // This will be set to true once we have a full string
boolean dir = true;
long unsigned target = 00;

void setup(){
  Serial.begin(115200); 
  stepper.setMaxSpeed(400);
  stepper.setAcceleration(2500);
  stepper.disable_on_ready = true;
//  stepper.moveTo(500);
//  stepper.setMinPulseWidth(20);
  stepper.setEnablePin(14);
  stepper.setPinsInverted( false, false, true ); // enable pin invert
  Serial.println("HELLO"); 
  Serial.println("example: 10,25,100,1"); 
  Serial.println("example: 10,25,100,2"); 
  Serial.println("example: 20,65,200,1");
  
  
  pinMode(15, OUTPUT);
  pinMode(16, OUTPUT);
  pinMode(17, OUTPUT);
  digitalWrite(15, HIGH );
  digitalWrite(16, HIGH );
  digitalWrite(17, HIGH );
}

void loop(){
    if( target > 0 ){
      if (stepper.distanceToGo() == 0){
        delay(2000);
        if( dir ){
          Serial.println("UP -" + String(target*2)); 
          stepper.move(-target*2); 
        }else{
          Serial.println("DOWN " + String(target*2));
          stepper.move(target*2);
        }
        dir = !dir;
      }
    }
    stepper.run();
  if (Console0Complete) {
    parseInput( serial0Buffer );                      // parsuj wejscie
    Console0Complete = false;
    serial0Buffer = "";
  }
}

//  format:  MAXSPEED,ACCELERATION,TARGET,MICROSTEPPING
// np 111,222,333,444

void parseInput( String input ){   // zrozum co sie dzieje
  input.trim();
  int comma = input.indexOf(',');
  byte pos = 0;
  Serial.println("------------------NOWY CONFIG-----------------"); 
  while( comma != -1 ){
      String current  = input.substring(0, comma);
      input           = input.substring(comma + 1 );    // wytnij od tego znaku
      setValue( pos, current );
      comma = input.indexOf(',');
      pos++;
  }
  if( input.length() > 0 ){
      setValue( pos, input );
  }  
  Serial.println("----------------------------------------------"); 
}

 // MAXSPEED,ACCELERATION,TARGET,PULSEWIDTH
void setValue(byte pos, String value ){
  long unsigned val = decodeInt( value, 0 );
  if( pos == 0 ){               // MAXSPEED
    val = val * 100;
    stepper.setMaxSpeed(val);
    Serial.println("setMaxSpeed: " + String(val) );
  }else if( pos == 1 ){         // ACCELERATION
    val = val * 100;
    stepper.setAcceleration(val);
    Serial.println("setAcceleration: " + String(val) );
  }else if( pos == 2 ){         // TARGET
    val = val * 10;
    Serial.println("moveTo: " + String(val) );
    target = val;
    stepper.move(target*2);
  }else if( pos == 3 ){         // MICROSTEPPING
    stepper.setInterface(val);
    if(val == 0 ){
       Serial.println("microstepping OFF"); 
    }else if( val == 1 ){
       Serial.println("microstepping DRIVER"); 
    }else if( val == 2 ){
       Serial.println("microstepping FULL2WIRE"); 
    }else if( val == 4 ){
       Serial.println("microstepping FULL4WIRE"); 
    }else if( val == 8 ){
       Serial.println("microstepping HALF4WIRE"); 
    }
  }
}

long unsigned decodeInt(String input, int odetnij ){
  long pos = 0;
  if(odetnij>0){
    input = input.substring(odetnij);    // obetnij znaki z przodu
  }
  pos = input.toInt();
  return pos;
}

void serialEvent(){                       // FUNKCJA WBUDOWANA - zbieraj dane z serial0 i serial3 i skadaj w komendy
  while (Serial.available() && !Console0Complete) {    // odczytuj gdy istnieja dane i poprzednie zostaly odczytane
    char inChar = (char)Serial.read(); 
    serial0Buffer += String(inChar);
    if (inChar == SEPARATOR_CHAR) {
      Console0Complete = true;
    }
  }
}

// użycie:
/*

MAXSPEED,ACCELERATION,TARGET,MICROSTEPPING

Dla dużego:
500,2500,500,8
500,2500,500,8

dla easydrivera:
500,2500,500,0
500,2500,500,1
500,2500,500,2

1000,2500,2000,1


DRIVER    = 1, ///< Stepper Driver, 2 driver pins required
FULL2WIRE = 2, ///< 2 wire stepper, 2 motor pins required
FULL4WIRE = 4, ///< 4 wire full stepper, 4 motor pins required
HALF4WIRE = 8  ///< 4 wire half stepper, 4 motor pins required

*/

