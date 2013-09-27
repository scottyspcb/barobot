#include <AccelStepper.h>
#include "config.cpp";
#include <NewPing.h>
#include <Servo.h>


/*
LIVE A 2,10,1
SET Y +100000
SET Y +20000

*/
// to jest master
#define I2C_ADDR 0x01
#define I2C_MASTER_ADDR 0x01
#define I2C_VERSION 0x01
#define I2C_DEVICE_TYPE 0x12

// stan uC
byte status = STATE_INIT;      // na pocztku jest w trakcje inicjacji
// koniec stanu uC
unsigned int waga_zero = 0;        // przy tej wadze uznaje ze nic nie stoi na wadze
byte version = 1;

volatile bool was_interrupt = LOW;    // przerwania połączona spójnikiem OR

//volatile bool int4_value = LOW;
//volatile bool int5_value = LOW;

volatile unsigned long encoder_diff_x = 0;    // przejechane przez enkoder
volatile unsigned long encoder_x = 0;         // pozycja enkodera (nieminusowa, volatile bo uzywana w pezrwaniach)
volatile unsigned long encoder_diff_y = 0;    // przejechane przez enkoder
volatile unsigned long encoder_y = 0;         // pozycja enkodera (nieminusowa, volatile bo uzywana w pezrwaniach)

// obsluga zrodel wejscia
boolean adb_ready = false;
boolean bt_ready = true;
String serial0Buffer = "";
String serial3Buffer = "";
boolean Console0Complete = false;   // This will be set to true once we have a full string
// koniec obsluga zrodel wejscia

boolean analog_reading = false;
byte analog_num = 0;
unsigned int analog_speed = 0;
byte analog_repeat = 0;	
byte analog_pos = 0;
unsigned long analog_sum = 0;


// http://code.google.com/p/arduino-new-ping/wiki/Using_NewPing_Syntax
NewPing ultrasonic0(PIN_ULTRA0_TRIG, PIN_ULTRA0_ECHO, MAX_DISTANCE);
NewPing ultrasonic1(PIN_ULTRA1_TRIG, PIN_ULTRA1_ECHO, MAX_DISTANCE);

void setupSerial(){      // uzyte w setup()
	if (DEBUG_OVER_SERIAL){
		Serial.begin(SERIAL0_BOUND);
		send2debuger("INIT", "START init");
	}
}


#if SERVOX4PIN==true
AccelStepper stepperX(8, STEPPER_X_STEP0, STEPPER_X_STEP1, STEPPER_X_STEP2, STEPPER_X_STEP3 );
#else
AccelStepper stepperX(1, STEPPER_X_STEP, STEPPER_X_DIR);      // Step, DIR
#endif

#if SERVOY4PIN==true
AccelStepper stepperY(8, STEPPER_Y_STEP0, STEPPER_Y_STEP1, STEPPER_Y_STEP2, STEPPER_Y_STEP3 );
#else
AccelStepper stepperY(1, STEPPER_Y_STEP, STEPPER_Y_DIR);// Step, DIR
#endif

void setupOutputs(){      // uzyte w setup()
	pinMode( STATUS_LED01, OUTPUT);      // LEDY podświetlenia
	pinMode( STATUS_LED02, OUTPUT);
	pinMode( STATUS_LED03, OUTPUT);
	pinMode( STATUS_LED04, OUTPUT);
	pinMode( STATUS_LED05, OUTPUT);
	pinMode( STATUS_LED06, OUTPUT);
	pinMode( STATUS_LED07, OUTPUT);
	pinMode( STATUS_LED08, OUTPUT);
	pinMode( STATUS_LED09, OUTPUT);

	pinMode( IRRX_PIN, INPUT);
	pinMode( IRRY_PIN, INPUT);
//  pinMode( IRRZ_PIN, INPUT);

//  pinMode( STEPPER_Z_ENABLE, OUTPUT);
	pinMode( STEPPER_Z_PWM, OUTPUT);
	digitalWrite( STATUS_LED01, LOW );    // zgas ledy na początku
	digitalWrite( STATUS_LED02, LOW );
	digitalWrite( STATUS_LED03, LOW );
	digitalWrite( STATUS_LED04, LOW );
	digitalWrite( STATUS_LED05, LOW );
	digitalWrite( STATUS_LED06, LOW );
	digitalWrite( STATUS_LED07, LOW );
	digitalWrite( STATUS_LED08, LOW );
	digitalWrite( STATUS_LED09, LOW );
}

