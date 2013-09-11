volatile uint8_t licznik = 0;

void setup(){

   // prescale timer to 1/1024th the clock rate
   TCCR0B |= (1<<CS02) | (1<<CS00);
 
   // enable timer overflow interrupt
   TIMSK0 |=1<<TOIE0;
   sei();
  
  
}
void loop(){
}

ISR( SIG_OVERFLOW1  ){

 PORTA |= (1<<PA0);
 PORTA |= (1<<PA1); 
 PORTA |= (1<<PA2);
 licznik++;
} 
