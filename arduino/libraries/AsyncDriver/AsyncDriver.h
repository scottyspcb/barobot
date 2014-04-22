#ifndef ASYNC_DRIVER_HEADER
	#define ASYNC_DRIVER_HEADER
	#include <stdlib.h>
	#include <Arduino.h>

	class AsyncDriver{
	public:
		typedef enum{
			DRIVER	= 1,	// Stepper Driver, 2 pins
			FULL4WIRE = 2,	// 4 wire full stepper, 4 pins
			HALF4WIRE = 4	// 4 wire half stepper, 4 pins
		} MotorInterfaceType;

		AsyncDriver(uint8_t type, uint8_t pin1, uint8_t pin2, uint8_t pin3, uint8_t pin4);
		AsyncDriver(uint8_t pin1, uint8_t pin2, uint8_t disablePin );

		long	distanceToGo();
		long	targetPosition();
		long	currentPosition();
		float	getSpeed();
		float	getAcceleration();
		float	getMaxSpeed();
		boolean haveToRun();
		void	moveTo(long absolute); 
		void	run();
		void	setMaxSpeed(float speed);
		void	setAcceleration(float acceleration);
		void	setSpeed(float speed);
		void	setCurrentPosition(long position);
		void	setOnReady( void (*)(long int) );
		void	stop();
		void	stop( float multispeed );
		void	stopNow();
		void	debug();
		void	disableOutputs();
		void	enableOutputs();

		// Disable outputs on ready?
		volatile boolean		disable_on_ready;
		volatile boolean		is_disabled;

	protected:
		typedef enum{
			DIR_ACW	= 0,// Clockwise
			DIR_CW	= 1	// Counter-Clockwise
		} Direction;

		void	computeNewSpeed( boolean sync );
		void	setOutput2(boolean step, boolean dir );
		void	setOutput4(uint8_t mask);
		void	step(uint8_t step);
		void	step1(uint8_t step);
		void	step4(uint8_t step);
		void	step8(uint8_t step);
		void	fastWrite(uint8_t pin, uint8_t val);
		void	init();
		void	onReady();

	private:
		volatile uint8_t		driver_type;
		volatile uint8_t		_pin[4];
		volatile long			_n;
		volatile long			_currentPos;	// Steps
		volatile long			_targetPos;		// Steps
		volatile float			_speed;			// Steps per second
		volatile float			_acceleration;
		volatile float			_maxSpeed;
		volatile float			_c0;
		volatile float			_cn;
		volatile float			_cmin;
		volatile uint8_t		disablePin;
		volatile uint8_t		last_output;
		volatile unsigned long	lastStepTime;
		volatile unsigned long	stepInterval;
		volatile boolean _direction;
		static void (*user_onReady)(long int);
	};

#endif 
