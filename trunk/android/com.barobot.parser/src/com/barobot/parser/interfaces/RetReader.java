package com.barobot.parser.interfaces;

import com.barobot.parser.Queue;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.message.Mainboard;

public interface RetReader {
	public boolean isRetOf(Mainboard asyncDevice, AsyncMessage wait_for2, String fromArduino, Queue mainQueue);
}
