package com.barobot.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Handler;

import com.barobot.BarobotMain;
import com.barobot.common.Initiator;
import com.barobot.common.constant.Constant;
import com.barobot.gui.database.BarobotData;
import com.barobot.gui.dataobjects.Slot;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.other.UpdateManager;
import com.barobot.parser.Queue;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.message.Mainboard;

public class AndroidHelpers {

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
		if( isOnline > -1 ){	// check ne version of firmware and APK
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
								barobot.setRobotId( q, robot_id, barobot.pcb_type );
								return q;	// before all other commands currently in queue
							}
						}
						if( barobot.pcb_type == 0 && barobot.robot_id_ready){
							throw new RuntimeException("Brak pcb_type");
						}
						return null;
					}
				});
				barobot.main_queue.add(q);
			}
			if(barobot.pcb_type == 0){
				if(alertResult){
					Android.alertMessage(act, "Error 5422. Unknown PCB type");
				}
			}else{
				UpdateManager.checkNewVersion( act, barobot, alertResult );
			}
			return 1;
		}else{
			if(alertResult){
				Android.alertMessage(act, "Error 5322. No connection");
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
			    if(mTemperature!=null){
			    	int power = Math.round(mTemperature.getPower() * 10000);
			    	BarobotConnector barobot = Arduino.getInstance().barobot;
			    	barobot.state.set("TABLET_TEMPERATURE", power );
			    }
				return null;
			}
		} );
	}
	public static void resetApplicationData(BarobotConnector barobot) {
		barobot.state.resetToDefault("ONCE_PER_APP_START" );
		barobot.state.resetToDefault("ONCE_PER_ROBOT_START" );
		barobot.state.resetToDefault("ONCE_PER_ROBOT_LIFE" );
		barobot.state.resetToDefault("ROBOT_CAN_MOVE" );
		barobot.state.resetToDefault("ROBOT_ID" );
		barobot.state.resetToDefault("INIT" );
	}

	public static void askForClosingApp(BarobotConnector barobot) {
		final Activity dbw = BarobotMain.getInstance();
		dbw.runOnUiThread(new Runnable() {
			  public void run() {
				 final AlertDialog dialog = new AlertDialog.Builder(dbw).setTitle("Message").setMessage("Barobot is dosconnected. Do you want to close application?")
				    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int which) { 
				        	dbw.finish();
				        	System.exit(0);
				        }
				    })
				    .setPositiveButton(android.R.string.no, new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int which) { 
				        	
				        }
				    })
				    .setIcon(android.R.drawable.ic_dialog_alert).show();

				  new Handler().postDelayed(new Runnable() {
				        public void run() {                
				        	dialog.dismiss();         
				        }
				    }, Constant.AUTO_CLOSE_TIME_WITHOUT_BAROBOT ); 
			  }
			});
	}
}
