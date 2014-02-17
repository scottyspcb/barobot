#include <Servo.h> 
Servo myservo;  // create servo object to control a servo 
 
unsigned int tryb = 1;
unsigned int ile = 0;

volatile int int0r_value = 0;
volatile int int1r_value = 0;
volatile int int2r_value = 0;
volatile int int3r_value = 0;
volatile int int4r_value = 0;
volatile int int5r_value = 0;
volatile int int0f_value = 0;
volatile int int1f_value = 0;
volatile int int2f_value = 0;
volatile int int3f_value = 0;
volatile int int4f_value = 0;
volatile int int5f_value = 0;

volatile bool int0f_state = 0;
volatile bool int1f_state = 0;
volatile bool int2f_state = 0;
volatile bool int3f_state = 0;
volatile bool int4f_state = 0;
volatile bool int5f_state = 0;


volatile bool read0 = 0;
volatile bool read1 = 0;
volatile bool read2 = 0;
volatile bool read3 = 0;
volatile bool read4 = 0;
volatile bool read5 = 0;

void on_int0R(){  int0r_value = 1;read0 = HIGH;}    // pin 2
void on_int0F(){  int0f_value = 1;read0 = LOW;}    // pin 2
void on_int1R(){  int1r_value = 1;read1 = HIGH;}    // pin 21       // Krańcowy Y
void on_int1F(){  int1f_value = 1;read1 = LOW;}    // pin 21       // Krańcowy Y
void on_int2R(){  int2r_value = 1;read2 = HIGH;}    // pin 21       // Krańcowy Y
void on_int2F(){  int2f_value = 1;read2 = LOW;}    // pin 21       // Krańcowy Y
void on_int3R(){  int3r_value = 1;read3 = HIGH;}    // pin 20       // Krańcowy X
void on_int3F(){  int3f_value = 1;read3 = LOW;}          // pin 20       // Krańcowy X
void on_int4R(){  int4r_value = 1;read4 = HIGH;}          // pin 19       // Enkoder X
void on_int4F(){  int4f_value = 1;read4 = LOW;}          // pin 19       // Enkoder X
void on_int5R(){  int5r_value = 1;read5 = HIGH;}          // pin 18       // Enkoder Y
void on_int5F(){  int5f_value = 2;read5 = LOW;}          // pin 18       // Enkoder Y


volatile int int0c = 0;
volatile int int1c = 0;
volatile int int2c = 0;
volatile int int3c = 0;
volatile int int4c = 0;
volatile int int5c = 0;

void on_int0C(){  int0c = 1;int0f_state = !int0f_state; read0 = digitalRead(INT0);}    // pin 2
void on_int1C(){  int1c = 1;int1f_state = !int1f_state; read1 = digitalRead(INT0);}    // pin 21       // Krańcowy Y
void on_int2C(){  int2c = 1;int2f_state = !int2f_state; read2 = digitalRead(INT0);}    // pin 21       // Krańcowy Y
void on_int3C(){  int3c = 1;int3f_state = !int3f_state; read3 = digitalRead(INT0);}          // pin 20       // Krańcowy X
void on_int4C(){  int4c = 1;int4f_state = !int4f_state; read4 = digitalRead(INT0);}          // pin 19       // Enkoder X
void on_int5C(){  int5c = 1;int4f_state = !int4f_state; read5 = digitalRead(INT0);}          // pin 18       // Enkoder Y

