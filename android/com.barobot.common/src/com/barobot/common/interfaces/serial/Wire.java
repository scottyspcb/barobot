package com.barobot.common.interfaces.serial;

import java.io.IOException;


public interface Wire extends CanSend{
	public boolean isMainConnection = false;
	public boolean init();
	public String getName();
	public void setOnReceive(SerialInputListener inputListener);
	public void setSearching( boolean active );
	public void resume();
	public boolean setAutoConnect( boolean active );
	@Override
	public boolean isConnected();
	public void close();
	@Override
	public boolean send( String message ) throws IOException;
	public boolean send(byte[] buf, int size ) throws IOException;
	public boolean canConnect();
	public boolean implementAutoConnect();
	public void destroy();
	public void connectToId(String address);
	public void setBaud(int i);
	public void setSerialEventListener(SerialEventListener iel);
	public SerialEventListener getSerialEventListener();
	public void reset();
	boolean open();
	void onPause();
	Wire newInstance();
	SerialInputListener getReceiveListener();
}
