package com.barobot.wizard;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.barobot.R;
import com.barobot.common.constant.Methods;
import com.barobot.common.interfaces.HardwareState;
import com.barobot.parser.Queue;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.message.Mainboard;
import com.barobot.parser.utils.Decoder;

public class HallXActivity extends BlankWizardActivity {
	private TextView wizard_hallx_value_left;
	private TextView wizard_hallx_value_right;
	
	private TextView wizard_hallx_results_max;
	private TextView wizard_hallx_hints_max;
	private TextView wizard_hallx_results_start;
	private TextView wizard_hallx_hints_start;
	private TextView wizard_hallx_dispenser;

	static int wrong_connection_value = 534;
	static int wrong_connection_hysteresis = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wizard_hall_x);
		wizard_hallx_value_left		= (TextView) findViewById(R.id.wizard_hallx_value_left);
		wizard_hallx_value_right	= (TextView) findViewById(R.id.wizard_hallx_value_right);
		wizard_hallx_results_max	= (TextView) findViewById(R.id.wizard_hallx_results_max);
		wizard_hallx_hints_max		= (TextView) findViewById(R.id.wizard_hallx_hints_max);
		wizard_hallx_results_start	= (TextView) findViewById(R.id.wizard_hallx_results_start);
		wizard_hallx_hints_start	= (TextView) findViewById(R.id.wizard_hallx_hints_start);
		wizard_hallx_dispenser		= (TextView) findViewById(R.id.wizard_hallx_dispenser);

		wizard_hallx_results_max.setVisibility(View.GONE);
		wizard_hallx_results_start.setVisibility(View.GONE);

		//barobot.state.set("HALLX_MIN_VALUE", "-1" );
		barobot.state.set("HALLX_MIN_STATE", "-1" );
		//barobot.state.set("HALLX_MAX_VALUE", "-1" );
		barobot.state.set("HALLX_MAX_STATE", "-1" );
	}

	public void onOptionsButtonClicked(View view)
	{
		switch(view.getId()){
			case R.id.wizard_hallx_left:
				barobot.main_queue.add( "A0", true);
				barobot.main_queue.add( new AsyncMessage( true ) {
					@Override
					public String getName() {
						return "Save hall x left value";
					}
					@Override
					public Queue run(Mainboard dev, Queue queue) {
						checkValue( true );
						updateUi();
						return null;
					}
		    	});
				barobot.lightManager.carret_color(barobot.main_queue, 255, 0, 0);
				barobot.main_queue.addWait(100);
				barobot.lightManager.carret_color(barobot.main_queue, 0, 0, 0);
				break;
			case R.id.wizard_hallx_right:
				barobot.main_queue.add("A0",true);
				barobot.main_queue.add( new AsyncMessage( true ) {
					@Override
					public String getName() {
						return "Save hall x right value";
					}
					@Override
					public Queue run(Mainboard dev, Queue queue) {
						checkValue( false );
						updateUi();
						return null;
					}
		    	});
				barobot.lightManager.carret_color(barobot.main_queue, 255, 0, 0);
				barobot.main_queue.addWait(100);
				barobot.lightManager.carret_color(barobot.main_queue, 0, 0, 0);
				break;
			default:
				super.onOptionsButtonClicked(view);
				break;
		}
	}
	private boolean checkValue( boolean left ) {
		int value		= barobot.state.getInt("HALLX", 0 );
		int hall_state	= barobot.state.getInt("HX_STATE", 0 );

		if( left ){				// left = start position
			barobot.state.set("HALLX_MIN_VALUE", value );
			barobot.state.set("HALLX_MIN_STATE", hall_state );
			
			if(hall_state == Methods.HX_STATE_9 ){		// under start
				setText( wizard_hallx_value_left, "OK" );
			}else if( hall_state == Methods.HX_STATE_0 ){
				setText( wizard_hallx_value_left, getString(R.string.wizard_hallx_no_sensor) );
			}else if( hall_state == Methods.HX_STATE_10 ){
				setText( wizard_hallx_value_left, getString(R.string.wizard_hallx_no_sensor) );		
			}else{
				setText( wizard_hallx_value_left, "Error. State: " + hall_state );
			}
		}else{
			barobot.state.set("HALLX_MAX_VALUE", value );
			barobot.state.set("HALLX_MAX_STATE", hall_state );

			if(hall_state == Methods.HX_STATE_1 ){		// under start
				setText( wizard_hallx_value_right, "OK" );
			}else if( hall_state == Methods.HX_STATE_0 ){
				setText( wizard_hallx_value_right, getString(R.string.wizard_hallx_no_sensor) );
			}else{
				setText( wizard_hallx_value_right, "Error. State: " + hall_state );
			}
		}
		if( value > 100 && value < 900){
			return true;
		}
		return false;
	}
	private void updateUi() {
		runOnUiThread(new Runnable() {
			public void run() {
				int min_value = barobot.state.getInt("HALLX_MIN_VALUE", -1 );
				int min_state = barobot.state.getInt("HALLX_MIN_STATE", -1 );

				int max_value = barobot.state.getInt("HALLX_MAX_VALUE", -1 );
				int max_state = barobot.state.getInt("HALLX_MAX_STATE", -1 );

				boolean min_ok = false;
				boolean max_ok = false;
				String msg_min = "";
				String msg_max = "";
				if( min_state > -1 ){
					if(min_state == Methods.HX_STATE_9 ){		// under start
						min_ok = true;
						msg_min = "Left magnet OK";
					}else if( min_state == Methods.HX_STATE_0 ){
						msg_min = "No sensor (Error 0). Please Turn OFF Barobot and double check connections.";
					}else if( min_state == Methods.HX_STATE_10 ){
						msg_min = "No sensor(Error 10). Please Turn OFF Barobot and double check connections.";
					}else if( min_state == Methods.HX_STATE_5 ){		// neutral value
						msg_min = "No magnet detected. Is carriage in correct position? Please Turn OFF Barobot and double check connections and magnet in start position.";
					}else if( min_state == Methods.HX_STATE_7 || min_state == Methods.HX_STATE_6 ){		// front magnet
						msg_min = "Detected ferrite magnet (front), neodymium expected. Is carriage in correct position? Please Turn OFF Barobot and double check magnet in start position.";
					}else if( min_state == Methods.HX_STATE_3 ||  min_state == Methods.HX_STATE_4){		// back magnet
						msg_min = "Detected ferrite magnet (back), neodymium expected. Is carriage in correct position? Please Turn OFF Barobot and double check magnet in start position.";
					}else{
						msg_min = "Detected ferrite magnet, neodymium expected. Is carriage on correct position? Please Turn OFF Barobot and double check magnet in start position.";
					}
				}
				if( max_state > -1 ){
					if(max_state == Methods.HX_STATE_1 ){		// under max
						max_ok = true;
						msg_max = "Right magnet OK";
					}else if( max_state == Methods.HX_STATE_0 ){
						msg_max = "No sensor (Error 0). Please Turn OFF Barobot and double check connections.";
					}else if( max_state == Methods.HX_STATE_10 ){
						msg_max = "No sensor(Error 10). Please Turn OFF Barobot and double check connections.";
					}else if( max_state == Methods.HX_STATE_5 ){		// neutral value
						msg_max = "No magnet detected. Is carriage on correct position? Please Turn OFF Barobot and double check connections and magnet on right position.";
					}else if( max_state == Methods.HX_STATE_7 || min_state == Methods.HX_STATE_6 ){		// front magnet
						msg_max = "Detected ferrite magnet (front), neodymium expected. Is carriage in correct position? Please Turn OFF Barobot and double check magnet in right position.";
					}else if( max_state == Methods.HX_STATE_3 ||  min_state == Methods.HX_STATE_4){		// back magnet
						msg_max = "Detected ferrite magnet (back), neodymium expected. Is carriage in correct position? Please Turn OFF Barobot and double check magnet in right position.";
					}else{
						msg_max = "Detected ferrite magnet, neodymium expected. Is carriage in correct position? Please Turn OFF Barobot and double check magnet in right position.";
					}
				}
				if( max_value > -1 
						&& Math.abs( max_value - min_value) < 5 
						&& Math.abs( max_value - wrong_connection_value) <= wrong_connection_hysteresis  ){		// za mala róznica

					msg_max = "Isn't hall sensor connector reversed?";
				}
				if(min_ok && max_ok){
					wizard_hallx_hints_start.setVisibility(View.VISIBLE);
					wizard_hallx_hints_start.setTextColor(Color.WHITE);
					wizard_hallx_hints_max.setVisibility(View.VISIBLE);
					wizard_hallx_hints_max.setTextColor(Color.GREEN);
					wizard_hallx_hints_start.setText(msg_min);		// left magnet ok
					wizard_hallx_hints_max.setText("Test passed successfully. Click Next to continue.");
				}else{
					if(min_ok){
						wizard_hallx_hints_start.setTextColor(Color.WHITE);
					}else{
						wizard_hallx_hints_start.setTextColor(Color.RED);
					}
					if(max_ok){
						wizard_hallx_hints_max.setTextColor(Color.WHITE);
					}else{
						wizard_hallx_hints_max.setTextColor(Color.RED);
					}
					wizard_hallx_hints_start.setText(msg_min);
					wizard_hallx_hints_max.setText(msg_max);
				}
			}
		});
	}
	private void setText(final TextView wizard_hallx_value, final String string) {
		runOnUiThread(new Runnable() {
			  public void run() {
				  wizard_hallx_value.setText(string);
			  }
		});
	}

	public void onTick(){
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	protected void updateState(HardwareState state, String name, String value) {
	//	Initiator.logger.e("ServosActivity", "onUpdate "+name + "/" + value );
		if( "HALLX_UNDER".equals(name)){
			int hall_state = state.getInt("HX_STATE", 0 );
			if( hall_state < 0 ){
				wizard_hallx_dispenser.setText( ""+hall_state );
			}else if( hall_state == Methods.HX_STATE_0 
					|| hall_state == Methods.HX_STATE_10
					|| hall_state == Methods.HX_STATE_9
					|| hall_state == Methods.HX_STATE_1 ){	// nothing
			}else{
				if( value.equals("0")){
					value = "-";
				}else if( value.equals("1")){
					value = getString(R.string.wizard_hallx_front_position);
				}else if( value.equals("2")){
					value = getString(R.string.wizard_hallx_back_position);
				}
				wizard_hallx_dispenser.setText( value );
			}
		}else if( "HX_STATE".equals(name)){
			int hall_state = Decoder.toInt(value, -10);
			if( hall_state == Methods.HX_STATE_0 ){		// error - to low
				wizard_hallx_dispenser.setText( "Error - state 0 - no sensor" );
			}else if(hall_state == Methods.HX_STATE_10 ){		// ERROR not connected
				wizard_hallx_dispenser.setText( "Error - state 10" );
			}else if(hall_state == Methods.HX_STATE_1 ){		// X max
				wizard_hallx_dispenser.setText( "X max position" );
			}else if(hall_state == Methods.HX_STATE_9 ){		// under start
				wizard_hallx_dispenser.setText( "Start Position" );
			}
		}
	}
}
