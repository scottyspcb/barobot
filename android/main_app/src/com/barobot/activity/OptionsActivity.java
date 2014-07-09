package com.barobot.activity;

import com.barobot.AppInvoker;
import com.barobot.BarobotMain;
import com.barobot.R;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.other.Audio;
import com.barobot.parser.Queue;

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

	@Override
	protected void onResume() {
		super.onResume();
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
			mq = barobot.main_queue;
			mq.clear();
			barobot.moveToStart();
			break;
		case R.id.options_advanced_button:
			serverIntent  = new Intent(this, DebugActivity.class);
			break;
		case R.id.options_turn_off_button:
			mq = barobot.main_queue;
			barobot.doHoming(mq, true);
			break;
		case R.id.options_about_button:
			startActivity(new Intent(this, SettingsActivity.class));
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
			barobot.kalibrcja();
			break;
		case R.id.settings_unlock:
			barobot.main_queue.unlock();
			break;

		case R.id.options_demo_button:
			barobot.startDemo();
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
