#include <AccelStepper.h>

// Define a stepper and the pins it will use
//AccelStepper stepper(28,29,30,31); // Defaults to AccelStepper::FULL4WIRE (4 pins) on 2, 3, 4, 5
//AccelStepper stepper(4, 28,29,30,31 );

AccelStepper stepper(4, 46,47,48,49 );
//AccelStepper stepper(4, 40,41,42,43 );
//AccelStepper stepper(1, 46,47 );

void setup(){   
  stepper.setMaxSpeed(400);
  stepper.setAcceleration(2500);
  stepper.moveTo(500);
  stepper.setMinPulseWidth(20);
}

void loop(){
    if (stepper.distanceToGo() == 0){
       delay(1000);
      stepper.moveTo(-stepper.currentPosition());
    }
    stepper.run();
}
