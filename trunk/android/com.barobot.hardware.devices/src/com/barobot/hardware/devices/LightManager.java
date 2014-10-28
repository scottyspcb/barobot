package com.barobot.hardware.devices;

import com.barobot.common.Initiator;
import com.barobot.parser.Queue;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.message.Mainboard;

public class LightManager {

	String[] defaultDemo = {"","",""};
	public void startDemo(BarobotConnector barobot) {
		Queue q = barobot.main_queue;
		LightManager lm = new LightManager();
	//	lm.tecza( barobot, q, 10 );				// ok
	//	lm.loading(barobot, q, 10);				// ok
		
		lm.linijka( barobot, q, 10, 700 );		// nudne
	//	lm.flaga( barobot, q, 10, 700 );
		lm.mrygajRGB( barobot, q, 10 , 400);
	//	lm.nazmiane( barobot, q, 10, 700 );
		
	//	lm.strobo( barobot, q, 60 );
	//	lm.zapal(barobot, q);
	}

	public void loading(final BarobotConnector barobot, final Queue q, final int repeat) {
		final int time =200;

		q.add( new AsyncMessage( true ){		// na koncu zamknij
			@Override
			public String getName() {
				return "tecza";
			}
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				Queue q2 = new Queue();
				zgas( barobot, q2 );
				boolean top = true;
				
				Initiator.logger.i( "LightsManager", "start2 loading");
				
				for (int i=0;i<repeat;i+=1){
					//barobot.driver_x.d.moveTo( q, 1000);
					//barobot.setLedsByBottle(q, 1, "01", 255, 255, 0, 0, true);
					//barobot.color_by_bottle(q2, 1, true, 255, 255, 0);
					barobot.color_by_bottle(q2, 1, true, 200, 0, 0);	q2.addWait( time );barobot.color_by_bottle(q2, 1, true, 10, 0, 0);
					barobot.color_by_bottle(q2, 3, true, 200, 0, 0);	q2.addWait( time );barobot.color_by_bottle(q2, 3, true, 10, 0, 0);
					barobot.color_by_bottle(q2, 5, true, 200, 0, 0);	q2.addWait( time );barobot.color_by_bottle(q2, 5, true, 10, 0, 0);
					barobot.color_by_bottle(q2, 7, true, 200, 0, 0);	q2.addWait( time );barobot.color_by_bottle(q2, 7, true, 10, 0, 0);
					barobot.color_by_bottle(q2, 9, true, 200, 0, 0);	q2.addWait( time );barobot.color_by_bottle(q2, 9, true, 10, 0, 0);
					barobot.color_by_bottle(q2, 11, true, 200, 0, 0);	q2.addWait( time );barobot.color_by_bottle(q2, 11, true, 10, 0, 0);
					barobot.color_by_bottle(q2, 9, true, 200, 0, 0);	q2.addWait( time );barobot.color_by_bottle(q2, 9, true, 10, 0, 0);
					barobot.color_by_bottle(q2, 7, true, 200, 0, 0);	q2.addWait( time );barobot.color_by_bottle(q2, 7, true, 10, 0, 0);
					barobot.color_by_bottle(q2, 5, true, 200, 0, 0);	q2.addWait( time );barobot.color_by_bottle(q2, 5, true, 10, 0, 0);
					barobot.color_by_bottle(q2, 3, true, 200, 0, 0);	q2.addWait( time );barobot.color_by_bottle(q2, 3, true, 10, 0, 0);
/*
					
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
*/
				}
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

		Initiator.logger.i( "LightsManager", "start nazmiane");
		q.add( new AsyncMessage( true ){
			@Override
			public String getName() {
				return "nazmiane";
			}
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				Initiator.logger.i( "LightsManager", "start2 nazmiane");
				Queue q2 = new Queue();
				barobot.turnOffLeds(q2);
				for (int k = 0;k<repeat;k++){
					int i=0;
					for (;i<255;i+=colile){
						for(int j=0;j<12;j++){
							int a	= i + 40;
							int b	= 255 - i + 1;
							barobot.color_by_bottle( q2, j, true, a, 0, b );
							barobot.color_by_bottle( q2, j, false, b, 0, a);
						}
					}
					q2.addWait( time );
					for (i=255;i>=0;i-=colile){
						for(int j=0;j<12;j++){
							int a	= i + 40;
							int b	= 255 - i + 1;
							barobot.color_by_bottle( q2, j, true, a, 0, b );
							barobot.color_by_bottle( q2, j, false, b, 0, a);
						}
					}
					q2.addWait( time );
				} 
				Initiator.logger.i( "LightsManager", "koniec fadeButelka");
				return q2;
			}
		});
	}

	public void linijka(final BarobotConnector barobot, final Queue q, final int repeat, final int time) {
		q.add( new AsyncMessage( true ){
			@Override
			public String getName() {
				return "linijka";
			}
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				Queue q2 = new Queue();
				Initiator.logger.i( "LightsManager", "start2 linijka");
				LightManager.zgas( barobot, q2 );
				for (int i=0;i<3620;i+=10){
					barobot.color_by_bottle(q2, 0, true, 0, 0, flag( i + 00 ) );
					barobot.color_by_bottle(q2, 1, true, 0, 0, flag( i + 30 ) );
					barobot.color_by_bottle(q2, 2, true, 0, 0, flag( i + 60 ) );
					barobot.color_by_bottle(q2, 3, true, 0, 0, flag( i + 90 ) );
					barobot.color_by_bottle(q2, 4, true, 0, 0, flag( i + 120 ) );
					barobot.color_by_bottle(q2, 5, true, 0, 0, flag( i + 150 ) );
					barobot.color_by_bottle(q2, 6, true, 0, 0, flag( i + 180 ) );
					barobot.color_by_bottle(q2, 7, true, 0, 0, flag( i + 210 ) );
					barobot.color_by_bottle(q2, 8, true, 0, 0, flag( i + 240 ) );
					barobot.color_by_bottle(q2, 9, true, 0, 0, flag( i + 270 ) );
					barobot.color_by_bottle(q2, 10, true, 0, 0, flag( i + 300 ) );
					barobot.color_by_bottle(q2, 11, true, 0, 0, flag( i + 310 ) );
					
					/*
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
					list[11].setColor(q2, true, 0,0, flag( i + 310 ), 0);*/
				}
				q2.addWait( time );
				Initiator.logger.i( "LightsManager", "koniec fadeButelka");
				return q2;
			}
		});
	}

