package com.barobot.wizard;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;


import com.barobot.R;
import com.barobot.activity.ValidatorActivity;
import com.barobot.common.interfaces.HardwareState;
import com.barobot.parser.Queue;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.message.Mainboard;
import com.barobot.sofa.route.CommandRoute;

public class CalibrationActivity extends BlankWizardActivity {

	int[] bottles = {0,0,0,0,0,0,0,0,0,0,0,0};
	int founded = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wizard_calibration);
	}

	public void onOptionsButtonClicked(View view)
	{
		switch(view.getId()){
			case R.id.wizard_calibration_start:
				clearStats();
				
				TextView wizard_calibration_result		= (TextView) findViewById(R.id.wizard_calibration_result);
				wizard_calibration_result.setTextColor(Color.WHITE);
				wizard_calibration_result.setText(R.string.wizard_calibration_waiting);
	
				
				CommandRoute.runCommand("command_find_bottles");
				barobot.main_queue.add( new AsyncMessage( true ) {
					@Override
					public String getName() {
						return "Save hall x left value";
					}
					@Override
					public Queue run(Mainboard dev, Queue queue) {
						runOnUiThread(new Runnable() {
							  public void run() {
								  checkResult();
							  }
						});
						return null;
					}
		    	});
				break;
			case R.id.wizard_calibration_next:
				onWizardReady();
				break;
			default:
				super.onOptionsButtonClicked(view);
				break;
		}
	}
	private void clearStats() {
		// TODO Auto-generated method stub
		for(int i=0;i<bottles.length;i++){
			bottles[i] = 0;
		}
		founded = 0;
	}

	protected void onWizardReady() {	
		if(back_to_wizard){
		}
		finish();
		ValidatorActivity.saveSettings( this );
	}
	public void onTick(){
	}
	protected void updateState(HardwareState state, String name, String value) {
	//	Initiator.logger.e("CalibrationActivity", "onUpdate "+name + "/" + value );
		if( "BOTTLE_X_0".equals(name)){
			bottles[0]	= 1;founded++;
		}else if( "BOTTLE_X_1".equals(name)){
			bottles[1]	= 1;founded++;
		}else if( "BOTTLE_X_2".equals(name)){
			bottles[2]	= 1;founded++;	
		}else if( "BOTTLE_X_3".equals(name)){
			bottles[3]	= 1;founded++;	
		}else if( "BOTTLE_X_4".equals(name)){
			bottles[4]	= 1;founded++;	
		}else if( "BOTTLE_X_5".equals(name)){
			bottles[5]	= 1;founded++;
		}else if( "BOTTLE_X_6".equals(name)){
			bottles[6]	= 1;founded++;
		}else if( "BOTTLE_X_7".equals(name)){
			bottles[7]	= 1;founded++;
		}else if( "BOTTLE_X_8".equals(name)){
			bottles[8]	= 1;founded++;
		}else if( "BOTTLE_X_9".equals(name)){
			bottles[9]	= 1;founded++;
		}else if( "BOTTLE_X_10".equals(name)){
			bottles[10]	= 1;founded++;
		}else if( "BOTTLE_X_11".equals(name)){
			bottles[11]	= 1;founded++;
		}
	}
	protected void checkResult() {
		TextView wizard_calibration_result		= (TextView) findViewById(R.id.wizard_calibration_result);

		int f = 0;
		for(int i=0;i<bottles.length;i++){
			if(bottles[i] == 1){
				f++;
			}
		}
		if(f == 12 ){			// wtf: founded = 14
			wizard_calibration_result.setTextColor(Color.GREEN);
			wizard_calibration_result.setText(R.string.wizard_calibration_result_ok);
		}else{
			wizard_calibration_result.setTextColor(Color.RED);
			String notFounded = "Error. Not all dispensers were founded. Check magnets and hall sensor. Missing dispensers: ";
			for(int i=0;i<bottles.length;i++){
				if(bottles[i] == 0){
					notFounded +=i+ " ";
				}
			}
			notFounded +=". Number of dispensers founded: " + founded;
			wizard_calibration_result.setText(notFounded);
		}
	}

}
