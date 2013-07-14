#include <Servo.h> 
Servo myservo;  // create servo object to control a servo 

 
unsigned int tryb = 1;
unsigned int ile = 0;
unsigned int val = 1530;          // variable to read the value from the analog pin 

volatile int int0_value = 0;
volatile int int1_value = 0;
volatile int int2_value = 0;
volatile int int3_value = 0;
volatile int int4_value = 0;
volatile int int5_value = 0;
void on_int0R(){  int0_value = 1;}    // pin 2
void on_int0F(){  int0_value = 2;}    // pin 2
void on_int1R(){  int1_value = 1;}    // pin 21       // Krańcowy Y
void on_int1F(){  int1_value = 2;}    // pin 21       // Krańcowy Y
void on_int2R(){  int2_value = 1;}    // pin 21       // Krańcowy Y
void on_int2F(){  int2_value = 2;}    // pin 21       // Krańcowy Y
void on_int3R(){  int3_value = 1;}    // pin 20       // Krańcowy X
void on_int3F(){  int3_value = 2;}    // pin 20       // Krańcowy X
void on_int4R(){  int4_value = 1;}          // pin 19       // Enkoder X
void on_int4F(){  int4_value = 2;}          // pin 19       // Enkoder X
void on_int5R(){  int5_value = 1;}          // pin 18       // Enkoder Y
void on_int5F(){  int5_value = 2;}          // pin 18       // Enkoder Y
// Koniec Ustawienia Przerwań

void setup() { 
  Serial.begin(115200);

//  attachInterrupt( INT0, on_int0R, RISING);    // nasłuchuj zmiany PIN 2
  attachInterrupt( INT0, on_int0R, FALLING);    // nasłuchuj zmiany PIN 2

//  attachInterrupt( INT1, on_int1R, RISING);    // nasłuchuj zmiany PIN 3
  attachInterrupt( INT1, on_int1F, FALLING);    // nasłuchuj zmiany PIN 3
     
  attachInterrupt( INT2, on_int2R, RISING);    // nasłuchuj zmiany PIN 21   // Krańcowy Y
  attachInterrupt( INT2, on_int2F, FALLING);    // nasłuchuj zmiany PIN 21   // Krańcowy Y
  
  attachInterrupt( INT3, on_int3R, RISING);    // nasłuchuj zmiany PIN 20   // Krańcowy X
  attachInterrupt( INT3, on_int3F, FALLING);    // nasłuchuj zmiany PIN 20  // Krańcowy X

  attachInterrupt( INT4, on_int4R, RISING);    // nasłuchuj zmiany PIN 19  // Enkoder X
  attachInterrupt( INT4, on_int4F, FALLING);    // nasłuchuj zmiany PIN 19 // Enkoder X

  attachInterrupt( INT5, on_int5R, RISING);    // nasłuchuj zmiany PIN 18 // Enkoder Y
  attachInterrupt( INT5, on_int5F, FALLING);    // nasłuchuj zmiany PIN 18// Enkoder Y
}  
//mniej - w dół
//wiecej w góre
bool jedz = false;
void loop(){ 
    if( int0_value > 0 ){
      Serial.println("koniec2");
      delay(10000000);
//            int0_value = 0;
    }
    while(int0_value == 0){    
      String val2 = "Tryb:" + String(tryb);    
      Serial.println(val2);
      myservo.attach(4);  // attaches the servo on pin  to the servo object 
      myservo.writeMicroseconds(2100);
      delay(200);
      int1_value = 0;
      int licznik = 0;
      while( int1_value != 2 && licznik < 200){                   // az na górze  
        Serial.println("gora");
        licznik++;
        delay(20);
      }
      myservo.writeMicroseconds(1570);  // trzymaj
      Serial.println("czekam");
      delay(5000);
      myservo.writeMicroseconds(800);  // dół
      int1_value = 0;
      Serial.println("dol");
      licznik = 0;
      while( int1_value != 2 && licznik < 200){                   // az na górze  
        Serial.println("dol++");
        licznik++;
        delay(20);
      }
      Serial.println("koniec");
      myservo.detach();
      delay(5000);
    }
}
