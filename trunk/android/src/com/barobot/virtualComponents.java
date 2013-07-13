package com.barobot;
import java.util.HashMap;
import java.util.Map;

import android.util.Log;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

public class virtualComponents {
	private static Map<String, String> mapa = new HashMap<String, String>();

	// pozycje butelek, s¹ aktualizowane w trakcie
	private static int[] b_pos_x = {207,207, 394,394,581,581,768,768, 955,955,1142,1142,1329,1329,1516,1516};
	private static int[] b_pos_y = {90, 550, 90, 550, 90, 550, 90, 550, 90, 550, 90, 550, 90, 550, 90, 550};

	public static String get( String name ){
		return mapa.get(name);
	}
	public static int getInt( String name ){
		return virtualComponents.toInt(mapa.get(name));
	}
	public static void set( String name, String value ){
		mapa.put(name, value );
		virtualComponents.update( name, value );
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
		// TODO Auto-generated method stub
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
			final int val = virtualComponents.toInt( value );
			dialog.runOnUiThread(new Runnable() {
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
			dialog.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					SeekBar progresy = (SeekBar) dialog.findViewById(R.id.analog_y);
					if(progresy!=null){
						progresy.setMax(val);
						Constant.log(Constant.TAG,"setMaxy:"+ val);
					}
				}
			});
		}else if("ANALOG0".equals(name) &&  dialog != null ){
			dialog.setText( R.id.analog0, value );				
		}else if("DISTANCE0".equals(name) &&  dialog != null ){
			dialog.setText( R.id.dist1, value );				
		}else if("WEIGHT".equals(name) && dialog != null ){
			final String[] tokens = value.split(",");
			dialog.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					((TextView)dialog.findViewById(R.id.waga1)).setText( tokens[0]);
					((TextView)dialog.findViewById(R.id.waga2)).setText( tokens[1]);
					((TextView)dialog.findViewById(R.id.waga3)).setText( tokens[2]);
					((TextView)dialog.findViewById(R.id.waga4)).setText( tokens[3]);
					((TextView)dialog.findViewById(R.id.waga5)).setText( tokens[4]);
					((TextView)dialog.findViewById(R.id.waga6)).setText( tokens[5]);
					((TextView)dialog.findViewById(R.id.waga7)).setText( tokens[6]);
					((TextView)dialog.findViewById(R.id.waga8)).setText( tokens[7]);
					((TextView)dialog.findViewById(R.id.waga9)).setText( tokens[8]);
					((TextView)dialog.findViewById(R.id.waga10)).setText( tokens[9]);
					((TextView)dialog.findViewById(R.id.waga11)).setText( tokens[10]);
					((TextView)dialog.findViewById(R.id.waga12)).setText( tokens[11]);
					((TextView)dialog.findViewById(R.id.waga13)).setText( tokens[12]);
					((TextView)dialog.findViewById(R.id.waga14)).setText( tokens[13]);
					((TextView)dialog.findViewById(R.id.waga15)).setText( tokens[14]);
					((TextView)dialog.findViewById(R.id.waga16)).setText( tokens[15]);					
				}
			});

		}else if("GLASS".equals(name) &&  dialog != null ){
			dialog.setText( R.id.dist1, value );				

		}else if("POSX".equals(name) &&  dialog != null ){
			dialog.setText( R.id.position_x, value );
			final int val = virtualComponents.toInt( value );
			dialog.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					SeekBar progresx = (SeekBar) dialog.findViewById(R.id.analog_x);
					if(progresx!=null){
						progresx.setProgress(val);
					}
					ProgressBar progresx2 = (ProgressBar) dialog.findViewById(R.id.position_x2);
					if(progresx2!=null){
						progresx.setProgress(val);
						Constant.log(Constant.TAG,"setProgress X:"+ val);
					}					
				}
			});
		}else if("POSY".equals(name) &&  dialog != null ){
			dialog.setText( R.id.position_y, value );
			final int val = virtualComponents.toInt( value );
			dialog.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					SeekBar progresx = (SeekBar) dialog.findViewById(R.id.analog_y);
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