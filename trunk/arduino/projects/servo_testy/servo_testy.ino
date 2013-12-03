#include <FlexiTimer2.h>
#include <Servo.h>

#if 1
#define DEBUG(sth) (Serial.print(String(sth)))
#define DEBUGLN(sth) (Serial.println(String(sth)))
#else
#define DEBUG(sth)
#define DEBUGLN(sth)
#endif

String serial0Buffer = "";
boolean Console0Complete = false;   // This will be set to true once we have a full string
boolean dir = true;
Servo servoZ;

#define STEPPER_Z_PWM 6
#define SEPARATOR_CHAR '\n'
boolean enabled = false;

#define SERVO_MIN_POS 544
#define SERVO_MAX_POS 2400

volatile int delta_pos = 0;
volatile long unsigned last_distance = 0;
volatile long unsigned last_pos = 0;
volatile long unsigned target_pos = 0;
volatile boolean pos_changed = false;

void setup(){
  Serial.begin(115200); 
  DEBUG("HELLO");
  DEBUG("przyklad: 10,1900");
  enabled = false;
//  FlexiTimer2::set(40, 1.0/1000, flash); // call every 500 1ms "ticks"
  FlexiTimer2::set(40, 1.0/100, flash);
  // FlexiTimer2::set(500, flash); // MsTimer2 style is also supported
  FlexiTimer2::start();
//  in = analogRead(A0);
//  target_pos = map(in, 0, 1023, SERVO_MIN_POS,  SERVO_MAX_POS);
//  last_pos = target_pos;
}

byte sp = 1;
void flash(){
  if(last_pos != target_pos ){
    long int this_distance =0;
    if( last_pos > target_pos ){
      this_distance  = last_pos - target_pos;    
    }else if( last_pos < target_pos ){
      this_distance  = target_pos - last_pos;
    }
    long int delta = 0;
    int quoter = (last_distance >> 2);                // this_distance zawsze sie zmiejsza
    if( this_distance < quoter){                                // ostatnia cwiatrka = zwalniaj
      delta = (delta_pos * this_distance);
      delta = delta /quoter;
//      DEBUG("delta4 = " ); 
    }else if( this_distance > (last_distance - quoter)){        // pierwsza cwiatrka = przyspieszaj. tu zawsze this_distance > 3/4 * last_distance
      delta = (delta_pos * (last_distance - this_distance ) );      // tu zawsze (last_distance - this_distance ) < quoter
      delta = delta /quoter;
//      DEBUG("delta1 = " ); 
    }else{  // na maxa
//      DEBUG("delta2 = " ); 
      delta = delta_pos;
    }
    if(delta_pos > 0){
        if( delta < 10){
          delta = 10;
        }
    }else{
        if( delta > -10){
          delta = -10;
        }
    }
 //   DEBUGLN(String(delta)); 
    last_pos = last_pos + delta;
    if( delta_pos > 0 && last_pos > target_pos ){
      last_pos = target_pos;
    }else if( delta_pos < 0 && last_pos < target_pos ){
      last_pos = target_pos;    
    }
    pos_changed = true;
  }
}

void loop(){
  if(pos_changed){  // mam byc gdzie indziej
    DEBUG("\tpos= " + String(last_pos) );
    DEBUG("\ttarget_pos= " + String(target_pos) );
    DEBUGLN();
    if( last_pos == target_pos){
      DEBUGLN( "gotowe" ); 
    }else{
      servoZ.writeMicroseconds(last_pos);
    }
    pos_changed = false;
  }
  if (Console0Complete) {
    parseInput( serial0Buffer );                      // parsuj wejscie
    Console0Complete = false;
    serial0Buffer = "";
  }
}

//  format:  MAXSPEED,ACCELERATION,TARGET,MICROSTEPPING
// np 111,222,333,444

void parseInput( String input ){   // zrozum co sie dzieje
  input.trim();
  int comma = input.indexOf(',');
  byte byte_pos = 0;
  DEBUGLN("------------------NOWY CONFIG-----------------"); 
  while( comma != -1 ){
      String current  = input.substring(0, comma);
      input           = input.substring(comma + 1 );    // wytnij od tego znaku
      setValue( byte_pos, current );
      comma = input.indexOf(',');
      byte_pos++;
  }
  if( input.length() > 0 ){
      setValue( byte_pos, input );
  }  
  DEBUGLN("----------------------------------------------"); 
}

// MAXSPEED,TARGET
void setValue(byte byte_pos, String value ){
  long unsigned val = decodeInt( value, 0 );
  if( byte_pos == 0 ){               // MAXSPEED
    sp = val;
    DEBUGLN("setMaxSpeed: " + String(sp) );    
  }else if( byte_pos == 1 ){                  // TARGET
    target_pos = val;
    if( target_pos == last_pos ){  // rowne? nigdzie nie jedz
    }else if( target_pos < last_pos ){    // jedz w dol
      delta_pos = -sp;
      last_distance = last_pos - target_pos;
    }else if( target_pos > last_pos ){    // jedz w gore
      delta_pos = sp;
      last_distance = target_pos - last_pos;
    }
    if(!enabled){
      servoZ.attach(STEPPER_Z_PWM);
    }
    DEBUGLN("\tmoveTo: " + String(target_pos) );
    DEBUGLN("\tlast_pos: " + String(last_pos) );
    DEBUGLN("\tdelta= " + String(delta_pos) );
    DEBUGLN();
  }else if( byte_pos == 2 ){
  }else if( byte_pos == 3 ){
  }
}

long unsigned decodeInt(String input, int odetnij ){
  long byte_pos = 0;
  if(odetnij>0){
    input = input.substring(odetnij);    // obetnij znaki z przodu
  }
  byte_pos = input.toInt();
  return byte_pos;
}

void serialEvent(){                       // FUNKCJA WBUDOWANA - zbieraj dane z serial0 i serial3 i skadaj w komendy
  while (Serial.available() && !Console0Complete) {    // odczytuj gdy istnieja dane i poprzednie zostaly odczytane
    char inChar = (char)Serial.read(); 
    serial0Buffer += String(inChar);
    if (inChar == SEPARATOR_CHAR) {
      Console0Complete = true;
    }
  }
}

