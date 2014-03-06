package com.barobot.i2c;

import com.barobot.isp.Hardware;
import com.barobot.isp.IspSettings;

public class BarobotTester extends I2C_Device_Imp {
	private int default_index	= 4;
	private long speed;

	public BarobotTester(){
		this.cpuname	= "m328p";
		this.myindex	= default_index;
		this.speed		= 16000000L;
		this.lfuse		= "0xFF";
		this.hfuse		= "0xDE";
		this.lock		= "";
		this.efuse		= "0x05";
	}

	public String getReset() {
		return "RESET"+ this.myindex;
	}
	public String getIsp() {
		return "P"+ this.myindex;
	}
	public void isp(Hardware hw) {
		hw.send("P"+ getIndex(), "SISP" );		// and wait for device
	}
	public String getHexFile() {
		return IspSettings.mbBootloaderPath;
	}

	public int resetAndReadI2c(Hardware hw) {
		// TODO Auto-generated method stub
		return 0;
	}
}
