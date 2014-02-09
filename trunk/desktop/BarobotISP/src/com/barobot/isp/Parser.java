package com.barobot.isp;

public class Parser {
	public static int[] decodeBytes(String fromArduino) {
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
}
