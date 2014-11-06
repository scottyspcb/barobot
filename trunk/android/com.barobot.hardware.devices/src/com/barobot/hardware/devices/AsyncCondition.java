package com.barobot.hardware.devices;

import com.barobot.common.constant.Methods;
import com.barobot.parser.Queue;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.message.Mainboard;

/*
 * 
 * Ask robot, anaalyse result and choose one of 2 command stream
 * 
 */
public class AsyncCondition extends AsyncMessage{
	public AsyncCondition(String name) {
		super(true);
		this.name			= name;
		this.blocking		= true;
		this.direction		= AsyncMessage.OUTOUT;
	}
	public void run( BarobotConnector barobot, Queue q) {
		q.add( new AsyncMessage( "A0", true ) {
			@Override
			public String getName() {
				return "Condition"+"Check magnet before Z UP";
			}
			@Override
			public boolean isRet(String result, Queue mainQueue) {
				if(result.matches("^" +  Methods.METHOD_IMPORTANT_ANALOG + ",0,.*" )){	// 125,0,100,0,0,0,255,202,126,1
			//		int[] parts = Decoder.decodeBytes( result );
					boolean allowUp		 = false;
					if( allowUp ){
						Queue	q2			= new Queue(); 
						mainQueue.addFirst(q2);
					}
				}
				return false;
			}
			@Override
			public boolean onInput(String input, Mainboard dev, Queue mainQueue) {
				if(input.equals("RA0")){		// its me
					return true;
				}else{
					return false;
				}
			}
		});
	}

	public void onFalse() {
		// TODO Auto-generated method stub
		
	}

	public void onTrue() {
		// TODO Auto-generated method stub
		
	}

	public void onError(String ret, int errorCode) {
		// TODO Auto-generated method stub
		
	}
}
