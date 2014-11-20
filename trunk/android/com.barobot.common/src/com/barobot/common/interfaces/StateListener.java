package com.barobot.common.interfaces;

public interface StateListener {
	void onUpdate(HardwareState state, String name, String value);
}
