package com.barobot.activity;

import com.barobot.R;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.app.Activity;
public class AboutActivity extends Activity {

	public static final int INTENT_NAME = 6;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	   	 if (getIntent().hasExtra("bundle") && savedInstanceState==null){
	   	        savedInstanceState = getIntent().getExtras().getBundle("bundle");
	   	 }
	   	setTheme(android.R.style.Theme_Holo_Light);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
	    int mUIFlag = View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
	    getWindow().getDecorView().setSystemUiVisibility(mUIFlag);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
	}
/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.about, menu);
		return true;
	}
*/
}
