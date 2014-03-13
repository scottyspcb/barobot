package com.barobot.parser.utils;

public class Decoder {
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
	public static String strRepeat(  String input, int times ){
		return new String(new char[times]).replace("\0", input );
	}
	public static String toHexStr(byte[] b, int length) {
        String str="";
        for(int i=0; i<length; i++) {
            str += String.format("%02x ", b[i]);
        }
        return str;
    }
	public static String toHexStr(byte b) {
		return String.format("0x%02x", b);
	}
}

