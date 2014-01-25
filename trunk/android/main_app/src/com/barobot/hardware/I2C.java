package com.barobot.hardware;

public class I2C {
	public static void send( byte address, byte why_code ){
		byte[] a = null;
		send( address, why_code, (byte) 0, a );
	}
	public static void send( byte address, byte why_code, byte param_bytes, byte[] params ){
		
		
		
	}
}
