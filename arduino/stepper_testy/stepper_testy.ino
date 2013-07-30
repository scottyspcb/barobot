#include <AccelStepper.h>

#define SEPARATOR_CHAR '\n'
//AccelStepper stepper(28,29,30,31); // Defaults to AccelStepper::FULL4WIRE (4 pins) on 2, 3, 4, 5
//AccelStepper stepper(4, 28,29,30,31 );

//AccelStepper stepper(4, 46,47,48,49 );
//AccelStepper stepper(4, 40,41,42,43 );
AccelStepper stepper(8, 8,9,10,11);
//AccelStepper stepper(1, 46,47 );

String serial0Buffer = "";
boolean Console0Complete = false;   // This will be set to true once we have a full string
boolean dir = true;
long unsigned target = 500;

void setup(){   
  Serial.begin(115200); 
  stepper.setMaxSpeed(400);
  stepper.setAcceleration(2500);
  stepper.moveTo(500);
  stepper.setMinPulseWidth(20);
}
void loop(){
    if (stepper.distanceToGo() == 0){
      delay(2000);
      if( dir ){
        Serial.println("UP"); 
        stepper.move(-target*2); 
      }else{
        Serial.println("DOWN");  
        stepper.move(target*2);
      }
      dir = !dir;
    }
    stepper.run();
  if (Console0Complete) {
    parseInput( serial0Buffer );                      // parsuj wejscie
    Console0Complete = false;
    serial0Buffer = "";
  }
}

///  format:  MAXSPEED,ACCELERATION,TARGET,PULSEWIDTH,MICROSTEPPING
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
  long int val = decodeInt( value, 0 );
  if( pos == 0 ){               // MAXSPEED
    stepper.setMaxSpeed(val);
    Serial.println("setMaxSpeed: " + String(val) );
  }else if( pos == 1 ){         // ACCELERATION
    stepper.setAcceleration(val);
     Serial.println("setAcceleration: " + String(val) );
  }else if( pos == 2 ){         // TARGET
    Serial.println("moveTo: " + String(val) );
    target = val;
  }else if( pos == 3 ){         // PULSEWIDTH
    stepper.setMinPulseWidth(val);
    Serial.println("setMinPulseWidth: " + String(val) );
  }else if( pos == 4 ){         // MICROSTEPPING
    if(val == 0 ){
       stepper.setInterface(AccelStepper::FULL4WIRE);
       Serial.println("microstepping OFF"); 
    }else{
       stepper.setInterface(AccelStepper::HALF4WIRE);
       Serial.println("microstepping ON"); 
    }
  }
}





long decodeInt(String input, int odetnij ){
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

MAXSPEED,ACCELERATION,TARGET,PULSEWIDTH,MICROSTEPPING
400,2500,500,20,1

300,2500,500,20,1



*/

