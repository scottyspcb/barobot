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
	public static int mnoznikx = 10;
	public static int mnozniky = 10;
//	public static int neutral_pos_y = 200;
	public static boolean need_glass_fill = false;
	public static boolean need_glass_up = false;
	public static int weigh_min_diff = 20;
	public static boolean pac_enabled = true;

	//config
	private static final int SERVOZ_DOWN_POS = 900;
	private static final int SERVOZ_PAC_TIME_DOWN = 1000;
	private static final int SERVOZ_PAC_TIME_UP = 600;
	private static final int SERVOZ_PAC_POS = 1900;
	private static final int SERVOZ_PAC_TIME_WAIT = 400;

	public static final int SERVOZ_UP_TIME = 700;
	public static final int SERVOZ_DOWN_TIME = 600;
	
	public static final int ANALOG_WAGA = 2;
	public static final int ANALOG_DIST1 = 20;
	public static final int ANALOG_DIST2 = 21;
	public static final int ANALOG_HALL = 10;

	private static String[] persistant = {
		"LENGTHX","LENGTHY","LENGTHZ","LAST_BT_DEVICE",
		"NEUTRAL_POS_Y",
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
		"BOTTLE_X_12","BOTTLE_Y_12",
		"BOTTLE_X_13","BOTTLE_Y_13",
		"BOTTLE_X_14","BOTTLE_Y_14",
		"BOTTLE_X_15","BOTTLE_Y_15",
	};
	public static int graph_speed	= 20;
	public static int graph_repeat	= 2;
	public static int graph_source	= 2;
	public static int graph_xsize	= 4;
	public static int graph_fps		= 10;

	public static void init( Activity app ){
		application		= app;
		myPrefs			= application.getSharedPreferences(Constant.SETTINGS_TAG, Context.MODE_PRIVATE);
		config_editor	= myPrefs.edit();
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
	public static void set( String name, String value ){
		hashmap.put(name, value );
		virtualComponents.update( name, value );

		int remember = Arrays.asList(persistant).indexOf(name);			// czy zapisac w configu tą wartosc?
		if(remember > -1){
			config_editor.putString(name, value);
			config_editor.commit();
		}
	}
	public static int toInt( String input ){
		input = input.replaceAll( "[^\\d]", "" );
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
		q.add("EZ", true);
		q.add("SET Z MAX", true);		// SET Z zwraca początek operacji a nie koniec
		q.addWait( virtualComponents.SERVOZ_UP_TIME );	// wiec trzeba poczekać
		q.addWait( virtualComponents.SERVOZ_PAC_TIME_WAIT );
		q.add("SET Z " + virtualComponents.SERVOZ_PAC_POS, true);	
		q.addWait( virtualComponents.SERVOZ_PAC_TIME_UP );
		q.add("SET Z " + virtualComponents.SERVOZ_DOWN_POS, true);
		q.addWait( virtualComponents.SERVOZ_PAC_TIME_DOWN );
		q.add("SET Z MAX", true);		// SET Z zwraca początek operacji a nie koniec
		q.addWait( virtualComponents.SERVOZ_UP_TIME );	// wiec trzeba poczekać
		q.add("DX", true);
//	    q.add("DY", true);
	    q.add("DZ", true);
	    q.add("GET CARRET", true);
		ar.send(q);
	}

	public static void cancel_all() {
		Arduino ar = Arduino.getInstance();
		ar.clear();
		ar.send("LIVE A OFF");
		ar.send("EZ");
		ar.send("SET Z MAX");		// SET Z zwraca początek operacji a nie koniec
		ar.send("DX");
	    ar.send("DY");
		ar.send("DZ");
		ar.send("GET CARRET");
	}

	public static void stop_all() {
		Arduino ar = Arduino.getInstance();
		ar.clear();
	}
	public static void moveZDown( ArduinoQueue q ) {
		q.add("EZ", true);
		q.add("SET Z MIN", true);		// SET Z zwraca początek operacji a nie koniec
		q.addWait( virtualComponents.SERVOZ_DOWN_TIME );	// wiec trzeba poczekać
	    q.add("DZ", true);
	}
	public static void moveToBottle(final int num ) {
		int time			= 2000;
		Arduino ar			= Arduino.getInstance();
		ArduinoQueue q		= new ArduinoQueue();
		String autofill		= virtualComponents.get("AUTOFILL", "0" );
		moveZDown( q );

		q.add( new rpc_message( true ) {
			@Override
			public ArduinoQueue run() {
				this.name		= "check position";
				String posx		= virtualComponents.get("POSX", "0" );		// czy ja juz jestem na tej pozycji?	
				String posy		= virtualComponents.get("POSY", "0" );
				long x 			= getBottlePosX( num );
				long y  		= getBottlePosY( num );
				if(Long.parseLong(posx) != x || Long.parseLong(posy) != y ){		// musze jechac
					ArduinoQueue	q2	= new ArduinoQueue();
					q2.add("Y" + virtualComponents.get("NEUTRAL_POS_Y", "0" ), true );
					q2.add("X" + x, true);
					q2.add("Y" + y, true);
					return q2;
				}
				return null;
			}
		} );

		if( autofill== "1"){
			q.add("EX", true);
			q.add("EY", true);
			q.add("EZ", true);
			q.addWaitGlass();
			q.add("SET Z MAX", true);		// SET Z zwraca początek operacji a nie koniec
			q.addWait( virtualComponents.SERVOZ_UP_TIME );	// wiec trzeba poczekać

			q.addWait( time );			// czekaj na nalanie
			q.add("SET Z MIN", true);		// SET Z zwraca początek operacji a nie koniec
			q.addWait( virtualComponents.SERVOZ_DOWN_TIME );	// wiec trzeba poczekać
			q.add( new rpc_message( true ) {
				@Override
				public ArduinoQueue run() {
					this.name		= "pacpac";
					if(virtualComponents.pac_enabled){
						ArduinoQueue	q2	= new ArduinoQueue();
						q2.addWait( virtualComponents.SERVOZ_PAC_TIME_WAIT );
						q2.add("SET Z " + virtualComponents.SERVOZ_PAC_POS, true);	
						q2.addWait( virtualComponents.SERVOZ_PAC_TIME_UP );
						q2.add("SET Z " + virtualComponents.SERVOZ_DOWN_POS, true);
						q2.addWait( virtualComponents.SERVOZ_PAC_TIME_DOWN );
						q2.add("SET Z MIN", true);		// SET Z zwraca początek operacji a nie koniec
						q2.addWait( virtualComponents.SERVOZ_DOWN_TIME );	// wiec trzeba poczekać
						return q2;
					}
					return null;
				}
			} );
			q.add("DX", true);
		    q.add("DY", true);
		    q.add("DZ", true);
		}
		q.add("GET CARRET", true);
		ar.send( q );
	}

	public static void enable_analog( Arduino ar, int pin, int time, int repeat) {
		ar.send("LIVE A "+pin+","+time+","+repeat);		// repeat pomiary co time na porcie pin
	}
	public static void disable_analog(Arduino ar, int analogWaga) {
		ar.send("LIVE A OFF");
	}
	public static void nalej(int time) {
		Arduino ar = Arduino.getInstance();
		ArduinoQueue q = new ArduinoQueue();
		q.addWaitGlass();
		q.add("EX", true);
//		q.add("EY", true);	
		q.add("EZ", true);
		q.add("SET Z MAX", true);		// SET Z zwraca początek operacji a nie koniec
		q.addWait( virtualComponents.SERVOZ_UP_TIME );	// wiec trzeba poczekać

		q.addWait( time );
		q.add("SET Z MIN", true);		// SET Z zwraca początek operacji a nie koniec
		q.addWait( virtualComponents.SERVOZ_DOWN_TIME );	// wiec trzeba poczekać
		q.add( new rpc_message( true ) {
			@Override
			public ArduinoQueue run() {
				this.name		= "pacpac";
				if(virtualComponents.pac_enabled){
					ArduinoQueue	q2	= new ArduinoQueue();	
					q2.addWait( virtualComponents.SERVOZ_PAC_TIME_WAIT );
					q2.add("SET Z " + virtualComponents.SERVOZ_PAC_POS, true);	
					q2.addWait( virtualComponents.SERVOZ_PAC_TIME_UP );
					q2.add("SET Z " + virtualComponents.SERVOZ_DOWN_POS, true);
					q2.addWait( virtualComponents.SERVOZ_PAC_TIME_DOWN );
					q2.add("SET Z MIN", true);		// SET Z zwraca początek operacji a nie koniec
					q2.addWait( virtualComponents.SERVOZ_DOWN_TIME );	// wiec trzeba poczekać
					return q2;
				}
				return null;
			}
		} );
		q.add("DX", true);
//	    q.add("DY", true);
	    q.add("DZ", true);
	    q.add("GET CARRET", true);
	    ar.send(q);
	}
}
