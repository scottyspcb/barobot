package com.barobot;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.barobot.common.Initiator;
import com.barobot.common.constant.Constant;
import com.barobot.common.interfaces.HardwareState;
import com.barobot.gui.dataobjects.Engine;
import com.barobot.gui.dataobjects.Log_start;
import com.barobot.gui.dataobjects.StartupException;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.hardware.serial.AndroidLogger;
import com.barobot.other.Android;
import com.barobot.other.CameraManager;
import com.barobot.parser.Queue;
import com.barobot.parser.utils.Interval;
import com.barobot.sofa.route.SofaRouter;
import com.barobot.web.server.SofaServer;

public class AppInvoker {
    private static AppInvoker ins;
	private Activity main;
	public CameraManager cm;
    public ArrayList<Interval> inters = new ArrayList<Interval>();
	private static Arduino arduino;
	public static Map<String, Object> container =  new HashMap<String, Object>();

	SofaServer ss =null; 
	static boolean isCreated = false;

	public static AppInvoker getInstance() {
		return ins;
	}

	public void onCreate() throws StartupException {
		if(!isCreated){
			isCreated = true;
		    AndroidLogger dl = new AndroidLogger();
			com.barobot.common.Initiator.setLogger( dl );
			cm = new CameraManager( main );

			Handler handler = new Handler();
			final Runnable r = new Runnable()
			{
			    public void run() 
			    {
			    	try {
						SofaRouter sr = new SofaRouter();
						ss = SofaServer.getInstance();
						ss.setBaseContext(main.getBaseContext());
						ss.addRouter( sr );
						ss.start();
					} catch (IOException e) {
						Initiator.logger.appendError(e);
					}
			    }
			};

			handler.postDelayed(r, 1000);
			arduino			= new Arduino( main );
			arduino.onStart(main);

			doOnlyOnce(arduino.barobot.state);
			Engine.createInstance(main);

			BarobotConnector barobot = arduino.barobot;
			if(barobot.getRobotId() > 0 ){			// check exists in db
				Android.createRobot( -1, barobot.getRobotId() );
			}

			String lang = arduino.barobot.state.get("LANG", "pl" );
			Initiator.logger.w("set new lang", lang);
			BarobotMain.getInstance().changeLanguage(lang);

			Log_start ls 			= new Log_start();
			ls.datetime				= Android.getTimestamp() ;
			ls.start_type			= "app";
			ls.robot_id				= barobot.getRobotId();
			ls.language				= barobot.state.get("LANG", "pl" );
			ls.app_starts			= barobot.state.getInt("STAT1", 0);
			ls.arduino_starts		= barobot.state.getInt("ARDUINO_STARTS", 0);
			ls.serial_starts		= barobot.state.getInt("STAT2", 0);
			ls.app_version			= Constant.ANDROID_APP_VERSION;
	//		ls.arduino_version		= Constant.ANDROID_APP_VERSION;
	//		ls.database_version		= Constant.ANDROID_APP_VERSION;
			ls.temp_start			= barobot.getLastTemp();
			ls.insert();
		}
	}
	private int activities = 0;
	public void onStart() {
		activities++;
		Initiator.logger.i("MAINWINDOW", "onStart: " + activities);
	}
	public static AppInvoker createInstance(Activity barobotActivity) {
		if(ins == null){
			ins = new AppInvoker();
		}
		ins.main = barobotActivity;
		return ins;
	}
	public void onResume() {
		Initiator.logger.i("MAINWINDOW", "onResume");     
		arduino.resume();
	}
	public void onDestroy() {
		activities--;
	//	Initiator.logger.i("MAINWINDOW", "onDestroy: " + activities);
	//	if(activities == 0){
			Initiator.logger.i("MAINWINDOW", "onDestroy");
	    	ss.stop();
			arduino.destroy();
	    	Iterator<Interval> it = this.inters.iterator();
	    	while(it.hasNext()){
	    		it.next().cancel();
	    	}
	    	this.inters.clear();
	        cm.onDestroy();	
	//	}
	}
	private void doOnlyOnce(HardwareState state) throws StartupException {
		Initiator.logger.i("AppInvoker.doOnlyOnce", "value:" +state.getInt("INIT", 0 ));
		if(state.getInt("INIT", 0 ) < Constant.ANDROID_APP_VERSION ){
			File dir = new File(Environment.getExternalStorageDirectory(), "Barobot");
			if (!dir.exists()) {
				Android.createDirIfNotExists("Barobot");
			}
			try {
				String appPath2 	= main.getPackageManager().getPackageInfo(main.getPackageName(), 0).applicationInfo.dataDir;
				String dbFolderPath = appPath2+"/databases";
				Initiator.logger.i("Engine.app path", appPath2 );	// /data/data/com.barobot/databases/
				File file = new File(dbFolderPath);
				if (!file.exists()) {
					if (!file.mkdirs()) {
						Log.e("TravellerLog :: ", "Problem creating folder:" + file.getAbsolutePath());
						throw new StartupException( "mkdirs Error" );
					}
				}
				state.set("INIT", Constant.ANDROID_APP_VERSION );
			} catch (NameNotFoundException e1) {
				Initiator.logger.w("AppInvoker.doOnlyOnce", "NameNotFoundException", e1);
				throw new StartupException( "AppInvoker.doOnlyOnce", e1 );
			}
		}
	}

	public void onConnected(Queue mq) {
		BarobotConnector barobot = Arduino.getInstance().barobot;
		mq.add( "\n", false );	// clean up input
		mq.add( "\n", false );
//		mq.unlock();
		barobot.disabley( mq );
		barobot.disablez(mq);
		mq.add("DX", true);
		mq.add(Constant.GETXPOS, true);
		mq.add(Constant.GETYPOS, true);
		mq.add(Constant.GETZPOS, true);

		if(barobot.ledsReady){
			if(barobot.newLeds){
				barobot.setAllLeds(mq, "22", 0, 255, 0, 255);
			}
		}else{
			barobot.scann_leds( mq );
		}
		barobot.readHardwareRobotId(mq);
		barobot.doHoming( mq, false );

		if(barobot.newLeds){
			barobot.setAllLeds(mq, "22", 255, 255, 204, 221 );
		}
	}
	public void onDisconnect() {
	}

}
