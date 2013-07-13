/*
 * Copyright (C) 2012 Paul Bovbel, paul@bovbel.com
 * 
 * This file is part of the Mover-Bot robot platform (http://code.google.com/p/mover-bot/)
 * 
 * Mover-Bot is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this source code. If not, see http://www.gnu.org/licenses/
 */

/*
 * Motor.cpp - Motor + encoder combo PID controller
 *
 * Setup with the proper pin definitions
 */

#include "Arduino.h"
#include "Motor.h"

#define KP 0.4
#define KI 0
#define KD 1.2

#define FWD true
#define BWD false
#define SPEED_SCALE 1000
#define PIN_STANDBY 11

Motor::Motor(int pinfwd, int pinbwd, int pinpwm, int pindir, int pinenc, bool biasdir){

	pinMode(PIN_STANDBY, OUTPUT);
	pinMode(pinfwd, OUTPUT);
	pinMode(pinbwd, OUTPUT);
	pinMode(pinpwm, OUTPUT);
	pinMode(pindir, INPUT);
	pinMode(pinenc, INPUT);

	//Initialize Motor Controller pins
	_pinfwd = pinfwd;
	_pinbwd = pinbwd;
	_pinpwm = pinpwm;

	//Initialize encoder, damnit interrupts dont work, moved to global wrapper
	_pindir = pindir;
	last_update = micros();
	tics = 0;
	_pinenc = pinenc;
	//attachInterrupt(pinenc - 2, incTic(), CHANGE);

	//set bias direction for encoder
	_biasdir = biasdir;

	//Initialize PID values
	error = 0;
	sum_error = 0;
	prev_error = 0;
}

float Motor::getTargetSpeed(){
	return target_speed;
}

int Motor::getPinEnc(){
	//translate to interrupts
	return _pinenc-2;
}

int Motor::setPower(int set_power){

	int outputPWM = abs(set_power);
	analogWrite(_pinpwm, outputPWM);
	//Serial.print(set_power);
	//Serial.print("Trying to set direction = ");
	if (set_power > 0 == FWD){
		//Serial.print("FWD");
		digitalWrite(_pinfwd,HIGH);
		digitalWrite(_pinbwd,LOW);

	}else{
		//Serial.print("BWD");
		digitalWrite(_pinfwd,LOW);
		digitalWrite(_pinbwd,HIGH);

	}
	return power;

}


float Motor::setSpeed(float _target_speed){
	target_speed = _target_speed;	
}

int Motor::runPID(){
	//PID control of speed

	error = target_speed - speed;

	sum_error = sum_error + error;
	sum_error = constrain(sum_error,-500,500);

	diff_error = error - prev_error;
	prev_error = error;
	net_error = KP*error + KI*sum_error + KD*diff_error;

	power = power + net_error;
	power = constrain(power,-255,255);

	//Stop condition to prevent steady-state error
	if (abs(target_speed) == 0 && abs(speed) == 0){
		power = 0;
	}

	return setPower(power);
}

int Motor::updateSpeed(){
	//calculate speed and reset tics
	speed = tics;
	tics = 0;
	return speed;
}

void Motor::incTic(){
	//Check motor direction then inc/dec tics
	if((digitalRead(_pindir) == HIGH) == _biasdir){
		tics++;
	}else{
		tics--;
	}
}

void Motor::on(){
	digitalWrite(PIN_STANDBY, HIGH);
}

void Motor::off(){
	digitalWrite(PIN_STANDBY, LOW);
}