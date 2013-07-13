package com.barobot;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ToggleButton;

public class button_toggle implements OnClickListener{

	@Override
	public void onClick(View v) {
  	  	ToggleButton tb			= (ToggleButton) v;
  	  	boolean isChecked		= tb.isChecked();
  	  	tb.setChecked(!isChecked);		//anuluj zmianê, zrób to dopiero po otrzymaniu potwierdzenia
  	  	queue q = queue.getInstance();

		switch (v.getId()) {
	    	case R.id.led1:				// LED9
	    		if(isChecked){
	    			q.send("SET LED1 ON");
	    		}else{
	    			q.send("SET LED1 OFF");	    			
	    		}
		    	break;
	    	case R.id.led2:		// LED7
	    		if(isChecked){
	    			q.send("SET LED2 ON");
	    		}else{
	    			q.send("SET LED2 OFF");	    			
	    		}
	    		break;
			case R.id.led3:					// LED5
	    		if(isChecked){
	    			q.send("SET LED3 ON");
	    		}else{
	    			q.send("SET LED3 OFF");	    			
	    		}	    	  
				break;
			case R.id.led4:	// LED6
	    		if(isChecked){
	    			q.send("SET LED4 ON");
	    		}else{
	    			q.send("SET LED4 OFF");	    			
	    		}
				break;
			case R.id.led5:					// LED8
	    		if(isChecked){
	    			q.send("SET LED5 ON");
	    		}else{
	    			q.send("SET LED5 OFF");	    			
	    		}
	    	  	break;
			case R.id.led6:					// LED8
	    		if(isChecked){
	    			q.send("SET LED6 ON");
	    		}else{
	    			q.send("SET LED6 OFF");	    			
	    		}
	    	  	break;
			case R.id.led7:					// LED8
	    		if(isChecked){
	    			q.send("SET LED7 ON");
	    		}else{
	    			q.send("SET LED7 OFF");	    			
	    		}
	    	  	break;
			case R.id.led8:					// LED8
	    		if(isChecked){
	    			q.send("SET LED8 ON");
	    		}else{
	    			q.send("SET LED8 OFF");	    			
	    		}
	    	  	break;
			case R.id.led9:					// LED8
	    		if(isChecked){
	    			q.send("SET LED9 ON");
	    		}else{
	    			q.send("SET LED9 OFF");	    			
	    		}
	    	  	break;
			case R.id.led10:					// LED8
	    		if(isChecked){
	    			q.send("SET LED10 ON");
	    		}else{
	    			q.send("SET LED10 OFF");	    			
	    		}
	    	  	break;
		}
	}	
}
