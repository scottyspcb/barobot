package com.barobot.isp;

import com.barobot.hardware.devices.i2c.Carret;
import com.barobot.hardware.devices.i2c.I2C_Device;
import com.barobot.hardware.devices.i2c.Upanel;
import com.barobot.parser.Queue;
import com.barobot.parser.utils.Interval;

public class Macro {
	public void promo1(Hardware hw) {
		Queue q = hw.getQueue();
		hw.connectIfDisconnected();
		int i = 50;

		I2C_Device[] list = hw.barobot.i2c.getDevices();
		for (I2C_Device u : list){
			u.addLed( q, "ff", 0 );
		}

		while( i-- >0 ){
			for (I2C_Device u : list){
				u.addLed( q, "80", 0 );
			}
			q.addWait(5 );
			for (I2C_Device u : list){
				u.addLed( q, "80", 255 );
			}
		}
		q.addWaitThread(Main.mt);

	//	boolean is = q.isBusy();
	//	hw.closeOnReady();
	}

	public void promo_carret(Hardware hw) {
		Queue q = hw.getQueue();
		hw.connectIfDisconnected();
		Carret cc = hw.barobot.i2c.carret;

		Upanel[] list = hw.barobot.i2c.getUpanels();
		for (I2C_Device u : list){
			u.addLed( q, "ff", 0 );
		}
		int max = 20;
		int i = max;
		int repeat = 4;
		int time = 100;
		while( repeat-- >0 ){
			while( i-- >0 ){
				cc.addLed(q, "01", 255 );
				q.addWait(time );
				cc.addLed(q, "01", 0 );
				q.addWait(time );
			}
			cc.addLed(q, "ff", 0 );
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
				cc.addLed(q, "10", 255 );
				q.addWait(time );
				cc.addLed(q, "10", 0 );
				q.addWait(time );
			}
	
			i = max;
			while( i-- >0 ){
				cc.addLed(q, "20", 255 );
				q.addWait(time );
				cc.addLed(q, "20", 0 );
				q.addWait(time );
			}

			i = max;
			while( i-- >0 ){
				cc.addLed(q, "40", 255 );
				q.addWait(time );
				cc.addLed(q, "40", 0 );
				q.addWait(time );
			}	

			i = max;
			while( i-- >0 ){
				cc.addLed(q, "80", 255 );
				q.addWait(time );
				cc.addLed(q, "80", 0 );
				q.addWait(time );
			}
		}
		q.addWaitThread(Main.mt);
	}

	public void testBpm(final Hardware hw) {
		int bpm = 235;
		final Upanel[] list = hw.barobot.i2c.getUpanels();
		final Queue q = hw.getQueue();
		Interval ii1 = new Interval(new Runnable(){
			public void run() {
				System.out.println("teraz");
				for (I2C_Device u : list){
					u.addLed( q, "ff", 255 );
				}
			}});
		ii1.run(100, 60 * 1000 /bpm );
		Interval ii2 = new Interval(new Runnable(){
			public void run() {
				System.out.println("teraz2");
				for (I2C_Device u : list){
					u.addLed( q, "ff", 0 );
				}
			}});

		ii2.run(200, 60 * 1000 /bpm );


		q.addWait( 50000 );

		ii1.cancel();
		ii2.cancel();		
	}

}
