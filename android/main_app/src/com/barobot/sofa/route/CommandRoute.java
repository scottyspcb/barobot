package com.barobot.sofa.route;

import java.io.IOException;
import java.util.Random;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.Toast;

import com.barobot.AppInvoker;
import com.barobot.common.Initiator;
import com.barobot.common.constant.Constant;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.other.Android;
import com.barobot.other.Audio;
import com.barobot.other.InternetHelpers;
import com.barobot.other.OnDownloadReadyRunnable;
import com.barobot.other.update_drinks;
import com.barobot.parser.Queue;
import com.barobot.web.route.EmptyRoute;
import com.barobot.web.server.SofaServer;
import com.x5.template.Theme;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;

public class CommandRoute extends EmptyRoute {
	private String prefix;

	CommandRoute(){
		this.regex = "^/command/";
		this.prefix = "/command/";
		
	}

	@Override
	public String run(String url, SofaServer sofaServer, Theme theme,
			IHTTPSession session) {
		String url2 = url.replace(prefix, "");

		boolean ret = runCommand(url2);

		return ret ? "OK":"ERROR";
	}

	public static boolean runCommand( String command ){
		Queue q			= new Queue();
		BarobotConnector barobot = Arduino.getInstance().barobot;
		Queue mq		= barobot.main_queue;
		int posx		= barobot.driver_x.getSPos();
		int posy		= barobot.state.getInt("POSY", 0 );

		switch (command) {

		case "command_clear_queue":
			mq.clear();
			break;
		case "command_reset_serial":
			mq.clear();
			Arduino.getInstance().resetSerial();
			break;	

		case "command_set_x_1000":
	//		Log.i("nextpos-10000", "old: "+posx + " next: "+ ( posx -10000));
			barobot.moveZDown( q ,true );
			barobot.driver_x.moveTo( q, ( posx -10000));
			mq.add(q);
			break;
		case "command_set_x_100":
	//		Log.i("nextpos-1000", "old: "+posx + " next: "+ ( posx -1000));
			barobot.moveZDown( q ,true );
			barobot.driver_x.moveTo( q, ( posx -1000));

			mq.add(q);
			break;
		case "command_set_x_10":	
	//		Log.i("nextpos-100", "old: "+posx + " next: "+ ( posx -100)); 
			barobot.moveZDown( q ,true );
			barobot.driver_x.moveTo( q, ( posx -100));
			mq.add(q);
			break;
		case "command_set_x10":
		//	Log.i("nextpos+100", "old: "+posx + " next: "+ ( posx +100)); 
			barobot.moveZDown( q ,true );
			barobot.driver_x.moveTo( q, ( posx +100));
			mq.add(q);
			break;
		case "command_set_x100":
		//	Log.i("nextpos+1000", "old: "+posx + " next: "+ ( posx +1000));
			barobot.moveZDown( q ,true );
			barobot.driver_x.moveTo( q, ( posx +1000));
			mq.add(q);
			break;
		case "command_set_x1000":
		//	Log.i("nextpos+10000", "old: "+posx + " next: "+ ( posx +10000));
			barobot.moveZDown( q ,true );
			barobot.driver_x.moveTo( q, ( posx +10000));
			mq.add(q);
			break;  
		case "command_set_y_600":
			barobot.moveZDown( q ,true );
			barobot.moveY( q, ( posy -1000), true);
			mq.add(q);
			break;
		case "command_set_y_100":
			barobot.moveZDown( q ,true );
			barobot.moveY( q, ( posy -100), true);
			mq.add(q);
			break;
		case "command_set_y_10":
			barobot.moveZDown( q ,true );
			barobot.moveY( q, ( posy -10), true);
			mq.add(q);
			break;
		case "command_set_y10":
			barobot.moveZDown( q ,true );
			barobot.moveY( q, ( posy +10), true);
			mq.add(q);
			break;
		case "command_set_y100":
			barobot.moveZDown( q ,true );
			barobot.moveY( q, ( posy +100), true);
			mq.add(q);
			break;
		case "command_set_y600":
			barobot.moveZDown( q ,true );
			barobot.moveY( q, ( posy +1000), true);
			mq.add(q);
			break;
		case "command_download_database":
			//!!!
			break;
		case "command_set_neutral_y":
			barobot.state.set("NEUTRAL_POS_Y", ""+posy );
			String nn = barobot.state.get("NEUTRAL_POS_Y", "0" );
			break;
		case "command_goToNeutralY":
			barobot.moveY( q, barobot.state.get("NEUTRAL_POS_Y", "0" ));
			q.add("DY", true);
			mq.add(q);
			break;
		case "command_kalibrujy":
			barobot.moveZDown( q ,true );
			barobot.moveY(q, 900, false );
			barobot.moveY(q, 2100, false );
			barobot.moveY(q, 900, false );
			mq.add(q);
			break;
		case "command_kalibrujz":
			barobot.moveZDown( q ,true );
			mq.add(q);
			break;
		case "command_machajx":
			barobot.moveZDown( q ,true );
			
			int SERVOY_FRONT_POS = barobot.state.getInt("SERVOY_FRONT_POS", Constant.SERVOY_FRONT_POS );

			barobot.moveY( q, SERVOY_FRONT_POS, true);
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
		case "command_machajy":
			barobot.moveZDown( q ,true );
			
			int SERVOY_FRONT_POS2 = barobot.state.getInt("SERVOY_FRONT_POS", Constant.SERVOY_FRONT_POS );
			int SERVOY_BACK_POS = barobot.state.getInt("SERVOY_BACK_POS", Constant.SERVOY_BACK_POS );
			
			for( int i =0; i<10;i++){
				barobot.moveY( q, SERVOY_FRONT_POS2, false );
				barobot.moveY( q, SERVOY_BACK_POS, false );
			}
			barobot.moveY( q, SERVOY_FRONT_POS2, false );
			q.add("DY", true);
			mq.add(q);
			break;
		case "command_machajz":
			for( int i =0; i<10;i++){
				barobot.moveZDown(q, true );
				barobot.moveZUp(q,true);
			}
			barobot.moveZDown(q, true );
			q.add("DZ", true);
			mq.add(q);
			break;
		case "command_wznow":
			if(mq.isBusy()){
				mq.clear();
				mq.add("RESET2", "RRESET2");
				mq.add("RESET3", "RRESET3");
				mq.add("RESET4", "RRESET4");
				barobot.doHoming(mq, true);
			}
			break;
			
		case "command_i2c_test":
			mq.add("TEST", false);
			break;

		case "command_firmware_download":
			if(Android.createDirIfNotExists("Barobot")){
				InternetHelpers.doDownload(update_drinks.firmware, "firmware.hex", new OnDownloadReadyRunnable() {
				//	private String source;
					public void sendSource( String source ) {
				//		this.source = source;
						Initiator.logger.i("firmware_download","hex ready");
					}
				    @Override
					public void run() {
				    	// this.source
					}
				});
			}
			break;
		case "command_firmware_burn":
			break;
		case "command_losujx":
			Random generator2 = new Random( 19580427 );
			barobot.moveZDown( q, true  );
			int SERVOY_FRONT_POS3 = barobot.state.getInt("SERVOY_FRONT_POS", Constant.SERVOY_FRONT_POS );
			barobot.moveY( q, SERVOY_FRONT_POS3, true );
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
		case "command_losujy":
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
		case "command_fill5000":
			barobot.pour( q, -1, true );
			mq.add(q);
			break;

		case "command_max_z":
			q.add("EX", true);
		//	q.add("EY", true);		
			barobot.moveZUp(q,true);
			q.add("DX", true);
			q.add("DY", true);
			q.add(Constant.GETXPOS, true);
			mq.add(q);
			break;
		case "command_min_z":
			q.add("EX", true);
		//	q.add("EY", true);
			barobot.moveZDown( q ,true );
			q.add("DX", true);
			q.add("DY", true);
			q.add(Constant.GETXPOS, true);
			mq.add(q);
			break;
		case "command_max_x":
			barobot.moveZDown( q ,true );
			int lengthx2	=  barobot.state.getInt("LENGTHX", 600 );
			barobot.driver_x.moveTo( q, posx +lengthx2);
			mq.add(q);
			break;
		case "command_max_y":
			barobot.moveZDown( q ,true );
			int SERVOY_BACK_POS2 = barobot.state.getInt("SERVOY_BACK_POS", Constant.SERVOY_BACK_POS );
			barobot.moveY( q, SERVOY_BACK_POS2, true );
			mq.add(q);	
			break;
		case "command_min_x":
			barobot.moveZDown( q ,true );
			int lengthx3	=  barobot.state.getInt("LENGTHX", 600 );
			barobot.driver_x.moveTo( q, -lengthx3);
			mq.add(q);
			break;
		case "command_min_y":
			barobot.moveZDown( q ,true );
			int SERVOY_FRONT_POS5 = barobot.state.getInt("SERVOY_FRONT_POS", Constant.SERVOY_FRONT_POS );
			barobot.moveY( q, SERVOY_FRONT_POS5, true );
			mq.add(q);
			break;	
		case "command_unlock":
			mq.unlock();
			break;
		case "command_pacpac":	
			Initiator.logger.i(Constant.TAG,"pac");
		//	q.add( moveX );
			q.add("EX", true);
//			q.add("EY", true);
//			q.add("EZ", true);
			int SERVOZ_PAC_POS = barobot.state.getInt("SERVOZ_PAC_POS", Constant.SERVOZ_PAC_POS );
			int DRIVER_Z_SPEED = barobot.state.getInt("DRIVER_Z_SPEED", Constant.DRIVER_Z_SPEED );
			q.add("Z" + SERVOZ_PAC_POS+","+DRIVER_Z_SPEED, true);
			barobot.moveZDown(q, true );
			q.add("DY", true);
			q.add("DX", true);
			q.addWait(200);
			q.add("DZ", true);
			mq.add(q);
			break;

		case "command_enabley":
	//		q.add("EY", true);
			mq.add(q);
			break;	
		case "command_disablez":
			q.add("DZ", true);
			mq.add(q);
			break;		
		case "command_disabley":
			q.add("DY", true);
			mq.add(q);
			break;
		case "command_enablez":
			q.add("EZ", true);
			mq.add(q);
			break;		
			
			
			
			
			
			
		case "command_reset1":	
			mq.add("RESET1", false );
			break;	
		case "command_reset2":	
			mq.add("RESET2", true );
			break;	
		case "command_reset3":	
			mq.add("RESET3", true );
			break;	
		case "command_reset4":	
			mq.add("RESET4", true );
			break;	
		case "command_goto_max_x":
			break;
		case "command_goto_min_x":
			break;

		case "command_rb":
			mq.add("RB", true );
			break;		
		case "command_rb2":
			mq.add("RB2", false );
			break;
		case "command_scann_leds":
			barobot.scann_leds();
			break;
		case "command_led_green_on":
			barobot.setLeds( "22", 255 );
			break;	
		case "command_led_blue_on":
			barobot.setLeds( "44", 255 );
			break;
		case "command_led_red_on":
			barobot.setLeds( "11", 255 );
			break;
		case "command_reset_margin":
			barobot.driver_x.setM(0);
			barobot.state.set("MARGINX", 0);	
			barobot.driver_x.setHPos( 0 );
			break;	
		case "command_scann_i2c":
			mq.add("I", true );
			break;
		case "command_analog_temp":
			mq.add("T", true );
			mq.addWait(2000);
			mq.add("T", true );
			mq.addWait(2000);
			break;

		case "command_move_to_start":
			barobot.moveToStart();
			break;	
			
		case "command_homing":
			barobot.doHoming(mq, true);
			break;

		case "command_options_lights_off":
			final Audio a = getAudio();
			if(a.isRunning()){
				System.out.println("getAudio stop1");
				a.stop();
			} else {
				System.out.println("getAudio start");
				a.start(barobot);
				barobot.main_queue.clear();
				barobot.main_queue.unlock();
			}
			break;

		case "command_options_lights_on":
			final Audio a4 = getAudio();
			if(!a4.isRunning()){
				System.out.println("getAudio start");
				a4.start(barobot);
			}
			break;
		case "command_find_bottles":
			barobot.kalibrcja();
			break;

		case "command_demo":
			barobot.startDemo();
			break;
	   }
		return false;
	}
	private static Audio getAudio() {
		Audio a = (Audio) AppInvoker.container.get("Audio");
    	if(a == null ){
    		a = new Audio();
    		AppInvoker.container.put("Audio", a );
    	}
    	return a;
	}

