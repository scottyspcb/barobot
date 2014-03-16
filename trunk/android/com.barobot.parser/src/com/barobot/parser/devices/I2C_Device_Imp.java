package com.barobot.parser.devices;

import java.io.File;

import com.barobot.common.IspSettings;
import com.barobot.parser.Queue;
import com.barobot.parser.utils.Decoder;

public abstract class I2C_Device_Imp implements I2C_Device{
	protected int myaddress = 0;
	protected int myindex = 0;
	protected int order = -1;
	protected String cpuname = "";
	protected String protocol = "stk500v1";
	protected int bspeed = IspSettings.programmspeed;

	protected String lfuse = "";
	protected String hfuse = "";
	protected String lock = "";
	protected String efuse = "";

	public I2C_Device_Imp() {
	}
	public void setLed(Queue q, String selector, int pwm) {
		String command = "L" +this.getAddress() + ","+ selector +"," + pwm;
		q.add( command, true );
	}

	public void setAddress(int myaddress) {
		this.myaddress = myaddress;
	}

	public int getAddress() {
		return myaddress;
	}

	public void setIndex(int myindex) {
		this.myindex = myindex;
	}

	public int getIndex() {
		return myindex;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public int getOrder() {
		return order;
	}

	public String setFuseBits( String comPort ){
		String command = IspSettings.avrDudePath + " -C"+ IspSettings.configPath +" "+ I2C_Device_Imp.verbose( IspSettings.verbose )+ " " +
		"-p"+ this.cpuname +" -c"+this.protocol+" -P\\\\.\\"+comPort+" -b" + this.bspeed;

		if(!hfuse.equals("")){
			command = command + " -U lfuse:w:"+lfuse+":m";	
		}
		if(!lfuse.equals("")){
			command = command + " -U hfuse:w:"+hfuse+":m";
		}
		if(!efuse.equals("")){
			command = command + " -U efuse:w:"+efuse+":m";
		}	
		if(!lock.equals("")){
			command = command + " -U lock:w:"+ lock +":m";
		}
		if(IspSettings.safeMode){
			command = command + " -n";
		}
		return command;
	}

	public String checkFuseBits(String comPort) {
		String command = IspSettings.avrDudePath + " -C"+ IspSettings.configPath +" "+ I2C_Device_Imp.verbose( IspSettings.verbose )+ " " +
		"-p"+ this.cpuname +" -c"+this.protocol +" -P\\\\.\\"+comPort+" -b" + this.bspeed + " " +
		" -U lock:r:-:h";
		if(IspSettings.safeMode){
			command = command + " -n";
		}
		return command;
	}

	public String erase(String filePath, String comPort) {
		String command = IspSettings.avrDudePath + " -C"+ IspSettings.configPath +" "+I2C_Device_Imp.verbose( IspSettings.verbose )+ " " +
		"-p"+ this.cpuname +" -c"+this.protocol+" -P\\\\.\\"+comPort+" -b" + this.bspeed + " " +
		"-e";
		if(IspSettings.safeMode){
			command = command + " -n";
		}
		return command;
	}

	public String uploadCode( String filePath, String comPort ) {
		String command = IspSettings.avrDudePath + " -C"+ IspSettings.configPath +" "+ I2C_Device_Imp.verbose( IspSettings.verbose )+ " " +
		"-p"+ this.cpuname +" -c"+this.protocol+" -P\\\\.\\"+comPort+" -b" + this.bspeed + " " +
		"-Uflash:w:"+filePath+":i";
		if(IspSettings.safeMode){
			command = command + " -n";
		}
		return command;
	}
	public long isFresh() {
		long b = new File( getHexFile() ).lastModified() / 1000;
		//System.out.println("isFresh " + b );
		return b;
	}
	public void reset(Queue q ) {
		q.add( this.getReset(), false );
	}
	public void isp(Queue q) {
		q.add( this.getIsp(), false );
	}
	
	
	

	public static String verbose( int verbose ) {
		return Decoder.strRepeat(" -v", verbose);
	}
}
