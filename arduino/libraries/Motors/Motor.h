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
 * Motor.h - Motor + encoder combo PID controller
 *
 * Setup with the proper pin definitions
 */

#ifndef Motor_h
#define Motor_h

#include "Arduino.h"

class Motor
{

public:

	Motor(int pinfwd, int pinbwd, int pinpwm, int pindir, int pinenc, bool biasdir);
	
	//Exposed Methods
	float setSpeed(float _target_speed);
	float getTargetSpeed();

	//Timed tasks
	int updateSpeed();
	int runPID();

	//Encoder Helpers
	int getPinEnc();
	void incTic();

	//Motors on/off
	static void on();
	static void off();

private:
	
	//internal functions
	int setPower(int power);

	//Hardware definitions
	int _pinfwd, _pinbwd, _pinpwm, _pindir, _pinenc;
	bool _biasdir;

	//Encoder Internal Variables
	unsigned long last_update;
	volatile signed int tics;

	//State Variables
	
	float error, prev_error, sum_error, diff_error;
	
	int speed;
	float target_speed;
	float net_error;
	int power;
};


#endif //__Motor_H__