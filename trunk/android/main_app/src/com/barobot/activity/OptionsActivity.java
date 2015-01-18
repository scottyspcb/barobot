package com.barobot.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.barobot.AppInvoker;
import com.barobot.BarobotMain;
import com.barobot.R;
import com.barobot.android.Android;
import com.barobot.common.Initiator;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.other.Audio;
import com.barobot.parser.Queue;
import com.barobot.sofa.route.CommandRoute;

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
		this.checkVersion( false, false );
	}
	private int checkVersion( boolean alertResult, boolean force ) {
		if(!wasVersionChecked || force ){
			wasVersionChecked = true;
			return Android.checkNewSoftwareVersion( alertResult, this );
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
			CommandRoute.runCommand("command_stop_now");
			break;
		case R.id.options_advanced_button:
			serverIntent  = new Intent(this, DebugActivity.class);
			break;
		case R.id.options_turn_off_button:
			mq = barobot.main_queue;
			barobot.readHardwareRobotId(mq);
			barobot.doHoming(mq, true);
			break;

		case R.id.options_party_setup:
			serverIntent = new Intent(this, SettingsActivity.class);	
			break;
		case R.id.options_about_button:
	//		startActivity(new Intent(this, SettingsActivity.class));
			this.checkVersion(true, true);
			break;
		case R.id.options_lights_button:
			final Audio a = getAudio();
	    	if(a.isRunning()){
	    		Initiator.logger.i( this.getClass().getName(), "getAudio stop1");
	        	a.stop();
	        } else {
	        	Initiator.logger.i( this.getClass().getName(), "getAudio start");
	        	a.start(barobot);

	        	AlertDialog.Builder builder1 = new AlertDialog.Builder(BarobotMain.getInstance());
	            builder1.setMessage("Light show started");
	            builder1.setCancelable(true);
	            builder1.setPositiveButton("Close",
	                    new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int id) {
	                	a.stop();
	                    dialog.cancel();
	                    barobot.main_queue.clear();
	                }
	            });
	            AlertDialog alert11 = builder1.create();
	            alert11.show();
	        }
			break;
		case R.id.options_calibrate_button:
			serverIntent = new Intent(this, ValidatorActivity.class);
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
            this.finish();
            /*
    		AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
            builder1.setMessage("Demo started");
            builder1.setCancelable(true);
            builder1.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                    BarobotConnector barobot = Arduino.getInstance().barobot;
                    barobot.lightManager.stopDemo();
                    barobot.main_queue.clear();
                }
            });
            AlertDialog alert11 = builder1.create();
            alert11.show();*/
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
	public void gotoMainMenu(View view){		
		finish();
		overridePendingTransition(R.anim.push_down_in,R.anim.push_down_out);
	}
}
