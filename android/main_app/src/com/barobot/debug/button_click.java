package com.barobot.debug;

import java.util.Random;

import com.barobot.AppInvoker;
import com.barobot.R;
import com.barobot.R.id;
import com.barobot.R.layout;
import com.barobot.activity.BarobotMain;
import com.barobot.hardware.rpc_message;
import com.barobot.hardware.virtualComponents;
import com.barobot.utils.ArduinoQueue;
import com.barobot.utils.Constant;
import com.barobot.utils.Arduino;
import com.barobot.utils.RunnableWithData;
import com.barobot.web.server.AJS;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
public class button_click implements OnClickListener{
	private Context dbw;
	public button_click(Context debugWindow){
		dbw = debugWindow;
	}

	@Override
	public void onClick(View v) {
		Arduino ar		= Arduino.getInstance();
		ArduinoQueue q	= new ArduinoQueue();
		int posx		= virtualComponents.getInt("POSX", 0 );
		int posy		= virtualComponents.getInt("POSY", 0 );
		
	//	Log.i("XXXX", ""+ posx);

		switch (v.getId()) {
		case R.id.set_x_1000:
	//		Log.i("nextpos-10000", "old: "+posx + " next: "+ ( posx -10000));
			virtualComponents.moveZDown( q );
			virtualComponents.moveX( q, ( posx -10000));
			ar.send(q);
			break;
		case R.id.set_x_100:
			
	//		Log.i("nextpos-1000", "old: "+posx + " next: "+ ( posx -1000));
			
			virtualComponents.moveZDown( q );
			virtualComponents.moveX( q, ( posx -1000));
			ar.send(q);
			break;
		case R.id.set_x_10:
			
	//		Log.i("nextpos-100", "old: "+posx + " next: "+ ( posx -100)); 

			virtualComponents.moveZDown( q );
			virtualComponents.moveX( q, ( posx -100));
			ar.send(q);
			break;
		case R.id.set_x10:
			
		//	Log.i("nextpos+100", "old: "+posx + " next: "+ ( posx +100)); 
			
			virtualComponents.moveZDown( q );
			virtualComponents.moveX( q, ( posx +100));
			ar.send(q);
			break;
		case R.id.set_x100:
			
		//	Log.i("nextpos+1000", "old: "+posx + " next: "+ ( posx +1000));
			
			virtualComponents.moveZDown( q );
			virtualComponents.moveX( q, ( posx +1000));
			ar.send(q);
			break;
		case R.id.set_x1000:
		//	Log.i("nextpos+10000", "old: "+posx + " next: "+ ( posx +10000));
			virtualComponents.moveZDown( q );
			virtualComponents.moveX( q, ( posx +10000));
			ar.send(q);
			break;  
		case R.id.set_y_600:
			virtualComponents.moveZDown( q );
			virtualComponents.moveY( q, ( posy -1000), true);
			ar.send(q);
			break;
		case R.id.set_y_100:
			virtualComponents.moveZDown( q );
			virtualComponents.moveY( q, ( posy -100), true);
			ar.send(q);
			break;
		case R.id.set_y_10:
			virtualComponents.moveZDown( q );
			virtualComponents.moveY( q, ( posy -10), true);
			ar.send(q);
			break;
		case R.id.set_y10:
			virtualComponents.moveZDown( q );
			virtualComponents.moveY( q, ( posy +10), true);
			ar.send(q);
			break;
		case R.id.set_y100:
			virtualComponents.moveZDown( q );
			virtualComponents.moveY( q, ( posy +100), true);
			ar.send(q);
			break;
		case R.id.set_y600:
			virtualComponents.moveZDown( q );
			virtualComponents.moveY( q, ( posy +1000), true);
			ar.send(q);
			break;

		case R.id.set_neutral_y:
			virtualComponents.set("NEUTRAL_POS_Y", ""+posy );
			String nn = virtualComponents.get("NEUTRAL_POS_Y", "0" );
			Toast.makeText(dbw, "To jest pozycja bezpieczna ("+nn+")...", Toast.LENGTH_LONG).show();
			break;
		case R.id.goToNeutralY:
			virtualComponents.moveY( q, virtualComponents.get("NEUTRAL_POS_Y", "0" ));
			q.add("DY", true);
			ar.send(q);
			break;
		case R.id.kalibrujy:
			this.setSpeed();		// jeśli mam szklankę to bardzo wolno
			virtualComponents.moveZDown( q );
			virtualComponents.moveY(q, 900, false );
			virtualComponents.moveY(q, 2100, false );
			virtualComponents.moveY(q, 900, false );
			ar.send(q);
			break;
		case R.id.kalibrujz:
			virtualComponents.moveZDown( q );
			ar.send(q);
			break;
		case R.id.machajx:
			virtualComponents.moveZDown( q );
			virtualComponents.moveY( q, virtualComponents.SERVOY_FRONT_POS, true);
			long lengthx4	=  virtualComponents.getInt("LENGTHX", 600 );
			for( int i =0; i<10;i++){
			//	virtualComponents.moveX( q, (lengthx4/4) );
				//virtualComponents.moveX( q, (lengthx4/4 * 3) );
				
				virtualComponents.moveX( q, 0 );
				virtualComponents.moveX( q, lengthx4 );
			}
			q.add("DX", true);
			ar.send(q);
			break;
		case R.id.machajy:
			virtualComponents.moveZDown( q );
			for( int i =0; i<10;i++){
				virtualComponents.moveY( q, virtualComponents.SERVOY_FRONT_POS, false );
				virtualComponents.moveY( q, virtualComponents.SERVOY_BACK_POS, false );
			}
			virtualComponents.moveY( q, virtualComponents.SERVOY_FRONT_POS, false );
			q.add("DY", true);
			ar.send(q);
			break;
		case R.id.machajz:
			for( int i =0; i<10;i++){
				virtualComponents.moveZDown(q);
				virtualComponents.moveZUp(q,true);
			}
			virtualComponents.moveZDown(q);
			q.add("DZ", true);
			ar.send(q);
			break;
		case R.id.losujx:
			Random generator2 = new Random( 19580427 );
			virtualComponents.moveZDown( q );
			virtualComponents.moveY( q, virtualComponents.SERVOY_FRONT_POS, true );
			long lengthx5	=  virtualComponents.getInt("LENGTHX", 600 );
		    for(int f = 0;f<20;){
		    	int left = generator2.nextInt((int)(lengthx5/100 / 2));
		    	int right =generator2.nextInt((int)(lengthx5/100 / 2));
		    	right+= lengthx5/100 / 2;
				virtualComponents.moveX( q, (left * 100) );
				virtualComponents.moveX( q, (right * 100) );
		        f=f+2;
		      }
		    ar.send(q);
			break;
		case R.id.losujy:
			Random generator3 = new Random( 19580427 );
			virtualComponents.moveZDown( q );

/*
			virtualComponents.SERVOY_BACK_POS
			virtualComponents.SERVOY_FRONT_POS
		    for(int f = 0;f<20;){
		    	int left = generator3.nextInt((int)(lengthy5/100 / 2));
		    	int right =generator3.nextInt((int)(lengthy5/100));
		    	right+= lengthy5/100 / 2;
				virtualComponents.moveY( q, (left * 100));
				virtualComponents.moveY( q, (right * 100));
		        f=f+2;
		    }*/
		    ar.send(q);
			break;
		case R.id.fill5000:
			virtualComponents.nalej( -1 );
			break;
		case R.id.set_bottle:
			virtualComponents.set_bottle_on = true;
			// przełącz okno na listę butelek, 
			// zablokuj przyciski i po naciśnięciu ustaw w tym miejscu butelkę
			Constant.log(Constant.TAG,"wybierz butelkę...");
			Toast.makeText(dbw, "Wybierz butelkę do zapisania pozycji " + posx + "/" + posy, Toast.LENGTH_LONG).show();
//			dbw.tabHost.setCurrentTabByTag("tab0");
//			dbw.tabHost.bringToFront();
//			dbw.tabHost.setEnabled(true);

			break;
		case R.id.max_z:
			if(virtualComponents.need_glass_up){
				q.addWaitGlass();
			}
			q.add("EX", true);
		//	q.add("EY", true);		
			virtualComponents.moveZUp(q,true);
			q.add("DX", true);
			q.add("DY", true);
			q.add(Constant.GETXPOS, true);
			ar.send(q);
			break;
		case R.id.min_z:
			q.add("EX", true);
		//	q.add("EY", true);
			virtualComponents.moveZDown( q );
			q.add("DX", true);
			q.add("DY", true);
			q.add(Constant.GETXPOS, true);
			ar.send(q);
			break;

		case R.id.max_x:
			virtualComponents.moveZDown( q );
			long lengthx2	=  virtualComponents.getInt("LENGTHX", 600 );
			virtualComponents.moveX( q, posx +lengthx2 );
			ar.send(q );
			break;

		case R.id.max_y:
			virtualComponents.moveZDown( q );
			virtualComponents.moveY( q, virtualComponents.SERVOY_BACK_POS, true );
			ar.send(q);	
			break;
		case R.id.min_x:
			virtualComponents.moveZDown( q );
			long lengthx3	=  virtualComponents.getInt("LENGTHX", 600 );
			virtualComponents.moveX( q, -lengthx3 );
			ar.send(q );
			break;
		case R.id.min_y:
			virtualComponents.moveZDown( q );
			virtualComponents.moveY( q, virtualComponents.SERVOY_FRONT_POS, true );
			ar.send(q);	
			break;	
		case R.id.unlock:
			ar.unlock();
			break;
		case R.id.pacpac:	
			virtualComponents.pacpac();
			break;
		case R.id.smile:
			AppInvoker.getInstance().cm.doPhoto();
			break;
		case R.id.bottle_next:
			break;
		case R.id.kalibrujx:
			virtualComponents.kalibrcja();
			/*
			virtualComponents.moveZDown( q );			
			virtualComponents.moveY( q, virtualComponents.SERVOY_FRONT_POS, true);
			long lengthx1	=  virtualComponents.getInt("LENGTHX", 600 ) * 10;		
			virtualComponents.moveX( q, (posx +lengthx1));
			q.add("DY", false);
			virtualComponents.moveX( q, -lengthx1);
			virtualComponents.moveX( q, 0 );
			ar.send(q);*/
			break;
		case R.id.enabley:
	//		q.add("EY", true);
			ar.send(q);
			break;		
		case R.id.disablez:
			q.add("DZ", true);
			ar.send(q);
			break;		
		case R.id.disabley:
			q.add("DY", true);
			ar.send(q);
			break;	
		case R.id.reset_carret:	
			ar.send("RESETN 1");
			break;	
		case R.id.goto_max_x:
			break;
		case R.id.goto_min_x:
			break;
		case R.id.find_bottles:
			virtualComponents.kalibrcja();
			break;			
		case R.id.bottle_prev:
			/*
	  	  	final Button btn			= (Button) v;
			final rpc_message m2 = new rpc_message( true ) {
				@Override
				public ArduinoQueue run() {
					this.name		= "unlock UI";
					return null;
				}
				public boolean isBlocing() {
					return true;
				}
			};
			Arduino ard			= Arduino.getInstance();
			ArduinoQueue qq		= new ArduinoQueue();
			qq.add( m2 );
			ard.send(q);
*/
			break;
	   }
	}

	private void setSpeed() {
		if( virtualComponents.hasGlass() ){
			this.setSpeed( virtualComponents.WITHGLASS );
		}else{
			this.setSpeed( virtualComponents.WITHOUTGLASS );
		}
	}

	private void setSpeed(int withglass) {
//		Arduino ar = Arduino.getInstance();
/*
		q.add("SX 400", true);
		q.add("SY 400", true);	
		q.add("SX 400", true);
		q.add("SY 400", true);
	*/	
	}
}
