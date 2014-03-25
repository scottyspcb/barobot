package com.barobot.hardware.devices.i2c;

import com.barobot.common.IspSettings;
import com.barobot.parser.Queue;

public class MainBoard extends I2C_Device_Imp {
	private int default_address	= 0x01;
	private int default_index	= 1;

	public MainBoard(){
		this.cpuname		= "m328p";
		this.myaddress		= default_address;
		this.myindex		= default_index;
		this.protocol		= "arduino";
	//	this.bspeed			= 57600;		// arduino bootloader
		this.bspeed			= 115200;		// optiboot can faster
	}
	public String setFuseBits(Queue q) {
		return "";
	}
	public String getReset() {
		return "RESET"+ this.myindex;
	}
	public String getIsp() {
		return "RESET"+ this.myindex;
	}
	public String getHexFile() {
		return IspSettings.mbHexPath;
	}
}
