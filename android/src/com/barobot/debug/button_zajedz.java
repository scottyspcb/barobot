package com.barobot.debug;
import com.barobot.BarobotMain;
import com.barobot.DebugActivity;
import com.barobot.R;
import com.barobot.R.id;
import com.barobot.hardware.virtualComponents;
import com.barobot.utils.Constant;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import android.widget.ToggleButton;

public class button_zajedz  implements OnClickListener {
	@Override
	public void onClick(View v) {
		boolean setting_mode	= false;
		String autofill			= virtualComponents.get("AUTOFILL", "0" );
		BarobotMain bb		= BarobotMain.getInstance();

		if(virtualComponents.set_bottle_on){
			setting_mode = true;
		}
	
		switch (v.getId()) {
		  case R.id.start_pos:
			  if(setting_mode){
				int posx		=  virtualComponents.getInt("POSX", 0 );	
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
				  virtualComponents.moveToBottle(0);
				  if( autofill== "1"){
					  virtualComponents.nalej();
				  }
			  }
		    break;
		  case R.id.nalej2:
			  if(setting_mode){
				  virtualComponents.hereIsBottle(1);
			  }else{
				  virtualComponents.moveToBottle(1);
				  if( autofill== "1"){
					  virtualComponents.nalej();
				  }
			  }	
			  break;
		  case R.id.nalej3:
			  if(setting_mode){
				  virtualComponents.hereIsBottle(2);

			  }else{
				  virtualComponents.moveToBottle(2);
				  if( autofill== "1"){
					  virtualComponents.nalej();
				  }
			  }	    	  
		      break;
		  case R.id.nalej4:
			  if(setting_mode){
				  virtualComponents.hereIsBottle(3);

			  }else{
				  virtualComponents.moveToBottle(3);
				  if( autofill== "1"){
					  virtualComponents.nalej();
				  }
			  }
		      break;
		  case R.id.nalej5:
			  if(setting_mode){
				  virtualComponents.hereIsBottle(4);

			  }else{
				  virtualComponents.moveToBottle(4);
				  if( autofill== "1"){
					  virtualComponents.nalej();
				  }
			  }
		      break;
		  case R.id.nalej6:
			  if(setting_mode){
				  virtualComponents.hereIsBottle(5);

			  }else{
				  virtualComponents.moveToBottle(5);
				  if( autofill== "1"){
					  virtualComponents.nalej();
				  }
			  }
		      break;
		  case R.id.nalej7:
			  if(setting_mode){
				  virtualComponents.hereIsBottle(6);

			  }else{
				  virtualComponents.moveToBottle(6);
				  if( autofill== "1"){
					  virtualComponents.nalej();
				  }
			  }
		      break;
		  case R.id.nalej8:
			  if(setting_mode){
				  virtualComponents.hereIsBottle(7);

			  }else{
				  virtualComponents.moveToBottle(7);
				  if( autofill== "1"){
					  virtualComponents.nalej();
				  }
			  }	  
		      break;
		  case R.id.nalej9:
			  if(setting_mode){
				  virtualComponents.hereIsBottle(8);

			  }else{
				  virtualComponents.moveToBottle(8);
				  if( autofill== "1"){
					  virtualComponents.nalej();
				  }
			  }
			  break;
	      case R.id.nalej10:
			  if(setting_mode){
				  virtualComponents.hereIsBottle(9);

			  }else{
				  virtualComponents.moveToBottle(9);
				  if( autofill== "1"){
					  virtualComponents.nalej();
				  }
			  }
		      break;
	      case R.id.nalej11:
			  if(setting_mode){
				  virtualComponents.hereIsBottle(10);

			  }else{
				  virtualComponents.moveToBottle(10);
				  if( autofill== "1"){
					  virtualComponents.nalej();
				  }
			  }
		      break;
	      case R.id.nalej12:
			  if(setting_mode){
				  virtualComponents.hereIsBottle(11);

			  }else{
				  virtualComponents.moveToBottle(11);
				  if( autofill== "1"){
					  virtualComponents.nalej();
				  }
			  }	    	  
		      break;
		}
	  if(setting_mode){
		  virtualComponents.set_bottle_on= false;
		int posx		=  virtualComponents.getInt("POSX", 0 );	
		int posy		=  virtualComponents.getInt("POSY", 0 );
		Toast.makeText(bb, "Zapisano ["+posx+"/"+posy+"] jako butelka", Toast.LENGTH_LONG).show();
	  }
	}
}
