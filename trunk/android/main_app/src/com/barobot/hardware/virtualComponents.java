package com.barobot.hardware;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.barobot.R;
import com.barobot.R.id;
import com.barobot.activity.DebugActivity;
import com.barobot.utils.ArduinoQueue;
import com.barobot.utils.Constant;
import com.barobot.utils.Arduino;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

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
	private static final int SERVOZ_PAC_POS = 1100;
	private static final int SERVOZ_PAC_TIME_WAIT = 400;

	public static final int SERVOZ_POUR_TIME = 4000;
	
	public static final int SERVOZ_UP_TIME = 400;
	public static final int SERVOZ_DOWN_TIME = 300;
	
	public static final int SERVOZ_UP_POS = 900;
	public static final int SERVOZ_DOWN_POS = 1600;
	public static final int SERVOZ_TEST_POS = 1650;
	
	public static final int SERVOY_FRONT_POS = 800;
	public static final int SERVOY_BACK_POS = 2090;
	public static final int SERVOY_TEST_POS = 1000;
	public static final int SERVOY_BACK_NEUTRAL = 1800;
	
	public static final int DRIVER_X_SPEED = 4000;
	public static final int DRIVER_Y_SPEED = 40;
	public static final int DRIVER_Z_SPEED = 200;

	public static final int ANALOG_WAGA = 2;
	public static final int ANALOG_DIST1 = 20;
	public static final int ANALOG_DIST2 = 21;
	public static final int ANALOG_HALL = 10;
	public static final int SERVOY_REPEAT_TIME = 2000;

	public static int margin_front = 0;
	public static int margin_back = 0;

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
		300,
		0,
		300,
		0,
		300,
		0,
		300,
		0, 
		300,
		0,
		300,
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
		"POSX","POSY","POSY",
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
	public static boolean scann_bottles = false;
	public static int scann_num = 0;
	public static boolean set_bottle_on = false;

	public static void init( Activity app ){
		application		= app;
		myPrefs			= application.getSharedPreferences(Constant.SETTINGS_TAG, Context.MODE_PRIVATE);
		config_editor	= myPrefs.edit();
		driver_x		= new MotorDriver();

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
		return virtualComponents.toInt(virtualComponents.get( name, ""+def ));
	}
	public static void set(String name, long value) {
		virtualComponents.set(name, "" + value );
	}
	public static void set( String name, String value ){
		if(name == "POSX"){
	//		Constant.log(Constant.TAG,"zapisuje posx:"+ value );	
		}
		hashmap.put(name, value );
		virtualComponents.update( name, value );

		int remember = Arrays.asList(persistant).indexOf(name);			// czy zapisac w configu tą wartosc?
		if(remember > -1){
			config_editor.putString(name, value);
			config_editor.commit();
		}
	}
	public static int toInt( String input ){
		input = input.replaceAll( "[^-\\d]", "" );
		int res;
		try {
			res = Integer.parseInt(input);
		} catch (NumberFormatException e) {
			return 0;
		}
		return res;
	}
	public static long getBottlePosX( int i ) {
		return virtualComponents.getInt("BOTTLE_X_" + i, b_pos_x[i]);
	}
	public static long getBottlePosY( int i ) {
		return virtualComponents.getInt("BOTTLE_Y_" + i, b_pos_y[i]);
	}
	private static void update(String name, String value) {
		final DebugActivity dialog = DebugActivity.getInstance();
		if(dialog!=null){
			dialog.update(name, value );
		}
	}
	// zapisz ze tutaj jest butelka o danym numerze
	public static void hereIsBottle(int i, long posx, long posy) {
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
		Arduino ar = Arduino.getInstance();
		ArduinoQueue q = new ArduinoQueue();	

		Constant.log(Constant.TAG,"pac");

		q.add("EX", true);
//		q.add("EY", true);
//		q.add("EZ", true);
		q.add("Z" + virtualComponents.SERVOZ_PAC_POS+","+virtualComponents.DRIVER_Z_SPEED, true);
		
		Constant.log("pacpac","Z" + virtualComponents.SERVOZ_PAC_POS+","+virtualComponents.DRIVER_Z_SPEED);

		virtualComponents.moveZDown(q);
		q.add("DX", true);
		q.addWait(200);
		q.add("DZ", true);
		ar.send(q);
	}

	public static void cancel_all() {
		Arduino ar = Arduino.getInstance();
		ar.clear();
		ar.send("LIVE A OFF");
//		ar.send("EZ");
		int poszdown	=  virtualComponents.getInt("ENDSTOP_Z_MIN", SERVOZ_DOWN_POS );
		ar.send("Z" + poszdown);		// zwraca początek operacji a nie koniec
		ar.send("DX");
	    ar.send("DY");
		ar.send("DZ");
		ar.send(Constant.GETXPOS);
	}
	public static void stop_all() {
		Arduino ar = Arduino.getInstance();
		ar.clear();
	}

	public static void moveToBottle(final int num, final boolean disableOnReady ){
		Arduino ar			= Arduino.getInstance();
		ArduinoQueue q		= new ArduinoQueue();
		moveZDown( q );
		q.add( new rpc_message( true ) {
			@Override
			public ArduinoQueue run() {
				this.name		= "check position";
				String posx		= virtualComponents.get("POSX", "0" );		// czy ja juz jestem na tej pozycji?	
				String posy		= virtualComponents.get("POSY", "0" );
				long tx 		= getBottlePosX( num );
				long ty  		= getBottlePosY( num );
				long cx  		= Long.parseLong(posx);
				long cy  		= Long.parseLong(posy);

				if(cx == tx && cy == ty ){		// nie musze jechac
					ArduinoQueue	q2	= new ArduinoQueue();
					q2.addWait( virtualComponents.SERVOY_REPEAT_TIME );
					return q2;
				}else if(cx != tx && cy == ty ){		// jade tylem lub przodem
					ArduinoQueue	q2	= new ArduinoQueue();
					virtualComponents.moveZDown(q2, disableOnReady );
					if( cy > virtualComponents.SERVOY_BACK_NEUTRAL ){
						virtualComponents.moveY( q2, virtualComponents.SERVOY_BACK_NEUTRAL, true);
					}else{
						virtualComponents.moveY( q2, virtualComponents.SERVOY_FRONT_POS, true);	
					}
					virtualComponents.moveX( q2, tx);
					virtualComponents.moveY( q2, ty, disableOnReady);
					return q2;		
				}else{
					ArduinoQueue	q2	= new ArduinoQueue();
					virtualComponents.moveZDown(q2, disableOnReady );
					virtualComponents.moveY( q2, virtualComponents.SERVOY_FRONT_POS, true);
					virtualComponents.moveX( q2, tx);
					virtualComponents.moveY( q2, ty, disableOnReady);
					return q2;
				}
			}
		} );
		q.add(Constant.GETXPOS, true);
		ar.send( q );
	}

	public static void nalej(int num) {			// num 0-11
		Arduino ar = Arduino.getInstance();
		ArduinoQueue q = new ArduinoQueue();
		int time = getPourTime(num);
		q.addWaitGlass();
		q.add("EX", true);
//		q.add("EY", true);	
//		q.add("EZ", true);
		virtualComponents.moveZUp(q, false);
		q.addWait( time );
		virtualComponents.moveZDown(q,false);
		q.add( new rpc_message( true ) {
			@Override
			public ArduinoQueue run() {
				this.name		= "pacpac";
				if(virtualComponents.pac_enabled){
					ArduinoQueue	q2	= new ArduinoQueue();	
					q2.addWait( virtualComponents.SERVOZ_PAC_TIME_WAIT );
					q2.add("Z " + virtualComponents.SERVOZ_PAC_POS+",250", true);	
					virtualComponents.moveZDown(q2);
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
	    ar.send(q);
	}

	public static void enable_analog( Arduino ar, int pin, int time, int repeat) {
		ar.send("LIVE A "+pin+","+time+","+repeat);		// repeat pomiary co time na porcie pin
	}
	public static void disable_analog(Arduino ar, int analogWaga) {
		ar.send("LIVE A OFF");
	}
	public static void moveX( ArduinoQueue q, long pos ) {
		pos = driver_x.soft2hard(pos);
		q.add("X" + pos+ ","+virtualComponents.DRIVER_X_SPEED, true);	
	}
	public static void moveX( ArduinoQueue q, String pos ) {
		moveX(q, toInt(pos));
	}
	public static void moveY( ArduinoQueue q, long pos, boolean disableOnReady ) {
		q.add("Y" + pos+ ","+virtualComponents.DRIVER_Y_SPEED, true);
		if(disableOnReady){
			q.add("DY", true );
		}
	}
	public static void moveY( ArduinoQueue q, String pos ) {
		q.add("Y" + pos+ ","+virtualComponents.DRIVER_Y_SPEED, true);	
	}
	public static void hereIsStart( long posx, long posy) {
		//Constant.log(Constant.TAG,"zapisuje start:" +posx+ " " + posy );
		virtualComponents.set("POS_START_X", posx );
		virtualComponents.set("POS_START_Y", posy );
	}
	public static void moveZDown( ArduinoQueue q ) {
		moveZDown(q, true);
	}
	private static void moveZ(ArduinoQueue q, int pos) {
		q.add("Z" + pos +","+virtualComponents.DRIVER_Z_SPEED, true);
		q.addWait(100);
	}
	private static void moveZDown(ArduinoQueue q, boolean b) {
		int poszdown	=  virtualComponents.getInt("ENDSTOP_Z_MIN", SERVOZ_DOWN_POS );
		moveZ(q, poszdown );
	    q.add("DZ", true);
	}
	public static void moveZUp( ArduinoQueue q, boolean disableOnReady ) {
//		q.add("EZ", true);
		int poszup	=  virtualComponents.getInt("ENDSTOP_Z_MAX", SERVOZ_UP_POS );
		q.add("Z" + poszup+","+virtualComponents.DRIVER_Z_SPEED, true);
	//	q.addWait( virtualComponents.SERVOZ_UP_TIME );	// wiec trzeba poczekać
		if(disableOnReady){
			q.addWait(100);
			q.add("DZ", true);
		}
	}

	public static void moveToStart() {
		Arduino ar		= Arduino.getInstance();
		ArduinoQueue q	= new ArduinoQueue();
		moveZDown( q );
		q.add( new rpc_message( true ) {
			@Override
			public ArduinoQueue run() {
				this.name		= "check position";
				long posx		= virtualComponents.getInt("POSX", 0 );		// czy ja juz jestem na tej pozycji?	
				long posy		= virtualComponents.getInt("POSY", 0 );
				long sposx		= virtualComponents.getInt("POS_START_X", 0 );		// tu mam byc
				long sposy		= virtualComponents.getInt("POS_START_X", 0 );

				if(posx != sposx || posy != sposy ){		// musze jechac?
					ArduinoQueue	q2	= new ArduinoQueue();
					virtualComponents.moveZDown(q2);
					//virtualComponents.moveY( q2, virtualComponents.get("NEUTRAL_POS_Y", "0" ));
					virtualComponents.moveY( q2, virtualComponents.SERVOY_FRONT_POS, false );
					virtualComponents.moveX( q2, sposx);
					virtualComponents.moveY( q2, sposy, true );
					return q2;
				}
				return null;
			}
		} );
		q.add(Constant.GETXPOS, true);
		ar.send( q );
	}
	public static void kalibrcja() {
		Arduino ar		= Arduino.getInstance();
		ArduinoQueue q	= new ArduinoQueue();
		int posx		= virtualComponents.getInt("POSX", 0 );
		int posy		= virtualComponents.getInt("POSY", 0 );

		Constant.log("+find_bottles", "start");
		q.add("EX", true );
		virtualComponents.moveZDown( q );
		virtualComponents.moveZ( q, virtualComponents.SERVOZ_TEST_POS );
		virtualComponents.moveZDown( q );
		virtualComponents.moveY( q, virtualComponents.SERVOY_TEST_POS, true);		
		virtualComponents.moveY( q, virtualComponents.SERVOY_FRONT_POS, true);
		long lengthx19	=  virtualComponents.getInt("LENGTHX", 60000 );	
		virtualComponents.moveX( q, posx + 2000);
		virtualComponents.moveX( q, -70000 );	// read margin
		// scann Triggers
		q.add( new rpc_message( true ) {
			@Override
			public ArduinoQueue run() {
				this.name		= "scanning up";
				Constant.log("+find_bottles", "up");
				virtualComponents.scann_bottles = true;
				virtualComponents.scann_num = 0;
				return null;
			}
		} );
		virtualComponents.moveX( q, 70000 );
		q.add( new rpc_message( true ) {
			@Override
			public ArduinoQueue run() {
				this.name		= "scanning back";

				Constant.log("+find_bottles", "down na:" + virtualComponents.scann_num);
				virtualComponents.scann_num = 1;
				return null;
			}
		} );
		virtualComponents.moveX( q, -lengthx19);	
		q.add( new rpc_message( true ) {
			@Override
			public ArduinoQueue run() {
				this.name		= "end scanning";
				Constant.log("+find_bottles", "koniec na:" + virtualComponents.scann_num);
				virtualComponents.scann_bottles = false;
				return null;
			}
		} );
		ar.send(q);
	}
}
