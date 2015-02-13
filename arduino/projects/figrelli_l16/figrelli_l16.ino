#define POS_FREQ 120

int pos_max = 620;           // max 1022
int pos_min = 270;           // min 1

//int pos_max = 990;           // max 1022
//int pos_min = 20;           // min 1

int pos_margin = 50;         // marines ponizej ktorego zmienjszaj moc (na krańcach zakresu osi Y)
int lowpower = 0;            // spowalnianie PWM w marginesie, 0-255, domyślnie 100, 0 wyłączone spowalnianie (wyłączać dla osi Z)

const int sensor = A0;
const int pwmsensor = A1;

uint8_t counter = 200;

uint8_t frequency = POS_FREQ;

int dest = 0;
int sensorReading = 0;
int last_pos = 0;

#define PIN_B2_OUT_Y1 3
#define PIN_B2_OUT_Y2 5
#define OUT_Y_ENABLE 9

void setup() {
 Serial.begin(115200);       // use the serial port
 pinMode(PIN_B2_OUT_Y1, OUTPUT);
 pinMode(PIN_B2_OUT_Y2, OUTPUT);
 
 
 pinMode(OUT_Y_ENABLE, OUTPUT);

 digitalWrite(PIN_B2_OUT_Y1, HIGH);
 digitalWrite(PIN_B2_OUT_Y2, LOW);
 digitalWrite(OUT_Y_ENABLE, HIGH);
 dest = 1;

 digitalWrite(OUT_Y_ENABLE, HIGH);
 analogWrite(OUT_Y_ENABLE, 255);
}

int lastpwm = 0;
int pwmread = 1023;
int pwm    = 255;

long unsigned int time_start_moving = 0;
long unsigned int now = 0;

void goDown(){
  //    digitalWrite(PIN_B2_OUT_Y1, LOW);
      digitalWrite(PIN_B2_OUT_Y2, HIGH);
      analogWrite(PIN_B2_OUT_Y1, pwm);
      

      time_start_moving = millis();
      dest = 2;
      Serial.println("now go down");
      last_pos = 0; 
      frequency = POS_FREQ;
}

void goUp(){
      digitalWrite(PIN_B2_OUT_Y1, HIGH);
    //  digitalWrite(PIN_B2_OUT_Y2, LOW);
      
      analogWrite(PIN_B2_OUT_Y2, pwm);
   //   analogWrite(PIN_B2_OUT_Y2, pwm);
      
      
      time_start_moving = millis();
      Serial.println("now go up");
      dest = 1;
      last_pos = 0;
      frequency = POS_FREQ;
}

void stopHere(){
      Serial.println("stop");
      digitalWrite(PIN_B2_OUT_Y1, LOW);      // wyłącz
      digitalWrite(PIN_B2_OUT_Y2, LOW);
      dest = 0; 
      frequency = POS_FREQ;
}

void loop() {
  now = millis(); 
  sensorReading = analogRead(sensor);
  pwmread = analogRead(pwmsensor);
  if( abs(pwmread - lastpwm) > 6 ){
    lastpwm = pwmread;
     pwm = pwmread>>2;
     analogWrite(OUT_Y_ENABLE, pwm);
     Serial.print("pwm: ");
     Serial.println(pwmread);
  }
  counter--;
  if( counter <= 1 ){
   counter = 150;
 //  Serial.println(sensorReading);    // pokaż wartość sensora
  }
  if(dest == 1){        // up
    if( sensorReading >= pos_max ){
      Serial.print("value max: "); 
      Serial.println(sensorReading); 

      stopHere();
     //delay(4000);                 // tyle czekaj na dole
      delay(1000);                 // tyle czekaj na górze
      goDown();
    }
  }else if(dest == 2){    // down
    if( sensorReading <= pos_min ){
      Serial.print("value min: "); 
      Serial.println(sensorReading); 
      stopHere();
   //   delay(4000);                 // tyle czekaj na górze
     delay(1000);                 // tyle czekaj na górze
      goUp();
    }
  }
  
  frequency--;
  if( dest != 0 && frequency == 0 ){
    frequency = POS_FREQ;
    Serial.println(sensorReading);    // pokaż wartość sensora
    if( last_pos == sensorReading ){
      Serial.println("cant move 1");
    //  stopHere();
    //  delay(1700);
      delay(100);
      if(dest == 1){
        goDown();
      }else{
        goUp();
      }
    }
    last_pos = sensorReading;
  }
 
  if(lowpower > 0){
    if( (sensorReading > pos_max - pos_margin) || (sensorReading < pos_min + pos_margin) ){
       if(pwm != lowpower){
         pwm = lowpower;
         analogWrite(OUT_Y_ENABLE, pwm);
         Serial.print("pwm: ");
         Serial.println(pwm);
       }
    }else if( pwm != 255 ){
       pwm = 255;
       analogWrite(OUT_Y_ENABLE, pwm);
       Serial.print("pwm: ");
       Serial.println(pwm);
    }
  }
  
  
}


