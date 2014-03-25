package com.barobot.hardware.devices.i2c;

import java.io.File;

import com.barobot.common.IspSettings;
import com.barobot.common.constant.LowHardware;
import com.barobot.common.constant.Methods;
import com.barobot.parser.Queue;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.utils.Decoder;

public abstract class I2C_Device_Imp implements I2C_Device{
	protected int myaddress = 0;
	protected int myindex = 0;
	protected int order = -1;
	protected String cpuname = "";
	protected String protocol = "stk500v1";
	protected int bspeed = IspSettings.programmspeed;
	public int have_reset_address	= -1;

	protected String lfuse = "";
	protected String hfuse = "";
	protected String lock = "";
	protected String efuse = "";
	protected I2C_Device canBeResetedBy;

	public I2C_Device_Imp() {
	}
	@Override
	public void setLed(Queue q, String selector, int pwm) {
		String command = "L" + myaddress + ","+ selector +"," + pwm;
		q.add( command, true );
	}

	@Override
	public void setAddress(int myaddress) {
		this.myaddress = myaddress;
	}

	@Override
	public int getAddress() {
		return myaddress;
	}

	@Override
	public void setIndex(int myindex) {
		this.myindex = myindex;
	}

	@Override
	public int getIndex() {
		return myindex;
	}

	@Override
	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public int getOrder() {
		return order;
	}

	@Override
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

	@Override
	public String checkFuseBits(String comPort) {
		String command = IspSettings.avrDudePath + " -C"+ IspSettings.configPath +" "+ I2C_Device_Imp.verbose( IspSettings.verbose )+ " " +
		"-p"+ this.cpuname +" -c"+this.protocol +" -P\\\\.\\"+comPort+" -b" + this.bspeed + " " +
		" -U lock:r:-:h";
		if(IspSettings.safeMode){
			command = command + " -n";
		}
		return command;
	}

	@Override
	public String erase(String filePath, String comPort) {
		String command = IspSettings.avrDudePath + " -C"+ IspSettings.configPath +" "+I2C_Device_Imp.verbose( IspSettings.verbose )+ " " +
		"-p"+ this.cpuname +" -c"+this.protocol+" -P\\\\.\\"+comPort+" -b" + this.bspeed + " " +
		"-e";
		if(IspSettings.safeMode){
			command = command + " -n";
		}
		return command;
	}

	@Override
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
	@Override
	public void reset(Queue q ) {
		q.add( this.getReset(), false );
	}
	@Override
	public void isp(Queue q) {
		q.add( this.getIsp(), false );
	}

	public static String verbose( int verbose ) {
		return Decoder.strRepeat(" -v", verbose);
	}
	public Queue progStartCommand() {
		Queue q = new Queue();
		// prog mode on, powiadom wszystkich
		for (I2C_Device v : I2C.lista){
		    if(v.getAddress() > 0 && v.getAddress() !=  LowHardware.I2C_ADR_MAINBOARD){
		    	q.add( 
		    			I2C.send( LowHardware.I2C_ADR_MAINBOARD, 
		    					Methods.METHOD_PROG_MODE_ON,
		    					new byte[]{(byte) this.myaddress}
		    			));
		    }
		}
		if( myindex > 0 ){
			q.add(new AsyncMessage( "RESETN " + myindex, true, true ));
		}else{
			// znajdz poprzedni i kaz zresetowac mnie
			if(canBeResetedBy!= null){
				q.add(I2C.send( canBeResetedBy.getAddress(), Methods.METHOD_RESET_NEXT ));
			}
		}
		return q;
	}

	public Queue progEndCommand() {
		Queue q = new Queue();
		// prog mode off, powiadom wszystkich
		for (I2C_Device v : I2C.lista){
		    if(v.getAddress() > 0 && v.getAddress() !=  LowHardware.I2C_ADR_MAINBOARD){
		    	q.add( I2C.send( LowHardware.I2C_ADR_MAINBOARD, Methods.METHOD_PROG_MODE_OFF ));
		    }
		}
		return q;
	}
	public void hasResetTo(I2C_Device dev2 ) {
		dev2.isResetedBy( this );
		//canReset.add(dev2);
	}
	public void isResetedBy(I2C_Device i2c_device) {
		canBeResetedBy = i2c_device;
	}
	public Queue resetCommand() {
		Queue q = new Queue();
		if( myindex > 0 ){
			q.add( new AsyncMessage( "RESETN " + myindex, true, true ));
		}else{
			// znajdz poprzedni i kaz zresetowac mnie
			if(canBeResetedBy!= null){
				q.add(I2C.send( canBeResetedBy.getAddress(), Methods.METHOD_RESET_NEXT ));
				q.add(I2C.send( canBeResetedBy.getAddress(), Methods.METHOD_RUN_NEXT ));
			}
		}
		return q;
	}
	
	
	
	
	
}