	public static String[] geCommands(){
		return new String[]{
				"command_clear_queue",
				"command_reset_serial",
				"command_set_x_1000",
				"command_set_x_100",
				"command_set_x_10",
				"command_set_x10",
				"command_set_x100",
				"command_set_x1000",
				"command_set_y_600",
				"command_set_y_100",
				"command_set_y_10",
				"command_set_y10",
				"command_set_y100",
				"command_set_y600",
				"command_download_database",
				"command_set_neutral_y",
				"command_goToNeutralY",
				"command_kalibrujy",
				"command_kalibrujz",
				"command_machajx",
				"command_machajy",
				"command_machajz",
				"command_wznow",
				"command_i2c_test",
				"command_firmware_download",
				"command_firmware_burn",
				"command_losujx",
				"command_losujy",
				"command_fill5000",
				"command_max_z",
				"command_min_z",
				"command_max_x",
				"command_max_y",
				"command_min_x",
				"command_min_y",
				"command_unlock",
				"command_pacpac",
				"command_enabley",
				"command_disablez",
				"command_disabley",
				"command_enablez",
				"command_reset1",
				"command_reset2",
				"command_reset3",
				"command_reset4",
				"command_goto_max_x",
				"command_goto_min_x",
				"command_rb",
				"command_rb2",
				"command_scann_leds",
				"command_led_green_on",
				"command_led_blue_on",
				"command_led_red_on",
				"command_reset_margin",
				"command_scann_i2c",
				"command_analog_temp",
				"command_move_to_start",
				"command_homing",
				"command_options_lights_off",
				"command_options_lights_on",
				"command_find_bottles",
				"command_demo"
	   } ;
	}
}
