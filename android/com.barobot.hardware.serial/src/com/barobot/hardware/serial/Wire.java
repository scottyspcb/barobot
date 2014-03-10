package com.barobot.hardware.serial;

import java.io.IOException;



import com.barobot.parser.interfaces.CanSend;

public interface Wire extends CanSend{
	public boolean isMainConnection = false;
	public boolean init();
	public String getName();
	public void setOnReceive(InputListener inputListener);
	public void setSearching( boolean active );
	public void pause();
	public void resume();
	public boolean setAutoConnect( boolean active );
	public boolean isConnected();
	public void disconnect();
	public boolean send( String message ) throws IOException;
	public boolean canConnect();
	public boolean implementAutoConnect();
	public void stateHasChanged();
	public void destroy();
	public void setup();
	public void connectToId(String address);
	public void setBaud(int i);
}
