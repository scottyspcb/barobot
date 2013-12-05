package com.barobot.wire;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.microbridge.server.Client;
import org.microbridge.server.Server;
import org.microbridge.server.ServerListener;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.barobot.BarobotMain;
import com.barobot.utils.Arduino;
import com.barobot.utils.Constant;
import com.barobot.utils.input_parser;

public class ADB_wire implements Wire, ServerListener{
	private static int adb_port = 4567;
	private boolean adb_connected = false;
	private Server server = null;
	
	@Override
	public boolean init() {
		boolean autoadb = false;
        if(autoadb){
        	boolean res = false;
    		// Create TCP server (based on  MicroBridge LightWeight Server)
    		try{
    			server = new Server(ADB_wire.adb_port); //Use the same port number used in ADK Main Board firmware
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
    			res = true;
    			return true;
    		} catch (IOException e){
    			Constant.log(Constant.TAG, "+ ADB Server error", e );
    		}
			if(res == false ){
				Constant.log(Constant.TAG, "Unable to start TCP server" );
			}
        }
        return false;
	}

	@Override
	public void setup() {
	}
	@Override
	public void setOnReceive() {
		// TODO Auto-generated method stub
	}
	@Override
	public void setSearching(boolean active) {
		// nie dotyczy
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public boolean isConnected() {
		return adb_connected;
	}

	@Override
	public void disconnect() {
		server.stop();
	}

	@Override
	public boolean send(String command) throws IOException {
		if( this.adb_connected){
//			Constant.log(Constant.TAG, "+ trysend ADB: " + command);
			server.send(command);		
		}
		return false;
	}

	@Override
	public boolean implementAutoConnect() {
		return false;
	}

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
	
	private Context getContext(){
		Context bb = BarobotMain.getInstance();
		if(bb!=null){
			return bb;
		}
		return null;
	}
	// ADB
	
	
	public void onServerStarted(Server server){
		Constant.log(Constant.TAG, "+ onServerStarted");
		Context bb = getContext();
		if(bb!=null){
			Toast.makeText(bb, "Server started", Toast.LENGTH_LONG).show();
		}
	}

	public void onServerStopped(Server server){
		this.adb_connected = false;
		stateHasChanged();
		Constant.log(Constant.TAG, "+ onServerStopped");
		Context bb = getContext();
		if(bb!=null){
			Toast.makeText(bb, "ADB onServerStopped", Toast.LENGTH_LONG).show();
		}
	}

	public void onClientConnect(Server server, Client client){
		this.adb_connected = true;
		stateHasChanged();
		Constant.log(Constant.TAG, "+ onClientConnect");
	}

	public void onClientDisconnect(Server server, Client client){
		Context bb = getContext();	
		if(bb!=null){
			Toast.makeText(bb, "ADB onClientDisconnect", Toast.LENGTH_LONG).show();
		}
		this.adb_connected = false;
		stateHasChanged();
		Constant.log(Constant.TAG, "+ onClientDisconnect");
	}

 // koniec serwera ADB
	public void stateHasChanged() {
		Arduino ar =  Arduino.getInstance();
		ar.clear();
	}

	@Override
	public void destroy() {
		if(server != null && server.isRunning() ){
			server.stop();
		}
	}

	@Override
	public boolean setAutoConnect(boolean active) {
		return false;
	}

	@Override
	public void connectToId(String address) {
	}

	@Override
	public String getName() {
		return "ADB";
	}

	@Override
	public boolean canConnect() {
		// TODO Auto-generated method stub
		return true;
	}
}
