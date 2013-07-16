package com.barobot;

import java.util.Timer;
import java.util.TimerTask;

import android.os.Handler;

public class interval {
	private TimerTask scanTask;
	private static Timer t = new Timer();
	private Runnable rrr=null;

	public interval(){
		this.rrr= new Runnable() {
		    public void run() {
		    	Constant.log("RUNNABLE", "TICK" );
		   }
		};
	}

	public void run( long zaile ){
		run( zaile, 0 );
	}
	public void run( long zaile, long coile ){
		final Handler handler = new Handler();

		scanTask = new TimerTask() {
		    public void run() {
		            handler.post(rrr);
		    }};

		if( coile > 0 ){
			t.schedule(scanTask, zaile, coile);		// task, za ile pierwsze, co ile nastêpne
		}else{
			t.schedule(scanTask, zaile);		// task, za ile pierwsze
		}
	}
	public void cancel(){
		scanTask.cancel();	
	}
}
