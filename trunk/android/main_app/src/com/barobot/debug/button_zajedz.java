package com.barobot.debug;
import com.barobot.R;
import com.barobot.R.id;
import com.barobot.activity.BarobotMain;
import com.barobot.activity.DebugActivity;
import com.barobot.common.constant.Constant;
import com.barobot.hardware.virtualComponents;
import com.barobot.hardware.devices.BarobotConnector;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import android.widget.ToggleButton;

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
		BarobotConnector barobot = virtualComponents.barobot;
		boolean autofill		= ( barobot.state.getInt("AUTOFILL", 1 ) == 1 );
		boolean setting_mode	= button_click.set_bottle_on;
		
		
		switch (v.getId()) {
		  case R.id.start_pos:
			  if(setting_mode){
				int posx		=  barobot.driver_x.getSPos();;	
				int posy		=  barobot.state.getInt("POSY", 0 );
				barobot.hereIsStart(posx, posy);
			  }else{
				  barobot.moveToStart();
				  barobot.onDrinkFinish();
			  }
			  break;
		
		  case R.id.nalej1:
			  if(setting_mode){
				  barobot.hereIsBottle(0);
			  }else{
				  barobot.moveToBottle(0, !autofill);
				  if( autofill){
					  barobot.nalej(0);
				  }
			  }
		    break;
		  case R.id.nalej2:
			  if(setting_mode){
				  barobot.hereIsBottle(1);
			  }else{
				  barobot.moveToBottle(1, !autofill);
				  if( autofill){
					  barobot.nalej(1);
				  }
			  }	
			  break;
		  case R.id.nalej3:
			  if(setting_mode){
				  barobot.hereIsBottle(2);

			  }else{
				  barobot.moveToBottle(2, !autofill);
				  if( autofill){
					  barobot.nalej(2);
				  }
			  }	    	  
		      break;
		  case R.id.nalej4:
			  if(setting_mode){
				  barobot.hereIsBottle(3);
			  }else{
				  barobot.moveToBottle(3, !autofill);
				  if( autofill){
					  barobot.nalej(3);
				  }
			  }
		      break;
		  case R.id.nalej5:
			  if(setting_mode){
				  barobot.hereIsBottle(4);
			  }else{
				  barobot.moveToBottle(4, !autofill);
				  if( autofill){
					  barobot.nalej(4);
				  }
			  }
		      break;
		  case R.id.nalej6:
			  if(setting_mode){
				  barobot.hereIsBottle(5);

			  }else{
				  barobot.moveToBottle(5, !autofill);
				  if( autofill){
					  barobot.nalej(5);
				  }
			  }
		      break;
		  case R.id.nalej7:
			  if(setting_mode){
				  barobot.hereIsBottle(6);

			  }else{
				  barobot.moveToBottle(6, !autofill);
				  if( autofill){
					  barobot.nalej(6);
				  }
			  }
		      break;
		  case R.id.nalej8:
			  if(setting_mode){
				  barobot.hereIsBottle(7);
			  }else{
				  barobot.moveToBottle(7, !autofill);
				  if( autofill){
					  barobot.nalej(7);
				  }
			  }	  
		      break;
		  case R.id.nalej9:
			  if(setting_mode){
				  barobot.hereIsBottle(8);
			  }else{
				  barobot.moveToBottle(8, !autofill);
				  if( autofill){
					  barobot.nalej(8);
				  }
			  }
			  break;
	      case R.id.nalej10:
			  if(setting_mode){
				  barobot.hereIsBottle(9);
			  }else{
				  barobot.moveToBottle(9, !autofill);
				  if( autofill){
					  barobot.nalej(9);
				  }
			  }
		      break;
	      case R.id.nalej11:
			  if(setting_mode){
				  barobot.hereIsBottle(10);
			  }else{
				  barobot.moveToBottle(10, !autofill);
				  if( autofill){
					  barobot.nalej(10);
				  }
			  }
		      break;
	      case R.id.nalej12:
			  if(setting_mode){
				  barobot.hereIsBottle(11);
			  }else{
				  barobot.moveToBottle(11, !autofill);
				  if( autofill){
					  barobot.nalej(11);
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
