package com.barobot.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.LinkedList;

import org.microbridge.server.AbstractServerListener;
import org.microbridge.server.Client;
import org.microbridge.server.Server;

import com.barobot.BarobotMain;
import com.barobot.DebugWindow;
import com.barobot.hardware.ArduinoQueue;
import com.barobot.hardware.BluetoothChatService;
import com.barobot.hardware.rpc_message;
import com.barobot.hardware.virtualComponents;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class Arduino extends AbstractServerListener{
	private static Arduino instance = null;
	private static int adb_port = 4567;

	private LinkedList<rpc_message> output2 = new LinkedList<rpc_message>();
	private rpc_message wait_for = null;

	//private ArrayList <message> output3 = new ArrayList <message>();
	//private static Queue<String> input = new LinkedList<String>();
	
	public boolean stop_autoconnect = false;
	private Server server = null;
    private static BluetoothAdapter mBluetoothAdapter = null;    // Local Bluetooth adapter
    private static BluetoothChatService mChatService = null;    // Member object for the chat services

	public boolean adb_connected = false;
	public static Arduino getInstance(){
		if( instance == null){
			instance = new Arduino();
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
			Constant.log(Constant.TAG, "+ ADB Server error", e );
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
            switch (msg.what) {
            	case Constant.MESSAGE_STATE_CHANGE:
	                switch (msg.arg1) {
	                case Constant.STATE_CONNECTED:
	                	Arduino.getInstance().clear();
	                    DebugWindow dd = DebugWindow.getInstance();
	                    if(dd!= null){
	                    	dd.clearList();
	                    }        	
	                    break;
	            //    case Constant.STATE_CONNECTING:
	            //    case Constant.STATE_LISTEN:
	            //    case Constant.STATE_NONE:
	                }
	                break;
	                /*
            case Constant.MESSAGE_WRITE:
                byte[] writeBuf = (byte[]) msg.obj;
                String writeMessage = new String(writeBuf);
                DebugWindow dd = DebugWindow.getInstance();
                if(dd!= null){
                	dd.addToList(writeMessage, true );
                }
                break;*/
            case Constant.MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
                //Log.i(Constant.TAG, "buffer read " + readMessage );
				input_parser.readInput(readMessage);
                break;
            case Constant.MESSAGE_DEVICE_NAME:
                // save the connected device's name
            	mChatService.bt_connected_device = msg.getData().getString(Constant.DEVICE_NAME);
            	mChatService.is_connected		= true;
            	Activity act1 = getView();
            	Toast.makeText( act1.getApplicationContext(), "Connected to "
                               + mChatService.bt_connected_device, Toast.LENGTH_SHORT).show();
                break;
            case Constant.MESSAGE_TOAST:
            	Activity act2 = getView();
                Toast.makeText( act2.getApplicationContext(), msg.getData().getString(Constant.TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };

	public void destroy() {
		this.clear();
        if (mChatService != null){ 
        	mChatService.destroy();
        }
        Constant.log(Constant.TAG, "--- ON DESTROY ---");
	}
	public void resume() {
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
	public boolean allowAutoconnect() {
		if( this.isBT()){
		//	Constant.log(Constant.TAG, "nie autoconnect bo juz połączony");
			return false;
		}
		if( this.isAdb() ){
		//	Constant.log(Constant.TAG, "nie autoconnect bo ADB");
			return false;
		}
		if (mChatService == null) {
			Constant.log(Constant.TAG, "nie autoconnect bo NULL");
			return false;
		}
		if (stop_autoconnect == true ) {
			Constant.log(Constant.TAG, "nie autoconnect bo STOP");
			return false;
		}
		return true;
	}


	public void setupBT(BarobotMain barobotMain) {
		Constant.log(Constant.TAG, "setupBT()");
        // Initialize the BluetoothChatService to perform bluetooth connections
        try {
			mChatService = new BluetoothChatService( this.mHandler, barobotMain);
			if(this.allowAutoconnect()){
				autoconnect();
			}
		} catch (Exception e) {
			Constant.log(Constant.TAG, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", e);
		}
	}
    public void bt_disconnect() {
    	mChatService.stop();
    }
    public void autoconnect() {
    	String bt_id = virtualComponents.get( "LAST_BT_DEVICE", "");
	    if(bt_id!= null && !"".equals(bt_id) ){
	    	mChatService.connectBTDeviceId(bt_id);
	    }
    }
    public boolean checkBT() {
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
			mChatService.is_connected		= false;
			this.clear();
			return false;
        }    	   	
        return true;
    }

    public static void connectBTDeviceId(String address) {
    	mChatService.connectBTDeviceId(address);
    } 

	public static int startBt() {
      	if (Arduino.mChatService == null){ 
    		return 34;
    	}
		return Arduino.mChatService.initBt();
	}

	public boolean isBT(){
		return mChatService.is_connected;
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
		clear();
		Constant.log(Constant.TAG, "+ onServerStopped");
		Context bb = getContext();
		if(bb!=null){
			Toast.makeText(bb, "ADB onServerStopped", Toast.LENGTH_LONG).show();
		}
	}

	public void onClientConnect(Server server, Client client){
		this.adb_connected = true;
		this.clear();
		Constant.log(Constant.TAG, "+ onClientConnect");
	}

	public void onClientDisconnect(Server server, Client client){
		final DebugWindow bb = DebugWindow.getInstance();		
		if(bb!=null){
			bb.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(bb, "ADB onClientDisconnect", Toast.LENGTH_LONG).show();
				}
			});
		}
		this.adb_connected = false;
		clear();
		Constant.log(Constant.TAG, "+ onClientDisconnect");
	}
// koniec serwera ADB
	
	public void send( String message ){
		rpc_message m = new rpc_message( message, true, false );
		output2.add( m );
		exec();
	}

	public void sendFirst(ArduinoQueue q2) {
		this.output2.addAll( 0, q2.output);		// dodja na począku, reszte przesun dalej
		exec();		
	}

	public void send(ArduinoQueue q) {
		this.output2.addAll(q.output);
		BarobotMain.getInstance().cm.doPhoto();
		exec();
	}

	public boolean read_ret(String retm) {	// czy moze to jest zwrotka
		boolean is_ret = false;
		if( this.wait_for != null){
			//Constant.log("isRet?", "["+message+"][" +  this.wait_for.command+"]");
			is_ret = true;
			if(this.wait_for.isRet(retm)){
		        DebugWindow dd = DebugWindow.getInstance();
				this.wait_for = null;
				if(output2.isEmpty()){
		            if(dd!= null){
		            	dd.addToList( "--------------------------------------------------", true );
		            }
				}else{
					exec();		// wyslij wszystko co jest dalej
				}
			}
		}
		return is_ret;
	}
	
	private synchronized void exec(){
		if(this.wait_for != null){
			return;		// jestem w trakcie oczekiwania
		}
        DebugWindow dd = DebugWindow.getInstance();
		try	{
			boolean wasEmpty = output2.isEmpty();
			while (!output2.isEmpty()) {
				rpc_message m = output2.pop();

				if( m.command == null){
					m.start( this );
				}else{
					this.passString(m.command);
				}
		        if(dd!= null){
		        	dd.addToList( m );
		        }
				if(m.isBlocing()){		// czekam na zwrotkę tej komendy zanim wykonam coś dalej
					m.send_timestamp	= System.nanoTime();
                	this.wait_for		= m;
                	return;					// przerwij do czasu otrzymania zwrotki lub odblokowania
                }else{
                	this.wait_for = null;
                }
			}
			if(!wasEmpty){
	            if(dd!= null){
	            	dd.addToList( "--------------------------------------------------", true );
	            }
			}
		} catch (IOException e)	{
			Constant.log(Constant.TAG, "problem sending TCP message",e);
		}
	}
	private void passString( String command ) throws IOException {
		if( this.adb_connected){
//			Constant.log(Constant.TAG, "+ trysend ADB: " + command);
			server.send(command + input_parser.separator);		
		}else if(mChatService!=null && mChatService.getState() == Constant.STATE_CONNECTED ) {
//			Constant.log(Constant.TAG, "BT SEND:["+ command +"]");
			String command2 = command + input_parser.separator;
        	byte[] send = command2.getBytes();
        	mChatService.write(send);
        }else{
        	Constant.log(Constant.TAG, "+ NO_CONN: " + command);
        }
	}
    public void unlock() {
    	if(wait_for!=null){
    		Constant.log("+unlock", "czekalem na [" + wait_for.command+"] w kolejce: " +output2.size() );
    		this.wait_for = null;
    		this.exec();
    	}
    }
	public void unlock(rpc_message m) {
		if( this.wait_for != null && this.wait_for.equals(m)){
			this.wait_for = null;
			this.exec();
		}
	}
    public void clear() {
		this.output2.clear();
		this.wait_for = null;
	}

}

/*
public boolean addRetToList( final String last, final String ret ) {
	final DebugWindow dd = DebugWindow.getInstance();
	if(dd!=null){
		int count = dd.mConversationArrayAdapter.getCount();
		for(int i =count-1; i>=0;i--){
			History_item hi = dd.mConversationArrayAdapter.getItem(i);
		//	Constant.log("+addRetToList", last + "/" + ret + "/" + i +"/"+ hi.getCommand() );
			if( hi.direction && hi.getCommand().equals(last)){
				hi.setRet(ret);
	//			Constant.log("+addRetToList","ustawiam " + i + " na " + hi.toString() );
				dd.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						dd.mConversationArrayAdapter.notifyDataSetChanged();
					}
				});
				return true;
			}			
		}
	}
	return false;
}
*/
