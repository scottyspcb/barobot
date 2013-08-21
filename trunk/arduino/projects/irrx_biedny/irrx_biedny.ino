#include <AccelStepper.h>

#define PIN_MADDR0 30
#define PIN_MADDR1 32
#define PIN_MADDR2 34
#define PIN_MADDR3 36

//AccelStepper stepper(28,29,30,31); // Defaults to AccelStepper::FULL4WIRE (4 pins) on 2, 3, 4, 5
//AccelStepper stepper(4, 28,29,30,31 );

AccelStepper stepper(4, 46,47,48,49 );
//AccelStepper stepper(4, 40,41,42,43 );
//AccelStepper stepper(1, 46,47 );

volatile unsigned long encoder_diff_x = 0;    // przejechane przez enkoder

void setup(){
  Serial.begin(115200);
  stepper.setMaxSpeed(400);
  stepper.setAcceleration(500);
  stepper.moveTo(400);
  pinMode( 13, OUTPUT); 
  pinMode( PIN_MADDR0, OUTPUT);      // adresowanie wyjscia w multiplekserze
  pinMode( PIN_MADDR1, OUTPUT);
  pinMode( PIN_MADDR2, OUTPUT);
  pinMode( PIN_MADDR3, OUTPUT);
  stepper.setMinPulseWidth(20);
//  pinMode(INT4, INPUT); 
//  digitalWrite(INT4, HIGH);       // turn on pullup resistor  
  attachInterrupt( INT4, on_int4R, CHANGE);    // nasłuchuj zmiany PIN 19  // Enkoder X
  stepper.disableOutputs();
}

void on_int4R(){    
  encoder_diff_x++;
  digitalWrite( 13, encoder_diff_x%2);
  digitalWrite(PIN_MADDR0, bitRead(encoder_diff_x,0) );      //   // Ustaw numer na muxach, wystaw adres
  digitalWrite(PIN_MADDR1, bitRead(encoder_diff_x,1) );
  digitalWrite(PIN_MADDR2, bitRead(encoder_diff_x,2) );
  digitalWrite(PIN_MADDR3, bitRead(encoder_diff_x,3) );
}

void loop(){
  if (stepper.distanceToGo() == 0){
      stepper.disableOutputs();
      Serial.println(  "IRR:" + String(encoder_diff_x) );
      delay(1500);
      encoder_diff_x = 0;
      stepper.enableOutputs();
      stepper.moveTo(-stepper.currentPosition());      
    }
     stepper.run();

//    Serial.println(  "IRR:" + String(encoder_diff_x) );
  // delay(1000);

 }

