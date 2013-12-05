package com.barobot.hardware;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.barobot.DebugActivity;
import com.barobot.R;
import com.barobot.R.id;
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
	// pozycje butelek, sa aktualizowane w trakcie
	private static int[] b_pos_x = {207,207, 394,394,581,581,768,768, 955,955,1142,1142,1329,1329,1516,1516};
	private static int[] b_pos_y = {90, 550, 90, 550, 90, 550, 90, 550, 90, 550, 90, 550, 90, 550, 90, 550};

	// todo - porządek z tymi wartościami 
	public static int mnoznikx = 1;
	public static int mnozniky = 1;
//	public static int neutral_pos_y = 200;
	public static boolean need_glass_fill = false;
	public static boolean need_glass_up = false;
	public static int weigh_min_diff = 20;
	public static boolean pac_enabled = false;

	//config
	private static final int SERVOZ_PAC_TIME_UP = 600;
	private static final int SERVOZ_PAC_POS = 1400;
	private static final int SERVOZ_PAC_TIME_WAIT = 400;

	public static final int SERVOZ_POUR_TIME = 5000;
	
	public static final int SERVOZ_UP_TIME = 400;
	public static final int SERVOZ_DOWN_TIME = 300;
	
	public static final int SERVOZ_UP_POS = 900;
	public static final int SERVOZ_DOWN_POS = 1600;
	
	public static final int SERVOY_FRONT_POS = 800;
	public static final int SERVOY_BACK_POS = 2080;

	public static final int DRIVER_X_SPEED = 4000;
	public static final int DRIVER_Y_SPEED = 30;
	public static final int DRIVER_Z_SPEED = 200;

	public static final int ANALOG_WAGA = 2;
	public static final int ANALOG_DIST1 = 20;
	public static final int ANALOG_DIST2 = 21;
	public static final int ANALOG_HALL = 10;

	private static String[] persistant = {
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
	public static int graph_speed	= 20;
	public static int graph_repeat	= 2;
	public static int graph_source	= 2;
	public static int graph_xsize	= 4;
	public static int graph_fps		= 10;
	public static Driver driver_x;

	public static void init( Activity app ){
		application		= app;
		myPrefs			= application.getSharedPreferences(Constant.SETTINGS_TAG, Context.MODE_PRIVATE);
		config_editor	= myPrefs.edit();
		driver_x		= new Driver();
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
	public static int getInt( String name, int def ){
		return virtualComponents.toInt(virtualComponents.get( name, ""+def ));
	}
	public static void set(String name, long value) {
		virtualComponents.set(name, "" + value );
	}
	public static void set( String name, String value ){
		if(name == "POSX"){
			Constant.log(Constant.TAG,"zapisuje posx:"+ value );	
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
	public static void hereIsBottle(int i, int posx, int posy) {
		Constant.log(Constant.TAG,"zapisuje pozycje:"+ i + " " +posx+ " " + posy );
		virtualComponents.set("BOTTLE_X_" + i, ""+posx );
		virtualComponents.set("BOTTLE_Y_" + i, ""+posy );
		Toast.makeText(application, "Zapisano ["+posx+"/"+posy+"] jako butelka " + (i+1), Toast.LENGTH_LONG).show();
	}
	// zapisz ze tutaj jest butelka o danym numerze
	public static void hereIsBottle(int i) {
		String posx		=  virtualComponents.get("POSX", "0" );	
		String posy		=  virtualComponents.get("POSY", "0" );
		Constant.log(Constant.TAG,"zapisuje pozycje:"+ i + " " +posx+ " " + posy );
		virtualComponents.set("BOTTLE_X_" + i, posx );
		virtualComponents.set("BOTTLE_Y_" + i, posy );
		Toast.makeText(application, "Zapisano ["+posx+"/"+posy+"] jako butelka " + (i+1), Toast.LENGTH_LONG).show();
	}
	public static boolean hasGlass() {
		return false;
	}

	public static void pacpac() {
		Arduino ar = Arduino.getInstance();
		ArduinoQueue q = new ArduinoQueue();	

		q.add("EX", true);
//		q.add("EY", true);
//		q.add("EZ", true);
		virtualComponents.moveZUp(q);
		q.add("Z" + virtualComponents.SERVOZ_PAC_POS+","+virtualComponents.DRIVER_Z_SPEED, true);	
		virtualComponents.moveZDown(q);
		q.add("DX", true);
//	    q.add("DY", true);
	    q.add("DZ", true);
	    q.add("GPX", true);		// get pos
	//	ar.send(q);
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
		ar.send("GPX");
	}

	public static void stop_all() {
		Arduino ar = Arduino.getInstance();
		ar.clear();
	}
	public static void moveZDown( ArduinoQueue q ) {
		moveZDown(q, false);
	}
	private static void moveZDown(ArduinoQueue q, boolean b) {
		int poszdown	=  virtualComponents.getInt("ENDSTOP_Z_MIN", SERVOZ_DOWN_POS );
		q.add("Z" + poszdown+","+virtualComponents.DRIVER_Z_SPEED, true);
	//	q.addWait( virtualComponents.SERVOZ_DOWN_TIME );	// wiec trzeba poczekać
	    q.add("DZ", true);
	}
	
	public static void moveZUp( ArduinoQueue q ) {
		moveZUp(q,false);
	}
	public static void moveZUp( ArduinoQueue q, boolean disableOnReady ) {
//		q.add("EZ", true);
		int poszup	=  virtualComponents.getInt("ENDSTOP_Z_MAX", SERVOZ_UP_POS );
		q.add("Z" + poszup+","+virtualComponents.DRIVER_Z_SPEED, true);
	//	q.addWait( virtualComponents.SERVOZ_UP_TIME );	// wiec trzeba poczekać
		if(disableOnReady){
			q.add("DZ", true);
		}
	}

	public static void moveToBottle(final int num ){
		Arduino ar			= Arduino.getInstance();
		ArduinoQueue q		= new ArduinoQueue();
		moveZDown( q );
		q.add( new rpc_message( true ) {
			@Override
			public ArduinoQueue run() {
				this.name		= "check position";
				String posx		= virtualComponents.get("POSX", "0" );		// czy ja juz jestem na tej pozycji?	
				String posy		= virtualComponents.get("POSY", "0" );
				long x 			= getBottlePosX( num );
				long y  		= getBottlePosY( num );
				if(Long.parseLong(posx) != x || Long.parseLong(posy) != y ){		// musze jechac?
					ArduinoQueue	q2	= new ArduinoQueue();
					virtualComponents.moveZDown(q2);
					virtualComponents.moveY( q2, virtualComponents.SERVOY_FRONT_POS);
					virtualComponents.moveX( q2, x);
					virtualComponents.moveY( q2, y);
					q2.add("DY", true);
					return q2;
				}
				return null;
			}
		} );
		q.add("GPX", true);
		ar.send( q );
	}

	public static void nalej(int time) {
		Arduino ar = Arduino.getInstance();
		ArduinoQueue q = new ArduinoQueue();
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
					q2.add("Z " + virtualComponents.SERVOZ_PAC_POS+","+virtualComponents.DRIVER_Z_SPEED, true);	
					q2.addWait( virtualComponents.SERVOZ_PAC_TIME_UP );
					virtualComponents.moveZDown(q2);
					return q2;
				}
				return null;
			}
		} );
		q.add("DX", true);
	    q.add("DY", true);
	    q.add("DZ", true);
	    q.add("GPX", true);
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
	public static void moveY( ArduinoQueue q, long pos ) {
		q.add("Y" + pos+ ","+virtualComponents.DRIVER_Y_SPEED, true);	
	}
	public static void moveY( ArduinoQueue q, String pos ) {
		q.add("Y" + pos+ ","+virtualComponents.DRIVER_Y_SPEED, true);	
	}
	public static void nalej() {
		virtualComponents.nalej(virtualComponents.SERVOZ_POUR_TIME);
	}
	public static void hereIsStart() {
		String posx		=  virtualComponents.get("POSX", "0" );	
		String posy		=  virtualComponents.get("POSY", "0" );
		Constant.log(Constant.TAG,"zapisuje start:" +posx+ " " + posy );
		virtualComponents.set("POS_START_X", posx );
		virtualComponents.set("POS_START_Y", posy );
		Toast.makeText(application, "Zapisano ["+posx+"/"+posy+"] jako butelka start", Toast.LENGTH_LONG).show();
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
					virtualComponents.moveY( q2, virtualComponents.SERVOY_FRONT_POS );
					virtualComponents.moveX( q2, sposx);
					virtualComponents.moveY( q2, sposy);
					return q2;
				}
				return null;
			}
		} );
		q.add("GPX", true);
		ar.send( q );
	}
}
