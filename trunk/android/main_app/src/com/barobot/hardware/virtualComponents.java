package com.barobot.hardware;

import com.barobot.android.AndroidBarobotState;
import com.barobot.common.Initiator;
import com.barobot.common.constant.Constant;
import com.barobot.common.interfaces.HardwareState;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.hardware.devices.LedOrder;
import com.barobot.hardware.devices.i2c.Upanel;
import com.barobot.parser.Queue;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.message.Mainboard;
import android.app.Activity;
import android.graphics.Color;

public class virtualComponents {
	public static boolean need_glass_up = false;
	public static boolean pac_enabled = true;
	public static final int SERVOY_REPEAT_TIME = 2000;
	public static boolean scann_bottles = false;
	public static boolean set_bottle_on = false;
	public static boolean ledsReady = false;
	public static HardwareState state= null;
	public static BarobotConnector barobot;

	public static void init( Activity app ){
		state			= new AndroidBarobotState(app);	
		barobot			= new BarobotConnector( state );	
		state.set("show_unknown", 1 );
		state.set("show_sending", 1 );
		state.set("show_reading", 1 );
	}

	public static int getPourTime( int num ){
		if( num > 0 && num < BarobotConnector.capacity.length){
			int capacity	= BarobotConnector.capacity[ num ];
			return capacity * BarobotConnector.SERVOZ_POUR_TIME;
		}
		return BarobotConnector.SERVOZ_POUR_TIME;
	}

	public static int getBottlePosX( int i ) {
		return state.getInt("BOTTLE_X_" + i, 0 );
	}

	public static int getBottlePosY( int i ) {
		return state.getInt("BOTTLE_Y_" + i, 0 );
	}

	// zapisz ze tutaj jest butelka o danym numerze
	public static void hereIsBottle(int i, int posx, int posy) {
		//Initiator.logger.i(Constant.TAG,"zapisuje pozycje:"+ i + " " +posx+ " " + posy );
		state.set("BOTTLE_X_" + i, posx );
		state.set("BOTTLE_Y_" + i, posy );
	}
	// zapisz ze tutaj jest butelka o danym numerze
	public static void hereIsBottle(int i) {
		int posx		=  barobot.driver_x.getSPos();
		int posy		=  state.getInt("POSY", 0 );
	//	Initiator.logger.i(Constant.TAG,"zapisuje pozycje:"+ i + " " +posx+ " " + posy );
		state.set("BOTTLE_X_" + i, posx );
		state.set("BOTTLE_Y_" + i, posy );
	}
	public static boolean hasGlass() {
		return false;
	}
	public static void pacpac() {
		Queue q = getMainQ();
		Initiator.logger.i(Constant.TAG,"pac");
	//	q.add( moveX );
		q.add("EX", true);
//		q.add("EY", true);
//		q.add("EZ", true);
		q.add("Z" + BarobotConnector.SERVOZ_PAC_POS+","+BarobotConnector.DRIVER_Z_SPEED, true);
		Initiator.logger.i("pacpac","Z" + BarobotConnector.SERVOZ_PAC_POS+","+BarobotConnector.DRIVER_Z_SPEED);
		virtualComponents.moveZDown(q, true );
		q.add("DY", true);
		q.add("DX", true);
		q.addWait(200);
		q.add("DZ", true);
	}

