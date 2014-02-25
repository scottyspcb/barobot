package com.barobot.isp.parser;

public class SerialInputBuffer {
	public static String separator = "\n";
	public static LineReader lr = null;
	static StringBuilder buffer2 = new StringBuilder();
/*
	public static void readInput(char[] readBuffer, int i, int bytesRead) {
		synchronized (SerialInputBuffer.class) {
			SerialInputBuffer.buffer2.append(readBuffer, 0, bytesRead);
		//	System.out.println("parse: " + SerialInputBuffer.buffer2.toString());

			int end = SerialInputBuffer.buffer2.indexOf(SerialInputBuffer.separator);
			if( end!=-1){
				while( end != -1 ){		// podziel to na kawalki
					String command				= SerialInputBuffer.buffer2.substring(0, end);
					SerialInputBuffer.buffer2	= SerialInputBuffer.buffer2.delete(0, end+1);

					command				= command.trim();
		//			System.out.println("command: " + command);
					if("".equals(command)){
			//			Log.i(Constant.TAG, "pusta komenda!!!]");
					}else if(lr != null){
						lr.read_line(command);
					}
					end		= SerialInputBuffer.buffer2.indexOf(separator);
				}
			}
        }
	}
	*/
	public static void clear() {
		synchronized (SerialInputBuffer.class) {
			SerialInputBuffer.buffer2 =  new StringBuilder();
		}
	}
	public static void readInput(String in) {
		synchronized (SerialInputBuffer.class) {
			SerialInputBuffer.buffer2.append(in);
		//	System.out.println("parse: " + SerialInputBuffer.buffer2.toString());

			int end = SerialInputBuffer.buffer2.indexOf(SerialInputBuffer.separator);
			if( end!=-1){
				while( end != -1 ){		// podziel to na kawalki
					String command				= SerialInputBuffer.buffer2.substring(0, end);
					SerialInputBuffer.buffer2	= SerialInputBuffer.buffer2.delete(0, end+1);

					command				= command.trim();
		//			System.out.println("command: " + command);
					if("".equals(command)){
			//			Log.i(Constant.TAG, "pusta komenda!!!]");
					}else if(lr != null){
						lr.read_line(command);
					}
					end		= SerialInputBuffer.buffer2.indexOf(separator);
				}
			}
        }
	}
}
