package com.barobot.wizard;

import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.barobot.R;
import com.barobot.common.Initiator;
import com.barobot.common.interfaces.HardwareState;
import com.barobot.hardware.devices.PcbType;
import com.barobot.parser.utils.Decoder;

public class ServoZActivity extends BlankWizardActivity {
	TextView wizard_servoz_pos;
	TextView wizard_servoz_up_pos;
	TextView wizard_servoz_down_pos;

	SeekBar wizard_servoz_seek_up;
	SeekBar wizard_servoz_seek_down;

	
	private int direction		= PcbType.FORWARD;
	
	private int prescaler_max	= 4;
	private int prescaler 		= prescaler_max;

	private int lastValue		= 1000;
	private int newValue		= 0;

	// UP config
	static int up_step			= 20;
	static int up_min			= 1700;
	static int up_max			= 1100;

	// DOWN config
	static int down_step		= 20;
	static int down_min			= 2600;
	static int down_max			= 1700;

	static int min_delta		= 30;

	private int neutral			= 1000;	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wizard_servo_z);

		up_step			= barobot.pcb.getDefault( "z_up_step" );
		up_min			= barobot.pcb.getDefault( "z_up_min" );
		up_max			= barobot.pcb.getDefault( "z_up_max" );

		// DOWN config
		down_step		= barobot.pcb.getDefault( "z_down_step" );
		down_min		= barobot.pcb.getDefault( "z_down_min" );
		down_max		= barobot.pcb.getDefault( "z_down_max" );

		neutral			= barobot.pcb.getDefault( "z_neutral" );
		min_delta		= barobot.pcb.getDefault( "z_delta_min" );
		direction		= barobot.pcb.getDefault( "z_direction" );

		wizard_servoz_pos = (TextView) findViewById(R.id.wizard_servoz_pos);
		enableTimer(500, 200);

		wizard_servoz_up_pos	= (TextView) findViewById(R.id.wizard_servoz_up_pos);
		wizard_servoz_down_pos	= (TextView) findViewById(R.id.wizard_servoz_down_pos);

		wizard_servoz_up_pos.setText(barobot.state.get("SERVOZ_UP_POS", "0"));
		wizard_servoz_down_pos.setText(barobot.state.get("SERVOZ_DOWN_POS", "0"));

		neutral					= barobot.state.getInt("SERVOZ_NEUTRAL", neutral );
		int up_startVal			= barobot.state.getInt("SERVOZ_UP_POS", down_min);
		int down_startVal		= barobot.state.getInt("SERVOZ_DOWN_POS", down_max);

		int up_progress = hardwaretoSlider( 1, up_startVal );
		
		wizard_servoz_seek_up	= (SeekBar) findViewById(R.id.wizard_servoz_seek_up);
		wizard_servoz_seek_up.setMax( Math.abs((up_max - up_min) / up_step));
		wizard_servoz_seek_up.setProgress( up_progress);
		wizard_servoz_seek_up.setOnSeekBarChangeListener(
			    new OnSeekBarChangeListener(){
			        @Override
			        public void onStopTrackingTouch(SeekBar seekBar) {}
			        @Override
			        public void onStartTrackingTouch(SeekBar seekBar) {}
			        @Override
			        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)  {
			        	newValue	= sliderToHardware(1, progress);
						
						prescaler = 0; 
						wizard_servoz_up_pos.setText(""+newValue);
			        }
			    }
			);

		wizard_servoz_seek_down	= (SeekBar) findViewById(R.id.wizard_servoz_seek_down);
		wizard_servoz_seek_down.setMax( Math.abs((down_max - down_min) / down_step));
		
		int back_progress = hardwaretoSlider( 2, down_startVal );
		wizard_servoz_seek_down.setProgress( back_progress);
		wizard_servoz_seek_down.setOnSeekBarChangeListener(
			    new OnSeekBarChangeListener(){
			        @Override
			        public void onStopTrackingTouch(SeekBar seekBar) {}
			        @Override
			        public void onStartTrackingTouch(SeekBar seekBar) {}
			        @Override
			        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)  {
						
						newValue	= sliderToHardware(2, progress);
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
			barobot.z.move(barobot.main_queue, value1, true );
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
			barobot.z.move(barobot.main_queue, value2, true );
			break;

		case R.id.wizard_servoz_next:
			int valueUp		= Decoder.toInt( ""+wizard_servoz_up_pos.getText(), -1);
			int valueDown	= Decoder.toInt( ""+wizard_servoz_down_pos.getText(), -1);
			Initiator.logger.e("onOptionsButtonClicked.valueUp", "" + valueUp);
			Initiator.logger.e("onOptionsButtonClicked.valueDown", "" + valueDown);
			countNeutral();
			barobot.state.set("SERVOZ_UP_POS", valueUp );		// save values
			barobot.state.set("SERVOZ_DOWN_POS", valueDown );		// save values
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
		int z_pos_known 	= barobot.state.getInt("z_pos_known", 0 );
		if( z_pos_known == 0 ){
			int SERVOZ_DOWN_POS = barobot.state.getInt("SERVOZ_DOWN_POS", 0 );
			barobot.state.set("SERVOZ_NEUTRAL", SERVOZ_DOWN_POS);
		}else{
			neutral	= Math.abs((valueUp + valueDown)) / 2;
			barobot.state.set("SERVOZ_NEUTRAL", neutral);
		}
		int min			= Math.min(valueUp, valueDown);
		int range		= Math.abs(valueUp - valueDown);
		int pac_pos		= 150/range + min;
		Initiator.logger.e("countNeutral.pac_pos should be", "" + pac_pos );
	}

	public void onTick() {
		if( newValue !=lastValue 
				&& newValue > barobot.pcb.getDefault( "z_physical_min" ) 
				&& newValue < barobot.pcb.getDefault( "z_physical_max" )
			){			// 0 on start won't move anything
			if(++prescaler >= prescaler_max ){
				int z_pos_known				= barobot.state.getInt("z_pos_known", 0 );
				int SERVOZ_READ_HYSTERESIS	= barobot.state.getInt("SERVOZ_READ_HYSTERESIS", 20 );
				if( z_pos_known == 0 || Math.abs(newValue - lastValue) < SERVOZ_READ_HYSTERESIS ){
					if( newValue < neutral ){
						barobot.z.move(barobot.main_queue, newValue +min_delta, false );
					}else{
						barobot.z.move(barobot.main_queue, newValue -min_delta, false );
					}
				}
				barobot.z.move(barobot.main_queue, newValue, true );
				lastValue = newValue;
				prescaler = 0;
			}
		}
	}


	int sliderToHardware(int posType, int progress){
		//direction
		if( posType == 1){		// up
			if( direction > 0 ){
				return up_min + (progress * up_step);
			}else{
				return up_max - (progress * up_step);
			}
		}else{	// down
			if( direction > 0 ){
				return down_min + (progress * down_step);
			}else{
				return down_max - (progress * down_step);
			}
		}
	}
	int hardwaretoSlider(int posType, int value){
		if( posType == 1){		// up
			if( direction > 0 ){
				return Math.abs( value- up_min) / up_step;
			}else{
				return Math.abs( up_max - value ) / up_step;
			}
		}else{	// down
			if( direction > 0 ){
				return Math.abs( value - down_min )/ down_step;
			}else{
				return Math.abs( down_max - value )/ down_step;
			}
		}
	}

	protected void updateState(HardwareState state, String name, String value) {
		if( "POSZ".equals(name)){
			wizard_servoz_pos.setText(value);
			int value2	= Decoder.toInt(value, 0);
			int progress_up = Math.abs((value2- up_min) / up_step);
			if(progress_up>=0){
				wizard_servoz_seek_up.setSecondaryProgress(progress_up);
			}
			int progress_down =Math.abs((value2- down_min) / down_step);
			if(progress_down>=0){
				wizard_servoz_seek_down.setSecondaryProgress(progress_up);
			}
		}
	}
}
