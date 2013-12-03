package com.barobot.debug;

import java.util.Random;

import com.barobot.BarobotMain;
import com.barobot.R;
import com.barobot.R.id;
import com.barobot.R.layout;
import com.barobot.drinks.RunnableWithData;
import com.barobot.hardware.rpc_message;
import com.barobot.hardware.virtualComponents;
import com.barobot.utils.ArduinoQueue;
import com.barobot.utils.Constant;
import com.barobot.utils.Arduino;
import com.barobot.webview.AJS;

import android.app.Dialog;
import android.content.Context;
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
		Arduino ar = Arduino.getInstance();
		ArduinoQueue q = new ArduinoQueue();

		switch (v.getId()) {
		case R.id.set_x_1000:
			virtualComponents.moveZDown( q );
			q.add("X" + ((-1000) * virtualComponents.mnoznikx), true);
			ar.send(q);
			break;
		case R.id.set_x_100:
			virtualComponents.moveZDown( q );
			q.add("X" + ((-100) * virtualComponents.mnoznikx), true );
			ar.send(q);
			break;
		case R.id.set_x_10:
			virtualComponents.moveZDown( q );
			q.add("X" + ((-10) * virtualComponents.mnoznikx), true );
			ar.send(q);
			break;
		case R.id.set_x10:
			virtualComponents.moveZDown( q );
			q.add("X+" + ((10) * virtualComponents.mnoznikx), true );
			ar.send(q);
			break;
		case R.id.set_x100:
			virtualComponents.moveZDown( q );
			q.add("X+" + ((100) * virtualComponents.mnoznikx), true );
			ar.send(q);
			break;
		case R.id.set_x1000:
			virtualComponents.moveZDown( q );
			q.add("X+" + ((1000) * virtualComponents.mnoznikx), true );
			ar.send(q);
			break;  
		case R.id.set_y_600:
			virtualComponents.moveZDown( q );
			q.add("Y" + ((-1000) * virtualComponents.mnoznikx), true );
			ar.send(q);
			break;
		case R.id.set_y_100:
			virtualComponents.moveZDown( q );
			q.add("Y" + ((-100) * virtualComponents.mnoznikx), true );
			ar.send(q);
			break;
		case R.id.set_y_10:
			virtualComponents.moveZDown( q );
			q.add("Y" + ((-10) * virtualComponents.mnozniky), true );
			ar.send(q);
			break;
		case R.id.set_y10:
			virtualComponents.moveZDown( q );
			q.add("Y+" + ((10) * virtualComponents.mnozniky), true );
			ar.send(q);
			break;
		case R.id.set_y100:
			virtualComponents.moveZDown( q );
			q.add("Y+" + ((100) * virtualComponents.mnozniky), true );
			ar.send(q);
			break;
		case R.id.set_y600:
			virtualComponents.moveZDown( q );
			q.add("Y+" + ((1000) * virtualComponents.mnozniky), true );
			ar.send(q);
			break;
		case R.id.kalibrujx:
			virtualComponents.moveZDown( q );
			q.add("Y" + virtualComponents.get("NEUTRAL_POS_Y", "0" ), true );
			long lengthx1	=  virtualComponents.getInt("LENGTHX", 600 ) * 10;
			q.add("X-" + lengthx1, true );
			q.add("X+" + lengthx1, true );
			q.add("X0", true);
			ar.send(q);
			break;
		case R.id.set_neutral_y:
			String posy2		=  virtualComponents.get("POSY", "0" );
			virtualComponents.set("NEUTRAL_POS_Y", ""+posy2 );
			String nn = virtualComponents.get("NEUTRAL_POS_Y", "0" );
			Toast.makeText(dbw, "To jest pozycja bezpieczna ("+nn+")...", Toast.LENGTH_LONG).show();
			break;
		case R.id.goToNeutralY:
			q.add("Y" + virtualComponents.get("NEUTRAL_POS_Y", "0" ), true );
			ar.send(q);
			break;
		case R.id.kalibrujy:
			this.setSpeed();		// jeśli mam szklankę to bardzo wolno
			virtualComponents.moveZDown( q );
			long lengthy1	=  virtualComponents.getInt("LENGTHY", 600 )  * 100;
			q.add("Y-" + lengthy1, true );
			q.add("Y+" + lengthy1, true );
			q.add("Y0", true);
			ar.send(q);
			break;
		case R.id.kalibrujz:
			virtualComponents.moveZDown( q );
			ar.send(q);
			break;
		case R.id.machajx:
			virtualComponents.moveZDown( q );
			q.add("Y" + virtualComponents.get("NEUTRAL_POS_Y", "0" ), true );
			long lengthx4	=  virtualComponents.getInt("LENGTHX", 600 );
			for( int i =0; i<10;i++){
				//q.add("X" + (lengthx4/4), true );
				//q.add("X" + (lengthx4/4 * 3) , true );
				q.add("X0", true );
				q.add("X" + (lengthx4) , true );
			}
			ar.send(q);
			break;
		case R.id.machajy:
			virtualComponents.moveZDown( q );
			long lengthy4	=  virtualComponents.getInt("LENGTHY", 600 );
			for( int i =0; i<10;i++){
				q.add("Y" + (lengthy4/4), true );
				q.add("Y" + (lengthy4/4 * 3) , true );
			}
			ar.send(q);
			break;
		case R.id.machajz:
			q.add("EZ", true);
			for( int i =0; i<10;i++){
				q.add("SET Z MAX", true);		// SET Z zwraca początek operacji a nie koniec
				q.addWait( virtualComponents.SERVOZ_UP_TIME );	// wiec trzeba poczekać
				q.add("SET Z MIN", true);		// SET Z zwraca początek operacji a nie koniec
				q.addWait( virtualComponents.SERVOZ_DOWN_TIME );	// wiec trzeba poczekać
			}
			q.add("DZ", true);
			ar.send(q);
			break;
		case R.id.losujx:
			Random generator2 = new Random( 19580427 );
			virtualComponents.moveZDown( q );
			q.add("Y" + virtualComponents.get("NEUTRAL_POS_Y", "0" ), true );
			long lengthx5	=  virtualComponents.getInt("LENGTHX", 600 );
		    for(int f = 0;f<20;){
		    	int left = generator2.nextInt((int)(lengthx5/100 / 2));
		    	int right =generator2.nextInt((int)(lengthx5/100 / 2));
		    	right+= lengthx5/100 / 2;
				q.add("X" + (left * 100), true );
				q.add("X" + (right * 100), true );
		        f=f+2;
		      }
		    ar.send(q);
			break;
		case R.id.losujy:
			Random generator3 = new Random( 19580427 );
			virtualComponents.moveZDown( q );
			long lengthy5	=  virtualComponents.getInt("LENGTHY", 600 );
		    for(int f = 0;f<20;){
		    	int left = generator3.nextInt((int)(lengthy5/100 / 2));
		    	int right =generator3.nextInt((int)(lengthy5/100));
		    	right+= lengthy5/100 / 2;
				q.add("Y" + (left * 100), true );
				q.add("Y" + (right * 100), true );
		        f=f+2;
		    }
		    ar.send(q);
			break;
		case R.id.fill5000:
			virtualComponents.nalej( 3000 );
			break;
		case R.id.set_bottle:
			// przełącz okno na listę butelek, 
			// zablokuj przyciski i po naciśnięciu ustaw w tym miejscu butelkę
			Constant.log(Constant.TAG,"wybierz butelkę...");
			String posx		=  virtualComponents.get("POSX", "0" );	
			String posy		=  virtualComponents.get("POSY", "0" );
			Constant.log(Constant.TAG,"wybierz butelkę3...");
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
			q.add("EY", true);
			q.add("EZ", true);		
			q.add("SET Z MAX", true);
			q.add("DX", true);
			q.add("DY", true);
			q.add("DZ", true);
			q.add("GET CARRET", true);
			ar.send(q);
			break;
		case R.id.min_z:
			q.add("EX", true);
			q.add("EY", true);
			q.add("EZ", true);	
			virtualComponents.moveZDown( q );
			q.add("DX", true);
			q.add("DY", true);
			q.add("DZ", true);
			q.add("GET CARRET", true);
			ar.send(q);
			break;

		case R.id.max_x:
			virtualComponents.moveZDown( q );
			long lengthx2	=  virtualComponents.getInt("LENGTHX", 600 );
			ar.send("X+" + lengthx2 );			
			break;

		case R.id.max_y:
			virtualComponents.moveZDown( q );
			long lengthy2	=  virtualComponents.getInt("LENGTHY", 600 );
			ar.send("Y+" + lengthy2 );			
			break;
		case R.id.min_x:
			virtualComponents.moveZDown( q );
			long lengthx3	=  virtualComponents.getInt("LENGTHY", 600 );
			ar.send("X-" + lengthx3 );
			break;
		case R.id.min_y:
			virtualComponents.moveZDown( q );
			long lengthy3	=  virtualComponents.getInt("LENGTHY", 600 );
			ar.send("Y-" + lengthy3 );
			break;	
		case R.id.unlock:
			ar.unlock();
			break;
		case R.id.pacpac:	
			virtualComponents.pacpac();
			break;
		case R.id.smile:
			BarobotMain.getInstance().cm.doPhoto();
			break;

		case R.id.bottle_next:
			break;	
		case R.id.bottle_prev:
			break;
		case R.id.goto_max_y:

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

			break;
		case R.id.goto_max_x:
			break;
		case R.id.goto_min_x:
			break;
		case R.id.goto_min_y:
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
		q.add("SET SPPEDX 400", true);
		q.add("SET SPPEDY 400", true);	
		q.add("SET SPPEDX 400", true);
		q.add("SET SPPEDY 400", true);
	*/	
	}
}
