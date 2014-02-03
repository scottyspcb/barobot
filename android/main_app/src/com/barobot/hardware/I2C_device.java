package com.barobot.hardware;

import java.util.ArrayList;
import java.util.List;

public class I2C_device{
	byte address = 0;
	byte type	= 0;
	byte version = 0;
	int ping_time = 0;
	String name = "";
	List<I2C_device> canReset = new ArrayList<I2C_device>();	// moze miec kilka
	I2C_device canBeResetedBy = null;	// tylko jedem moze resetowac
	I2C_device( int type, String name ){
		this.type = (byte) type;
		this.name = name;		
	}
	public void hasResetTo(I2C_device dev2 ) {
		dev2.isResetedBy( this );
		canReset.add(dev2);
	}
	private void isResetedBy(I2C_device i2c_device) {
		canBeResetedBy = i2c_device;
	}
	private rpc_message send( int why_code, byte[] params ) {
		String res = I2C.createCommand( this.address, why_code, params);
		rpc_message m =  new rpc_message( true, res, true );
		return m;
	}	
}
