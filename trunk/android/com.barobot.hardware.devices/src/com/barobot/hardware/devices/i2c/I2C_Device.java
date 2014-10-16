package com.barobot.hardware.devices.i2c;

import com.barobot.parser.Queue;

public interface I2C_Device {

	public abstract String setFuseBits(String comPort);
	public abstract String checkFuseBits(String comPort);

	//public abstract String uploadCode(Queue doAfter, String filePath, String comPort);
	public abstract String uploadCode( String filePath, String comPort);

	public abstract String erase( String filePath, String comPort);

	public abstract void reset(Queue q );
	public abstract void isp(Queue q );

	public abstract String getReset();
	public abstract String getIsp();

	public abstract void addLed(Queue q, String selector, int pwm);
	public abstract void setLed(Queue q, String selector, int pwm);
	public abstract void setColor(Queue q, boolean top, int red, int green, int blue, int white);

	public abstract String getHexFile();

	public abstract int getNumInRow();

	public abstract void setNumInRow(int order);

	public abstract int getRow();

	public abstract void setRow(int myindex);

	public abstract int getAddress();

	public abstract void setAddress(int myaddress);
	
	public abstract void isResetedBy(I2C_Device i2c_device);

	public void hasResetTo(int index, I2C_Device dev2 );

	public abstract String checkExists(Queue q);


	

}