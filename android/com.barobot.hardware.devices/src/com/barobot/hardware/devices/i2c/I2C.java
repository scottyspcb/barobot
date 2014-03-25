package com.barobot.hardware.devices.i2c;

import java.util.ArrayList;
import java.util.List;

import com.barobot.common.constant.LowHardware;
import com.barobot.common.constant.Methods;
import com.barobot.parser.Queue;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.message.Mainboard;

public class I2C{
	static List<I2C_Device> lista = new ArrayList<I2C_Device>();
	boolean inScanning = false;

	public static String createCommand( int address, int why_code, byte[] params ){
		String res = ""+Methods.METHOD_SEND2SLAVE + (char)address + (char)why_code;
		if(params!=null){
			for(byte i=0;i<params.length;i++){
				res += (char)params[i];
			}
		}
		return res;
	}
	public static AsyncMessage send( int address, int why_code, byte[] params ){	
		String res = createCommand( address, why_code, params);
		AsyncMessage m =  new AsyncMessage( res, true, true );
		return m;
	}
	public static AsyncMessage send( int address, int why_code ){
		byte[] a = null;
		return send( address, why_code, a );
	}
	public static Queue findNodes(){
		Queue q = new Queue();
		q.add("I2C", true );
		// skanuj magistralę. Gdy znaleziono wyslij informację o sprzęcie
		AsyncMessage m =  new AsyncMessage( true ){
			@Override
			public boolean isRet( String command, Queue queue ){
				if (command == "EI2C"){	// jesli nie znaleziono zadnego
					// clear active devices
					return true;
				}
				return false;
			}
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				return null;
			}
		};
		q.add( m );
		return q;
	}

	public static void temp( Queue mq ){
		mq.add( I2C.send( LowHardware.I2C_ADR_MAINBOARD, 0x11, new byte[]{1,2,3} ));
		mq.add( I2C.send( 0x12, 0x11, new byte[]{1,2,3} ));
		mq.add( I2C.findNodes() );
		// q.add( "RESETN 1", true );		// Reset Carret
		// czekam na zwrotkę
		// q.add( "RESETN 1", true );		// Reset Carret

		mq.add( I2C.send( 0x12, 0x11, new byte[]{1,2,3} ));
	}

}
