package com.barobot.debug;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.barobot.BarobotMain;
import com.barobot.R;
import com.barobot.common.Initiator;
import com.barobot.common.constant.Constant;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.other.Android;
import com.barobot.other.InternetHelpers;
import com.barobot.other.OnDownloadReadyRunnable;
import com.barobot.other.update_drinks;
public class button_click implements OnClickListener{
	private Context dbw;
	public static boolean set_bottle_on = false;

	public button_click(Context debugWindow){
		dbw = debugWindow;
	}
	@Override
	public void onClick(final View v) {
		// get out of the UI thread
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
	//	Queue q			= new Queue();
	//	Queue mq		= barobot.main_queue;

		switch (v.getId()) {
		case R.id.download_database:
			BarobotMain.getInstance().runOnUiThread(new Runnable() {
				  public void run() {
			    	new AlertDialog.Builder(dbw).setTitle("Are you sure?").setMessage("Are you sure?")
				    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int which) { 
				        	boolean success = false;
				        	File dir5 = new File(Environment.getExternalStorageDirectory(), "Barobot");
				        	  if (!dir5.exists()) {
				        		  Android.createDirIfNotExists("Barobot");
				        	  }
				        	  String path6 = 	Environment.getExternalStorageDirectory()+ update_drinks.copyPath;

				  			InternetHelpers.doDownload(update_drinks.fulldb, path6, new OnDownloadReadyRunnable() {
				  				public void sendSource( String source ) {	
				  				}
				  			    @Override
				  				public void run() {
				  			    	Initiator.logger.i("firmware_download","hex ready");
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
				        	try {	
				        		Date dNow = new Date( );
				        		SimpleDateFormat dd =  new SimpleDateFormat ("yyyy.MM.dd.hh.mm.ss");
				        		String resetPath	= 	Environment.getExternalStorageDirectory()+ update_drinks.copyPath;
				        		String backupPath 	= 	Environment.getExternalStorageDirectory()+ update_drinks.backupPath;
				        		backupPath 			= 	backupPath.replace("%DATE%", dd.format(dNow));

				        		Initiator.logger.i(Constant.TAG,"backupPath path" + backupPath);

				        		// do backup
				        		success = InternetHelpers.copy( update_drinks.localDbPath, backupPath );
				        		if(success){
				        			success = InternetHelpers.copy( resetPath, update_drinks.localDbPath );
				        		}
							} catch (IOException e) {
								e.printStackTrace();
								Initiator.logger.i(Constant.TAG,"download_database", e);
							}
				        	final String message = success ? "OK": "Error";
							BarobotMain.getInstance().runOnUiThread(new Runnable() {
								  public void run() {
							    	new AlertDialog.Builder(dbw).setTitle("Message").setMessage( message )
								    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
								        public void onClick(DialogInterface dialog, int which) { 
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
			File dir = new File(Environment.getExternalStorageDirectory(), "Barobot");
			if (!dir.exists()) {
			  Android.createDirIfNotExists("Barobot");
			}
			String path9 = 	dir.getAbsolutePath()+"/"+"firmware.hex";
			InternetHelpers.doDownload(update_drinks.fulldb, path9, new OnDownloadReadyRunnable() {
				public void sendSource( String source ) {
				}
			    @Override
				public void run() {
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
			});
			break;		
		case R.id.firmware_burn:
			BarobotMain.getInstance().runOnUiThread(new Runnable() {
				  public void run() {
					  new AlertDialog.Builder(dbw).setTitle("Are you sure?").setMessage("Are you sure?")
					    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					        public void onClick(DialogInterface dialog, int which) { 
					        	fimwareBurn();
					        }
					    })
					    .setIcon(android.R.drawable.ic_dialog_alert).show();
				  }
				});
			break;

		case R.id.set_bottle:
			BarobotConnector barobot = Arduino.getInstance().barobot;
			int posx		= barobot.driver_x.getSPos();
			int posy		= barobot.state.getInt("POSY", 0 );

			set_bottle_on  = true;
			// przełącz okno na listę butelek, 
			// zablokuj przyciski i po naciśnięciu ustaw w tym miejscu butelkę
			Initiator.logger.i(Constant.TAG,"wybierz butelkę...");
			Toast.makeText(dbw, "Wybierz butelkę do zapisania pozycji " + posx + "/" + posy, Toast.LENGTH_LONG).show();
			break;
		}
	}
	protected void fimwareBurn() {
		File file = new File(Environment.getExternalStorageDirectory(), "Barobot/firmware.hex");
		if (!file.exists()) {
	       
			
			
			
	    }		
	}
}
