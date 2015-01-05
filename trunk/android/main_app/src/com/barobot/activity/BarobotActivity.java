package com.barobot.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.widget.TextView;

import com.barobot.AppInvoker;
import com.barobot.BarobotMain;
import com.barobot.R;
import com.barobot.android.Android;
import com.barobot.common.Initiator;
import com.barobot.common.constant.Constant;
import com.barobot.gui.dataobjects.Engine;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;

public class BarobotActivity extends BarobotMain {
	private WakeLock wakeLock;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (getIntent().hasExtra("bundle") && savedInstanceState == null) {
			savedInstanceState = getIntent().getExtras().getBundle("bundle");
		}
		super.onCreate(savedInstanceState);
		this.setFullScreen();
		setContentView(R.layout.activity_router);

		TextView router_barobot_version			= (TextView) findViewById(R.id.router_barobot_version);
		router_barobot_version.setText(""+Constant.ANDROID_APP_VERSION);
		
		
	//	PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
	//    wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, WAKE_TAG );

		PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
	    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "no sleep");

		BroadcastReceiver receiver = new BroadcastReceiver() {
	        public void onReceive(Context context, Intent intent) {
	        //    int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
	            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
	            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;
	        //    Initiator.logger.i( "isCharging", ""+isCharging);
	            setPowerState( isCharging );
	        }
	    };
	    setPowerState(Android.isDcConnected(this));
	    IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
	    registerReceiver(receiver, filter);

	    BroadcastReceiver receiver2 = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                	Initiator.logger.i( "POWER", "ACTION_SCREEN_OFF");
                } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                	Initiator.logger.i( "POWER", "ACTION_SCREEN_ON");
                }
            }
        };
	    IntentFilter filter2 = new IntentFilter(Intent.ACTION_SCREEN_ON);
	    filter2.addAction(Intent.ACTION_SCREEN_OFF);
	    registerReceiver(receiver2, filter2);
	    startLoading();   
	}

	public void onStartupReady(){
	    /*
	    Intent serverIntent = new Intent(this, StartupActivity.class);
		serverIntent.putExtra(RecipeListActivity.MODE_NAME, RecipeListActivity.Mode.Normal.ordinal());
		serverIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        int requestCode = 0;
        startActivityForResult(serverIntent,requestCode); 
	     */

		BarobotConnector barobot = Arduino.getInstance().barobot;
		if(barobot==null || barobot.state.getInt("ROBOT_CAN_MOVE", 0) < Constant.WIZARD_VERSION ){
			Intent serverIntent = new Intent(this, ValidatorActivity.class);
			serverIntent.putExtra(RecipeListActivity.MODE_NAME, RecipeListActivity.Mode.Normal.ordinal());
			serverIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			serverIntent.putExtra("BACK_TO_WIZARD", 1 );
	        int requestCode = 0;
	        startActivityForResult(serverIntent,requestCode); 
	    }else{
	        Intent serverIntent = new Intent(this, StartupActivity.class);
			serverIntent.putExtra(RecipeListActivity.MODE_NAME, RecipeListActivity.Mode.Normal.ordinal());
			serverIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
	        int requestCode = 0;
	        startActivityForResult(serverIntent,requestCode); 
	    }
	}

	private void startLoading() {
		Thread t = new Thread( new Runnable() {
			@Override
			public void run() {
		    	loadData();
				onStartupReady();
			}
		});
		t.start();
	}

	protected void loadData() {
		Engine e = Engine.GetInstance();
		e.loadSlots();
		e.getRecipes();	
	}
	protected void setPowerState(boolean isPowered) {
    	Initiator.logger.i( "EXTRA_PLUGGED", isPowered? "dc": "battery");
		BarobotConnector barobot = Arduino.getInstance().barobot;
		if(barobot!=null ){
			barobot.state.set("DC_PLUGGED", isPowered ? 1 : 0 );
		}
	}
	@Override
	protected void onResume() {			// resume this activity
		super.onResume();
		this.setFullScreen();
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
