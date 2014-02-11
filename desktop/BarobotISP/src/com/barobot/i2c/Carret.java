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
		"-p m328p -cstk500v1 -P\\\\.\\"+hw.comPort+" -b" + IspSettings.programmspeed + " " +
		" -U lfuse:w:0xFF:m -U hfuse:w:0xDB:m -U efuse:w:0x05:m";
		if(IspSettings.safeMode){
			command = command + " -n";
		}
		return command;
	}

	public String uploadCode(Hardware hw, String filePath) {
		String command = IspSettings.avrDudePath + " -C"+ IspSettings.configPath +" "+ IspSettings.verbose()+ " " +
		"-pm328p -cstk500v1 -P\\\\.\\"+hw.comPort+" -b" + IspSettings.programmspeed + " " +
		"-Uflash:w:"+filePath+":i";
		if(IspSettings.safeMode){
			command = command + " -n";
		}
		return command;
	}

	public String erase(Hardware hw, String filePath) {
		String command = IspSettings.avrDudePath + " -C"+ IspSettings.configPath +" "+ IspSettings.verbose()+ " " +
		"-pm328p -cstk500v1 -P\\\\.\\"+hw.comPort+" -b" + IspSettings.programmspeed + " " +
		"-e";
		if(IspSettings.safeMode){
			command = command + " -n";
		}
		return command;
	}

	public void reset(Hardware hw) {
		hw.send("RESET "+ getIndex());
	}

	public void isp(Hardware hw) {
		hw.send("PROG "+ getIndex() );
	}

	public String getHexFile() {
		return IspSettings.carretHexPath;
	}
}
