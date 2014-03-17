package com.barobot;

import java.util.ArrayList;
import java.util.Iterator;

import android.util.Log;

import com.barobot.activity.BarobotMain;
import com.barobot.common.DesktopLogger;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.virtualComponents;
import com.barobot.hardware.serial.AndroidLogger;
import com.barobot.other.CameraManager;
import com.barobot.other.I2C;
import com.barobot.parser.utils.Interval;

public class AppInvoker {
    private static AppInvoker ins;
	private BarobotMain main;
	public CameraManager cm;
    public ArrayList<Interval> inters = new ArrayList<Interval>();

	public void onStart() {	
    }
	public void onCreate() {
		/*
		SofaServer ss = SofaServer.getInstance();
		ss.setBaseContext(main.getBaseContext());
        try {
			ss.start();
		} catch (IOException e1) {
			e1.printStackTrace();
		}*/
	//	I2C.init();

		cm = new CameraManager( main );
		cm.findCameras();
		virtualComponents.init( main );
	    Arduino.getInstance().onStart( main );

	    AndroidLogger dl = new AndroidLogger();
		com.barobot.common.Initiator.setLogger( dl );
	}
	public static AppInvoker createInstance(BarobotMain barobotMain) {
		ins = new AppInvoker();
		ins.main = barobotMain;
		return ins;
	}
	public static AppInvoker getInstance() {
		return ins;
	}
	public void onPause() {
		AppInvoker.log("MAINWINDOW", "onPause");
		cm.onPause();
	}
	public void onResume() {
		AppInvoker.log("MAINWINDOW", "onResume");     
		if(cm!=null){
			cm.onResume();
		}
        Arduino.getInstance().resume();
	}
	public void onDestroy() {
		AppInvoker.log("MAINWINDOW", "onDestroy");
    //	SofaServer.getInstance().stop();
    	Arduino.getInstance().destroy();
    	Iterator<Interval> it = this.inters.iterator();
    	while(it.hasNext()){
    		it.next().cancel();
    	}
	//	I2C.destroy();
        cm.onDestroy();	
	}
	public static void log(String tag4, String string) {
		Log.w(tag4,string);
	}
}
