package com.barobot.isp;

import com.barobot.common.Initiator;
import com.barobot.common.constant.Methods;
import com.barobot.hardware.devices.LedOrder;
import com.barobot.hardware.devices.i2c.BarobotTester;
import com.barobot.hardware.devices.i2c.Carret;
import com.barobot.hardware.devices.i2c.I2C_Device;
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

	public void mrygaj(Hardware hw, int repeat ) {
		hw.getQueue().addWaitThread( Main.main );

		I2C_Device[] list = hw.barobot.i2c.getDevices();
		final Queue q = hw.getQueue();
		if(list.length == 0 ){
			System.out.println("Pusto" );
			return;
		}
		System.out.println("Start" );
		int time =10;
		while (repeat-- > 0){
			for (I2C_Device u : list){
				u.addLed( q, "ff", 0 );	// off
			}
			q.addWait(time );

			for (I2C_Device u : list){
				u.addLed( q, "ff", 0 );	// off
				u.addLed( q, "01", 255 );
			}
			q.addWait(time );
			
			for (I2C_Device u : list){
				u.addLed( q, "ff", 0 );	// off
				u.addLed( q, "02", 255 );
			}
			q.addWait(time );

			for (I2C_Device u : list){
				u.addLed( q, "ff", 0 );	// off
				u.addLed( q, "04", 255 );
			}
			q.addWait(time );

			for (I2C_Device u : list){
				u.addLed( q, "ff", 0 );	// off
				u.addLed( q, "08", 255 );
			}
			q.addWait(time );

			for (I2C_Device u : list){
				u.addLed( q, "ff", 0 );	// off
				u.addLed( q, "10", 255 );
			}
			q.addWait(time );
			
			for (I2C_Device u : list){
				u.addLed( q, "ff", 0 );	// off
				u.addLed( q, "20", 255 );
			}
			q.addWait(time );
			
			for (I2C_Device u : list){
				u.addLed( q, "ff", 0 );	// off
				u.addLed( q, "40", 255 );
			}
			q.addWait(time );

			for (I2C_Device u : list){
				u.addLed( q, "ff", 0 );	// off
				u.addLed( q, "80", 255 );
			}
			q.addWait(time );
		}
	}

	public void findStops(Hardware hw) {
		Queue q = hw.getQueue();
		hw.connectIfDisconnected();
		hw.barobot.driver_x.moveTo( q, 100 );
		hw.barobot.driver_x.moveTo( q, 1000 );	
		q.addWait(5000 );
	}

	public void mrygaj_po_butelkach(Hardware hw, int time ) {
		hw.getQueue().addWaitThread( Main.main );
		I2C_Device[] list = hw.barobot.i2c.getUpanels();
		if(list.length == 0 ){
			System.out.println("Pusto" );
			return;
		}
		Queue q = hw.getQueue();
		for (I2C_Device u2 : list){
			u2.addLed( q, "ff", 0 );	// off
		}
	//	get 2 random from array java
		for (I2C_Device u : list){
			q.addWait(time );
			u.addLed( q, "ff", 0 );	// off
			q.addWait(time );

			u.addLed( q, "ff", 0 );	// off
			u.addLed( q, "01", 255 );
			
			q.addWait(time );

			u.addLed( q, "ff", 0 );	// off
			u.addLed( q, "02", 255 );
			
			q.addWait(time );

			u.addLed( q, "ff", 0 );	// off
			u.addLed( q, "04", 255 );
			
			q.addWait(time );
			u.addLed( q, "ff", 0 );	// off
			u.addLed( q, "08", 255 );
			
			q.addWait(time );
			
			u.addLed( q, "ff", 0 );	// off
			u.addLed( q, "10", 255 );
	
			q.addWait(time );
			

			u.addLed( q, "ff", 0 );	// off
			u.addLed( q, "20", 255 );
			
			q.addWait(time );
			
			
			u.addLed( q, "ff", 0 );	// off
			u.addLed( q, "40", 255 );
			
			q.addWait(time );
			

			u.addLed( q, "ff", 0 );	// off
			u.addLed( q, "80", 255 );
			
			q.addWait(time );

			u.addLed( q, "ff", 0 );	// off
		}
	}

	public void mrygaj_grb(Hardware hw, int repeat) {
		hw.getQueue().addWaitThread( Main.main );
		I2C_Device[] list = hw.barobot.i2c.getUpanels();
		if(list.length == 0 ){
			System.out.println("Pusto" );
			return;
		}
		hw.connectIfDisconnected();
		Queue q = hw.getQueue();
		int time = 0;
		while (repeat-- > 0){
			for (I2C_Device u : list){
				u.addLed( q, "ff", 0 );	// off
			}
			q.addWait(time );

			for (I2C_Device u : list){
				u.addLed( q, "ff", 0 );	// off
			//	u.setLed( q, "02", 0 );		// bottom green
			//	u.setLed( q, "08", 0 );		// bottom blue
			//	u.setLed( q, "04", 0 );		// bottom red 
				u.addLed( q, "07", 255 );	// top RGB
			//	u.setLed( q, "10", 255 );	// top green
			//	u.setLed( q, "20", 255 );	// top blue
			//	u.setLed( q, "40", 255 );	// top red
			}
			q.addWait(time );

			for (I2C_Device u : list){
				u.addLed( q, "ff", 0 );	// off
				u.addLed( q, "70", 255 );	// boottm RGB
			}
			q.addWait(time );
			time--;	
		}
	}
	
	public void findOrder(final Hardware hw) {
		hw.connectIfDisconnected();
		Queue q		= hw.getQueue();
		LedOrder lo = new LedOrder();
		lo.asyncStart(hw.barobot, q);
		q.add( new AsyncMessage( true ){
			@Override	
			public String getName() {
				return "onReady LedOrder" ;
			}
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				Upanel[] up		= hw.barobot.i2c.getUpanels();
				for(int i =up.length-1; i>=0;i--){
					Upanel uu = up[i];
					System.out.println("+Upanel "
							+ "dla butelki: " + uu.getBottleNum() 
							+ " w wierszu " + uu.getRow()
							+ " pod numerem " + uu.getNumInRow()
							+ " o indeksie " + uu.getRow()
							+ " ma adres " + uu.getAddress() );
				}
				return null;
			}
		});
		q.addWaitThread(Main.mt);
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

	public void illumination1(Hardware hw) {
		hw.connectIfDisconnected();
		hw.getQueue().addWaitThread(Main.main);
		int repeat = 1;
		while(repeat-->0){
		
	//		
		//	hw.getQueue().addWaitThread( Main.main );
			
			this.mrygaj_grb( hw, 30 );
			hw.getQueue().addWaitThread( Main.main );
			
		//	this.fadeAll( hw, 5 );
			
		//	hw.getQueue().addWaitThread( Main.main );
		}
		System.out.println("koniec illumination1");
	}
	public void ilumination2(Hardware hw) {
		hw.getQueue().addWaitThread( Main.main );
		I2C_Device[] list = hw.barobot.i2c.getDevices();
		System.out.println("upaneli: " +list.length);
		hw.connectIfDisconnected();
		Queue q = hw.getQueue();

		for (int b = 0;b<4;b++){
			int i=0;
			for (;i<245;i+=5){
				for (I2C_Device u2 : list){
					u2.addLed( q, "ff", i );
				}
			}
			for (;i>=0;i-=5){
				for (I2C_Device u2 : list){
					u2.addLed( q, "ff", i );
				}
			}
		}
		/*
		for (I2C_Device u : Upanel.list){
			u.setLed( q, "ff", 255);
		}
		for (I2C_Device u2 : Upanel.list){
			u2.setLed( hw, "ff", 0 );	// off
		}
		*/
		System.out.println("koniec ilumination2");
	}

	public void ilumination3(Hardware hw, String led, int value, String led2, int value2 ) {
		hw.getQueue().addWaitThread( Main.main );
		hw.connectIfDisconnected();
		I2C_Device[] list = hw.barobot.i2c.getDevices();
		System.out.println("upaneli: " + list.length);
		Queue q = hw.getQueue();
		Carret current_dev	= hw.barobot.i2c.carret;
		current_dev.addLed( q, "ff", 0 );
		current_dev.addLed( q, led2, value2 );
		for (I2C_Device u2 : list){
			u2.addLed( q, "ff", 0 );
			u2.addLed( q, led, value );
		}
		for (I2C_Device u2 : list){
			u2.addLed( q, "ff", 255);
		}
		for (I2C_Device u2 : list){
			u2.addLed( q, "ff", 0 );	// off
		}
		System.out.println("koniec ilumination2");
	}


	public void zapal(Hardware hw) {
		hw.getQueue().addWaitThread( Main.main );
		hw.connectIfDisconnected();
		Queue q = hw.getQueue();
		I2C_Device[] list = hw.barobot.i2c.getDevices();
		for (I2C_Device u2 : list){
			u2.addLed( q, "ff", 255 );
		}
		System.out.println("koniec zapal");
	}
	public void zgas(Hardware hw, Queue q ) {
		hw.connectIfDisconnected();
		I2C_Device[] list = hw.barobot.i2c.getDevices();
		for (I2C_Device u2 : list){
			u2.addLed( q, "ff", 0 );
		}
		System.out.println("koniec zgas");
	}
	public void zamrugaj(Hardware hw, int time, int razy ){
		hw.getQueue().addWaitThread( Main.main );
		hw.connectIfDisconnected();
		I2C_Device[] list = hw.barobot.i2c.getDevices();
		int swiec = 255;
		Queue q = hw.getQueue();
		for( int i =0; i<razy;i++){
			for (I2C_Device u2 : list){
				u2.addLed( q, "f0", 0 );
			}
			for (I2C_Device u2 : list){
				u2.addLed( q, "f0", swiec );
			}
		}
		System.out.println("koniec mrygaj");
	}
	public void fadeButelka(final Hardware hw, final int num, final int count) {
		Queue q = hw.getQueue();
		q.add( new AsyncMessage( true ){
			@Override
			public String getName() {
				return "aaaa";
			}
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				Queue q2 = new Queue();
				zgas( hw, q2 );
				Upanel[] list = hw.barobot.i2c.getUpanels();
				final Upanel butelka = list[num];
				for (int b = 0;b<count;b++){
					int i=0;
					for (;i<255;i+=4){
						butelka.addLed( q2, "ff", i );
					}
					for (i=255;i>=0;i-=4){
						butelka.addLed( q2, "ff", i );
					}
					zgas( hw, q2 );
				} 
				System.out.println("koniec fadeButelka1");
				return q2;
			}
		});		
		System.out.println("koniec fadeButelka2");
	}
	
	public void fadeAll(final Hardware hw, final int count) {
		Queue q = hw.getQueue();
		q.add( new AsyncMessage( true ){		// na koncu zamknij
			@Override
			public String getName() {
				return "aaaa";
			}
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				Queue q2 = new Queue();
				zgas( hw, q2 );
				Upanel[] list = hw.barobot.i2c.getUpanels();
				for (int b = 0;b<count;b++){
					int i=0;
					for (;i<255;i+=8){
						for (I2C_Device u2 : list){
							u2.addLed( q2, "ff", i );
						}
					}
					for (i=255;i>=0;i-=8){
						for (I2C_Device u2 : list){
							u2.addLed( q2, "ff", i );
						}
					}
					zgas( hw, q2 );
				} 
				System.out.println("koniec fadeButelka");
				return q2;
			}
		});
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
	
	public void zapalPrzod(Hardware hw) {
		Upanel[] list = hw.barobot.i2c.getUpanels();
		Queue q = hw.getQueue();
		for(int i =list.length-1; i>=0;i--){
			Upanel uu = list[i];
			if (uu.getRow() == Upanel.FRONT ){
				uu.addLed( q, "ff", 200 );
			}
		}
	}

	public void strobo(Hardware hw, int repeat) {
		hw.getQueue().addWaitThread( Main.main );

		I2C_Device[] list = hw.barobot.i2c.getDevices();
		final Queue q = hw.getQueue();
		if(list.length == 0 ){
			System.out.println("Pusto" );
			return;
		}
		hw.connectIfDisconnected();
		System.out.println("Start" );
		int time =5000;

		while (repeat-- > 0){
			for (I2C_Device u : list){
				u.addLed( q, "ff", 0 );	// off
			}
			if(time>0){
				q.addWait(	time / 100 );
			}
			for (I2C_Device u : list){
				u.addLed( q, "ff", 255 );		// on
			}
			time=time-10;
		}
	}

	public void nazmiane(final Hardware hw, final int repeat) {
		Queue q = hw.getQueue();
		q.addWaitThread( Main.main );
		final int time =700;
		final int colile = 40;

		q.add( new AsyncMessage( true ){		// na koncu zamknij
			@Override
			public String getName() {
				return "tecza";
			}
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				Queue q2 = new Queue();
				zgas( hw, q2 );
				Upanel[] list = hw.barobot.i2c.getUpanels();
				if(list.length == 0 ){
					System.out.println("Pusto" );
					return null;
				}
				for (int k = 0;k<repeat;k++){
					int i=0;
					for (;i<255;i+=colile){
						for (I2C_Device u2 : list){
							int a	= i + 40;
							int b	= 255 - i + 1;
							u2.setColor(q2, true,  a, 0, b, 0);
							u2.setColor(q2, false, b, 0, a, 0);
						}
					}
					q2.addWait( time );
					for (i=255;i>=0;i-=colile){
						for (I2C_Device u2 : list){
							int a	= i + 40;
							int b	= 255 - i + 1;
							u2.setColor(q2, true,  a, 0, b, 0);
							u2.setColor(q2, false, b, 0, a, 0);
						}
					}
					q2.addWait( time );
				} 
				System.out.println("koniec fadeButelka");
				return q2;
			}
		});
	}

	public void linijka(final Hardware hw, final int repeat) {
		Queue q = hw.getQueue();
		hw.getQueue().addWaitThread( Main.main );
		q.addWaitThread( Main.main );
		final int time =700;
		final int colile = 20;

		q.add( new AsyncMessage( true ){		// na koncu zamknij
			@Override
			public String getName() {
				return "tecza";
			}
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				Queue q2 = new Queue();
				zgas( hw, q2 );
				Upanel[] list = hw.barobot.i2c.getUpanels();
				if(list.length == 0 ){
					System.out.println("Pusto" );
					return null;
				}
				int i=0;
				for (;i<2620;i+=10){
					int b	= 0;
				//	System.out.println("aaaa: " + a);
					list[ 0].setColor(q2, true, 0,0, flag( i + 00 ), 0);
					list[ 1].setColor(q2, true, 0,0, flag( i + 30 ), 0);
					list[ 2].setColor(q2, true, 0,0, flag( i + 60 ), 0);
					list[ 3].setColor(q2, true, 0,0, flag( i + 90 ), 0);
					list[ 4].setColor(q2, true, 0,0, flag( i + 120 ), 0);
					list[ 5].setColor(q2, true, 0,0, flag( i + 150 ), 0);	
					list[ 6].setColor(q2, true, 0,0, flag( i + 180 ), 0);
					list[ 7].setColor(q2, true, 0,0, flag( i + 210 ), 0);
					list[ 8].setColor(q2, true, 0,0, flag( i + 240 ), 0);
					list[ 9].setColor(q2, true, 0,0, flag( i + 270 ), 0);
					list[10].setColor(q2, true, 0,0, flag( i + 300 ), 0);
					list[11].setColor(q2, true, 0,0, flag( i + 310 ), 0);
				}
				q2.addWait( time );
				System.out.println("koniec fadeButelka");
				return q2;
			}
		});
	}

	public void flaga(final Hardware hw, final int repeat) {
		Queue q = hw.getQueue();
		q.addWaitThread( Main.main );
		final int time =700;
		final int colile = 20;

		q.add( new AsyncMessage( true ){		// na koncu zamknij
			@Override
			public String getName() {
				return "tecza";
			}
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				Queue q2 = new Queue();
				zgas( hw, q2 );
				Upanel[] list = hw.barobot.i2c.getUpanels();
				if(list.length == 0 ){
					System.out.println("Pusto" );
					return null;
				}
				int i=0;
				boolean top = true;
				for (;i<2620;i+=20){
					int b	= 0;
				//	System.out.println("aaaa: " + a);

					list[ 0].setColor(q2, top, flag( i + 00 ), 0, flag( i + 180 ), flag( i + 180 ));
					list[ 1].setColor(q2, top, flag( i + 30 ), 0, flag( i + 210 ), flag( i + 210 ));
					list[ 2].setColor(q2, top, flag( i + 60 ), 0, flag( i + 240 ), flag( i + 240 ));
					list[ 3].setColor(q2, top, flag( i + 90 ), 0, flag( i + 270 ), flag( i + 270 ));
					list[ 4].setColor(q2, top, flag( i + 120 ), 0, flag( i + 300 ), flag( i + 300 ));
					list[ 5].setColor(q2, top, flag( i + 150 ), 0, flag( i + 330 ), flag( i + 330 ));	
					list[ 6].setColor(q2, top, flag( i + 180 ), 0, flag( i + 00 ), flag( i + 00 ));
					list[ 7].setColor(q2, top, flag( i + 210 ), 0, flag( i + 30 ), flag( i + 30 ));
					list[ 8].setColor(q2, top, flag( i + 240 ), 0, flag( i + 60 ), flag( i + 60 ));
					list[ 9].setColor(q2, top, flag( i + 270 ), 0, flag( i + 90 ), flag( i + 90 ));
					list[10].setColor(q2, top, flag( i + 300 ), 0, flag( i + 120 ), flag( i + 120 ));
					list[11].setColor(q2, top, flag( i + 310 ), 0, flag( i + 150 ), flag( i + 150 ));		
				}
				q2.addWait( time );
				System.out.println("koniec fadeButelka");
				return q2;
			}
		});
	}

	public static int flag(int degree ){
		int a	= (int) (Math.sin( Math.toRadians(degree) ) * 127 + 127);
		return a;
	}

	public void loading(final Hardware hw, final int repeat) {
		hw.getQueue().addWaitThread( Main.main );
		Queue q = hw.getQueue();
		q.addWaitThread( Main.main );
		final int time =100;
		final int colile = 20;

		q.add( new AsyncMessage( true ){		// na koncu zamknij
			@Override
			public String getName() {
				return "tecza";
			}
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				Queue q2 = new Queue();
				zgas( hw, q2 );
				Upanel[] list = hw.barobot.i2c.getUpanels();
				if(list.length == 0 ){
					System.out.println("Pusto" );
					return null;
				}
				int i=0;
				boolean top = true;
				for (;i<10;i+=2){
					list[ 1].setColor(q2, top, 200, 0, 0, 0);	q2.addWait( time );list[ 1].setColor(q2, top, 0, 0, 0, 0);	
					list[ 3].setColor(q2, top, 200, 0, 0, 0);	q2.addWait( time );list[ 3].setColor(q2, top, 0, 0, 0, 0);	
					list[ 5].setColor(q2, top, 200, 0, 0, 0);	q2.addWait( time );list[ 5].setColor(q2, top, 0, 0, 0, 0);	
					list[ 7].setColor(q2, top, 200, 0, 0, 0);	q2.addWait( time );list[ 7].setColor(q2, top, 0, 0, 0, 0);	
					list[ 9].setColor(q2, top, 200, 0, 0, 0);	q2.addWait( time );list[ 9].setColor(q2, top, 0, 0, 0, 0);	
					list[11].setColor(q2, top, 200, 0, 0, 0);	q2.addWait( time );list[11].setColor(q2, top, 0, 0, 0, 0);	
					list[ 9].setColor(q2, top, 200, 0, 0, 0);	q2.addWait( time );list[ 9].setColor(q2, top, 0, 0, 0, 0);	
					list[ 7].setColor(q2, top, 200, 0, 0, 0);	q2.addWait( time );list[ 7].setColor(q2, top, 0, 0, 0, 0);	
					list[ 5].setColor(q2, top, 200, 0, 0, 0);	q2.addWait( time );list[ 5].setColor(q2, top, 0, 0, 0, 0);	
					list[ 3].setColor(q2, top, 200, 0, 0, 0);	q2.addWait( time );list[ 3].setColor(q2, top, 0, 0, 0, 0);	
				}
				System.out.println("koniec fadeButelka");
				return q2;
			}
		});

	}


	public void tecza(final Hardware hw, final int repeat) {
		Queue q = hw.getQueue();
		q.addWaitThread( Main.main );
		final int time =100;
		final int colile = 40;

		q.add( new AsyncMessage( true ){		// na koncu zamknij
			@Override
			public String getName() {
				return "tecza";
			}
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				Queue q2 = new Queue();
				zgas( hw, q2 );
				Upanel[] list = hw.barobot.i2c.getUpanels();
				if(list.length == 0 ){
					System.out.println("Pusto" );
					return null;
				}
				int i=0;
				boolean top = true;
				for (;i<4620;i+=40){
					int b	= 0;
					list[ 0].setColor(q2, top, flag( i + 00 ), 0, flag( i + 180 ), 0);
					list[ 1].setColor(q2, top, flag( i + 30 ), 0, flag( i + 210 ), 0);
					list[ 2].setColor(q2, top, flag( i + 60 ), 0, flag( i + 240 ), 0);
					list[ 3].setColor(q2, top, flag( i + 90 ), 0, flag( i + 270 ), 0);
					list[ 4].setColor(q2, top, flag( i + 120 ), 0, flag( i + 300 ), 0);
					list[ 5].setColor(q2, top, flag( i + 150 ), 0, flag( i + 330 ), 0);	
					list[ 6].setColor(q2, top, flag( i + 180 ), 0, flag( i + 00 ), 0);
					list[ 7].setColor(q2, top, flag( i + 210 ), 0, flag( i + 30 ), 0);
					list[ 8].setColor(q2, top, flag( i + 240 ), 0, flag( i + 60 ), 0);
					list[ 9].setColor(q2, top, flag( i + 270 ), 0, flag( i + 90 ), 0);
					list[10].setColor(q2, top, flag( i + 300 ), 0, flag( i + 120 ), 0);
					list[11].setColor(q2, top, flag( i + 310 ), 0, flag( i + 150 ), 0);		

					list[ 0].setColor(q2, false, flag( i + 00 ), 0, flag( i + 180 ), 0);
					list[ 1].setColor(q2, false, flag( i + 30 ), 0, flag( i + 210 ), 0);
					list[ 2].setColor(q2, false, flag( i + 60 ), 0, flag( i + 240 ), 0);
					list[ 3].setColor(q2, false, flag( i + 90 ), 0, flag( i + 270 ), 0);
					list[ 4].setColor(q2, false, flag( i + 120 ), 0, flag( i + 300 ), 0);
					list[ 5].setColor(q2, false, flag( i + 150 ), 0, flag( i + 330 ), 0);	
					list[ 6].setColor(q2, false, flag( i + 180 ), 0, flag( i + 00 ), 0);
					list[ 7].setColor(q2, false, flag( i + 210 ), 0, flag( i + 30 ), 0);
					list[ 8].setColor(q2, false, flag( i + 240 ), 0, flag( i + 60 ), 0);
					list[ 9].setColor(q2, false, flag( i + 270 ), 0, flag( i + 90 ), 0);
					list[10].setColor(q2, false, flag( i + 300 ), 0, flag( i + 120 ), 0);
					list[11].setColor(q2, false, flag( i + 310 ), 0, flag( i + 150 ), 0);		
				}
				System.out.println("koniec fadeButelka");
				return q2;
			}
		});
	}

	public void koniec(Hardware hw) {
		hw.getQueue().addWaitThread( Main.main );
		Queue q = hw.getQueue();
		q.addWait(5000 );
		this.zgas(hw, q);
	}

}
