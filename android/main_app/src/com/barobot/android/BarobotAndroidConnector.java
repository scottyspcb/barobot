package com.barobot.android;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;

import com.barobot.BarobotMain;
import com.barobot.common.constant.Constant;
import com.barobot.common.interfaces.HardwareState;
import com.barobot.gui.dataobjects.Log_start;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.parser.Queue;
import com.barobot.parser.utils.Decoder;

public class BarobotAndroidConnector extends BarobotConnector {
	public BarobotAndroidConnector(HardwareState state) {
		super(state);
	}

	@Override
	public void onFirstStatReady(Queue mq ) {
		Log_start ls 			= new com.barobot.gui.dataobjects.Log_start();
		ls.datetime				= Decoder.getTimestamp() ;
		ls.start_type			= "hardware";
		ls.robot_id				= this.getRobotId();
		ls.language				= this.state.get("LANG", "pl" );
		ls.app_starts			= this.state.getInt("STAT1", 0);
		ls.arduino_starts		= this.state.getInt("ARDUINO_STARTS", 0);
		ls.serial_starts		= this.state.getInt("STAT2", 0);
		ls.app_version			= Constant.ANDROID_APP_VERSION;
		ls.arduino_version		= this.state.getInt("ARDUINO_VERSION", 0);
		//ls.database_version		= Constant.ANDROID_APP_VERSION;
		ls.temp_start			= this.getLastTemp();
		ls.insert();
	}
	protected void showError(final String msg) {
		final BarobotMain activity = BarobotMain.getInstance();
		
		activity.runOnUiThread(new Runnable() {
			  public void run() {
				  Builder bb = new AlertDialog.Builder(activity).setTitle("Error").setMessage(msg)
				    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int which) { 
				        }
				    });
				  bb.setIcon(android.R.drawable.ic_dialog_alert);
				  if (!activity.isFinishing()) {
					  bb.show();
				  }
			  }
			});
	}
}
