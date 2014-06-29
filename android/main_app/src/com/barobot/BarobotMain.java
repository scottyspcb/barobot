package com.barobot;


import android.app.Activity;
import android.os.Bundle;

import android.widget.Button;
import android.widget.TextView;

public class BarobotMain extends Activity {
	private static BarobotMain instance;
	public static BarobotMain getInstance() {
		return instance;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(instance == null){
			instance = this; // Set up the window layout
		}
		AppInvoker.createInstance(this);
		AppInvoker.getInstance().onCreate();
	}

    protected void setTextViewText(String text, int id){
		TextView tView = (TextView) findViewById(id);
		tView.setText(text);
	}

    protected void ButtonEnabled(boolean enabled, int id)
	{
		Button okButton = (Button) findViewById(id);
		okButton.setEnabled(enabled);
	}
    

	@Override
	protected void onResume() {			// resume this activity
		super.onResume();
		AppInvoker.getInstance().onResume();
	}

	@Override
	protected void onStart() {			// start this activity
		super.onStart();
		AppInvoker.getInstance().onStart();
	}
}




/*
case R.id.menu_favorite:
	serverIntent = new Intent(this, BarobotMain.class);
	serverIntent.putExtra(BarobotMain.MODE_NAME, BarobotMain.Mode.Favorite.ordinal());
	serverIntent.putExtra("Test", "Test2");
	break;
	
case R.id.menu_choose:
	serverIntent = new Intent(this, RecipeListActivity.class);
	serverIntent.putExtra(RecipeListActivity.MODE_NAME, RecipeListActivity.Mode.Normal.ordinal());
	break;

case R.id.menu_lucky:
	serverIntent = new Intent(this, RecipeListActivity.class);
	serverIntent.putExtra(RecipeListActivity.MODE_NAME, RecipeListActivity.Mode.Random.ordinal());
	break;	


if(serverIntent!=null){
	serverIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
	startActivity(serverIntent);
}*/