	public static void cancel_all() {
		Queue mq = getMainQ();
		mq.clear();
		mq.add("LIVE A OFF", false );
//		add("EZ");
		int poszdown	=  state.getInt("ENDSTOP_Z_MIN", BarobotConnector.SERVOZ_DOWN_POS );
		mq.add("Z" + poszdown, false );		// zwraca początek operacji a nie koniec
		mq.add("DX", false );
		mq.add("DY", false );
		mq.add("DZ", false );
		mq.add(Constant.GETXPOS, false );
	}
	public static void moveToBottle(final int num, final boolean disableOnReady ){
		Queue q			= new Queue();
		final Upanel up	= barobot.i2c.getUpanelByBottle(num);
		moveZDown( q ,true );
		q.add( new AsyncMessage( true ){
			@Override	
			public String getName() {
				return "moveToBottle" ;
			}
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				this.name	= "check position";
				int cx		= virtualComponents.barobot.driver_x.getSPos();		// czy ja juz jestem na tej pozycji?	
				int cy		= state.getInt("POSY", 0 );
				int tx 		= getBottlePosX( num );
				int ty  	= getBottlePosY( num );
				Queue	q2	= new Queue();
				if( up != null ){
					up.setLed( q2, "ff", 0 );
					up.setLed( q2, "11", 200 );
				}
				Initiator.logger.i("moveToBottle","(cx == tx && cy == ty)("+cx+" == "+tx+" && "+cy+" == "+ty+")");
				if(cx == tx && cy == ty ){		// nie musze jechac
					q2.addWait( virtualComponents.SERVOY_REPEAT_TIME );
				}else if(cx != tx && cy == ty ){		// jade tylem lub przodem
					virtualComponents.moveZDown(q2, disableOnReady );
					if( cy > BarobotConnector.SERVOY_BACK_NEUTRAL ){
						virtualComponents.moveY( q2, BarobotConnector.SERVOY_BACK_NEUTRAL, true);
					}else{
						virtualComponents.moveY( q2, BarobotConnector.SERVOY_FRONT_POS, true);	
					}
					barobot.driver_x.moveTo( q2, tx);
					virtualComponents.moveY( q2, ty, disableOnReady);		
				}else{	// jade przodem
					virtualComponents.moveZDown(q2, disableOnReady );
					virtualComponents.moveY( q2, BarobotConnector.SERVOY_FRONT_POS, true);
					barobot.driver_x.moveTo( q2, tx);
					virtualComponents.moveY( q2, ty, disableOnReady);
				}
				if( up != null ){
					up.setLed( q2, "ff", 0 );
					up.setLed( q2, "44", 200 );
				}
				return q2;
			}
		} );
		//q.add("DY", true);
	    q.add(Constant.GETXPOS, true);
	    q.add(Constant.GETYPOS, true);
	    q.add(Constant.GETZPOS, true);
	    getMainQ().add(q);
	}

	public static void nalej(int num) {			// num 0-11
		Queue q = getMainQ();
		int time = getPourTime(num);
		final Upanel up	= barobot.i2c.getUpanelByBottle(num);
		q.add("EX", true);
//		q.add("EY", true);
//		q.add("EZ", true);
		virtualComponents.moveZUp(q, false);
		if( up == null ){
			q.addWait( time/4 );
			virtualComponents.moveZLight(q, false);
			q.add("DY", true);
			q.addWait( 3* time/4 );
		}else{
			up.setLed( q, "ff", 0 );
			up.setLed( q, "04", 110 );
			q.addWait( time/4 );
			virtualComponents.moveZLight(q, false);
			q.add("DY", true);
			up.setLed( q, "04", 0 );
			q.addWait( time/4 );
			up.setLed( q, "04", 110 );
			q.addWait( time/4 );
			up.setLed( q, "04", 0 );
			q.addWait( time/4 );
			up.setLed( q, "20", 255 );
			up.setLed( q, "80", 100 );
		}
		q.add("DY", true);
		virtualComponents.moveZDown(q,false);
		q.add( new AsyncMessage( true ) {
			@Override	
			public String getName() {
				return "pac pac" ;
			}
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				if(virtualComponents.pac_enabled){
					Queue	q2	= new Queue();		
					if( up != null ){
						up.setLed( q2, "11", 100 );
					}
					q2.addWait( BarobotConnector.SERVOZ_PAC_TIME_WAIT );
					q2.add("Z" + BarobotConnector.SERVOZ_PAC_POS+",255", true);	
					virtualComponents.moveZDown(q2, true );
					if( up != null ){
						up.setLed( q2, "11", 0 );
					}
					return q2;
				}
				return null;
			}
		} );
		q.add("DX", true);
	    q.add("DY", true);
	    q.addWait(100);
	    q.add("DZ", true);
	    q.add(Constant.GETXPOS, true);
	    q.add(Constant.GETYPOS, true);
	    q.add(Constant.GETZPOS, true);
	    if( up != null ){
			up.setLed( q, "ff", 0 );
		}
	}
	public static void enable_analog( Queue q, int pin, int time, int repeat) {
		q.add("LIVE A "+pin+","+time+","+repeat, false);		// repeat pomiary co time na porcie pin
	}
	public static void disable_analog(Queue q, int analogWaga) {
		q.add("LIVE A OFF", false);
	}
	public static void moveY( Queue q, int pos, boolean disableOnReady ) {
		q.add("Y" + pos+ ","+BarobotConnector.DRIVER_Y_SPEED, true);
		if(disableOnReady){
			q.add("DY", true );
		}
	}
	public static void moveY( Queue q, String pos ) {
		q.add("Y" + pos+ ","+BarobotConnector.DRIVER_Y_SPEED, true);	
	}
	public static void hereIsStart( int posx, int posy) {
		//Initiator.logger.i(Constant.TAG,"zapisuje start:" +posx+ " " + posy );
		state.set("POS_START_X", posx );
		state.set("POS_START_Y", posy );
	}
	public static void moveZDown(Queue q, boolean disableOnReady) {
		int poszdown	=  state.getInt("ENDSTOP_Z_MIN", BarobotConnector.SERVOZ_DOWN_POS );
		moveZ(q, poszdown );
		q.add("DZ", true);
	}
	private static void moveZ(Queue q, int pos) {
		q.add("Z" + pos +","+BarobotConnector.DRIVER_Z_SPEED, true);
		q.addWait(300);
	}

	private static void moveZLight(Queue q, boolean disableOnReady) {
//		q.add("EZ", true);
		int poszup	=  BarobotConnector.SERVOZ_UP_LIGHT_POS;
		q.add("Z" + poszup+","+BarobotConnector.DRIVER_Z_SPEED, true);
	//	q.addWait( virtualComponents.SERVOZ_UP_TIME );	// wiec trzeba poczekać
		if(disableOnReady){
			q.addWait(300);
			q.add("DZ", true);
		}
	}
	public static void moveZUp( Queue q, boolean disableOnReady ) {
//		q.add("EZ", true);
		int poszup	=  BarobotConnector.SERVOZ_UP_POS;
		q.add("Z" + poszup+","+BarobotConnector.DRIVER_Z_SPEED, true);
	//	q.addWait( virtualComponents.SERVOZ_UP_TIME );	// wiec trzeba poczekać
		if(disableOnReady){
			q.addWait(300);
			q.add("DZ", true);
		}
	}

	public static void moveToStart() {
		Queue q = getMainQ();
		moveZDown( q ,true );
		q.add( new AsyncMessage( true ) {
			@Override	
			public String getName() {
				return "moveToStart" ;
			}
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				this.name		= "check position";
				int posx		= barobot.driver_x.getSPos();;		// czy ja juz jestem na tej pozycji?	
				int posy		= state.getInt("POSY", 0 );
				int sposx		= state.getInt("POS_START_X", 0 );		// tu mam byc
				int sposy		= state.getInt("POS_START_X", 0 );

				if(posx != sposx || posy != sposy ){		// musze jechac?
					Queue	q2	= new Queue();
					virtualComponents.moveZDown(q2, true );
					//virtualComponents.moveY( q2, virtualComponents.get("NEUTRAL_POS_Y", "0" ));
					virtualComponents.moveY( q2, BarobotConnector.SERVOY_FRONT_POS, true );
					barobot.driver_x.moveTo( q2, sposx);
					virtualComponents.moveY( q2, sposy, true );
					return q2;
				}
				return null;
			}
		} );
	    q.add(Constant.GETXPOS, true);
	    q.add(Constant.GETYPOS, true);
	    q.add(Constant.GETZPOS, true);

	    barobot.i2c.carret.setLed( q, "ff", 0 );
	    barobot.i2c.carret.setLed( q, "22", 250 );
	    virtualComponents.setLeds( "ff", 0 );
		Queue q1			= new Queue();
		Upanel[] up 		= barobot.i2c.getUpanels();
		for(int i =up.length-1; i>=0;i--){
			up[i].setLed(q1, "22", 200);
			q1.addWait(100);
			up[i].setLed(q1, "22", 0);
		}
		q.add(q1);
		q.addWait(100);
	    virtualComponents.setLeds( "88", 100 );
	    virtualComponents.setLeds( "22", 200 );
		q.addWait(200);
		barobot.i2c.carret.setLed( q, "22", 20 );
		Queue q2			= new Queue();
		for(int i =up.length-1; i>=0;i--){
			up[i].setLed(q1, "88", 200);
			up[i].setLed(q1, "04", 50);
			up[i].setLed(q1, "10", 50);
			up[i].setLed(q1, "08", 50);
		}
		q.add(q2);
		q.addWait(500);
		barobot.i2c.carret.setLed( q, "22", 250 );
	}
	
	public static void startDoingDrink() {
		Queue q = getMainQ();
		barobot.i2c.carret.setLed( q, "ff", 0 );
		barobot.i2c.carret.setLed( q, "11", 250 );
		Queue q1		= new Queue();	
		Upanel[] up		= barobot.i2c.getUpanels();
		for(int i =0; i<up.length;i++){
			up[i].setLed(q1, "ff", 0);
			up[i].setLed(q1, "ff", 200);
		}
		q.add(q1);
	}

	public static void kalibrcja() {
		Queue q			= getMainQ();
		q.add( "\n", false );
		q.add( "\n", false );
		virtualComponents.setLeds( "ff", 0 );
		int posx		= barobot.driver_x.getSPos();
		for(int i=0;i<12;i++){
			state.set("BOTTLE_X_" + i, "0" );
			state.set("BOTTLE_Y_" + i, "0" );
		}
		state.set("POS_START_X", "0" );
		state.set("POS_START_Y", "0" );

		Initiator.logger.i("+find_bottles", "start");
		q.add("EX", true );
		virtualComponents.moveZDown( q ,true );
		q.addWait(100);
		virtualComponents.moveZ( q, BarobotConnector.SERVOZ_TEST_POS );
		q.addWait(100);
		virtualComponents.moveZDown( q ,true );
		q.addWait(200);
		virtualComponents.moveY( q, BarobotConnector.SERVOY_TEST_POS, true);
		q.addWait(200);
		virtualComponents.moveY( q, BarobotConnector.SERVOY_FRONT_POS, true);
		q.addWait(200);
		int lengthx19	=  state.getInt("LENGTHX", 60000 );	
		
		Initiator.logger.i("+find_bottles", "up");
		barobot.driver_x.moveTo( q, posx + 2000);
		q.addWait(100);
		barobot.driver_x.moveTo( q, -70000 );

		q.addWait(100);
		// scann Triggers
		q.add( new AsyncMessage( true ) {			// go up
			@Override	
			public String getName() {
				return "kalibrcja" ;
			}
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				this.name		= "scanning up";
				virtualComponents.barobot.driver_x.defaultSpeed = 1000;
				Initiator.logger.i("+find_bottles", "up");
				virtualComponents.scann_bottles = true;
				return null;
			}
		});
		barobot.driver_x.moveTo( q, 30000);		// go down
		q.add( new AsyncMessage( true ) {
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				this.name		= "scanning back";
				virtualComponents.barobot.driver_x.defaultSpeed = BarobotConnector.DRIVER_X_SPEED;
				Initiator.logger.i("+find_bottles", "down kalibrcja");
				return null;
			}
		} );
		barobot.driver_x.moveTo( q, -lengthx19);
		q.add( new AsyncMessage( true ) {
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				this.name		= "end scanning";
				Initiator.logger.i("+find_bottles", "koniec kalibrcja");
				virtualComponents.scann_bottles = false;
				boolean error = false;
				for(int i=0;i<12;i++){
					int xpos = state.getInt("BOTTLE_X_" + i, 0 );
					int ypos = state.getInt("BOTTLE_Y_" + i, 0 );
					if(xpos ==0 || ypos == 0 ){
						error = true;
					}
				}
				if(error){
					Initiator.logger.i("+find_bottles", "show error");
				//	BarobotMain.getInstance().showError();
				}
				return null;
			}
		} );
		//virtualComponents.scann_leds();
	}
	public static void scann_leds(){
		Queue q			= getMainQ();
		LedOrder lo = new LedOrder();
		lo.asyncStart(barobot, q);
		q.add( new AsyncMessage( true ){
			@Override	
			public String getName() {
				return "onReady LedOrder" ;
			}
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				Upanel[] up		= barobot.i2c.getUpanels();
				for(int i =0; i<up.length;i++){
					Upanel uu = up[i];
					System.out.println("+Upanel "
							+ "dla butelki: " + uu.getBottleNum() 
							+ " w wierszu " + uu.getRow()
							+ " pod numerem " + uu.getNumInRow()
							+ " o indeksie " + uu.getRow()
							+ " ma adres " + uu.getAddress() );
				}
				barobot.i2c.rememberStructure();
				ledsReady	= true;
				Queue q3	= new Queue();
				Queue q1	= new Queue();
				Queue q2	= new Queue();
				for(int i =0; i<up.length;i++){
					up[i].setLed(q1, "ff", 200);
					up[i].setLed(q2, "ff", 0);
				}
				q3.add(q1);
				q3.addWait(500);
				q3.add(q2);
				return q3;
			}
		});
	}
	public static Queue getMainQ() {
		return virtualComponents.barobot.main_queue;
	}
	public static void setLeds(String string, int value ) {
		Queue q1		= new Queue();	
		Upanel[] up		= barobot.i2c.getUpanels();
		for(int i =0; i<up.length;i++){
			up[i].setLed(q1, "ff", 0);
			up[i].setLed(q1, string, value);
		}
		Queue q			= getMainQ();
		q.add(q1);
	}
	public static void setLedsOff(String string ) {
		Queue q1		= new Queue();	
		Upanel[] up		= barobot.i2c.getUpanels();
		for(int i =0; i<up.length;i++){
			up[i].setLed(q1, "ff", 0);
		}
		Queue q			= getMainQ();
		q.add(q1);
	}

	public static void setColor(String leds, int color) {
		int blue	= Color.blue(color);
    	int red		= Color.red(color);
    	int green	= Color.green(color);
    	int white	= 0;
		Queue q1	= new Queue();
		Upanel[] up = barobot.i2c.getUpanels();
		for(int i =0; i<up.length;i++){
			up[i].setRgbW(q1, red,green,blue,white);
		}
		Queue q			= getMainQ();
		q.add(q1);
	}
}
