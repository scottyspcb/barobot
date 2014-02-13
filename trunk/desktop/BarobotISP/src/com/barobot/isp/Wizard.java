package com.barobot.isp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import com.barobot.i2c.Carret;
import com.barobot.i2c.I2C_Device;
import com.barobot.i2c.MainBoard;
import com.barobot.i2c.Upanel;
import com.barobot.isp.parser.CopyStream;

public class Wizard {

	public void findOrder(Hardware hw) {
		String TimeStamp = new java.util.Date().toString();

		hw.connect();
		int current_index	= 0;
		Upanel current_dev	= new Upanel( 3, 0 );
		int device_found	= current_dev.resetAndReadI2c( hw );
		if( device_found > 0 ){		// pierwszy ma adres com.barobot.i2c
			Upanel.list.add( current_dev );	
			System.out.println("Upanel " + current_index + " ma adres " + device_found);
			current_dev.setAddress(device_found);
			find_next_to( hw, current_dev, current_index );
		}else{
			System.out.println("B£¥D Upanel " + (current_index) +" o adresie "+ current_dev.getAddress() + " jest ostatni");
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
				u.setOrder( next_index+1);
				Upanel.list.add( u );
			}else{
				u = Upanel.list.get( index );
				u.setOrder( next_index+1);
				u.canResetMe(current_dev);
			}
			System.out.println("Upanel " + (next_index+1) + " ma adres " + device_found);
			find_next_to( hw, u, next_index+1 );
		}else{
			System.out.println("Upanel " + (next_index) +" o adresie "+ current_dev.getAddress() + " jest ostatni");
		}
	}

	public void prepareUpanel(Hardware hw) {
		Upanel.list.clear();
		Upanel.list.add( new Upanel( 3, 0 ) );	
		String command = "";

		hw.connect();
		int current_index	= 0;
		Upanel current_dev	= Upanel.list.get(current_index);
		String upanel_code = current_dev.getHexFile();

		if( IspSettings.setFuseBits){
			current_dev.isp(hw);
			command = current_dev.setFuseBits(hw);
			run(command, hw);
			wait(2000);
		}
		if(IspSettings.setHex){
			current_dev.isp(hw);
			command = current_dev.uploadCode(hw, upanel_code );
			run(command, hw);
			wait(2000);
		}

		int device_found  = current_dev.resetAndReadI2c( hw );
		if( device_found > 0 ){		// pierwszy ma adres com.barobot.i2c
			System.out.println("+Upanel " + current_index + " ma adres " + device_found);
			current_dev.setAddress(device_found);
			prog_next_to( hw, current_dev, current_index+1, upanel_code );
		}else{
			System.out.println("B£¥D Upanel " + (current_index) +" o adresie "+ current_dev.getAddress() + " jest ostatni");
		}
		hw.close();
	}

	private void prog_next_to(Hardware hw, Upanel current_dev, int next_index, String upanel_code ) {
		String command = "";
		Upanel next_device	= new Upanel( 0, 0, current_dev );
		
		if( IspSettings.setFuseBits){
			current_dev.isp_next(hw);
			command = next_device.setFuseBits(hw);
			run(command, hw);
			wait(1000);
		}
		if(IspSettings.setHex){
			current_dev.isp_next(hw);
			command = next_device.uploadCode(hw, upanel_code );
			run(command, hw);
			wait(2000);
		}

		int device_found  = current_dev.resetNextAndReadI2c( hw );
		wait(2000);
		if( device_found > 0 ){		// pierwszy ma adres com.barobot.i2c
			next_device.setAddress(device_found);
			next_device.setOrder( next_index+1);
			next_device.canResetMe(current_dev);
			Upanel.list.add( next_device );
			System.out.println("++Upanel " + next_index + " ma adres " + device_found);
			prog_next_to( hw, next_device, next_index+1, upanel_code );
		}else{
			System.out.println("++Upanel " + (next_index-1) +" o adresie "+ current_dev.getAddress() + " jest ostatni");
		}
	}

	public static void wait(int ms) {
		try {
			 Thread.sleep(ms);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}
	

	public void clearUpanel(Hardware hw) {
		while(Upanel.list.size() > 0 ){
			boolean found = false;
			for (Upanel u : Upanel.list){
				if(u.have_reset_to == null ){
					 System.out.println("Rozpoczynam id " + u.getAddress() );
					 Upanel.list.remove(u);
					 u.can_reset_me_dev.have_reset_to = null;
					 found = true;
					 break;
				}
			}
			if(!found){
				System.out.println("Brak wêz³ów koñcowych" );
				break;
			}
		}
		System.out.println("Lista pusta" );
	}

	public void mrygaj(Hardware hw) {
		if(Upanel.list.size() == 0 ){
			return;
		}

		hw.connect();
		int repeat = 100;

		System.out.println("Start" );
		
		int time = 30;
		while (repeat-- > 0){
			for (I2C_Device u : Upanel.list){
				u.setLed( hw, "0x0e", 255 );	// zgas
				u.setLed( hw, "0xf1", 0 );		// zgas
				u.setLed( hw, "0x01", 255 );
			}
			wait(time);	
			
			for (I2C_Device u : Upanel.list){
				u.setLed( hw, "0x0e", 255 );	// zgas
				u.setLed( hw, "0xf1", 0 );		// zgas

				u.setLed( hw, "0x02", 0 );
			}
			wait(time);	
			
	
			for (I2C_Device u : Upanel.list){
				u.setLed( hw, "0x0e", 255 );	// zgas
				u.setLed( hw, "0xf1", 0 );		// zgas

				u.setLed( hw, "0x04", 0 );
			}
			wait(time);		
			
			
			for (I2C_Device u : Upanel.list){
				u.setLed( hw, "0x0e", 255 );	// zgas
				u.setLed( hw, "0xf1", 0 );		// zgas

				u.setLed( hw, "0x08", 0 );
			}
			wait(time);	
			
			
			
			for (I2C_Device u : Upanel.list){
				u.setLed( hw, "0x0e", 255 );	// zgas
				u.setLed( hw, "0xf1", 0 );		// zgas

				u.setLed( hw, "0x10", 255 );
			}
			wait(time);	
			

			for (I2C_Device u : Upanel.list){
				u.setLed( hw, "0x0e", 255 );	// zgas
				u.setLed( hw, "0xf1", 0 );		// zgas

				u.setLed( hw, "0x20", 255 );
			}
			wait(time);	
			
			for (I2C_Device u : Upanel.list){
				u.setLed( hw, "0x0e", 255 );	// zgas
				u.setLed( hw, "0xf1", 0 );		// zgas

				u.setLed( hw, "0x40", 255 );
			}
			wait(time);
			
			
			for (I2C_Device u : Upanel.list){
				u.setLed( hw, "0x0e", 255 );	// zgas
				u.setLed( hw, "0xf1", 0 );		// zgas

				u.setLed( hw, "0x80", 255 );
			}
			wait(time);
		}
		hw.close();
	}

	public void mrygaj_po_butelkach(Hardware hw) {
		if(Upanel.list.size() == 0 ){
			return;
		}
		hw.connect();

		int time = 900;
		for (I2C_Device u2 : Upanel.list){
			u2.setLed( hw, "0x0e", 255 );	// zgas
			u2.setLed( hw, "0xf1", 0 );		// zgas
		}

		for (I2C_Device u : Upanel.list){

			wait(time);

				u.setLed( hw, "0x0e", 0 );
				u.setLed( hw, "0xf1", 255 );

			wait(time);
			
		
				u.setLed( hw, "0x0e", 255 );	// zgas
				u.setLed( hw, "0xf1", 0 );		// zgas
				u.setLed( hw, "0x01", 255 );
			
			wait(time);	
			
	
				u.setLed( hw, "0x0e", 255 );	// zgas
				u.setLed( hw, "0xf1", 0 );		// zgas
				u.setLed( hw, "0x02", 0 );
			
			wait(time);	
			

				u.setLed( hw, "0x0e", 255 );	// zgas
				u.setLed( hw, "0xf1", 0 );		// zgas
				u.setLed( hw, "0x04", 0 );
			
			wait(time);		
			
			
			
				u.setLed( hw, "0x0e", 255 );	// zgas
				u.setLed( hw, "0xf1", 0 );		// zgas
				u.setLed( hw, "0x08", 0 );
			
			wait(time);	
			

				u.setLed( hw, "0x0e", 255 );	// zgas
				u.setLed( hw, "0xf1", 0 );		// zgas
				u.setLed( hw, "0x10", 255 );
	
			wait(time);	
			

			
				u.setLed( hw, "0x0e", 255 );	// zgas
				u.setLed( hw, "0xf1", 0 );		// zgas
				u.setLed( hw, "0x20", 255 );
			
			wait(time);	
			
			
				u.setLed( hw, "0x0e", 255 );	// zgas
				u.setLed( hw, "0xf1", 0 );		// zgas
				u.setLed( hw, "0x40", 255 );
			
			wait(time);
			

				u.setLed( hw, "0x0e", 255 );	// zgas
				u.setLed( hw, "0xf1", 0 );		// zgas
				u.setLed( hw, "0x80", 255 );
			
			wait(time);		

			u.setLed( hw, "0x0e", 255 );	// zgas
			u.setLed( hw, "0xf1", 0 );		// zgas

		}
		hw.close();
	}

	public void mrygaj_grb(Hardware hw) {
		if(Upanel.list.size() == 0 ){
			return;
		}
		hw.connect();
		int repeat = 6;

		int time = 20;
		while (repeat-- > 0){

			for (I2C_Device u : Upanel.list){
				u.setLed( hw, "0x0e", 255 );	// zgas
				u.setLed( hw, "0xf1", 0 );		// zgas
			}
			wait(time);	
			
			for (I2C_Device u : Upanel.list){
				u.setLed( hw, "0x0e", 255 );	// zgas
				u.setLed( hw, "0xf1", 0 );		// zgas
				
			//	u.setLed( hw, "0x02", 0 );		// bottom green
			//	u.setLed( hw, "0x08", 0 );		// bottom blue
			//	u.setLed( hw, "0x04", 0 );		// bottom red 

				u.setLed( hw, "0x0E", 0 );	// top RGB

			//	u.setLed( hw, "0x10", 255 );	// top green
			//	u.setLed( hw, "0x20", 255 );	// top blue
			//	u.setLed( hw, "0x40", 255 );	// top red
			}
			wait(time);	
			
			
			for (I2C_Device u : Upanel.list){
				u.setLed( hw, "0x0e", 255 );	// zgas
				u.setLed( hw, "0xf1", 0 );		// zgas

				u.setLed( hw, "0x7E", 255 );	// boottm RGB

			}
			wait(time);			
		}
		wait(2000);
		hw.close();
	}

	public void test(Hardware hw) {
		hw.connect();
		hw.send("I2C");
		wait(1000);
		hw.send("TEST");
		wait(2000);
		hw.close();
	}

	public void prepareCarret(Hardware hw) {
		String command = "";
		hw.connect();
		Carret current_dev	= new Carret();
		String carret_code = current_dev.getHexFile();

		if( IspSettings.setFuseBits){
			current_dev.isp(hw);
			command = current_dev.setFuseBits(hw);
			run(command, hw);
			wait(2000);
		}

		if(IspSettings.setHex){
			current_dev.isp(hw);
			command = current_dev.uploadCode(hw, carret_code );
			run(command, hw);
			wait(2000);
		}
		int device_found  = current_dev.resetAndReadI2c( hw );
		if( device_found > 0 ){		// pierwszy ma adres com.barobot.i2c
			System.out.println("+Carret  ma adres " + device_found);
		}else{
			System.out.println("B£¥D Carret o nie zg³asza siê");
		}
		hw.close();
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
			CopyStream ct = new CopyStream(p.getInputStream(), System.out);
			ct.start();
			
			CopyStream ce = new CopyStream(p.getErrorStream(), System.out);
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
	public void checkCarret(Hardware hw) {
		String command = "";
		hw.connect();
		//I2C_Device current_dev	= new Upanel( 3, 0 );
		I2C_Device current_dev	= new Carret();
		current_dev.isp(hw);
		command = current_dev.checkFuseBits(hw);
		run(command, hw);
		hw.close();
	}

	public void prepareMB(Hardware hw) {
		String command = "";
		hw.connect();
		I2C_Device current_dev	= new MainBoard();
		String upanel_code = current_dev.getHexFile();
		if(IspSettings.setHex){	
			current_dev.isp(hw);	// mam 2 sek na wystartwanie
			command = current_dev.uploadCode(hw, upanel_code);
			run(command, hw);
			wait(1000);
		}
		wait(5000);		// wait for arduino bootloader
		hw.close();
	}
/*
	public void prepareSlaveMB(Hardware hw) {	// zaprogramuj p³ytkê g³ówn¹ pod³¹czon¹ jako SLAVE
		String command = "";
		hw.connect();
		I2C_Device current_dev	= new MainBoard();
		if( IspSettings.setFuseBits){
			current_dev.isp(hw);	// mam 2 sek na wystartwanie
			command = current_dev.setFuseBits(hw);
			run(command, hw);
			wait(1000);
		}
		if(IspSettings.setHex){	
			current_dev.isp(hw);	// mam 2 sek na wystartwanie
			command = current_dev.setFuseBits(hw);
			run(command, hw);
			wait(1000);
		}
		hw.close();
	}*/
}

/**
 * 			
    //  run( "tasklist.exe" ); 
	//	resetDevice( hw, 0, 2 );	// carret
	//	command = "ping.exe localhost";
	//	run( command );	

/

	hw.send("RB");
	wait(1000);
	hw.send("I2C");
	wait(3000);
	hw.send("TEST");
	wait(5000);
	uf0.reset( hw );
*/	