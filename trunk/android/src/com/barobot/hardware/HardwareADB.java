package com.barobot.hardware;

import java.io.IOException;
import java.util.Iterator;

import org.microbridge.server.AbstractServerListener;
import org.microbridge.server.Server;

import com.barobot.utils.Constant;
import com.barobot.utils.Arduino;

import android.content.Context;
import android.widget.Toast;

public class HardwareADB extends AbstractServerListener {
	private static int adb_port = 4567;
	private Server server = null;

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
			Context bb = Arduino.getInstance().getContext();
			if(bb!=null){
				Toast.makeText(bb, "ADB Server start", Toast.LENGTH_LONG).show();
			}
			return true;
		} catch (IOException e){
			return false;
		}
	}

	
	public void connect(){
	//	q.send("GET CARRET");	
		
	}
	
	
	public void onConnect(){
		
	}
	public void onDisconnect(){
		
	}

}
