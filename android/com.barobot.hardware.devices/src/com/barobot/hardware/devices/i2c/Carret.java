package com.barobot.hardware.devices.i2c;

import com.barobot.common.IspSettings;

public class Carret extends I2C_Device_Imp {

	public Carret(int index, int address ){
		this.cpuname	= "m328p";
		this.myaddress	= address;
		this.myindex	= index;
		this.lfuse		= "0xFF";
		this.hfuse		= "0xDB";
		this.lock		= "";
		this.efuse		= "0x05";
	}

	public String getReset() {
		return "RESET"+ this.myindex;
	}
	public String getIsp() {
		return "P"+ this.myindex;
	}
	public String getHexFile() {
		return IspSettings.carretHexPath;
	}
}
