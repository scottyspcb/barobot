package com.barobot.isp;

import com.barobot.i2c.Carret;
import com.barobot.i2c.I2C_Device;
import com.barobot.i2c.Upanel;
import com.barobot.parser.Queue;

public class Macro {
	public void promo1(Hardware hw) {
		Queue q = hw.getQueue();
		hw.connect();
		int i = 50;

		for (I2C_Device u : Upanel.list){
			u.setLed( hw, "ff", 0 );
		}

		while( i-- >0 ){
			for (I2C_Device u : Upanel.list){
				u.setLed( hw, "80", 0 );
			}
			q.addWaitThread(Main.mt);
			Main.wait(5);

			for (I2C_Device u : Upanel.list){
				u.setLed( hw, "80", 255 );
			}
		}
		q.addWaitThread(Main.mt);

	//	boolean is = q.isBusy();
	//	hw.closeOnReady();
	}

	public void promo_carret(Hardware hw) {
		Queue q = hw.getQueue();
		hw.connect();
		Carret cc = new Carret();
	
		for (I2C_Device u : Upanel.list){
			u.setLed( hw, "ff", 0 );
		}
		int max = 20;
		int i = max;
		int repeat = 4;
		int time = 100;
		while( repeat-- >0 ){
			while( i-- >0 ){
				cc.setLed(hw, "01", 255 );
				Main.wait(time);
				cc.setLed(hw, "01", 0 );
				Main.wait(time);
			}
			cc.setLed(hw, "ff", 0 );
			q.addWaitThread(Main.mt);
/*
			i = max;
			while( i-- >0 ){
				cc.setLed(hw, "02", 255 );
				Main.wait(time);
				cc.setLed(hw, "02", 0 );
			}
			i = max;
			while( i-- >0 ){
				cc.setLed(hw, "04", 255 );
				Main.wait(time);
				cc.setLed(hw, "04", 0 );
			}
			i = max;
			while( i-- >0 ){
				cc.setLed(hw, "08", 255 );
				Main.wait(time);
				cc.setLed(hw, "08", 0 );
			}
			*/
			/*
			1	green
			2	blue
			4	white
			8	red
			*/
	
			i = max;
			while( i-- >0 ){
				cc.setLed(hw, "10", 255 );
				Main.wait(time);
				cc.setLed(hw, "10", 0 );
				Main.wait(time);
			}
	
			i = max;
			while( i-- >0 ){
				cc.setLed(hw, "20", 255 );
				Main.wait(time);
				cc.setLed(hw, "20", 0 );
				Main.wait(time);
			}
			
			
			i = max;
			while( i-- >0 ){
				cc.setLed(hw, "40", 255 );
				Main.wait(time);
				cc.setLed(hw, "40", 0 );
				Main.wait(time);
			}	

			i = max;
			while( i-- >0 ){
				cc.setLed(hw, "80", 255 );
				Main.wait(time);
				cc.setLed(hw, "80", 0 );
				Main.wait(time);
			}
			Main.wait(2000);	
		}
		q.addWaitThread(Main.mt);
	}

	public void testBpm(final Hardware hw) {
		int bpm = 235;
		Interval ii1 = new Interval(new Runnable(){
			public void run() {
				System.out.println("teraz");
				for (I2C_Device u : Upanel.list){
					u.setLed( hw, "ff", 255 );
				}
			}});
		ii1.run(100, 60 * 1000 /bpm );
		Interval ii2 = new Interval(new Runnable(){
			public void run() {
				System.out.println("teraz2");
				for (I2C_Device u : Upanel.list){
					u.setLed( hw, "ff", 0 );
				}
			}});

		ii2.run(200, 60 * 1000 /bpm );

		Main.wait(50000);
		ii1.cancel();
		ii2.cancel();
		
	}

	public void resetuj(Hardware hw) {
		Queue q = hw.getQueue();
		hw.connect();
		
		
		
		q.add("RESET2", true );
		Main.wait(50000);
		q.add("Z1000,100", true );

		

		
		
	}
}
