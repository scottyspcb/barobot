package com.barobot;

import com.barobot.drinks.drinks_database;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class UpdateActivity extends Activity {

	public static final int INTENT_NAME = 5;
	private drinks_database ddb;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_update);

		ddb = new drinks_database();
		ddb.setActivity(this);
		ddb.load();
	}
    @Override
    public void onDestroy() {
        super.onDestroy();
        ddb.stop();
    }
   
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.update, menu);
		return true;
	}

}
