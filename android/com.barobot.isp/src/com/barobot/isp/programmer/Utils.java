package com.barobot.isp.programmer;

import com.barobot.common.Initiator;
import com.barobot.isp.Uploader;

public class Utils {
	public static String toHexStr(byte[] b, int length) {
	    String str="";
	    for(int i=0; i<length; i++) {
	        str += String.format("0x%02x ", b[i]);
	    }
	    return str;
	}
	public static String toHexStr(byte b) {
		return String.format("0x%02x", b);
	}
	public static void dumpHex(long byteLength, byte[] buf ) {
	    String str = "";
	    for(int i=0; i<16; i++) {
	        str += String.format("%02x ", buf[i]);
	    }
	    Initiator.logger.d(Uploader.TAG, "Hex Dump [0:16]: "+ str );
	    str = "";
	    for(int i=(int) (byteLength-16); i<byteLength; i++) {
	        str += String.format("%02x ", buf[i]);
	    }
	    Initiator.logger.d(Uploader.TAG, "Hex Dump ["+(byteLength-16)+":"+byteLength+"]: "+str);
	}
	static void dumpLogE(byte[] buf) {
	    if(Stk500.DEBUG_SHOW_DUMP_LOGE) {
	    	Initiator.logger.e(Stk500.TAG, "buffer("+buf.length+") : "+toHexStr(buf, buf.length));
	    }
	}
}
