package com.barobot.android;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;

import com.barobot.BarobotMain;
import com.barobot.common.Initiator;
import com.barobot.gui.database.BarobotData;
import com.barobot.gui.dataobjects.Slot;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.other.UpdateManager;
import com.barobot.parser.Queue;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.message.Mainboard;

public class AndroidWithBarobot {

	public static void pourFromBottle(int position, Queue q, boolean needGlass ) {
		BarobotConnector barobot = Arduino.getInstance().barobot;
		int robot_id = barobot.getRobotId();
		Slot slot = BarobotData.GetSlot(position+1);		// in db slots have numbers 1-12, 
		if( slot == null ){
			Initiator.logger.w("pourFromBottle", "no slot: " + position + " robot_id: " + robot_id);
		}else{
			barobot.moveToBottle(q, position, true);
			barobot.pour(q, slot.dispenser_type, position, true, needGlass );
		}
	}

	public static int checkNewSoftwareVersion(boolean alertResult, Activity act ) {
		int isOnline = Android.isOnline(act);
		if(isOnline > -1 ){	// check ne version of firmware and APK
			final BarobotConnector barobot = Arduino.getInstance().barobot;
			if( barobot.getRobotId() == 0 ){
				Queue q = new Queue();
				barobot.readHardwareRobotId(q);			// check hardware version
				q.add( new AsyncMessage( true ) {		// when version readed
					@Override
					public String getName() {
						return "Check robot_id";
					}
					@Override
					public Queue run(Mainboard dev, Queue queue) {
						if( barobot.getRobotId() == 0 && barobot.robot_id_ready ){						// once again
							int robot_id = UpdateManager.getNewRobotId();		// download new robot_id (init hardware)
							Initiator.logger.w("onResume", "robot_id" + robot_id);
							if( robot_id > 0 ){		// save robot_id to android and arduino
								Queue q = new Queue();
								barobot.setRobotId( q, robot_id);
								return q;	// before all other commands currently in queue
							}
						}
						return null;
					}
				});
				barobot.main_queue.add(q);
			}
			UpdateManager.checkNewVersion( act, alertResult );
			return 1;
		}else{
			if(alertResult){
				Android.alertMessage(act, "No connection");
			}
			return 0;
		}
	}

	public static void readTabletTemp(Queue q) {
		q.add( new AsyncMessage( true ) {
			@Override	
			public String getName() {
				return "check  tablet temp" ;
			}
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				Application ba = BarobotMain.getInstance().getApplication();
			    SensorManager mSensorManager = (SensorManager)ba.getSystemService(Context.SENSOR_SERVICE);
			    Sensor mTemperature = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE );
			    int power = Math.round(mTemperature.getPower() * 10000);
				BarobotConnector barobot = Arduino.getInstance().barobot;
				barobot.state.set("TABLET_TEMPERATURE", power );
				return null;
			}
		} );
	}

	public static void resetApplicationData(BarobotConnector barobot) {
		barobot.state.set("ONCE_PER_APP_START", 0 );
		barobot.state.set("ONCE_PER_ROBOT_START", 0 );
		barobot.state.set("ONCE_PER_ROBOT_LIFE", 0 );
		barobot.state.set("ROBOT_CAN_MOVE", 0 );
		barobot.state.set("ROBOT_ID", 0 );
		barobot.state.set("INIT", 0 );
	}

}
