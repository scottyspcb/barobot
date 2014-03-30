package com.barobot.isp;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

import com.barobot.common.IspSettings;
import com.barobot.common.constant.Methods;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.hardware.devices.LedOrder;
import com.barobot.hardware.devices.OnReadyListener;
import com.barobot.hardware.devices.i2c.BarobotTester;
import com.barobot.hardware.devices.i2c.Carret;
import com.barobot.hardware.devices.i2c.I2C_Device;
import com.barobot.hardware.devices.i2c.MainboardI2c;
import com.barobot.hardware.devices.i2c.Upanel;
import com.barobot.parser.Queue;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.message.Mainboard;
import com.barobot.parser.utils.Decoder;

public class Wizard {

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

	public void mrygaj(Hardware hw) {
		final Queue q = hw.getQueue();
		if(hw.barobot.i2c.list.size() == 0 ){
			return;
		}

		hw.connectIfDisconnected();
		int repeat = 20;

		System.out.println("Start" );
		
		int time = 1000;
		while (repeat-- > 0){
			for (I2C_Device u : hw.barobot.i2c.list){
				u.setLed( q, "ff", 0 );	// zgas
			}
			Main.wait(time);	

			for (I2C_Device u : hw.barobot.i2c.list){
				u.setLed( q, "ff", 0 );	// zgas
				u.setLed( q, "01", 255 );
			}
			Main.wait(time);
			
			for (I2C_Device u : hw.barobot.i2c.list){
				u.setLed( q, "ff", 0 );	// zgas
				u.setLed( q, "02", 255 );
			}
			Main.wait(time);	

			for (I2C_Device u : hw.barobot.i2c.list){
				u.setLed( q, "ff", 0 );	// zgas
				u.setLed( q, "04", 255 );
			}
			Main.wait(time);		

			for (I2C_Device u : hw.barobot.i2c.list){
				u.setLed( q, "ff", 0 );	// zgas
				u.setLed( q, "08", 255 );
			}
			Main.wait(time);	

			for (I2C_Device u : hw.barobot.i2c.list){
				u.setLed( q, "ff", 0 );	// zgas
				u.setLed( q, "10", 255 );
			}
			Main.wait(time);	
			

			for (I2C_Device u : hw.barobot.i2c.list){
				u.setLed( q, "ff", 0 );	// zgas
				u.setLed( q, "20", 255 );
			}
			Main.wait(time);	
			
			for (I2C_Device u : hw.barobot.i2c.list){
				u.setLed( q, "ff", 0 );	// zgas
				u.setLed( q, "40", 255 );
			}
			Main.wait(time);

			for (I2C_Device u : hw.barobot.i2c.list){
				u.setLed( q, "ff", 0 );	// zgas
				u.setLed( q, "80", 255 );
			}
			Main.wait(time);
		}
	}

