package com.barobot.parser.utils;

import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.output.AsyncDevice;

abstract public class GlobalMatch{
	abstract public boolean run( AsyncDevice asyncDevice, String in, AsyncMessage wait_for );
}
