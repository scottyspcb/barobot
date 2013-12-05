package com.barobot.debug;
import com.barobot.R;
import com.barobot.R.id;
import com.barobot.hardware.virtualComponents;
import com.barobot.utils.Arduino;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ToggleButton;

public class button_toggle implements OnClickListener{

	@Override
	public void onClick(View v) {
  	  	ToggleButton tb			= (ToggleButton) v;
  	  	boolean isChecked		= tb.isChecked();
  	  	tb.setChecked(!isChecked);		//anuluj zmian, zrb to dopiero po otrzymaniu potwierdzenia

  	  	Arduino ar = Arduino.getInstance();
		switch (v.getId()) {
	    	case R.id.led1:
	    		if(isChecked){
	    			ar.send("SET LED1 ON");
	    		}else{
	    			ar.send("SET LED1 OFF");	    			
	    		}
		    	break;
	    	case R.id.led2:
	    		if(isChecked){
	    			ar.send("SET LED2 ON");
	    		}else{
	    			ar.send("SET LED2 OFF");	    			
	    		}
	    		break;
			case R.id.led3:
	    		if(isChecked){
	    			ar.send("SET LED3 ON");
	    		}else{
	    			ar.send("SET LED3 OFF");	    			
	    		}	    	  
				break;
			case R.id.led4:	// LED6
	    		if(isChecked){
	    			ar.send("SET LED4 ON");
	    		}else{
	    			ar.send("SET LED4 OFF");	    			
	    		}
				break;
			case R.id.led5:
	    		if(isChecked){
	    			ar.send("SET LED5 ON");
	    		}else{
	    			ar.send("SET LED5 OFF");	    			
	    		}
	    	  	break;
			case R.id.led6:	
	    		if(isChecked){
	    			ar.send("SET LED6 ON");
	    		}else{
	    			ar.send("SET LED6 OFF");	    			
	    		}
	    	  	break;
			case R.id.led7:
	    		if(isChecked){
	    			ar.send("SET LED7 ON");
	    		}else{
	    			ar.send("SET LED7 OFF");	    			
	    		}
	    	  	break;
			case R.id.led8:
	    		if(isChecked){
	    			ar.send("SET LED8 ON");
	    		}else{
	    			ar.send("SET LED8 OFF");	    			
	    		}
	    	  	break;
			case R.id.led9:
	    		if(isChecked){
	    			ar.send("SET LED9 ON");
	    		}else{
	    			ar.send("SET LED9 OFF");	    			
	    		}
	    	  	break;
			case R.id.led10:
	    		if(isChecked){
	    			ar.send("SET LED10 ON");
	    		}else{
	    			ar.send("SET LED10 OFF");
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

			case R.id.auto_fill:
		  	  	tb.setChecked(isChecked);		//tutaj jednak zmieniaj 
				if(isChecked){
					virtualComponents.set("AUTOFILL", "1" );
				}else{
					virtualComponents.set("AUTOFILL", "0" );
				}
				break;
			case R.id.wagi_live:
	    		if(isChecked){
	    			virtualComponents.enable_analog(ar, virtualComponents.ANALOG_WAGA, 50, 2);
	    		}else{
	    			virtualComponents.disable_analog( ar, virtualComponents.ANALOG_WAGA ); 			
	    		}
	    	  	break;
		}
	}
}
