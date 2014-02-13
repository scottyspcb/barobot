package com.barobot.isp;

public class IspSettings {
	public static boolean safeMode		= true;
	public static boolean setFuseBits	= false;
	public static boolean setHex		= false;
	public static boolean force			= false;

	public static int verbose			= 3;
	public static String avrDudePath	= "D:\\PROG\\arduino-1.0.5\\hardware\\tools\\avr\\bin\\avrdude";
	public static String configPath		= "D:\\PROG\\arduino-1.0.5\\hardware\\tools\\avr\\etc\\avrdude.conf";
	public static String upHexPath		= "C:\\Temp\\build8082421301939424932.tmp\\barobot_upanel.cpp.hex";
	public static String mbHexPath		= "C:\\workspace\\Barobot\\arduino\\projects2\\barobot_mainboard\\build\\barobot_mainboard.hex";
	public static String carretHexPath	= "C:\\workspace\\Barobot\\arduino\\projects2\\barobot_carret\\build\\barobot_carret.hex";
	public static int fullspeed			= 115200;
	public static int programmspeed		= 19200;

	public static int last_found_device = 0;
	public static int wait_tries		= 30;
	public static int wait_time			= 200;
	public static int reset_tries		= 3;

	public static String verbose() {
		String repeated = new String(new char[verbose]).replace("\0", " -v" );
		return repeated;
	}
}
