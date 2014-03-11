package com.barobot.wire;

public interface InputListener {
	void onNewData(byte[] data);
	void onRunError(Exception e);
}
