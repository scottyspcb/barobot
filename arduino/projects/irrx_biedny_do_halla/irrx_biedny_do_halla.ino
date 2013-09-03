#include <AccelStepper.h>

//AccelStepper stepper(28,29,30,31); // Defaults to AccelStepper::FULL4WIRE (4 pins) on 2, 3, 4, 5
//AccelStepper stepper(4, 28,29,30,31 );

AccelStepper stepper(4, 40,41,42,43 );
//AccelStepper stepper(4, 40,41,42,43 );
//AccelStepper stepper(1, 46,47 );

volatile unsigned long encoder_diff_x = 0;    // przejechane przez enkoder
const int analogInPin = A0;  // Analog input pin that the potentiometer is attached to
const int analogOutPin = 9; // Analog output pin that the LED is attached to
long int sensorValue = 0;        // value read from the pot

void setup(){
  Serial.begin(115200);
  stepper.setMaxSpeed(400);
  stepper.setAcceleration(5000);
  stepper.moveTo(300);
  pinMode( 13, OUTPUT); 
  pinMode( 6, OUTPUT); 
  pinMode( 7, OUTPUT);   
  digitalWrite(7, 1 );
  digitalWrite(6, 1 );
  stepper.disableOutputs();
}
unsigned long time = 1000;
unsigned long halltime = 10;

int localmin = 0;
int margin =0;
byte sign = 0;


byte const hist_length = 10;
byte const upfilter = 20;
byte const downfilter = 10;
unsigned int history[hist_length] = {0,0,0,0,0,0,0,0,0,0};

byte hist_pos = 0;

unsigned int getPos( int relative ){
  byte pos = (relative + hist_pos  + hist_length ) % hist_length;
  return history[ pos ];
}
  
boolean idewgore = false;
int state = 0;

void jest(){
  digitalWrite(7, 0 );
//  digitalWrite(6, 1 );
}

void loop(){
  if (stepper.distanceToGo() == 0){
      if( time == 0){
        stepper.enableOutputs();
        stepper.moveTo(-stepper.currentPosition());            
        time = 10000;
      }else{
        time--;
        stepper.disableOutputs();
      }
    }
    stepper.run();
    if( time > 1000 ){
      return;
    }
    if(halltime == 0 ){
      sensorValue == analogRead(analogInPin);
      halltime = 20;
      unsigned int last1 = (getPos(-1) +  getPos(-2)+  getPos(-3));
      unsigned int last2 = (getPos(-4) +  getPos(-5)+  getPos(-6));
      int diff = last1 - last2;
      if( diff > upfilter ){      // w gore
        if(idewgore){
          state = 600;
        }else{              // koniec w dół
          idewgore = true;
          state = 200;
        }
      }else if( diff < -downfilter ){      // w dól
        if(idewgore){    // koniec w gore
          state = 700;
        }else{
          idewgore = false;
          state = 400;
        }
      }else{
          if(idewgore){
            state = 0;
          }else{
            state = 500;
          }
      }
      sensorValue += analogRead(analogInPin);
      sensorValue = sensorValue>>1;
      int scaled = abs(sensorValue - 500);

      history[ hist_pos % hist_length] = scaled;
      hist_pos = ( hist_pos + 1 ) % hist_length;

      if(scaled > 0 ){
        sign = 1;
      }else if( scaled < 0 ){
        sign = 2;
      }else if( scaled == 0 ){
        sign = 0;
      }
//      analogWrite(analogOutPin, sensorValue);  
//      Serial.println( String(state) + " " + String(sensorValue) + " " + String(hist_pos) + " " + String(diff) );
      Serial.println( String(state) + " " + String(scaled) );
//      Serial.println(""+ String(sensorValue) + " " + String(scaled) );
    }else{
      halltime--;
    }
 }