void setupSteppers(){				  // uzyte w setup()
	stepperX.setPinsInverted( false, false, true ); // enable pin invert
	stepperY.setPinsInverted( false, false, true ); // enable pin invert
	stepperX.disable_on_ready = STEPPERX_READY_DISABLE;
	stepperY.disable_on_ready = STEPPERY_READY_DISABLE;
	#if SERVOX4PIN==true
	#else
		stepperX.setEnablePin(STEPPER_X_ENABLE);
	#endif

	#if SERVOY4PIN==true
	#else
		stepperY.setEnablePin(STEPPER_Y_ENABLE);
	#endif

	if(STEPPERX_READY_DISABLE){
		stepperX.disableOutputs();
	}
	if(STEPPERY_READY_DISABLE){
		stepperY.disableOutputs();
	}

	stepperX.setAcceleration(ACCELERX);    // lewo prawo
	stepperX.setMaxSpeed(SPEEDX); 

	stepperY.setAcceleration(ACCELERY);    // wgląb
	stepperY.setMaxSpeed(SPEEDY);
	// zjedz na dół stepperem Z  	
}

void setupUltrasonic(){      // uzyte w setup()
}

// Ustawienia Przerwań
void setupInts(){      // uzyte w setup()
}



void afterSetup(){ 
	send2debuger("INIT", "START ready");
//	NewPing::timer_ms(1, run_steppers);   // Create a Timer2 interrupt that calls func
	//NewPing::timer_ms(40, in_40ms);   // Create a Timer2 interrupt that calls func
	//NewPing::timer_ms(100, in_100ms);   // Create a Timer2 interrupt that calls func
	//NewPing::timer_ms(1000, in_1000ms);   // Create a Timer2 interrupt that calls func
}

void setup(){ 
	// noInterrupts();
	status= STATE_BUSY;      // w inicie jest w stanie zajętym
	setupOutputs();
	setupSerial();
	setupInts();
	setupUltrasonic();
	setupSteppers();
	afterSetup();
	status= STATE_READY;      // po wykonaniu operacji jest w stanie gotowym
	send2debuger("INIT", "Koniec init");
	send2android( "R REBOOT" );
	//interrupts();
}

Servo servoZ;
long int margin_x = 0;          // odstępstwo od technicznej pozycji X
long int margin_y = 0;          // odstępstwo od technicznej pozycji Y
long int dlugosc_x = 0;
long int dlugosc_y = 0;
int dlugosc_z = 100;

boolean send_ret = true;        // czy wysyłać odpowiedzi po wykonaniu komend
boolean last_stepper_operation = false; 
boolean irr_handled_x = true;
boolean irr_min_x = false;          //czy jestem na dole?
boolean irr_max_x = false;
boolean irr_handled_y = true;
boolean irr_min_y = false;          //czy jestem na dole?
boolean irr_max_y = false;
boolean irr_min_z = false;          //czy jestem na dole?
boolean irr_max_z = false;
boolean irr_x  = HIGH;			// czy dotykam przerwania
boolean irr_y  = HIGH;			// czy dotykam przerwania

unsigned int acc_x = ACCELERX;
unsigned int acc_y = ACCELERY;
unsigned int max_speed_x = SPEEDX;
unsigned int max_speed_y = SPEEDY;

unsigned int last_max_x = XLENGTH;
unsigned int last_max_y = YLENGTH;
unsigned long milis40 = 0, milis100 = 0, milis1000 = 0,milisAnalog = 0;
unsigned long mil = 0;
unsigned int servo_last = 123;

