package com.barobot.parser.utils;

import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.output.AsyncDevice;

public interface GlobalMatch{
	public boolean run( AsyncDevice asyncDevice, String fromArduino, String wait4Command, AsyncMessage wait_for );
	public String getMatchRet();
	public String getMatchCommand();
}
