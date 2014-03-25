package com.barobot.audio.example;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.barobot.audio.DetectorThread;
import com.barobot.common.interfaces.serial.SerialEventListener;
import com.barobot.common.interfaces.serial.SerialInputListener;
import com.barobot.common.interfaces.HardwareState;
import com.barobot.common.interfaces.OnSignalsDetectedListener;
import com.barobot.common.interfaces.serial.Wire;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.hardware.serial.AndroidBarobotState;
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
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity implements OnSignalsDetectedListener{
	private DetectorThread detectorThread;
	private AndroidRecorderThread recorderThread;

	public HardwareState state= null;
	public BarobotConnector barobot;

	private View mainView;
	public long max			= 100;
	public long min			= 10000;
	Wire connection			= null;
	public boolean sync		= false;
	
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
    	
		startConnection();
		System.out.println("koniec test connection ");
		
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
		
		final Mainboard mb	= new Mainboard();
		//	AsyncDevice c	= new Console();
		//	AsyncDevice u	= new MainScreen();
		mb.registerSender( connection );
    //	init();
    	
		Button xb1 = (Button) findViewById(R.id.unlock);
		xb1.setOnClickListener( new OnClickListener(){
			@Override
			public void onClick(View v) {
				barobot.main_queue.unlock();
			}
		});
	}

	private void startConnection() {
		this.state			= new AndroidBarobotState(this);	
		this.barobot		= new BarobotConnector( state );	
		if( connection != null ){
			connection.close();
			connection = null;
		}
		connection		= new Serial_wire( this );
		connection.addOnReceive( new SerialInputListener(){
			@Override
			public void onNewData(byte[] data, int length) {
				String message = new String(data, 0, length);
		//		Log.e("Serial addOnReceive", message);
				barobot.mb.read( message );
	//			debug( message );
			}
			@Override
			public void onRunError(Exception e) {
			}
			@Override
			public boolean isEnabled() {
				return true;
			}});
		connection.init();
		connection.setSerialEventListener( new SerialEventListener() {
			@Override
			public void onConnect() {
				barobot.main_queue.add( "\n", false );	// clean up input
				barobot.main_queue.add( "\n", false );
			}
			@Override
			public void onClose() {
			}
			@Override
			public void connectedWith(String bt_connected_device, String address) {
			}
		});
		barobot.mb.registerSender( connection );
		Queue.enableDevice( barobot.mainboardSource );
		barobot.main_queue.add( "\n", true );
		barobot.main_queue.add( "\n", true );
		for( int i=0;i<BarobotConnector.upanels.length ;i++){
			barobot.main_queue.add( "L"+ BarobotConnector.upanels[i] + ",ff,0", true );
		}
	}

	public void init() {
		ii1 = new Interval(new Runnable(){
			public void run() {
				toggled1 = !toggled1;
				if(toggled1){
					barobot.main_queue.add( "L"+ BarobotConnector.upanels[0] +",04,0", sync);
				}else{
					barobot.main_queue.add( "L"+ BarobotConnector.upanels[0] +",04,255", sync);
				}
			}});
		ii2 = new Interval(new Runnable(){
			public void run() {
				toggled2 = !toggled2;
				if(toggled2){
					barobot.main_queue.add( "L"+ BarobotConnector.upanels[2] +",01,0", sync);
				}else{
					barobot.main_queue.add( "L"+ BarobotConnector.upanels[2] +",01,255", sync);
				}
			}});
		ii3 = new Interval(new Runnable(){
			public void run() {
				toggled3 = !toggled3;
				if(toggled3){
					barobot.main_queue.add( "L"+ BarobotConnector.upanels[4] +",02,0", sync);
				}else{
					barobot.main_queue.add( "L"+ BarobotConnector.upanels[4] +",02,255", sync);
				}
			}});

		ii0 = new Interval(new Runnable(){
			public void run() {
				toggled0 = !toggled0;
				if(toggled0){
					barobot.main_queue.add( "L"+ BarobotConnector.upanels[6] +",01,0", sync);
				}else{
					barobot.main_queue.add( "L"+ BarobotConnector.upanels[6] +",01,255", sync);
				}
			}});
	}
	protected void onDestroy() {
		max = 10;
		barobot.main_queue.clear(mainboardSource);
		setContentView(mainView);
		if (recorderThread != null) {
			recorderThread.stopRecording();
			recorderThread = null;
		}
		if (detectorThread != null) {
			detectorThread.stopDetection();
			detectorThread = null;
		}
		
		if(ii1 != null && ii1.isRunning()){
			ii1.cancel();
		}
		if(ii2 != null && ii2.isRunning()){
			ii2.cancel();
		}
		if(ii3 != null && ii3.isRunning()){
			ii3.cancel();
		}
		if(ii0 != null && ii0.isRunning()){
			ii0.cancel();
		}
	   	if(connection !=null){
    		connection.destroy();
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
			final String command1 = ",11," + ((int) b);
			final String command2 = ",22," + ((int) b);
			final String command3 = ",44," + ((int) b);
	//		System.out.println("\t>>>add: " + command);

			barobot.main_queue.add( "L"+ BarobotConnector.upanels[1] +command3, sync);
			barobot.main_queue.add( "L"+ BarobotConnector.upanels[3] +command2, sync);
			barobot.main_queue.add( "L"+ BarobotConnector.upanels[5] +command1, sync);
			barobot.main_queue.add( "L"+ BarobotConnector.upanels[7] +command1, sync);
			barobot.main_queue.add( "L"+ BarobotConnector.upanels[9] +command2, sync);
			barobot.main_queue.add( "L"+ BarobotConnector.upanels[11] +command3, sync);
			/*
			barobot.main_queue.add( "L"+ BarobotConnector.upanels[6] +command1, sync);
			barobot.main_queue.add( "L"+ BarobotConnector.upanels[7] +command1, sync);
			barobot.main_queue.add( "L"+ BarobotConnector.upanels[9] +command1, sync);
			barobot.main_queue.add( "L"+ BarobotConnector.upanels[10] +command1, sync);
			barobot.main_queue.add( "L"+ BarobotConnector.upanels[10] +command1, sync);
			barobot.main_queue.add( "L"+ BarobotConnector.upanels[11] +command1, sync);
			*/
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
//			barobot.main_queue.add( "L"+ BarobotConnector.upanels[] +""+command2, true);
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
