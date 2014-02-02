package com.barobot.activity;

import com.barobot.R;
import com.barobot.R.layout;
import com.barobot.R.menu;
import com.barobot.utils.update_drinks;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.TextView;

public class UpdateActivity extends Activity {
	public static final int INTENT_NAME = 5;
	private update_drinks ddb;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_update);

		ddb = new update_drinks();
		ddb.setActivity(this);
		ddb.load();
	}
    @Override
    public void onDestroy() {
        super.onDestroy();
        ddb.stop();
    }
	public void setText(final int target, final String result) {
		if(result!=null){
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					TextView bt = (TextView) findViewById(target);	    	
					bt.setText(result);
				}
			});
		}
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.update, menu);
		return true;
	}
}
