package com.barobot.debug;

import java.util.Random;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.barobot.R;
import com.barobot.common.Initiator;
import com.barobot.common.constant.Constant;
import com.barobot.gui.database.BarobotDataStub;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.hardware.devices.i2c.Upanel;
import com.barobot.parser.Queue;
public class button_click implements OnClickListener{
	private Context dbw;
	public static boolean set_bottle_on = false;
	public button_click(Context debugWindow){
		dbw = debugWindow;
	}
	@Override
	public void onClick(final View v) {
		// get out of the UI thread
		Log.i("button click","click");
		new Thread( new Runnable(){
			@Override
			public void run() {
				Log.i("button click","exec start");
				exec(v);
				Log.i("button click","exec end");
			}}).start();
	}
	public void exec(View v) {
		Queue q			= new Queue();
		BarobotConnector barobot = Arduino.getInstance().barobot;
		Queue mq		= barobot.main_queue;
		int posx		= barobot.driver_x.getSPos();
		int posy		= barobot.state.getInt("POSY", 0 );

		Log.i("currentpos", ""+  posx);
		switch (v.getId()) {
		case R.id.set_x_1000:
	//		Log.i("nextpos-10000", "old: "+posx + " next: "+ ( posx -10000));
			barobot.moveZDown( q ,true );
			barobot.driver_x.moveTo( q, ( posx -10000));
			mq.add(q);
			break;
		case R.id.set_x_100:
	//		Log.i("nextpos-1000", "old: "+posx + " next: "+ ( posx -1000));
			barobot.moveZDown( q ,true );
			barobot.driver_x.moveTo( q, ( posx -1000));
			
			mq.add(q);
			break;
		case R.id.set_x_10:	
	//		Log.i("nextpos-100", "old: "+posx + " next: "+ ( posx -100)); 
			barobot.moveZDown( q ,true );
			barobot.driver_x.moveTo( q, ( posx -100));
			mq.add(q);
			break;
		case R.id.set_x10:

		//	Log.i("nextpos+100", "old: "+posx + " next: "+ ( posx +100)); 
			barobot.moveZDown( q ,true );
			barobot.driver_x.moveTo( q, ( posx +100));
			mq.add(q);
			break;
		case R.id.set_x100:
			
		//	Log.i("nextpos+1000", "old: "+posx + " next: "+ ( posx +1000));
			
			barobot.moveZDown( q ,true );
			barobot.driver_x.moveTo( q, ( posx +1000));
			mq.add(q);
			break;
		case R.id.set_x1000:
		//	Log.i("nextpos+10000", "old: "+posx + " next: "+ ( posx +10000));
			barobot.moveZDown( q ,true );
			barobot.driver_x.moveTo( q, ( posx +10000));
			mq.add(q);
			break;  
		case R.id.set_y_600:
			barobot.moveZDown( q ,true );
			barobot.moveY( q, ( posy -1000), true);
			mq.add(q);
			break;
		case R.id.set_y_100:
			barobot.moveZDown( q ,true );
			barobot.moveY( q, ( posy -100), true);
			mq.add(q);
			break;
		case R.id.set_y_10:
			barobot.moveZDown( q ,true );
			barobot.moveY( q, ( posy -10), true);
			mq.add(q);
			break;
		case R.id.set_y10:
			barobot.moveZDown( q ,true );
			barobot.moveY( q, ( posy +10), true);
			mq.add(q);
			break;
		case R.id.set_y100:
			barobot.moveZDown( q ,true );
			barobot.moveY( q, ( posy +100), true);
			mq.add(q);
			break;
		case R.id.set_y600:
			barobot.moveZDown( q ,true );
			barobot.moveY( q, ( posy +1000), true);
			mq.add(q);
			break;

		case R.id.set_neutral_y:
			barobot.state.set("NEUTRAL_POS_Y", ""+posy );
			String nn = barobot.state.get("NEUTRAL_POS_Y", "0" );
			Toast.makeText(dbw, "To jest pozycja bezpieczna ("+nn+")...", Toast.LENGTH_LONG).show();
			break;
		case R.id.goToNeutralY:
			barobot.moveY( q, barobot.state.get("NEUTRAL_POS_Y", "0" ));
			q.add("DY", true);
			mq.add(q);
			break;
		case R.id.kalibrujy:
			barobot.moveZDown( q ,true );
			barobot.moveY(q, 900, false );
			barobot.moveY(q, 2100, false );
			barobot.moveY(q, 900, false );
			mq.add(q);
			break;
		case R.id.kalibrujz:
			barobot.moveZDown( q ,true );
			mq.add(q);
			break;
		case R.id.machajx:
			barobot.moveZDown( q ,true );
			barobot.moveY( q, Constant.SERVOY_FRONT_POS, true);
			int lengthx4	=  barobot.state.getInt("LENGTHX", 600 );
			for( int i =0; i<10;i++){
			//	virtualComponents.moveX( q, (lengthx4/4) );
				//virtualComponents.moveX( q, (lengthx4/4 * 3) );
				barobot.driver_x.moveTo( q, 0);
				q.addWait(50);
				barobot.driver_x.moveTo( q, lengthx4);
			}
			q.add("DX", true);
			mq.add(q);
			break;
		case R.id.machajy:
			barobot.moveZDown( q ,true );
			for( int i =0; i<10;i++){
				barobot.moveY( q, Constant.SERVOY_FRONT_POS, false );
				barobot.moveY( q, Constant.SERVOY_BACK_POS, false );
			}
			barobot.moveY( q, Constant.SERVOY_FRONT_POS, false );
			q.add("DY", true);
			mq.add(q);
			break;
		case R.id.machajz:
			for( int i =0; i<10;i++){
				barobot.moveZDown(q, true );
				barobot.moveZUp(q,true);
			}
			barobot.moveZDown(q, true );
			q.add("DZ", true);
			mq.add(q);
			break;
		case R.id.losujx:
			Random generator2 = new Random( 19580427 );
			barobot.moveZDown( q, true  );
			barobot.moveY( q, Constant.SERVOY_FRONT_POS, true );
			int lengthx5	=  barobot.state.getInt("LENGTHX", 600 );
		    for(int f = 0;f<20;){
		    	int left = generator2.nextInt((int)(lengthx5/100 / 2));
		    	int right =generator2.nextInt((int)(lengthx5/100 / 2));
		    	right+= lengthx5/100 / 2;
				barobot.driver_x.moveTo( q, (left * 100));
				barobot.driver_x.moveTo( q, (right * 100) );
		        f=f+2;
		      }
		    mq.add(q);
			break;
		case R.id.losujy:
			Random generator3 = new Random( 19580427 );
			barobot.moveZDown( q ,true );

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
			mq.add(q);
			break;
		case R.id.fill5000:
			barobot.pour( q, -1, true );
			mq.add(q);
			break;
		case R.id.set_bottle:
			set_bottle_on  = true;
			// przełącz okno na listę butelek, 
			// zablokuj przyciski i po naciśnięciu ustaw w tym miejscu butelkę
			Initiator.logger.i(Constant.TAG,"wybierz butelkę...");
			Toast.makeText(dbw, "Wybierz butelkę do zapisania pozycji " + posx + "/" + posy, Toast.LENGTH_LONG).show();
//			dbw.tabHost.setCurrentTabByTag("tab0");
//			dbw.tabHost.bringToFront();
//			dbw.tabHost.setEnabled(true);

			break;
		case R.id.max_z:
			q.add("EX", true);
		//	q.add("EY", true);		
			barobot.moveZUp(q,true);
			q.add("DX", true);
			q.add("DY", true);
			q.add(Constant.GETXPOS, true);
			mq.add(q);
			break;
		case R.id.min_z:
			q.add("EX", true);
		//	q.add("EY", true);
			barobot.moveZDown( q ,true );
			q.add("DX", true);
			q.add("DY", true);
			q.add(Constant.GETXPOS, true);
			mq.add(q);
			break;

		case R.id.max_x:
			barobot.moveZDown( q ,true );
			int lengthx2	=  barobot.state.getInt("LENGTHX", 600 );
			barobot.driver_x.moveTo( q, posx +lengthx2);
		
			mq.add(q);
			break;

		case R.id.max_y:
			barobot.moveZDown( q ,true );
			barobot.moveY( q, Constant.SERVOY_BACK_POS, true );
			mq.add(q);	
			break;
		case R.id.min_x:
			barobot.moveZDown( q ,true );
			int lengthx3	=  barobot.state.getInt("LENGTHX", 600 );
			barobot.driver_x.moveTo( q, -lengthx3);

			mq.add(q);
			break;
		case R.id.min_y:
			barobot.moveZDown( q ,true );
			barobot.moveY( q, Constant.SERVOY_FRONT_POS, true );
			mq.add(q);
			break;	
		case R.id.unlock:
			mq.unlock();
			break;
		case R.id.pacpac:	
			Initiator.logger.i(Constant.TAG,"pac");
		//	q.add( moveX );
			q.add("EX", true);
//			q.add("EY", true);
//			q.add("EZ", true);
			q.add("Z" + Constant.SERVOZ_PAC_POS+","+Constant.DRIVER_Z_SPEED, true);
			Initiator.logger.i("pacpac","Z" + Constant.SERVOZ_PAC_POS+","+Constant.DRIVER_Z_SPEED);
			barobot.moveZDown(q, true );
			q.add("DY", true);
			q.add("DX", true);
			q.addWait(200);
			q.add("DZ", true);
			mq.add(q);
			break;
		case R.id.smile:
			q.add("Y" + Constant.SERVOY_FRONT_POS+ ","+Constant.DRIVER_Y_SPEED, true);
			q.addWait(5000);
			q.add("DY", true );
			mq.add(q);
			break;
		case R.id.kalibrujx:
			barobot.kalibrcja();
			break;
		case R.id.enabley:
	//		q.add("EY", true);
			mq.add(q);
			break;		
		case R.id.disablez:
			q.add("DZ", true);
			mq.add(q);
			break;		
		case R.id.disabley:
			q.add("DY", true);
			mq.add(q);
			break;	
		case R.id.reset1:	
			mq.add("RESET1", false );
			break;	
		case R.id.reset2:	
			mq.add("RESET2", true );
			break;	
		case R.id.reset3:	
			mq.add("RESET3", true );
			break;	
		case R.id.reset4:	
			mq.add("RESET4", true );
			break;	
		case R.id.goto_max_x:
			break;
		case R.id.goto_min_x:
			break;
		case R.id.find_bottles:
			barobot.kalibrcja();
			break;			
		case R.id.rb:
			mq.add("RB", true );
			break;		
		case R.id.rb2:
			mq.add("RB2", false );
			break;

		case R.id.scann_leds:
			barobot.scann_leds();
			break;
		case R.id.led_green_on:
			barobot.setLeds( "22", 255 );
			break;	
		case R.id.led_blue_on:
			barobot.setLeds( "44", 255 );
			break;
		case R.id.led_red_on:
			barobot.setLeds( "11", 255 );
			break;
		case R.id.reset_margin:
			barobot.driver_x.setM(0);
			barobot.state.set("MARGINX", 0);	
			barobot.driver_x.setHPos( 0 );
			break;	
		case R.id.reset_upanels:
			Queue q1		= new Queue();
			Upanel[] up		= barobot.i2c.getUpanels();
			for(int i =up.length-1; i>=0;i--){
				q1.add("RESET_NEXT"+ up[i].getAddress(), true);
			}
			Arduino.getMainQ().add(q1);
			break;
		case R.id.scann_i2c:
			mq.add("I", true );
			break;	
		case R.id.analog_temp:
			mq.add("T", true );
			mq.addWait(2000);
			mq.add("T", true );
			mq.addWait(2000);
			break;
		case R.id.clear_queue:
			mq.clear();
			break;
		case R.id.reset_serial:
			mq.clear();
			Arduino.getInstance().resetSerial();
			break;	
	   }
	}
}
