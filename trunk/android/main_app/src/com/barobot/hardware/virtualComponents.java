package com.barobot.hardware;

import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.parser.Queue;

public class virtualComponents {
	public static BarobotConnector barobot;
	public static Queue getMainQ() {
		return virtualComponents.barobot.main_queue;
	}
}
