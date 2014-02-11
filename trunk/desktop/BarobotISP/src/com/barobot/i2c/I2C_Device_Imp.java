package com.barobot.i2c;

import com.barobot.isp.Hardware;
import com.barobot.isp.IspSettings;
import com.barobot.isp.Main;
import com.barobot.isp.Wizard;

public abstract class I2C_Device_Imp implements I2C_Device{
	protected int myaddress = 0;
	protected int myindex = 0;
	protected int order = -1;
	protected String cpuname = "";

	public I2C_Device_Imp() {
	}
	public void setLed(Hardware hw, String selector, int pwm) {
			hw.send("L " +this.getAddress() + ","+ selector +"," + pwm );
			synchronized (Main.main) {
				try {
					Main.main.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} 
	//		Wizard.wait(30);
			//hw.send("L 12,0xfe,0");
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

	public int resetAndReadI2c(Hardware hw) {
		int reset_tries = IspSettings.reset_tries;
		while( reset_tries-- > 0 ){
			this.reset( hw );
			int wait_tries = IspSettings.wait_tries;
			while( IspSettings.last_found_device == 0 && (wait_tries-- > 0 ) ){
				Wizard.wait(IspSettings.wait_time);
			}
			if( IspSettings.last_found_device > 0 ){
				break;
			}
			System.out.println("Reset try " + reset_tries );
		}
		int ret = IspSettings.last_found_device;
		IspSettings.last_found_device = 0;
		return ret;
	}
	public String checkFuseBits(Hardware hw) {
		String command = IspSettings.avrDudePath + " -C"+ IspSettings.configPath +" "+ IspSettings.verbose()+ " " +
		"-p"+ this.cpuname +" -cstk500v1 -P\\\\.\\"+hw.comPort+" -b" + IspSettings.programmspeed + " " +
		" -U lock:r:-:h";
		if(IspSettings.safeMode){
			command = command + " -n";
		}
		return command;
	}
}