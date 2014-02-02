package com.barobot.constant;

public class Methods {
	public static int METHOD_GETVERSION =	5;
	public static int METHOD_PROG_MODE_ON =	10;
	public static int METHOD_PROG_MODE_OFF =	14;
	public static int METHOD_TEST_SLAVE =	19;
	public static int METHOD_RESETCYCLES =	20;
	public static int METHOD_SETPWM =	38;
	public static int METHOD_SETTIME =	42;
	public static int METHOD_SETFADING =	46;
	public static int METHOD_RESETSLAVEADDRESS =	50;
	public static int METHOD_GETANALOGVALUE =	51;
	public static int METHOD_GETVALUE =	55;
	public static int METHOD_RESET_NEXT =	58;
	public static int METHOD_RUN_NEXT =	62;
	public static int METHOD_GETSERVOYPOS =	65;
	public static int METHOD_GOTOSERVOYPOS =	66;
	public static int METHOD_GETSERVOZPOS =	69;
	public static int METHOD_GOTOSERVOZPOS =	70;
	public static int METHOD_HERE_I_AM =	114;
	public static int METHOD_SEND_PIN_VALUE =	118;
	public static int METHOD_I2C_SLAVEMSG =	122;
	public static int METHOD_LIVE_ANALOG =	74;
	public static int METHOD_LIVE_OFF =	72;
	public static int METHOD_SEND2SLAVE =	127;
	public static int METHOD_DRIVER_ENABLE =	82;
	public static int METHOD_DRIVER_DISABLE =	86;

	public static int METHOD_RET_FROM_SLAVE =	88;
	public static int METHOD_I2C_ERROR =		90;
	public static int METHOD_RET_FROM_SLAVE2 =	92;

	public static int METHOD_DEVICE_FOUND	=	220;
	public static int RETURN_PIN_VALUE =		211;
	public static int RETURN_ANALOG_CHANGE =	111;

	public static int DRIVER_DIR_FORWARD	= 32;
	public static int DRIVER_DIR_BACKWARD	= 64;
	public static int DRIVER_DIR_STOP		= 0;

	public static int DRIVER_X	= 4;
	public static int DRIVER_Y	= 8;
	public static int DRIVER_Z	= 16;
	
	public static int HALL_GLOBAL_MIN	 	= 1;
	public static int HALL_GLOBAL_MAX 		= 2;
	public static int HALL_LOCAL_MAX		= 4;
	public static int HALL_LOCAL_MIN 		= 8;

}
