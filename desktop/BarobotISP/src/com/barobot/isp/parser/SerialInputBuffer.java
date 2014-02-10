package com.barobot.isp.parser;

public class SerialInputBuffer {
	public static char separator = '\n';
	private static String buffer = "";
	public static LineReader lr = null;
	
	public static void readInput( final String in ){
		synchronized (SerialInputBuffer.class) {
			SerialInputBuffer.buffer = SerialInputBuffer.buffer + in;
		//	System.out.println("readInput: " + in);

			int end = SerialInputBuffer.buffer.indexOf(separator);
			if( end!=-1){
				while( end != -1 ){		// podziel to na kawalki
					String command		= SerialInputBuffer.buffer.substring(0, end);
					SerialInputBuffer.buffer = SerialInputBuffer.buffer.substring(end+1);
					command				= command.trim();
			//		System.out.println("command: " + command);
					if("".equals(command)){
			//			Log.i(Constant.TAG, "pusta komenda!!!]");
					}else{
						if(lr != null){
							lr.read_line(command);
						}
					}
					end		= SerialInputBuffer.buffer.indexOf(separator);
				}
			}
        }
	}
	public static void clear() {
		synchronized (SerialInputBuffer.class) {
			buffer = "";
		}
	}
}
