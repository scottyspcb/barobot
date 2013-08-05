package com.barobot;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ToggleButton;

public class button_zajedz  implements OnClickListener {
	@Override
	public void onClick(View v) {
		boolean setting_mode	= false;	
		ToggleButton tb			= null;
		DebugWindow bb			= DebugWindow.getInstance();
		if(bb!=null){
			tb			= (ToggleButton) bb.findViewById(R.id.set_bottle);
			if (tb != null) {
				setting_mode		= tb.isChecked();
			}
		}
		switch (v.getId()) {
		  case R.id.nalej1:
			  if(setting_mode){
				  virtualComponents.hereIsBottle(0);
				  bb.setChecked( R.id.set_bottle, false );
			  }else{
				  virtualComponents.moveToBottle(0);
			  }
		    break;
		  case R.id.nalej2:
			  if(setting_mode){
				  virtualComponents.hereIsBottle(1);
				  bb.setChecked( R.id.set_bottle, false );
			  }else{
				  virtualComponents.moveToBottle(1);
			  }		      break;
		  case R.id.nalej3:
			  if(setting_mode){
				  virtualComponents.hereIsBottle(2);
				  bb.setChecked( R.id.set_bottle, false );
			  }else{
				  virtualComponents.moveToBottle(2);
			  }	    	  
		      break;
		  case R.id.nalej4:
			  if(setting_mode){
				  virtualComponents.hereIsBottle(3);
				  bb.setChecked( R.id.set_bottle, false );
			  }else{
				  virtualComponents.moveToBottle(3);
			  }
		      break;
		  case R.id.nalej5:
			  if(setting_mode){
				  virtualComponents.hereIsBottle(4);
				  bb.setChecked( R.id.set_bottle, false );
			  }else{
				  virtualComponents.moveToBottle(4);
			  }
		      break;
		  case R.id.nalej6:
			  if(setting_mode){
				  virtualComponents.hereIsBottle(5);
				  bb.setChecked( R.id.set_bottle, false );
			  }else{
				  virtualComponents.moveToBottle(5);
			  }
		      break;
		  case R.id.nalej7:
			  if(setting_mode){
				  virtualComponents.hereIsBottle(6);
				  bb.setChecked( R.id.set_bottle, false );
			  }else{
				  virtualComponents.moveToBottle(6);
			  }
		      break;
		  case R.id.nalej8:
			  if(setting_mode){
				  virtualComponents.hereIsBottle(7);
				  bb.setChecked( R.id.set_bottle, false );
			  }else{
				  virtualComponents.moveToBottle(7);
			  }	  
		      break;
		  case R.id.nalej9:
			  if(setting_mode){
				  virtualComponents.hereIsBottle(8);
				  bb.setChecked( R.id.set_bottle, false );
			  }else{
				  virtualComponents.moveToBottle(8);
			  }
			  break;
	      case R.id.nalej10:
			  if(setting_mode){
				  virtualComponents.hereIsBottle(9);
				  bb.setChecked( R.id.set_bottle, false );
			  }else{
				  virtualComponents.moveToBottle(9);
			  }
		      break;
	      case R.id.nalej11:
			  if(setting_mode){
				  virtualComponents.hereIsBottle(10);
				  bb.setChecked( R.id.set_bottle, false );
			  }else{
				  virtualComponents.moveToBottle(10);
			  }
		      break;
	      case R.id.nalej12:
			  if(setting_mode){
				  virtualComponents.hereIsBottle(11);
				  bb.setChecked( R.id.set_bottle, false );
			  }else{
				  virtualComponents.moveToBottle(11);
			  }	    	  
		      break;
	      case R.id.nalej13:
			  if(setting_mode){
				  virtualComponents.hereIsBottle(12);
				  bb.setChecked( R.id.set_bottle, false );
			  }else{
				  virtualComponents.moveToBottle(12);
			  }    	  
	          break;
	      case R.id.nalej14:
			  if(setting_mode){
				  virtualComponents.hereIsBottle(13);
				  bb.setChecked( R.id.set_bottle, false );
			  }else{
				  virtualComponents.moveToBottle(13);
			  }
	          break;
	      case R.id.nalej15:
			  if(setting_mode){
				  virtualComponents.hereIsBottle(14);
				  bb.setChecked( R.id.set_bottle, false );
			  }else{
				  virtualComponents.moveToBottle(14);
			  }
	          break;
	      case R.id.nalej_tutaj:
	    	  //virtualComponents.moveToBottle(15);
	    	  virtualComponents.nalej( 5000 );	// 5000 to czas - 5 sek
	          break;
		}
	}
}
