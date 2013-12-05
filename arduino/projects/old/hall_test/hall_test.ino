int sensorValue = 0;
void setup() {
  Serial.begin(115200); 
}

void loop() {
  sensorValue = 0;
  for(byte i =0;i<16;i++){
    sensorValue += analogRead(A0);  
  }
  sensorValue = sensorValue>>4;

  Serial.print("A ");
  Serial.println(sensorValue);
  delay(2);                     
}
