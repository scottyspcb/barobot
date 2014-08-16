package com.barobot.hardware.devices.i2c;

import com.barobot.common.IspSettings;
import com.barobot.common.constant.Constant;
import com.barobot.parser.Queue;

public class MainboardI2c extends I2C_Device_Imp {

	public MainboardI2c(int index, int address ){
		this.cpuname		= "m328p";
		this.myaddress		= address;
		this.row			= index;
		this.resetIndex			= index;
		this.numInRow		= 0;
		this.protocol		= "arduino";
		this.bspeed			= 57600;		// arduino bootloader
	//	this.bspeed			= 115200;		// optiboot can faster
	}
	public MainboardI2c() {
		this(Constant.mdefault_index, Constant.mdefault_address);
	}
	public String setFuseBits(Queue q) {
		return "-setFuseBits";
	}
	public String getIsp() {
		return "RESET"+ this.row;
	}
	@Override
	public void isp(Queue q) {
		q.add( this.getIsp(), false );		// dont wait
	}
	public String getHexFile() {
		return IspSettings.mbHexPath;
	}
}
