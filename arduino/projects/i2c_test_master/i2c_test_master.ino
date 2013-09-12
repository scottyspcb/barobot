#include <Wire.h>
int x = 1;

// to jest master
#define MY_ADDR 02
#define SLAVE_ADDR 04
#define MASTER_ADDR 02

void setup()  {
  Serial.begin(115200); 
  Wire.begin(MY_ADDR);
  Wire.onReceive(receiveEvent);
  Serial.println("START MASTER");
}
void loop(){
  Wire.beginTransmission(SLAVE_ADDR); // transmit to device #4  
  Wire.write("master x is ");
  Wire.write(x); // sends one byte
  Wire.endTransmission(); // stop transmitting
  x++;  
  delay(500);
}

void receiveEvent(int howMany){
    while( Wire.available()){ // loop through all but the last  
        char c = Wire.read(); // receive byte as a character
        Serial.print(c); // print the character  
     }
     Serial.println();
}
