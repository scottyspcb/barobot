package com.barobot.wizard;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.barobot.R;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.sofa.route.CommandRoute;

public class ServosActivity extends BlankWizardActivity {
	TextView wizard_servos_x_pos;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wizard_servos);
		wizard_servos_x_pos	= (TextView) findViewById(R.id.wizard_servos_x_pos);
		enableTimer( 500, 500 );
	}

	public void onOptionsButtonClicked(View view)
	{
		switch(view.getId()){
			case R.id.wizard_servos_left:
				CommandRoute.runCommand("command_set_x_100");
				break;
			case R.id.wizard_servos_right:
				CommandRoute.runCommand("command_set_x100");
				break;
			default:
				super.onOptionsButtonClicked(view);
				break;
		}
	}

	public void onTick(){
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				final BarobotConnector barobot = Arduino.getInstance().barobot;
				wizard_servos_x_pos.setText( barobot.state.get("POSX", "0") );
			}
		});	
	}
}
