void setup()
{
    Serial.begin(115200,SERIAL_8N1); //pc
    Serial3.begin(9600,SERIAL_8N1); //BT
//    Serial2.begin(9600,SERIAL_8N1); //BT

    Serial.println("Hello!");
    delay(1000);
    Serial3.print("AT");
    delay(1000);
    Serial3.print("AT+STATE?");    
    delay(1000);
    Serial3.print( "AT+PIN1234" );
    delay(1000);
    Serial3.print( "AT+NAMEPAD2.0" );
    Serial.println("init end");
}

//  String input;
char ser_char;
char ser2_char;
char ser3_char;
char nl = '\n';
char rl = '\r';

String stringOne= String("");
String stringTwo= String("");
String stringThree= String("");

unsigned long milis40 = 0;
unsigned long mil = 0;
unsigned long int czestotliwosc = 0;

void loop(){
  if (Serial.available() >0){
      char ser_char = Serial.read();
      if(ser_char == nl || ser_char == rl ){
//        Serial.print(stringOne);
        Serial3.print(stringOne);
        Serial.println(stringOne);
//        Serial2.println(stringOne);
        stringOne = String("");
      }else{
        stringOne = stringOne + ser_char;
      }
    }
    if (Serial3.available() >0){
      char ser3_char = Serial3.read();
      Serial.print(ser3_char);
   }  
}

/*
    if (Serial2.available() >0){
      char ser2_char = Serial2.read();
       if(ser2_char == nl || ser2_char == rl ){
        Serial.println(stringTwo);
        stringTwo = String("");
      }else{
        stringTwo = stringTwo + ser2_char;
      }
    }
*/

/*
    if (Serial3.available() >0){
      char ser3_char = Serial3.read();
       if(ser3_char == nl || ser3_char == rl ){
        Serial.println(stringThree);
        stringThree = String("");
      }else{
        stringThree = stringThree + ser3_char;
      }
    }*/

