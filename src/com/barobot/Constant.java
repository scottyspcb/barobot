package com.barobot;

import java.io.IOException;
import java.util.UUID;

import android.util.Log;

public class Constant {
    // Debugging
    public static final String TAG = "BluetoothChat";
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
    public static final int REQUEST_BEBUG_WINDOW = 32;
    public static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    public static final int REQUEST_ENABLE_BT = 3;
    
    // Debugging
    public static final String TAG2 = "BluetoothChatService";

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
    public static final String TAG3 = "DeviceListActivity";

    // Return Intent extra
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

	public static void log(String tag4, String string) {
		if(Constant.D) {
			Log.w(tag4,string);
		}
	}
	public static void log(String tag22, String string, IOException e) {
		// TODO Auto-generated method stub
		if(Constant.D) {
			Log.w(tag22,string,e);
		}		
	}
	


    
}

