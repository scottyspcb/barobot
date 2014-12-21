package com.barobot.common.interfaces;

public interface OnDownloadRunnable extends OnDownloadReadyRunnable{
	@Override
	public void run();
	public void sendSource( String source );
	public void sendProgress( int value );	
	public void onError(Exception e );	
}