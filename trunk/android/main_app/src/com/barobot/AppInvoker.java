package com.barobot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import com.barobot.activity.BarobotMain;
import com.barobot.common.Initiator;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.virtualComponents;
import com.barobot.hardware.serial.AndroidLogger;
import com.barobot.other.CameraManager;
import com.barobot.parser.utils.Interval;
import com.barobot.sofa.route.SofaRouter;
import com.barobot.web.server.SofaServer;

public class AppInvoker {
    private static AppInvoker ins;
	private BarobotMain main;
	public CameraManager cm;
    public ArrayList<Interval> inters = new ArrayList<Interval>();
	private static Arduino arduino;

	SofaServer ss =null; 
			
	public void onCreate() {
	//	I2C.init();
	    AndroidLogger dl = new AndroidLogger();
		com.barobot.common.Initiator.setLogger( dl );
		cm = new CameraManager( main );
		try {
			SofaRouter sr = new SofaRouter();
			ss = SofaServer.getInstance();
			ss.setBaseContext(main.getBaseContext());
			ss.addRouter( sr );
			ss.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	//	cm.findCameras();
		arduino			= new Arduino( main );
		arduino.onStart(main);
		
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
		Initiator.logger.i("MAINWINDOW", "onPause");
		cm.onPause();
	//	arduino.onPause();
	}
	public void onResume() {
		Initiator.logger.i("MAINWINDOW", "onResume");     
		if(cm!=null){
			cm.onResume();
		}
		arduino.resume();
	}
	public void onDestroy() {
		Initiator.logger.i("MAINWINDOW", "onDestroy");
    	ss.stop();
		arduino.destroy();
    	Iterator<Interval> it = this.inters.iterator();
    	while(it.hasNext()){
    		it.next().cancel();
    	}
	//	I2C.destroy();
        cm.onDestroy();	
	}
}
