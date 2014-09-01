package com.barobot.activity;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.barobot.BarobotMain;
import com.barobot.R;
import com.barobot.gui.dataobjects.Engine;
import com.barobot.gui.utils.LangTool;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;

public class WizardStartActivity extends BarobotMain{

	private static long TIMER_DELAY = 0;
	private static long TIMER_REPEAT = 1000;
	private Handler handler;
	private Timer timer;
	
	private class MessageTask extends TimerTask
	{
		private Handler mHandler;
		public MessageTask(Handler handler)
		{
			mHandler = handler;
		}

		@Override
		public void run() {
			
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					CheckMessages();
				}
			});
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(R.layout.activity_wizard_start);
		Engine.GetInstance().getRecipes();
		String langCode = Locale.getDefault().getLanguage();	// i.e. "pl"
		//Log.i("readLangId1", Locale.getDefault().getDisplayLanguage());
		LangTool.setLanguage(langCode);
		setFullScreen();
		handler = new Handler();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		setFullScreen();
		MessageTask task = new MessageTask(handler);
		
		timer = new Timer(true);
		timer.schedule(task, TIMER_DELAY, TIMER_REPEAT);
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		
		timer.cancel();
	}
	
	private void CheckMessages()
	{
		String message = Engine.GetInstance().GetMessage();
		
		if (message != "")
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Message")
				.setMessage(message);
			
			builder.create().show();
		}
	}

	public void onMenuButtonClicked(View view)
	{
		Intent serverIntent = null;
		int animIn = R.anim.push_left_in;
		int animOut = R.anim.push_left_out;
		
		switch(view.getId())
		{
			case R.id.wizard_list:
				serverIntent = new Intent(this, RecipeListActivity.class);
				serverIntent.putExtra(RecipeListActivity.MODE_NAME, RecipeListActivity.Mode.Normal.ordinal());
				animIn = R.anim.push_left_in;
				animOut = R.anim.push_left_out;
				break;
			case R.id.wizard_own:
				serverIntent = new Intent(this, CreatorActivity.class);
				animIn = R.anim.push_right_in;
				animOut = R.anim.push_right_out;
				break;	
			case R.id.button_settings:
				serverIntent = new Intent(this, OptionsActivity.class);
				animIn = R.anim.push_down_in;
				animOut = R.anim.push_down_out;
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
		}
		Resources res = getBaseContext().getResources();
		DisplayMetrics dm = res.getDisplayMetrics();

		android.content.res.Configuration conf = res.getConfiguration();

		conf.locale = new Locale(langCode);
		res.updateConfiguration(conf, dm);
		LangTool.setLanguage(langCode);
		
		Log.i("lang changed to", langCode);
		setContentView(R.layout.activity_wizard_start);	//reload

		Log.i("translateName2 ", LangTool.translateName(2, "type", "aa" ));
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
		return super.onKeyDown(keyCode, event);
	}

 	@Override
	public void onBackPressed() {
		new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle("Koniec?")
				.setMessage(
						"Czy na pewno zamknąć aplikację i przerwać pracę robota?")
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								finish();
							}
						}).setNegativeButton("No", null).show();
	}
}
