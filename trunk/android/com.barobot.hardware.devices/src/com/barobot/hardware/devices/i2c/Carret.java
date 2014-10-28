package com.barobot.hardware.devices.i2c;

import com.barobot.common.IspSettings;

public class Carret extends I2C_Device_Imp {

	public Carret(int index, int address ){
		this.myaddress	= address;
		this.row		= index;
		this.resetIndex	= index;
		this.numInRow	= 0;
		this.cpuname	= "m328p";
		this.lfuse		= "0xFF";
		this.hfuse		= "0xDB";
		this.lock		= "";
		this.efuse		= "0x05";
	}
	public String getHexFile() {
		return IspSettings.carretHexPath;
	}
}
