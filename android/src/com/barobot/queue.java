package com.barobot;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import org.microbridge.server.AbstractServerListener;
import org.microbridge.server.Client;
import org.microbridge.server.Server;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class queue extends AbstractServerListener{
	//private static Queue<String> input = new LinkedList<String>();
	private static queue instance = null;
	private static int adb_port = 14568;
	private Queue<String> output = new LinkedList<String>();
	private Server server = null;
    // Local Bluetooth adapter
    private static BluetoothAdapter mBluetoothAdapter = null;
	public static String bt_connected_device = null;

    // Member object for the chat services
    private static BluetoothChatService mChatService = null;
	private boolean bt_exists;

	public boolean adb_connected = false;
	public boolean bt_connected = false;
	public static queue getInstance(){
		if( instance == null){
			instance = new queue();
		}
		return instance;
	}

	public boolean connectADB(){
		// Create TCP server (based on  MicroBridge LightWeight Server)
		try{
			server = new Server(adb_port); //Use the same port number used in ADK Main Board firmware
			if(server.isRunning()){
				Constant.log(Constant.TAG, "Server already running");	
			}
			server.start();
			server.addListener( this );
			Constant.log(Constant.TAG, "+ ADB Server start");
			Context bb = getContext();
			if(bb!=null){
				Toast.makeText(bb, "ADB Server start", Toast.LENGTH_LONG).show();
			}
			return true;
		} catch (IOException e){
			return false;
		}
	}
	private Activity getView() {
		return BarobotMain.getInstance();
	//	return DebugWindow.getInstance();
	}

    // The Handler that gets information back from the BluetoothChatService
    public final Handler mHandler = new Handler() {
        @Override
        public synchronized void handleMessage(Message msg) {
        	//BarobotMain.getInstance().mHandler.handleMessage(msg);
            switch (msg.what) {
            	case Constant.MESSAGE_STATE_CHANGE:
            		Constant.log(Constant.TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
	                switch (msg.arg1) {
	                case Constant.STATE_CONNECTED:
	                    break;
	                case Constant.STATE_CONNECTING:
	                    break;
	                case Constant.STATE_LISTEN:
	                case Constant.STATE_NONE:
	                    break;
	                }
	                break;
            case Constant.MESSAGE_WRITE:
                break;
            case Constant.MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
                //Log.i(Constant.TAG, "buffer read " + readMessage );
				input_parser.readInput(readMessage);
				saveInHistory(readMessage);
                break;
            case Constant.MESSAGE_DEVICE_NAME:
                // save the connected device's name
            	queue.bt_connected_device = msg.getData().getString(Constant.DEVICE_NAME);
            	queue.getInstance().bt_exists = true;
            	queue.getInstance().bt_connected = true;
            	Activity act1 = getView();
            	Toast.makeText( act1.getApplicationContext(), "Connected to "
                               + queue.bt_connected_device, Toast.LENGTH_SHORT).show();
                break;
            case Constant.MESSAGE_TOAST:
            	Activity act2 = getView();
                Toast.makeText( act2.getApplicationContext(), msg.getData().getString(Constant.TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };

	public void setupBT(BarobotMain barobotMain) {
		Constant.log(Constant.TAG, "setupBT()");
        // Initialize the BluetoothChatService to perform bluetooth connections
        try {
			mChatService = new BluetoothChatService( queue.getInstance().mHandler);
			autoconnect();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Constant.log(Constant.TAG, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
		}
	}

    private void autoconnect() {
    	String bt_id = virtualComponents.get( "LAST_BT_DEVICE", "");
	    Constant.log(Constant.TAG, "ostati BT "+ bt_id);
	    if(bt_id!= null && !"".equals(bt_id) ){
	    	queue.connectBTDeviceId(bt_id);
	    }
    }

    public boolean checkBT() {
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
        	bt_exists = false;
            return false;
        }    	   	
        return true;
    }

    public static void connectBTDeviceId(String address) {
        // Get the BluetoothDevice object    	
        Constant.log(Constant.TAG, "zapisuje BT "+ address);
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device);
    	// remember device ID
        virtualComponents.set( "LAST_BT_DEVICE",address);
    } 
    
	public static int startBt() {
        Constant.log(Constant.TAG, "++ ON START ++");

        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if(mBluetoothAdapter==null){
        	return 12;
        }
        if (mBluetoothAdapter.isEnabled()) {
        	if (mChatService == null){ 
        		return 34;
        	}
        }else{
        	return 12;
        }
        return 1;
	}





	public boolean isBT(){
		return bt_connected;
	}
	public boolean isAdb(){
		return adb_connected;
	}
//pochodzace z serwera ADB
	// ADB
	public void onReceive(org.microbridge.server.Client client, byte[] data){
		if( data.length > 0 ){	
			try {
				String in = new String(data, "UTF-8");
				
				Log.i(Constant.TAG, "ADB input [" + in +"]" );

				input_parser.readInput(in);	
				saveInHistory(in);
			} catch (UnsupportedEncodingException e) {
				Constant.log(Constant.TAG, "+ onReceive UnsupportedEncodingException ",e );
			}
		}
	}
	public Context getContext(){
		Context bb = BarobotMain.getInstance();
		if(bb!=null){
			return bb;
		}
		bb = DebugWindow.getInstance();
		if(bb!=null){
			return bb;
		}
		return null;
	}
	public void onServerStarted(Server server){
		Constant.log(Constant.TAG, "+ onServerStarted");
		Context bb = getContext();
		if(bb!=null){
			Toast.makeText(bb, "Server started", Toast.LENGTH_LONG).show();
		}
	}

	public void onServerStopped(Server server){
		this.adb_connected = false;
		Constant.log(Constant.TAG, "+ onServerStopped");
		Context bb = getContext();
		if(bb!=null){
			Toast.makeText(bb, "ADB onServerStopped", Toast.LENGTH_LONG).show();
		}
	}

	public void onClientConnect(Server server, Client client){
		this.adb_connected = true;
		Constant.log(Constant.TAG, "+ onClientConnect");
	}

	public void onClientDisconnect(Server server, Client client){
		Context bb = getContext();
		if(bb!=null){
			Toast.makeText(bb, "ADB onClientDisconnect", Toast.LENGTH_LONG).show();
		}
		this.adb_connected = false;
		Constant.log(Constant.TAG, "+ onClientDisconnect");
	}
// koniec serwera ADB
	
	/*
 	if(mChatService==null || mChatService.getState() != Constant.STATE_CONNECTED){
		Toast.makeText(BarobotMain.getInstance(), R.string.not_connected, Toast.LENGTH_SHORT).show();
//  		return 0;
	}else{
        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);
 //           return 1;
        }
	}
	*/
	public int send( String message ){
		if( message != null && message!= ""){
			output.add(message + input_parser.separator );
			saveOutHistory(message);
			/*
			int id = R.id.editText1;
			if(message!=null){
				DebugWindow dd = DebugWindow.getInstance();
				if(dd!=null){
					dd.setText( id, message );
				}
			}*/
			exec();
		}
		return 1;
	}
	private void saveInHistory( String command ){

	}
	private void saveOutHistory( String command ){
	}
	private void exec(){
		Iterator<String> iter = output.iterator();
		try	{
			while (iter.hasNext()) {
				String ob = iter.next();
				if( this.adb_connected){
					Constant.log(Constant.TAG, "+ trysend ADB: " + ob);
					server.send(ob);		
				}else if(mChatService!=null && mChatService.getState() == Constant.STATE_CONNECTED ) {
		            // Get the message bytes and tell the BluetoothChatService to write
		        	byte[] send = ob.getBytes();
		        	mChatService.write(send);
		        }else{
		        	Constant.log(Constant.TAG, "+ NO_CONN: " + ob);
		        }
                DebugWindow dd = DebugWindow.getInstance();
                if(dd!= null){
                	dd.addToList(ob, true );
                }
			    iter.remove();
			}
		} catch (IOException e)	{
			Constant.log(Constant.TAG, "problem sending TCP message");
		}
	}
	public static void stop() {
        if (mChatService != null){ 
        	mChatService.stop();
        }
        Constant.log(Constant.TAG, "--- ON DESTROY ---");
	}
	public static void resume() {
        Constant.log(Constant.TAG, "+ ON RESUME +");
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == Constant.STATE_NONE) {
              // Start the Bluetooth chat services
              mChatService.start();
            }
        }
	}
}
