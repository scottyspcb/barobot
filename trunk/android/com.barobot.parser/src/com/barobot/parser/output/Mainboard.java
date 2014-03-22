package com.barobot.parser.output;

import com.barobot.common.Initiator;
import com.barobot.parser.utils.Decoder;

public class Mainboard extends AsyncDevice{
	public Mainboard() {
		super("Mainboard");
		this.addGlobalModifier( new GlobalInputModifier("^25", "125"));		// add 1 if num < 100
		this.addGlobalModifier( new GlobalInputModifier("^RR", "R"));		// RR => R
	}
	@Override
	public boolean parse(String in) {
		//Parser.log(Level.INFO, "parse: " + in);
		// all other messages
		if( in.startsWith( "-") ){			// comment
			Initiator.logger.i("Mainboard.parse.comment", in);
			return true;
		}else{
			Initiator.logger.i("Mainboard.parse", in);	
			Initiator.logger.i("Mainboard.parse", Decoder.toHexStr(in.getBytes(), in.length()));
		}
/*
		if( in.startsWith( "" + METHOD_DEVICE_FOUND + ",") ){			// device found
			 int[] parts = Decoder.decodeBytes( in );
			 // Znaleziono urzadzenie
			 int address = parts[1];
		
		if( in.startsWith( "" + Methods.METHOD_CHECK_NEXT + ",") ){			// METHOD_I2C_SLAVEMSG
			 int[] parts = Decoder.decodeBytes( in );
			 if( parts.length > 3 && parts[ 2 ] ==  188 ){
				 Parser.last_has_next	= parts[ 3 ];		// 0 or 1	
			 }
		}*/
		return false;
	}
}
