package com.barobot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.widget.Button;

import com.barobot.activity.BarobotMain;
import com.barobot.common.Initiator;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;
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
	public static Map<String, Object> container =  new HashMap<String, Object>();

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
			Initiator.logger.appendError(e);
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

	public void onConnected() {
		BarobotConnector barobot = Arduino.getInstance().barobot;
		if(!Arduino.getInstance().barobot.ledsReady){
			Arduino.getInstance().barobot.scann_leds();
		}
		barobot.doHoming( barobot.main_queue, false );

		main.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Button xb1 = (Button) main.findViewById(R.id.choose_pour_button);
				if(xb1!=null){
					xb1.setEnabled(true);
				}
				Button xb2 = (Button) main.findViewById(R.id.creator_pour_button);
				if(xb2!=null){
					xb2.setEnabled(true);
				}
			}
		});
		
		
	}
	public void onDisconnect() {
	}
}
