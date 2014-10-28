package com.barobot.parser.utils;

import com.barobot.common.Initiator;

public class Decoder {
	public static int[] decodeBytes(String fromArduino) {
		String[] parts = fromArduino.split(",");
		int[] iparts = new int[parts.length];
		for(byte i=0;i<parts.length;i++){
			iparts[ i ] = toInt(parts[i], 0);
		}
		return iparts;
	}
	public static int toInt( String input ){
		return toInt(input, 0 );
	}
	public static int toInt( String input, int defaultVal ){
	//	input	= input.replaceAll( "[^-\\d]", "" );
		try {
			return Integer.parseInt(input);
		} catch (NumberFormatException e) {
			return defaultVal;
		}
	//	Initiator.logger.i(Constant.TAG,"toInt:"+ input + "/ "+ res );
	}
	public static int fromHex(String input, int defaultVal) {
		input	= input.replaceAll( "[^-\\d]", "" );
		int res = 0;
		try {
			res = Integer.parseInt(input, 16 );
		} catch (NumberFormatException e) {
			return defaultVal;
		}
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
	public static String toHexByte(int oneByte) {
		int oneByte2	= (oneByte & 0xff);
	//	Initiator.logger.i("Decoder","toHexByte:"+ oneByte + "/ "+ oneByte2 + " / "+ (0x100 | oneByte2) + " / "+ Integer.toHexString(0x100 | oneByte2));
		return Integer.toHexString(0x100 | oneByte2).substring(1);
	}
	
}

