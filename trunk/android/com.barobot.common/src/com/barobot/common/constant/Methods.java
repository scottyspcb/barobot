package com.barobot.common.constant;

public class Methods {

	public static final int METHOD_GETVERSION 	= 5;
	public static final int METHOD_PROG_MODE_ON 	= 10;
	public static final int METHOD_PROG_MODE_OFF 	= 14;
	public static final int METHOD_TEST_SLAVE 	= 19;
	public static final int METHOD_RESETCYCLES 	= 20;
	public static final int METHOD_SETPWM 	= 38;
	public static final int METHOD_SETTIME 	= 42;
	public static final int METHOD_SETFADING 	= 46;
	public static final int METHOD_RESETSLAVEADDRESS 	= 50;
	public static final int METHOD_GETANALOGVALUE 	= 51;
	public static final int METHOD_GETVALUE 	= 55;
	public static final int METHOD_RESET_NEXT 	= 58;
	public static final int METHOD_RUN_NEXT 	= 62;
	public static final int METHOD_GET_Y_POS 	= 65;
	public static final int METHOD_SET_Y_POS 	= 66;
	public static final int METHOD_GET_Z_POS 	= 69;
	public static final int METHOD_SET_Z_POS 	= 70;
	public static final int METHOD_LIVE_OFF 	= 72;
	public static final int METHOD_LIVE_ANALOG 	= 74;
	public static final int METHOD_DRIVER_ENABLE 	= 82;
	public static final int METHOD_DRIVER_DISABLE 	= 86;
	public static final int METHOD_HERE_I_AM 	= 114;
	public static final int METHOD_SEND_PIN_VALUE 	= 118;
	public static final int METHOD_CAN_FILL 	= 120;
	public static final int METHOD_I2C_SLAVEMSG 	= 122;
	public static final int RETURN_DRIVER_ERROR 	= 180;
	public static final int RETURN_DRIVER_READY 	= 182;
	public static final int RETURN_DRIVER_READY_REPEAT 	= 184;
	public static final int RETURN_PIN_VALUE 	= 211;
	public static final int METHOD_STEPPER_MOVING 	= 222;
	public static final int METHOD_IMPORTANT_ANALOG 	= 224;
	public static final int RETURN_I2C_ERROR 	= 53;
	public static final int METHOD_EXEC_ERROR 	= 33;
	public static final int METHOD_DEVICE_FOUND 	= 12;
	public static final int METHOD_SETLED 	= 40;
	public static final int METHOD_SETLEDS 	= 44;
	public static final int METHOD_CHECK_NEXT 	= 188;
	public static final int METHOD_EEPROM_READ_I2C 	= 190;
	public static final int METHOD_EEPROM_WRITE_I2C	= 192;
	public static final int METHOD_GET_X_POS 	= 57;


	public static final String METHOD_SET_X_ACCELERATION	= "AX";
	public static final String METHOD_MASTER_CAN_FILL	= "C";
	public static final String METHOD_SET_X_DISABLE	= "DX";
	public static final String METHOD_ERROR	= "E";
	public static final String METHOD_SET_X_ENABLE	= "EX";
	public static final String METHOD_POING	= "POING";
	public static final String METHOD_PONG	= "PONG";
	public static final String METHOD_PROG	= "PROG";
	public static final String METHOD_PROGN	= "PROGN";
	public static final String METHOD_RESET_BUS	= "RB";
	public static final String METHOD_RESET	= "RESET";
	public static final String METHOD_RWIRE	= "RWIRE";
	public static final String METHOD_SEND2SLAVE	= "S";
	public static final String METHOD_TRIGGER	= "T";
	public static final String METHOD_WAIT_READY	= "WR";
	public static final String METHOD_SET_X_POS	= "X";
	public static final String METHOD_GET_TEMP	= "T";
	public static final String METHOD_MSET_LED	= "L";
	public static final String METHOD_HAS_NEXT	= "N";
	public static final String METHOD_SLAVE_HAS_NEXT	= "n";
	public static final String METHOD_EEPROM_READ	= "M";
	public static final String METHOD_EEPROM_WRITE	= "S";


//24 * 2^0  + 252* 2^1 + 255* 2^2 + 255* 2^3

// typy b³êdów:
	public static final int T_UNKNOWN_E		= 0x05;
	public static final int T_ENGINE_E		= 0x06;
	public static final int T_WRITE_I2C_E	= 0x07;
	public static final int T_READ_I2C_E	= 0x08;



// inne
	public static final int DRIVER_DIR_FORWARD 	= 32;
	public static final int DRIVER_DIR_BACKWARD	= 64;
	public static final int DRIVER_DIR_STOP		= 0;

	public static final int INNER_SERVOY 	= 0;
	public static final int INNER_SERVOZ 	= 1;

	public static final int DRIVER_X 	= 4;
	public static final int DRIVER_Y 	= 8;
	public static final int DRIVER_Z 	= 16;

	public static final int INNER_HALL_X	= 0;
	public static final int INNER_HALL_Y	= 1;
	public static final int INNER_WEIGHT	= 2;
	public static final int INNER_CURRENTY	= 3;
	public static final int INNER_CURRENTZ	= 4;
	public static final int INNER_TABLET	= 5;
	public static final int INNER_MB_TEMP	= 6;
	public static final int INNER_CARRET_TEMP	= 7;


	public static final int HX_STATE_0		= 11;
	public static final int HX_STATE_1		= 22;
	public static final int HX_STATE_2		= 33;
	public static final int HX_STATE_3		= 44;
	public static final int HX_STATE_4		= 55;

	public static final int HX_STATE_5		= 66;

	public static final int HX_STATE_6		= 77;
	public static final int HX_STATE_7		= 88;
	public static final int HX_STATE_8		= 99;
	public static final int HX_STATE_9		= 100;
	public static final int HX_STATE_10		= 111;



	public static final int HX_SPEED 	= 50;

// HALL X VALUES 
	public static final int HX_NEODYM_UP_BELOW 	= 750;
	public static final int HX_NEODYM_UP_START 	= 600;
	public static final int HX_FERRITE_UP_IS_BELOW 	= 560;
	public static final int HX_LOCAL_UP_MAX_OVER 	= 540;

	public static final int HX_NOISE_BELOW	= 531;
	public static final int HX_NOISE_OVER 	= 518;

	public static final int HX_LOCAL_DOWN_IS_BELOW 	= 513;
	public static final int HX_FERRITE_DOWN_IS_BELOW 	= 495;
	public static final int HX_NEODYM_DOWN_START 	= 450;
	public static final int HX_NEODYM_DOWN_OVER 	= 300;

// end HALL X VALUES 




}
