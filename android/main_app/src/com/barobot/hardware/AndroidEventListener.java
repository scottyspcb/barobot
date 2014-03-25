package com.barobot.hardware;

import com.barobot.common.interfaces.HardwareContext;
import com.barobot.common.interfaces.HardwareState;
import com.barobot.hardware.devices.BarobotConnector;

public class AndroidEventListener implements BarobotEventListener {
	private HardwareContext hwc;
	private BarobotConnector barobot;
	private HardwareState state;

	public AndroidEventListener(BarobotConnector barobot, HardwareState state) {
		//this.hwc		= hwc;
		this.barobot	= barobot;
		this.state		= state;
	}

}
