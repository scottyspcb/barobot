package com.barobot.isp;


import com.barobot.hardware.devices.i2c.I2C_Device;

import com.barobot.parser.Queue;
import com.barobot.parser.utils.Interval;

public class Macro {
	public void promo1(Hardware hw) {
		Queue q = hw.getQueue();
		hw.connectIfDisconnected();
		int i = 50;
		hw.barobot.turnOffLeds(q);
		while( i-- >0 ){
			hw.barobot.setAllLeds(q, "80", 0, 0, 0, 0);
			q.addWait(5 );
			hw.barobot.setAllLeds(q, "80", 255, 255, 255, 255);
		}
		q.addWaitThread(Main.mt);

	//	boolean is = q.isBusy();
	//	hw.closeOnReady();
	}

	public void testBpm(final Hardware hw) {
		int bpm = 235;
		final Queue q = hw.getQueue();
		Interval ii1 = new Interval(new Runnable(){
			public void run() {
				System.out.println("teraz");
				hw.barobot.setAllLeds(q, "ff", 255, 255, 255, 255);
			}});
		ii1.run(100, 60 * 1000 /bpm );
		Interval ii2 = new Interval(new Runnable(){
			public void run() {
				System.out.println("teraz2");
				hw.barobot.setAllLeds(q, "ff", 0, 0, 0, 0);
			}});
		ii2.run(200, 60 * 1000 /bpm );
		q.addWait( 50000 );
		ii1.cancel();
		ii2.cancel();		
	}
}
