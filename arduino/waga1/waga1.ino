// Enter you own analog values here
//float loadA = 10; // kg
//int analogvalA = 200; // analog reading taken with load A on the load cell

//float loadB = 30; // kg 
//int analogvalB = 600; // analog reading taken with load B on the load cell

int waga_min  = 13;
int waga_max  = 864;
int waga_lmin  = 0;
int waga_lmax  = 2000;

int PIN_MADDR0 = 24;
int PIN_MADDR1 = 26;
int PIN_MADDR2 = 28;
int PIN_MADDR3 = 30;

int PIN_WAGA_ANALOG = A0;

void setup() {
  Serial.begin(115200);
  pinMode(24, OUTPUT);
  pinMode(26, OUTPUT);
  pinMode(28, OUTPUT);
  pinMode(30, OUTPUT);
}

unsigned long skaluj(unsigned long value){
//  unsigned long tt =  maplong(value, waga_min, waga_max, waga_lmin, waga_lmax);
//  return tt;
    return value;
}

unsigned long read_butelka(byte numer){
  digitalWrite(PIN_MADDR0, bitRead(numer,0) );      //   // Ustaw numer na muxach, wystaw adres
  digitalWrite(PIN_MADDR1, bitRead(numer,1) );
  digitalWrite(PIN_MADDR2, bitRead(numer,2) );
  digitalWrite(PIN_MADDR3, bitRead(numer,3) );
  unsigned long rr = 0;
  delay(10);
  int j=0;
  for( j=0;j<2;j++){
    rr+= analogRead(PIN_WAGA_ANALOG);
  }
  return rr >>1;
}

void loop() {
 //  int analogValue = analogRead(A1);
  // running average - We smooth the readings a little bit
    String res = "";
    Serial.print("Load ");
//    for (byte count=0; count<4; count++) {              // 16 razy
    for (byte count=4; count>0; count--) {
      unsigned long waga = read_butelka(count-1);
   ///   waga = skaluj(waga);
      res = String(waga);    // odczytaj wartosc
      res = res + ',';
      Serial.print(res);
//      delay(20);
    }
    Serial.println("");
  //analogValueAverage = 0.99*analogValueAverage + 0.01*analogValue;
//  if(millis() > time + timeBetweenReadings){
  //  float load = analogToLoad(analogValueAverage);
  //  Serial.print("analogValue: ");Serial.println(analogValueAverage);
  //  Serial.print("             load: ");Serial.println(load,5);
  //  time = millis();
//  }
}
/*
float analogToLoad(float analogval){
  // using a custom map-function, because the standard arduino map function only uses int
  float load = mapfloat(analogval, analogvalA, analogvalB, loadA, loadB);
  return load;
}

float mapfloat(float x, float in_min, float in_max, float out_min, float out_max){
  return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
}
*/
unsigned long int maplong(unsigned long x, unsigned long in_min, unsigned long in_max, unsigned long out_min, unsigned long out_max){
  return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
}

