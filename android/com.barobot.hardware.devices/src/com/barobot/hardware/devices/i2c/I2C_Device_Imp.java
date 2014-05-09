package com.barobot.hardware.devices.i2c;

import java.io.File;

import com.barobot.common.IspSettings;
import com.barobot.common.constant.Pwm;
import com.barobot.parser.Queue;
import com.barobot.parser.utils.Decoder;

public abstract class I2C_Device_Imp implements I2C_Device{
	protected int myaddress = 0;
	protected int row = -1;
	protected int numInRow = -1;
	protected int index	= -1;
	protected String cpuname = "";
	protected String protocol = "stk500v1";
	protected int bspeed = IspSettings.programmspeed;
	protected String lfuse = "";
	protected String hfuse = "";
	protected String lock = "";
	protected String efuse = "";
	protected I2C_Device canBeResetedBy;
	protected Runnable onchange = null;

	public I2C_Device_Imp() {
	}

	@Override
	public void addLed(Queue q, String selector, int pwm ) {
		pwm				= Pwm.linear2log(pwm);
		String command	= "L" + myaddress + ","+ selector +"," + pwm;
	//	System.out.println("+addLed " +command);
		q.add( command, true );
	}

	public void setRgbw(Queue q1, int red, int green, int blue, int white) {
		red		= Pwm.linear2log(red);
		green	= Pwm.linear2log(green);	
		blue	= Pwm.linear2log(blue);
		white	= Pwm.linear2log(white);
		this.addLed(q1, "11", red);
		this.addLed(q1, "22", green);
		this.addLed(q1, "44", blue);
	//	this.setLed(q1, "88", white);
	}
	
	@Override
	public void setLed(Queue q, String selector, int pwm ) {
		pwm				= Pwm.linear2log(pwm);
		String command	= "B" + myaddress + ","+ selector +"," + pwm;
		q.add( command, true );
	}

	@Override
	public void setColor(Queue q, boolean top, int red, int green, int blue, int white) {
		red		= Pwm.linear2log(red);
		green	= Pwm.linear2log(green);	
		blue	= Pwm.linear2log(blue);
		white	= Pwm.linear2log(white);
		String command;
		if(top){
			 command	= "C";
		}else{
			 command	= "c";
		}
		command += String.format("%02x", myaddress ) 
				+ String.format("%02x", red )
				+ String.format("%02x", green )
				+ String.format("%02x", blue  )
				+ String.format("%02x", white );
		q.add( command, true );
	}

	@Override
	public String checkExists(Queue q) {
		if( myaddress > 0 ){
			return "n"+ this.myaddress;
		}else if( row > 0 ){
			return "N"+ this.row;
		}
		return "-checkExists";
	}

	@Override
	public void setAddress(int myaddress) {
		this.myaddress = myaddress;
		if(onchange!=null){
			onchange.run();
		}
	}

	@Override
	public int getAddress() {
		return myaddress;
	}

	@Override
	public void setRow(int myindex) {
		this.row = myindex;
		if(onchange!=null){
			onchange.run();
		}
	}

	@Override
	public int getRow() {
		return row;
	}

	public void setIndex(int index) {
		this.index = index;
		this.numInRow = 0;
		if(onchange!=null){
			onchange.run();
		}
	}
	
	
	@Override
	public void setNumInRow(int order) {
		this.numInRow = order;
		if(onchange!=null){
			onchange.run();
		}
	}

	@Override
	public int getNumInRow() {
		return numInRow;
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

	//public String uploadCode( Queue doAfter, String filePath, String comPort ) {
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
		q.add( this.getReset(), true );
	}
	@Override
	public void isp(Queue q) {
		q.add( this.getIsp(), "SISP" );
	}
	@Override
	public String getIsp() {
		if( index > 0 && index < 5 && numInRow == 0 ){
			return "P"+ this.row;
		}else if( canBeResetedBy != null ){
			return "p"+ canBeResetedBy.getAddress();
		}
		return "-getIsp";
	}
	public String getReset() {
		if( index > 0 ){
			return "RESET"+ this.index;
		}else if( canBeResetedBy != null ){
			return "RESET_NEXT" + canBeResetedBy.getAddress();
		}
		return "-getReset";
	}
	public static String verbose( int verbose ) {
		return Decoder.strRepeat(" -v", verbose);
	}
	/*
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
	}*/
/*
	public Queue progEndCommand() {
		Queue q = new Queue();
		// prog mode off, powiadom wszystkich
		for (I2C_Device v : I2C.lista){
		    if(v.getAddress() > 0 && v.getAddress() !=  LowHardware.I2C_ADR_MAINBOARD){
		    	q.add( I2C.send( LowHardware.I2C_ADR_MAINBOARD, Methods.METHOD_PROG_MODE_OFF ));
		    }
		}
		return q;
	}*/
	public void hasResetTo(int index, I2C_Device dev2 ) {
		dev2.isResetedBy( this );
		//canReset.add(dev2);
	}
	public void isResetedBy(I2C_Device i2c_device) {
		canBeResetedBy = i2c_device;
		if(onchange!=null){
			onchange.run();
		}
	}
	/*
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
	}*/

	public void onchange(Runnable runnable) {
		this.onchange = runnable;
	}
}
