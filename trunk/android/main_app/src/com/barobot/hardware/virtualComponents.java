package com.barobot.hardware;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.barobot.AppInvoker;
import com.barobot.activity.BarobotMain;
import com.barobot.activity.DebugActivity;
import com.barobot.common.Initiator;
import com.barobot.common.constant.Constant;
import com.barobot.common.constant.Methods;
import com.barobot.parser.Queue;
import com.barobot.parser.devices.MotorDriver;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.output.AsyncDevice;
import com.barobot.parser.utils.Decoder;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
public class virtualComponents {
	public static final int WITHGLASS = 111;
	public static final int WITHOUTGLASS = 222;

	public static Activity application;
	private static SharedPreferences myPrefs;
	private static SharedPreferences.Editor config_editor;			// config systemu android
	private static Map<String, String> hashmap = new HashMap<String, String>();
	public static boolean is_ready = false;

	// todo - porządek z tymi wartościami 
	public static int mnoznikx = 1;
	public static int mnozniky = 1;
//	public static int neutral_pos_y = 200;
	public static boolean need_glass_fill = false;
	public static boolean need_glass_up = false;
	public static int weigh_min_diff = 20;
	public static boolean pac_enabled = true;

	//config
	private static final int SERVOZ_PAC_TIME_UP = 600;
	private static final int SERVOZ_PAC_POS = 1800;
	private static final int SERVOZ_PAC_TIME_WAIT = 400;

	public static final int SERVOZ_POUR_TIME = 4000;
	
	public static final int SERVOZ_UP_TIME = 400;
	public static final int SERVOZ_DOWN_TIME = 300;

	public static final int SERVOZ_UP_POS = 2100;
	public static final int SERVOZ_DOWN_POS = 1100;
	public static final int SERVOZ_TEST_POS = 1300;

	public static final int SERVOY_FRONT_POS = 800;
	public static final int SERVOY_BACK_POS = 2100;
	public static final int SERVOY_TEST_POS = 1000;
	public static final int SERVOY_BACK_NEUTRAL = 1800;
	
	public static final int DRIVER_X_SPEED = 2500;
	public static final int DRIVER_Y_SPEED = 40;
	public static final int DRIVER_Z_SPEED = 250;

	public static final int ANALOG_WAGA = 2;
	public static final int ANALOG_DIST1 = 20;
	public static final int ANALOG_DIST2 = 21;
	public static final int ANALOG_HALL = 10;
	public static final int SERVOY_REPEAT_TIME = 2000;

	public static int margin_front = 0;
	public static int margin_back = 0;

	public static int[] upanels = {
		23,					// 0, num 1
		16,					// 1, num 2
		19,					// 2, num 3
		12,					// 3, num 4
		17,					// 4, num 5
		18,					// 5, num 6
		20,					// 6, num 7
		15,					// 7, num 8
		22,					// 8, num 9
		21,					// 9, num 10
		14,					// 10, num 11
		13,					// 11, num 12
	};

	// pozycje butelek, sa aktualizowane w trakcie
	public static int[] times = {
		SERVOZ_POUR_TIME,		
		SERVOZ_POUR_TIME, 
		SERVOZ_POUR_TIME,
		SERVOZ_POUR_TIME,
		SERVOZ_POUR_TIME,
		SERVOZ_POUR_TIME,
		SERVOZ_POUR_TIME,
		SERVOZ_POUR_TIME, 
		SERVOZ_POUR_TIME,
		SERVOZ_POUR_TIME,
		SERVOZ_POUR_TIME*2,
		SERVOZ_POUR_TIME
	};

	// pozycje butelek, sa aktualizowane w trakcie
	public static int[] margin_x = {
		0,
		0,
		0,
		0,
		0,
		0,
		0,
		0, 
		0,
		0,
		0,
		0
	};

