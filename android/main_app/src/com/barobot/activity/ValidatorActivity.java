package com.barobot.activity;

import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.barobot.BarobotMain;
import com.barobot.R;
import com.barobot.common.constant.Constant;
import com.barobot.gui.utils.LangTool;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.wizard.CalibrationActivity;
import com.barobot.wizard.FirmwareActivity;
import com.barobot.wizard.HallXActivity;
import com.barobot.wizard.HallYActivity;
import com.barobot.wizard.LedActivity;
import com.barobot.wizard.PowerActivity;
import com.barobot.wizard.SensorsActivity;
import com.barobot.wizard.ServoYActivity;
import com.barobot.wizard.ServoZActivity;
import com.barobot.wizard.ServosActivity;
import com.barobot.wizard.WeightSensorActivity;

public class ValidatorActivity extends BarobotMain {
	boolean disable_back = false;

	public static Class<?>[] list = {
		PowerActivity.class,
		FirmwareActivity.class,
		SensorsActivity.class,
		LedActivity.class,
		ServoZActivity.class,
		ServoYActivity.class,
		HallYActivity.class,
		HallXActivity.class,
		ServosActivity.class,
		WeightSensorActivity.class,
		CalibrationActivity.class,
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle extras = getIntent().getExtras();

		BarobotConnector barobot = Arduino.getInstance().barobot;
		barobot.state.set("ROBOT_CAN_MOVE", 0 );		// disable homeing

		if (extras != null) {
		    disable_back = (extras.getInt("BACK_TO_WIZARD", 0) > 0) ;		// disable_back in TRUE at first start
		}
		Log.e("ValidatorActivity.no disable_back", "");

		if( BarobotMain.canStart ){
	        requestWindowFeature(Window.FEATURE_NO_TITLE);
	        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
	//        getWindow().addFlags(WindowManager.LayoutParams.PREVENT_POWER_KEY);
	        setContentView(R.layout.activity_validator);
			String langCode = Locale.getDefault().getLanguage();	// i.e. "pl"
			LangTool.setLanguage(langCode);
			setFullScreen();
		 }
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(!disable_back){				// to it in saveSettings()
			BarobotConnector barobot = Arduino.getInstance().barobot;
			barobot.state.set("ROBOT_CAN_MOVE", Constant.WIZARD_VERSION );
		}
	}

	public void onOptionsButtonClicked(View view)	{
		switch(view.getId()){
			case R.id.wizard_close:		// start wizard
				saveSettings(this);
				break;
			case R.id.wizard0:			// start wizard
				gotoActivity(0);
				break;
			case R.id.wizard1:
				gotoActivity(0);
				break;
			case R.id.wizard2:
				gotoActivity(1);
				break;		
			case R.id.wizard3:
				gotoActivity(2);
				break;		
			case R.id.wizard4:
				gotoActivity(3);
				break;	
			case R.id.wizard5:
				gotoActivity(4);
				break;		
			case R.id.wizard6:
				gotoActivity(5);
				break;
			case R.id.wizard7:
				gotoActivity(6);
				break;
			case R.id.wizard8:
				gotoActivity(7);
				break;
			case R.id.wizard9:
				gotoActivity(8);
				break;
			case R.id.wizard10:
				gotoActivity(9);
				break;	
			case R.id.wizard11:
				gotoActivity(10);
				break;

				/*	case R.id.wizard12:
				gotoActivity(11);
				break;	
			case R.id.wizard13:
				gotoActivity(12);
				break;
			case R.id.wizard14:
				gotoActivity(13);
				break;
			case R.id.wizard15:
				gotoActivity(14);
				break;		*/
		}
	}

	public void gotoActivity(int num)	{
		Class<?> c				= getActivityClass(num);
		Intent serverIntent 	= new Intent(this, c);
		serverIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		serverIntent.putExtra("BACK_TO_WIZARD", (disable_back ? 1 : 0) );
		serverIntent.putExtra("STEP", num );
		startActivity(serverIntent);
	}

	public static Class<?> getActivityClass(int num) {
	//	Initiator.logger.i("ValidatorActivity.getActivityClass", " step: "+ num);
		return list[num];
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(disable_back){
			if (keyCode == KeyEvent.KEYCODE_BACK) {
				
				Intent serverIntent = new Intent(this, StartupActivity.class);
				serverIntent.putExtra(RecipeListActivity.MODE_NAME, RecipeListActivity.Mode.Normal.ordinal());
				serverIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		        int requestCode = 0;
		        this.startActivityForResult(serverIntent,requestCode);

		//		return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	public void onLangButtonClicked(View view)
	{
		String langCode = "";
		switch(view.getId())
		{
			case R.id.wizard_lang_ru:
				langCode = "ru";
				break;
			case R.id.wizard_lang_pl:
				langCode = "pl";
				break;	
			case R.id.wizard_lang_en:
				langCode = "en";
				break;
			default:
				langCode = "en";
				break;
		}
		BarobotMain.getInstance().changeLanguage(langCode);
        setContentView(R.layout.activity_validator);
		Log.i("translateName2 ", LangTool.translateName(2, "type", "aa" ));
	}

	public static void saveSettings( Activity act ){
		BarobotConnector barobot = Arduino.getInstance().barobot;
		barobot.state.set("ROBOT_CAN_MOVE", Constant.WIZARD_VERSION );

		Intent serverIntent = new Intent(act, StartupActivity.class);
		serverIntent.putExtra(RecipeListActivity.MODE_NAME, RecipeListActivity.Mode.Normal.ordinal());
		serverIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        int requestCode = 0;
        act.startActivityForResult(serverIntent,requestCode); 
	}
}
