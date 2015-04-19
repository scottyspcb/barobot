package com.barobot;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;

import com.barobot.android.Android;
import com.barobot.android.AndroidHelpers;
import com.barobot.common.Initiator;
import com.barobot.common.constant.Constant;
import com.barobot.common.interfaces.HardwareState;
import com.barobot.gui.dataobjects.Engine;
import com.barobot.gui.dataobjects.Log_start;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.hardware.serial.AndroidLogger;
import com.barobot.other.CameraManager;
import com.barobot.other.StartupException;
import com.barobot.parser.utils.Decoder;
import com.barobot.parser.utils.Interval;
import com.barobot.sofa.route.SofaRouter;
import com.barobot.web.server.SofaServer;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;

public class AppInvoker {
    private static AppInvoker ins;
	private Activity main;
	public CameraManager cm;
    public ArrayList<Interval> inters = new ArrayList<Interval>();
	private static Arduino arduino;
	private SofaRouter sr = null;
//	public static Map<String, Object> container =  new HashMap<String, Object>();
	private SofaServer ss =null;
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
						sr = new SofaRouter();
						ss = SofaServer.getInstance();
						ss.setBaseContext(main.getBaseContext());
						ss.addRouter( sr );
						ss.start();
					} catch (IOException e) {
						Initiator.logger.appendError(e);
					}
			    }
			};
			arduino			= new Arduino( main );
			arduino.connect();

			final BarobotConnector barobot = arduino.barobot;
			if(barobot.state.getInt("SSERVER", 0) > 0 ){
				handler.postDelayed(r, 1000);
			}
			resetLanguage( main, "en_US" );
			doOnlyOnce(barobot.state, main);
			Engine.createInstance(main);
			if(barobot.getRobotId() > 0 ){
				Android.createRobot( -1, barobot.getRobotId() );
			}

			String lang = arduino.barobot.state.get("LANG", "pl" );
			Initiator.logger.w("set new lang", lang);
			BarobotMain.getInstance().changeLanguage(lang);

			Log_start ls 			= new Log_start();
			ls.datetime				= Decoder.getTimestamp() ;
			ls.start_type			= "app";
			ls.robot_id				= barobot.getRobotId();
			ls.language				= barobot.state.get("LANG", "pl" );
			ls.app_starts			= barobot.state.getInt("STAT1", 0);
			ls.arduino_starts		= barobot.state.getInt("ARDUINO_STARTS", 0);
			ls.serial_starts		= barobot.state.getInt("STAT2", 0);
			ls.app_version			= Constant.ANDROID_APP_VERSION;
			ls.arduino_version		= barobot.state.getInt("ARDUINO_VERSION", 0);
	//		ls.database_version		= Constant.ANDROID_APP_VERSION;
			ls.temp_start			= barobot.getLastTemp();
			ls.insert();

			Interval watchdog = new Interval(new Runnable() {
				@Override
				public void run() {
					if(barobot.init_done){
						if(Decoder.getTimestamp()- barobot.lastSeenRobotTimestamp > Constant.TIMEOUT_WITHOUT_ROBOT ){
							Initiator.logger.i("MAINWINDOW", "timeout without barobot");
							barobot.init_done = false;
							AndroidHelpers.askForClosingApp( barobot );
						}
					}
				}
			});
			inters.add(watchdog);
			
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
			if( ss != null){
				ss.stop();
			}
			arduino.destroy();
	    	Iterator<Interval> it = this.inters.iterator();
	    	while(it.hasNext()){
	    		it.next().cancel();
	    	}
	    	this.inters.clear();
	        cm.onDestroy();	
	//	}
	}
	private void doOnlyOnce(HardwareState state, Activity main2) throws StartupException {
		// once and never again
		if(state.getInt("INIT", 0 ) < Constant.ANDROID_APP_VERSION ){
			File dir = new File(Environment.getExternalStorageDirectory(), "Barobot");
			if (!dir.exists()) {
				Android.createDirIfNotExists("Barobot");
			}
			Android.createShortcutOnDesktop(main);			
			// change language to en_US
			resetLanguage( main2, "en_US" );
		}
		
		
		// once per version
		if(state.getInt("INIT", 0 ) < Constant.ANDROID_APP_VERSION ){
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
		//	Settings.System.putString(ctx.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, "-1");
		}
	}
	private void resetLanguage(Activity actv, String string) {		// Change locale settings in the app.

		Locale locale = new Locale("en_US");
		Locale.setDefault(locale);
		Configuration config = new Configuration();
		config.locale = locale;
		Context ctx = main.getApplicationContext();
		ctx.getResources().updateConfiguration(config, null);

	
		Resources res = actv.getResources();
		DisplayMetrics dm = res.getDisplayMetrics();
		android.content.res.Configuration conf = res.getConfiguration();
		conf.locale = new Locale(string);
		res.updateConfiguration(conf, dm);

		actv.getBaseContext().getResources().updateConfiguration(config, actv.getBaseContext().getResources().getDisplayMetrics());
	
		
		actv.onConfigurationChanged(conf);
		
		String ll = res.getConfiguration().locale.getDisplayName();
		Initiator.logger.w("AppInvoker.resetLanguage", "new lang: "+ ll);
	}

	public void onDisconnect() {
		Android.alertMessage( main, "Barobot has been disconnected.");
	}
}
