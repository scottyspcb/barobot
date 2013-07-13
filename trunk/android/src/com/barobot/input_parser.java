package com.barobot;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.SeekBar;

public class input_parser {
	private static String buffer = "";
	/*
//	DebugWindow.getInstance().setText( R.id.inputArea, in.replace("\n", "|") );
	*/
	static BarobotMain bb6 = null;
	public static String separator = "\n";

	public static void readInput( final String in ){
		if(null == in){
			return;
		}
		if( input_parser.bb6 == null ){
			input_parser.bb6 = BarobotMain.getInstance();			
		}
		synchronized (input_parser.class) {
			input_parser.buffer = input_parser.buffer + in;
			//Log.i(Constant.TAG, "input [" + input_parser.buffer+"]" );
			int end = input_parser.buffer.indexOf(separator);
			if( end!=-1){
				while( end != -1 ){		// podziel to na kawa³ki
			//		Log.i(Constant.TAG, "input [" + input_parser.buffer+"] d³:" +input_parser.buffer.length() );
					String command		= input_parser.buffer.substring(0, end);
					input_parser.buffer = input_parser.buffer.substring(end+1);
					command				= command.trim();
				//	Log.i(Constant.TAG, "zostalo [" + input_parser.buffer +"]");
					if(! "".equals(command)){
						showInput(command);
						if(bb6 !=null){
							bb6.addToList3( command );
							//bb6.addToList(command + " /// " + input_parser.buffer );
						}
					}
					end					= input_parser.buffer.indexOf(separator);
				//	Log.i(Constant.TAG, "[" + command +"/"+ input_parser.buffer+"]");
				}
			}
        }
	}
	private static void showInput( final String in ){
		queue.getInstance().send("ANDROID " + in );
		/*
		dialog.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				parseInput(in);
				//UpdateData bb = new UpdateData();
				//bb.execute(in);
			}
		});
		*/
		Thread thread = new Thread(){
		    public void run(){
		    	parseInput(in);  
		    }
		  };
		thread.start();
	}

	private static void parseInput(String fromArduino) {
		Log.i(Constant.TAG, "parse:[" + fromArduino +"]");

		if( fromArduino.startsWith("RET VAL ANALOG0")){
			String fromArduino2 = fromArduino.replace("RET VAL ANALOG0 ", "");			
			virtualComponents.set( "ANALOG0",fromArduino2);

		}else if( fromArduino.startsWith("RET VAL DISTANCE0")){
			String fromArduino2 = fromArduino.replace("RET VAL DISTANCE0 ", "");
			virtualComponents.set( "DISTANCE0",fromArduino2);

		}else if(fromArduino.startsWith("IRR ")){	

// komponenty wirtualne
		}else if(fromArduino.startsWith("LENGTHX")){	
			String fromArduino2 = fromArduino.replace("LENGTHX ", "");
			virtualComponents.set( "LENGTHX",fromArduino2);
			
		}else if(fromArduino.startsWith("LENGTHY")){	
			String fromArduino2 = fromArduino.replace("LENGTHY ", "");
			virtualComponents.set( "LENGTHY",fromArduino2);
			
		}else if(fromArduino.startsWith("GLASS")){	
			String fromArduino2 = fromArduino.replace("GLASS ", "");
			virtualComponents.set( "GLASS",fromArduino2);

		}else if(fromArduino.startsWith("WEIGHT")){	
			String fromArduino2 = fromArduino.replace("WEIGHT ", "");
			virtualComponents.set( "WEIGHT",fromArduino2);

		}else if(fromArduino.startsWith("RET SET LED")){	
			String fromArduino2 = fromArduino.replace("RET SET LED", "");
			String[] tokens = fromArduino2.split(" ");		// numer i wartoœæ
			virtualComponents.set( "LED" + tokens[0],  tokens[1] );		//  ON lub OFF		

		}else if(fromArduino.startsWith("READY AT")){	
			String fromArduino2 = fromArduino.replace("READY AT ", "");
			String[] tokens = fromArduino2.split(",");
	
			virtualComponents.set( "POSX",tokens[0]);
			virtualComponents.set( "POSY",tokens[1]);
			virtualComponents.set( "POSZ",tokens[2]);

		}else if(fromArduino.equals("PONG")){

		}else if(fromArduino.equals("PING")){
//			toSend.add("PONG");
		}
	}
	
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
/*
		// Called when there's a status to be updated
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
		}*/
/*
		// Called once the background activity has completed
	//	@Override
		protected void onPostExecute(String result) {
		}*/
	}
}
