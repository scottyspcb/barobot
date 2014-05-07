package com.barobot.common;

public class IspSettings {
	public static boolean safeMode		= true;
	public static boolean setFuseBits	= false;
	public static boolean setHex		= false;
	public static boolean force			= false;

	public static int verbose			= 3;
	public static String avrDudePath	= "C:\\PROG\\arduino\\hardware\\tools\\avr\\bin\\avrdude";
	public static String configPath		= "C:\\PROG\\arduino\\hardware\\tools\\avr\\etc\\avrdude.conf";
	public static String upHexPath		= "C:\\Temp\\build80277117667458955.tmp\\barobot_upanel.cpp.hex";
	public static String mbHexPath		= "C:\\workspace\\Barobot\\arduino\\projects2\\barobot_mainboard\\build\\barobot_mainboard.hex";
	public static String carretHexPath	= "C:\\workspace\\Barobot\\arduino\\projects2\\barobot_carret\\build\\barobot_carret.hex";
//	public static String carretHexPath	= "c:\\temp\\build5989512994425100310.tmp\\servo_testy.cpp.hex";
	public static String mbBootloaderPath	= "C:\\workspace\\Barobot\\desktop\\BarobotISP\\assets\\optiboot_atmega328.hex";
	//public static String mbBootloaderPath		= "C:\\workspace\\Barobot\\arduino\\projects2\\barobot_mainboard\\build\\barobot_mainboard.hex";
	//public static String mbBootloaderPath		= "C:\\workspace\\Barobot\\desktop\\BarobotISP\\assets\\barobot_mainboard.hex";

	public static int fullspeed			= 115200;
	public static int programmspeed		= 19200;

	public static int wait_tries		= 30;
	public static int wait_time			= 200;
	public static int reset_tries		= 3;
}
