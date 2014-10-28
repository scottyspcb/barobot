package com.barobot.debug;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ToggleButton;

import com.barobot.R;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.parser.Queue;

public class button_toggle implements OnClickListener{

	@Override
	public void onClick(View v) {
  	  	ToggleButton tb			= (ToggleButton) v;
  	  	boolean isChecked		= tb.isChecked();
  	  	BarobotConnector barobot = Arduino.getInstance().barobot;
		switch (v.getId()) {
			case R.id.need_hall_up:
				if(isChecked){
					barobot.state.set("NEED_HALL_X", 1 );
				}else{
					barobot.state.set("NEED_HALL_X", 0 );
				}
				break;
			case R.id.allow_light_cup:
				if(isChecked){
					barobot.state.set("ALLOW_LIGHT_CUP", 1 );
				}else{
					barobot.state.set("ALLOW_LIGHT_CUP", 0 );
				}
				break;		

			case R.id.need_glass:
				if(isChecked){
					barobot.state.set("NEED_GLASS", 1 );
				}else{
					barobot.state.set("NEED_GLASS", 0 );
				}
				break;

			case R.id.auto_fill_on_ready:
				if(isChecked){
					barobot.state.set("AUTOFILL", "1" );
				}else{
					barobot.state.set("AUTOFILL", "0" );
				}
				break;
		}
	}
}
