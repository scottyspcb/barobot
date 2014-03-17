package com.barobot.other;

import java.util.ArrayList;
import java.util.List;

import com.barobot.common.constant.LowHardware;
import com.barobot.common.constant.Methods;
import com.barobot.parser.Queue;
import com.barobot.parser.message.AsyncMessage;

public class I2C_device{
	byte address = 0;
	byte type	= 0;
	byte version = 0;
	int ping_time = 0;
	int resetCode = -1;

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

	public void hasResetCode(int i) {
		resetCode = i;
	}
	public Queue resetCommand() {
		Queue q = new Queue();
		if( resetCode > 0 ){
			q.add( new AsyncMessage( "RESETN " + resetCode, true, true ));
		}else{
			// znajdz poprzedni i kaz zresetowac mnie
			if(canBeResetedBy!= null){
				q.add(I2C.send( canBeResetedBy.address, Methods.METHOD_RESET_NEXT ));
				q.add(I2C.send( canBeResetedBy.address, Methods.METHOD_RUN_NEXT ));
			}
		}
		return q;
	}
	public Queue progStartCommand() {
		Queue q = new Queue();
		// prog mode on, powiadom wszystkich
		for (I2C_device v : I2C.lista){
		    if(v.address > 0 && v.address !=  LowHardware.I2C_ADR_MAINBOARD){
		    	q.add( I2C.send( LowHardware.I2C_ADR_MAINBOARD, Methods.METHOD_PROG_MODE_ON, new byte[]{this.address} ));
		    }
		}
		if( resetCode > 0 ){
			q.add(new AsyncMessage( "RESETN " + resetCode, true, true ));
		}else{
			// znajdz poprzedni i kaz zresetowac mnie
			if(canBeResetedBy!= null){
				q.add(I2C.send( canBeResetedBy.address, Methods.METHOD_RESET_NEXT ));
			}
		}
		return q;
	}

	public Queue progEndCommand() {
		Queue q = new Queue();
		// prog mode off, powiadom wszystkich
		for (I2C_device v : I2C.lista){
		    if(v.address > 0 && v.address !=  LowHardware.I2C_ADR_MAINBOARD){
		    	q.add( I2C.send( LowHardware.I2C_ADR_MAINBOARD, Methods.METHOD_PROG_MODE_OFF ));
		    }
		}
		return q;
	}
}

