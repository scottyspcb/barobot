#include <Wire.h>
int x = 1;

// to jest slave
#define MY_ADDR 04
#define SLAVE_ADDR 04
#define MASTER_ADDR 02

void setup(){
  Serial.begin(115200); 
  Wire.begin(MY_ADDR);
  Wire.onReceive(receiveEvent); // register event   
  Serial.println("START SLAVE"); 
}
void loop(){
  Wire.beginTransmission(MASTER_ADDR);
  Wire.write("slave x is ");
  Wire.write(x); 
  Wire.endTransmission();
  x++;
  delay(100);
}

void receiveEvent(int howMany){
    while( Wire.available()){ // loop through all but the last  
        char c = Wire.read(); // receive byte as a character
        Serial.print(c); // print the character  
     }
     Serial.println();
}



