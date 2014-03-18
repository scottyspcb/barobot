package com.barobot.debug;
import com.barobot.R;
import com.barobot.R.id;
import com.barobot.activity.BarobotMain;
import com.barobot.activity.DebugActivity;
import com.barobot.common.constant.Constant;
import com.barobot.hardware.virtualComponents;

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
		boolean setting_mode	= false;
		BarobotMain bb			= BarobotMain.getInstance();
		boolean autofill		= ( virtualComponents.get("AUTOFILL", "1" )== "1");
		if(virtualComponents.set_bottle_on){
			setting_mode = true;
		}
		switch (v.getId()) {
		  case R.id.start_pos:
			  if(setting_mode){
				int posx		=  virtualComponents.driver_x.getSPos();;	
				int posy		=  virtualComponents.getInt("POSY", 0 );
				virtualComponents.hereIsStart(posx, posy);
			  }else{
				  virtualComponents.moveToStart();
			  }
			  break;
		
		  case R.id.nalej1:
			  if(setting_mode){
				  virtualComponents.hereIsBottle(0);
			  }else{
				  virtualComponents.moveToBottle(0, !autofill);
				  if( autofill){
					  virtualComponents.nalej(0);
				  }
			  }
		    break;
		  case R.id.nalej2:
			  if(setting_mode){
				  virtualComponents.hereIsBottle(1);
			  }else{
				  virtualComponents.moveToBottle(1, !autofill);
				  if( autofill){
					  virtualComponents.nalej(1);
				  }
			  }	
			  break;
		  case R.id.nalej3:
			  if(setting_mode){
				  virtualComponents.hereIsBottle(2);

			  }else{
				  virtualComponents.moveToBottle(2, !autofill);
				  if( autofill){
					  virtualComponents.nalej(2);
				  }
			  }	    	  
		      break;
		  case R.id.nalej4:
			  if(setting_mode){
				  virtualComponents.hereIsBottle(3);
			  }else{
				  virtualComponents.moveToBottle(3, !autofill);
				  if( autofill){
					  virtualComponents.nalej(3);
				  }
			  }
		      break;
		  case R.id.nalej5:
			  if(setting_mode){
				  virtualComponents.hereIsBottle(4);

			  }else{
				  virtualComponents.moveToBottle(4, !autofill);
				  if( autofill){
					  virtualComponents.nalej(4);
				  }
			  }
		      break;
		  case R.id.nalej6:
			  if(setting_mode){
				  virtualComponents.hereIsBottle(5);

			  }else{
				  virtualComponents.moveToBottle(5, !autofill);
				  if( autofill){
					  virtualComponents.nalej(5);
				  }
			  }
		      break;
		  case R.id.nalej7:
			  if(setting_mode){
				  virtualComponents.hereIsBottle(6);

			  }else{
				  virtualComponents.moveToBottle(6, !autofill);
				  if( autofill){
					  virtualComponents.nalej(6);
				  }
			  }
		      break;
		  case R.id.nalej8:
			  if(setting_mode){
				  virtualComponents.hereIsBottle(7);
			  }else{
				  virtualComponents.moveToBottle(7, !autofill);
				  if( autofill){
					  virtualComponents.nalej(7);
				  }
			  }	  
		      break;
		  case R.id.nalej9:
			  if(setting_mode){
				  virtualComponents.hereIsBottle(8);
			  }else{
				  virtualComponents.moveToBottle(8, !autofill);
				  if( autofill){
					  virtualComponents.nalej(8);
				  }
			  }
			  break;
	      case R.id.nalej10:
			  if(setting_mode){
				  virtualComponents.hereIsBottle(9);
			  }else{
				  virtualComponents.moveToBottle(9, !autofill);
				  if( autofill){
					  virtualComponents.nalej(9);
				  }
			  }
		      break;
	      case R.id.nalej11:
			  if(setting_mode){
				  virtualComponents.hereIsBottle(10);
			  }else{
				  virtualComponents.moveToBottle(10, !autofill);
				  if( autofill){
					  virtualComponents.nalej(10);
				  }
			  }
		      break;
	      case R.id.nalej12:
			  if(setting_mode){
				  virtualComponents.hereIsBottle(11);
			  }else{
				  virtualComponents.moveToBottle(11, !autofill);
				  if( autofill){
					  virtualComponents.nalej(11);
				  }
			  }	    	  
		      break;
		}
	  if(setting_mode){
		 virtualComponents.set_bottle_on= false;
		int posx		=  virtualComponents.driver_x.getSPos();;	
		int posy		=  virtualComponents.getInt("POSY", 0 );
		Toast.makeText(bb, "Zapisano ["+posx+"/"+posy+"] jako butelka", Toast.LENGTH_LONG).show();
	  }
	}
}
