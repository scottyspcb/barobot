package com.barobot.parser.output;

import com.barobot.common.Initiator;

public class Mainboard extends AsyncDevice{
	public Mainboard() {
		super("Mainboard");
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
		}
/*
		if( in.startsWith( "12,") ){			// device found
			 int[] parts = Decoder.decodeBytes( in );
			 // Znaleziono urzadzenie
			 int address = parts[1];
		
		if( in.startsWith( "122,") ){			// METHOD_I2C_SLAVEMSG
			 int[] parts = Decoder.decodeBytes( in );
			 if( parts.length > 3 && parts[ 2 ] ==  188 ){
				 Parser.last_has_next	= parts[ 3 ];		// 0 or 1	
			 }
		}*/
		return false;
	}
}
