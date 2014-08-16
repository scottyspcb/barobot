package com.barobot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.widget.Button;

import com.barobot.activity.RecipeListActivity;
import com.barobot.common.Initiator;
import com.barobot.gui.dataobjects.Engine;
import com.barobot.gui.dataobjects.Slot;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.hardware.serial.AndroidLogger;
import com.barobot.other.CameraManager;
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

	public void onCreate() {
		if(!isCreated){
			isCreated = true;
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
			Engine.createInstance(main);
		}
	}
	
	private int activities = 0;
	public void onStart() {
		// TODO Auto-generated method stub
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
	        cm.onDestroy();	
	//	}
	}

	public void onConnected() {
		BarobotConnector barobot = Arduino.getInstance().barobot;
		if(!Arduino.getInstance().barobot.ledsReady){
			Arduino.getInstance().barobot.scann_leds( barobot.main_queue );
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
