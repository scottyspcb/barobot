package com.barobot.hardware.serial;

public interface InputListener {
	void onNewData(byte[] data);
	void onRunError(Exception e);
}