void loop(){
	mil = millis();
	run_steppers();


/*
LIVE A 2,10,1
SET Y +200000
SET Y +20000
*/	

	if( analog_reading &&  mil > milisAnalog ){
		milisAnalog = mil+ analog_speed;
		if( analog_pos == analog_repeat ){			// wyślij
			//String aa = "A" + String(analog_num) + " " + String(analog_sum);
			//String vv = String("DEBUG ")+ "2ANDROID NOSEND" + ": [" + aa + "]";
			//vv.trim();
			//Serial.println(vv);

			//send2android(aa);	// bez spacji przed numerem pina
			//Serial.println(vv);
			//String vv = "DEBUG 2ANDROID NOSEND: [A" + String(analog_num) + " " + String(analog_sum) + "]";                        
			//Serial.print("DEBUG 2ANDROID NOSEND: [A");

			//char ss[] = "A2 2000";
		//	Serial.println(ss);

			Serial.print("A");
			Serial.print(analog_num);
			Serial.print(' ');	
			Serial.println(analog_sum);
			/*  
			Serial.print("A");
			Serial.print(2);
			Serial.print(" ");
			Serial.println(200);*/
			//          Serial.println("]");
			analog_pos = 0;
			analog_sum = 0;
		}
		analog_pos++;
		if( analog_num == 20 ){
			analog_sum	+= ultrasonic0.ping_median( DIST_REPEAT ); // Do multiple pings (default=5), discard out of range pings and return median in microseconds. 
		}else if( analog_num == 21 ){
			analog_sum	+= ultrasonic1.ping_median( DIST_REPEAT ); // Do multiple pings (default=5), discard out of range pings and return median in microseconds. 	
		}else{
			analog_sum	+= analogRead(analog_num);
		}
	}
	#if DEBUG_OVER_SERIAL || DEBUG_SERIAL_INPUT
		if (Console0Complete) {
			parseInput( serial0Buffer );				      // parsuj wejscie
			if(DEBUG_SERIAL_INPUT){
				send2debuger( "READ0", serial0Buffer );
			}
		//	send2android(serial0Buffer);
			Console0Complete = false;
			serial0Buffer = "";
		}
	#endif
}
long decodePosition( int axis, String input, int odetnij ){
	long pos = 0;
	if(odetnij>0){
		input = input.substring(odetnij);    // obetnij SET X lub SET Y, lub SEY Z, zostaje np "343" lub "+432"
	}
	if( input.startsWith("+") || input.startsWith("-")){				     // relatywnie + lub - czyli oddal o pozana pozycje od aktualnej
		long diff = input.substring(1).toInt();          // +
		if( input.startsWith("-")){
			diff = - diff;							   // -
		}
		if( axis == AXIS_X){
			pos = target_posx() + diff;
//      send2debuger( "decodePosition X", "target(" + String(target_posx()) + ") T(" + String(stepperX.currentPosition()) + ") diff:" + String(diff)+ ") L(" + String(pos)+")");
		}else if( axis == AXIS_Y){
			pos = target_posy() + diff;
		}else if( axis == AXIS_Z){
			// generalnie niemozliwe
		}
	}else{				    // dokladna pozycja 
		pos = input.toInt();    // pozycja sprzętowa jest oddalona o margin_x od pozycji programowej  
	}
	return pos;
}

long decodeInt(String input, int odetnij ){
	long pos = 0;
	if(odetnij>0){
		input = input.substring(odetnij);    // obetnij SET SPEEDX lub SET SPEEDY, lub SEY SPEEDZ, zostaje np "343" lub "+432"
	}
	pos = input.toInt();
	return pos;
}

