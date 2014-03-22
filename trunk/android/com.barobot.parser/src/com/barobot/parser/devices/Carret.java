package com.barobot.parser.devices;

import com.barobot.common.IspSettings;

public class Carret extends I2C_Device_Imp {
	private int default_address	= 0x0A;
	private int default_index	= 2;
	public Carret(){
		this.cpuname	= "m328p";
		this.myaddress	= default_address;
		this.myindex	= default_index;
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