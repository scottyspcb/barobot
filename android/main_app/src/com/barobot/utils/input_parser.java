package com.barobot.utils;

import android.util.Log;

import com.barobot.R;
import com.barobot.activity.DebugActivity;
import com.barobot.constant.Constant;
import com.barobot.constant.Methods;
import com.barobot.debug.DebugTabGraph;
import com.barobot.hardware.virtualComponents;
import com.barobot.web.server.AJS;

public class input_parser {
	private static String buffer = "";
	public static String separator = "\n";

	public static void readInput( final String in ){
		//if(null == in){
		//	return;
		//}
		synchronized (input_parser.class) {
			input_parser.buffer = input_parser.buffer + in;
		//	Log.v(Constant.TAG, "input [" + input_parser.buffer+"]" );
			int end = input_parser.buffer.indexOf(separator);
			if( end!=-1){
				while( end != -1 ){		// podziel to na kawalki
			//		Log.i(Constant.TAG, "input [" + input_parser.buffer+"] dł:" +input_parser.buffer.length() );
					String command		= input_parser.buffer.substring(0, end);
					input_parser.buffer = input_parser.buffer.substring(end+1);
					command				= command.trim();
				//	Log.i(Constant.TAG, "zostalo [" + input_parser.buffer +"]");
					if("".equals(command)){
						Log.i(Constant.TAG, "posta komenda!!!]");
					}else{
						parseInput(command);
						Arduino.getInstance().debug(command+"\n");
					}
					end		= input_parser.buffer.indexOf(separator);
				//	
				}
			}
        }
	}

