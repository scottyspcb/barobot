package com.barobot.debug;
import android.view.View;
import android.view.View.OnClickListener;

import com.barobot.R;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.parser.Queue;

public class button_zajedz  implements OnClickListener {
	@Override
	public void onClick(final View v) {
		new Thread( new Runnable(){
			@Override
			public void run() {
				exec(v);
			}}).start();
	}
	public void exec(View v) {
	//	BarobotMain bb			= BarobotMain.getInstance();
		BarobotConnector barobot = Arduino.getInstance().barobot;
		boolean autofill		= ( barobot.state.getInt("AUTOFILL", 1 ) == 1 );
		Queue q					= new Queue();

		switch (v.getId()) {
		  case R.id.start_pos:
				  barobot.moveToStart( q );
				  barobot.onDrinkFinish( q );
			  break;
		  case R.id.nalej1:
				  barobot.moveToBottle(q,0, !autofill);
				  if( autofill){
					  barobot.pour(q, 20, 0, true);
				  }
		    break;
		  case R.id.nalej2:
				  barobot.moveToBottle(q,1, !autofill);
				  if( autofill){
					  barobot.pour(q, 20, 1, true);
				  }
			  break;
		  case R.id.nalej3:
				  barobot.moveToBottle(q,2, !autofill);
				  if( autofill){
					  barobot.pour(q, 20, 2, true);
				  }   	  
		      break;
		  case R.id.nalej4:
				  barobot.moveToBottle(q,3, !autofill);
				  if( autofill){
					  barobot.pour(q, 20, 3, true);
				  }
		      break;
		  case R.id.nalej5:
				  barobot.moveToBottle(q,4, !autofill);
				  if( autofill){
					  barobot.pour(q, 20, 4, true);
				  }
		      break;
		  case R.id.nalej6:
				  barobot.moveToBottle(q,5, !autofill);
				  if( autofill){
					  barobot.pour(q, 20, 5, true);
				  }
		      break;
		  case R.id.nalej7:
				  barobot.moveToBottle(q,6, !autofill);
				  if( autofill){
					  barobot.pour(q, 20, 6, true);
				  }
		      break;
		  case R.id.nalej8:
				  barobot.moveToBottle(q,7, !autofill);
				  if( autofill){
					  barobot.pour(q, 20, 7, true);
				  } 
		      break;
		  case R.id.nalej9:
				  barobot.moveToBottle(q,8, !autofill);
				  if( autofill){
					  barobot.pour(q, 20, 8, true);
				  }
			  break;
	      case R.id.nalej10:
				  barobot.moveToBottle(q,9, !autofill);
				  if( autofill){
					  barobot.pour(q, 20, 9, true);
				  }
		      break;
	      case R.id.nalej11:
				  barobot.moveToBottle(q,10, !autofill);
				  if( autofill){
					  barobot.pour(q, 20, 10, true);
				  }
		      break;
	      case R.id.nalej12:
				  barobot.moveToBottle(q,11, !autofill);
				  if( autofill){
					  barobot.pour(q, 20, 11, true);
				  }    	  
		      break;
		}
		barobot.main_queue.add(q);
	}
}
