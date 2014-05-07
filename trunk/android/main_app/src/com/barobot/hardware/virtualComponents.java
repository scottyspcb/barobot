package com.barobot.hardware;

import com.barobot.android.AndroidBarobotState;
import com.barobot.common.Initiator;
import com.barobot.common.constant.Constant;
import com.barobot.common.interfaces.HardwareState;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.hardware.devices.i2c.Upanel;
import com.barobot.parser.Queue;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.message.Mainboard;

import android.app.Activity;
import android.graphics.Color;

public class virtualComponents {
	public static boolean need_glass_up = false;
	public static boolean pac_enabled = true;
	public static boolean set_bottle_on = false;
	public static BarobotConnector barobot;

	public static void init( Activity app ){
		HardwareState state	= new AndroidBarobotState(app);	
		barobot				= new BarobotConnector( state );
		state.set("show_unknown", 1 );
		state.set("show_sending", 1 );
		state.set("show_reading", 1 );
	}

	public static void pacpac() {
		Queue q = getMainQ();
		Initiator.logger.i(Constant.TAG,"pac");
	//	q.add( moveX );
		q.add("EX", true);
//		q.add("EY", true);
//		q.add("EZ", true);
		q.add("Z" + Constant.SERVOZ_PAC_POS+","+Constant.DRIVER_Z_SPEED, true);
		Initiator.logger.i("pacpac","Z" + Constant.SERVOZ_PAC_POS+","+Constant.DRIVER_Z_SPEED);
		barobot.moveZDown(q, true );
		q.add("DY", true);
		q.add("DX", true);
		q.addWait(200);
		q.add("DZ", true);
	}

	public static void enable_analog( Queue q, int pin, int time, int repeat) {
		q.add("LIVE A "+pin+","+time+","+repeat, false);		// repeat pomiary co time na porcie pin
	}
	public static void disable_analog(Queue q, int analogWaga) {
		q.add("LIVE A OFF", false);
	}
	public static Queue getMainQ() {
		return virtualComponents.barobot.main_queue;
	}
	public static void setLedsOff(String string ) {
		Queue q1		= new Queue();	
		Upanel[] up		= barobot.i2c.getUpanels();
		for(int i =0; i<up.length;i++){
			up[i].addLed(q1, "ff", 0);
		}
		Queue q			= virtualComponents.barobot.main_queue;
		q.add(q1);
	}

	public static void setColor(String leds, int color) {
		int blue	= Color.blue(color);
    	int red		= Color.red(color);
    	int green	= Color.green(color);
    	int white	= 0;
		Queue q1	= new Queue();
		Upanel[] up = barobot.i2c.getUpanels();
		for(int i =0; i<up.length;i++){
			up[i].setRgbw(q1, red,green,blue,white);
		}
		Queue q			= virtualComponents.barobot.main_queue;
		q.add(q1);
	}
}
