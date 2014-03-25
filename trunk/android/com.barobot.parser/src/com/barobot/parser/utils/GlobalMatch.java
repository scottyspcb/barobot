package com.barobot.parser.utils;

import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.message.Mainboard;

public interface GlobalMatch{
	public boolean run( Mainboard asyncDevice, String fromArduino, String wait4Command, AsyncMessage wait_for );
	public String getMatchRet();
	public String getMatchCommand();
}
