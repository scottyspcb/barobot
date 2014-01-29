package com.barobot_graph;

import android.util.Log;
public class input_parser {
	private static String buffer = "";
	public static String separator = "\n";

	public static void readInput(final String in) {
		// if(null == in){
		// return;
		// }
		synchronized (input_parser.class) {
			input_parser.buffer = input_parser.buffer + in;
			// Log.v(Constant.TAG, "input [" + input_parser.buffer+"]" );
			int end = input_parser.buffer.indexOf(separator);
			if (end != -1) {
				while (end != -1) { // podziel to na kawalki
					// Log.i(Constant.TAG, "input [" +
					// input_parser.buffer+"] dł:" +input_parser.buffer.length()
					// );
					String command = input_parser.buffer.substring(0, end);
					input_parser.buffer = input_parser.buffer
							.substring(end + 1);
					command = command.trim();
					// Log.i(Constant.TAG, "zostalo [" + input_parser.buffer
					// +"]");
					if ("".equals(command)) {
						Log.i(Constant.TAG, "posta komenda!!!]");
					} else {
						parseInput(command);
						Arduino.getInstance().debug(command + "\n");
					}
					end = input_parser.buffer.indexOf(separator);
					//
				}
			}
		}
	}

	private static void parseInput(String fromArduino) {
		if (fromArduino.length() == 0) {
			return;
		}
		//Log.i(Constant.TAG, "parse:[" + fromArduino + "]");
		int b  = fromArduino.indexOf(",");
		if( b > -1 ){
		//	int[] values2 = decodeBytes(fromArduino);
		//	value = value / BGraph.graph_repeat;
			AJS aa = AJS.getInstance();			
			if(fromArduino.lastIndexOf(",") == fromArduino.length()-1){
				fromArduino = fromArduino.substring(0,  fromArduino.length()-1 );
			}
			BGraph.getInstance().setText(R.id.input_field, fromArduino);
			if (aa != null) {
				aa.oscyloskop(fromArduino);
			}
		}else if(fromArduino.startsWith("state")){
			String[] parts = fromArduino.split(" ");
			for( int i=0;i<parts[1].length();i++){
				int port = toInt(""+parts[1].charAt(i));
				BGraph.getInstance().port_enabled(port, true );
			}
		}else if(fromArduino.startsWith("t")){
		}else if(fromArduino.startsWith("s")){
		}else if(fromArduino.startsWith("r")){	
		}
	}

	private static int[] decodeBytes(String fromArduino) {
		String[] parts = fromArduino.split(",");
		int[] iparts = new int[parts.length];
		for (byte i = 0; i < parts.length; i++) {
			iparts[i] = toInt(parts[i]);
		}
		return iparts;
	}

	public static int toInt(String input) {
		input = input.replaceAll("[^-\\d]", "");
		int res = 0;
		try {
			res = Integer.parseInt(input);
		} catch (NumberFormatException e) {
			return 0;
		}
		// Constant.log(Constant.TAG,"toInt:"+ input + "/ "+ res );
		return res;
	}
}
