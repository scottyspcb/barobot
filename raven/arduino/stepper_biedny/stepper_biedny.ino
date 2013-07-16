#include <Stepper.h>

const int stepsPerRevolution = 500;
//Stepper myStepper(stepsPerRevolution, 3,4,5,6);
Stepper myStepper(stepsPerRevolution, 28,29,30,31);

void setup() {
  // set the speed at 60 rpm:
  myStepper.setSpeed(20);
  // initialize the serial port:
  Serial.begin(115200);
}

void loop() {
  // step one revolution  in one direction:
   Serial.println("clockwise");
  myStepper.step(stepsPerRevolution);
  delay(100);
   // step one revolution in the other direction:
  Serial.println("counterclockwise");
  myStepper.step(-stepsPerRevolution);
  delay(100);
}

