package com.barobot;


import java.util.ArrayList;
import java.util.Iterator;

import com.barobot.hardware.virtualComponents;
import com.barobot.utils.CameraManager;
import com.barobot.utils.Constant;
import com.barobot.utils.interval;
import com.barobot.utils.Arduino;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class BarobotMain extends Activity {
    // Layout Views
	private static BarobotMain instance;
	public CameraManager cm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		instance = this;	        // Set up the window layout
        setContentView(R.layout.main);
        virtualComponents.init( this );

        // Initialize the compose field with a listener for the return key
        /*
        mOutEditText = (EditText) findViewById(R.id.edit_text_out);
        mOutEditText.setOnEditorActionListener( new TextView.OnEditorActionListener() {
	        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
	            // If the action is a key-up event on the return key, send the message
		            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
		            	Constant.log(Constant.TAG, "END onEditorAction+++");
		                String message = view.getText().toString();
		                Arduino.getInstance().send(message);
		            }
		            Constant.log(Constant.TAG, "END onEditorAction");
	            return true;
	        }
	    });
	    */
        boolean autoadb = true;
        if(autoadb){
			boolean res = Arduino.getInstance().connectADB();
			if(res == false ){
				Constant.log(Constant.TAG, "Unable to start TCP server" );
				System.exit(-1);
			}
        }       
		this.runTimer();

		cm = new CameraManager( this );
		cm.findCameras();
    }

    private ArrayList<interval> inters = new ArrayList<interval>();    
    private void runTimer() {
//    	interval inn = new interval();
//   	inn.run(1000,5000);
//    	this.inters.add(inn);
    	interval inn = new interval(new Runnable() {
    		private int count = 0;
		    public void run() {
		    	Arduino ar = Arduino.getInstance();
		        if( ar.allowAutoconnect()){
		        	count++;
		        	if(count > 2){		// po 10 sek
		//        		Constant.log("RUNNABLE", "3 try autoconnect" );
		        		ar.autoconnect();
		        	}
			    }else{
			    	count = 0;
		        }
		   }
		});
    	inn.run(1000,5000);
    	inn.pause();
    	this.inters.add(inn);
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.option_menu, menu);
	    return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent serverIntent = null;
        switch (item.getItemId()) {
        case R.id.secure_connect_scan:
             if( Arduino.getInstance().checkBT() == false ){
                Toast.makeText(this, "Bluetooth jest niedostępny", Toast.LENGTH_LONG).show();
                finish();
            }
            // Launch the BTListActivity to see devices and do scan
            serverIntent = new Intent(this, BTListActivity.class);
            startActivityForResult(serverIntent, BTListActivity.INTENT_NAME);
            return true;

        case R.id.update_drinks:
            serverIntent = new Intent(this, UpdateActivity.class);
            startActivityForResult(serverIntent, UpdateActivity.INTENT_NAME );
	        return true;

        case R.id.about_item:
            serverIntent = new Intent(this, AboutActivity.class);
            startActivityForResult(serverIntent, AboutActivity.INTENT_NAME );
	        return true;   

	    case R.id.debug_mode_window:
            serverIntent = new Intent(this, DebugWindow.class);
            startActivityForResult(serverIntent, DebugWindow.INTENT_NAME);
	        return true;

       case R.id.menu_settings:
    	   serverIntent = new Intent(this, MainSettingsActivity.class);
            startActivityForResult(serverIntent, MainSettingsActivity.INTENT_NAME);
            break;
	    }        

        return false;
    }
    @Override
    public void onStart() {
        super.onStart();
        if( Arduino.getInstance().checkBT() != false ){
	        int res = Arduino.startBt();
	        if( res == 34){		// jesli jest wlaczony
	        	 Arduino.getInstance().setupBT( this);
	        }else if( res == 12){	// jesli wymaga wlaczenia to wroci do onActivityResult
	            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	            startActivityForResult(enableIntent, Constant.REQUEST_ENABLE_BT);
	        }
        }
    }

	@Override
	protected void onPause() {
		Constant.log("MAINWINDOW", "onPause");
		cm.onPause();
		super.onPause();
	}
    @Override
    public synchronized void onResume() {
    	Constant.log("MAINWINDOW", "onResume");
        super.onResume();
		if(cm!=null){
			cm.onResume();
		}
        Arduino.getInstance().resume();
    }
    
    @Override
    public void onDestroy() {
    	Constant.log("MAINWINDOW", "onDestroy");
    	Iterator<interval> it = this.inters.iterator();
    	while(it.hasNext()){
    		it.next().cancel();
    	}
        cm.onDestroy();
        Arduino.getInstance().destroy();
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
        case DebugWindow.INTENT_NAME:
        	Constant.log(Constant.TAG, "END OF BEBUG_WINDOW");
            break;
        case MainSettingsActivity.INTENT_NAME:
        	Constant.log(Constant.TAG, "END OF SETTINGS");
            break;
        case BTListActivity.INTENT_NAME:
        	Constant.log(Constant.TAG, "REQUEST_CONNECT_DEVICE_SECURE");
            // When BTListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                String address = data.getExtras().getString(Constant.EXTRA_DEVICE_ADDRESS);           // Get the device MAC address
                Arduino.connectBTDeviceId(address);
            }
            break;

        case Constant.REQUEST_ENABLE_BT:
        	Constant.log(Constant.TAG, "REQUEST_ENABLE_BT " + resultCode);
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up session
                Arduino.getInstance().setupBT( this);
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