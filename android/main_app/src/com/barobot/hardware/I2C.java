package com.barobot.hardware;

import com.barobot.constant.Methods;
import com.barobot.utils.Arduino;
import com.barobot.utils.ArduinoQueue;

public class I2C {

	public static String createCommand( int address, int why_code, byte[] params ){
		String res = ""+Methods.METHOD_SEND2SLAVE + (char)address + (char)why_code;
		if(params!=null){
			for(byte i=0;i<params.length;i++){
				res += (char)params[i];
			}
		}
		return res;
	}
	public static rpc_message send( int address, int why_code, byte[] params ){	
		String res = createCommand( address, why_code, params);
		rpc_message m =  new rpc_message( true, res, true );
		return m;
	}
	public static rpc_message send( int address, int why_code ){
		byte[] a = null;
		return send( address, why_code, a );
	}

	public static ArduinoQueue findNodes(){
		ArduinoQueue q = new ArduinoQueue();
		q.add("I2C", true );
		// skanuj magistralę. Gdy znaleziono wyslij informację o sprzęcie
		rpc_message m =  new rpc_message( true ){
			public boolean handle( String command ){
				if (command == "EI2C"){	// jesli nie znaleziono zadnego
					// clear active devices
					return true;
				}
				return false;
			}
			public ArduinoQueue run(){
				return null;
			}
		};	
		q.add( m );
		return q;
	}
	public static void findNodesOrder(){
		// resetuj wózek
		Arduino ar		= Arduino.getInstance();
		ArduinoQueue q = new ArduinoQueue();
		q.add( I2C.findNodes() );
		q.add( I2C.send( 0x12, 0x11, new byte[]{1,2,3} ));
		ar.send( q );
	}
}
