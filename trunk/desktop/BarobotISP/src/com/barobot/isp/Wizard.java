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

	public void test(Hardware hw, int index ) {
		hw.connect();
		try {
			 Thread.sleep(1000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		hw.close();
	}
	
	public void findOrder(Hardware hw, int index ) {
	//	String TimeStamp = new java.util.Date().toString();
		hw.connect();
		int current_index		= 0;
		MainBoard mb			= new MainBoard();
		Upanel current_dev		= new Upanel( index, 0 );
		current_dev.setOrder( 0 );
		int has_next 			= mb.readHasNext( hw, index );
		System.out.println("has_next " + has_next );
		if(has_next>0){
			int device_found	= current_dev.resetAndReadI2c( hw );
			if( device_found > 0 ){		// pierwszy ma adres com.barobot.i2c
				Upanel.list.add( current_dev );	
				System.out.println("Upanel " + current_index + " ma adres " + device_found);
				current_dev.setAddress(device_found);
				current_dev.setLed( hw, "22", 50 );
				find_next_to( hw, current_dev, current_index );
			}else{
				System.out.println("B£¥D Upanel " + (current_index) +" o adresie "+ current_dev.getAddress() + " jest ostatni");
			}
		}
		for (I2C_Device u : Upanel.list){
			u.setLed( hw, "ff", 00 );
		}
		System.out.println("upaneli: " + Upanel.list.size());
		for (I2C_Device u : Upanel.list){
			System.out.println("Upanel numer "+ u.getOrder() + ", adres: "+ u.getAddress() +", index "+ u.getIndex());
		}
		hw.close();
	}
	private void find_next_to( Hardware hw, Upanel current_dev, int next_index) {
		int has_next  = current_dev.readHasNext( hw );
		System.out.println("has_next " + has_next );
		if(has_next>0){
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
				u.setLed( hw, "22", 50 );
				System.out.println("Upanel " + (next_index+1) + " ma adres " + device_found);
				find_next_to( hw, u, next_index+1 );
			}else{
				System.out.println("Error. Upanel " + (next_index) +" o adresie "+ current_dev.getAddress() + " jest ostatni ale coœ jest po nim");
			}
		}else{
			System.out.println("Upanel " + (next_index) +" o adresie "+ current_dev.getAddress() + " jest ostatni");
		}
	}
	public void showOrder() {
		for (I2C_Device u2 : Upanel.list){
			System.out.println("+Upanel" + u2.getIndex() + " pod numerem " + u2.getOrder() + "  ma adres " + u2.getAddress() );
		}
	}
	public void prepareUpanel(Hardware hw, int index ) {
		Upanel.list.clear();
		Upanel.list.add( new Upanel( index, 0 ) );	
		String command = "";

		hw.connect();
		int current_index	= 0;
		Upanel current_dev	= Upanel.list.get(current_index);
		String upanel_code = current_dev.getHexFile();
		current_dev.setOrder( index );

		if( IspSettings.setFuseBits){
			current_dev.isp(hw);
			wait(2000);
			command = current_dev.setFuseBits(hw);
			run(command, hw);
			wait(1000);
		}
		if(IspSettings.setHex){
			current_dev.isp(hw);
			wait(2000);
			command = current_dev.uploadCode(hw, upanel_code );
			run(command, hw);
			wait(1000);
		}

		int device_found  = current_dev.resetAndReadI2c( hw );
		if( device_found > 0 ){		// pierwszy ma adres com.barobot.i2c
			System.out.println("+Upanel " + current_index + " ma adres " + device_found);
			wait(2000);
			current_dev.setAddress(device_found);
			current_dev.setLed( hw, "22", 255 );	
			int has_next  = current_dev.readHasNext( hw );
			System.out.println("has_next " + has_next );
			if(has_next>0){
				prog_next_to( hw, current_dev, current_index+1, upanel_code );	
			}
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
			next_device.setLed( hw, "22", 255 );

			int has_next  = next_device.readHasNext( hw );
			System.out.println("has_next " + has_next );
			if(has_next>0){
				prog_next_to( hw, next_device, next_index+1, upanel_code );
			}
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
		int repeat = 10;

		System.out.println("Start" );
		
		int time = 30;
		while (repeat-- > 0){
			for (I2C_Device u : Upanel.list){
				u.setLed( hw, "ff", 0 );	// zgas
			}
			wait(time);	

			for (I2C_Device u : Upanel.list){
				u.setLed( hw, "ff", 0 );	// zgas
				u.setLed( hw, "01", 255 );
			}
			for (I2C_Device u : Upanel.list){
				u.setLed( hw, "ff", 0 );	// zgas
				u.setLed( hw, "02", 255 );
			}
			wait(time);	
			
	
			for (I2C_Device u : Upanel.list){
				u.setLed( hw, "ff", 0 );	// zgas
				u.setLed( hw, "04", 255 );
			}
			wait(time);		
			
			
			for (I2C_Device u : Upanel.list){
				u.setLed( hw, "ff", 0 );	// zgas
				u.setLed( hw, "08", 255 );
			}
			wait(time);	
			
			
			
			for (I2C_Device u : Upanel.list){
				u.setLed( hw, "ff", 0 );	// zgas
				u.setLed( hw, "10", 255 );
			}
			wait(time);	
			

			for (I2C_Device u : Upanel.list){
				u.setLed( hw, "ff", 0 );	// zgas
				u.setLed( hw, "20", 255 );
			}
			wait(time);	
			
			for (I2C_Device u : Upanel.list){
				u.setLed( hw, "ff", 0 );	// zgas
				u.setLed( hw, "40", 255 );
			}
			wait(time);
			
			
			for (I2C_Device u : Upanel.list){
				u.setLed( hw, "ff", 0 );	// zgas
				u.setLed( hw, "80", 255 );
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

		int time = 200;
		for (I2C_Device u2 : Upanel.list){
			u2.setLed( hw, "ff", 0 );	// zgas
		}

		for (I2C_Device u : Upanel.list){

			wait(time);

			u.setLed( hw, "ff", 0 );	// zgas

			wait(time);
			
		
			u.setLed( hw, "ff", 0 );	// zgas
				u.setLed( hw, "01", 255 );
			
			wait(time);	
			
	
			u.setLed( hw, "ff", 0 );	// zgas
				u.setLed( hw, "02", 255 );
			
			wait(time);	
			

			u.setLed( hw, "ff", 0 );	// zgas
				u.setLed( hw, "04", 255 );
			
			wait(time);		
			
			
			
			u.setLed( hw, "ff", 0 );	// zgas
				u.setLed( hw, "08", 255 );
			
			wait(time);	
			
			u.setLed( hw, "ff", 0 );	// zgas
				u.setLed( hw, "10", 255 );
	
			wait(time);	
			

			
			u.setLed( hw, "ff", 0 );	// zgas
				u.setLed( hw, "20", 255 );
			
			wait(time);	
			
			
			u.setLed( hw, "ff", 0 );	// zgas
				u.setLed( hw, "40", 255 );
			
			wait(time);
			

			u.setLed( hw, "ff", 0 );	// zgas
				u.setLed( hw, "80", 255 );
			
			wait(time);		

			u.setLed( hw, "ff", 0 );	// zgas

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
				u.setLed( hw, "ff", 0 );	// zgas
			}
			wait(time);	
			
			for (I2C_Device u : Upanel.list){
				u.setLed( hw, "ff", 0 );	// zgas

			//	u.setLed( hw, "02", 0 );		// bottom green
			//	u.setLed( hw, "08", 0 );		// bottom blue
			//	u.setLed( hw, "04", 0 );		// bottom red 

				u.setLed( hw, "07", 255 );	// top RGB

			//	u.setLed( hw, "10", 255 );	// top green
			//	u.setLed( hw, "20", 255 );	// top blue
			//	u.setLed( hw, "40", 255 );	// top red
			}
			wait(time);	
			
			
			for (I2C_Device u : Upanel.list){
				u.setLed( hw, "ff", 0 );	// zgas
				u.setLed( hw, "70", 255 );	// boottm RGB

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
		/*
		int device_found  = current_dev.resetAndReadI2c( hw );
		if( device_found > 0 ){		// pierwszy ma adres com.barobot.i2c
			System.out.println("+Carret  ma adres " + device_found);
		}else{
			System.out.println("B£¥D Carret o nie zg³asza siê");
		}*/
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
	public void prepare1Upanel(Hardware hw, int index ) {
		String command = "";
		hw.connect();
		Upanel current_dev	= new Upanel( index, 0 );
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
		hw.close();
		System.out.println("koniec prepare1Upanel");
	}
	public void illumination1(Hardware hw) {
		int repeat = 1;
		while(repeat-->0){
			this.mrygaj( hw );
		//	this.mrygaj_grb( hw );
		//	this.mrygaj_po_butelkach( hw );
		}
		System.out.println("koniec illumination1");
	}
	public void ilumination2(Hardware hw) {
		System.out.println("upaneli: " + Upanel.list.size());
		hw.connect();

		for (int b = 0;b<4;b++){
			int i=0;
			for (;i<245;i+=5){
				for (I2C_Device u2 : Upanel.list){
					u2.setLed( hw, "ff", i );
				}
			}
			for (;i>=0;i-=5){
				for (I2C_Device u2 : Upanel.list){
					u2.setLed( hw, "ff", i );
				}
			}
		}
		
		hw.close();
		
		/*
		for (I2C_Device u : Upanel.list){
			u.setLed( hw, "ff", 255);
		}

		for (I2C_Device u2 : Upanel.list){
			u2.setLed( hw, "ff", 0 );	// zgas
		}
		*/
		System.out.println("koniec ilumination2");
	}
	public void zapal(Hardware hw) {
		hw.connect();
		for (I2C_Device u2 : Upanel.list){
			u2.setLed( hw, "ff", 255 );
		}
		hw.close();
		System.out.println("koniec zapal");
	}
	public void zgas(Hardware hw) {
		hw.connect();
		for (I2C_Device u2 : Upanel.list){
			u2.setLed( hw, "ff", 0 );
		}
		hw.close();	
		System.out.println("koniec zgas");
	}
	public void mrygaj(Hardware hw, int time) {
		hw.connect();
		int swiec = 255;
		int razy = 500;

		boolean now = true;
		for( int i =0; i<razy;i++){
			for (I2C_Device u2 : Upanel.list){
				u2.setLed( hw, "0f", 0 );
			}
			if(time > 0){
				wait( time * (now ? 2: 1));
				now=!now;
			}
			for (I2C_Device u2 : Upanel.list){
				u2.setLed( hw, "0f", swiec );
			}
		}
		hw.close();
		System.out.println("koniec mrygaj");
	}
	public void fadeButelka(Hardware hw, int num, int count) {
		zgas( hw );
		hw.connect();
		Upanel butelka = Upanel.list.get(num);

		for (int b = 0;b<count;b++){
			int i=0;
			for (;i<205;i+=3){
					butelka.setLed( hw, "ff", i );
			}
			for (;i>=0;i-=1){
					butelka.setLed( hw, "ff", i );
			}
			butelka.setLed( hw, "ff", 0 );
			wait( 100 );
		} 
		hw.close();
		System.out.println("koniec fadeButelka");
	}
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