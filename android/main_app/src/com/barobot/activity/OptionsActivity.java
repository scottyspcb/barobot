package com.barobot.activity;

import com.barobot.R;
import com.barobot.gui.fragment.MenuFragment;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.parser.Queue;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;

public class OptionsActivity extends BarobotActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_options);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		MenuFragment menuFrag = (MenuFragment) getFragmentManager().findFragmentById(R.id.fragment_menu);
		menuFrag.SetBreadcrumb(MenuFragment.MenuItem.Options);
	}
	
	public void onOptionsButtonClicked(View view)
	{
		Intent serverIntent = null;
		BarobotConnector barobot = Arduino.getInstance().barobot;
		Queue mq;
		switch(view.getId())
		{
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
}
