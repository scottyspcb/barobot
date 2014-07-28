package com.barobot.other;

public interface OnDownloadReadyRunnable extends Runnable{
	@Override
	public void run();
	public void sendSource( String source );
}