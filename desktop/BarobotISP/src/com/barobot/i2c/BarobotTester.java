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

	public void reset(Hardware hw) {
		hw.send("RESET"+ this.myindex );
	}

	public String getHexFile() {
		return IspSettings.mbBootloaderPath;
	}

	public void isp(Hardware hw) {
		hw.send("P"+ getIndex(), "SISP" );
	}
}
