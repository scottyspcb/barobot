package com.barobot.isp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class Wizard {

	static String avrDudePath	= "D:\\PROG\\arduino-1.0.5\\hardware\\tools\\avr\\bin\\avrdude";
	static String configPath	= "D:\\PROG\\arduino-1.0.5\\hardware\\tools\\avr\\etc\\avrdude.conf";

	static String carretHexPath	= "C:\\workspace\\Barobot\\arduino\\projects2\\barobot_carret\\build\\barobot_carret.hex";
	static String mbHexPath		= "C:\\workspace\\Barobot\\arduino\\projects2\\barobot_mainboard\\build\\barobot_carret.hex";
	static String upHexPath		= "C:\\Temp\\build4134267430172719603.tmp\\barobot_upanel.cpp.hex";
	public static int last_found_device = 0;


	public String uploadHex(Hardware hw, String cpu, String filePath ) {
		if( cpu.equals("atmega8")){	// todo
		
		}else if( cpu.equals("m328p")){	// todo
			String command = avrDudePath + " -C"+ configPath +" -v -v -D " +
			"-pm328p -cstk500v1 -P\\\\.\\"+hw.comPort+" -b" + hw.programmspeed + " " +
			"";
			return command;
		}
		return "";
	}

	public String setFuseBits(Hardware hw, String cpu) {
		//, int lfuse, int hfuse
		if( cpu.equals("atmega8")){
			
		}else if( cpu.equals("m328p")){
			String command = avrDudePath + " -C"+ configPath +" -v -v -D " +
			"-pm328p -cstk500v1 -P\\\\.\\"+hw.comPort+" -b" + hw.programmspeed + " " +
			"";
			return command;
		}
		return "";
	}

	public void findOrder(Hardware hw) {
		Upanel.list.add( new Upanel( 3, 0 ) );
		String command = "";
		String TimeStamp = new java.util.Date().toString();

		hw.connect();
		int current_index	= 0;
		Upanel current_dev	= Upanel.list.get(current_index);
		int device_found  = current_dev.resetAndReadI2c( hw );
		if( device_found > 0 ){		// pierwszy ma adres i2c
			System.out.println("Upanel " + current_index + " ma adres " + device_found);
			current_dev.hasAddress(device_found);
			find_next_to( hw, current_dev, current_index );
		}else{
			System.out.println("B£¥D Upanel " + (current_index) +" o adresie "+ current_dev.myaddress + " jest ostatni");
		}
		/*
		hw.send("RB");
		wait(1000);
		hw.send("I2C");
		wait(3000);
		hw.send("TEST");
*/	
		hw.close();
	}
	private void find_next_to( Hardware hw, Upanel current_dev, int next_index) {
		int device_found  = current_dev.resetNextAndReadI2c( hw );
		if( device_found > 0 ){		// cos powsta³o na koñcu
			int index = Upanel.findByI2c( device_found );
			Upanel u = null;
			if( index == -1 ){
				u = new Upanel( 0, device_found, current_dev );
				u.hesOrder( next_index+1);
				Upanel.list.add( u );
			}else{
				u = Upanel.list.get( index );
				u.hesOrder( next_index+1);
				u.canResetMe(current_dev);
			}
			System.out.println("Upanel " + (next_index+1) + " ma adres " + device_found);
			find_next_to( hw, u, next_index+1 );
		}else{
			System.out.println("Upanel " + (next_index) +" o adresie "+ current_dev.myaddress + " jest ostatni");
		}
	}

	public void preparePCB(Hardware hw) {
		String upanel_code = upHexPath;
		Upanel.list.add( new Upanel( 3, 0 ) );	
		String command = "";

		hw.connect();
		int current_index	= 0;
		Upanel current_dev	= Upanel.list.get(current_index);
		current_dev.isp(hw);
		wait(1000);

		command = current_dev.setFuseBits(hw);
		run(command, hw);
		System.out.println("po run");
		wait(2000);

		current_dev.isp(hw);
		command = current_dev.uploadCode(hw, upanel_code );
		run(command, hw);

		wait(5000);

		int device_found  = current_dev.resetAndReadI2c( hw );
		if( device_found > 0 ){		// pierwszy ma adres i2c
			System.out.println("Upanel " + current_index + " ma adres " + device_found);
			current_dev.hasAddress(device_found);
			prog_next_to( hw, current_dev, current_index+1, upanel_code );
		}else{
			System.out.println("B£¥D Upanel " + (current_index) +" o adresie "+ current_dev.myaddress + " jest ostatni");
		}
	//	hw.send("LEDS 13,0xfe,0");
	//	hw.send("LEDS 12,0xfe,0");
		hw.close();
	}

	private void prog_next_to(Hardware hw, Upanel current_dev, int next_index, String upanel_code) {
		Upanel next_device	= new Upanel( 0, 0, current_dev );
		current_dev.isp_next(hw);
		wait(1000);

		String command = next_device.setFuseBits(hw);
		run(command, hw);
		wait(2000);

		current_dev.isp_next(hw);
		command = next_device.uploadCode(hw, upanel_code );
		run(command, hw);

		wait(5000);
		int device_found  = current_dev.resetNextAndReadI2c( hw );
		if( device_found > 0 ){		// pierwszy ma adres i2c
			next_device.hasAddress(device_found);
			next_device.hesOrder( next_index+1);
			next_device.canResetMe(current_dev);
			System.out.println("Upanel " + next_index + " ma adres " + device_found);
			prog_next_to( hw, current_dev, next_index+1, upanel_code );
		}else{
			System.out.println("B£¥D Upanel " + (next_index) +" o adresie "+ current_dev.myaddress + " jest ostatni");
		}
	}

	public static void wait(int ms) {
		try {
			 Thread.sleep(ms);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}
	void run(String command, Hardware closeSerial ) {
		String line;
        Process p;
        if(closeSerial != null ){
        	closeSerial.close();
        }
		try {
			System.out.println("-----------------------------------------------");
			System.out.println("\t>>>Running " + command);
			p = Runtime.getRuntime().exec(command);
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));  
			
			System.out.println("\t>>>RESULT ");
			CopyThread ct = new CopyThread(p.getInputStream(), System.out);
			ct.start();
			
			CopyThread ce = new CopyThread(p.getErrorStream(), System.out);
			ce.start();	
			
			try {
				p.waitFor();
				 System.out.println("\t>>>RETURN FROM TASK");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		//	while ((line = input.readLine()) != null) {
	     //       System.out.println(line);
	     //   }
		//	System.out.println("\t>>>ERROR ");
		//	BufferedReader in = new BufferedReader(new InputStreamReader(p.getErrorStream()));
	     //   while ((line = in.readLine()) != null) {
	    //       System.out.println(line);
	    //    }
	        System.out.println("------------------------------------------------");
	        input.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		if(closeSerial != null ){
			closeSerial.connect();
	    }
	}
}

/**
 * 	
		
    //  run( "tasklist.exe" ); 
	//	resetDevice( hw, 0, 2 );	// carret
	//	command = "ping.exe localhost";
	//	run( command );	

command = avrDudePath + " -C"+ configPath +" -v -v -v -v -D "+
		"-patmega8 -cstk500v1 -P\\\\.\\"+hw.comPort+" -b" + hw.programmspeed + " ";
/
 */

/*
	uf0.prepareToSetFuseBits( hw );
	command = uf0.setFuseBits(hw);
	run( command, hw );
	hw.send("RB");
	wait(1000);
	hw.send("I2C");
	wait(3000);
	hw.send("TEST");
	wait(5000);
		uf0.reset( hw );
*/	