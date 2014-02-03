package com.barobot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import com.barobot.activity.BarobotMain;
import com.barobot.constant.Constant;
import com.barobot.hardware.DeviceSet;
import com.barobot.hardware.I2C;
import com.barobot.hardware.virtualComponents;
import com.barobot.utils.Arduino;
import com.barobot.utils.CameraManager;
import com.barobot.utils.interval;
import com.barobot.web.server.SofaServer;

public class AppInvoker {
    private static AppInvoker ins;
	private BarobotMain main;
	public CameraManager cm;
    public ArrayList<interval> inters = new ArrayList<interval>();

	public void onStart() {	
    }
	public void onCreate() {
		SofaServer ss = SofaServer.getInstance();
		ss.setBaseContext(main.getBaseContext());
        try {
			ss.start();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		I2C.init();

		cm = new CameraManager( main );
		cm.findCameras();
		virtualComponents.init( main );

	    DeviceSet.loadXML(main, R.raw.devices);
	    Arduino.getInstance().onStart( main );	
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
		Constant.log("MAINWINDOW", "onPause");
		cm.onPause();
	}
	public void onResume() {
		Constant.log("MAINWINDOW", "onResume");     
		if(cm!=null){
			cm.onResume();
		}
        Arduino.getInstance().resume();
	}
	public void onDestroy() {
		Constant.log("MAINWINDOW", "onDestroy");
    	SofaServer.getInstance().stop();
    	Arduino.getInstance().destroy();
    	DeviceSet.clear();
    	Iterator<interval> it = this.inters.iterator();
    	while(it.hasNext()){
    		it.next().cancel();
    	}
		I2C.destroy();
        cm.onDestroy();	
	}
}