	private static void parseInput(String fromArduino) {
		boolean is_ret = false;
		if(fromArduino.length() == 0){
			return;
		}
		if(fromArduino.startsWith("x")){		// bad hack
			fromArduino = "R" + fromArduino;
			Log.e(Constant.TAG, "poprawiam:[" + fromArduino +"]");
		}
		Log.i(Constant.TAG, "parse:[" + fromArduino +"]");
		char command = fromArduino.charAt(0);
		if( command == Constant.METHOD_LIVE_ANALOG ){		// analog na zywo
			int[] parts = decodeBytes( fromArduino );
			int value = parts[4] + (parts[5] << 8) + (parts[6] << 16 + (parts[7] << 24));	
			value	= value / DebugTabGraph.graph_repeat;		// podziel przez liczbe powtorzen
			AJS aa = AJS.getInstance();
			if(aa!=null){
				aa.oscyloskop( value );
			}
			
			
		}else if( command == Constant.COMMENT ){		// nic- to komentarz	
		}else if( fromArduino.startsWith( "" + Constant.METHOD_I2C_SLAVEMSG) ){		// msg od slave
			int[] parts = decodeBytes( fromArduino );
			String retLike = fromArduino;
/*
			parts[0]	= METHOD_I2C_SLAVEMSG
			parts[1]	= my_address
			parts[2]	= METHOD_GET_Y_POS (WHY_CODE)
			parts[3]	- param 0
*/
			if( parts[2] == Constant.METHOD_GET_X_POS ){
				int hpos = parts[3] + (parts[4] << 8); 
				int posx = virtualComponents.driver_x.hard2soft(hpos);
				virtualComponents.set( "POSX",posx);
				int lx	=  virtualComponents.getInt("LENGTHX", 600 );
				if( posx > lx){		// Pozycja wieksza niz długosc? Zwieksz długosc
					virtualComponents.set( "LENGTHX", "" + posx);
				}
				retLike = "Rx";
			}else if( parts[2] == Constant.METHOD_GET_Y_POS ){
				int pos = parts[3] + (parts[4] << 8); 
				virtualComponents.set( "POSY",""+pos);
				retLike = "Ry";
			}else if( parts[2] == Constant.METHOD_GET_Z_POS ){
				int pos = parts[3] + (parts[4] << 8); 
				virtualComponents.set( "POSZ",""+pos);
				retLike = "Rz";
			}else if( parts[2] == Constant.METHOD_DRIVER_DISABLE ){
				if( parts[3] == Constant.DRIVER_X){
					retLike = "RDX";
				}else if( parts[3] == Constant.DRIVER_Y){
					retLike = "RDY";
				}else if( parts[3] == Constant.DRIVER_Z){
					retLike = "RDZ";
				}
			}else if( parts[2] == Constant.METHOD_DRIVER_ENABLE ){
				if( parts[3] == Constant.DRIVER_X){
					retLike = "REX";
				}else if( parts[3] == Constant.DRIVER_Y){
					retLike = "REY";
				}else if( parts[3] == Constant.DRIVER_Z){
					retLike = "REZ";
				}
			}else if( parts[2] == Constant.RETURN_DRIVER_READY ){
				int pos = parts[4] + (parts[5] << 8);
				if( parts[3] == Constant.DRIVER_X){
					pos = pos + (parts[6] << 16 + (parts[7] << 24));
					retLike = "Rx";
				}else if( parts[3] == Constant.DRIVER_Y){
					retLike = "Ry";
				}else if( parts[3] == Constant.DRIVER_Z){
					retLike = "Rz";
				}
				virtualComponents.set( "POSX",pos);
			}else if( parts[2] == Constant.RETURN_PIN_VALUE ){
				//{METHOD_I2C_SLAVEMSG,my_address, RETURN_PIN_VALUE,my_address,pin,value}
				byte my_address = (byte) parts[3];
				byte pin		= (byte) parts[4];
				byte value		= (byte) parts[5];
				Log.e("POKE-BUTTON", "Address:" + my_address + ", pin: " + pin+ ", value: " + value );
			}else{
				Log.e("FINDER", "no METHOD_I2C_SLAVEMSG");
			}
			Log.i("retLike", retLike);
			is_ret = Arduino.getInstance().read_ret( retLike );		// zapisuj zwrotki
		}else if( fromArduino.startsWith( "" + Constant.METHOD_DEVICE_FOUND) ){
			// byte ttt[5] = {METHOD_DEVICE_FOUND,addr,type,ver,pos};
			// byte ttt[5] = {METHOD_DEVICE_FOUND,I2C_ADR_MAINBOARD,MAINBOARD_DEVICE_TYPE,MAINBOARD_VERSION,0};

			boolean scanning = true;
			if( scanning ){
/*
				byte pos = getResetOrder(buffer[1]);
				i2c_reset_next( buffer[1], false );       // reset next (next to slave)
				i2c_reset_next( buffer[1], true );
				}else if( scann_order ){ 
					if( pos == 0xff ){        // nie ma na liscie?
						order[nextpos++]  = buffer[1];            // na tm miejscu slave o tym adresie
						pos = getResetOrder(buffer[1]);
					}else{
						scann_order  =  false;
					}
				}
			*/
			}

			int[] parts = decodeBytes( fromArduino );
			if(parts[2] == Constant.MAINBOARD_DEVICE_TYPE ){
				Arduino a	= Arduino.getInstance();
				int cx		= virtualComponents.getInt("POSX", 0 );
				virtualComponents.driver_x.setM(cx);	// ostatnia znana pozycja jest marginesem
				a.clear();
			}else if(parts[2] == Constant.UPANEL_DEVICE_TYPE ){		// upanel

			}else if(parts[2] == Constant.IPANEL_DEVICE_TYPE ){		// wozek
			}

		}else if( fromArduino.startsWith( "" + Constant.RETURN_I2C_ERROR) ){
			// byte ttt[4] = {RETURN_I2C_ERROR,my_address, deviceAddress,length, command }
			// Urządzenie 'my_address' wysyłało do 'deviceAddress' bajtów length
			Arduino.getInstance().unlock();
			// todo, obsłużyc to lepiej

		}else if( fromArduino.startsWith( "" + Constant.METHOD_EXEC_ERROR) ){		// msg od slave		
			int[] parts = decodeBytes( fromArduino );
			String retLike = fromArduino;
			if( parts[3] == Constant.DRIVER_X){
				retLike = "Rx";
			}else if( parts[3] == Constant.DRIVER_Y){
				retLike = "Ry";
			}else if( parts[3] == Constant.DRIVER_Z){
				retLike = "Rz";
			}
			is_ret = Arduino.getInstance().read_ret( retLike );		// zapisuj zwrotki
		}else if(command == Constant.RET ){		// na końcu bo to może odblokować wysyłanie i spowodować zapętlenie
			String fromArduino2 = fromArduino.substring(1);
			/*
			if(fromArduino.startsWith("R SET LED")){	
				String fromArduino2 = fromArduino.replace("R SET LED", "");
				String[] tokens = fromArduino2.split(" ");		// numer i wartosc
				virtualComponents.set( "LED" + tokens[0],  tokens[1] );		//  ON lub OFF

			}else if(fromArduino.startsWith("R LIVE A OFF")){
				final DebugActivity dialog = DebugActivity.getInstance();
				if(dialog!=null){
					dialog.setChecked( R.id.wagi_live, false );
				}
			}else if(fromArduino.startsWith("R LIVE A 2,")){
				String fromArduino2 = fromArduino.replace("R LIVE A 2,", "");
				final DebugActivity dialog = DebugActivity.getInstance();
				if(dialog!=null){
					dialog.setChecked( R.id.wagi_live, !"OFF".equals(fromArduino2) );
				}
			}else{}
				*/
			if(fromArduino2.startsWith(Constant.GETXPOS)){
				String fromArduino3 = fromArduino2.replace(Constant.GETXPOS, "");	
				int posx = input_parser.toInt(fromArduino3);	// hardware pos
				posx = virtualComponents.driver_x.hard2soft(posx);
				virtualComponents.set( "POSX",posx);
				int lx	=  virtualComponents.getInt("LENGTHX", 600 );
				if( posx > lx){		// Pozycja wieksza niz długosc? Zwieksz długosc
					virtualComponents.set( "LENGTHX", "" + posx);
				}
			}else if(fromArduino2.startsWith(Constant.GETYPOS)){
				String fromArduino3 = fromArduino2.replace(Constant.GETYPOS, "");
				virtualComponents.set( "POSY",fromArduino3);

			}else if(fromArduino2.startsWith(Constant.GETZPOS)){
				String fromArduino3 = fromArduino2.replace(Constant.GETZPOS, "");
				virtualComponents.set( "POSZ",fromArduino3);
			}
			is_ret = Arduino.getInstance().read_ret( fromArduino );		// zapisuj zwrotki

		}else if(command == Constant.TRIGGER ){  // trigger	
			String[] tokens = fromArduino.split(",");
			char axis		= fromArduino.charAt(1);
			int reason		= input_parser.toInt(tokens[1]);	// reason, 
			int direction	= input_parser.toInt(tokens[2]);	// direction,					       
			int pos			= input_parser.toInt(tokens[3]);	// pos
			if( axis == 'X'){
				if(reason == Methods.HALL_GLOBAL_MIN){				// endstop MIN
					virtualComponents.set( "X_GLOBAL_MIN", "" + pos );
					int posx = virtualComponents.driver_x.hard2soft(pos);
					virtualComponents.set( "POSX","" + posx);
					virtualComponents.driver_x.setM(pos);
					if(virtualComponents.scann_bottles == true){
						Log.i("input_parser", "jestem w: " + posx );
						virtualComponents.hereIsStart(posx, virtualComponents.SERVOY_FRONT_POS );
					}

				}else if(reason == Methods.HALL_GLOBAL_MAX){		// endstop MAX
					int posx = virtualComponents.driver_x.hard2soft(pos);
					if(virtualComponents.scann_bottles == true){
						virtualComponents.set( "LENGTHX", "" + posx);
						virtualComponents.set( "X_GLOBAL_MAX", "" + posx );
						virtualComponents.hereIsBottle(11, posx, virtualComponents.SERVOY_FRONT_POS );
						Log.i("input_parser "+ virtualComponents.scann_num+" "+virtualComponents.SERVOY_FRONT_POS, "butelka 11: " + posx );
					}

				}else if(reason == Methods.HALL_LOCAL_MAX){			// butelka
					int posx = virtualComponents.driver_x.hard2soft(pos);
					if(direction == Methods.DRIVER_DIR_BACKWARD){
					//	Log.i("FINDER+", "Znalazlem cos pod adresem: "+ virtualComponents.scann_num+" "+posx);	
					}		

					if(virtualComponents.scann_bottles == true){
						if(virtualComponents.scann_num < 12 && virtualComponents.scann_num >= 0 ){
							int ind	= virtualComponents.scann_num;
							if(direction == Methods.DRIVER_DIR_BACKWARD){
								ind	= 11-virtualComponents.scann_num;
							}
							int num = virtualComponents.magnet_order[ind];
							int ypos	= virtualComponents.b_pos_y[ num ];
							posx		= posx + virtualComponents.margin_x[num];
							Log.i("input_parser "+ virtualComponents.scann_num+" "+ypos, "butelka "+num+": " + posx+ " / " + virtualComponents.margin_x[num] );

							if(direction == Methods.DRIVER_DIR_BACKWARD){
								virtualComponents.hereIsBottle(num, posx, ypos );	
							}
							virtualComponents.scann_num++;							
						}else{
							Log.i("input_parser BACK", "za duzo butelek" + virtualComponents.scann_num );								
						}
					}
				}else if(reason == Methods.HALL_LOCAL_MIN){			// butelka
					int posx = virtualComponents.driver_x.hard2soft(pos);
					if(direction == Methods.DRIVER_DIR_BACKWARD){
				//	Log.i("FINDER-", "Znalazlem cos pod adresem: "+ virtualComponents.scann_num+" "+posx);	
					}				

					if(virtualComponents.scann_bottles == true){
						if(virtualComponents.scann_num < 12 && virtualComponents.scann_num >= 0 ){
							int ind	= virtualComponents.scann_num;
							if(direction == Methods.DRIVER_DIR_BACKWARD){
								ind	= 11-virtualComponents.scann_num;
							}
							int num		= virtualComponents.magnet_order[ind];							
							int ypos	= virtualComponents.b_pos_y[ num ];
							posx		= posx + virtualComponents.margin_x[num];
							if(direction == Methods.DRIVER_DIR_BACKWARD){
								virtualComponents.hereIsBottle(num, posx, ypos );	
							}
							Log.i("input_parser "+ virtualComponents.scann_num+" "+ypos, "butelka "+num+": " + posx+ " / " + virtualComponents.margin_x[num] );
							virtualComponents.scann_num++;							
						}else{
							Log.i("input_parser MIN", "za duzo butelek" );								
						}
					}
				}
			}
		}else if( fromArduino.startsWith( "" + Constant.METHOD_IMPORTANT_ANALOG) ){		// msg od slave		
			/*
			byte ttt[5] = {
			  METHOD_IMPORTANT_ANALOG,
			  buffer[1],                        // analog num
			  buffer[2],                        // reason
			  buffer[2],                        // pos low byte
			  buffer[2]                         // pos high byte
			};
			*/

		}else if(command == Constant.ERROR ){  //error
			Arduino a			= Arduino.getInstance();
			a.throwError(fromArduino);

		}else if(fromArduino.startsWith("INTERVAL")){
			AJS aa = AJS.getInstance();
			if(aa!=null){
				aa.oscyloskop_interval();
			}
		}else if( fromArduino.startsWith("VAL A0")){
			String fromArduino2 = fromArduino.replace("VAL A0 ", "");			
			virtualComponents.set( "A0",fromArduino2);

		}else if(fromArduino.equals(Constant.PONG)){

		}else if(fromArduino.startsWith(Constant.PING)){
			Arduino ar		= Arduino.getInstance();
			ArduinoQueue q	= new ArduinoQueue();
			q.add("PONG", false);
			ar.send(q);
		}
     //   if( !is_ret ){ // jesli nie jest zwrotka
			Arduino q			= Arduino.getInstance();
        	q.addToList(fromArduino, false );
      //  }
	}

