package com.barobot.i2c;

import com.barobot.isp.Hardware;
import com.barobot.isp.IspSettings;

public class Carret extends I2C_Device_Imp {
	private int default_address	= 0x0A;
	private int default_index	= 2;
	public Carret(){
		this.cpuname		= "m328p";
		this.myaddress		= default_address;
		this.myindex		= default_index;
	}

	public String setFuseBits(Hardware hw) {
		String command = IspSettings.avrDudePath + " -C"+ IspSettings.configPath +" "+ IspSettings.verbose()+ " " +
		"-p"+ this.cpuname +" -c"+this.protocol+" -P\\\\.\\"+hw.comPort+" -b" + this.bspeed + " " +
		" -U lfuse:w:0xFF:m -U hfuse:w:0xDB:m -U efuse:w:0x05:m";
		if(IspSettings.safeMode){
			command = command + " -n";
		}
		return command;
	}

	public void reset(Hardware hw) {
		hw.send("RESET"+ this.myindex);
	}

	public void isp(Hardware hw) {
		hw.send("P"+ this.myindex );
	}

	public String getHexFile() {
		return IspSettings.carretHexPath;
	}
}
