package com.barobot.isp;

import java.util.ArrayList;
import java.util.List;

public class Upanel {
	static int speed				= 9200;
	public int myaddress			= 0;
	public int myindex				= 0;
	public Upanel can_reset_me_dev	= null;
	public int order;
	static List<Upanel> list		= new ArrayList<Upanel>();
	public static int findByI2c(int device_add) {
		for (Upanel s : list){
			if(s.myaddress == device_add ){
				return Upanel.list.indexOf(s);
			}
		}
		return -1;
	}
	
	
	
	public Upanel(int index, int address ){
		this.myaddress		= address;
		this.myindex		= index;
	}
	public Upanel(int index, int address, Upanel prev_dev){
		this.myaddress			= address;
		this.myindex			= index;
		this.can_reset_me_dev	= prev_dev;	
	}
	public void canResetMe( Upanel current_dev){
		this.can_reset_me_dev = current_dev;
	}
	public String setFuseBits(  Hardware hw){
		String command = Wizard.avrDudePath + " -C"+ Wizard.configPath +" -v -v -D " +
		"-patmega8 -cstk500v1 -P\\\\.\\"+hw.comPort+" -b" + hw.programmspeed + " " +
		"-Ulock:w:0x3F:m -Uhfuse:w:0xC7:m -Ulfuse:w:0xA4:m";
		return command;
	}
	public String uploadCode(Hardware hw, String filePath){
		String command = Wizard.avrDudePath + " -C"+ Wizard.configPath +" -v -v -D " +
		"-patmega8 -D -cstk500v1 -P\\\\.\\"+hw.comPort+" -b" + hw.programmspeed + " " +
		"-Uflash:w:"+filePath+":i";
		return command;
	}
	public void hasAddress(int last_found_device) {
		myaddress = last_found_device;
	}
	public void reset(Hardware hw) {
		if(myindex > 0 ){
			hw.send("RESET "+ myindex);
		}else if( can_reset_me_dev == null ){
			hw.send("RESET_NEXT "+ can_reset_me_dev.myaddress );
		}
	}
	public void reset_next(Hardware hw) {
		if( myaddress > 0 ){
			hw.send("RESET_NEXT "+ myaddress );
		}
	}
	public void isp(Hardware hw) {		// mnie
		if(myindex > 0 ){
			hw.send("PROG "+ myindex );
		}else if( can_reset_me_dev == null ){
			hw.send("PROG_NEXT "+ can_reset_me_dev.myaddress );
		}
	}
	public void isp_next(Hardware hw) {	// pod³¹czony do mnie
		hw.send("PROG_NEXT "+ myaddress );
	}

	public void hesOrder(int i) {
		this.order = i;
	}
	public int resetAndReadI2c(Hardware hw) {
		int reset_tries = 3;
		while( reset_tries-- > 0 ){
			this.reset( hw );
			int tries = 50;	
			while( Wizard.last_found_device == 0 && (tries-- > 0 ) ){
				Wizard.wait(100);
			}
			if( Wizard.last_found_device > 0 ){
				break;
			}
			System.out.println("Reset try " + reset_tries );
		}
		int ret = Wizard.last_found_device;
		Wizard.last_found_device = 0;	// resetuj
		return ret;
	}
	
	
	public int resetNextAndReadI2c(Hardware hw) {

		int reset_tries = 3;
		while( reset_tries-- > 0 ){
			this.reset_next( hw );
			int tries = 50;	
			while( Wizard.last_found_device == 0 && (tries-- > 0 ) ){
				Wizard.wait(100);
			}
			if( Wizard.last_found_device > 0 ){
				break;
			}
			System.out.println("Reset try " + reset_tries );
		}
		int ret = Wizard.last_found_device;
		Wizard.last_found_device = 0;	// resetuj
		return ret;
	}

	

}
