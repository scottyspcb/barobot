package com.barobot_graph.wire;

import java.io.IOException;

public interface Wire {
	public boolean init();

	public String getName();

	public boolean isMainConnection = false;

	public void setOnReceive();

	public void setSearching(boolean active);

	public void pause();

	public void resume();

	public boolean setAutoConnect(boolean active);

	public boolean isConnected();

	public void disconnect();

	public boolean send(String message) throws IOException;

	public boolean canConnect();

	public boolean implementAutoConnect();

	public void stateHasChanged();

	public void destroy();

	public void setup();

	public void connectToId(String address);
}