	private static int[] b_pos_x = {207,207, 394,394,581,581,768,768, 955,955,1142,1142};
	public static int[] b_pos_y = {
		SERVOY_BACK_POS,					// 0, num 1
		SERVOY_FRONT_POS,					// 1, num 2
		SERVOY_BACK_POS,					// 2, num 3
		SERVOY_FRONT_POS,					// 3, num 4
		SERVOY_BACK_POS,					// 4, num 5
		SERVOY_FRONT_POS,					// 5, num 6
		SERVOY_BACK_POS,					// 6, num 7
		SERVOY_FRONT_POS,					// 7, num 8
		SERVOY_BACK_POS,					// 8, num 9
		SERVOY_FRONT_POS,					// 9, num 10
		SERVOY_BACK_POS,					// 10, num 11
		SERVOY_FRONT_POS,					// 11, num 12
	};

	public static int[] magnet_order = {0,2,1,4,3,6,5,8,7,10,9,11 };	// numer butelki, odjąc 1 aby numer ID
	private static String[] persistant = {
		"POSX",
		"POSY",
		"POSY",
		"X_GLOBAL_MIN",
		"X_GLOBAL_MAX",
		"LENGTHX","LAST_BT_DEVICE",
		"POS_START_X",
		"POS_START_Y",
		"NEUTRAL_POS_Y",
		"NEUTRAL_POS_Z",
		"ENDSTOP_X_MIN",
		"ENDSTOP_X_MAX",
		"ENDSTOP_Y_MIN",
		"ENDSTOP_Y_MAX",
		"ENDSTOP_Z_MIN",
		"ENDSTOP_Z_MAX",
		"BOTTLE_X_0","BOTTLE_Y_0",
		"BOTTLE_X_1","BOTTLE_Y_1",
		"BOTTLE_X_2","BOTTLE_Y_2",
		"BOTTLE_X_3","BOTTLE_Y_3",
		"BOTTLE_X_4","BOTTLE_Y_4",
		"BOTTLE_X_5","BOTTLE_Y_5",
		"BOTTLE_X_6","BOTTLE_Y_6",
		"BOTTLE_X_7","BOTTLE_Y_7",
		"BOTTLE_X_8","BOTTLE_Y_8",
		"BOTTLE_X_9","BOTTLE_Y_9",
		"BOTTLE_X_10","BOTTLE_Y_10",
		"BOTTLE_X_11","BOTTLE_Y_11",
	};

	public static MotorDriver driver_x;
	public static boolean scann_bottles = true;
	public static int scann_num = 0;
	public static boolean set_bottle_on = false;
	public static boolean ledsReady = false;
	private static Carret carret;
	
	public static void init( Activity app ){
		application			= app;
		myPrefs				= application.getSharedPreferences(Constant.SETTINGS_TAG, Context.MODE_PRIVATE);
		config_editor		= myPrefs.edit();
		driver_x			= new MotorDriver();
		carret				= new Carret( 2, 10 );
		driver_x.defaultSpeed = virtualComponents.DRIVER_X_SPEED;
		driver_x.setSPos( virtualComponents.getInt( "POSX", 0 ) );
	}
	public static String get( String name, String def ){
		String ret = hashmap.get(name);
		if( ret == null ){ 
			if((Arrays.asList(persistant).indexOf(name) > -1 )){
				ret = myPrefs.getString(name, def );
			}else{
				ret = def;
			}
		}
		return ret;
	}
	public static int getPourTime( int num ){
		if( num > 0 && num < times.length){
			return times[num];
		}
		return SERVOZ_POUR_TIME;
	}
	
