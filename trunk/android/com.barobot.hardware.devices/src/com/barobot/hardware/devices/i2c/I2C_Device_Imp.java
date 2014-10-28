package com.barobot.hardware.devices.i2c;

import java.io.File;

import com.barobot.common.Initiator;
import com.barobot.common.IspSettings;
import com.barobot.common.constant.Pwm;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.parser.Queue;
import com.barobot.parser.utils.Decoder;

public abstract class I2C_Device_Imp implements I2C_Device{
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
	protected I2C_Device canBeResetedBy;
	protected Runnable onchange = null;

	protected int ledOrderType =1;		// RED GREEN BLUE
	//protected int ledOrderType =2;		// RED BLUE GREEN
	
	public I2C_Device_Imp() {
	}

	@Override
	public void addLed(Queue q, String selector, int pwm ) {
		float ratio		= ((float)I2C_Device_Imp.level)/I2C_Device_Imp.MAX_LEVEL;
		pwm				= Pwm.linear2log(pwm, 1 );
		if(ledOrderType == 1){
		}else{
			selector = selector.replaceAll("2$", "a");
			selector = selector.replaceAll("4$", "2");
			selector = selector.replaceAll("a$", "4");
		}
		String command	= "L" + myaddress + ","+ selector +"," + pwm;
	//	System.out.println("+addLed " +command);
		q.add( command, true );
	}
	
	@Override
	public void setLed(Queue q, String selector, int pwm ) {
		float ratio		= ((float)I2C_Device_Imp.level)/I2C_Device_Imp.MAX_LEVEL;
		pwm				= Pwm.linear2log(pwm, ratio);
		if(ledOrderType == 1){
			String command	= "B" + myaddress + ","+ selector +"," + pwm;
			q.add( command, true );
		}else{
			selector = selector.replaceAll("2$", "a");
			selector = selector.replaceAll("4$", "2");
			selector = selector.replaceAll("a$", "4");
			String command	= "B" + myaddress + ","+ selector +"," + pwm;
			q.add( command, true );
		}
	}

	@Override
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
		if(ledOrderType == 1 || !top ){
			command += String.format("%02x", myaddress ) 
					+ String.format("%02x", red )
					+ String.format("%02x", green )
					+ String.format("%02x", blue  )
					+ String.format("%02x", white );
		}else{
			command += String.format("%02x", myaddress ) 
					+ String.format("%02x", red )
					+ String.format("%02x", blue )
					+ String.format("%02x", green  )
					+ String.format("%02x", white );
		}
		q.add( command, true );
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
		this.resetIndex = index;
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
		if(BarobotConnector.pureCrystal && order == 5 && row == Upanel.BACK ){
			ledOrderType = 2;
			Initiator.logger.i("setNumInRow ledOrderType", "order" + order + ", row= " +row );
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
/*
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
*/
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

	public void onchange(Runnable runnable) {
		this.onchange = runnable;
	}
}
