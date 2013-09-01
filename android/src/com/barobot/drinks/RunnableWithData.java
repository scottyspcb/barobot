package com.barobot.drinks;

public abstract class RunnableWithData implements Runnable{
	protected String data="";
	protected Exception error;

	public void sendData( String source ) {
		this.data = source;
	}
	public abstract void run();
	public void sendError(Exception e) {
		e.printStackTrace();
		error = e;
	}
}