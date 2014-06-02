package com.barobot.hardware.devices;

import com.barobot.common.Initiator;
import com.barobot.common.constant.Methods;
import com.barobot.common.interfaces.HardwareState;
import com.barobot.parser.Queue;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.message.Mainboard;
import com.barobot.parser.utils.Decoder;

public class MotorDriver {
	int hardware_pos		= 0;
	int software_pos		= 0;
	int direction			= Methods.DRIVER_DIR_STOP;
	public int defaultSpeed = 0;
	public String axis		= "X";

	int m1 = 0;
	int m2 = 0;
	//	todo s = (( (h * p1 + m1) / d ) + m2) * p2 
	private HardwareState state;

	public MotorDriver(HardwareState state ){
		this.state = state;
		this.setM( state.getInt( "MARGINX", 0 ) );
		//this.setSPos( state.getInt( "POSX", 0 ) );
	}	
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
	public void setSPos( int spos ){
		software_pos = spos;
		hardware_pos = software_pos + m1;
		state.set( "POSX", software_pos );
		int lx	=  state.getInt("LENGTHX", 600 );
		if( software_pos > lx){		// Pozycja wieksza niz d³ugosc? Zwieksz d³ugosc
			state.set( "LENGTHX", software_pos);
		}
	//	Initiator.logger.w("MotorDriver setSPos1", "" + software_pos + ", setHPos1: "+hardware_pos );
	}
	public void setHPos( int hpos ){
		hardware_pos = hpos;
		software_pos = hardware_pos - m1;
		state.set( "POSX", software_pos );
		int lx	=  state.getInt("LENGTHX", 600 );
		if( software_pos > lx){		// Pozycja wieksza niz d³ugosc? Zwieksz d³ugosc
			state.set( "LENGTHX", software_pos);
		}
	//	Initiator.logger.w("MotorDriver setSPos2", "" + software_pos+ ", setHPos2: "+hardware_pos );
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
	public void moveTo( Queue q, final int pos ) {
		final int newx		= soft2hard(pos);
		final int currentx	= getSPos();

		q.add( new AsyncMessage( true, true ) {
			@Override
			public String getName() {
				return axis + " moveTo logic";
			}
			@Override
			public boolean isRet(String result, Queue mainQueue) {
				return false;
			}
			@Override
			public Queue run(Mainboard dev, Queue queue){
				this.name		= "Check Hall X";
		//		Initiator.logger.w("MotorDriver.movoTo.AsyncMessage.run", "want to s:" + pos + " / hpos" + newx );
				queue.sendNow("A0");
				return null;
			}
			@Override
			public boolean onInput(String input, Mainboard dev, Queue mainQueue) {
	//			Initiator.logger.w("MotorDriver.movoTo.AsyncMessage.onInput", input );
				if(input.matches("^" +  Methods.METHOD_IMPORTANT_ANALOG + ",0,.*" )){		//	224,0,66,0,208,7,15,2
					int[] parts = Decoder.decodeBytes( input );
					boolean can = true;
					if( parts[2] == Methods.HX_STATE_9 ){		// this is max	//	224,0,100,0,204,3,185,1
						if(pos < currentx ){		// move backward
							can = false;
	//						Initiator.logger.w("MotorDriver.movoTo.AsyncMessage.onInput2", "BELOW MIN1 newx: "+ pos+ "currentx:"+currentx   );
						}
					}else if( parts[2] == Methods.HX_STATE_1 ){		// this is min
						if( pos > currentx ){		// move forward
							can = false;
	//						Initiator.logger.w("MotorDriver.movoTo.AsyncMessage.onInput2", "OVER max newx: "+ pos+ "currentx:"+currentx  );
						}
					}
					if( can ){
	//					Initiator.logger.w("MotorDriver.movoTo.AsyncMessage.onInput", "MOVE" );
						Queue	q2	= new Queue(); 
						q2.add("X" + newx+ ","+defaultSpeed, true);
						mainQueue.addFirst(q2);
						dev.unlockRet(this, "A0 OK");
						return true;
					}
					dev.unlockRet(this, "A0 FAIL");
				}
				return false;
			}
		});
	}
}
