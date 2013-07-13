package com.barobot;
import android.view.View;
import android.view.View.OnClickListener;

public class button_zajedz  implements OnClickListener {
	@Override
	public void onClick(View v) {
		Constant.log(Constant.TAG,"click:"+ v.getId());

		switch (v.getId()) {
		  case R.id.nalej1:
			  virtualComponents.moveToBottle(1);
		    break;
		  case R.id.nalej2:
			  virtualComponents.moveToBottle(2);
		      break;
		  case R.id.nalej3:
			  virtualComponents.moveToBottle(3);	    	  
		      break;
		  case R.id.nalej4:
			  virtualComponents.moveToBottle(4);
		      break;
		  case R.id.nalej5:
			  virtualComponents.moveToBottle(5);
		      break;
		  case R.id.nalej6:
			  virtualComponents.moveToBottle(6);
		      break;
		  case R.id.nalej7:
			  virtualComponents.moveToBottle(7);
		      break;
		  case R.id.nalej8:
			  virtualComponents.moveToBottle(8);	  
		      break;

		  case R.id.nalej9:
			  virtualComponents.moveToBottle(9);
			  break;
	      case R.id.nalej10:
	    	  virtualComponents.moveToBottle(10);
		      break;
	      case R.id.nalej11:
	    	  virtualComponents.moveToBottle(11);
		      break;
	      case R.id.nalej12:
	    	  virtualComponents.moveToBottle(12);	    	  
		      break;
	      case R.id.nalej13:
	    	  virtualComponents.moveToBottle(13);    	  
	          break;
	      case R.id.nalej14:
	    	  virtualComponents.moveToBottle(14);
	          break;
	      case R.id.nalej15:
	    	  virtualComponents.moveToBottle(15);
	          break;
	      case R.id.nalej16:
	    	  //virtualComponents.moveToBottle(16);
	    	  virtualComponents.nalej( 5000 );
	          break;
		}
	}
}
