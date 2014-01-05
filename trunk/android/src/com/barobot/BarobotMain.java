package com.barobot;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;


import com.barobot.activity.AboutActivity;
import com.barobot.activity.BTListActivity;
import com.barobot.activity.DebugActivity;
import com.barobot.activity.MainSettingsActivity;
import com.barobot.activity.UpdateActivity;
import com.barobot.gui.BarobotActivity;
import com.barobot.hardware.DeviceSet;
import com.barobot.hardware.virtualComponents;
import com.barobot.utils.CameraManager;
import com.barobot.utils.Constant;
import com.barobot.utils.interval;
import com.barobot.utils.Arduino;
import com.barobot.web.server.SofaServer;
import com.x5.template.providers.AndroidTemplates;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class BarobotMain extends BarobotActivity {
    // Layout Viewsd
	private static BarobotMain instance;
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
		instance = this;	        // Set up the window layout
		AppInvoker.createInstance( this ).onCreate();

		if (getIntent().hasExtra("bundle") && savedInstanceState==null){
		   savedInstanceState = getIntent().getExtras().getBundle("bundle");
		}
//		requestWindowFeature(Window.FEATURE_NO_TITLE);
//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

	    int mUIFlag = View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
	    getWindow().getDecorView().setSystemUiVisibility(mUIFlag);       

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        virtualComponents.init( this );
    }
    @Override
    public void onStart() {
        super.onStart();
    }
	@Override
	protected void onPause() {
		AppInvoker.getInstance().onPause();
		super.onPause();
	}
    @Override
    public synchronized void onResume() {
        super.onResume();
        AppInvoker.getInstance().onResume();
    }
    @Override
    public void onDestroy() {
    	AppInvoker.getInstance().onDestroy();
        super.onDestroy();
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Constant.log(Constant.TAG, "onActivityResult " + resultCode);
        switch (requestCode){
        case UpdateActivity.INTENT_NAME:
        	Constant.log(Constant.TAG, "END OF UpdateActivity");
            break;
        case AboutActivity.INTENT_NAME:
        	Constant.log(Constant.TAG, "END OF AboutActivity");
            break;
        case MainSettingsActivity.INTENT_NAME:
        	Constant.log(Constant.TAG, "END OF SETTINGS");
            break;
        case DebugActivity.INTENT_NAME:
        	Constant.log(Constant.TAG, "END OF DebugActivity");
            break;
        case BTListActivity.INTENT_NAME:
        	Constant.log(Constant.TAG, "REQUEST_CONNECT_DEVICE_SECURE");
            // When BTListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                String address = data.getExtras().getString(Constant.EXTRA_DEVICE_ADDRESS);           // Get the device MAC address
                Arduino.getInstance().connectId(address);
            }
            break;

        case Constant.REQUEST_ENABLE_BT:
        	Constant.log(Constant.TAG, "REQUEST_ENABLE_BT " + resultCode);
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up session
                Arduino.getInstance().setupBT( this );
            } else {
                // User did not enable Bluetooth or an error occurred
                Constant.log(Constant.TAG, "BT not enabled");
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

	//Any update to UI can not be carried out in a non UI thread like the one used
	//for Server. Hence runOnUIThread is used.
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
	public static BarobotMain getInstance(){
		return instance;
	}

	@Override
	public void onBackPressed() {
	    new AlertDialog.Builder(this)
	        .setIcon(android.R.drawable.ic_dialog_alert)
	        .setTitle("Koniec?")
	        .setMessage("Czy na pewno zamknąć aplikację przerwać pracę robota?")
	        .setPositiveButton("Yes", new DialogInterface.OnClickListener(){
	        @Override
	        public void onClick(DialogInterface dialog, int which) {
	            finish();
	        }
	    })
	    .setNegativeButton("No", null).show();
	}
}

/*
new AlertDialog.Builder(this)
.setTitle("Delete entry")
.setMessage("Are you sure you want to delete this entry?")
.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
    public void onClick(DialogInterface dialog, int which) { 
        // continue with delete
    }
 })
.setNegativeButton("No", new DialogInterface.OnClickListener() {
    public void onClick(DialogInterface dialog, int which) { 
        // do nothing
    }
 })
 .show();
*/