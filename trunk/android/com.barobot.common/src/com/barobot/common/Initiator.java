package com.barobot.common;

import com.barobot.common.interfaces.CanLog;

public class Initiator {
	public static CanLog logger = new DesktopLogger();
	public static void setLogger( CanLog l ){
		Initiator.logger = l;
	}
}