	public static int getInt( String name, int def ){
		return Decoder.toInt(virtualComponents.get( name, ""+def ));
	}
	public static void set(String name, long value) {
		virtualComponents.set(name, "" + value );
	}
	public static void set( String name, String value ){
	//	if(name == "POSX"){
			AppInvoker.log("virtualComponents.set","save: "+name + ": "+ value );	
	//	}
		hashmap.put(name, value );
		virtualComponents.update( name, value );

		int remember = Arrays.asList(persistant).indexOf(name);			// czy zapisac w configu tą wartosc?
		if(remember > -1){
			config_editor.putString(name, value);
			config_editor.commit();
		}
	}
	public static int getBottlePosX( int i ) {
		return virtualComponents.getInt("BOTTLE_X_" + i, b_pos_x[i]);
	}
	public static int getBottlePosY( int i ) {
		return virtualComponents.getInt("BOTTLE_Y_" + i, b_pos_y[i]);
	}
	private static void update(String name, String value) {
		final DebugActivity dialog = DebugActivity.getInstance();
		if(dialog!=null){
			dialog.update(name, value );
		}
	}
	// zapisz ze tutaj jest butelka o danym numerze
	public static void hereIsBottle(int i, int posx, int posy) {
		//Constant.log(Constant.TAG,"zapisuje pozycje:"+ i + " " +posx+ " " + posy );
		virtualComponents.set("BOTTLE_X_" + i, ""+posx );
		virtualComponents.set("BOTTLE_Y_" + i, ""+posy );
	}
	// zapisz ze tutaj jest butelka o danym numerze
	public static void hereIsBottle(int i) {
		String posx		=  virtualComponents.get("POSX", "0" );	
		String posy		=  virtualComponents.get("POSY", "0" );
	//	Constant.log(Constant.TAG,"zapisuje pozycje:"+ i + " " +posx+ " " + posy );
		virtualComponents.set("BOTTLE_X_" + i, posx );
		virtualComponents.set("BOTTLE_Y_" + i, posy );
	}
	public static boolean hasGlass() {
		return false;
	}
	public static void pacpac() {
		Queue q = Arduino.getInstance().getMainQ();
		AppInvoker.log(Constant.TAG,"pac");
/*
		AsyncMessage moveX = new AsyncMessage(true){
			@Override
			public long getTimeout() {
				return AsyncMessage.DEFAULT_TIME;
			}
			@Override
			public void onException(String input) {
			}

			@Override
			public Queue run(AsyncDevice dev) {
				dev.send("X1000,1000");
				return null;
			}

			@Override
			public boolean wait4Finish() {
				return true;
			}

			@Override
			public boolean isRet(String result) {
				return false;
			}

			@Override
			public boolean onInput(String input) {
				return false;
			}

			@Override
			public void afterTimeout() {
				AppInvoker.log("pacpac","afterTimeout");
			}
		};*/
	//	q.add( moveX );
		q.add("EX", true);
//		q.add("EY", true);
//		q.add("EZ", true);
		q.add("Z" + virtualComponents.SERVOZ_PAC_POS+","+virtualComponents.DRIVER_Z_SPEED, true);
		AppInvoker.log("pacpac","Z" + virtualComponents.SERVOZ_PAC_POS+","+virtualComponents.DRIVER_Z_SPEED);
		virtualComponents.moveZDown(q, true );
		q.add("DX", true);
		q.addWait(200);
		q.add("DZ", true);
	}

	public static void cancel_all() {
		Queue mq = Arduino.getInstance().getMainQ();
		mq.clearAll();
		mq.add("LIVE A OFF", false );
//		add("EZ");
		int poszdown	=  virtualComponents.getInt("ENDSTOP_Z_MIN", SERVOZ_DOWN_POS );
		mq.add("Z" + poszdown, false );		// zwraca początek operacji a nie koniec
		mq.add("DX", false );
		mq.add("DY", false );
		mq.add("DZ", false );
		mq.add(Constant.GETXPOS, false );
	}
	public static void stop_all() {
		Queue mq = Arduino.getInstance().getMainQ();
		mq.clearAll();
	}

