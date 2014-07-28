#include "AsyncDriver.h"

AsyncDriver::AsyncDriver(uint8_t pin1, uint8_t pin2, uint8_t newDisablePin ){
	init();
	_pin[0]		= pin1;
	_pin[1] 	= pin2;
	disablePin	= newDisablePin;
	pinMode(_pin[0], OUTPUT);
	pinMode(_pin[1], OUTPUT);
	enableOutputs();
	if (disablePin != 0xff){
		pinMode(disablePin, OUTPUT);
		fastWrite(disablePin, true );
	}
}
void AsyncDriver::init(){
	_currentPos = 0;
	_targetPos = 0;
	_speed = 0.0;
	_maxSpeed = 1.0;
	_acceleration = 1.0;
	stepInterval = 0;
	disablePin = 0xff;
	lastStepTime = 0;
	disable_on_ready = false;
	_n = 0;
	_c0 = 0.0;
	_cn = 0.0;
	_cmin = 1.0;
	_direction = DIR_ACW;
	is_disabled = true;
}

void AsyncDriver::disableOutputs(){
	if(!is_disabled){
		is_disabled = true;
		setOutput2( false, false );
		if (disablePin != 0xff){
			digitalWrite(disablePin, HIGH );
		}
	}
}

void AsyncDriver::enableOutputs(){
	if(is_disabled){
		is_disabled = false;
		if (disablePin != 0xff){
			fastWrite(disablePin, LOW );
		}
		setOutput2(last_output, false);
	}
}
long AsyncDriver::targetPosition(){
	return _targetPos;
}

long AsyncDriver::currentPosition(){
	//uint8_t oldSREG = SREG;
	//noInterrupts();
	long ppp =  _currentPos;
	//SREG = oldSREG;
	return ppp;
}

void (*AsyncDriver::user_onReady)(long int);
void AsyncDriver::moveTo(long absolute){
	uint8_t oldSREG = SREG;
	noInterrupts();
	if ( _targetPos == absolute ){
		if( _targetPos ==_currentPos){
			onReady();
		}
	}else{
		if( disable_on_ready && is_disabled ){
			enableOutputs();
		}
		_targetPos = absolute;
		computeNewSpeed(true);
	}
	SREG = oldSREG;
}
// Sets speed to 0
void AsyncDriver::setCurrentPosition(long position){
	//uint8_t oldSREG = SREG;
	//noInterrupts();
	_targetPos	= position;
	_currentPos = position;
	_n			= 0;
	stepInterval = 0;
	//SREG = oldSREG;
}

void AsyncDriver::debug(){
	volatile long distanceTo = distanceToGo();
	volatile long stepsToStop = (long)((_speed * _speed) / (2.0 * _acceleration)); // Equation 16
	Serial.println("-");
	Serial.println(_n);
	Serial.println(_speed);
	Serial.println(_acceleration);
	Serial.println(_cn);
	Serial.println(_c0);
	Serial.println(stepInterval);
	Serial.println(distanceTo);
	Serial.println(stepsToStop);
	Serial.println("-");
}

