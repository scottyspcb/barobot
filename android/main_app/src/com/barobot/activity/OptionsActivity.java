package com.barobot.activity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.barobot.AppInvoker;
import com.barobot.BarobotMain;
import com.barobot.R;
import com.barobot.common.Initiator;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.other.Android;
import com.barobot.other.Audio;
import com.barobot.other.UpdateManager;
import com.barobot.parser.Queue;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.message.Mainboard;
import com.barobot.sofa.route.CommandRoute;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;

public class OptionsActivity extends BarobotMain {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_options);
	}
	static boolean wasVersionChecked = false;
	
	@Override
	protected void onResume() {
		super.onResume();
		this.checkVersion( false );
	}

	private int checkVersion( boolean force ) {
		if(!wasVersionChecked || force ){
			wasVersionChecked = true;
			int isOnline = Android.isOnline(this);
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
							if( barobot.getRobotId() == 0 ){						// once again
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
				UpdateManager.checkNewVersion( this );
			}else{
				return 0;
			}
		}
		return 1;
	}

	public void onOptionsButtonClicked(View view)
	{
		Intent serverIntent = null;
		final BarobotConnector barobot = Arduino.getInstance().barobot;
		Queue mq;
		switch(view.getId()){
		case R.id.options_bottle_setup_button:
			serverIntent = new Intent(this, BottleSetupActivity.class);
			break;
		case R.id.options_recipe_setup_button:
			serverIntent = new Intent(this, RecipeSetupActivity.class);
			break;
		case R.id.options_stop:
			CommandRoute.runCommand("command_clear");
			CommandRoute.runCommand("command_move_to_start");
			break;
		case R.id.options_advanced_button:
			serverIntent  = new Intent(this, DebugActivity.class);
			break;
		case R.id.options_turn_off_button:
			mq = barobot.main_queue;
			barobot.readHardwareRobotId(mq);
			barobot.doHoming(mq, true);
			break;
		case R.id.options_about_button:
	//		startActivity(new Intent(this, SettingsActivity.class));
			this.checkVersion(true);
			break;
		case R.id.options_lights_button:
			final Audio a = getAudio();
	    	if(a.isRunning()){
	    		System.out.println("getAudio stop1");
	        	a.stop();
	        } else {
	        	System.out.println("getAudio start");
	        	a.start(barobot);

	        	AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
	            builder1.setMessage("Light show started");
	            builder1.setCancelable(true);
	            builder1.setPositiveButton("Close",
	                    new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int id) {
	                	a.stop();
	                    dialog.cancel();
	                    barobot.main_queue.clear();
	                    barobot.main_queue.unlock();
	                }
	            });
	            AlertDialog alert11 = builder1.create();
	            alert11.show();
	        }
			break;
		case R.id.options_calibrate_button:
			CommandRoute.runCommand("command_find_bottles");
			break;

		case R.id.option345:
			String ip = Android.getLocalIpAddress();
			String msg = "http://" + ip + ":8000";
			if(ip.equals("")){
				msg = "no connection";
			}
			new AlertDialog.Builder(this)
		    .setTitle("Barobot address")
		    .setMessage(msg)
		    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) { 
		        }
		     }) .setIcon(android.R.drawable.ic_dialog_alert).show();
			break;
		case R.id.settings_unlock:
			CommandRoute.runCommand("command_unlock");
			break;

		case R.id.options_demo_button:
			CommandRoute.runCommand("command_demo");
			break;
		}
		if(serverIntent!=null){
    		serverIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
    		startActivity(serverIntent);
    	}
	}

	private Audio getAudio() {
		Audio a = (Audio) AppInvoker.container.get("Audio");
    	if(a == null ){
    		a = new Audio();
    		AppInvoker.container.put("Audio", a );
    	}
    	return a;
	}
}
