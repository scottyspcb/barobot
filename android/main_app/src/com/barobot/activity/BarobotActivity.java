package com.barobot.activity;

import android.content.Intent;
import android.os.Bundle;

import com.barobot.AppInvoker;
import com.barobot.BarobotMain;

public class BarobotActivity extends BarobotMain {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (getIntent().hasExtra("bundle") && savedInstanceState == null) {
			savedInstanceState = getIntent().getExtras().getBundle("bundle");
		}
		super.onCreate(savedInstanceState);

        Intent serverIntent = new Intent(this, WizardStartActivity.class);
		serverIntent.putExtra(RecipeListActivity.MODE_NAME, RecipeListActivity.Mode.Normal.ordinal());
		serverIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

        int requestCode = 0;
        startActivityForResult(serverIntent,requestCode);

	}
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
	}
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
	@Override
	protected void onDestroy() {
		AppInvoker.getInstance().onDestroy();
		super.onDestroy();
	}
}
