package com.barobot.debug;
import com.barobot.R;
import com.barobot.R.id;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.virtualComponents;
import com.barobot.parser.Queue;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ToggleButton;

public class button_toggle implements OnClickListener{

	@Override
	public void onClick(View v) {
  	  	ToggleButton tb			= (ToggleButton) v;
  	  	boolean isChecked		= tb.isChecked();
  	  	tb.setChecked(!isChecked);		//anuluj zmian, zrb to dopiero po otrzymaniu potwierdzenia
		Queue q					= Arduino.getInstance().getMainQ();
		switch (v.getId()) {
	    	case R.id.led1:
	    		if(isChecked){
	    			q.add("SET LED1 ON", false);
	    		}else{
	    			q.add("SET LED1 OFF", false);	    			
	    		}
		    	break;
	    	case R.id.led2:
	    		if(isChecked){
	    			q.add("SET LED2 ON", false);
	    		}else{
	    			q.add("SET LED2 OFF", false);   			
	    		}
	    		break;
			case R.id.led3:
	    		if(isChecked){
	    			q.add("SET LED3 ON", false);
	    		}else{
	    			q.add("SET LED3 OFF", false);	    			
	    		}	    	  
				break;
			case R.id.led4:	// LED6
	    		if(isChecked){
	    			q.add("SET LED4 ON", false);
	    		}else{
	    			q.add("SET LED4 OFF", false);
	    		}
				break;
			case R.id.led5:
	    		if(isChecked){
	    			q.add("SET LED5 ON", false);
	    		}else{
	    			q.add("SET LED5 OFF", false);
	    		}
	    	  	break;
			case R.id.led6:	
	    		if(isChecked){
	    			q.add("SET LED6 ON", false);
	    		}else{
	    			q.add("SET LED6 OFF", false);
	    		}
	    	  	break;
			case R.id.led7:
	    		if(isChecked){
	    			q.add("SET LED7 ON", false);
	    		}else{
	    			q.add("SET LED7 OFF", false);
	    		}
	    	  	break;
			case R.id.led8:
	    		if(isChecked){
	    			q.add("SET LED8 ON", false);
	    		}else{
	    			q.add("SET LED8 OFF", false);
	    		}
	    	  	break;
			case R.id.led9:
	    		if(isChecked){
	    			q.add("SET LED9 ON", false);
	    		}else{
	    			q.add("SET LED9 OFF", false);
	    		}
	    	  	break;
			case R.id.led10:
	    		if(isChecked){
	    			q.add("SET LED10 ON", false);
	    		}else{
	    			q.add("SET LED10 OFF", false);
	    		}
	    	  	break;
	    	 
	    // ---------------------------------  	

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
					virtualComponents.set("AUTOFILL", "1" );
				}else{
					virtualComponents.set("AUTOFILL", "0" );
				}
				break;
			case R.id.wagi_live:
	    		if(isChecked){
	    			virtualComponents.enable_analog(q, virtualComponents.ANALOG_WAGA, 50, 2);
	    		}else{
	    			virtualComponents.disable_analog( q, virtualComponents.ANALOG_WAGA ); 			
	    		}
	    	  	break;
		}
	}
}
