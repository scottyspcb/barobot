package com.barobot.hardware.devices.i2c;

import java.util.ArrayList;
import java.util.List;

import com.barobot.common.constant.LowHardware;
import com.barobot.common.constant.Methods;
import com.barobot.parser.Queue;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.output.AsyncDevice;
import com.barobot.parser.output.Mainboard;

public class I2C{
	static List<I2C_Device> lista = new ArrayList<I2C_Device>();
	boolean inScanning = false;

	public static void init(){
		MainBoard MainBoard	= new MainBoard();
		I2C_Device Carret	= new Carret();
		I2C_Device UpanelF0 = new Upanel( 3, 0);
		I2C_Device UpanelF1 = new Upanel( 0, 0);
		I2C_Device UpanelF2 = new Upanel( 0, 0);
		I2C_Device UpanelF3 = new Upanel( 0, 0);
		I2C_Device UpanelF4 = new Upanel( 0, 0);
		I2C_Device UpanelF5 = new Upanel( 0, 0);
		I2C_Device UpanelB0 = new Upanel( 4, 0);
		I2C_Device UpanelB1 = new Upanel( 0, 0);
		I2C_Device UpanelB2 = new Upanel( 0, 0);
		I2C_Device UpanelB3 = new Upanel( 0, 0);
		I2C_Device UpanelB4 = new Upanel( 0, 0);
		I2C_Device UpanelB5 = new Upanel( 0, 0);

		MainBoard.hasResetTo( Carret );
		MainBoard.hasResetTo( UpanelF0 );
		MainBoard.hasResetTo( UpanelB0 );
		MainBoard.hasResetTo( MainBoard );


		UpanelB0.hasResetTo( UpanelB1 );
		UpanelB1.hasResetTo( UpanelB2 );
		UpanelB2.hasResetTo( UpanelB3 );
		UpanelB3.hasResetTo( UpanelB4 );
		UpanelB4.hasResetTo( UpanelB5 );


		
		UpanelF0.hasResetTo( UpanelF1 );
		UpanelF1.hasResetTo( UpanelF2 );
		UpanelF2.hasResetTo( UpanelF3 );
		UpanelF3.hasResetTo( UpanelF4 );
		UpanelF4.hasResetTo( UpanelF5 );



		lista.add( MainBoard );
		lista.add( Carret );
		lista.add( UpanelF0 );
		lista.add( UpanelF1 );
		lista.add( UpanelF2 );
		lista.add( UpanelF3 );
		lista.add( UpanelF4 );
		lista.add( UpanelF5 );
		lista.add( UpanelB0 );
		lista.add( UpanelB1 );
		lista.add( UpanelB2 );
		lista.add( UpanelB3 );	
		lista.add( UpanelB4 );
		lista.add( UpanelB5 );
	}
	public static void destroy(){
		lista.clear();
	}
	public static I2C_Device getByAddress( int address ){
		I2C_Device t = null;
		for (I2C_Device v : lista){
		    System.out.print(v + " ");
		}
		return t;
	}
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
			public Queue run(AsyncDevice dev, Queue queue) {
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
