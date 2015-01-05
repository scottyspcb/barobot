package com.barobot.wizard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import com.barobot.AppInvoker;
import com.barobot.R;
import com.barobot.activity.RecipeListActivity;
import com.barobot.activity.ValidatorActivity;
import com.barobot.common.Initiator;
import com.barobot.common.interfaces.HardwareState;
import com.barobot.common.interfaces.StateListener;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.parser.utils.Interval;
import com.barobot.sofa.route.CommandRoute;

public abstract class BlankWizardActivity extends Activity {

	protected boolean back_to_wizard = false;
	protected int step = 0;
	protected Interval ii1;
	protected StateListener sl;
	protected BarobotConnector barobot;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setFullScreen();
		barobot						= Arduino.getInstance().barobot;
		super.onCreate(savedInstanceState);
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			back_to_wizard	= (extras.getInt("BACK_TO_WIZARD", 0) > 0) ;
			step			= extras.getInt("STEP", 0);
		}
		sl = new StateListener(){
			@Override
			public void onUpdate( final HardwareState state, final String name, final String value ) {
				BlankWizardActivity.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						updateState( state, name, value);
					}
				});	
			}
		};
		barobot.state.registerListener( sl );
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(back_to_wizard){
			if (keyCode == KeyEvent.KEYCODE_BACK) {
				close();
			}	
		}
		return super.onKeyDown(keyCode, event);
	}

	protected void close() {
		this.finish();
		if(this.back_to_wizard){
			Intent serverIntent = new Intent(this, ValidatorActivity.class);
			serverIntent.putExtra(RecipeListActivity.MODE_NAME, RecipeListActivity.Mode.Normal.ordinal());
			serverIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			serverIntent.putExtra("BACK_TO_WIZARD", (back_to_wizard ? 1 : 0) );
	        int requestCode = 0;
	        startActivityForResult(serverIntent,requestCode); 
		}else{	
		}
	}

	protected void goNext() {
		this.finish();
		int nextStep = step + 1;
		Class<?> cc = ValidatorActivity.getActivityClass(nextStep);
		Intent serverIntent  = new Intent(this, cc);
		serverIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		serverIntent.putExtra("BACK_TO_WIZARD", (back_to_wizard ? 1 : 0) );
		serverIntent.putExtra("STEP", nextStep );
		startActivity(serverIntent);
	}
	private void goPrev() {
		this.finish();
		int nextStep = step - 1;
		Class<?> cc = ValidatorActivity.getActivityClass(nextStep);
		Intent serverIntent  = new Intent(this, cc);
		serverIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		serverIntent.putExtra("BACK_TO_WIZARD", (back_to_wizard ? 1 : 0) );
		serverIntent.putExtra("STEP", nextStep );
		startActivity(serverIntent);
	}

	public void onOptionsButtonClicked(View view) {
		switch(view.getId()){
			case R.id.wizard_sensor_back:
			case R.id.wizard_power_back:
			case R.id.wizard_firmware_close:
			case R.id.wizard_servoy_close:
			case R.id.wizard_servoz_close:
			case R.id.wizard_weight_close:
			case R.id.wizard_servos_close:
			case R.id.wizard_calibration_close:
			case R.id.wizard_hallx_close:	
			case R.id.wizard_hally_close:
			case R.id.wizard_led_close:
				this.close();
				break;

			case R.id.wizard_servoy_prev:
			case R.id.wizard_servoz_prev:		
			case R.id.wizard_weight_prev:
			case R.id.wizard_servos_prev:
			case R.id.wizard_sensor_prev:
			case R.id.wizard_firmware_prev:
			case R.id.wizard_calibration_prev:
			case R.id.wizard_hallx_prev:
			case R.id.wizard_hally_prev:
			case R.id.wizard_led_prev:
				goPrev();
				break;	

			case R.id.wizard_hallx_unlock:
			case R.id.wizard_sensor_unlock:
				CommandRoute.runCommand("command_unlock");
				break;

			case R.id.wizard_firmware_next:
			case R.id.wizard_servoy_next:
			case R.id.wizard_servoz_next:
			case R.id.wizard_weight_next:
			case R.id.wizard_servos_next:
			case R.id.wizard_calibration_next:
			case R.id.wizard_hallx_next:
			case R.id.wizard_hally_next:
			case R.id.wizard_led_next:
				goNext();
				break;
		}	
	}

	public void onTick(){
		Initiator.logger.e("BlankWizardActivity.onTick", "onTick");
	}
	protected void updateState(HardwareState state, String name,String value) {

	}
	
	protected void enableTimer( long zaile, long coile ) {
		ii1 = new Interval(new Runnable(){
			public void run() {
				onTick();
			}
		});
		AppInvoker.getInstance().inters.add(ii1);
		ii1.run( zaile, coile );
	}

	protected void onDestroy() {
		super.onDestroy();
		if( ii1 != null && ii1.isRunning()){
			ii1.cancel();
		//	AppInvoker.getInstance().inters.remove(ii1);
		}
		if( sl != null ){
			barobot.state.unregisterListener( sl );
		}
	}
	@SuppressLint("InlinedApi")
	public void setFullScreen() {
	//	int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		getWindow().getDecorView().setSystemUiVisibility(
		  //        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
		  //      | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
		        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
		        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
		        | View.SYSTEM_UI_FLAG_FULLSCREEN
		        | View.SYSTEM_UI_FLAG_IMMERSIVE
		   );			
	}	
}