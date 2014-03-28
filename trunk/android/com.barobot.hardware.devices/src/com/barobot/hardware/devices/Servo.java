package com.barobot.hardware.devices;

import com.barobot.common.Initiator;
import com.barobot.common.constant.Methods;
import com.barobot.common.interfaces.HardwareState;
import com.barobot.parser.Queue;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.message.Mainboard;
import com.barobot.parser.utils.Decoder;

public class Servo {
	public static final String AXIS_Y	= "Y";
	public static final String AXIS_Z	= "Z";

	public int defaultSpeed = 0;
	public String axis;

	protected int m1 = 0;
	protected int m2 = 0;
	protected int hardware_pos		= 0;
	protected int software_pos		= 0;
	protected int direction			= Methods.DRIVER_DIR_STOP;
	private HardwareState state;

	public Servo(HardwareState state, String axis ){
		this.state	= state;
		this.axis	= axis;
		this.setM( state.getInt( "MARGIN" + this.axis, 0 ) );
		this.setSPos( state.getInt( "POS" + this.axis, 0 ) );
	}
	public void setM( int margin1 ){
		m1 = margin1;
		//Initiator.logger.w("set MARGIN " + this.axis, "" + m1);
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
		state.set( "POS" + this.axis, software_pos );
		int lx	=  state.getInt("LENGTH" + this.axis, 600 );
		if( spos > lx){		// Pozycja wieksza niz d³ugosc? Zwieksz d³ugosc
			state.set( "LENGTH" + this.axis, spos);
		}
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
	public void moveTo( final Queue q, final int pos ) {
		final int newx		= soft2hard(pos);
		final int currentx	= getSPos();

		q.add( new AsyncMessage( true, true ) {
			@Override
			public String getName() {
				return axis + "moveTo logic";
			}
			@Override
			public boolean isRet(String result, Queue mainQueue) {
				return false;
			}
			@Override
			public Queue run(Mainboard dev, Queue queue){
				this.name		= "Check Hall " + axis;
				Initiator.logger.w("MotorDriver.movoTo.AsyncMessage.run", "want to s:" + pos + " / hpos" + newx );
				q.sendNow( "A0");
				return null;
			}
			@Override
			public boolean onInput(String input, Mainboard dev, Queue mainQueue) {
				Initiator.logger.w("MotorDriver.movoTo.AsyncMessage.onInput", input );
				if(input.matches("^" +  Methods.METHOD_IMPORTANT_ANALOG + ",0,.*" )){		//	224,0,66,0,208,7,15,2
					int[] parts = Decoder.decodeBytes( input );
					boolean can = true;
					if( parts[2] == Methods.HX_STATE_9 ){		// this is max	//	224,0,100,0,204,3,185,1
						if(pos < currentx ){		// move backward
							can = false;
							Initiator.logger.w("MotorDriver.movoTo.AsyncMessage.onInput2", "BELOW MIN1 newx: "+ pos+ "currentx:"+currentx   );
						}
					}else if( parts[2] == Methods.HX_STATE_1 ){		// this is min
						if( pos > currentx ){		// move forward
							can = false;
							Initiator.logger.w("MotorDriver.movoTo.AsyncMessage.onInput2", "OVER max newx: "+ pos+ "currentx:"+currentx  );
						}
					}
					if( can ){
						Initiator.logger.w("MotorDriver.movoTo.AsyncMessage.onInput", "MOVE" );
						Queue	q2	= new Queue(); 
						q2.add( axis + newx+ ","+defaultSpeed, true);
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
