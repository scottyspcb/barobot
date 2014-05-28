package com.barobot.hardware;

import com.barobot.common.interfaces.HardwareContext;
import com.barobot.common.interfaces.HardwareState;
import com.barobot.hardware.devices.BarobotConnector;

public class AndroidHardwareContext implements HardwareContext {
	private BarobotConnector barobot;
	private HardwareState state;

	public AndroidHardwareContext(BarobotConnector barobot, HardwareState state) {
		this.barobot	= barobot;
		this.state		= state;
	}
}
