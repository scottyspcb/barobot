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

	// pozycje butelek, s¹ aktualizowane w trakcie
	private static int[] b_pos_x = {207,207, 394,394,581,581,768,768, 955,955,1142,1142,1329,1329,1516,1516};
	private static int[] b_pos_y = {90, 550, 90, 550, 90, 550, 90, 550, 90, 550, 90, 550, 90, 550, 90, 550};

	private static String[] persistant = {"LENGTHX","LENGTHY","LENGTHZ",""};

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

		int remember = Arrays.asList(persistant).indexOf(name);			// czy zapisac w configu t¹ wartoœæ?
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
			dialog.setText( R.id.dlugosc_x, value );
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
			dialog.setText( R.id.dlugosc_y, value );
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
			dialog.setText( R.id.dlugosc_z, value );
		}else if("ANALOG0".equals(name) &&  dialog != null ){
			dialog.setText( R.id.analog0, value );
		}else if("DISTANCE0".equals(name) &&  dialog != null ){
			dialog.setText( R.id.dist1, value );				
		}else if("WEIGHT".equals(name) && dialog != null ){
			final String[] tokens = value.split(",");
			application.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					((TextView)application.findViewById(R.id.waga1)).setText( tokens[0]);
					((TextView)application.findViewById(R.id.waga2)).setText( tokens[1]);
					((TextView)application.findViewById(R.id.waga3)).setText( tokens[2]);
					((TextView)application.findViewById(R.id.waga4)).setText( tokens[3]);
					((TextView)application.findViewById(R.id.waga5)).setText( tokens[4]);
					((TextView)application.findViewById(R.id.waga6)).setText( tokens[5]);
					((TextView)application.findViewById(R.id.waga7)).setText( tokens[6]);
					((TextView)application.findViewById(R.id.waga8)).setText( tokens[7]);
					((TextView)application.findViewById(R.id.waga9)).setText( tokens[8]);
					((TextView)application.findViewById(R.id.waga10)).setText( tokens[9]);
					((TextView)application.findViewById(R.id.waga11)).setText( tokens[10]);
					((TextView)application.findViewById(R.id.waga12)).setText( tokens[11]);
					((TextView)application.findViewById(R.id.waga13)).setText( tokens[12]);
					((TextView)application.findViewById(R.id.waga14)).setText( tokens[13]);
					((TextView)application.findViewById(R.id.waga15)).setText( tokens[14]);
					((TextView)application.findViewById(R.id.waga16)).setText( tokens[15]);					
				}
			});

		}else if("GLASS".equals(name) &&  dialog != null ){
			dialog.setText( R.id.dist1, value );				

		}else if("POSX".equals(name) &&  dialog != null ){
			dialog.setText( R.id.position_x, value );
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
			dialog.setText( R.id.position_y, value );
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
			dialog.setText( R.id.position_z, value );

		}else if("LED1".equals(name) &&  dialog != null ){
			dialog.setChecked( R.id.dist1, "ON".equals(value) );
			
		}else if("DISTANCE0".equals(name) &&  dialog != null ){

			dialog.setText( R.id.dist1, value );
		}else if("DISTANCE0".equals(name) &&  dialog != null ){
			dialog.setText( R.id.dist1, value );
		}else if("DISTANCE0".equals(name) &&  dialog != null ){
			dialog.setText( R.id.dist1, value );
		}else if("DISTANCE0".equals(name) &&  dialog != null ){
			dialog.setText( R.id.dist1, value );
		}else if("DISTANCE0".equals(name) &&  dialog != null ){
			dialog.setText( R.id.dist1, value );
		}else if("DISTANCE0".equals(name) &&  dialog != null ){
			dialog.setText( R.id.dist1, value );
		}else if("DISTANCE0".equals(name) &&  dialog != null ){
			dialog.setText( R.id.dist1, value );
		}else if("DISTANCE0".equals(name) &&  dialog != null ){
			dialog.setText( R.id.dist1, value );
		}
	}
	public static void nalej(int i) {
		queue q = queue.getInstance();
		q.send("FILL 5000");
	}

}

/* Nazwy komponentów
 * DISTANCE0
 * DISTANCE1
 * 
SET SPEEDX
SET ACCX
 * */