	public void flaga(final BarobotConnector barobot, final Queue q, final int repeat, final int time) {
		q.add( new AsyncMessage( true ){
			@Override
			public String getName() {
				return "flaga";
			}
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				Queue q2 = new Queue();
				Initiator.logger.i( "LightsManager", "start2 flaga");
				LightManager.zgas( barobot, q2 );
				boolean top = true;
				for (int i=0;i<2620;i+=20){
				//	Initiator.logger.i( "LightsManager", "aaaa: " + a);
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

				}
				q2.addWait( time );
				Initiator.logger.i( "LightsManager", "koniec fadeButelka");
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
				Initiator.logger.i( "LightsManager", "start2 tecza");
				LightManager.zgas( barobot, q2 );
				int i=0;
				for (;i<4620;i+=40){
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

					barobot.color_by_bottle(q2, 0, false, flag( i + 00 ), 0, flag( i + 180 ));
					barobot.color_by_bottle(q2, 1, false, flag( i + 30 ), 0, flag( i + 210 ));
					barobot.color_by_bottle(q2, 2, false, flag( i + 60 ), 0, flag( i + 240 ));
					barobot.color_by_bottle(q2, 3, false, flag( i + 90 ), 0, flag( i + 270 ));
					barobot.color_by_bottle(q2, 4, false, flag( i + 120 ), 0, flag( i + 300 ));
					barobot.color_by_bottle(q2, 5, false, flag( i + 150 ), 0, flag( i + 330 ));	
					barobot.color_by_bottle(q2, 6, false, flag( i + 180 ), 0, flag( i + 00 ));
					barobot.color_by_bottle(q2, 7, false, flag( i + 210 ), 0, flag( i + 30 ));
					barobot.color_by_bottle(q2, 8, false, flag( i + 240 ), 0, flag( i + 60 ));
					barobot.color_by_bottle(q2, 9, false, flag( i + 270 ), 0, flag( i + 90 ));
					barobot.color_by_bottle(q2, 10, false, flag( i + 300 ), 0, flag( i + 120 ));
					barobot.color_by_bottle(q2, 11, false, flag( i + 310 ), 0, flag( i + 150 ));		
				}
				Initiator.logger.i( "LightsManager", "koniec fadeButelka");
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
		Initiator.logger.i( "LightsManager", "koniec zapal");
	}

	public static void zgas(BarobotConnector barobot, Queue q ) {
		barobot.turnOffLeds(q);
		Initiator.logger.i( "LightsManager", "koniec zgas");
	}

	public void mrygajRGB(BarobotConnector barobot, Queue q, int repeat, int time  ) {
		barobot.turnOffLeds(q);
		while (repeat-- > 0){

			q.addWait( time/2 );
			barobot.setAllLeds(q, "01", 255, 100, 0, 0);
			q.addWait( time/2 );
			barobot.setAllLeds(q, "01", 255, 255, 0, 0);

			q.addWait(time/2 );
			barobot.setAllLeds(q, "02", 255, 0, 100, 0);
			q.addWait( time/2 );
			barobot.setAllLeds(q, "02", 255, 0, 255, 0);

			q.addWait( time/2 );
			barobot.setAllLeds(q, "02", 255, 0, 0, 100);
			q.addWait( time/2 );
			barobot.setAllLeds(q, "02", 255, 0, 0, 255);
		}
	}
}

