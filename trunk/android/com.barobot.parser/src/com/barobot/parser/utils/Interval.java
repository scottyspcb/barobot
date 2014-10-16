package com.barobot.parser.utils;

import java.util.Timer;
import java.util.TimerTask;

public class Interval {
	private TimerTask scanTask;
	private static Timer t = new Timer();
	private Runnable rrr=null;
	private boolean running;
	public Interval( Runnable r){
		this.rrr= r;
	}
	public void run( long zaile ){		// run once
		run( zaile, 0 );
	}
	public void run( long zaile, long coile ){
		scanTask = new TimerTask() {
		    public void run() {
		            rrr.run();
		    }};
		if( coile > 0 ){
			t.schedule(scanTask, zaile, coile);		// task, za ile pierwsze, co ile następne
		}else{
			t.schedule(scanTask, zaile);		// task, za ile pierwsze
		}
		running = true;
	}
	public void cancel(){
		running = false;
		scanTask.cancel();
	}
	public void pause() { 
	}
	public boolean isRunning() {
		return running;
	}
}
