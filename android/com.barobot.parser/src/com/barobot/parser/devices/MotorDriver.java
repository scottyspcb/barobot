package com.barobot.parser.devices;

import com.barobot.common.Initiator;
import com.barobot.common.constant.Methods;
import com.barobot.parser.Queue;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.output.AsyncDevice;
import com.barobot.parser.utils.Decoder;

public class MotorDriver {
	int hardware_pos		= 0;
	int software_pos		= 0;
	int direction			= Methods.DRIVER_DIR_STOP;
	public int defaultSpeed = 0;

	int m1 = 0;
	int m2 = 0;
	//	todo s = (( (h * p1 + m1) / d ) + m2) * p2 

	public void setM( int margin1 ){
		m1 = margin1;
		Initiator.logger.w("set MARGIN X", "" + m1);
	}
	public int getSPos(){
		return software_pos;
	}
	public int getHPos(){
		return hardware_pos;
	}
	public void setSPos( int pos2 ){
		software_pos = pos2;
		hardware_pos = software_pos + m1;	
	}
	public void setHPos( int pos2 ){
		hardware_pos = pos2;
		software_pos = hardware_pos - m1;
	}
	public int getDirection() {
		return direction;
	}
	public void setDirection(int direction) {
		this.direction = direction;
	}
	/*
	hardw		softw	margin
	-4000		 = 0	-4000
	*/
	public int hard2soft( int pos2 ){
	//	Initiator.logger.w("MARGIN X1", "Margin: " + m1 + "  hard: " + pos2 + "=> soft " + (pos2 -m1) );
		return pos2 - m1;
	}
	public int soft2hard( int pos3 ){
	//	Initiator.logger.w("MARGIN X2", "Margin: " + m1 + "  soft: " + pos3 + " => hard " + (pos3 -( -m1)));
		return pos3 - (- m1);
	}
	public void movoTo( final Queue q, int pos ) {
		final int newx		= this.soft2hard(pos);
		final int currentx	= software_pos;

		q.add( new AsyncMessage( true, true ) {
			@Override
			public Queue run(AsyncDevice dev) {
				this.name		= "Check Hall X";
				q.sendNow(Queue.DFAULT_DEVICE, "A0");
				Initiator.logger.w("MotorDriver.movoTo.AsyncMessage.run", "sendNow" );
				return null;
			}
			@Override
			public boolean onInput(String input, AsyncDevice dev ) {
				Initiator.logger.w("MotorDriver.movoTo.AsyncMessage.onInput", input );
				if(input.matches("^" +  Methods.METHOD_IMPORTANT_ANALOG + ",0,.*" )){		//	224,0,66,0,208,7,15,2
					int[] parts = Decoder.decodeBytes( input );
					boolean can = true;
					if( parts[2] == Methods.HX_STATE_9 ){		// this is max	//	224,0,100,0,204,3,185,1
						if(newx < currentx ){		// move backward
							can = false;
							Initiator.logger.w("MotorDriver.movoTo.AsyncMessage.onInput", "OVER max" );
						}
					}else if( parts[2] == Methods.HX_STATE_1 ){		// this is min
						if( newx > currentx ){		// move forward
							can = false;
							Initiator.logger.w("MotorDriver.movoTo.AsyncMessage.onInput", "BELOW MIN" );
						}
					}
					if( can ){
						Initiator.logger.w("MotorDriver.movoTo.AsyncMessage.onInput", "MOVE" );
						Queue	q2	= new Queue();
						q2.add("X" + newx+ ","+defaultSpeed, true);
						q.addFirst(q2);
						dev.unlockRet(this, "A0 OK");
						return true;
					}
				}
				return false;
			}
		} );
	}
}
