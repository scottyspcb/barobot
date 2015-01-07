package com.barobot.wizard;

import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.barobot.R;
import com.barobot.common.Initiator;
import com.barobot.common.interfaces.HardwareState;
import com.barobot.parser.utils.Decoder;

public class ServoZActivity extends BlankWizardActivity {

	TextView wizard_servoz_pos;
	TextView wizard_servoz_up_pos;
	TextView wizard_servoz_down_pos;

	SeekBar wizard_servoz_seek_up;
	SeekBar wizard_servoz_seek_down;
	
	private int prescaler_max	= 4;
	private int prescaler 		= prescaler_max;

	private int neutral = 1000;
	private int lastValue = 1000;
	private int newValue = 0;

	// UP config
	static int up_step			= 20;
	static int up_min			= 1700;
	static int up_max			= 1100;

	// DOWN config
	static int down_step		= 20;
	static int down_min			= 2600;
	static int down_max			= 1700;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wizard_servo_z);
		wizard_servoz_pos = (TextView) findViewById(R.id.wizard_servoz_pos);
		enableTimer(500, 200);

		wizard_servoz_up_pos	= (TextView) findViewById(R.id.wizard_servoz_up_pos);
		wizard_servoz_down_pos	= (TextView) findViewById(R.id.wizard_servoz_down_pos);

		wizard_servoz_up_pos.setText(barobot.state.get("SERVOZ_UP_POS", "0"));
		wizard_servoz_down_pos.setText(barobot.state.get("SERVOZ_DOWN_POS", "0"));

		neutral					= barobot.state.getInt("SERVOZ_NEUTRAL", 1000);
		int up_startVal			= barobot.state.getInt("SERVOZ_UP_POS", 0);
		int down_startVal		= barobot.state.getInt("SERVOZ_DOWN_POS", 0);

		wizard_servoz_seek_up	= (SeekBar) findViewById(R.id.wizard_servoz_seek_up);
		wizard_servoz_seek_up.setMax( -(up_max - up_min) / up_step);
		wizard_servoz_seek_up.setProgress( -(up_startVal- up_min) / up_step );

		wizard_servoz_seek_up.setOnSeekBarChangeListener(
			    new OnSeekBarChangeListener(){
			        @Override
			        public void onStopTrackingTouch(SeekBar seekBar) {}
			        @Override
			        public void onStartTrackingTouch(SeekBar seekBar) {}
			        @Override
			        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)  {
						newValue	= up_min + (-progress * up_step);
						prescaler = 0; 
						wizard_servoz_up_pos.setText(""+newValue);
			        }
			    }
			);

		wizard_servoz_seek_down	= (SeekBar) findViewById(R.id.wizard_servoz_seek_down);
		wizard_servoz_seek_down.setMax( -(down_max - down_min) / down_step);
		wizard_servoz_seek_down.setProgress( -( down_startVal - down_min )/ down_step );
		wizard_servoz_seek_down.setOnSeekBarChangeListener(
			    new OnSeekBarChangeListener(){
			        @Override
			        public void onStopTrackingTouch(SeekBar seekBar) {}
			        @Override
			        public void onStartTrackingTouch(SeekBar seekBar) {}
			        @Override
			        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)  {
						newValue = down_min + (-progress * down_step);
						prescaler = 0; 
						wizard_servoz_down_pos.setText(""+newValue);
			        }
			    }
			);
		barobot.lightManager.turnOffLeds(barobot.main_queue);
		barobot.lightManager.carret_color(barobot.main_queue, 255, 255, 255);
		barobot.lightManager.setBottomLeds(barobot.main_queue, 255, 255, 255);
	}

	public void onOptionsButtonClicked(View view) {
		switch (view.getId()) {
		case R.id.wizard_servoz_up:
			int value1 = Decoder.toInt( ""+wizard_servoz_up_pos.getText(), -1);
			barobot.z.move(barobot.main_queue, value1 );
			barobot.z.disable(barobot.main_queue);
			break;

		case R.id.wizard_servoz_up_more_up:
			int val2 = wizard_servoz_seek_up.getProgress();
			wizard_servoz_seek_up.setProgress( val2+1 );	
			
			break;

		case R.id.wizard_servoz_up_more_down:
			int val1 = wizard_servoz_seek_up.getProgress();
			wizard_servoz_seek_up.setProgress( val1-1 );
			break;

			
			
		case R.id.wizard_servoz_down_more_up:
			int val3 = wizard_servoz_seek_down.getProgress();
			wizard_servoz_seek_down.setProgress( val3+1 );
			countNeutral();
			break;

		case R.id.wizard_servoz_down_more_down:
			int val4 = wizard_servoz_seek_down.getProgress();
			wizard_servoz_seek_down.setProgress( val4-1 );
			countNeutral();
			break;

		case R.id.wizard_servoz_down:
			int value2 = Decoder.toInt( ""+wizard_servoz_down_pos.getText(), -1);
			barobot.z.move(barobot.main_queue, value2 );
			barobot.z.disable(barobot.main_queue);
			break;

		case R.id.wizard_servoz_next:
			int valueUp		= Decoder.toInt( ""+wizard_servoz_up_pos.getText(), -1);
			int valueDown	= Decoder.toInt( ""+wizard_servoz_down_pos.getText(), -1);
			Initiator.logger.e("onOptionsButtonClicked.valueUp", "" + valueUp);
			Initiator.logger.e("onOptionsButtonClicked.valueDown", "" + valueDown);
			countNeutral();
			if( valueUp > 700 && valueDown < 2600 ){
				barobot.state.set("SERVOZ_UP_POS", valueUp );		// save values
			}
			if( valueDown > 700 && valueDown < 2600 ){
				barobot.state.set("SERVOZ_DOWN_POS", valueDown );		// save values
			}
			barobot.z.moveDown(barobot.main_queue, true);		// move down	
			super.onOptionsButtonClicked(view);
			break;
		default:
			super.onOptionsButtonClicked(view);
			break;
		}
	}
	private void countNeutral() {
		int valueUp		= Decoder.toInt( ""+wizard_servoz_up_pos.getText(), -1);
		int valueDown	= Decoder.toInt( ""+wizard_servoz_down_pos.getText(), -1);
		neutral	= (valueUp + valueDown) / 2;
		barobot.state.set("SERVOZ_NEUTRAL", neutral);
		
		int min			= Math.min(valueUp, valueDown);
		int range		= Math.abs(valueUp - valueDown);
		int pac_pos		= 150/range + min;
		Initiator.logger.e("countNeutral.pac_pos should be", "" + pac_pos );
	}

	public void onTick() {
		if( newValue !=lastValue && newValue > 700 && newValue < 2600 ){			// 0 on start won't move anything
			if(++prescaler >= prescaler_max ){
				if( newValue < neutral ){
					barobot.z.move(barobot.main_queue, newValue +300 );
				}else{
					barobot.z.move(barobot.main_queue, newValue -300 );
				}
				barobot.z.move(barobot.main_queue, newValue );
				barobot.z.disable(barobot.main_queue);
				lastValue = newValue;
				prescaler = 0;
			}
		}
	}
	
	protected void updateState(HardwareState state, String name, String value) {
		if( "POSZ".equals(name)){
			wizard_servoz_pos.setText(value);
			
			int value2	= Decoder.toInt(value, 0);
			
			int progress_up =-(value2- up_min) / up_step;
			if(progress_up>=0){
				wizard_servoz_seek_up.setSecondaryProgress(progress_up);
			}

			int progress_down =-(value2- down_min) / down_step;
			if(progress_down>=0){
				wizard_servoz_seek_down.setSecondaryProgress(progress_up);
			}
		}
	}
}
