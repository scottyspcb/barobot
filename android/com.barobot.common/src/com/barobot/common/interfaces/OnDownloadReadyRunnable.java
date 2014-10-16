package com.barobot.common.interfaces;

public interface OnDownloadReadyRunnable extends Runnable{
	@Override
	public void run();
	public void sendSource( String source );
	public void sendProgress( int value );	
}