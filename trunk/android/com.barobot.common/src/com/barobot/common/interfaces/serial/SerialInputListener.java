package com.barobot.common.interfaces.serial;

public interface SerialInputListener {
	public void onNewData(byte[] data, int length);
	public void onRunError(Exception e);
}
