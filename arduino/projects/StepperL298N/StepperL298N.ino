
int ledPin = 13;

void setup()                    // run once, when the sketch starts
{
  pinMode(ledPin, OUTPUT);      // sets the digital pin as output
}

void mydelay(int clk)
{
  int i;
  for (i=0;i< clk;i++);
  //delay(clk);
}

class StepperL298N
{
  public:
  byte pins[4];
  byte curState;
  byte* lut;
  StepperL298N(byte pin1,byte pin2,byte pin3,byte pin4);
     
  void step(boolean dir);
  void halfStep(boolean yes) { if (yes) lut = halfStepLut; else lut = fullStepLut;}
  void start() { curState = 0xa;}
  void free() { curState = 0; }
  void brake() { curState = 0; }

  byte fullStepLut[16];
  byte halfStepLut[16];
};


StepperL298N::StepperL298N(byte pin1,byte pin2,byte pin3,byte pin4)
{
  pins[0] = pin1; pins[1] = pin2; pins[2]=pin3; pins[3] = pin4;
  lut = fullStepLut;
  for (int i=0;i<4;i++)
    {
    pinMode(pins[i], OUTPUT);      // sets the digital pin as output
    }

 memset(&fullStepLut,sizeof(byte)*16,0);
 memset(&halfStepLut,sizeof(byte)*16,0);

 // High nibble goes one way, low the other
 fullStepLut[0] = 0;
 // 1010 -> 1001 -> 0101 -> 0110
 fullStepLut[0xa] = 0x9 | 0x60;
 fullStepLut[0x9] = 0x5 | 0xa0;
 fullStepLut[0x5] = 0x6 | 0x90;
 fullStepLut[0x6] = 0xa | 0x50;

 halfStepLut[0] = 0;
 // 1010 -> 1000 -> 1001 -> 0001 -> 0101 -> 0100 -> 0110 -> 0010
 halfStepLut[0xa] = 0x8 | 0x20;
 halfStepLut[0x8] = 0x9 | 0xa0;
 halfStepLut[0x9] = 0x1 | 0x80;
 halfStepLut[0x1] = 0x5 | 0x90;
 halfStepLut[0x5] = 0x4 | 0x10;
 halfStepLut[0x4] = 0x6 | 0x50;
 halfStepLut[0x6] = 0x2 | 0x40;
 halfStepLut[0x2] = 0xa | 0x60;
}

void StepperL298N::step(boolean dir)
{
  curState = lut[curState];
  if (dir) curState &= 0xf;
  else curState >>= 4;
  for (int i=1,j=0;i< 16;i*= 2,j++)
    {
    byte lvl = LOW;
    if (i&curState) lvl=HIGH;
    digitalWrite(pins[j],lvl);
    }
}


int serDataVal = LOW;
void loop()                     // run over and over again
{
  unsigned long int j; 
  StepperL298N motor(40,41,42,43);
  motor.halfStep(true);
  motor.start();
  for (j=0;j< 1000;j++){
    motor.step(1);
    delay(10);  // Change the delay to change the rate the motor spins
  }
  motor.free();
 digitalWrite(ledPin, 0);
  delay(4000);

/*
  motor.halfStep(false);
  motor.start();
  for (j=0;j< 1000;j++)
    {
    motor.step(0);
    delay(5+j/10);
    }*/
}
