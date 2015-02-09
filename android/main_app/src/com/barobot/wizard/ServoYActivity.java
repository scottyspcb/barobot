package com.barobot.wizard;

import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.barobot.R;
import com.barobot.common.Initiator;
import com.barobot.common.interfaces.HardwareState;
import com.barobot.parser.Queue;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.message.Mainboard;
import com.barobot.parser.utils.Decoder;

public class ServoYActivity extends BlankWizardActivity {
	TextView wizard_servoy_pos;
	TextView wizard_servoy_front_pos;
	TextView wizard_servoy_back_pos;

	SeekBar wizard_servoy_seek_front;
	SeekBar wizard_servoy_seek_back;

	private int prescaler_max	= 4;
	private int prescaler 		= prescaler_max;

	private int neutral = 1000;
	private int lastValue = 1000;
	private int newValue = 0;			// start with 0 = no move

	// front config
	static int front_step		= 15;
	static int front_min		= 780;
	static int front_max		= 1200;
	//static int front_startVal	= 1400;

	// back config
	static int back_step		= 15;
	static int back_min			= 1600;
	static int back_max			= 2300;
	//static int back_startVal	= 2300;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wizard_servo_y);
		wizard_servoy_pos = (TextView) findViewById(R.id.wizard_servoy_pos);
		enableTimer(500, 200);

		wizard_servoy_front_pos	= (TextView) findViewById(R.id.wizard_servoy_front_pos);
		wizard_servoy_back_pos	= (TextView) findViewById(R.id.wizard_servoy_back_pos);
		wizard_servoy_front_pos.setText(barobot.state.get("SERVOY_FRONT_POS", "0"));
		wizard_servoy_back_pos.setText(barobot.state.get("SERVOY_BACK_POS", "0"));

		neutral					= barobot.state.getInt("SERVOY_BACK_NEUTRAL", 1000);

		int front_startVal		= barobot.state.getInt("SERVOY_FRONT_POS", 0);
		int back_startVal		= barobot.state.getInt("SERVOY_BACK_POS", 0);

