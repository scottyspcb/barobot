/*
AT+BAUD5
AT+BAUD7
http://english.cxem.net/arduino/arduino4.php
http://cxem.net/arduino/download/HC%20Serial%20Bluetooth%20Products%20201104.pdf
http://www.mcu-turkey.com/wp-content/uploads/2013/01/HC-Serial-Bluetooth-Products-201104.pdf
http://blog.zakkemble.co.uk/getting-bluetooth-modules-talking-to-each-other/
*/

void setup(){
    Serial.begin(9600,SERIAL_8N1); //pc
//    Serial3.begin(19200,SERIAL_8N1); //BT
    Serial3.begin(57600,SERIAL_8N1); //BT
//    Serial2.begin(9600,SERIAL_8N1); //BT

    Serial.println("Hello???");
    delay(1000);
    Serial3.print("AT");
    delay(1000);
    Serial3.print("AT+STATE?");    
    delay(1000);
    Serial3.print( "AT+PIN1234" );
//    delay(1000);
//    Serial3.print( "AT+NAMEPAD1.0" );

}
//  String input;
char ser_char;
char nl = '\n';
char rl = '\r';

String stringOne= String("");

void loop(){
    if (Serial.available() >0){
      char in = Serial.read();
      if(in == nl || in == rl ){
//        Serial.println(stringOne);
        Serial3.print(stringOne);
        Serial2.print(stringOne);
        stringOne = String("");
      }else{
        stringOne = stringOne + in;
      }
    }
/*
    if (Serial2.available() >0){
      char ser_char2 = Serial2.read();
      Serial.print(ser_char2);
    //  Serial2.write(ser_char); //ECHO
    }
*/
    if (Serial3.available() >0){
      char ser_char3 = Serial3.read();
      Serial.print(ser_char3);
    //  Serial3.write(ser_char); //ECHO
    }
}

