
void setup(){
  Serial.begin(115200);
  Serial.println(F("Internal Temperature Sensor"));
}

void loop(){
  // Show the temperature in degrees Celcius.
  Serial.println(GetTemp());
  delay(400);
}

int GetTemp(void){
  // The internal temperature has to be used
  // with the internal reference of 1.1V.
  // Channel 8 can not be selected with
  // the analogRead function yet.
  // Set the internal reference and mux.
//  ADMUX = (_BV(REFS1) | _BV(REFS0) | _BV(MUX3));
  ADMUX = 0;
  ADMUX |= _BV(REFS1);
  ADMUX |= _BV(REFS0);
  ADMUX |= 8;
  ADCSRA |= _BV(ADEN);  // enable the ADC

  delay(20);            // wait for voltages to become stable.
  ADCSRA |= _BV(ADSC);  // Start the ADC
  // Detect end-of-conversion
  while (bit_is_set(ADCSRA,ADSC));
  // Reading register "ADCW" takes care of how to read ADCL and ADCH.
  unsigned int wADC = ADCW;
  return wADC;
}