void parseInput( String input ){   // zrozum co sie dzieje
	input.trim();
	bool defaultResult = true;

	if ( input.startsWith("SET") ) {      // tutaj niektore beda synchroniczne inne asynchroniczne wiec czasem zwracaj RET, a czasem dopiero po zakonczeniu
		if( input.startsWith("SET LED") ){    // zapal LEDa o numerze 1-9
			unsigned int pin = input.substring(7).toInt();      // "SET LED4 ON" na "4 ON"
			if( pin == 1 ){			pin = STATUS_LED01;
			}else if( pin == 2 ){	pin = STATUS_LED02;
			}else if( pin == 3 ){	pin = STATUS_LED03;
			}else if( pin == 4 ){	pin = STATUS_LED04;
			}else if( pin == 5 ){	pin = STATUS_LED05;
			}else if( pin == 6 ){	pin = STATUS_LED06;
			}else if( pin == 7 ){	pin = STATUS_LED07;
			}else if( pin == 8 ){	pin = STATUS_LED08;
			}else if( pin == 9 ){	pin = STATUS_LED09;
			}else{					pin = 0;
			}
			if(pin != 0 ){
				if( input.endsWith("ON") ){
					digitalWrite(pin, HIGH );      
				} else{
					digitalWrite(pin, LOW );
				}
			}
		}else if( input.startsWith("SET X") ){
			long pos = decodePosition( AXIS_X, input, 6 );
			last_stepper_operation = true;
			posx(pos);
			defaultResult = false;
		} else if( input.startsWith("SET Y") ){
			long pos = decodePosition( AXIS_Y, input, 6 );
			last_stepper_operation = true;
			posy(pos);
			defaultResult = false;
		}else if( input.startsWith("SET SPEEDX") ){
			max_speed_x = decodeInt( input, 11 );    // 10 znakow i spacja
			stepperX.setMaxSpeed(max_speed_x);    // lewo prawo
		}else if( input.startsWith("SET SPEEDY") ){
			max_speed_y = decodeInt( input, 11 );
			stepperY.setMaxSpeed(max_speed_y);      // wgląb
		} else if( input.startsWith("SET ACCX") ){
			acc_x = decodeInt( input, 9 );    // SET ACCX i spacja
			stepperX.setAcceleration(acc_x);
		}else if( input.startsWith("SET ACCY") ){      
			acc_y = decodeInt( input, 9 );    // SET ACCY i spacja
			stepperY.setAcceleration(acc_y);
		}else if( input.equals("SET Z MAX") ){
			 if(!irr_max_z){
				int up_pos = SERVOZ_UP_POS;
				servo_last = up_pos;
				servoZ.writeMicroseconds(up_pos);			 // na doł
				irr_max_z = true;
				irr_min_z = false;
			 }
	//	   send_current_position(true);
		 }else if( input.equals("SET Z MIN") ){
			 if(!irr_min_z){
				int down_pos = SERVOZ_DOWN_POS;
				servo_last = down_pos;
				servoZ.writeMicroseconds(down_pos);         // na doł
				irr_max_z = false;
				irr_min_z = true;
			 }
			 send2android("LENGTHZ "+ String(dlugosc_z) );
	//		send_current_position(true);
		 }else if( input.startsWith("SET Z ") ){
			int msec = decodeInt( input, 6 );    // 10 znakow i spacja
			send2debuger( "msec", "w gore1" );
			servo_last = msec;
			servoZ.writeMicroseconds(msec);
		}else{
			send2android("ARDUINO NO COMMAND [" + input +"]");
			defaultResult = false;
		}
	}else if( input.equals("LIVE A OFF") ){
		analog_reading	= false;
	}else if( input.startsWith("LIVE A ") ){      // LIVE A 3,100,5
		String digits     = input.substring( 7 );
		char charBuf[50];
		digits.toCharArray(charBuf, 50);
		sscanf(charBuf,"%i,%i,%i", &analog_num, &analog_speed, &analog_repeat );
		if(analog_num >= 22){
			analog_num = 0;
		}
		analog_reading	= true;
		analog_pos = 0;
		analog_sum = 0;
		if( analog_num <= 15 ){
			pinMode( A0 + analog_num, INPUT);      // numer portu analoga to nie numer pinu (w mega A0 to 54)
		}
//		LIVE A 2,200,2
//		LIVE A 2,20,4
//		LIVE A 4,100,1
//		LIVE A 20,100,1
//		LIVE A 21,100,1
//		LIVE A OFF
	}else if( input.startsWith("GET")) {
		if( input == "GET SPEED" ){      // podaje prędkosci i akceleracje silnikow X i Y
			send2android("VAL SPEEDX " + String(max_speed_y) + ","+ String(acc_x) );
			send2android("VAL SPEEDY " + String(max_speed_x) + ","+ String(acc_y) );
		}else if( input.equals( "GET VERSION" )){
			send2android( "VAL VERSION " + String(version));
		}else if( input.equals( "GET CARRET" )){      // pozycja karetki x,y
			send_current_position( false );
		}else if( input.startsWith("GET A ") ){      // GET A 3 raz. Spacja przed numerem pinu
			int numer = decodeInt( input, 11 );    // 10 znakow i spacja
			int val = 0;
			if( numer <= 15 ){
				pinMode( A0 + numer, INPUT);      // numer portu analoga to nie numer pinu (w mega A0 to 54)
			}
			if( analog_num == 20 ){
				val	= ultrasonic0.ping_median( DIST_REPEAT );
			}else if( analog_num == 21 ){
				val	= ultrasonic1.ping_median( DIST_REPEAT );
			}else{
				val	= analogRead(numer);
			}
			send2android("A" + String(numer) + " " + String(val));				// A2 124		// bez spacji przed numerem pinu
	//		GET A 2
	//		GET A 2
	//		GET A 4
	//		GET A 20
	//		GET A 21
		}else{
			send2android("ARDUINO NO COMMAND [" + input +"]");
		}
		defaultResult = false;

	}else if( input.equals("ENABLEX") ){
		stepperX.enableOutputs();
	}else if( input.equals("DISABLEX") ){
		if(STEPPERX_READY_DISABLE){
			stepperX.disableOutputs();
		}
	}else if( input.startsWith("I2C ") ){			// wyślij komendę do urządzenia i2c o podanym numerze
		byte address	= input.charAt( 4 );		// adres i2c
		String ss 		= input.substring( 5 );		// "I2C " 4 znaki, 5 to adres więc podaj od szóstego

		send2debuger( "I2C ADR", ""+String(address) );
		send2debuger( "I2C LEN", ""+String(ss.length()) );
		send2debuger( "I2C PARAMS", ss );

		char const *c	= ss.c_str();				// lista parametrow, pierwszy to komenda
		for(byte a; a<ss.length(); a++){
			send2debuger( "I2C PARAM", ""+String(c[a]) );
		}	
		defaultResult = false; 

	}else if( input.equals("ENABLEY") ){
		stepperY.enableOutputs();
	}else if( input.equals("DISABLEY") ){
		if(STEPPERY_READY_DISABLE){
			stepperY.disableOutputs();
		}
	}else if( input.equals("ENABLEZ") ){
		servoZ.attach(STEPPER_Z_PWM);				  // przypisz do pinu, uruchamia PWMa 
	}else if( input.equals("DISABLEZ") ){
		servoZ.detach();							 // odetnij sterowanie
	}else if( input.equals("PING2ARDUINO") ){        // odeslij PONG
		send2android("PONG");
		defaultResult = false;
	}else if( input.startsWith("ANDROID ") ){    // zwrotka, nic nie rób
		defaultResult = false;
	}else if( input.equals( "PONG" )){			// nic, to byla odpowiedz na moje PING
		defaultResult = false;
	}else if( input.equals( "PING2ANDROID") ){      // nic nie rob
		defaultResult = false;
	}else if( input.equals( "WAIT READY") ){      // tylko zwróc zwrotkę

	}else{
		send2android("ARDUINO NO COMMAND [" + input +"]");
		defaultResult = false;  
	}
	if(defaultResult && send_ret ){
		send2android("R " + input );
	}
}

