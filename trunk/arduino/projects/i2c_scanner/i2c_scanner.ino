#include <WSWire.h>

void setup(){
  Wire.begin(0x01);
  pinMode(5,INPUT_PULLUP);
  pinMode(6,INPUT_PULLUP);
  Serial.begin(115200);
  Serial.println("\nI2C Scanner");
}
byte error, address;
int nDevices;

void loop(){
  Serial.println("Scanning...");
    Wire.begin(0x01);
  nDevices = 0;
  for(address = 1; address < 20; address++ )   {
    Wire.beginTransmission(address);
    error = Wire.endTransmission();

    if (error == 0){
      Serial.print("I2C device found at address: ");
      Serial.print(address,HEX);
      Serial.println("  !");

      nDevices++;
    }else{
     Serial.println("ERROR:"+String(address)+" / "+String(error));
    }
  }
  Serial.println("done\n");
  delay(500);           // wait 5 seconds for next scan
}

