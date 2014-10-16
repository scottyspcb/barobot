package com.barobot.hardware.devices;

import com.barobot.hardware.devices.i2c.I2C_Device;
import com.barobot.hardware.devices.i2c.Upanel;
import com.barobot.parser.Queue;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.message.Mainboard;

public class LightManager {

	String[] defaultDemo = {"","",""};
	public void startDemo(BarobotConnector barobot) {
		Queue q = barobot.main_queue;
		LightManager lm = new LightManager();

		lm.loading(barobot, q, 10);
	//	lm.linijka( barobot, q, 10, 700 );
	//	lm.flaga( barobot, q, 10, 700 );
		lm.mrygajRGB( barobot, q, 60 , 100);
		lm.nazmiane( barobot, q, 10, 700 );
		lm.tecza( barobot, q, 10 );
		lm.strobo( barobot, q, 60 );
		lm.zapal(barobot, q);
	}

	public void loading(final BarobotConnector barobot, final Queue q, final int repeat) {
		final int time =100;

		q.add( new AsyncMessage( true ){		// na koncu zamknij
			@Override
			public String getName() {
				return "tecza";
			}
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				Queue q2 = new Queue();
				zgas( barobot, q2 );
				Upanel[] list = barobot.i2c.getUpanels();
				if(list.length == 0 ){
					System.out.println("Pusto" );
					return null;
				}
				int i=0;
				boolean top = true;
				for (;i<10;i+=2){
					//barobot.driver_x.d.moveTo( q, 1000);
			//		q2.add("X1000,"+barobot.driver_x.defaultSpeed * 1.4, false);

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
				//	q2.add("X10000,"+barobot.driver_x.defaultSpeed * 1.4, false);
				}
				System.out.println("koniec fadeButelka");
				return q2;
			}
		});
	}


	public void strobo(final BarobotConnector barobot, final Queue q, int repeat) {
		int time =5000;
		while (repeat-- > 0){
			barobot.turnOffLeds(q);
			if(time>0){
				q.addWait(	time / 100 );
			}
			barobot.setAllLeds(q, "ff", 255, 255, 255, 255);
			time=time-10;
		}
	}

	public void nazmiane(final BarobotConnector barobot, final Queue q, final int repeat,final int time ) {
		final int colile = 40;

		q.add( new AsyncMessage( true ){
			@Override
			public String getName() {
				return "tecza";
			}
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				Queue q2 = new Queue();
				LightManager.zgas( barobot, q2 );
				Upanel[] list = barobot.i2c.getUpanels();

				for (int k = 0;k<repeat;k++){
					int i=0;
					for (;i<255;i+=colile){
						for (I2C_Device u2 : list){
							int a	= i + 40;
							int b	= 255 - i + 1;
							u2.setColor(q2, true,  a, 0, b,0);
							u2.setColor(q2, false, b, 0, a,0);
						}
					}
					q2.addWait( time );
					for (i=255;i>=0;i-=colile){
						for (I2C_Device u2 : list){
							int a	= i + 40;
							int b	= 255 - i + 1;
							u2.setColor(q2, true,  a, 0, b,0);
							u2.setColor(q2, false, b, 0, a,0);
						}
					}
					q2.addWait( time );
				} 
				System.out.println("koniec fadeButelka");
				return q2;
			}
		});
	}

	public void linijka(final BarobotConnector barobot, final Queue q, final int repeat, final int time) {
		q.add( new AsyncMessage( true ){
			@Override
			public String getName() {
				return "tecza";
			}
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				Queue q2 = new Queue();
				LightManager.zgas( barobot, q2 );
				Upanel[] list = barobot.i2c.getUpanels();
				if(list.length == 0 ){
					System.out.println("Pusto" );
					return null;
				}
				int i=0;
				for (;i<2620;i+=10){
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

	public void flaga(final BarobotConnector barobot, final Queue q, final int repeat, final int time) {
		q.add( new AsyncMessage( true ){
			@Override
			public String getName() {
				return "tecza";
			}
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				Queue q2 = new Queue();
				LightManager.zgas( barobot, q2 );
				Upanel[] list = barobot.i2c.getUpanels();
				if(list.length == 0 ){
					System.out.println("Pusto" );
					return null;
				}
				int i=0;
				boolean top = true;
				for (;i<2620;i+=20){
				//	System.out.println("aaaa: " + a);

					if(barobot.newLeds){
						barobot.color_by_bottle(q2, 0, true, flag( i + 00 ), 0, flag( i + 180 ));
						barobot.color_by_bottle(q2, 1, true, flag( i + 30 ), 0, flag( i + 210 ));
						barobot.color_by_bottle(q2, 2, true, flag( i + 60 ), 0, flag( i + 240 ));
						barobot.color_by_bottle(q2, 3, true, flag( i + 90 ), 0, flag( i + 270 ));
						barobot.color_by_bottle(q2, 4, true, flag( i + 120 ), 0, flag( i + 300 ));
						barobot.color_by_bottle(q2, 5, true, flag( i + 150 ), 0, flag( i + 330 ));	
						barobot.color_by_bottle(q2, 6, true, flag( i + 180 ), 0, flag( i + 00 ));
						barobot.color_by_bottle(q2, 7, true, flag( i + 210 ), 0, flag( i + 30 ));
						barobot.color_by_bottle(q2, 8, true, flag( i + 240 ), 0, flag( i + 60 ));
						barobot.color_by_bottle(q2, 9, true, flag( i + 270 ), 0, flag( i + 90 ));
						barobot.color_by_bottle(q2, 10, true, flag( i + 300 ), 0, flag( i + 120 ));
						barobot.color_by_bottle(q2, 11, true, flag( i + 310 ), 0, flag( i + 150 ));		
					}else{
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
				}
				q2.addWait( time );
				System.out.println("koniec fadeButelka");
				return q2;
			}
		});
	}

	public void tecza(final BarobotConnector barobot, final Queue q, final int repeat) {
		q.add( new AsyncMessage( true ){
			@Override
			public String getName() {
				return "tecza";
			}
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				Queue q2 = new Queue();
				LightManager.zgas( barobot, q2 );
				Upanel[] list = barobot.i2c.getUpanels();
				if(list.length == 0 ){
					System.out.println("Pusto" );
					return null;
				}
				int i=0;
				boolean top = true;
				for (;i<4620;i+=40){
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

	public static int flag(int degree ){
		int a	= (int) (Math.sin( Math.toRadians(degree) ) * 127 + 127);
		return a;
	}

	public void zapal(BarobotConnector barobot, Queue q) {		
		barobot.setAllLeds(q, "ff", 255, 255, 255, 255);
		System.out.println("koniec zapal");
	}

	public static void zgas(BarobotConnector barobot, Queue q ) {
		barobot.turnOffLeds(q);
		System.out.println("koniec zgas");
	}
	

	public void mrygajRGB(BarobotConnector barobot, Queue q, int repeat, int time  ) {
		while (repeat-- > 0){
			barobot.turnOffLeds(q);
			q.addWait(time );
			barobot.setAllLeds(q, "01", 255, 255, 0, 0);
			q.addWait(time );
			barobot.setAllLeds(q, "02", 255, 0, 255, 0);
			q.addWait(time );
			barobot.setAllLeds(q, "02", 255, 0, 0, 255);
			q.addWait(time );
		}
	}
}

