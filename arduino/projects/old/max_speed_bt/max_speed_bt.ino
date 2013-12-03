void setup(){
    Serial3.begin(9600,SERIAL_8N1); //BT
}
void loop(){
   Serial3.println( "RET VAL ANALOG 123;" );
}

