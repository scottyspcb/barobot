package com.barobot.common.interfaces;

import com.barobot.common.Initiator;

public abstract class RunnableWithData implements Runnable{
	protected String data="";
	protected Exception error;

	public void sendData( String source ) {
		this.data = source;
	}
	public abstract void run();
	public void sendError(Exception e) {
		Initiator.logger.appendError(e);
		error = e;
	}
}