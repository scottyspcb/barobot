package com.barobot.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.barobot.BarobotMain;
import com.barobot.R;
import com.barobot.gui.dataobjects.Engine;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;

public class StartupActivity extends BarobotMain{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if( BarobotMain.canStart ){
	        requestWindowFeature(Window.FEATURE_NO_TITLE);
	        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
			setContentView(R.layout.activity_startup);
			Engine.GetInstance().getRecipes();
			setFullScreen();
		 }
	}

	@Override
	protected void onResume() {
		super.onResume();
		if( BarobotMain.canStart ){
			setFullScreen();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	public void onMenuButtonClicked(View view)
	{
		Intent serverIntent = null;
		int animIn = R.anim.push_left_in;
		int animOut = R.anim.push_left_out;
		BarobotConnector barobot = Arduino.getInstance().barobot;
		switch(view.getId())
		{
			case R.id.wizard_list:
				serverIntent = new Intent(this, RecipeListActivity.class);
				serverIntent.putExtra(RecipeListActivity.MODE_NAME, RecipeListActivity.Mode.Normal.ordinal());
				animIn = R.anim.push_left_in;
				animOut = R.anim.push_left_out;
				if(barobot.lightManager.demoStarted){ 
					barobot.lightManager.stopDemo(barobot.main_queue);
				}
				break;
			case R.id.wizard_own:
				serverIntent = new Intent(this, CreatorActivity.class);
				animIn = R.anim.push_right_in;
				animOut = R.anim.push_right_out;
				if(barobot.lightManager.demoStarted){
					barobot.lightManager.stopDemo(barobot.main_queue);
				}
				break;	
			case R.id.button_settings:
				serverIntent = new Intent(this, OptionsActivity.class);
				animIn = R.anim.push_up_in;
				animOut = R.anim.push_up_out;
				break;
		}
		if(serverIntent!=null){
    		serverIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
    		startActivity(serverIntent);
    		overridePendingTransition(animIn,animOut);
    	}
	}
	public void onLangButtonClicked(View view)
	{
		String langCode = "";
		switch(view.getId())
		{
			case R.id.lang_ru:
				langCode = "ru";
				break;
			case R.id.lang_pl:
				langCode = "pl";
				break;	
			case R.id.lang_en:
				langCode = "en";
				break;
			default:
				langCode = "en";
				break;
		}
		BarobotMain.getInstance().changeLanguage(langCode);
		setContentView(R.layout.activity_startup);	//reload
	//	Log.i("translateName2 ", LangTool.translateName(2, "type", "aa" ));
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_HOME) {
			Log.i("onKeyDown", "KEYCODE_HOME");
			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Log.i("onKeyDown", "KEYCODE_BACK");
			BarobotConnector barobot = Arduino.getInstance().barobot;
			barobot.main_queue.unlock();
			return true;
		}
		if (event.getKeyCode() == KeyEvent.KEYCODE_POWER) {
			Log.i("onKeyDown", "KEYCODE_POWER");
	        return true;
	    }
		if (keyCode == KeyEvent.KEYCODE_POWER) {
			Log.i("onKeyDown", "KEYCODE_POWER");
	        return true;
	    }		
		return super.onKeyDown(keyCode, event);
	}
}
//        getWindow().addFlags(WindowManager.LayoutParams.PREVENT_POWER_KEY);