package com.barobot.isp;

import com.barobot.parser.utils.Decoder;

public class IspSettings {
	public static boolean safeMode		= true;
	public static boolean setFuseBits	= false;
	public static boolean setHex		= false;
	public static boolean force			= false;

	public static int verbose			= 3;
	public static String avrDudePath	= "D:\\PROG\\arduino-1.0.5\\hardware\\tools\\avr\\bin\\avrdude";
	public static String configPath		= "D:\\PROG\\arduino-1.0.5\\hardware\\tools\\avr\\etc\\avrdude.conf";
	public static String upHexPath		= "C:\\Temp\\build5085049497541168201.tmp\\barobot_upanel.cpp.hex";
	public static String mbHexPath		= "C:\\workspace\\Barobot\\arduino\\projects2\\barobot_mainboard\\build\\barobot_mainboard.hex";
	public static String carretHexPath	= "C:\\workspace\\Barobot\\arduino\\projects2\\barobot_carret\\build\\barobot_carret.hex";
	//public static String carretHexPath	= "C:\\Temp\\build7947917518911125744.tmp\\analog_read_test.cpp.hex";
	//public static String mbBootloaderPath	= "C:\\workspace\\Barobot\\desktop\\BarobotISP\\assets\\optiboot_atmega328.hex";
	//public static String mbBootloaderPath		= "C:\\workspace\\Barobot\\arduino\\projects2\\barobot_mainboard\\build\\barobot_mainboard.hex";
	public static String mbBootloaderPath		= "C:\\workspace\\Barobot\\desktop\\BarobotISP\\assets\\barobot_mainboard.hex";


	public static int fullspeed			= 115200;
	public static int programmspeed		= 19200;

	public static int wait_tries		= 30;
	public static int wait_time			= 200;
	public static int reset_tries		= 3;


	public static String verbose() {
		return Decoder.strRepeat(" -v", verbose);
	}
}
