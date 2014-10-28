package com.barobot.activity;

import com.barobot.BarobotMain;
import com.barobot.Power;
import com.barobot.R;
import com.barobot.R.layout;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.other.Android;
import com.barobot.other.Audio;
import com.barobot.parser.Queue;
import com.barobot.sofa.route.CommandRoute;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class ValidatorActivity extends BarobotMain {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_validator);
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
			case R.id.wizard0:			// start wizard
				serverIntent  = new Intent(this, Power.class);
				break;
			case R.id.wizard1:
				serverIntent  = new Intent(this, Power.class);
				break;
			case R.id.wizard2:
				break;		
			case R.id.wizard3:
				break;		
			case R.id.wizard4:
				break;		
			case R.id.wizard5:
				break;		
			case R.id.wizard6:
				break;		
			case R.id.wizard7:
				break;	
			case R.id.wizard8:
				break;
			case R.id.wizard9:
				break;	
			case R.id.wizard10:
				break;				
			case R.id.wizard11:
				break;	
			case R.id.wizard12:
				break;	
			case R.id.wizard13:
				break;
			case R.id.wizard14:
				break;
			case R.id.wizard15:
				break;		

			case R.id.options_demo_button:
				serverIntent  = new Intent(this, DebugActivity.class);
				mq = barobot.main_queue;
				barobot.readHardwareRobotId(mq);
				barobot.doHoming(mq, true);

				CommandRoute.runCommand("command_find_bottles");
				CommandRoute.runCommand("command_demo");
				break;
		}

		if(serverIntent!=null){
    		serverIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
    		startActivity(serverIntent);
    	}
	}
}

