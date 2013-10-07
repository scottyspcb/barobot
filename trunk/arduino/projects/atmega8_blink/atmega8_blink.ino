
// Pin 13 has an LED connected on most Arduino boards.
// give it a name:
int led = 8;

// the setup routine runs once when you press reset:
void setup() {                
  // initialize the digital pin as an output.
  pinMode(led, OUTPUT);
  Serial.begin(38400); 
//  pinMode( A0, INPUT);
}

// the loop routine runs over and over again forever:
void loop() {

  int sw = analogRead( A0 );
  Serial.println(sw);
  if( sw > 1000 ){
    digitalWrite(led, HIGH);   // turn the LED on (HIGH is the voltage level)  
  }else{
    digitalWrite(led, LOW);    // turn the LED off by making the voltage LOW  
  }
}