// newPos = pozucja logiczna (zamieniana jest na tecniczną wewnątrz)
void posx( long newPos ){
//  send2debuger("osX","jade do:L(" + String(newPos)+") czyli T(" + String(newPos * STEPPER_X_MUL + margin_x)+ "). margines: " + String(margin_x));
	encoder_diff_x = 0;
	stepperX.moveTo( newPos * STEPPER_X_MUL + margin_x );
}
void posy( long newPos ){
//  send2debuger("osY","jade do:L(" + String(newPos)+") czyli: T(" + String(newPos * STEPPER_Y_MUL + margin_x)+ "). margines: " + String(margin_y));
	encoder_diff_y = 0;
	stepperY.moveTo( newPos * STEPPER_Y_MUL + margin_y );
}

void posz( long newPos ){    // zajedz do pozycji
	send2debuger("osZ", "jade do:L(" + String(newPos) +")");
	servoZ.attach(STEPPER_Z_PWM);				  // przypisz do pinu, uruchamia PWMa 

//  int up_pos = SERVOZ_UP_POS;
	int roznica = abs(servo_last - newPos);
	// zamien pozycje na czas?


	servoZ.writeMicroseconds(newPos);			 // na doł
	servo_last = newPos;
}
long int posx(){
	return (stepperX.currentPosition() - margin_x) /STEPPER_X_MUL;
}
long int posy(){
	 return (stepperY.currentPosition() - margin_y) /STEPPER_Y_MUL;
}
long int target_posx(){
	 return (stepperX.targetPosition() - margin_x) /STEPPER_X_MUL;
}
long int target_posy(){
	 return (stepperY.targetPosition() - margin_y) /STEPPER_Y_MUL;
}
long int target_posz(){
	 return servo_last;
}

