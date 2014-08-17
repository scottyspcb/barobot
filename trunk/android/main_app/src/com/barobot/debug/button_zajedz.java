package com.barobot.debug;
import com.barobot.BarobotMain;
import com.barobot.R;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

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
		BarobotMain bb			= BarobotMain.getInstance();
		BarobotConnector barobot = Arduino.getInstance().barobot;
		boolean autofill		= ( barobot.state.getInt("AUTOFILL", 1 ) == 1 );
		boolean setting_mode	= button_click.set_bottle_on;

		switch (v.getId()) {
		  case R.id.start_pos:
			  if(setting_mode){
				int posx		=  barobot.driver_x.getSPos();;	
				int posy		=  barobot.state.getInt("POSY", 0 );
				barobot.hereIsStart(posx, posy);
			  }else{
				  barobot.moveToStart( barobot.main_queue );
				  barobot.onDrinkFinish( barobot.main_queue );
			  }
			  break;
		  case R.id.nalej1:
			  if(setting_mode){
				  barobot.hereIsBottle(0);
			  }else{
				  barobot.moveToBottle(barobot.main_queue,0, !autofill);
				  if( autofill){
					  barobot.pour(barobot.main_queue, 20, 0, true);
				  }
			  }
		    break;
		  case R.id.nalej2:
			  if(setting_mode){
				  barobot.hereIsBottle(1);
			  }else{
				  barobot.moveToBottle(barobot.main_queue,1, !autofill);
				  if( autofill){
					  barobot.pour(barobot.main_queue, 20, 1, true);
				  }
			  }
			  break;
		  case R.id.nalej3:
			  if(setting_mode){
				  barobot.hereIsBottle(2);
			  }else{
				  barobot.moveToBottle(barobot.main_queue,2, !autofill);
				  if( autofill){
					  barobot.pour(barobot.main_queue, 20, 2, true);
				  }
			  }	    	  
		      break;
		  case R.id.nalej4:
			  if(setting_mode){
				  barobot.hereIsBottle(3);
			  }else{
				  barobot.moveToBottle(barobot.main_queue,3, !autofill);
				  if( autofill){
					  barobot.pour(barobot.main_queue, 20, 3, true);
				  }
			  }
		      break;
		  case R.id.nalej5:
			  if(setting_mode){
				  barobot.hereIsBottle(4);
			  }else{
				  barobot.moveToBottle(barobot.main_queue,4, !autofill);
				  if( autofill){
					  barobot.pour(barobot.main_queue, 20, 4, true);
				  }
			  }
		      break;
		  case R.id.nalej6:
			  if(setting_mode){
				  barobot.hereIsBottle(5);
			  }else{
				  barobot.moveToBottle(barobot.main_queue,5, !autofill);
				  if( autofill){
					  barobot.pour(barobot.main_queue, 20, 5, true);
				  }
			  }
		      break;
		  case R.id.nalej7:
			  if(setting_mode){
				  barobot.hereIsBottle(6);

			  }else{
				  barobot.moveToBottle(barobot.main_queue,6, !autofill);
				  if( autofill){
					  barobot.pour(barobot.main_queue, 20, 6, true);
				  }
			  }
		      break;
		  case R.id.nalej8:
			  if(setting_mode){
				  barobot.hereIsBottle(7);
			  }else{
				  barobot.moveToBottle(barobot.main_queue,7, !autofill);
				  if( autofill){
					  barobot.pour(barobot.main_queue, 20, 7, true);
				  }
			  }	  
		      break;
		  case R.id.nalej9:
			  if(setting_mode){
				  barobot.hereIsBottle(8);
			  }else{
				  barobot.moveToBottle(barobot.main_queue,8, !autofill);
				  if( autofill){
					  barobot.pour(barobot.main_queue, 20, 8, true);
				  }
			  }
			  break;
	      case R.id.nalej10:
			  if(setting_mode){
				  barobot.hereIsBottle(9);
			  }else{
				  barobot.moveToBottle(barobot.main_queue,9, !autofill);
				  if( autofill){
					  barobot.pour(barobot.main_queue, 20, 9, true);
				  }
			  }
		      break;
	      case R.id.nalej11:
			  if(setting_mode){
				  barobot.hereIsBottle(10);
			  }else{
				  barobot.moveToBottle(barobot.main_queue,10, !autofill);
				  if( autofill){
					  barobot.pour(barobot.main_queue, 20, 10, true);
				  }
			  }
		      break;
	      case R.id.nalej12:
			  if(setting_mode){
				  barobot.hereIsBottle(11);
			  }else{
				  barobot.moveToBottle(barobot.main_queue,11, !autofill);
				  if( autofill){
					  barobot.pour(barobot.main_queue, 20, 11, true);
				  }
			  }	    	  
		      break;
		}
	  if(setting_mode){
		button_click.set_bottle_on= false;
		int posx		=  barobot.driver_x.getSPos();;	
		int posy		=  barobot.state.getInt("POSY", 0 );
		Toast.makeText(bb, "Zapisano ["+posx+"/"+posy+"] jako butelka", Toast.LENGTH_LONG).show();
	  }
	}
}
