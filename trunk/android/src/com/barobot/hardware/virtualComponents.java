package com.barobot.hardware;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.barobot.DebugWindow;
import com.barobot.R;
import com.barobot.R.id;
import com.barobot.utils.Constant;
import com.barobot.utils.queue;

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
	public static void moveToBottle(int i) {
		String autofill = virtualComponents.get("AUTOFILL", "0" );
		queue q = queue.getInstance();
		if( autofill== "1"){
			moveZDown();
			q.add("SET Y " + virtualComponents.get("NEUTRAL_POS_Y", "0" ), true );
			long x  =  getBottlePosX( i );
			long y  =  getBottlePosY( i );
			q.add("SET X " + x, true);
			q.add("SET Y " + y, true);

			q.add("ENABLEX", true);
			q.add("ENABLEY", true);
			q.add("ENABLEZ", true);

			if(virtualComponents.need_glass_up){
				q.add("WAIT GLASS " + virtualComponents.weigh_min_diff, true);
			}
			q.add("SET Z MAX", true);
			q.add("WAIT TIME " + i, true);
			
			q.add("SET Z MIN", true);
			if(virtualComponents.pac_enabled){
				q.add("PACPAC", true);
			}
			q.add("DISABLEX", true);
		    q.add("DISABLEY", true);
		    q.add("DISABLEZ", true);
		    q.add("GET CARRET", true);
		    q.send();

		}else{
			moveZDown();
			q.add("SET Y " + virtualComponents.get("NEUTRAL_POS_Y", "0" ), true );
			long x  =  getBottlePosX( i );
			long y  =  getBottlePosY( i );
			q.add("SET X " + x, true);
			q.add("SET Y " + y, true);
			q.send();
		}
	}
	public static void moveZDown() {
		queue q = queue.getInstance();
		q.add("ENABLEZ", true);
		q.add("SET Z MIN", true );
	    q.add("DISABLEZ", true);	
	}

	private static void update(String name, String value) {
		final DebugWindow dialog = DebugWindow.getInstance();
		if( "LENGTHX".equals(name)){
			dialog.setText( R.id.dlugosc_x, value, false );
			final int val = virtualComponents.toInt( value );
			application.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					SeekBar progresx = (SeekBar) dialog.findViewById(R.id.analog_x);
					if(progresx!=null){
						progresx.setMax(val);
					}				
				}
			});
		}else if( "LENGTHY".equals(name)){
			final int val = virtualComponents.toInt( value );
			dialog.setText( R.id.dlugosc_y, value, false );
			application.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					SeekBar progresy = (SeekBar) application.findViewById(R.id.analog_y);
					if(progresy!=null){
						progresy.setMax(val);
						Constant.log(Constant.TAG,"setMaxy:"+ val);
					}
				}
			});
		}else if("LENGTHZ".equals(name)){
			dialog.setText( R.id.dlugosc_z, value, false );
		}else if("WEIGHT".equals(name) && dialog != null ){
			final String[] tokens = value.split(",");
	    	Constant.log("RUNNABLE", "waga: "+value );
	    	Constant.log("RUNNABLE", "waga: "+tokens.length );
	    	if(tokens.length == 16){
				application.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						dialog.setText( R.id.waga1, tokens[0], false );
						dialog.setText( R.id.waga2, tokens[1], false );
						dialog.setText( R.id.waga3, tokens[2], false );
						dialog.setText( R.id.waga4, tokens[3], false );
						dialog.setText( R.id.waga5, tokens[4], false );
						dialog.setText( R.id.waga6, tokens[5], false );
						dialog.setText( R.id.waga7, tokens[6], false );
						dialog.setText( R.id.waga8, tokens[7], false );
						dialog.setText( R.id.waga9, tokens[8], false );
						dialog.setText( R.id.waga10, tokens[9], false );
						dialog.setText( R.id.waga11, tokens[10], false );
						dialog.setText( R.id.waga12, tokens[11], false );
						dialog.setText( R.id.waga13, tokens[12], false );
						dialog.setText( R.id.waga14, tokens[13], false );
						dialog.setText( R.id.waga15, tokens[14], false );
						dialog.setText( R.id.waga16, tokens[15], false );
					}
				});
	    	}

		}else if("GLASS".equals(name) &&  dialog != null ){
			dialog.setText( R.id.dist1, value, false );				

		}else if("POSX".equals(name) &&  dialog != null ){
			dialog.setText( R.id.position_x, value, false );
			final int val = virtualComponents.toInt( value );
			application.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					SeekBar progresx = (SeekBar) application.findViewById(R.id.analog_x);
					if(progresx!=null){
						progresx.setProgress(val);
					}				
				}
			});
		}else if("POSY".equals(name) &&  dialog != null ){
			dialog.setText( R.id.position_y, value, false );
			final int val = virtualComponents.toInt( value );
			application.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					SeekBar progresx = (SeekBar) application.findViewById(R.id.analog_y);
					if(progresx!=null){
						progresx.setProgress(val);
					}					
				}
			});
		}else if("POSZ".equals(name) &&  dialog != null ){
			dialog.setText( R.id.position_z, value, false );	
		}else if("ANALOG0".equals(name) &&  dialog != null ){
			dialog.setText( R.id.analog0, value, false );
		}else if("DISTANCE0".equals(name) &&  dialog != null ){
			dialog.setText( R.id.dist1, value, false );				
		}else if("LED1".equals(name) &&  dialog != null ){
			dialog.setChecked( R.id.led1, "ON".equals(value) );
		}else if("LED2".equals(name) &&  dialog != null ){
			dialog.setChecked( R.id.led2, "ON".equals(value) );
		}else if("LED3".equals(name) &&  dialog != null ){
			dialog.setChecked( R.id.led3, "ON".equals(value) );
		}else if("LED4".equals(name) &&  dialog != null ){
			dialog.setChecked( R.id.led4, "ON".equals(value) );
		}else if("LED5".equals(name) &&  dialog != null ){
			dialog.setChecked( R.id.led5, "ON".equals(value) );
		}else if("LED6".equals(name) &&  dialog != null ){
			dialog.setChecked( R.id.led6, "ON".equals(value) );
		}else if("LED7".equals(name) &&  dialog != null ){
			dialog.setChecked( R.id.led7, "ON".equals(value) );
		}else if("LED8".equals(name) &&  dialog != null ){
			dialog.setChecked( R.id.led8, "ON".equals(value) );
		}else if("LED9".equals(name) &&  dialog != null ){
			dialog.setChecked( R.id.led9, "ON".equals(value) );
		}else if("LED10".equals(name) &&  dialog != null ){
			dialog.setChecked( R.id.led10, "ON".equals(value) );
		}else if("DISTANCE0".equals(name) &&  dialog != null ){
			dialog.setText( R.id.dist1, value, false );
		}else if("DISTANCE0".equals(name) &&  dialog != null ){
			dialog.setText( R.id.dist1, value, false );
		}else if("DISTANCE0".equals(name) &&  dialog != null ){
			dialog.setText( R.id.dist1, value, false );
		}else if("DISTANCE0".equals(name) &&  dialog != null ){
			dialog.setText( R.id.dist1, value, false );
		}else if("DISTANCE0".equals(name) &&  dialog != null ){
			dialog.setText( R.id.dist1, value, false );
		}else if("DISTANCE0".equals(name) &&  dialog != null ){
			dialog.setText( R.id.dist1, value, false );
		}else if("DISTANCE0".equals(name) &&  dialog != null ){
			dialog.setText( R.id.dist1, value, false );
		}else if("DISTANCE0".equals(name) &&  dialog != null ){
			dialog.setText( R.id.dist1, value, false );
		}
	}
	public static void nalej(int i) {
		queue q = queue.getInstance();
		if(virtualComponents.need_glass_up){
			q.add("WAIT GLASS " + virtualComponents.weigh_min_diff, true);
		}
		q.add("ENABLEX", true);
		q.add("ENABLEY", true);
		q.add("ENABLEZ", true);
		q.add("SET Z MAX", true);
		q.add("WAIT TIME " + i, true);
		
		q.add("SET Z MIN", true);
		if(virtualComponents.pac_enabled){
			q.add("PACPAC", true);
		}
		q.add("DISABLEX", true);
	    q.add("DISABLEY", true);
	    q.add("DISABLEZ", true);
	    q.add("GET CARRET", true);
	    q.send();
	}

	// zapisz ze tutaj jest butelka o danym numerze
	public static void hereIsBottle(int i) {
		String posx		=  virtualComponents.get("POSX", "0" );	
		String posy		=  virtualComponents.get("POSY", "0" );
		Constant.log(Constant.TAG,"zapisuje pozycje:"+ i + " " +posx+ " " + posy );
		
		virtualComponents.set("BOTTLE_X_" + i, posx );
		virtualComponents.set("BOTTLE_Y_" + i, posy );

		Toast.makeText(application, "Zapisano ["+posx+"/"+posy+"] jako butelka " + (i+1), Toast.LENGTH_LONG).show();
		DebugWindow bb			= DebugWindow.getInstance();
		if(bb!=null){
			bb.refreshPos();
			/*
			bb.tabHost.setCurrentTabByTag("tab1");
			bb.tabHost.bringToFront();
			bb.tabHost.setEnabled(true);
			*/
		}
	}
	public static boolean hasGlass() {
		return false;
	}
}

/* Nazwy komponent�w
 * DISTANCE0
 * DISTANCE1
 * 
SET SPEEDX
SET ACCX
 * */