package com.barobot.constant;

public class LowHardware {

	/*------------------------------    i2c     ------------------------------*/
	//public static final int I2C_ADR_MASTER = 0x01;
	public static final int I2C_ADR_MAINBOARD = 0x01;
	public static final int I2C_ADR_RESERVED = 0x06;
	public static final int I2C_ADR_PROGRAMMER = 0x07;
	//public static final int I2C_ADR_TROLLEY = 0x09;
	public static final int I2C_ADR_CARRET = 0x0A;
	public static final int I2C_ADR_USTART =  0x0B;
	public static final int I2C_ADR_UEND = 0x70;

	/*------------------------------ MAINBOARD  ------------------------------*/
	public static final int MAINBOARD_DEVICE_TYPE = 0x10;
	public static final int MAINBOARD_VERSION = 0x01;
	public static final int MAINBOARD_F_CPU = 16000000;
	public static final String MAINBOARD_CPU = "atmega328";
	public static final int MAINBOARD_SERIAL0_BOUND = 115200;

	/*------------------------------ CARRET (carret)    ------------------------------*/
	public static final int CARRET_DEVICE_TYPE = 0x11;
	public static final int CARRET_VERSION = 0x01;
	public static final int CARRET_F_CPU = 16000000;
	public static final String CARRET_CPU = "atmega328";
	public static final int CARRET_SERIAL0_BOUND = 115200;


	/*------------------------------ PROGRAMMER ------------------------------*/
	public static final int PROGRAMMER_DEVICE_TYPE = 0x14;
	public static final int PROGRAMMER_VERSION = 0x01;
	public static final int PROGRAMMER_F_CPU = 12000000;
	public static final String PROGRAMMER_CPU = "atmega8";

	public static final int UPANEL_DEVICE_TYPE= 0x13;
	public static final int UPANEL_VERSION= 0x01;
	public static final int UPANEL_F_CPU =8000000;
	public static final String UPANEL_CPU="atmega8";
	public static final int UPANEL_SERIAL0_BOUND= 115200;

	
	
	
	
}
