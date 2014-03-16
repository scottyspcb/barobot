package com.barobot.parser.output;

import com.barobot.parser.message.AsyncMessage;

public interface RetReader {
	public boolean isRetOf(AsyncDevice asyncDevice, AsyncMessage wait_for2, String fromArduino);
}