	public void mrygaj_po_butelkach(Hardware hw) {
		if(hw.barobot.i2c.list.size() == 0 ){
			return;
		}
		hw.connectIfDisconnected();
		Queue q = hw.getQueue();

		int time = 200;
		for (I2C_Device u2 : hw.barobot.i2c.list){
			u2.setLed( q, "ff", 0 );	// zgas
		}

		for (I2C_Device u : hw.barobot.i2c.list){

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
		if(hw.barobot.i2c.list.size() == 0 ){
			return;
		}
		hw.connectIfDisconnected();
		Queue q = hw.getQueue();
		int repeat = 6;

		int time = 20;
		while (repeat-- > 0){

			for (I2C_Device u : hw.barobot.i2c.list){
				u.setLed( q, "ff", 0 );	// zgas
			}
			Main.wait(time);	
			
			for (I2C_Device u : hw.barobot.i2c.list){
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
			
			
			for (I2C_Device u : hw.barobot.i2c.list){
				u.setLed( q, "ff", 0 );	// zgas
				u.setLed( q, "70", 255 );	// boottm RGB

			}
			Main.wait(time);			
		}
		Main.wait(2000);
	}
	
	public void findOrder(final Hardware hw) {
		hw.connectIfDisconnected();
		Queue q = hw.getQueue();

		LedOrder lo = new LedOrder();
		lo.addOnReady( new OnReadyListener<LedOrder>(){
			public void onReady(LedOrder res) {
				for (I2C_Device u2 : hw.barobot.i2c.list){
					Upanel uu = (Upanel)u2;
					System.out.println("+Upanel "
							+ "dla butelki: " + uu.getBottleNum() 
							+ " w wierszu " + uu.getRow()
							+ " pod numerem " + uu.getNumInRow()
							+ " o indeksie " + uu.getRow()
							+ " ma adres " + uu.getAddress() );
				}
				System.out.println("Tkkkkkkkkkkkkkkkkkkk");
			}
		});
		lo.asyncStart(hw.barobot, q);
		
		Main.wait(7000);
		hw.closeNow();
		
		/*
		hw.send("I", "RI");
		Operation  op	= new Operation( "runTo" );
		op.needParam("x", 10 );
		op.needParam("y" );
		op.needParam("z", 20 );
		op.needParam("sth", null );		
*/
//		hw.send("TEST", "RTEST");
	//	q.addWaitThread( Main.main );
		System.out.println("wizard end");
	}

	public void checkCarret(Hardware hw) {
		String command = "";
		Queue q = hw.getQueue();
		hw.connectIfDisconnected();
		//I2C_Device current_dev	= new Upanel( 3, 0 );
		I2C_Device current_dev	= hw.barobot.i2c.carret;
		current_dev.isp( q );
		command = current_dev.checkFuseBits( hw.comPort );
		Main.main.runCommand(command, hw);
	}

	public void prepareMB(final Hardware hw ) {
		Queue q = hw.getQueue();
		hw.connectIfDisconnected();
		q.add( "\n", false );
		q.add( "\n", false );
		final I2C_Device current_dev	= new MainboardI2c();
		final String upanel_code = current_dev.getHexFile();
		q.add("", false);		
		q.add("PING", "PONG");
		q.addWaitThread(Main.mt);
		if(IspSettings.setHex){	
			current_dev.isp( q );	// mam 2 sek na wystartwanie
			q.add( new AsyncMessage( true ){		// na koncu zamknij
				@Override
				public String getName() {
					return "prepareMB";
				}
				@Override
				public Queue run(Mainboard dev, Queue queue) {
					command = current_dev.uploadCode( upanel_code, hw.comPort);
					Main.main.runCommand(command, hw);
					return null;
				}
			});
		}
		q.addWaitThread(Main.mt);
		q.add( "\n", false );
	}

	public void prepareMBManualReset(final Hardware hw) {
		String command					= "";
		Queue q							= hw.getQueue();
		final I2C_Device current_dev	= new MainboardI2c();
		final String upanel_code		= current_dev.getHexFile();
		if(IspSettings.setHex){	
			command = current_dev.uploadCode(upanel_code, hw.comPort);
			Main.main.runCommand(command, hw);
		}
	}

	public void prepareSlaveMB(final Hardware hw) {
		final I2C_Device current_dev	= new BarobotTester();
		Queue q = hw.getQueue();

		if(IspSettings.setFuseBits){
			hw.connectIfDisconnected();
			q.add( "\n", false );
			q.add( "\n", false );
			q.add("PING", "PONG");
			q.addWaitThread(Main.mt);
			current_dev.isp( q );
			q.add( new AsyncMessage( true ){		// na koncu zamknij
				@Override
				public String getName() {
					return "prepareSlaveMB2";
				}
				@Override
				public Queue run(Mainboard dev, Queue queue) {
					String command = current_dev.setFuseBits( hw.comPort );
					Main.main.runCommand(command, hw);
					return null;
				}
			});
			hw.closeOnReady();
			q.addWaitThread(Main.main);
		}
		if(IspSettings.setHex){	
			hw.connectIfDisconnected();
			q.add("PING", "PONG");
			current_dev.isp( q );	// mam 2 sek na wystartwanie
			q.add( new AsyncMessage( true ){		// na koncu zamknij
				@Override
				public String getName() {
					return "prepareSlaveMB";
				}
				@Override
				public Queue run(Mainboard dev, Queue queue) {
					command = current_dev.uploadCode( current_dev.getHexFile(), hw.comPort);
					Main.main.runCommand(command, hw);
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
		hw.connectIfDisconnected();
		Upanel current_dev	= new Upanel();
		current_dev.setRow(index);
		current_dev.setIndex(index);
		
		String upanel_code = current_dev.getHexFile();
		if( IspSettings.setFuseBits){
			current_dev.isp( q );
			command = current_dev.setFuseBits(hw.comPort);
			Main.main.runCommand(command, hw);
			Main.wait(2000);
		}
		if(IspSettings.setHex){
			current_dev.isp( q );
			command = current_dev.uploadCode( upanel_code, hw.comPort );
			Main.main.runCommand(command, hw);
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
		System.out.println("upaneli: " + hw.barobot.i2c.list.size());
		hw.connectIfDisconnected();
		Queue q = hw.getQueue();
		
		for (int b = 0;b<4;b++){
			int i=0;
			for (;i<245;i+=5){
				for (I2C_Device u2 : hw.barobot.i2c.list){
					u2.setLed( q, "ff", i );
				}
			}
			for (;i>=0;i-=5){
				for (I2C_Device u2 : hw.barobot.i2c.list){
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
		System.out.println("upaneli: " + hw.barobot.i2c.list.size());
		hw.connectIfDisconnected();
		Queue q = hw.getQueue();
	
		Carret current_dev	= hw.barobot.i2c.carret;
		current_dev.setLed( q, "ff", 0 );
		current_dev.setLed( q, led2, value2 );
		for (I2C_Device u2 : hw.barobot.i2c.list){
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
		hw.connectIfDisconnected();
		Queue q = hw.getQueue();
		for (I2C_Device u2 : hw.barobot.i2c.list){
			u2.setLed( q, "ff", 255 );
		}
		System.out.println("koniec zapal");
	}
	public void zgas(Hardware hw) {
		hw.connectIfDisconnected();
		Queue q = hw.getQueue();
		for (I2C_Device u2 : hw.barobot.i2c.list){
			u2.setLed( q, "ff", 0 );
		}
		System.out.println("koniec zgas");
	}
	public void mrygaj(Hardware hw, int time){
		hw.connectIfDisconnected();
		int swiec = 255;
		int razy = 500;
		Queue q = hw.getQueue();
		for( int i =0; i<razy;i++){
			for (I2C_Device u2 : hw.barobot.i2c.list){
				u2.setLed( q, "0f", 0 );
			}
			for (I2C_Device u2 : hw.barobot.i2c.list){
				u2.setLed( q, "0f", swiec );
			}
		}
		System.out.println("koniec mrygaj");
	}
	public void fadeButelka(Hardware hw, int num, int count) {
		final Queue q = hw.getQueue();
		zgas( hw );
		hw.connectIfDisconnected();
		Upanel butelka = hw.barobot.i2c.list.get(num);

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
		hw.connectIfDisconnected();
		/*
		Queue q						= hw.getQueue();
		MotorDriver driver_x		= new MotorDriver();
		driver_x.defaultSpeed		= 2500;
		driver_x.setSPos( 0 );
		driver_x.movoTo(q, 1000);
		driver_x.movoTo(q, 2000);
		driver_x.movoTo(q, 1000);
	//	MainBoard mb	= new MainBoard();
	//	mb.moveX(max);*/
	}

	public void test_proc(Hardware hw) {
		Queue q = hw.getQueue();
		q.add( "P3", false );
	//	SISP
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
		q.add("I", "RI");
		q.add("TEST", "RTEST");

	//	q.addWaitThread( Main.main );
	}

	public void fast_close_test(Hardware hw) {
		Queue q = hw.getQueue();
		I2C_Device current_dev	= new BarobotTester();
		hw.connectIfDisconnected();
		q.add("K1","RK1");

		hw.closeNow();
		hw.connectIfDisconnected();
		q.add("K1","RK1");
		hw.closeNow();
		
		hw.connectIfDisconnected();
		q.add("K1","RK1");
		hw.closeNow();

		hw.connectIfDisconnected();
		q.add("K1","RK1");
		hw.closeNow();
	}
	public void prepareMB2(final Hardware hw) {	
		Queue q = hw.getQueue();
		hw.connectIfDisconnected();
		q.add( "\n", false );
		q.add( "\n", false );
		final I2C_Device current_dev	= new MainboardI2c();
		final String upanel_code = current_dev.getHexFile();

   	 //	com.barobot.isp.IspOverComSerial mSerial = new IspOverComSerial();
   	 	
		q.add("", false);		
		q.add("PING", "PONG");
		q.addWaitThread(Main.mt);
		if(IspSettings.setHex){	
			current_dev.isp( q );	// mam 2 sek na wystartwanie
			q.add( new AsyncMessage( true ){		// na koncu zamknij
				@Override
				public String getName() {
					return "prepareMB2";
				}
				@Override
				public Queue run(Mainboard dev, Queue queue) {
					command = current_dev.uploadCode( upanel_code, hw.comPort);
					Main.main.runCommand(command, hw);
					return null;
				}
			});
		}
		q.addWaitThread(Main.mt);
	}

}
