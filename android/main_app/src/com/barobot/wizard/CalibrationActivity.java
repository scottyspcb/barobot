package com.barobot.wizard;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.barobot.R;
import com.barobot.activity.ValidatorActivity;
import com.barobot.sofa.route.CommandRoute;

public class CalibrationActivity extends BlankWizardActivity {

	Button wizard_calibration_start;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wizard_calibration);
		wizard_calibration_start = (Button) findViewById(R.id.wizard_calibration_start);
		wizard_calibration_start.setEnabled(true);
		//enableTimer( 1000, 2000 );
	}

	public void onOptionsButtonClicked(View view)
	{
		switch(view.getId()){
			case R.id.wizard_calibration_start:
				CommandRoute.runCommand("command_find_bottles");
				break;
			case R.id.wizard_calibration_next:	
				wizard_calibration_start.setEnabled(false);
				onWizardReady();
				break;
			default:
				super.onOptionsButtonClicked(view);
				break;
		}
	}

	protected void onWizardReady() {	
		if(back_to_wizard){
		}
		finish();
		ValidatorActivity.saveSettings( this );
	}
	public void onTick(){
		
	}
}