		wizard_servoy_seek_front	= (SeekBar) findViewById(R.id.wizard_servoy_seek_front);
		wizard_servoy_seek_front.setMax( (front_max - front_min) / front_step);
		wizard_servoy_seek_front.setProgress( (front_startVal- front_min) / front_step );
		wizard_servoy_seek_front.setOnSeekBarChangeListener(
			    new OnSeekBarChangeListener(){
			        @Override
			        public void onStopTrackingTouch(SeekBar seekBar) {}
			        @Override
			        public void onStartTrackingTouch(SeekBar seekBar) {}
			        @Override
			        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)  {
						newValue	= front_min + (progress * front_step);
						prescaler = 0; 
						wizard_servoy_front_pos.setText(""+newValue);
			        }
			    }
			);

		wizard_servoy_seek_back	= (SeekBar) findViewById(R.id.wizard_servoy_seek_back);
		wizard_servoy_seek_back.setMax( (back_max - back_min) / back_step);
		wizard_servoy_seek_back.setProgress(( back_startVal - back_min )/ back_step );
		wizard_servoy_seek_back.setOnSeekBarChangeListener(
			    new OnSeekBarChangeListener(){
			        @Override
			        public void onStopTrackingTouch(SeekBar seekBar) {}
			        @Override
			        public void onStartTrackingTouch(SeekBar seekBar) {}
			        @Override
			        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)  {
						newValue = back_min + (progress * back_step);
						prescaler = 0; 
						wizard_servoy_back_pos.setText(""+newValue);
			        }
			    }
			);
		barobot.lightManager.turnOffLeds(barobot.main_queue);
		barobot.lightManager.carret_color(barobot.main_queue, 255, 255, 255);
	//	barobot.lightManager.setBottomLeds(barobot.main_queue, 255, 255, 255);
	}

	public void onOptionsButtonClicked(View view)
	{
		switch (view.getId()) {
		case R.id.wizard_servoy_front:
			int value1 = Decoder.toInt( ""+wizard_servoy_front_pos.getText(), -1);
			barobot.y.move(barobot.main_queue, value1, 100, true, true);
			barobot.y.readHallY( barobot.main_queue );
			break;
	
		case R.id.wizard_servoy_front_more_front:
			int val1 = wizard_servoy_seek_front.getProgress();
			wizard_servoy_seek_front.setProgress( val1  -1 );
			countNeutral();

			break;
		case R.id.wizard_servoy_front_more_back:
			int val2 = wizard_servoy_seek_front.getProgress();
			wizard_servoy_seek_front.setProgress( val2 + 1 );
			countNeutral();

			break;
		case R.id.wizard_servoy_back_more_front:
			int val3 = wizard_servoy_seek_back.getProgress();
			wizard_servoy_seek_back.setProgress( val3 - 1);
			countNeutral();
			break;
		case R.id.wizard_servoy_back_more_back:
			int val4 = wizard_servoy_seek_back.getProgress();
			wizard_servoy_seek_back.setProgress( val4 +1 );
			countNeutral();

		case R.id.wizard_servoy_back:
			int value2 = Decoder.toInt( ""+wizard_servoy_back_pos.getText(), -1);
		//	barobot.moveZ(barobot.main_queue, value2 );
			barobot.y.move(barobot.main_queue, value2, 100, true, true);
			barobot.y.readHallY( barobot.main_queue );
			break;
		case R.id.wizard_servoy_next:
			int valuefront	= Decoder.toInt( ""+wizard_servoy_front_pos.getText(), -1);
			int valueback	= Decoder.toInt( ""+wizard_servoy_back_pos.getText(), -1);

			Initiator.logger.e("onOptionsButtonClicked.valuefront", "" + valuefront);
			Initiator.logger.e("onOptionsButtonClicked.valueback", "" + valueback);

			if( valuefront > 700 && valueback < 2500 ){
				barobot.state.set("SERVOY_FRONT_POS", valuefront );		// save values
			}
			if( valueback > 700 && valueback < 2500 ){
				barobot.state.set("SERVOY_BACK_POS", valueback );		// save values
			}
			countNeutral();
			barobot.y.moveToFront(barobot.main_queue);
			super.onOptionsButtonClicked(view);
			break;
		default:
			super.onOptionsButtonClicked(view);
			break;
		}
	}

	private void countNeutral() {
		int valuefront	= Decoder.toInt( ""+wizard_servoy_front_pos.getText(), -1);
		int valueback	= Decoder.toInt( ""+wizard_servoy_back_pos.getText(), -1);
		neutral	= (valuefront + valueback*4) / 5;
		barobot.state.set("SERVOY_BACK_NEUTRAL", neutral);
	}

	public void onTick(){
		if( newValue !=lastValue && newValue > 700 && newValue < 2500 ){			// 0 on start won't move anything
			if(++prescaler >= prescaler_max ){
				if( newValue < neutral ){
					barobot.y.move(barobot.main_queue, newValue +300,  100, false, true);
				}else{
					barobot.y.move(barobot.main_queue, newValue -300,  100, false, true);
				}
				barobot.y.move(barobot.main_queue, newValue, 100, true, true);
				lastValue = newValue;
				prescaler = 0;
			}
		}
	}
	
	
	protected void updateState(HardwareState state, String name, String value) {
		if( "POSY".equals(name)){
			wizard_servoy_pos.setText(value);
			int value2			= Decoder.toInt(value, 0);
			int progress_front 	= (value2- front_min) / front_step;
			if(progress_front>=0){
				wizard_servoy_seek_front.setSecondaryProgress(progress_front);
			}
			int progress_back =(value2- back_min) / back_step;
			if(progress_back>=0){
				wizard_servoy_seek_back.setSecondaryProgress(progress_front);
			}
		}
	}
	
	@Override
	protected void onDestroy() {
		barobot.lightManager.turnOffLeds(barobot.main_queue);
		super.onDestroy();
	}

}