void setup() { 
  Serial.begin(115200);
  Serial.println("Start");
    
 // attachInterrupt( INT0, on_int0C, CHANGE );    // nasłuchuj zmiany PIN 2
//1  attachInterrupt( INT0, on_int0F, FALLING);    // nasłuchuj zmiany PIN 2
//  attachInterrupt( INT0, on_int0R, RISING );    // nasłuchuj zmiany PIN 2

 // attachInterrupt( INT1, on_int1R, RISING );    // nasłuchuj zmiany PIN 3
//  attachInterrupt( INT1, on_int1C, CHANGE );    // nasłuchuj zmiany PIN 3
//1  attachInterrupt( INT1, on_int1F, FALLING);    // nasłuchuj zmiany PIN 3
     
 // attachInterrupt( INT2, on_int2R, RISING );    // nasłuchuj zmiany PIN 21   // Krańcowy Y
//  attachInterrupt( INT2, on_int2C, CHANGE );    // nasłuchuj zmiany PIN 21   // Krańcowy Y
  attachInterrupt( INT2, on_int2F, FALLING);    // nasłuchuj zmiany PIN 21   // Krańcowy Y
  
//  attachInterrupt( INT3, on_int3R, RISING );    // nasłuchuj zmiany PIN 20   // Krańcowy X
//  attachInterrupt( INT3, on_int3C, CHANGE );    // nasłuchuj zmiany PIN 20   // Krańcowy X
//1  attachInterrupt( INT3, on_int3F, FALLING);    // nasłuchuj zmiany PIN 20  // Krańcowy X

 //1 attachInterrupt( INT4, on_int4R, RISING );    // nasłuchuj zmiany PIN 19  // Enkoder X
//  attachInterrupt( INT4, on_int4C, CHANGE );    // nasłuchuj zmiany PIN 19  // Enkoder X
 // attachInterrupt( INT4, on_int4F, FALLING);    // nasłuchuj zmiany PIN 19 // Enkoder X

//1  attachInterrupt( INT5, on_int5R, RISING );    // nasłuchuj zmiany PIN 18 // Enkoder Y
//  attachInterrupt( INT5, on_int5C, CHANGE );    // nasłuchuj zmiany PIN 18 // Enkoder Y
  //attachInterrupt( INT5, on_int5F, FALLING);    // nasłuchuj zmiany PIN 18// Enkoder Y
  pinMode( 8, OUTPUT);
}
unsigned long ble = 0;

void loop(){ 
//  digitalWrite( 8, ble%2 );
  digitalWrite( 8, int0f_value );
//    digitalWrite( 8, int0f_state );
  ble++;

  if( int0r_value ){
    Serial.println(String(ble) + " INT0 UP " + String(read0));
    int0r_value = 0;
  }
  if( int1r_value ){
    Serial.println(String(ble) + " INT1 UP " + String(read1));
    int1r_value = 0;
  }
  if( int2r_value ){
    Serial.println(String(ble) + " INT2 UP " + String(read2));
    int2r_value = 0;
  }
  if( int3r_value ){
    Serial.println(String(ble) + " INT3 UP " + String(read3));
    int3r_value = 0;
  }
  if( int4r_value ){
    Serial.println(String(ble) + " INT4 UP " + String(read4));
    int4r_value = 0;
  }
  if( int5r_value ){
    Serial.println(String(ble) + " INT5 UP " + String(read5));
    int5r_value = 0;
  } 
  if( int0f_value ){
    Serial.println(String(ble) + " INT0 DOWN" + String(read0));
    int0f_value = 0;
  }
  if( int1f_value ){
    Serial.println(String(ble) + " INT1 DOWN" + String(read1));
    int1f_value = 0;
  }
  if( int2f_value ){
    Serial.println(String(ble) + " INT2 DOWN" + String(read2));
    int2f_value = 0;
  }
  if( int3f_value ){
    Serial.println(String(ble) + " INT3 DOWN" + String(read3));
    int3f_value = 0;
  }
  if( int4f_value ){
    Serial.println(String(ble) + " INT4 DOWN" + String(read4));
    int4f_value = 0;
  }
  if( int5f_value ){
    Serial.println(String(ble) + " INT5 DOWN" + String(read5));
    int5f_value = 0;
  }
  
  if( int0c ){
    Serial.println(String(ble) + " INT0 CHANGE " + String(read0)+ " / "+ String(int0f_state));
    int0c = 0;
  }
  if( int1c ){
    Serial.println(String(ble) + " INT0 CHANGE " + String(read1)+ " / "+ String(int1f_state));
    int1c = 0;
  }
  if( int2c ){
    Serial.println(String(ble) + " INT0 CHANGE " + String(read2)+ " / "+ String(int2f_state));
    int2c = 0;
  }
  if( int3c ){
    Serial.println(String(ble) + " INT0 CHANGE " + String(read3)+ " / "+ String(int3f_state));
    int3c = 0;
  }
  if( int4c ){
    Serial.println(String(ble) + " INT0 CHANGE " + String(read4)+ " / "+ String(int4f_state));
    int4c = 0;
  }
  if( int5c ){
    Serial.println(String(ble) + " INT0 CHANGE " + String(read5)+ " / "+ String(int5f_state));
    int5c = 0;
  }
  
  delay(10);
}

