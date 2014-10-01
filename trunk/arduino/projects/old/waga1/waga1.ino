
int PIN_WAGA_ANALOG = A0;

void setup() {
  Serial.begin(57600);
}

unsigned long skaluj(unsigned long value){
//  unsigned long tt =  maplong(value, waga_min, waga_max, waga_lmin, waga_lmax);
//  return tt;
    return value;
}

uint16_t last_waga = 0;
uint16_t rr = 0;
void loop() {
  rr =0;
  rr+= analogRead(PIN_WAGA_ANALOG);
  rr+= analogRead(PIN_WAGA_ANALOG);
  rr+= analogRead(PIN_WAGA_ANALOG);
  rr+= analogRead(PIN_WAGA_ANALOG);

  int16_t rr2 = rr>>2;

  int8_t diff = last_waga - rr;
  if( abs(diff) >2  ){
    Serial.println(String(rr));
    last_waga = rr;
  }
  delay(10);
  
  
}


