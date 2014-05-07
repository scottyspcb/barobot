package com.barobot.hardware;

import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.hardware.devices.BarobotEventListener;

public class AndroidEventListener implements BarobotEventListener {
	private BarobotConnector barobot;

	public AndroidEventListener(BarobotConnector barobot) {
		this.barobot	= barobot;
	}

}
