package com.barobot.isp;

import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.hardware.devices.BarobotEventListener;

public class DesktopEventListener implements BarobotEventListener {
	private BarobotConnector barobot;

	public DesktopEventListener(BarobotConnector barobot) {
		this.barobot	= barobot;
	}
}
