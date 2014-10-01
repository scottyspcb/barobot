#include <Servo.h> 
 
Servo myservo;  // create servo object to control a servo 
                // a maximum of eight servo objects can be created 
int pos = 0;    // variable to store the servo position 
int keep = 0;
 
void setup() { 
  myservo.attach(9);  // attaches the servo on pin 9 to the servo object 
  Serial.begin(115200); 
  Serial.println("HELLO");

} 
 
 
void loop() { 
    Serial.println(String(900)); 
  
  myservo.writeMicroseconds(900);              // do góry
  delay(1000);
/*
  for(keep = 0; keep < 2000; keep += 1)  {
    myservo.writeMicroseconds(900); 
    delay(2);
  } 
 */
  
   Serial.println(String(2100));
  myservo.writeMicroseconds(2100);             // na doł
  delay(1000);
  
   Serial.println(String(910));
  myservo.writeMicroseconds(910);              // do góry
  delay(500);
  
   Serial.println(String(2100));
  myservo.writeMicroseconds(2100);             // na doł
  delay(1000);


   /*

  for(keep = 0; keep < 100; keep += 1)  // goes from 0 degrees to 180 degrees 
  {                                  // in steps of 1 degree 
    myservo.writeMicroseconds(910);              // tell servo to go to position in variable 'pos' 
    delay(2);                       // waits 15ms for the servo to reach the position 
  }   */
}



