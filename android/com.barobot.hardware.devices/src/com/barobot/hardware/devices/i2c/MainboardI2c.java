package com.barobot.hardware.devices.i2c;

import com.barobot.common.IspSettings;
import com.barobot.parser.Queue;

public class MainboardI2c extends I2C_Device_Imp {

	public MainboardI2c(int index, int address ){
		this.cpuname		= "m328p";
		this.myaddress		= address;
		this.myindex		= index;
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
