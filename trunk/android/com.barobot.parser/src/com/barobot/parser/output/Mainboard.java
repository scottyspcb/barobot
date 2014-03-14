package com.barobot.parser.output;

import com.barobot.parser.message.AsyncMessage;

public class Mainboard extends AsyncDevice{
	public Mainboard() {
		super("Mainboard");
	}
	@Override
	public boolean parse(String in) {
		//Parser.log(Level.INFO, "parse: " + in);
		// all other messages
		if( in.startsWith( "-") ){			// comment
			System.out.println("arduino comment: " + in);
			return true;
		}else{
	//		System.out.println("read1: " + in);	
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

	public boolean isRetOf56(AsyncMessage msg, String result) {
	//	dfgf;
		result =result.trim().toUpperCase();
		String command2	= msg.command.toUpperCase();

		if( msg.isBlocing()){
			if( result.startsWith("RX") && command2.startsWith("X") ){
				return true;
			}
			if( result.startsWith("RY") && command2.startsWith("Y") ){
				return true;
			}
			if( result.startsWith("RZ") && command2.startsWith("Z") ){
				return true;
			}
			if( result.startsWith( "R" + command2 )){
				return true;
			}
			if( result.startsWith( "E" + command2)){		// error tez odblokowuje
				return true;
			}
		}
		return false;
	}
}
