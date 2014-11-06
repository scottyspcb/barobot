package com.barobot.debug;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.barobot.BarobotMain;
import com.barobot.R;
import com.barobot.common.Initiator;
import com.barobot.common.constant.Constant;
import com.barobot.common.interfaces.OnDownloadReadyRunnable;
import com.barobot.gui.dataobjects.Engine;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.other.Android;
import com.barobot.other.InternetHelpers;
import com.barobot.other.UpdateManager;
import com.barobot.parser.Queue;
public class button_click implements OnClickListener{
	private Activity dbw;
	public button_click(Activity debugWindow){
		dbw = debugWindow;
	}
	@Override
	public void onClick(final View v) {		// get out of the UI thread
		Log.i("button click","click");
		new Thread( new Runnable(){
			@Override
			public void run() {
				Log.i("button click","exec start");
				exec(v);
				Log.i("button click","exec end");
			}}).start();
	}
	public void exec(View v) {
		final BarobotConnector barobot	= Arduino.getInstance().barobot;

		switch (v.getId()) {
		case R.id.download_database:
			BarobotMain.getInstance().runOnUiThread(new Runnable() {
				  public void run() {
			    	new AlertDialog.Builder(dbw).setTitle("Are you sure?").setMessage("Are you sure?")
				    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int which) {
				    //    	boolean success = false;
				        	String path6 = 	Environment.getExternalStorageDirectory()+ Constant.copyPath;
				  			InternetHelpers.doDownload(Constant.databaseWeb, path6, new OnDownloadReadyRunnable() {
				  				public void sendSource( String source ) {	
				  				}
				  			    @Override
				  				public void run() {
				  			    	alertOk();
				  				}
								@Override
								public void sendProgress(int value) {
									// TODO Auto-generated method stub
								}
				  			});
				        }
				    }).setIcon(android.R.drawable.ic_dialog_alert).show();
			    }
			});
			break;

		case R.id.reset_database:
			BarobotMain.getInstance().runOnUiThread(new Runnable() {
				  public void run() {
			    	new AlertDialog.Builder(dbw).setTitle("Are you sure?").setMessage("Are you sure?")
				    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int which) { 
				        	boolean success = false;
				        	String error_name = "Error";
				        	try {
				        		Date dNow			= new Date( );
				        		SimpleDateFormat dd =  new SimpleDateFormat ("yyyy.MM.dd.hh.mm.ss");
				        		String resetPath	= 	Environment.getExternalStorageDirectory()+ Constant.copyPath;
				        		String backupPath 	= 	Environment.getExternalStorageDirectory()+ Constant.backupPath;
				        		backupPath 			= 	backupPath.replace("%DATE%", dd.format(dNow));
				        		Initiator.logger.i(Constant.TAG,"backupPath path" + backupPath);

				        		// do backup
				        		success = Android.copy( Constant.localDbPath, backupPath );
				        		if(success){
				        			success = Android.copy( resetPath, Constant.localDbPath );
				        			Engine.GetInstance().invalidateData();
				        			resetApplicationData(barobot);
				        		}
							} catch (IOException e) {
								e.printStackTrace();
								success		= false;
								error_name	= e.getMessage();
								Initiator.logger.i(Constant.TAG,"download_database", e);
							}
				        	final String message = success ? "OK": error_name;
							BarobotMain.getInstance().runOnUiThread(new Runnable() {
								  public void run() {
							    	new AlertDialog.Builder(dbw).setTitle("Message").setMessage( message )
								    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
								        public void onClick(DialogInterface dialog, int which) { 
								        	pleaseResetApp();
								        }
								    }).setIcon(android.R.drawable.ic_dialog_alert).show();
							    }
							});
				        }
				    }).setIcon(android.R.drawable.ic_dialog_alert).show();
			    }
			});
			break;
		case R.id.firmware_download:
			UpdateManager.downloadAndBurnFirmware( dbw, barobot.use_beta, false );
			break;

		case R.id.firmware_download_manual:
			UpdateManager.downloadAndBurnFirmware( dbw, barobot.use_beta, true );
			break;
		case R.id.new_robot_id:
			int isOnline = Android.isOnline(dbw);
			if(isOnline > -1 && barobot.use_beta ){					// beta only
				int robot_id = UpdateManager.getNewRobotId();		// download new robot_id (init hardware)
				Initiator.logger.w("button_click.new_robot_id", "robot_id" + robot_id);
				if( robot_id > 0 ){		// save robot_id to android and arduino
					Queue q = new Queue();
					barobot.setRobotId( q, robot_id);
					barobot.main_queue.add(q);
				}
				Android.alertMessage(dbw, "New ID= "+ robot_id);
			}else{
				Android.alertMessage(dbw,"No internet connection");
			}
			break;
		}
	}
	private void pleaseResetApp() {
	    	BarobotMain.getInstance().runOnUiThread(new Runnable() {
			  public void run() {
				  new AlertDialog.Builder(dbw).setTitle("Message").setMessage("Please restart application")
				    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int which) { 
				        }
				    })
				    .setIcon(android.R.drawable.ic_dialog_alert).show();
			  }
			});
	}
	private void alertOk() {
		BarobotMain.getInstance().runOnUiThread(new Runnable() {
			  public void run() {
				  new AlertDialog.Builder(dbw).setTitle("Message").setMessage("It is pleasure to inform that operation was completed successfully.")
				    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int which) { 
				        }
				    })
				    .setIcon(android.R.drawable.ic_dialog_alert).show();
			  }
			});
	}

	private void resetApplicationData(BarobotConnector barobot) {
		barobot.state.set("ONCE_PER_APP_START", 0 );
		barobot.state.set("ONCE_PER_ROBOT_START", 0 );
		barobot.state.set("ONCE_PER_ROBOT_LIFE", 0 );
		barobot.state.set("ROBOT_CAN_MOVE", 0 );
		barobot.state.set("ROBOT_ID", 0 );
		barobot.state.set("INIT", 0 );
	}
}