	public static void moveToBottle(final int num, final boolean disableOnReady ){
		Arduino ar		= Arduino.getInstance();
		Queue q			= new Queue();
		moveZDown( q ,true );
		q.add( new AsyncMessage( true ) {
			@Override
			public Queue run(AsyncDevice dev) {
				this.name	= "check position";
				int cx		= virtualComponents.driver_x.getSPos();		// czy ja juz jestem na tej pozycji?	
				int cy		= virtualComponents.getInt("POSY", 0 );
				int tx 		= getBottlePosX( num );
				int ty  	= getBottlePosY( num );
				Queue	q2	= new Queue();

				Upanel up =  getUpanelBottle(num);
				if( up != null ){
					up.setLed( q2, "ff", 0 );
					up.setLed( q2, "11", 200 );
				}
				if(cx == tx && cy == ty ){		// nie musze jechac
					q2.addWait( virtualComponents.SERVOY_REPEAT_TIME );
				}else if(cx != tx && cy == ty ){		// jade tylem lub przodem
					virtualComponents.moveZDown(q2, disableOnReady );
					if( cy > virtualComponents.SERVOY_BACK_NEUTRAL ){
						virtualComponents.moveY( q2, virtualComponents.SERVOY_BACK_NEUTRAL, true);
					}else{
						virtualComponents.moveY( q2, virtualComponents.SERVOY_FRONT_POS, true);	
					}
					virtualComponents.moveX( q2, tx);
					virtualComponents.moveY( q2, ty, disableOnReady);		
				}else{
					virtualComponents.moveZDown(q2, disableOnReady );
					virtualComponents.moveY( q2, virtualComponents.SERVOY_FRONT_POS, true);
					virtualComponents.moveX( q2, tx);
					virtualComponents.moveY( q2, ty, disableOnReady);
				}
				if( up != null ){
					up.setLed( q2, "ff", 0 );
					up.setLed( q2, "22", 200 );
				}
				return q2;
			}
		} );
	    q.add(Constant.GETXPOS, true);
	    q.add(Constant.GETYPOS, true);
	    q.add(Constant.GETZPOS, true);
	    ar.getMainQ().add(q);
	}

	private static Upanel getUpanelBottle(int num) {
		if( num > 0 && num < upanels.length ){
			int addr = upanels[ num ];
			return new Upanel( 0 , addr);
		}
		return null;
	}
	public static void nalej(int num) {			// num 0-11
		Queue q = Arduino.getInstance().getMainQ();
		int time = getPourTime(num);
		q.add("EX", true);
//		q.add("EY", true);	
//		q.add("EZ", true);
		virtualComponents.moveZUp(q, false);
		q.addWait( time );
		virtualComponents.moveZDown(q,false);
		q.add( new AsyncMessage( true ) {
			@Override
			public Queue run(AsyncDevice dev) {
				this.name		= "pacpac";
				if(virtualComponents.pac_enabled){
					Queue	q2	= new Queue();	
					q2.addWait( virtualComponents.SERVOZ_PAC_TIME_WAIT );
					q2.add("Z" + virtualComponents.SERVOZ_PAC_POS+",250", true);	
					virtualComponents.moveZDown(q2, true );
					return q2;
				}
				return null;
			}
		} );
	//	q.add("DX", true);
	    q.add("DY", true);
	    q.addWait(100);
	    q.add("DZ", true);
	    q.add(Constant.GETXPOS, true);
	    q.add(Constant.GETYPOS, true);
	    q.add(Constant.GETZPOS, true);
	}

	public static void enable_analog( Queue q, int pin, int time, int repeat) {
		q.add("LIVE A "+pin+","+time+","+repeat, false);		// repeat pomiary co time na porcie pin
	}
	public static void disable_analog(Queue q, int analogWaga) {
		q.add("LIVE A OFF", false);
	}

