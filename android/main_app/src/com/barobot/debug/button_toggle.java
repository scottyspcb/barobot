package com.barobot.debug;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ToggleButton;

import com.barobot.R;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;

public class button_toggle implements OnClickListener{

	@Override
	public void onClick(View v) {
  	  	ToggleButton tb			= (ToggleButton) v;
  	  	boolean isChecked		= tb.isChecked();
  	  	BarobotConnector barobot = Arduino.getInstance().barobot;
		switch (v.getId()) {

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
