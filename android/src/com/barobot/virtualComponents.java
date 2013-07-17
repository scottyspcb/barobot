package com.barobot;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

public class virtualComponents {
	public static Activity application;
	private static SharedPreferences myPrefs;
	private static SharedPreferences.Editor config_editor;			// config systemu android
	private static Map<String, String> hashmap = new HashMap<String, String>();
	public static boolean is_ready = false;
	// pozycje butelek, sa aktualizowane w trakcie
	private static int[] b_pos_x = {207,207, 394,394,581,581,768,768, 955,955,1142,1142,1329,1329,1516,1516};
	private static int[] b_pos_y = {90, 550, 90, 550, 90, 550, 90, 550, 90, 550, 90, 550, 90, 550, 90, 550};

	private static String[] persistant = {"LENGTHX","LENGTHY","LENGTHZ","LAST_BT_DEVICE"};

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

	public static void moveToBottle(int i) {
		int x = b_pos_x[i];
		int y = b_pos_y[i];
		queue q = queue.getInstance();
		q.send("SET X " + x);
		q.send("SET Y " + y);
	}

	private static void update(String name, String value) {
		// TODO Auto-generated method stub
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
					ProgressBar progresx2 = (ProgressBar) dialog.findViewById(R.id.position_x2);
					if(progresx2!=null){
						progresx2.setMax(val);
						Constant.log(Constant.TAG,"LENGTHX:"+ val);
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
					ProgressBar progresx2 = (ProgressBar) application.findViewById(R.id.position_x2);
					if(progresx2!=null){
						progresx.setProgress(val);
						Constant.log(Constant.TAG,"setProgress X:"+ val);
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
		q.send("FILL 5000");
	}

}

/* Nazwy komponent�w
 * DISTANCE0
 * DISTANCE1
 * 
SET SPEEDX
SET ACCX
 * */