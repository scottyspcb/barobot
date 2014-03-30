package com.barobot.hardware.devices.i2c;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.barobot.common.constant.Constant;
import com.barobot.common.constant.LowHardware;
import com.barobot.common.constant.Methods;
import com.barobot.parser.Queue;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.message.Mainboard;

public class I2C{
	public Carret carret			= null;
	public MainboardI2c mainBoard	= null;
	public List<Upanel> list		= new ArrayList<Upanel>();

	private static Map<Integer, I2C_Device> byaddress = new HashMap<Integer, I2C_Device>();
	private static Map<Integer, Upanel> bybottle = new HashMap<Integer, Upanel>();

	public I2C() {
		super();
		carret		= new Carret(Constant.cdefault_index, Constant.cdefault_address);
		mainBoard	= new MainboardI2c(Constant.mdefault_index, Constant.mdefault_address);
		mainBoard.hasResetTo( 2, carret );
		mainBoard.hasResetTo( 1, mainBoard );

		byaddress.put( mainBoard.getAddress(), mainBoard );		// index by ADDRESS
		byaddress.put( carret.getAddress(), carret );		// index by ADDRESS		
	}

	public I2C_Device getByAddress( int address ){
		return byaddress.get(address);
	}

	public Upanel getUpanelByBottle( int num ) {
		return bybottle.get(num);
	}
	public void add(Upanel u) {
		this.list.add(u);
		byaddress.put( u.getAddress(), u );		// index by ADDRESS
		bybottle.put( u.getBottleNum(), u );	// index by BUTTLE NUM
	}

	public Upanel[] getUpanels() {
		Upanel[] a = null;
		return bybottle.values().toArray(a);
	}

	
	
	
	
	
	
	private static String createCommand( int address, int why_code, byte[] params ){
		String res = ""+Methods.METHOD_SEND2SLAVE + (char)address + (char)why_code;
		if(params!=null){
			for(byte i=0;i<params.length;i++){
				res += (char)params[i];
			}
		}
		return res;
	}
	private static AsyncMessage send( int address, int why_code, byte[] params ){	
		String res = createCommand( address, why_code, params);
		AsyncMessage m =  new AsyncMessage( res, true, true );
		return m;
	}
	private static AsyncMessage send( int address, int why_code ){
		byte[] a = null;
		return send( address, why_code, a );
	}
	private static Queue findNodes(){
		Queue q = new Queue();
		q.add("I2C", true );
		// skanuj magistralę. Gdy znaleziono wyslij informację o sprzęcie
		AsyncMessage m =  new AsyncMessage( true ){
			@Override
			public String getName() {
				return "findNodes";
			}
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

	private static void temp( Queue mq ){
		mq.add( I2C.send( LowHardware.I2C_ADR_MAINBOARD, 0x11, new byte[]{1,2,3} ));
		mq.add( I2C.send( 0x12, 0x11, new byte[]{1,2,3} ));
		mq.add( I2C.findNodes() );
		// q.add( "RESETN 1", true );		// Reset Carret
		// q.add( "RESETN 1", true );		// Reset Carret

		mq.add( I2C.send( 0x12, 0x11, new byte[]{1,2,3} ));
	}

}
