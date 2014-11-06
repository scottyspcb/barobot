package com.barobot.wizard;

import com.barobot.R;
import com.barobot.common.Initiator;
import com.barobot.common.constant.Constant;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class WeightSensorActivity extends BlankWizardActivity {
	TextView wizard_weight_weight;
	TextView wizard_weight_value;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wizard_weigh_sensor);
		wizard_weight_weight	= (TextView) findViewById(R.id.wizard_weight_weight);
		wizard_weight_value		= (TextView) findViewById(R.id.wizard_weight_value);
		enableTimer( 400, 150 );
		wizard_weight_weight.setText( "" );	

		final BarobotConnector barobot = Arduino.getInstance().barobot;
		barobot.lightManager.turnOffLeds(barobot.main_queue);
		barobot.lightManager.carret_color(barobot.main_queue, 0, 255, 0);
	}
	public void onOptionsButtonClicked(View view)
	{
		final BarobotConnector barobot = Arduino.getInstance().barobot;
		switch(view.getId()){
		//	case R.id.wizard_weight_next:
		//		break;
			case R.id.wizard_weight_without:
				int weight1 =barobot.state.getInt("LAST_WEIGHT", 0);
				barobot.state.getInt("WEIGHT_WITHOUT_TRY", weight1 );
				TextView wizard_weight_value_without	= (TextView) findViewById(R.id.wizard_weight_value_without);
				wizard_weight_value_without.setText( "" + weight1 );

				barobot.lightManager.carret_color(barobot.main_queue, 255, 0, 0);
				barobot.main_queue.addWait(100);
				barobot.lightManager.carret_color(barobot.main_queue, 0, 255, 0);
				
				break;
			case R.id.wizard_weight_with:
				int weight2		= barobot.state.getInt("LAST_WEIGHT", 0 );
				int weight3		= barobot.state.getInt("WEIGHT_WITHOUT_TRY", 0);
				int diff		= weight2 - weight3;
				Initiator.logger.i( "WeightSensorActivity.diff", "" + diff );
				if( diff > 2 ){
					float ratio = (float)diff / Constant.glass_try_weight;
					int ratioi	= (int)ratio * 10000;
					Initiator.logger.i("WeightSensorActivity.ratio", "" + ratioi  + "/" + diff) ;
					barobot.state.set("WEIGHT_PRESCALER", ratioi);
					barobot.state.set("WEIGHT_WITH_TRY", weight2);
				}
				TextView wizard_weight_value_with	= (TextView) findViewById(R.id.wizard_weight_value_with);
				wizard_weight_value_with.setText( "" + weight2 );

				barobot.lightManager.carret_color(barobot.main_queue, 255, 0, 0);
				barobot.main_queue.addWait(100);
				barobot.lightManager.carret_color(barobot.main_queue, 0, 255, 0);

				break;
			default:
				super.onOptionsButtonClicked(view);
				break;
		}
	}

	public void onTick(){
		final BarobotConnector barobot = Arduino.getInstance().barobot;
		barobot.main_queue.add("A2", true);					// load cell
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				int prescaler = barobot.state.getInt("WEIGHT_PRESCALER", 0);
				int weight =barobot.state.getInt("LAST_WEIGHT", 0);
				wizard_weight_value.setText( ""+weight );
				if( prescaler > 0 ){
					float prescalerf	= (float) prescaler / 10000;
					int value	= Math.round((float)weight / prescalerf);
					wizard_weight_weight.setText( "" + value );	
					Initiator.logger.i("WeightSensorActivity.WEIGHT_PRESCALER", ""+prescaler+ "/" + value);
				}
			}
		});	
	}
}
	