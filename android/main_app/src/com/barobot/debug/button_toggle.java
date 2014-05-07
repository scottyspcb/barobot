package com.barobot.debug;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ToggleButton;

import com.barobot.R;
import com.barobot.hardware.virtualComponents;
import com.barobot.parser.Queue;

public class button_toggle implements OnClickListener{

	@Override
	public void onClick(View v) {
  	  	ToggleButton tb			= (ToggleButton) v;
  	  	boolean isChecked		= tb.isChecked();
  	  	tb.setChecked(!isChecked);		//anuluj zmian, zrb to dopiero po otrzymaniu potwierdzenia
		Queue q					= virtualComponents.barobot.main_queue;
		switch (v.getId()) {

			case R.id.need_glass:
				tb.setChecked(isChecked);		//tutaj jednak zmieniaj
				if(isChecked){
					virtualComponents.need_glass_up = true;
				}else{
					virtualComponents.need_glass_up = false;
				}

			case R.id.auto_fill_on_ready:
		  	  	tb.setChecked(isChecked);		//tutaj jednak zmieniaj 
				if(isChecked){
					virtualComponents.barobot.state.set("AUTOFILL", "1" );
				}else{
					virtualComponents.barobot.state.set("AUTOFILL", "0" );
				}
				break;
			case R.id.wagi_live:

	    	  	break;
		}
	}
}