void AsyncDriver::fastWrite(uint8_t pin, uint8_t val){
	uint8_t bit = digitalPinToBitMask(pin);
	uint8_t port = digitalPinToPort(pin);
	if (port == NOT_A_PIN){
		return;
	}
	volatile uint8_t *out;
	out = portOutputRegister(port);
	if (val) {
		*out |= bit;
	} else {
		*out &= ~bit;
	}
}
void AsyncDriver::computeNewSpeed( boolean sync ){
	volatile long distanceTo	= _targetPos - _currentPos;
	volatile long stepsToStop	= (long)((_speed * _speed) / (2.0 * _acceleration)); // Equation 16

	if (distanceTo == 0 && stepsToStop <= 1){	// We are at the target and its time to stop
		stepInterval = 0;
		_speed = 0.0;
		_n = 0;
		onReady();
		return;
	}
	if (distanceTo > 0){
		// We are anticlockwise from the target
		// Need to go clockwise from here, maybe decelerate now
		if (_n > 0){
			// Currently accelerating, need to decel now? Or maybe going the wrong way?
			if ((stepsToStop >= distanceTo) || _direction == DIR_ACW)
			_n = -stepsToStop; // Start deceleration
		}else if (_n < 0){
			// Currently decelerating, need to accel again?
			if ((stepsToStop < distanceTo) && _direction == DIR_CW)
			_n = -_n; // Start accceleration
		}
	}else if (distanceTo < 0){
		// We are clockwise from the target
		// Need to go anticlockwise from here, maybe decelerate
		if (_n > 0){
			// Currently accelerating, need to decel now? Or maybe going the wrong way?
			if ((stepsToStop >= -distanceTo) || _direction == DIR_CW)
			_n = -stepsToStop; // Start deceleration
		}else if (_n < 0){
			// Currently decelerating, need to accel again?
			if ((stepsToStop < -distanceTo) && _direction == DIR_ACW)
			_n = -_n; // Start accceleration
		}
	}
	// Need to accelerate or decelerate
	if (_n == 0){
		// First step from stopped
		_cn = _c0;
		_direction = (distanceTo > 0) ? DIR_CW : DIR_ACW;
	}else{	// Subsequent step. Works for accel (n is +_ve) and decel (n is -ve).
		_cn = _cn - ((2.0 * _cn) / ((4.0 * _n) + 1)); // Equation 13
		_cn = max(_cn, _cmin); 
	}
	_n++;
	stepInterval = _cn;
	_speed = 1000000.0 / _cn;
	if (_direction == DIR_ACW){
		_speed = -_speed;
	}
	if(sync){
	//	debug();
	//# if 0
	//# endif
	}
}
// Implements steps according to the current step interval
// You must call this at least once per step
// returns true if a step occurred
boolean AsyncDriver::haveToRun(){
	// Dont do anything unless we actually have a step interval
	if (!stepInterval){
		return false;
	}

	volatile unsigned long time = micros();
	// Gymnastics to detect wrapping of either the nextStepTime and/or the current time
	volatile unsigned long nextStepTime = lastStepTime + stepInterval;

	if (((nextStepTime >= lastStepTime) && ((time >= nextStepTime) || (time < lastStepTime)))
	|| ((nextStepTime < lastStepTime) && ((time >= nextStepTime) && (time < lastStepTime)))){
		if (_direction == DIR_CW){			// Clockwise
			_currentPos += 1;
		}else{
			_currentPos -= 1;	// Anticlockwise
		}
		step(_currentPos & 0x7); // Bottom 3 bits (same as mod 8, but works with + and - numbers) 
		lastStepTime = time;
		return true;
	}else{
		return false;
	}
}
void AsyncDriver::setMaxSpeed(float speed){
	//uint8_t oldSREG = SREG;
//	noInterrupts();
	if (_maxSpeed != speed){
		_maxSpeed = speed;
		_cmin = 1000000.0 / speed;
		if (_n > 0){					// Recompute _n from current speed and adjust speed if accelerating or cruising
			_n = (long)((_speed * _speed) / (2.0 * _acceleration)); // Equation 16
			computeNewSpeed(true);
		}
	}
//	SREG = oldSREG;
}

// Run the motor to implement speed and acceleration in order to proceed to the target position
// You must call this at least once per step, preferably in your main loop
// If the motor is in the desired position, the cost is very small
// returns true if we are still running to position
void AsyncDriver::run(){
	if (haveToRun()){
		computeNewSpeed(false);
	}
}
void AsyncDriver::onReady(){
	if( disable_on_ready ){
		disableOutputs();
	}
	if(user_onReady){
		user_onReady( _currentPos );
	}
}
void AsyncDriver::setAcceleration(float acceleration){
	if (acceleration == 0.0){
		return;
	}
	//uint8_t oldSREG = SREG;
	//noInterrupts();
	if (_acceleration != acceleration) {
		// Recompute _n per Equation 17
		_n = _n * (_acceleration / acceleration);
		// New c0 per Equation 7
		_c0 = sqrt(2.0 / acceleration) * 1000000.0;
		_acceleration = acceleration;
	}
	//SREG = oldSREG;
}
// 1 pin step function (ie for stepper drivers)
// This is passed the current step number (0 to 7)
// Subclasses can override
void AsyncDriver::step(uint8_t step){
	// _pin[0] is step, _pin[1] is direction
	setOutput2( _direction ? true : false, true ); // step HIGH
	asm("nop");
	asm("nop");
	setOutput2( _direction ? true : false, false ); // step LOW
}
void AsyncDriver::setOutput2( boolean step, boolean dir ){
	fastWrite(_pin[0], dir);
	fastWrite(_pin[1], step);
	last_output = step;
}
void AsyncDriver::stop(){
	stop( 2.0 );
}
void AsyncDriver::stop( float multispeed ){
	if (_speed != 0.0){	
		volatile long stepsToStop = (long)((_speed * _speed) / (multispeed * _acceleration)) + 1; // Equation 16 (+integer rounding)
		if (_speed > 0){
			moveTo(_currentPos + stepsToStop);
		}else{
			moveTo(_currentPos - stepsToStop);
		}
	}
}
void AsyncDriver::stopNow(){
	_targetPos		= _currentPos;
	stepInterval	= 0;
	_speed			= 0.0;
	lastStepTime	= 0;
	_n				= 0;
	onReady();
}
float AsyncDriver::getMaxSpeed(){
	return _maxSpeed;
}
float AsyncDriver::getSpeed(){
	return _speed;
}
long AsyncDriver::distanceToGo(){
	//uint8_t oldSREG = SREG;
	//noInterrupts();
	long dist = _targetPos - _currentPos;
	//SREG = oldSREG;
	return dist;
}
// sets event handler function
void AsyncDriver::setOnReady( void (*fun)(long int) ){
	user_onReady = fun;
}
float AsyncDriver::getAcceleration(){
	return _acceleration;
}