	public static void moveX( final Queue q, int pos ) {
		final int newx		= driver_x.soft2hard(pos);
		final int currentx	= driver_x.getSPos();

		q.add( new AsyncMessage( true, true ) {
			@Override
			public boolean isRet(String result) {
				return false;
			}
			@Override
			public Queue run(AsyncDevice dev) {
				this.name		= "Check Hall X";
				q.sendNow(Queue.DFAULT_DEVICE, "A0");
				return null;
			}
			@Override
			public boolean onInput(String input, AsyncDevice dev) {
				Initiator.logger.w("MotorDriver.movoTo.AsyncMessage.onInput", input );
				if(input.matches("^" +  Methods.METHOD_IMPORTANT_ANALOG + ",0,.*" )){		//	224,0,66,0,208,7,15,2
					int[] parts = Decoder.decodeBytes( input );
					boolean can = true;
					if( parts[2] == Methods.HX_STATE_9 ){		// this is max	//	224,0,100,0,204,3,185,1
						if(newx < currentx ){		// move backward
							can = false;
							Initiator.logger.w("MotorDriver.movoTo.AsyncMessage.onInput", "OVER max" );
						}
					}else if( parts[2] == Methods.HX_STATE_1 ){		// this is min
						if( newx > currentx ){		// move forward
							can = false;
							Initiator.logger.w("MotorDriver.movoTo.AsyncMessage.onInput", "BELOW MIN" );
						}
					}
					if( can ){
						Initiator.logger.w("MotorDriver.movoTo.AsyncMessage.onInput", "MOVE" );
						Queue	q2	= new Queue();
						q2.add("X" + newx+ ","+virtualComponents.DRIVER_X_SPEED, true);
						q.addFirst(q2);
						dev.unlockRet(this, "A0 OK");
						return true;
					}
				}
				return false;
			}
		} );
	}
	public static void moveX( Queue q, String pos ) {
		moveX(q, Decoder.toInt(pos));
	}
	public static void moveY( Queue q, int pos, boolean disableOnReady ) {
		q.add("Y" + pos+ ","+virtualComponents.DRIVER_Y_SPEED, true);
		if(disableOnReady){
			q.add("DY", true );
		}
	}
	public static void moveY( Queue q, String pos ) {
		q.add("Y" + pos+ ","+virtualComponents.DRIVER_Y_SPEED, true);	
	}
	public static void hereIsStart( int posx, int posy) {
		//Constant.log(Constant.TAG,"zapisuje start:" +posx+ " " + posy );
		virtualComponents.set("POS_START_X", posx );
		virtualComponents.set("POS_START_Y", posy );
	}
	public static void moveZDown(Queue q, boolean b) {
		int poszdown	=  virtualComponents.getInt("ENDSTOP_Z_MIN", SERVOZ_DOWN_POS );
		moveZ(q, poszdown );
	    q.add("DZ", true);
	}
	private static void moveZ(Queue q, int pos) {
		q.add("Z" + pos +","+virtualComponents.DRIVER_Z_SPEED, true);
		q.addWait(300);
	}

	public static void moveZUp( Queue q, boolean disableOnReady ) {
//		q.add("EZ", true);
		int poszup	=  virtualComponents.getInt("ENDSTOP_Z_MAX", SERVOZ_UP_POS );
		q.add("Z" + poszup+","+virtualComponents.DRIVER_Z_SPEED, true);
	//	q.addWait( virtualComponents.SERVOZ_UP_TIME );	// wiec trzeba poczekać
		if(disableOnReady){
			q.addWait(300);
			q.add("DZ", true);
		}
	}

