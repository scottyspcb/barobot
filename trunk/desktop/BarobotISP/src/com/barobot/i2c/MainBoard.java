package com.barobot.i2c;

import com.barobot.isp.Hardware;
import com.barobot.isp.IspSettings;

public class MainBoard extends I2C_Device_Imp {
	private int default_address	= 0x01;
	private int default_index	= 1;

	public MainBoard(){
		this.cpuname		= "m328p";
		this.myaddress		= default_address;
		this.myindex		= default_index;
		this.protocol		= "arduino";
		this.bspeed			= 57600;
	}

	public String setFuseBits(Hardware hw) {
		String command = IspSettings.avrDudePath + " -C"+ IspSettings.configPath +" "+ IspSettings.verbose()+ " " +
		"-p"+ this.cpuname +" -c"+this.protocol+" -P\\\\.\\"+hw.comPort+" -b" + this.bspeed + " ";
		//+ " -U lfuse:w:0xFF:m -U hfuse:w:0xDB:m -U efuse:w:0x05:m";
		if(IspSettings.safeMode){
			command = command + " -n";
		}
		return command;
	}
	
	
	public String uploadCode(Hardware hw, String filePath) {
		String command = IspSettings.avrDudePath + " -C"+ IspSettings.configPath +" "+ IspSettings.verbose()+ " " +
		"-p"+ this.cpuname +" -c"+this.protocol+" -P\\\\.\\"+hw.comPort+" -b" + this.bspeed + " " +
		"-Uflash:w:"+filePath+":i";
		if(IspSettings.safeMode){
			command = command + " -n";
		}
		return command;
	}
	
	//-carduino -P\\.\COM39 -b57600 -D -Uflash:w:"c:\workspace\barobot\arduino\projects2\barobot_mainboard\build\barobot_mainboard.hex":i
	
	
	public void reset(Hardware hw) {
		hw.send("RESET "+ this.myindex);
	}
	public void isp(Hardware hw) {
		hw.send("RESET "+ getIndex() );
	}
	public String getHexFile() {
		return IspSettings.mbHexPath;
	}
}
