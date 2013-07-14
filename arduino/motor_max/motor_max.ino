// Bounce.pde
// -*- mode: C++ -*-
//
// Make a single stepper bounce from one limit to another
//
// Copyright (C) 2012 Mike McCauley
// $Id: Random.pde,v 1.1 2011/01/05 01:51:01 mikem Exp mikem $

#include <AccelStepper.h>

// Define a stepper and the pins it will use
AccelStepper stepper(1, 46, 47);      // Step, DIR

void setup()
{  
  // Change these to suit your stepper if you want
  stepper.setMaxSpeed(20000);
  stepper.setAcceleration(1000000);
  stepper.moveTo(1000500);
}

void loop()
{
    // If at the end of travel go to the other end
  //  if (stepper.distanceToGo() == 0)
//      stepper.moveTo(-stepper.currentPosition());

    stepper.run();
}
