package com.barobot.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.WindowManager;

import com.barobot.AppInvoker;
import com.barobot.BarobotMain;
import com.barobot.R;
import com.barobot.other.Android;

public class BarobotActivity extends BarobotMain {
	private WakeLock wakeLock;
	private static final String WAKE_TAG = "com.blundell.tut.ui.phone.ScreenOnWakeLockActivity.WAKE_LOCK_TAG";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (getIntent().hasExtra("bundle") && savedInstanceState == null) {
			savedInstanceState = getIntent().getExtras().getBundle("bundle");
		}
		super.onCreate(savedInstanceState);
		Android.createShortcutOnDesktop(this);

		PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
	    wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, WAKE_TAG );

		BroadcastReceiver receiver = new BroadcastReceiver() {
	        public void onReceive(Context context, Intent intent) {
	            int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
	            if (plugged == BatteryManager.BATTERY_PLUGGED_AC) {
	                // on AC power
	            	
	            	
	            	
	            	
	            } else if (plugged == BatteryManager.BATTERY_PLUGGED_USB) {
	                // on USB power
	            } else if (plugged == 0) {
	                // on battery power
	            } else {
	                // intent didnt include extra info
	            }
	        }
	    };
	    IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
	    registerReceiver(receiver, filter);
        Intent serverIntent = new Intent(this, StartupActivity.class);
		serverIntent.putExtra(RecipeListActivity.MODE_NAME, RecipeListActivity.Mode.Normal.ordinal());
		serverIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        int requestCode = 0;
        startActivityForResult(serverIntent,requestCode); 
	}    
	@Override
	protected void onResume() {			// resume this activity
		super.onResume();
		wakeLock.acquire();
	}

    @Override
    protected void onPause() {
        super.onPause();
        wakeLock.release();
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
