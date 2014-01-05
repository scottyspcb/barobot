package com.barobot.utils;

import android.util.Log;

import com.barobot.R;
import com.barobot.activity.DebugActivity;
import com.barobot.hardware.Methods;
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
			//Log.i(Constant.TAG, "input [" + input_parser.buffer+"]" );
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
		Log.i(Constant.TAG, "parse:[" + fromArduino +"]");

		if(fromArduino.startsWith("POS")){
			fromArduino = "R" + fromArduino;			// VERY BAD HACK
		}
		char command = fromArduino.charAt(0);
		if( command =='A' ){
			AJS aa = AJS.getInstance();
			if(aa!=null){
				String[] tokens = fromArduino.split(" ");
				if(tokens.length > 1 ){
					try {
						int e = Integer.parseInt( tokens[1] );
						e	= e / virtualComponents.graph_repeat;		// podziel przez liczbe powtorzen
						aa.oscyloskop( e );
					} catch ( java.lang.NumberFormatException e2) {
					}
				}
			}
			if(fromArduino.startsWith("A2 ")){			// analog2
				String fromArduino21 = fromArduino.replace("A2 ", "");
				virtualComponents.set( "GLASS_WEIGHT", fromArduino21);
				int noglass_weight = virtualComponents.getInt( "NOGLASS_WEIGHT", 0 );
				try {
					int e = Integer.parseInt(fromArduino21);
					if( e < noglass_weight ){		// jesli jest lżej od tego ile powinno byc
						virtualComponents.set( "NOGLASS_WEIGHT", fromArduino21);
					}
				} catch ( java.lang.NumberFormatException e2) {
				}
			}
		}else if( command =='-' ){		// nic- to komentarz	
			
		}else if(command == 'R' ){		// na końcu bo to może odblokować wysyłanie i spowodować zapętlenie
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
			}else
				*/
				if( fromArduino.equals("RREBOOT") ){		//  właśnie uruchomiłem arduino
				Arduino q			= Arduino.getInstance();
				q.clear();

			}else if(fromArduino.startsWith("RPOSX")){
				String fromArduino2 = fromArduino.replace("RPOSX", "");	
				long posx = virtualComponents.toInt(fromArduino2);	// hardware pos
				posx = virtualComponents.driver_x.hard2soft(posx);
				virtualComponents.set( "POSX",posx);
				long lx	=  virtualComponents.getInt("LENGTHX", 600 );
				if( posx > lx){		// Pozycja wieksza niz długosc? Zwieksz długosc
					virtualComponents.set( "LENGTHX", "" + posx);
				}
			}else if(fromArduino.startsWith("RPOSY")){
				String fromArduino2 = fromArduino.replace("RPOSY", "");
				virtualComponents.set( "POSY",fromArduino2);

			}else if(fromArduino.startsWith("RPOSZ")){
				String fromArduino2 = fromArduino.replace("RPOSZ", "");
				virtualComponents.set( "POSZ",fromArduino2);
/*
			}else if(fromArduino.startsWith("R READY ")){	
				String fromArduino2 = fromArduino.replace("R READY AT ", "");
				String[] tokens = fromArduino2.split(",");
				virtualComponents.is_ready = true;
				virtualComponents.set( "POSX",tokens[0]);
				virtualComponents.set( "POSY",tokens[1]);
				virtualComponents.set( "POSZ",tokens[2]);*/
			}
			is_ret = Arduino.getInstance().read_ret( fromArduino );		// zapisuj zwrotki
	
		}else if(command == 'T' ){  // trigger	
			String[] tokens = fromArduino.split(",");
			char axis		= fromArduino.charAt(1);
			int reason		= virtualComponents.toInt(tokens[1]);	// reason, 
			int direction	= virtualComponents.toInt(tokens[2]);	// direction,					       
			long pos		= virtualComponents.toInt(tokens[3]);	// pos
			if( axis == 'X'){
				if(reason == Methods.HALL_GLOBAL_MIN){				// endstop MIN
					virtualComponents.set( "X_GLOBAL_MIN", "" + pos );
					virtualComponents.driver_x.setM(pos);

					long posx = virtualComponents.driver_x.hard2soft(pos);
					virtualComponents.set( "POSX","" + posx);
					Log.i("input_parser", "jestem w: " + posx );
					if(virtualComponents.scann_bottles == true){
						virtualComponents.hereIsStart(posx, virtualComponents.SERVOY_FRONT_POS );
					}

				}else if(reason == Methods.HALL_GLOBAL_MAX){		// endstop MAX
					long posx = virtualComponents.driver_x.hard2soft(pos);
					
					virtualComponents.set( "LENGTHX", "" + posx);
					virtualComponents.set( "X_GLOBAL_MAX", "" + posx );
					if(virtualComponents.scann_bottles == true){
						virtualComponents.hereIsBottle(11, posx, virtualComponents.SERVOY_FRONT_POS );
						Log.i("input_parser "+ virtualComponents.scann_num+" "+virtualComponents.SERVOY_FRONT_POS, "butelka 11: " + posx );
					}

				}else if(reason == Methods.HALL_LOCAL_MAX){			// butelka
					long posx = virtualComponents.driver_x.hard2soft(pos);
					
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
					long posx = virtualComponents.driver_x.hard2soft(pos);
					
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
			}else if( axis == 'Y'){
			}else if( axis == 'Z'){
			}
		
		}else if(command == 'E' ){  //error	
			input_parser.handleError( fromArduino );			// analizuj błędy

		}else if(fromArduino.startsWith("INTERVAL")){
			AJS aa = AJS.getInstance();
			if(aa!=null){
				aa.oscyloskop_interval();
			}
		}else if( fromArduino.startsWith("VAL A0")){
			String fromArduino2 = fromArduino.replace("VAL A0 ", "");			
			virtualComponents.set( "A0",fromArduino2);

		}else if(fromArduino.startsWith("LENGTHX")){
			String fromArduino2 = fromArduino.replace("LENGTHX ", "");
			virtualComponents.set( "LENGTHX",fromArduino2);

		}else if(fromArduino.startsWith("WEIGHT")){	
			String fromArduino2 = fromArduino.replace("WEIGHT ", "");
			virtualComponents.set( "WEIGHT",fromArduino2);

		}else if(fromArduino.equals("PONG")){

		}else if(fromArduino.startsWith("PING")){
//			toSend.add("PONG");
		}
     //   if( !is_ret ){ // jesli nie jest zwrotka
			Arduino q			= Arduino.getInstance();
        	q.addToList(fromArduino, false );
      //  }
	}
	private static void handleError(String fromArduino) {	
	}
	/*
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
