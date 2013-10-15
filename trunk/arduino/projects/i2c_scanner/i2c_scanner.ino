#include <Wire.h>

void setup(){
  Wire.begin();
  pinMode(5,INPUT_PULLUP);
  pinMode(6,INPUT_PULLUP);
  Serial.begin(9600);
  Serial.println("\nI2C Scanner");
}
byte error, address;
int nDevices;

void loop(){
  Serial.println("Scanning...");
  nDevices = 0;
  for(address = 1; address < 127; address++ )   {
    Wire.beginTransmission(address);
    error = Wire.endTransmission();

    if (error == 0){
      Serial.print("I2C device found at address: ");
      Serial.print(address,HEX);
      Serial.println("  !");

      nDevices++;
    }
    else if (error==4){
      Serial.print("Unknow error at address: ");
      Serial.println(address,HEX);
    }
  }
  Serial.println("done\n");
  delay(500);           // wait 5 seconds for next scan
}

