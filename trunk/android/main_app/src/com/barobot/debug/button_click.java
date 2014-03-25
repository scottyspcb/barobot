package com.barobot.debug;

import java.util.Random;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.barobot.AppInvoker;
import com.barobot.R;
import com.barobot.common.Initiator;
import com.barobot.common.constant.Constant;
import com.barobot.gui.database.BarobotDataStub;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.virtualComponents;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.parser.Queue;
public class button_click implements OnClickListener{
	private Context dbw;
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
		BarobotConnector barobot = virtualComponents.barobot;
		Queue mq		= barobot.main_queue;
		int posx		= barobot.driver_x.getSPos();;
		int posy		= virtualComponents.state.getInt("POSY", 0 );

		Log.i("currentpos", ""+  posx);
		switch (v.getId()) {
		case R.id.set_x_1000:
	//		Log.i("nextpos-10000", "old: "+posx + " next: "+ ( posx -10000));
			virtualComponents.moveZDown( q ,true );

			barobot.driver_x.moveTo( q, ( posx -10000));
			
			
			mq.add(q);
			break;
		case R.id.set_x_100:
	//		Log.i("nextpos-1000", "old: "+posx + " next: "+ ( posx -1000));
			virtualComponents.moveZDown( q ,true );
			barobot.driver_x.moveTo( q, ( posx -1000));
			
			mq.add(q);
			break;
		case R.id.set_x_10:	
	//		Log.i("nextpos-100", "old: "+posx + " next: "+ ( posx -100)); 
			virtualComponents.moveZDown( q ,true );
			barobot.driver_x.moveTo( q, ( posx -100));
			mq.add(q);
			break;
		case R.id.set_x10:

		//	Log.i("nextpos+100", "old: "+posx + " next: "+ ( posx +100)); 
			virtualComponents.moveZDown( q ,true );
			barobot.driver_x.moveTo( q, ( posx +100));
			mq.add(q);
			break;
		case R.id.set_x100:
			
		//	Log.i("nextpos+1000", "old: "+posx + " next: "+ ( posx +1000));
			
			virtualComponents.moveZDown( q ,true );
			barobot.driver_x.moveTo( q, ( posx +1000));
			mq.add(q);
			break;
		case R.id.set_x1000:
		//	Log.i("nextpos+10000", "old: "+posx + " next: "+ ( posx +10000));
			virtualComponents.moveZDown( q ,true );
			barobot.driver_x.moveTo( q, ( posx +10000));
			mq.add(q);
			break;  
		case R.id.set_y_600:
			virtualComponents.moveZDown( q ,true );
			virtualComponents.moveY( q, ( posy -1000), true);
			mq.add(q);
			break;
		case R.id.set_y_100:
			virtualComponents.moveZDown( q ,true );
			virtualComponents.moveY( q, ( posy -100), true);
			mq.add(q);
			break;
		case R.id.set_y_10:
			virtualComponents.moveZDown( q ,true );
			virtualComponents.moveY( q, ( posy -10), true);
			mq.add(q);
			break;
		case R.id.set_y10:
			virtualComponents.moveZDown( q ,true );
			virtualComponents.moveY( q, ( posy +10), true);
			mq.add(q);
			break;
		case R.id.set_y100:
			virtualComponents.moveZDown( q ,true );
			virtualComponents.moveY( q, ( posy +100), true);
			mq.add(q);
			break;
		case R.id.set_y600:
			virtualComponents.moveZDown( q ,true );
			virtualComponents.moveY( q, ( posy +1000), true);
			mq.add(q);
			break;

		case R.id.set_neutral_y:
			virtualComponents.state.set("NEUTRAL_POS_Y", ""+posy );
			String nn = virtualComponents.state.get("NEUTRAL_POS_Y", "0" );
			Toast.makeText(dbw, "To jest pozycja bezpieczna ("+nn+")...", Toast.LENGTH_LONG).show();
			break;
		case R.id.goToNeutralY:
			virtualComponents.moveY( q, virtualComponents.state.get("NEUTRAL_POS_Y", "0" ));
			q.add("DY", true);
			mq.add(q);
			break;
		case R.id.kalibrujy:
			virtualComponents.moveZDown( q ,true );
			virtualComponents.moveY(q, 900, false );
			virtualComponents.moveY(q, 2100, false );
			virtualComponents.moveY(q, 900, false );
			mq.add(q);
			break;
		case R.id.kalibrujz:
			virtualComponents.moveZDown( q ,true );
			mq.add(q);
			break;
		case R.id.machajx:
			virtualComponents.moveZDown( q ,true );
			virtualComponents.moveY( q, BarobotConnector.SERVOY_FRONT_POS, true);
			int lengthx4	=  virtualComponents.state.getInt("LENGTHX", 600 );
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
			virtualComponents.moveZDown( q ,true );
			for( int i =0; i<10;i++){
				virtualComponents.moveY( q, BarobotConnector.SERVOY_FRONT_POS, false );
				virtualComponents.moveY( q, BarobotConnector.SERVOY_BACK_POS, false );
			}
			virtualComponents.moveY( q, BarobotConnector.SERVOY_FRONT_POS, false );
			q.add("DY", true);
			mq.add(q);
			break;
		case R.id.machajz:
			for( int i =0; i<10;i++){
				virtualComponents.moveZDown(q, true );
				virtualComponents.moveZUp(q,true);
			}
			virtualComponents.moveZDown(q, true );
			q.add("DZ", true);
			mq.add(q);
			break;
		case R.id.losujx:
			Random generator2 = new Random( 19580427 );
			virtualComponents.moveZDown( q, true  );
			virtualComponents.moveY( q, BarobotConnector.SERVOY_FRONT_POS, true );
			int lengthx5	=  virtualComponents.state.getInt("LENGTHX", 600 );
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
			virtualComponents.moveZDown( q ,true );

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
			virtualComponents.nalej( -1 );
			break;
		case R.id.set_bottle:
			virtualComponents.set_bottle_on = true;
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
			virtualComponents.moveZUp(q,true);
			q.add("DX", true);
			q.add("DY", true);
			q.add(Constant.GETXPOS, true);
			mq.add(q);
			break;
		case R.id.min_z:
			q.add("EX", true);
		//	q.add("EY", true);
			virtualComponents.moveZDown( q ,true );
			q.add("DX", true);
			q.add("DY", true);
			q.add(Constant.GETXPOS, true);
			mq.add(q);
			break;

		case R.id.max_x:
			virtualComponents.moveZDown( q ,true );
			int lengthx2	=  virtualComponents.state.getInt("LENGTHX", 600 );
			barobot.driver_x.moveTo( q, posx +lengthx2);
		
			mq.add(q);
			break;

		case R.id.max_y:
			virtualComponents.moveZDown( q ,true );
			virtualComponents.moveY( q, BarobotConnector.SERVOY_BACK_POS, true );
			mq.add(q);	
			break;
		case R.id.min_x:
			virtualComponents.moveZDown( q ,true );
			int lengthx3	=  virtualComponents.state.getInt("LENGTHX", 600 );

			barobot.driver_x.moveTo( q, -lengthx3);

			mq.add(q);
			break;
		case R.id.min_y:
			virtualComponents.moveZDown( q ,true );
			virtualComponents.moveY( q, BarobotConnector.SERVOY_FRONT_POS, true );
			mq.add(q);
			break;	
		case R.id.unlock:
			mq.unlock();
			break;
		case R.id.pacpac:	
			virtualComponents.pacpac();
			break;
		case R.id.smile:
			BarobotDataStub.SetupDatabase();
			break;
		case R.id.kalibrujx:
			virtualComponents.kalibrcja();
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
			virtualComponents.kalibrcja();
			break;			
		case R.id.rb:
			mq.add("RB", true );
			break;		
		case R.id.rb2:
			mq.add("RB2", false );
			break;

		case R.id.scann_leds:
			virtualComponents.scann_leds();
			break;
	
		case R.id.led_green_on:
			if(!virtualComponents.ledsReady){
				virtualComponents.scann_leds();
			}
			virtualComponents.setLeds( "22", 200 );
			break;	
		case R.id.led_blue_on:
			if(!virtualComponents.ledsReady){
				virtualComponents.scann_leds();
			}
			virtualComponents.setLeds( "44", 200 );
			
			break;
		case R.id.led_red_on:
			if(!virtualComponents.ledsReady){
				virtualComponents.scann_leds();
			}
			virtualComponents.setLeds( "11", 200 );
			break;
		case R.id.reset_margin:
			barobot.driver_x.setM(0);
			
			virtualComponents.state.set("MARGINX", 0);
			
			
			int spos = barobot.driver_x.hard2soft( 0 );
			barobot.driver_x.setSPos( spos );
			break;	
		case R.id.reset_upanels:
			Queue q1			= new Queue();
			for(int i =BarobotConnector.upanels.length-1; i>=0;i--){
				q1.add("RESET_NEXT"+ BarobotConnector.upanels[i], true);
			}
			virtualComponents.getMainQ().add(q1);
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
