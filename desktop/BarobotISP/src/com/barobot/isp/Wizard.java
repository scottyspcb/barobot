package com.barobot.isp;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import com.barobot.common.IspSettings;
import com.barobot.i2c.BarobotTester;
import com.barobot.i2c.Carret;
import com.barobot.i2c.MainBoard;
import com.barobot.i2c.Upanel;
import com.barobot.parser.Operation;
import com.barobot.parser.Queue;
import com.barobot.parser.devices.I2C_Device;
import com.barobot.parser.devices.MotorDriver;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.output.AsyncDevice;

public class Wizard {
	public static int[] magnet_order = {0,2,1,4,3,6,5,8,7,10,9,11 };	// numer butelki, odj¹c 1 aby numer ID

	public static int[] upanelIndex2order = {0,2,4,6,8,10,1,3,5,7,9,11};	// numer butelki, odj¹c 1 aby numer ID
	public static int[] order2upanelIndex = {0,6,1,7,2,8,3,9,4,10,5,11};	// numer butelki, odj¹c 1 aby numer ID

	/*
	0	Upanel numer 0, adres: 12, index 3	0
	1	Upanel numer 1, adres: 19, index 0	2
	2	Upanel numer 2, adres: 17, index 0	4
	3	Upanel numer 3, adres: 20, index 0	6
	4	Upanel numer 4, adres: 22, index 0	8
	5	Upanel numer 5, adres: 14, index 0	10
	6	Upanel numer 0, adres: 16, index 4	1
	7	Upanel numer 1, adres: 23, index 0	3
	8	Upanel numer 2, adres: 18, index 0	5
	9	Upanel numer 3, adres: 15, index 0	7
	10	Upanel numer 4, adres: 21, index 0	9
	11	Upanel numer 5, adres: 13, index 0	11
	
	0,2,4,6,8,10,1,3,5,7,9,11
	*/
	
	
	
	
	
	
	public void findOrder(Hardware hw, int index ) {
		Queue q = hw.getQueue();
	//	String TimeStamp = new java.util.Date().toString();
		hw.connect();
		int current_index		= 0;
		MainBoard mb			= new MainBoard();
		Upanel current_dev		= new Upanel( index, 0 );
		current_dev.setOrder( 0 );
		q.add("", false);//reset
		boolean has_next 		= mb.readHasNext( q, index );
		System.out.println("has_next " + has_next );
		if(has_next){
			int device_found	= current_dev.resetAndReadI2c( hw.getQueue() );
			if( device_found > 0 ){		// pierwszy ma adres com.barobot.i2c
				Upanel.list.add( current_dev );	
				System.out.println("Upanel " + current_index + " ma adres " + device_found);
				current_dev.setAddress(device_found);
				current_dev.setLed( q, "22", 50 );
				find_next_to( hw, current_dev, current_index );
			}else{
				System.out.println("B£¥D Upanel " + (current_index) +" o adresie "+ current_dev.getAddress() + " jest ostatni");
			}
		}
		for (I2C_Device u : Upanel.list){
			u.setLed( q, "ff", 00 );
		}
		q.addWaitThread( Main.main );
		System.out.println("upaneli: " + Upanel.list.size());
		for (I2C_Device u : Upanel.list){
			System.out.println("Upanel numer "+ u.getOrder() + ", adres: "+ u.getAddress() +", index "+ u.getIndex());
		}
		if(order2upanelIndex.length == Upanel.list.size()){
			//upanel_order
			String res = "";
			for( int i = 0;i<order2upanelIndex.length; i++){
				int ind		= order2upanelIndex[ i ];
				int address = Upanel.list.get(ind).getAddress();
				res = res + address + ",";
			}
			System.out.println("Kolejnoœæ: " + res);
			
		}else{
			System.out.println("Upaneli: " +Upanel.list.size() +  " a powinno byc: " +  order2upanelIndex.length);
			
		}
		
		
		
		
	}
	private void find_next_to( Hardware hw, Upanel current_dev, int next_index) {
		Queue q = hw.getQueue();
		boolean has_next  = current_dev.readHasNext( q );
		System.out.println("has_next " + has_next );
		if(has_next){
			int device_found  = current_dev.resetNextAndReadI2c( q );
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
				u.setLed( q, "22", 50 );
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
		Queue q = hw.getQueue();
		Upanel.list.clear();
		Upanel.list.add( new Upanel( index, 0 ) );	
		String command = "";

		hw.connect();
		int current_index	= 0;
		Upanel current_dev	= Upanel.list.get(current_index);
		String upanel_code = current_dev.getHexFile();
		current_dev.setOrder( index );

		if( IspSettings.setFuseBits){
			current_dev.isp( q );
			Main.wait(2000);
			command = current_dev.setFuseBits( hw.comPort );
			Main.main.run(command, hw);
			Main.wait(1000);
		}
		if(IspSettings.setHex){
			current_dev.isp( q );
			Main.wait(2000);
			command = current_dev.uploadCode(upanel_code, hw.comPort );
			Main.main.run(command, hw);
			Main.wait(1000);
		}
		int device_found  = current_dev.resetAndReadI2c( q);
		if( device_found > 0 ){		// pierwszy ma adres com.barobot.i2c
			System.out.println("+Upanel " + current_index + " ma adres " + device_found);
			current_dev.setAddress(device_found);
			current_dev.setLed( q, "22", 255 );	
			boolean has_next  = current_dev.readHasNext( q );
		//	System.out.println("has_next " + has_next );
			if(has_next){
				prog_next_to( hw, current_dev, current_index+1, upanel_code );	
			}
		}else{
			System.out.println("B£¥D Upanel " + (current_index) +" o adresie "+ current_dev.getAddress() + " jest ostatni");
		}
	}

	private void prog_next_to(Hardware hw, Upanel current_dev, int next_index, String upanel_code ) {
		String command = "";
		Queue q = hw.getQueue();
		Upanel next_device	= new Upanel( 0, 0, current_dev );

		if( IspSettings.setFuseBits){
			current_dev.isp_next(q);
			command = next_device.setFuseBits( hw.comPort );
			Main.main.run(command, hw);
			Main.wait(1000);
		}
		if(IspSettings.setHex){
			current_dev.isp_next(q);
			command = next_device.uploadCode( upanel_code, hw.comPort );
			Main.main.run(command, hw);
			Main.wait(2000);
		}
		int device_found  = current_dev.resetNextAndReadI2c( q );
		Main.wait(2000);
		if( device_found > 0 ){		// pierwszy ma adres com.barobot.i2c
			next_device.setAddress(device_found);
			next_device.setOrder( next_index+1);
			next_device.canResetMe(current_dev);
			Upanel.list.add( next_device );
			System.out.println("++Upanel " + next_index + " ma adres " + device_found);
			next_device.setLed( q, "22", 255 );
			boolean has_next  = next_device.readHasNext( q );
			System.out.println("has_next " + has_next );
			if(has_next){
				prog_next_to( hw, next_device, next_index+1, upanel_code );
			}
		}else{
			System.out.println("++Upanel " + (next_index-1) +" o adresie "+ current_dev.getAddress() + " jest ostatni");
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
		final Queue q = hw.getQueue();
		if(Upanel.list.size() == 0 ){
			return;
		}

		hw.connect();
		int repeat = 20;

		System.out.println("Start" );
		
		int time = 1000;
		while (repeat-- > 0){
			for (I2C_Device u : Upanel.list){
				u.setLed( q, "ff", 0 );	// zgas
			}
			Main.wait(time);	

			for (I2C_Device u : Upanel.list){
				u.setLed( q, "ff", 0 );	// zgas
				u.setLed( q, "01", 255 );
			}
			Main.wait(time);
			
			for (I2C_Device u : Upanel.list){
				u.setLed( q, "ff", 0 );	// zgas
				u.setLed( q, "02", 255 );
			}
			Main.wait(time);	

			for (I2C_Device u : Upanel.list){
				u.setLed( q, "ff", 0 );	// zgas
				u.setLed( q, "04", 255 );
			}
			Main.wait(time);		

			for (I2C_Device u : Upanel.list){
				u.setLed( q, "ff", 0 );	// zgas
				u.setLed( q, "08", 255 );
			}
			Main.wait(time);	

			for (I2C_Device u : Upanel.list){
				u.setLed( q, "ff", 0 );	// zgas
				u.setLed( q, "10", 255 );
			}
			Main.wait(time);	
			

			for (I2C_Device u : Upanel.list){
				u.setLed( q, "ff", 0 );	// zgas
				u.setLed( q, "20", 255 );
			}
			Main.wait(time);	
			
			for (I2C_Device u : Upanel.list){
				u.setLed( q, "ff", 0 );	// zgas
				u.setLed( q, "40", 255 );
			}
			Main.wait(time);

			for (I2C_Device u : Upanel.list){
				u.setLed( q, "ff", 0 );	// zgas
				u.setLed( q, "80", 255 );
			}
			Main.wait(time);
		}
	}

	public void mrygaj_po_butelkach(Hardware hw) {
		if(Upanel.list.size() == 0 ){
			return;
		}
		hw.connect();
		Queue q = hw.getQueue();

		int time = 200;
		for (I2C_Device u2 : Upanel.list){
			u2.setLed( q, "ff", 0 );	// zgas
		}

		for (I2C_Device u : Upanel.list){

			Main.wait(time);

			u.setLed( q, "ff", 0 );	// zgas

			Main.wait(time);
			
		
			u.setLed( q, "ff", 0 );	// zgas
				u.setLed( q, "01", 255 );
			
			Main.wait(time);	
			
	
			u.setLed( q, "ff", 0 );	// zgas
				u.setLed( q, "02", 255 );
			
			Main.wait(time);	
			

			u.setLed( q, "ff", 0 );	// zgas
				u.setLed( q, "04", 255 );
			
			Main.wait(time);		
			
			
			
			u.setLed( q, "ff", 0 );	// zgas
				u.setLed( q, "08", 255 );
			
			Main.wait(time);	
			
			u.setLed( q, "ff", 0 );	// zgas
				u.setLed( q, "10", 255 );
	
			Main.wait(time);	
			

			
			u.setLed( q, "ff", 0 );	// zgas
				u.setLed( q, "20", 255 );
			
			Main.wait(time);	
			
			
			u.setLed( q, "ff", 0 );	// zgas
				u.setLed( q, "40", 255 );
			
			Main.wait(time);
			

			u.setLed( q, "ff", 0 );	// zgas
				u.setLed( q, "80", 255 );
			
			Main.wait(time);		

			u.setLed( q, "ff", 0 );	// zgas

		}
	}

	public void mrygaj_grb(Hardware hw) {
		if(Upanel.list.size() == 0 ){
			return;
		}
		hw.connect();
		Queue q = hw.getQueue();
		int repeat = 6;

		int time = 20;
		while (repeat-- > 0){

			for (I2C_Device u : Upanel.list){
				u.setLed( q, "ff", 0 );	// zgas
			}
			Main.wait(time);	
			
			for (I2C_Device u : Upanel.list){
				u.setLed( q, "ff", 0 );	// zgas

			//	u.setLed( q, "02", 0 );		// bottom green
			//	u.setLed( q, "08", 0 );		// bottom blue
			//	u.setLed( q, "04", 0 );		// bottom red 

				u.setLed( q, "07", 255 );	// top RGB

			//	u.setLed( q, "10", 255 );	// top green
			//	u.setLed( q, "20", 255 );	// top blue
			//	u.setLed( q, "40", 255 );	// top red
			}
			Main.wait(time);	
			
			
			for (I2C_Device u : Upanel.list){
				u.setLed( q, "ff", 0 );	// zgas
				u.setLed( q, "70", 255 );	// boottm RGB

			}
			Main.wait(time);			
		}
		Main.wait(2000);
	}
	
	public void test(Hardware hw) {
		/*
		try {
			FileHandler fh = new FileHandler("log_test.txt");
			Main.logger.addHandler(fh);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		
		hw.connect();
		/*
		Queue q = hw.getQueue();
		q.add(new AsyncMessage( "I", true ){
					@Override
					public boolean isRet(String result, Queue q) {
						if( "RI".equals(result)){
							return true;
						}
						return false;
					}
					public boolean onInput(String command) {
						System.out.println("onInput: " + command);
						return false;
					}
		});*/
		hw.send("I", "RI");

		Operation  op	= new Operation( "runTo" );
		op.needParam("x", 10 );
		op.needParam("y" );
		op.needParam("z", 20 );
		op.needParam("sth", null );		

		hw.send("TEST", "RTEST");
	//	q.addWaitThread( Main.main );
		System.out.println("wizard end");
	}

	public void prepareCarret(Hardware hw) {
		String command = "";
		Queue q = hw.getQueue();
		hw.connect();
		hw.send("\n");
		hw.send("\n");
		hw.send("PING", "PONG");
		Carret current_dev	= new Carret();
		String carret_code = current_dev.getHexFile();
		q.addWaitThread(Main.mt);
		if( IspSettings.setFuseBits){
			current_dev.isp( q );
			q.addWaitThread(Main.mt);
			command = current_dev.setFuseBits( hw.comPort );
			Main.main.run(command, hw);
			Main.wait(2000);
		}

		if(IspSettings.setHex){
			current_dev.isp( q );
			command = current_dev.uploadCode( carret_code, hw.comPort );
			Main.main.run(command, hw);
			Main.wait(2000);
		}
		/*
		int device_found  = current_dev.resetAndReadI2c( hw );
		if( device_found > 0 ){		// pierwszy ma adres com.barobot.i2c
			System.out.println("+Carret  ma adres " + device_found);
		}else{
			System.out.println("B£¥D Carret o nie zg³asza siê");
		}*/
	}

	public void checkCarret(Hardware hw) {
		String command = "";
		Queue q = hw.getQueue();
		hw.connect();
		//I2C_Device current_dev	= new Upanel( 3, 0 );
		I2C_Device current_dev	= new Carret();
		current_dev.isp( q );
		command = current_dev.checkFuseBits( hw.comPort );
		Main.main.run(command, hw);
	}

	public void prepareMB(final Hardware hw ) {
		Queue q = hw.getQueue();
		hw.connect();
		hw.send("\n");
		hw.send("\n");
		final I2C_Device current_dev	= new MainBoard();
		final String upanel_code = current_dev.getHexFile();
		q.add("", false);		
		hw.send("PING", "PONG");
		q.addWaitThread(Main.mt);
		if(IspSettings.setHex){	
			current_dev.isp( q );	// mam 2 sek na wystartwanie
			q.add( new AsyncMessage( true ){		// na koncu zamknij
				@Override
				public Queue run(AsyncDevice dev, Queue queue) {
					command = current_dev.uploadCode( upanel_code, hw.comPort);
					Main.main.run(command, hw);
					return null;
				}
			});
		}
		q.addWaitThread(Main.mt);
		hw.send("");
	}

	public void prepareMBManualReset(final Hardware hw) {
		String command					= "";
		Queue q = hw.getQueue();
		final I2C_Device current_dev	= new MainBoard();
		final String upanel_code		= current_dev.getHexFile();
		if(IspSettings.setHex){	
			command = current_dev.uploadCode(upanel_code, hw.comPort);
			Main.main.run(command, hw);
		}
	}

	public void prepareSlaveMB(final Hardware hw) {
		final I2C_Device current_dev	= new BarobotTester();
		Queue q = hw.getQueue();

		if(IspSettings.setFuseBits){
			hw.connect();
			hw.send("\n");
			hw.send("PING", "PONG");
			q.addWaitThread(Main.mt);
			current_dev.isp( q );
			q.add( new AsyncMessage( true ){		// na koncu zamknij
				@Override
				public Queue run(AsyncDevice dev, Queue queue) {
					String command = current_dev.setFuseBits( hw.comPort );
					Main.main.run(command, hw);
					return null;
				}
			});
			hw.closeOnReady();
			q.addWaitThread(Main.main);
		}
		if(IspSettings.setHex){	
			hw.connect();
			hw.send("PING", "PONG");
			current_dev.isp( q );	// mam 2 sek na wystartwanie
			q.add( new AsyncMessage( true ){		// na koncu zamknij
				public Queue run(AsyncDevice dev) {
					command = current_dev.uploadCode( current_dev.getHexFile(), hw.comPort);
					Main.main.run(command, hw);
					return null;
				}
			});
			hw.closeOnReady();
			q.addWaitThread(Main.main);
		}
	}

	public void prepare1Upanel(Hardware hw, int index ) {
		String command = "";
		Queue q = hw.getQueue();
		hw.connect();
		Upanel current_dev	= new Upanel( index, 0 );
		String upanel_code = current_dev.getHexFile();
		if( IspSettings.setFuseBits){
			current_dev.isp( q );
			command = current_dev.setFuseBits(hw.comPort);
			Main.main.run(command, hw);
			Main.wait(2000);
		}
		if(IspSettings.setHex){
			current_dev.isp( q );
			command = current_dev.uploadCode( upanel_code, hw.comPort );
			Main.main.run(command, hw);
			Main.wait(2000);
		}
		System.out.println("koniec prepare1Upanel");
	}
	public void illumination1(Hardware hw) {
		int repeat = 1;
		while(repeat-->0){
			this.mrygaj( hw );
			this.mrygaj_grb( hw );
			this.mrygaj_po_butelkach( hw );
		}
		System.out.println("koniec illumination1");
	}

	public void ilumination2(Hardware hw) {
		System.out.println("upaneli: " + Upanel.list.size());
		hw.connect();
		Queue q = hw.getQueue();
		
		for (int b = 0;b<4;b++){
			int i=0;
			for (;i<245;i+=5){
				for (I2C_Device u2 : Upanel.list){
					u2.setLed( q, "ff", i );
				}
			}
			for (;i>=0;i-=5){
				for (I2C_Device u2 : Upanel.list){
					u2.setLed( q, "ff", i );
				}
			}
		}
		
		/*
		for (I2C_Device u : Upanel.list){
			u.setLed( q, "ff", 255);
		}

		for (I2C_Device u2 : Upanel.list){
			u2.setLed( hw, "ff", 0 );	// zgas
		}
		*/
		System.out.println("koniec ilumination2");
	}

	public void ilumination3(Hardware hw, String led, int value, String led2, int value2 ) {
		System.out.println("upaneli: " + Upanel.list.size());
		hw.connect();
		Queue q = hw.getQueue();
	
		Carret current_dev	= new Carret();
		current_dev.setLed( q, "ff", 0 );
		current_dev.setLed( q, led2, value2 );
		for (I2C_Device u2 : Upanel.list){
			u2.setLed( q, "ff", 0 );
			u2.setLed( q, led, value );
		}

	
		/*
		for (I2C_Device u : Upanel.list){
			u.setLed( q, "ff", 255);
		}

		for (I2C_Device u2 : Upanel.list){
			u2.setLed( hw, "ff", 0 );	// zgas
		}
		*/
		System.out.println("koniec ilumination2");
	}

	public void zapal(Hardware hw) {
		hw.connect();
		Queue q = hw.getQueue();
		for (I2C_Device u2 : Upanel.list){
			u2.setLed( q, "ff", 255 );
		}
		System.out.println("koniec zapal");
	}
	public void zgas(Hardware hw) {
		hw.connect();
		Queue q = hw.getQueue();
		for (I2C_Device u2 : Upanel.list){
			u2.setLed( q, "ff", 0 );
		}
		System.out.println("koniec zgas");
	}
	public void mrygaj(Hardware hw, int time){
		hw.connect();
		int swiec = 255;
		int razy = 500;
		Queue q = hw.getQueue();
		for( int i =0; i<razy;i++){
			for (I2C_Device u2 : Upanel.list){
				u2.setLed( q, "0f", 0 );
			}
			for (I2C_Device u2 : Upanel.list){
				u2.setLed( q, "0f", swiec );
			}
		}
		System.out.println("koniec mrygaj");
	}
	public void fadeButelka(Hardware hw, int num, int count) {
		final Queue q = hw.getQueue();
		zgas( hw );
		hw.connect();
		Upanel butelka = Upanel.list.get(num);

		for (int b = 0;b<count;b++){
			int i=0;
			for (;i<205;i+=3){
					butelka.setLed( q, "ff", i );
			}
			for (;i>=0;i-=1){
					butelka.setLed( q, "ff", i );
			}
			butelka.setLed( q, "ff", 0 );
			Main.wait( 100 );
		} 
		System.out.println("koniec fadeButelka");
	}

	public void swing(Hardware hw, int i, int min, int max) {
		hw.connect();
		Queue q						= hw.getQueue();
		MotorDriver driver_x		= new MotorDriver();
		driver_x.defaultSpeed		= 2500;
		driver_x.setSPos( 0 );
		driver_x.movoTo(q, 1000);
		driver_x.movoTo(q, 2000);
		driver_x.movoTo(q, 1000);
	//	MainBoard mb	= new MainBoard();
	//	mb.moveX(max);
	}

	public void test_proc(Hardware hw) {
		Queue q = hw.getQueue();
		hw.send( "P3" );
	//	SISP

		hw.send( "P3" );
/*
		q.add(new AsyncMessage( "I", true ){
					@Override
					public boolean isRet(String result, Queue q) {
						if( "RI".equals(result)){
							return true;
						}
						return false;
					}
					public boolean onInput(String command) {
						System.out.println("onInput: " + command);
						return false;
					}
		});*/
		hw.send("I", "RI");
		hw.send("TEST", "RTEST");

	//	q.addWaitThread( Main.main );
	}

	public void fast_close_test(Hardware hw) {
		I2C_Device current_dev	= new BarobotTester();
		hw.connect();
		hw.send("K1","RK1");

		hw.close();
		hw.connect();
		hw.send("K1","RK1");
		hw.close();
		
		hw.connect();
		hw.send("K1","RK1");
		hw.close();

		hw.connect();
		hw.send("K1","RK1");
		hw.close();
	}
	public void prepareMB2(final Hardware hw) {	
		Queue q = hw.getQueue();
		hw.connect();
		hw.send("\n");
		hw.send("\n");
		final I2C_Device current_dev	= new MainBoard();
		final String upanel_code = current_dev.getHexFile();
		
		
   	 //	com.barobot.isp.IspOverComSerial mSerial = new IspOverComSerial();
   	 	
   	 	
		q.add("", false);		
		hw.send("PING", "PONG");
		q.addWaitThread(Main.mt);
		if(IspSettings.setHex){	
			current_dev.isp( q );	// mam 2 sek na wystartwanie
			q.add( new AsyncMessage( true ){		// na koncu zamknij
				@Override
				public Queue run(AsyncDevice dev, Queue queue) {
					command = current_dev.uploadCode( upanel_code, hw.comPort);
					Main.main.run(command, hw);
					return null;
				}
			});
		}
		q.addWaitThread(Main.mt);
	}
	public void createContstans(Hardware hw) {
		String source = "D:\\PROG\\arduino-1.0.5\\libraries\\barobot_common\\constants.h";
		String detsination = "C:\\workspace\\Barobot\\android\\com.barobot.common\\src\\com\\barobot\\common\\constant\\Methods.java";	

		BufferedReader br = null;
		String data = "";
		try {
			br = new BufferedReader(new FileReader(source));
	        StringBuilder sb = new StringBuilder();
	        String line = translateLine(br.readLine());
	        while (line != null) {
	            sb.append(line);
	            sb.append("\n");
	            line = translateLine(br.readLine());
	        }
	        data = sb.toString();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
	    } catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(br!=null){
		        try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
	    }
		FileOutputStream fout;		
		try{
		    fout = new FileOutputStream (detsination);
		    PrintStream writer = new PrintStream(fout);
		    
		    writer.print("package com.barobot.common.constant;\n\n" +
		    		"public class Methods {\n\n");

		 	writer.println (data);
		   	writer.println ("}");
		    fout.close();		
		}
		catch (IOException e){
			System.err.println ("Unable to write to file");
			System.exit(-1);
		}	
	}
	private String translateLine(String readLine) {
		if(readLine!=null){
			if(readLine.contains("'")){		// CHAR
				readLine = readLine.replaceAll("'", "\"");
			}
			if(readLine.contains("\"")){	// STRING
				readLine = readLine.replaceAll("#define\\s", "\tpublic static final String ");
				readLine = readLine.replaceAll("\\s+(\".*\")$", "\t= $1;");
			}else{
				readLine = readLine.replaceAll("#define\\s", "\tpublic static final int ");
				readLine = readLine.replaceAll("\\s((0x)?\\d+)\\s*$", "\t= $1;");
			}
		//	System.out.println("onInput: " + readLine);
		}
		return readLine;
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