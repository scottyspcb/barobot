package com.barobot.wizard;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.barobot.R;
import com.barobot.common.Initiator;
import com.barobot.common.constant.Constant;
import com.barobot.common.interfaces.HardwareState;
import com.barobot.parser.utils.Decoder;

public class WeightSensorActivity extends BlankWizardActivity {
	TextView wizard_weight_weight;
	TextView wizard_weight_value;
	TextView wizard_weight_result;
	static int min_diff	= 100;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wizard_weigh_sensor);
		wizard_weight_weight	= (TextView) findViewById(R.id.wizard_weight_weight);
		wizard_weight_value		= (TextView) findViewById(R.id.wizard_weight_value);
		wizard_weight_result		= (TextView) findViewById(R.id.wizard_weight_result);

		enableTimer( 400, 150 );
		wizard_weight_weight.setText( "" );	

		barobot.lightManager.turnOffLeds(barobot.main_queue);
		barobot.lightManager.carret_color(barobot.main_queue, 0, 255, 0);

	}
	public void onOptionsButtonClicked(View view)
	{
		switch(view.getId()){
		//	case R.id.wizard_weight_next:
		//		break;
			case R.id.wizard_weight_without:
				int weight1 =barobot.state.getInt("LAST_WEIGHT", 0);
				barobot.state.set("WEIGHT_WITHOUT_TRAY", weight1 );
				TextView wizard_weight_value_without	= (TextView) findViewById(R.id.wizard_weight_value_without);
				wizard_weight_value_without.setText( "" + weight1 );

				barobot.lightManager.carret_color(barobot.main_queue, 255, 0, 0);
				barobot.main_queue.addWait(100);
				barobot.lightManager.carret_color(barobot.main_queue, 0, 255, 0);

				if( weight1 < 10 || weight1 > 1022 ){
					wizard_weight_result.setText(R.string.wizard_weight_sensor_error);
					wizard_weight_result.setTextColor(Color.RED);
				}else{
					wizard_weight_result.setText("");
				}
				break;
			case R.id.wizard_weight_with:
				int weight2		= barobot.state.getInt("LAST_WEIGHT", 0 );
				int weight3		= barobot.state.getInt("WEIGHT_WITHOUT_TRAY", 0);
				int diff		= weight2 - weight3;

				barobot.state.set("WEIGHT_WITH_TRAY", 0);
				Initiator.logger.i( "WeightSensorActivity.diff", "" + diff );
				TextView wizard_weight_value_with	= (TextView) findViewById(R.id.wizard_weight_value_with);
				wizard_weight_value_with.setText( "" + weight2 );

				barobot.lightManager.carret_color(barobot.main_queue, 255, 0, 0);
				barobot.main_queue.addWait(100);
				barobot.lightManager.carret_color(barobot.main_queue, 0, 255, 0);

				if( diff < min_diff ){
					wizard_weight_result.setText(R.string.wizard_diff_to_low);
					wizard_weight_result.setTextColor(Color.RED);
				}else{
					wizard_weight_result.setText(R.string.wizard_weight_is_ok);
					wizard_weight_result.setTextColor(Color.GREEN);
				}
				if( diff > 2 ){
					float ratio = (float)diff / Constant.glass_tray_weight;
					int ratioi	= (int)(ratio * 10000);
					Initiator.logger.i("WeightSensorActivity.ratio", "" + ratioi  + "/" + diff) ;
					barobot.state.set("WEIGHT_PRESCALER", ratioi);
					barobot.state.set("WEIGHT_WITH_TRAY", weight2);
				}
				break;
			default:
				super.onOptionsButtonClicked(view);
				break;
		}
	}
	public void onTick(){
		if(!barobot.main_queue.isBusy()){
			barobot.main_queue.add("A2", true);					// load cell
		}
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		barobot.main_queue.add("DX", true );
	}
	protected void updateState(HardwareState state, String name, String value) {
		if( "LAST_WEIGHT".equals(name)){
			int weight	= Decoder.toInt(value, 0);
			int prescaler	= barobot.state.getInt("WEIGHT_PRESCALER", 0);
			wizard_weight_value.setText( ""+weight );
			if( prescaler > 0 ){
				float prescalerf	= (float) prescaler / 10000;
				int value2			= Math.round((float)weight / prescalerf);
				wizard_weight_weight.setText( "" + value2 );	
	//			Initiator.logger.i("WeightSensorActivity.WEIGHT_PRESCALER", ""+prescaler+ "/" + value);
			}
		}
	}
}

	