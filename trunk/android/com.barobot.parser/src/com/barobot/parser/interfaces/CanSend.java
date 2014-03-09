package com.barobot.parser.interfaces;

import java.io.IOException;

public interface CanSend {
	public boolean send( String message ) throws IOException;
	public boolean isConnected();
}
