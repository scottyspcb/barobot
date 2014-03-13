package com.barobot.wire;

import java.io.IOException;

public interface Wire{
	public boolean isMainConnection = false;
	public boolean init();
	public String getName();
	public void setOnReceive(InputListener inputListener);
	public void setSearching( boolean active );
	public void resume();
	public boolean setAutoConnect( boolean active );
	public boolean isConnected();
	public void close();
	public boolean send( String message ) throws IOException;
	public boolean canConnect();
	public boolean implementAutoConnect();
	public void stateHasChanged();
	public void destroy();
	public void connectToId(String address);
	public void setBaud(int i);
}
