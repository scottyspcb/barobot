package com.barobot.wizard;

import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.barobot.R;
import com.barobot.common.constant.Methods;
import com.barobot.common.interfaces.HardwareState;
import com.barobot.parser.Queue;
import com.barobot.parser.utils.Decoder;

public class ServosActivity extends BlankWizardActivity {
	TextView wizard_servos_x_pos;
	TextView wizard_servos_x_speed;
//	TextView wizard_servos_hall_value;
	TextView wizard_servos_dispenser;
	SeekBar wizard_servos_speed;
	static int step		= 100;
	static int min		= 1500;
	static int max		= 3200;
	private int startVal;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wizard_servos);
		wizard_servos_x_pos			= (TextView) findViewById(R.id.wizard_servos_x_pos);
		wizard_servos_x_speed		= (TextView) findViewById(R.id.wizard_servos_x_speed);
//		wizard_servos_hall_value	= (TextView) findViewById(R.id.wizard_servos_hall_value);
		wizard_servos_dispenser		= (TextView) findViewById(R.id.wizard_servos_dispenser);
		wizard_servos_speed			= (SeekBar) findViewById(R.id.wizard_servos_speed);

		enableTimer( 1000, 1000 );

		startVal = barobot.state.getInt("DRIVER_X_SPEED", 0 );
				
		wizard_servos_x_pos.setText( barobot.state.get("POSX", "0") );
		wizard_servos_x_speed.setText( barobot.state.get("DRIVER_X_SPEED", "0") );
//		wizard_servos_hall_value.setText( barobot.state.get("HALLX", "0") );		
		wizard_servos_speed.setMax( (max - min) / step);
		wizard_servos_speed.setProgress(( startVal - min )/ step );
		wizard_servos_speed.setOnSeekBarChangeListener(
			    new OnSeekBarChangeListener(){
			        @Override
			        public void onStopTrackingTouch(SeekBar seekBar) {}
			        @Override
			        public void onStartTrackingTouch(SeekBar seekBar) {}
			        @Override
			        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)  {
						int newValue = min + (progress * step);
						if( newValue > 0 ){
							barobot.state.set("DRIVER_X_SPEED", newValue);
						}
			        }
			    }
			);
	}

	protected void updateState(HardwareState state, String name, String value) {
	//	Initiator.logger.e("ServosActivity", "onUpdate "+name + "/" + value );
		if( "POSX".equals(name)){
			wizard_servos_x_pos.setText( value );
		}else if( "DRIVER_X_SPEED".equals(name)){
			wizard_servos_x_speed.setText( value );
	//	}else if( "HALLX".equals(name)){
	//		wizard_servos_hall_value.setText( value );
		}else if( "HALLX_UNDER".equals(name)){
			int hall_state = state.getInt("HX_STATE", 0 );
			if( hall_state < 0 ){
				wizard_servos_dispenser.setText( ""+hall_state );
			}else if( hall_state == Methods.HX_STATE_0 
					|| hall_state == Methods.HX_STATE_10
					|| hall_state == Methods.HX_STATE_9
					|| hall_state == Methods.HX_STATE_1 ){	// nothing
			}else{
				if( value.equals("0")){
					value = "no";
				}else if( value.equals("1")){
					value = "front position";
				}else if( value.equals("2")){
					value = "back position";
				}
				wizard_servos_dispenser.setText( value );
			}
		}else if( "HX_STATE".equals(name)){
			int hall_state = Decoder.toInt(value, -10);
			if( hall_state == Methods.HX_STATE_0 ){		// error - to low
				wizard_servos_dispenser.setText( "Error - state 0 - no sensor" );
			}else if(hall_state == Methods.HX_STATE_10 ){		// ERROR not connected
				wizard_servos_dispenser.setText( "Error - state 10" );
			}else if(hall_state == Methods.HX_STATE_1 ){		// X max
				wizard_servos_dispenser.setText( "X max position" );

			}else if(hall_state == Methods.HX_STATE_9 ){		// under start
				wizard_servos_dispenser.setText( "Start Position" );
			}
		}
	}

	public void onOptionsButtonClicked(View view)
	{
		Queue q		= barobot.main_queue;
		int posx	= barobot.state.getInt("POSX", 0);
		ToggleButton tb			= (ToggleButton) findViewById(R.id.wizard_servos_brake);
		switch(view.getId()){
			case R.id.wizard_servos_brake:
				if(tb.isChecked()){
					barobot.main_queue.add("EX", true );
				}else{
					barobot.main_queue.add("DX", true );
				}
				break;
			case R.id.wizard_servos_left:
				tb.setChecked(false);
				barobot.z.moveDown(q, true);
				barobot.x.moveTo(q, (posx - 2000));
				break;
			case R.id.wizard_servos_right:
				tb.setChecked(false);
				barobot.z.moveDown(q, true);
				barobot.x.moveTo(q, (posx + 2000));
				break;
			default:
				tb.setChecked(false);
				super.onOptionsButtonClicked(view);
				break;
		}
	}

	public void onTick(){
		barobot.main_queue.add("A0", true);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		barobot.state.set("DRIVER_X_SPEED", startVal);
		barobot.main_queue.add("DX", true );
	}
}
