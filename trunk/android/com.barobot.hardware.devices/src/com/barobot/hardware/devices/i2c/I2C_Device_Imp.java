package com.barobot.hardware.devices.i2c;

import com.barobot.common.IspSettings;
import com.barobot.common.constant.Pwm;
import com.barobot.parser.Queue;
import com.barobot.parser.utils.Decoder;

public abstract class I2C_Device_Imp{
	public static int level = 10000;
	public static int MAX_LEVEL = 10000;

	protected int myaddress = 0;			// 1-120
	protected int row = -1;					// FRONT, BACK
	protected int numInRow = -1;			// 0-5
	protected int resetIndex	= -1;		// 0,1,2,3
	protected String cpuname = "";
	protected String protocol = "stk500v1";
	protected int bspeed = IspSettings.programmspeed;
	protected String lfuse = "";
	protected String hfuse = "";
	protected String lock = "";
	protected String efuse = "";
	protected I2C_Device_Imp canBeResetedBy;
	protected Runnable onchange = null;

	public I2C_Device_Imp() {
	}

	public void addLed(Queue q, String selector, int pwm ) {
	//	float ratio		= ((float)I2C_Device_Imp.level)/I2C_Device_Imp.MAX_LEVEL;
		pwm				= Pwm.linear2log(pwm, 1 );
		String command	= "L" + myaddress + ","+ selector +"," + pwm;
	//	System.out.println("+addLed " +command);
		q.add( command, true );
	}

	public void setLed(Queue q, String selector, int pwm ) {
		float ratio		= ((float)I2C_Device_Imp.level)/I2C_Device_Imp.MAX_LEVEL;
		pwm				= Pwm.linear2log(pwm, ratio);
		String command	= "B" + myaddress + ","+ selector +"," + pwm;
		q.add( command, true );

	}


	public void setColor(Queue q, boolean top, int red, int green, int blue, int white) {
		float ratio		= ((float)I2C_Device_Imp.level)/I2C_Device_Imp.MAX_LEVEL;
		red		= Pwm.linear2log(red, ratio);
		green	= Pwm.linear2log(green, ratio);	
		blue	= Pwm.linear2log(blue, ratio);
		white	= Pwm.linear2log(white, ratio);
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

	public void setAddress(int myaddress) {
		this.myaddress = myaddress;
		if(onchange!=null){
			onchange.run();
		}
	}


	public int getAddress() {
		return myaddress;
	}


	public void setRow(int myindex) {
		this.row = myindex;
		if(onchange!=null){
			onchange.run();
		}
	}


	public int getRow() {
		return row;
	}

	public void setIndex(int index) {
		this.resetIndex = index;
		this.numInRow = 0;
		if(onchange!=null){
			onchange.run();
		}
	}
	
	

	public void setNumInRow(int order) {
		this.numInRow = order;
		if(onchange!=null){
			onchange.run();
		}
	}


	public int getNumInRow() {
		return numInRow;
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


	public String uploadCode( String filePath, String comPort ) {
		String command = IspSettings.avrDudePath + " -C"+ IspSettings.configPath +" "+ I2C_Device_Imp.verbose( IspSettings.verbose )+ " " +
		"-p"+ this.cpuname +" -c"+this.protocol+" -P\\\\.\\"+comPort+" -b" + this.bspeed + " " +
		"-Uflash:w:"+filePath+":i";
		if(IspSettings.safeMode){
			command = command + " -n";
		}
		return command;
	}


	public void reset(Queue q ) {
		q.add( this.getReset(), true );
	}

	public void isp(Queue q) {
		q.add( this.getIsp(), "SISP" );
	}

	public String getIsp() {
		if( resetIndex > 0 && resetIndex < 5 && numInRow == 0 ){
			return "P"+ this.row;
		}else if( canBeResetedBy != null ){
			return "p"+ canBeResetedBy.getAddress();
		}
		return "-getIsp";
	}
	public String getReset() {
		if( resetIndex > 0 ){
			return "RESET"+ this.resetIndex;
		}else if( canBeResetedBy != null ){
			return "RESET_NEXT" + canBeResetedBy.getAddress();
		}
		return "-getReset";
	}
	public static String verbose( int verbose ) {
		return Decoder.strRepeat(" -v", verbose);
	}
	
	public void hasResetTo(int index, I2C_Device_Imp dev2 ) {
		dev2.isResetedBy( this );
		//canReset.add(dev2);
	}
	
	public void isResetedBy(I2C_Device_Imp i2c_device) {
		canBeResetedBy = i2c_device;
		if(onchange!=null){
			onchange.run();
		}
	}

	public void onchange(Runnable runnable) {
		this.onchange = runnable;
	}
}
