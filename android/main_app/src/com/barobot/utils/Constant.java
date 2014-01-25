package com.barobot.utils;


import java.util.UUID;

import android.util.Log;

public class Constant {
    // Debugging
    public static final String TAG = "BarobotBT";
    public static final String SETTINGS_TAG = "BAROBOT";

    public static final boolean D = true;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    public static final int REQUEST_ENABLE_BT = 103;
    
    // Debugging
    public static final String TAG2 = "BarobotBTService";

    // Name for the SDP record when creating server socket
    public static final String NAME_SECURE = "BluetoothChatSecure";

    // Unique UUID for this application
    public static final UUID MY_UUID_SECURE =
    	UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    // Debugging
    public static final String TAG3 = "BTListActivity";
    
    
    // commands
	public static final byte TRIGGER = 'T';
	public static final byte RET = 'R';
	public static final byte ERROR = 'R';
	public static final byte ANALOG = 'A';
	public static final byte COMMENT = '-';
   
	public static final String GETXPOS = "x";
	public static final String GETYPOS = "y";
	public static final String GETZPOS = "z";
	public static final String REBOOT = "REBOOT";

	public static final String PING = "PING";
	public static final String PONG = "PONG";
	

	
    // Return Intent extra
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

	public static void log(String tag4, String string) {
		if(Constant.D) {
			Log.w(tag4,string);
		}
	}
	public static void log(String tag22, String string, Exception e) {
		// TODO Auto-generated method stub
		if(Constant.D) {
			Log.w(tag22,string,e);
		}		
	}    
	
	public static final int DRIVER_X 		= 4;
	public static final int DRIVER_Y 		= 8;
	public static final int DRIVER_Z 		= 16;

	public static final int MAINBOARD_DEVICE_TYPE 	= 0x10;
	public static final int IPANEL_DEVICE_TYPE 		= 0x11;
	public static final int UPANEL_DEVICE_TYPE 		= 0x13;

	public static final int METHOD_GETVERSION 		= 5;
	public static final int METHOD_PROG_MODE_ON 	= 10;
	public static final int METHOD_PROG_MODE_OFF 	= 14;
	public static final int METHOD_TEST_SLAVE 		= 19;
	public static final int METHOD_RESETCYCLES 		= 20;
	public static final int METHOD_SETPWM 			= 38;
	public static final int METHOD_SETTIME 			= 42;
	public static final int METHOD_SETFADING 		= 46;
	public static final int METHOD_RESETSLAVEADDRESS= 50;
	public static final int METHOD_GETANALOGVALUE 	= 51;
	public static final int METHOD_GETVALUE 		= 55;
	public static final int METHOD_RESET_NEXT 		= 58;
	public static final int METHOD_RUN_NEXT 		= 62;
	public static final int METHOD_GET_Y_POS 		= 65;
	public static final int METHOD_SET_Y_POS 		= 66;
	public static final int METHOD_GET_Z_POS 		= 69;
	public static final int METHOD_SET_Z_POS 		= 70;
	public static final int METHOD_LIVE_OFF 		= 72;
	public static final int METHOD_LIVE_ANALOG 		= 74;
	public static final int METHOD_DRIVER_ENABLE 	= 82;
	public static final int METHOD_DRIVER_DISABLE 	= 86;
	public static final int METHOD_HERE_I_AM 		= 114;
	public static final int METHOD_SEND_PIN_VALUE 	= 118;
	public static final int METHOD_CAN_FILL 		= 120;
	public static final int METHOD_I2C_SLAVEMSG 	= 122;
	public static final int RETURN_DRIVER_ERROR 	= 180;
	public static final int RETURN_DRIVER_READY 	= 182;
	public static final int RETURN_PIN_VALUE 		= 211;
	public static final int METHOD_STEPPER_MOVING 	= 222;
	public static final int METHOD_IMPORTANT_ANALOG = 224;
	public static final int RETURN_I2C_ERROR 		= 53;
	public static final int METHOD_GET_X_POS 		= 57;
	public static final int METHOD_EXEC_ERROR		= 22;
	public static final int METHOD_DEVICE_FOUND		= 12;

	
	
	
	
}
