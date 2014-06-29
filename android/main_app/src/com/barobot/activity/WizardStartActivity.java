package com.barobot.activity;

import java.util.Locale;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.barobot.BarobotMain;
import com.barobot.R;
import com.barobot.gui.dataobjects.Engine;
import com.barobot.gui.utils.LangTool;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;

public class WizardStartActivity extends BarobotMain{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wizard_start);
		Engine.GetInstance(this).getRecipes();
		String langCode = Locale.getDefault().getLanguage();	// i.e. "pl"
		//Log.i("readLangId1", Locale.getDefault().getDisplayLanguage());
		LangTool.setLanguage(langCode);
	}

	public void onMenuButtonClicked(View view)
	{
		Intent serverIntent = null;
		switch(view.getId())
		{
			case R.id.wizard_list:
				serverIntent = new Intent(this, RecipeListActivity.class);
				serverIntent.putExtra(RecipeListActivity.MODE_NAME, RecipeListActivity.Mode.Normal.ordinal());
				break;
			case R.id.wizard_own:
				serverIntent = new Intent(this, CreatorActivity.class);
				break;	
			case R.id.button_settings:
				serverIntent = new Intent(this, OptionsActivity.class);
				break;
		}
		if(serverIntent!=null){
    		serverIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
    		overridePendingTransition(R.anim.push_left_in,R.anim.push_left_out);
    		startActivityForResult(serverIntent,0);
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
