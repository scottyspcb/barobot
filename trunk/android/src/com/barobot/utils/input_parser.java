package com.barobot.utils;

import android.util.Log;

import com.barobot.DebugActivity;
import com.barobot.R;
import com.barobot.hardware.virtualComponents;
import com.barobot.webview.AJS;

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
					}
					parseInput(command);
					end		= input_parser.buffer.indexOf(separator);
				//	
				}
			}
        }
	}

	private static void parseInput(String fromArduino) {
		//Log.i(Constant.TAG, "parse:[" + fromArduino +"]");
		boolean is_ret = false;

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
		}else if(command == 'R' ){		// na końcu bo to może odblokować wysyłanie i spowodować zapętlenie
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
			}else if( fromArduino.equals("R REBOOT") ){		//  właśnie uruchomiłem arduino
				Arduino q			= Arduino.getInstance();
				q.clear();

			}else if(fromArduino.startsWith("POSX")){
				String fromArduino2 = fromArduino.replace("POSX", "");
				virtualComponents.set( "POSX",fromArduino2);
				long posx = Long.parseLong(fromArduino2);
				long lx	=  virtualComponents.getInt("LENGTHX", 600 );
				if( posx > lx){		// Pozycja wieksza niz długosc? Zwieksz długosc
					virtualComponents.set( "LENGTHX", "" + posx);
				}
			}else if(fromArduino.startsWith("POSY")){
				String fromArduino2 = fromArduino.replace("POSY", "");
				virtualComponents.set( "POSY",fromArduino2);
				long posy = Long.parseLong(fromArduino2);
				long ly	=  virtualComponents.getInt("LENGTHY", 600 );
				if( posy > ly){		// Pozycja wieksza niz długosc? Zwieksz długosc
					virtualComponents.set( "LENGTHY", "" + posy);
				}
			}else if(fromArduino.startsWith("POSZ")){
				String fromArduino2 = fromArduino.replace("POSZ", "");
				virtualComponents.set( "POSZ",fromArduino2);
				long posz = Long.parseLong(fromArduino2);
				long lz	=  virtualComponents.getInt("LENGTHZ", 600 );
				if( posz > lz){		// Pozycja wieksza niz długosc? Zwieksz długosc
					virtualComponents.set( "LENGTHZ", "" + posz);
				}				

			}else if(fromArduino.startsWith("R READY ")){	
				String fromArduino2 = fromArduino.replace("R READY AT ", "");
				String[] tokens = fromArduino2.split(",");
				virtualComponents.is_ready = true;
				virtualComponents.set( "POSX",tokens[0]);
				virtualComponents.set( "POSY",tokens[1]);
				virtualComponents.set( "POSZ",tokens[2]);
			}
			is_ret = Arduino.getInstance().read_ret( fromArduino );		// zapisuj zwrotki
		
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

		}else if(fromArduino.startsWith("IRR ")){
		}else if(fromArduino.startsWith("LENGTHX")){	
			String fromArduino2 = fromArduino.replace("LENGTHX ", "");
			virtualComponents.set( "LENGTHX",fromArduino2);
			
		}else if(fromArduino.startsWith("LENGTHY")){	
			String fromArduino2 = fromArduino.replace("LENGTHY ", "");
			virtualComponents.set( "LENGTHY",fromArduino2);

		}else if(fromArduino.startsWith("LENGTHZ")){
			String fromArduino2 = fromArduino.replace("LENGTHZ ", "");
			virtualComponents.set( "LENGTHZ",fromArduino2);

		}else if(fromArduino.startsWith("WEIGHT")){	
			String fromArduino2 = fromArduino.replace("WEIGHT ", "");
			virtualComponents.set( "WEIGHT",fromArduino2);

		}else if(fromArduino.equals("PONG")){

		}else if(fromArduino.startsWith("PING")){
//			toSend.add("PONG");
		}
        if( !is_ret ){ // jesli nie jest zwrotka
			Arduino q			= Arduino.getInstance();
        	q.addToList(fromArduino, false );
        }
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
