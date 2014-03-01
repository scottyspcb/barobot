package com.barobot.i2c;

import com.barobot.isp.Hardware;
import com.barobot.isp.IspSettings;
import com.barobot.isp.Wizard;
import com.barobot.parser.Parser;

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
		return "";
	}
	public void reset(Hardware hw) {
		hw.send("RESET"+ this.myindex);
	}
	public void isp(Hardware hw) {
		hw.send("RESET"+ getIndex() );
	}
	public String getHexFile() {
		return IspSettings.mbHexPath;
	}
	public int readHasNext(Hardware hw, int index) {
		int reset_tries = IspSettings.reset_tries;
		while( reset_tries-- > 0 ){
			hw.send("H" + index );
			int wait_tries = IspSettings.wait_tries;
			while( Parser.last_has_next == -1 && (wait_tries-- > 0 ) ){
				Wizard.wait(IspSettings.wait_time);
			}
			if( Parser.last_has_next > -1 ){
				break;
			}
			System.out.println("Check try " + IspSettings.reset_tries );
		}
		int ret = Parser.last_has_next;
		Parser.last_has_next = -1;	// resetuj
		return ret;
	}
	public void moveX(int max) {
		
		
	}
}