	private static int[] decodeBytes(String fromArduino) {
		String[] parts = fromArduino.split(",");
		int[] iparts = new int[parts.length];
		for(byte i=0;i<parts.length;i++){
			iparts[ i ] = toInt(parts[i]);
		}
		return iparts;
	}

	public static int toInt( String input ){
		input	= input.replaceAll( "[^-\\d]", "" );
		int res = 0;
		try {
			res = Integer.parseInt(input);
		} catch (NumberFormatException e) {
			return 0;
		}
	//	Constant.log(Constant.TAG,"toInt:"+ input + "/ "+ res );
		return res;
	}
	
	/*
	 * 
	// UpdateData Asynchronously sends the value received from ADK Main Board. 
	// This is triggered by onReceive()
	public static class UpdateData extends AsyncTask<String, Integer, String> {
		// Called to initiate the background activity
		@Override
		protected String doInBackground(String ...inputs) {
			for (String fromArduino : inputs) {
				parseInput(fromArduino);	
			}
			return inputs[0];
		}
		// Called when there's a status to be updated
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
		}
		// Called once the background activity has completed
	//	@Override
		protected void onPostExecute(String result) {
		}
	}*/
	/*
	private static void showInput( final String in ){
		//queue.getInstance().send("ANDROID " + in );
		dialog.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				parseInput(in);
				//UpdateData bb = new UpdateData();
				//bb.execute(in);
			}
		});
		Thread thread = new Thread(){
		    public void run(){
		    	  
		    }
		  };	  		
		//thread.start();
	}
*/
}
