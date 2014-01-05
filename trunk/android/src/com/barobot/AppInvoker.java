package com.barobot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import com.barobot.hardware.DeviceSet;
import com.barobot.utils.Arduino;
import com.barobot.utils.CameraManager;
import com.barobot.utils.Constant;
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

		cm = new CameraManager( main );
		cm.findCameras();

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
        cm.onDestroy();	
	}


}
