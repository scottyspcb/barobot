#include <Servo.h> 
 
Servo myservo;  // create servo object to control a servo 
long int val;          // variable to read the value from the analog pin 
 
void setup() { 
  myservo.attach(4);  // attaches the servo on pin  to the servo object 
  Serial.begin(115200);
} 
 
unsigned int tryb = 1;
unsigned int ile = 0;
void loop() 
{ 
  val = analogRead(A0);              // reads the value of the potentiometer (value between 0 and 1023) 
//  val  =(val-200)*6;
  val = map(val, 0, 1023, 600, 2200); 
  myservo.writeMicroseconds(val);                // sets the servo position according to the scaled value 

  String val2 = String(val);
  Serial.println(val2);
  
  delay(100);                           // waits for the servo to get there
  
}
