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
  	  	tb.setChecked(!isChecked);		//anuluj zmian, zrb to dopiero po otrzymaniu potwierdzenia
  	  	BarobotConnector barobot = Arduino.getInstance().barobot;
		Queue q					= barobot.main_queue;
		switch (v.getId()) {

			case R.id.need_glass:
				tb.setChecked(isChecked);		//tutaj jednak zmieniaj
				if(isChecked){
					barobot.state.set("NEED_GLASS", 1 );
				}else{
					barobot.state.set("NEED_GLASS", 0 );
				}

			case R.id.auto_fill_on_ready:
		  	  	tb.setChecked(isChecked);		//tutaj jednak zmieniaj 
				if(isChecked){
					barobot.state.set("AUTOFILL", "1" );
				}else{
					barobot.state.set("AUTOFILL", "0" );
				}
				break;
			case R.id.wagi_live:

	    	  	break;
		}
	}
}
