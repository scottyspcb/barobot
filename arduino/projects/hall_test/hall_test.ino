const int analogInPin = A0;  // Analog input pin that the potentiometer is attached to
const int analogOutPin = 9; // Analog output pin that the LED is attached to

long int sensorValue = 0;        // value read from the pot


void setup() {
  Serial.begin(115200); 
}

void loop() {
  sensorValue = 0;
  for(byte i =0;i<16;i++){
    sensorValue += analogRead(analogInPin);  
  }
  sensorValue = sensorValue>>4;

  analogWrite(analogOutPin, sensorValue);      

  Serial.print("\t output = ");
  Serial.println(sensorValue);
  delay(2);                     
}
