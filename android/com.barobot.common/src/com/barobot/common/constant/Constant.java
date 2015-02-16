package com.barobot.common.constant;

public class Constant {
	public static final int ANDROID_APP_VERSION		= 10;
	public static final int WIZARD_VERSION			= 8;
	public static final int MAX_FIRMWARE_VERSION	= 16;	

    // Debugging
    public static final String TAG					= "BarobotMainApp";
    public static final String SETTINGS_TAG 		= "BAROBOT";

    // Unique UUID for this application
   // public static final UUID MY_UUID_SECURE =	UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    // commands
//	public static final String TRIGGER = "T";
	public static final String RET = "R";
	public static final String ERROR = "R";
//	public static final String ANALOG = "A";
//	public static final String COMMENT = "-";

	public static final String GETXPOS = "x";
	public static final String GETYPOS = "y";
	public static final String GETZPOS = "z";
//	public static final String REBOOT = "REBOOT";

//	public static final String PING = "PING";
//	public static final String PONG = "RPONG";

//  public static final int DRIVER_X 		= 4;
//	public static final int DRIVER_Y 		= 8;
//	public static final int DRIVER_Z 		= 16;

	public static final int SOFA_SERVER_PORT 		= 8000;
	public static final int MAINBOARD_DEVICE_TYPE 	= 0x10;
	public static final int IPANEL_DEVICE_TYPE 		= 0x11;
	public static final int UPANEL_DEVICE_TYPE 		= 0x13;
	public static int cdefault_address	= 10;		// 10
	public static int cdefault_index	= 2;
	public static int mdefault_address	= 0x01;
	public static int mdefault_index	= 1;
	public static final int BOTTLE_IS_BACK = 2;
	public static final int BOTTLE_IS_FRONT = 4;
	public static final String DATABASE_NAME = "BarobotOrman.db";
	public static final int ACCELERATION_X = 90;

	public static int[] magnet_order = {0,2,1,4,3,6,5,8,7,10,9,11 };	// numer butelki, odjac 1 aby numer ID
	public static int[] bottle_row = {
		BOTTLE_IS_BACK,					// 0, num 1
		BOTTLE_IS_FRONT,				// 1, num 2
		BOTTLE_IS_BACK,					// 2, num 3
		BOTTLE_IS_FRONT,				// 3, num 4
		BOTTLE_IS_BACK,					// 4, num 5
		BOTTLE_IS_FRONT,				// 5, num 6
		BOTTLE_IS_BACK,					// 6, num 7
		BOTTLE_IS_FRONT,				// 7, num 8
		BOTTLE_IS_BACK,					// 8, num 9
		BOTTLE_IS_FRONT,				// 9, num 10
		BOTTLE_IS_BACK,					// 10, num 11
		BOTTLE_IS_FRONT,				// 11, num 12
	};
	//	private static String metadata		= "http://barobot.com/android_data/database.json";
	public static String drinks				= "http://barobot.com/android_data/drinks.json";
	//	private static String errorlog		= "http://barobot.com/android_data/error.php";
	public static String upload				= "http://barobot.com/synchro/store.php";
	public static String databaseWeb		= "http://barobot.com/synchro/" + DATABASE_NAME;
	public static String firmwareWeb		= "http://barobot.com/synchro/barobot.hex";
	public static String android_app		= "http://barobot.com/synchro/Barobot.apk";
	public static String firmwareWeb_beta	= "http://barobot.com/synchro/barobot_beta.hex";
	public static String android_app_beta	= "http://barobot.com/synchro/Barobot_beta.apk";
	public static String databaseWeb_beta	= "http://barobot.com/synchro/"+ "BarobotOrman_beta.db";

	public static String localDbPath		= "/data/data/com.barobot/databases/" + DATABASE_NAME;
//	public static String sourcepath			= "/storage/emulated/0/download/" + DATABASE_NAME;
	public static String home_path			= "/Barobot";			// relative path
	public static String copyPath			= "/Barobot/" + DATABASE_NAME;
	public static String logFile			= "/Barobot/log.log";
	public static String firmware			= "/Barobot/barobot.hex";
	public static String backupPath			= "/Barobot/BarobotOrman%DATE%.db";

	public static String raport_manager		= "http://barobot.com/synchro/raport_error.php";
	public static String robot_id_manager	= "http://barobot.com/synchro/init.php";
	public static String version_index		= "http://barobot.com/synchro/check_version.php";

	public static int glass_tray_weight		= 121;	// glass tray weight in g
	static public boolean use_beta			= false;
	public static boolean allowUnsafe 		= use_beta || false;
}
