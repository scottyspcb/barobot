package com.barobot.audio.example;

import java.util.HashMap;
import java.util.Map;

import other.InputListener;
import other.Wire;

import com.barobot.audio.DetectorThread;
import com.barobot.audio.utils.OnSignalsDetectedListener;
import com.barobot.hardware.serial.Serial_wire;

import com.barobot.parser.utils.Interval;
import com.barobot.parser.Queue;
import com.barobot.parser.output.Mainboard;

import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity implements OnSignalsDetectedListener{
	private DetectorThread detectorThread;
	private AndroidRecorderThread recorderThread;
	private Queue q = new Queue();

	private View mainView;
	public long max			= 100;
	public long min			= 10000;
	Wire connection			= null;
	private int mainboardSource;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// set views
		LayoutInflater inflater = LayoutInflater.from(this);
		mainView = inflater.inflate(R.layout.listening, null);
		setContentView(mainView);

		ProgressBar textView = (ProgressBar) mainView.findViewById(R.id.progressBar1);
		textView.setMax(1024);

    	if(connection !=null){
    		connection.destroy();
    	}

   	 	connection = new Serial_wire( this );
   	 	connection.setOnReceive( new InputListener(){
			@Override
			public void onNewData(byte[] data) {
			   	//Log.e("Serial input", message);
				String message = new String(data);
				readInput(message);
			}
			@Override
			public void onRunError(Exception e) {
			}});
    	connection.init();

		Map<String, Integer> config = new HashMap<String, Integer>();
		config.put("source",  MediaRecorder.AudioSource.MIC);
		config.put("frameByteSize", 2048);
		config.put("channelDef", AudioFormat.CHANNEL_IN_MONO);
		config.put("channels", 1 );
		config.put("sampleSize", 2048 );
		config.put("averageLength", 2048 );
		config.put("audioEncoding",  AudioFormat.ENCODING_PCM_16BIT);
		config.put("bitDepth",   16 );
		config.put("sampleRate", 44100);

		recorderThread = new AndroidRecorderThread( config );
		recorderThread.start();
		detectorThread = new DetectorThread( config, recorderThread);
		detectorThread.setOnSignalsDetectedListener(this);
		detectorThread.start();
		
		Mainboard mb	= new Mainboard();
		//	AsyncDevice c	= new Console();
		//	AsyncDevice u	= new MainScreen();
		mb.registerSender( connection );		
		mainboardSource = Queue.registerSource( mb );
		Queue.enableDevice( mainboardSource );

    	init();
	}


	
	public void init() {
		ii1 = new Interval(new Runnable(){
			public void run() {
				toggled1 = !toggled1;
				if(toggled1){
					q.add( "L19,04,0", true);
				}else{
					q.add( "L19,04,255", true);
				}
			}});
		ii2 = new Interval(new Runnable(){
			public void run() {
				toggled2 = !toggled2;
				if(toggled2){
					q.add( "L20,01,0", true);
				}else{
					q.add( "L20,01,255", true);
				}
			}});
		ii3 = new Interval(new Runnable(){
			public void run() {
				toggled3 = !toggled3;
				if(toggled3){
					q.add( "L18,02,0", true);
				}else{
					q.add( "L18,02,255", true);
				}
			}});

		ii0 = new Interval(new Runnable(){
			public void run() {
				toggled0 = !toggled0;
				if(toggled0){
					q.add( "L16,01,0", true);
				}else{
					q.add( "L16,01,255", true);
				}
			}});
	}
	protected void onDestroy() {
		max = 10;
		q.clear(mainboardSource);
		setContentView(mainView);
		if (recorderThread != null) {
			recorderThread.stopRecording();
			recorderThread = null;
		}
		if (detectorThread != null) {
			detectorThread.stopDetection();
			detectorThread = null;
		}
		if(ii1.isRunning()){
			ii1.cancel();
		}
		if(ii2.isRunning()){
			ii2.cancel();
		}
		if(ii3.isRunning()){
			ii3.cancel();
		}
		if(ii0.isRunning()){
			ii0.cancel();
		}
		super.onDestroy();
		android.os.Process.killProcess(android.os.Process.myPid());
	}
	long last = 0;
	private float oldbpm;
	public void peek(final float averageAbsValue) {
		//final int current = (int) averageAbsValue;

		final long current = (long) (averageAbsValue * (averageAbsValue));
		if( current > max ){
			max = current;
			runOnUiThread(new Runnable() {
				public void run() {
					ProgressBar textView = (ProgressBar) mainView.findViewById(R.id.progressBar1);
					int norm = (int) (current/max * 1024);
					textView.setMax( norm );
					TextView maxxxmaxxx = (TextView) mainView.findViewById(R.id.maxxx);
					maxxxmaxxx.setText(""+ max);
				}
			});
		}
		if( current < min ){
			min = current;
		}
		final float div = (( (float) current - (float) min)/ (float) max);
		final int norm = (int) (div * 1024);
	//	System.out.println("\t>>>add: " + current);
		if( Math.abs(norm - last) > 4 ){
			float b = div * 255;
			b = Math.min(b, 255);
			final String command2 = ",22," + ((int) b);
	//		System.out.println("\t>>>add: " + command);

			q.add( "L14"+command2, true);
		//	q.add( "L15"+command2, true);
		//	q.add( "L16"+command2, true);
		//	q.add( "L17"+command2, true);
		//	q.add( "L18"+command2, true);
		//	q.add( "L19"+command2, true);
			last = norm;
		}
		runOnUiThread(new Runnable() {
			public void run() {
				ProgressBar textView = (ProgressBar) mainView.findViewById(R.id.progressBar1);
				textView.setProgress(norm);

				TextView normnorm = (TextView) mainView.findViewById(R.id.normnorm);
				normnorm.setText(""+ norm);	

				TextView textView2 = (TextView) mainView.findViewById(R.id.curcur);
				textView2.setText(""+ current);
			}
		});
	}

	public void readInput(String message) {
		 q.read( mainboardSource, message );
	}
	
	public void notify(String string, final double value) {
		if(string.equals("energy")){
			runOnUiThread(new Runnable() {
				public void run() {
					TextView divdiv = (TextView) mainView.findViewById(R.id.divdiv);
					divdiv.setText(""+ value);		
				}
			});
		}else if(string.equals("bpm")){
				runOnUiThread(new Runnable() {
					public void run() {
						TextView textView = (TextView) mainView.findViewById(R.id.tempo);
						textView.setText(""+value);
					}
				});
		}else if(string.equals("local_bpm")){
			if(ii3.isRunning()){
				ii3.cancel();
			}
			if( value > 0 ){
				int time = (int)(60 * 1000 /value/2);		// interval
				ii0.run( 0, time );
			}
			runOnUiThread(new Runnable() {
				public void run() {
					TextView textView = (TextView) mainView.findViewById(R.id.localbpm);
					textView.setText(""+value);
					TextView textView2 = (TextView) mainView.findViewById(R.id.beats);
					textView2.setText("0");
				}
			});
		}else if(string.equals("beat")){
			float div = (float) (value / 6);
			float b = div * 255;
			b = Math.min(b, 255);
//			final String command2 = ",11," + ((int) b);
	//		System.out.println("\t>>>add: " + command);
//			q.add( "L15"+command2, true);
			runOnUiThread(new Runnable() {
				public void run() {
					TextView textView = (TextView) mainView.findViewById(R.id.beats);
					textView.setText(""+ ((int)value));
				}
			});	
		}
	}
	boolean toggled0 = false;
	boolean toggled1 = false;
	boolean toggled2 = false;
	boolean toggled3 = false;
	
	
	Interval ii1 = null;
	Interval ii2 = null;
	Interval ii3 = null;
	Interval ii0 = null;
	
	@Override
	public void changeBPM(float newbpm) {
		if( newbpm != oldbpm ){
			
			if(ii1.isRunning()){
				ii1.cancel();
			}
			if(ii2.isRunning()){
				ii2.cancel();
			}
			if(ii3.isRunning()){
				ii3.cancel();
			}
			if( newbpm > 0 ){
				int time = (int)( 60 * 1000 /newbpm/8 );		// interval
				System.out.println("co time " + time);
				ii1.run( 0, time );
				ii2.run( 0, time / 2 );
				ii3.run( 0, time / 4 );
				oldbpm = newbpm;
			}
		}
	}
}
