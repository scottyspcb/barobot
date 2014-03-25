package com.barobot.hardware;

import com.barobot.hardware.devices.BarobotConnector;

public class AndroidEventListener implements BarobotEventListener {
	private BarobotConnector barobot;

	public AndroidEventListener(BarobotConnector barobot) {
		this.barobot	= barobot;
	}

}