long int posz(){
	if(irr_min_z){
		return 0;
	}else{
		return dlugosc_z;
	}
}

void send_current_position( boolean isReady ){ 
	if(isReady){
		send2android("R READY AT " + String(posx()) + "," + String(posy())+ "," + String(posz()));
	}else{
		send2android("R POS " + String(posx()) + "," + String(posy())+ "," + String(posz()));
	}
//  send2android("ENCODERS [" + String(encoder_diff_x) + "/" + String(encoder_x)+ "] [" + String(encoder_diff_y)+ "/" + String(encoder_y));
}


// pin 3    // Krańcowy Z na puszczenie
// attachInterrupt( INT1, on_int1R, FALLING);   // nasłuchuj zmiany PIN 3    // Krańcowy Z na puszczenie

void run_steppers(){    // robione w każdym przebiegu loop
	long int dist_x = stepperX.distanceToGo();
	long int dist_y = stepperY.distanceToGo();

	if( dist_x == 0 && dist_y == 0 ){    // zajechalem 
		if( last_stepper_operation ){    // byla komenda
			stepperX.stopNow();
			stepperY.stopNow();
			if(send_ret){
			  send_current_position(true);    // wyslij ze skonczylem
			}
			send2debuger("steppers","koniec X:" + String(stepperX.distanceToGo()) + " Y:" + String(stepperY.distanceToGo()) + String(" margines_x ") + String(margin_x) );
			last_stepper_operation = false;
		}
	}else{
		irr_x  = 1;//digitalRead( IRRX_PIN );
		if( irr_x == LOW && dist_x != 0 ){		// wcisniete a mam gdzies jechac
			if( irr_min_x && dist_x < 0 ){    	// stoje na dole a mam jechac w dół
			  stepperX.stopNow();
			  send2debuger("osX MIN","jestem na dole i nie jade w dol roznica:" + String(dist_x)+" / R(" + String(stepperX.distanceToGo())+ ") / " + String(margin_x));
	//		  delay(100);
			  dist_x = 0;
			}else if( irr_max_x && dist_x > 0 ){    // stoje na górze a mam jechac w góre
			  stepperX.stopNow();
			  send2debuger("osX MAX","jestem na górze i nie jade w gore roznica:" + String(dist_x)+" / R(" + String(stepperX.distanceToGo())+ ") / " + String(margin_x));
			  dist_x = 0;
			}
		}
		if( irr_x == LOW && !irr_handled_x ) {      // wciśnięte = bylo przerwanie X i jeszcze nie jest obsluzone
			if( dist_x < 0 ){				       // jechalem w dół
			  margin_x = stepperX.currentPosition();				      // to jest pozycja skrajna
			  stepperX.stopNow();
			  irr_min_x = true;      // to jest minimum
			  dlugosc_x = last_max_x;
			  send2debuger("osX MIN", "dlugosc:" + String(dlugosc_x)+ " margines: " + String(margin_x)+ " jade: " + String(stepperX.distanceToGo()));
			  send2android("LENGTHX " +  String(dlugosc_x) );
			}else if( dist_x > 0 ){			 // jechalem w gore
			  stepperX.stopNow();
			  last_max_x = posx();   // to jest pozycja skrajna
			  irr_max_x = true;
			  dlugosc_x = last_max_x;
			  send2debuger("osX MAX", "dlugosc:" + String(dlugosc_x)+ " margines: " + String(margin_x));
			  send2android("LENGTHX " +  String(dlugosc_x) );
			}else{
			  send2debuger("osX", "dist_x :" + String(dist_x)+ " margines: " + String(margin_x) +  " distanceToGo: " + String(stepperX.distanceToGo()));
			}
			irr_handled_x = true;
		}else if(dist_x != 0){
			stepperX.run();
		}
		
		if( dist_x != 0 ){			// mam jechac
			// skrajny puszczony lub wcisniety ale jade w inną strone
			if(irr_x == HIGH || ( irr_min_x && dist_x < 0 ) || ( irr_max_x && dist_x > 0 ) ){	
				stepperX.run();
			}
		}
		

		if(irr_x == HIGH && irr_handled_x ){    // wlasnie puszczam i było obsłużone
			send2debuger("osX","puszczam IRR X i jade... dist_x: " + String( dist_x ));
			irr_handled_x	= false;
			irr_min_x 		 = false;
			irr_max_x 		 = false;
		}

// -------------oś Y--------------------
		irr_y  =  1;//digitalRead( IRRY_PIN );
		// IRR_Y jest w stanie HIGH lub LOW,
		if( irr_y == LOW && dist_y != 0 ){	  // wcisniete a mam gdzies jechac
			if( irr_min_y && dist_y < 0 ){    // stoje na dole a mam jechac w dół
			  stepperY.stopNow();
			  send2debuger("osY MIN","jestem na dole i nie jade w dol roznica:" + String(dist_y)+" / R(" + String(stepperY.distanceToGo())+ ") / " + String(margin_y));
	//		  delay(100);
			  dist_y = 0;
			}else if( irr_max_y && dist_y > 0 ){    // stoje na górze a mam jechac w góre
			  stepperY.stopNow();
			  send2debuger("osY MAX","jestem na górze i nie jade w gore roznica:" + String(dist_y)+" / R(" + String(stepperY.distanceToGo())+ ") / " + String(margin_y));
			  dist_y = 0;
			}
		}

		if( irr_y == LOW && !irr_handled_y) {      // wciśnięte = bylo przerwanie Y i jeszcze nie jest obsluzone
			if( dist_y < 0 ){				       // jechalem w dół
			  margin_y = stepperY.currentPosition();				      // to jest pozycja skrajna
			  stepperY.stopNow();			// stuj to gdziejestes
			  irr_min_y = true;			 // to jest minimum
			  dlugosc_y = last_max_y;       // margines rowny różnicy
			  send2debuger("osY MIN", "dlugosc:" + String(dlugosc_y)+ " margines: " + String(margin_y)+ " jade: " + String(stepperY.distanceToGo()));
			  send2android("LENGTHY " +  String(dlugosc_y) );
			}else if( dist_y > 0 ){			 // jechalem w gore
			  stepperY.stopNow();
			  last_max_y = posy();   // to jest pozycja skrajna
			  irr_max_y = true;
			  dlugosc_y = last_max_y;
			  send2debuger("osY MAX", "dlugosc:" + String(dlugosc_y)+ " margines: " + String(margin_y));
			  send2android("LENGTHY " +  String(dlugosc_y) );
			}else{
			  send2debuger("osY", "dist_y :" + String(dist_y)+ " margines: " + String(margin_y) +  " distanceToGo: " + String(stepperY.distanceToGo()));
			}
			irr_handled_y = true;
		}else if(dist_y != 0){
			stepperY.run();
		}
		if(irr_y == HIGH && irr_handled_y ){    // wlasnie puszczam i było obsłużone
			send2debuger("osY","puszczam IRR Y i jade.. dist_y: " + String( dist_y ));
			irr_handled_y  = false;
			irr_min_y  = false;
			irr_max_y  = false;
		}
	}
}



void send2debuger( String ns, String logstr ){
	if(DEBUG_OVER_SERIAL){
		Serial.println("DEBUG "+ ns +": [" + logstr + "]");
	}
}

void serialEvent(){				       // FUNKCJA WBUDOWANA - zbieraj dane z serial0 i serial3 i skadaj w komendy
	while (Serial.available() && !Console0Complete) {    // odczytuj gdy istnieja dane i poprzednie zostaly odczytane
		char inChar = (char)Serial.read(); 
		serial0Buffer += String(inChar);
		if (inChar == SEPARATOR_CHAR) {
			Console0Complete = true;
		}
	}
}

#if USE_SERIAL0
	int send2android( String output2 ){      // wyslij string do androida
	#if DEBUG_OUTPUT2ANDROID && !DEBUG_OVER_SERIAL
		send2debuger( "2ANDROID BTSEND", output2 );
	#endif
	Serial.println( output2 );
	return 0;
}
#endif


