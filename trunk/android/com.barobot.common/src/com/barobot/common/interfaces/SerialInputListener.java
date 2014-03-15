package com.barobot.common.interfaces;

public interface SerialInputListener {
	public boolean isEnabled();
	public void onNewData(byte[] data, int length);
	public void onRunError(Exception e);
}