	public static void moveToStart() {
		Queue q = Arduino.getInstance().getMainQ();
		moveZDown( q ,true );
		q.add( new AsyncMessage( true ) {
			@Override
			public Queue run(AsyncDevice dev) {
				this.name		= "check position";
				int posx		= driver_x.getSPos();;		// czy ja juz jestem na tej pozycji?	
				int posy		= virtualComponents.getInt("POSY", 0 );
				int sposx		= virtualComponents.getInt("POS_START_X", 0 );		// tu mam byc
				int sposy		= virtualComponents.getInt("POS_START_X", 0 );

				if(posx != sposx || posy != sposy ){		// musze jechac?
					Queue	q2	= new Queue();
					virtualComponents.moveZDown(q2, true );
					//virtualComponents.moveY( q2, virtualComponents.get("NEUTRAL_POS_Y", "0" ));
					virtualComponents.moveY( q2, virtualComponents.SERVOY_FRONT_POS, true );
					virtualComponents.moveX( q2, sposx);
					virtualComponents.moveY( q2, sposy, true );
					return q2;
				}
				return null;
			}
		} );
	    q.add(Constant.GETXPOS, true);
	    q.add(Constant.GETYPOS, true);
	    q.add(Constant.GETZPOS, true);

		carret.setLed( q, "ff", 0 );
		carret.setLed( q, "22", 250 );
	}
	
	public static void startDoingDrink() {
		Queue q = Arduino.getInstance().getMainQ();
		carret.setLed( q, "ff", 0 );
		carret.setLed( q, "11", 250 );
		Queue q1		= new Queue();	
		for(int i =0; i<upanels.length;i++){
			q1.add("L"+ upanels[i] +",ff,0", true);
			q1.add("L"+ upanels[i] +",ff,200", true);
		}
		q.add(q1);
	}

	public static void kalibrcja() {
		Queue q			= Arduino.getInstance().getMainQ();
		int posx		= driver_x.getSPos();
		int posy		= virtualComponents.getInt("POSY", 0 );

		AppInvoker.log("+find_bottles", "start");
		q.add("EX", true );
		virtualComponents.moveZDown( q ,true );
		virtualComponents.moveZ( q, virtualComponents.SERVOZ_TEST_POS );
		virtualComponents.moveZDown( q ,true );
		virtualComponents.moveY( q, virtualComponents.SERVOY_TEST_POS, true);
		virtualComponents.moveY( q, virtualComponents.SERVOY_FRONT_POS, true);
		int lengthx19	=  virtualComponents.getInt("LENGTHX", 60000 );	
		virtualComponents.moveX( q, posx + 2000);
		virtualComponents.moveX( q, -70000 );	// read margin
		// scann Triggers
		q.add( new AsyncMessage( true ) {			// go up
			@Override
			public Queue run(AsyncDevice dev) {
				this.name		= "scanning up";
				AppInvoker.log("+find_bottles", "up");
				virtualComponents.scann_bottles = true;
				virtualComponents.scann_num = 0;
				return null;
			}
		} );
		virtualComponents.moveX( q, 70000 );		// go down

		q.add( new AsyncMessage( true ) {
			@Override
			public Queue run(AsyncDevice dev) {
				this.name		= "scanning back";
				AppInvoker.log("+find_bottles", "down na:" + virtualComponents.scann_num);
				virtualComponents.scann_num = 1;
				return null;
			}
		} );
		virtualComponents.moveX( q, -lengthx19);			// down to 0
		q.add( new AsyncMessage( true ) {
			@Override
			public Queue run(AsyncDevice dev) {
				this.name		= "end scanning";
				AppInvoker.log("+find_bottles", "koniec na:" + virtualComponents.scann_num);
				virtualComponents.scann_bottles = false;
				if( virtualComponents.scann_num != 12 ){
					BarobotMain.getInstance().showError();
				}
				return null;
			}
		} );
		virtualComponents.scann_leds();
	}
	public static void scann_leds() {
		Queue q1			= new Queue();
		Queue q2			= new Queue();		
		for(int i =0; i<upanels.length;i++){
			q1.add("L"+ upanels[i] +",ff,200", true);
			q2.add("L"+ upanels[i] +",ff,0", true);
		}
		Queue q			= Arduino.getInstance().getMainQ();
		q.add(q1);
		q.addWait(1000);
		q.add(q2);
		ledsReady = true;	
	}
	public static void saveXPos(int spos) {
		if( spos < 100 ){
			spos = 0;
		}
		virtualComponents.set( "POSX", "" + spos);
		driver_x.setSPos( spos );
		
	}
}
