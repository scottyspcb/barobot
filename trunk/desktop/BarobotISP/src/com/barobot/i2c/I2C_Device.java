package com.barobot.i2c;

import com.barobot.isp.Hardware;

public interface I2C_Device {

	public abstract String setFuseBits(Hardware hw);
	public abstract String checkFuseBits(Hardware hw);
	
	public abstract String uploadCode(Hardware hw, String filePath);

	public abstract String erase(Hardware hw, String filePath);

	public abstract void reset(Hardware hw);

	public abstract void isp(Hardware hw);

	public abstract int resetAndReadI2c(Hardware hw);

	public abstract void setLed(Hardware hw, String selector, int pwm);

	public abstract String getHexFile();

	public abstract int getOrder();

	public abstract void setOrder(int order);

	public abstract int getIndex();

	public abstract void setIndex(int myindex);

	public abstract int getAddress();

	public abstract void setAddress(int myaddress);
}