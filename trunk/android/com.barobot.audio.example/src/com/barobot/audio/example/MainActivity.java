package com.barobot.audio.example;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.barobot.audio.DetectorThread;
import com.barobot.common.Initiator;
import com.barobot.common.interfaces.HardwareState;
import com.barobot.common.interfaces.OnSignalsDetectedListener;
import com.barobot.common.interfaces.serial.SerialEventListener;
import com.barobot.common.interfaces.serial.SerialInputListener;
import com.barobot.common.interfaces.serial.Wire;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.hardware.devices.i2c.Upanel;
import com.barobot.hardware.serial.AndroidBarobotState;
import com.barobot.hardware.serial.Serial_wire;
import com.barobot.parser.Queue;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.message.Mainboard;
import com.barobot.parser.utils.Interval;

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
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		System.out.println("before onCreate");

		// set views
		LayoutInflater inflater = LayoutInflater.from(this);
		mainView = inflater.inflate(R.layout.listening, null);
		setContentView(mainView);

		ProgressBar textView = (ProgressBar) mainView.findViewById(R.id.progressBar1);
		textView.setMax(1024);
    	
		startConnection();

		Button xb1 = (Button) findViewById(R.id.unlock);
		xb1.setOnClickListener( new OnClickListener(){
			@Override
			public void onClick(View v) {
				barobot.main_queue.unlock();
			}
		});
		System.out.println("before scann_leds");
    //	init();

	}

	private void startConnection() {
		this.state			= new AndroidBarobotState(this);	
		this.barobot		= new BarobotConnector( state );
		state.set("show_unknown", 0 );

		if( connection != null ){
			connection.close();
			connection = null;
		}
		connection		= new Serial_wire( this );
		connection.setSerialEventListener( new SerialEventListener() {
			@Override
			public void onConnect() {
				barobot.main_queue.add( "\n", false );	// clean up input
				barobot.main_queue.add( "\n", false );
				barobot.main_queue.addWait(600);
				startQueue();
			}
			@Override
			public void onClose() {
			}
			@Override
			public void connectedWith(String bt_connected_device, String address) {
			}
		});
		connection.init();
		SerialInputListener listener = barobot.willReadFrom( connection );
		barobot.willWriteThrough( connection );
	}

	protected void startQueue() {
		barobot.scann_leds();
		barobot.main_queue.add( new AsyncMessage( true ) {
			@Override
			public Queue run(Mainboard dev, Queue queue) {

				this.name		= "turnoff";
				
				System.out.println("turnoff");
				
				Upanel[] up		= barobot.i2c.getUpanels();
				for( int i=0;i<up.length ;i++){
					barobot.main_queue.add( "L"+ up[i].getAddress() + ",ff,0", true );
				}
				return null;
			}
		} );
		barobot.main_queue.add( new AsyncMessage( true ) {
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				this.name		= "scanning";

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
				detectorThread.setOnSignalsDetectedListener(MainActivity.this);
				detectorThread.start();

				return null;
			}
		} );
	}

	public void init() {
		final Upanel[] up		= barobot.i2c.getUpanels();
		ii1 = new Interval(new Runnable(){
			public void run() {
				toggled1 = !toggled1;
				if(toggled1){
				//	up[0].setLed(barobot.main_queue, "04", 0);	
					barobot.main_queue.add( "L"+ up[0].getAddress() +",04,0", sync);
				}else{
					barobot.main_queue.add( "L"+ up[0].getAddress() +",04,255", sync);
				}
			}});
		ii2 = new Interval(new Runnable(){
			public void run() {
				toggled2 = !toggled2;
				if(toggled2){
					barobot.main_queue.add( "L"+ up[2].getAddress() +",01,0", sync);
				}else{
					barobot.main_queue.add( "L"+ up[2].getAddress() +",01,255", sync);
				}
			}});
		ii3 = new Interval(new Runnable(){
			public void run() {
				toggled3 = !toggled3;
				if(toggled3){
					barobot.main_queue.add( "L"+ up[4].getAddress() +",02,0", sync);
				}else{
					barobot.main_queue.add( "L"+ up[4] +",02,255", sync);
				}
			}});

		ii0 = new Interval(new Runnable(){
			public void run() {
				toggled0 = !toggled0;
				if(toggled0){
					barobot.main_queue.add( "L"+ up[6].getAddress() +",01,0", sync);
				}else{
					barobot.main_queue.add( "L"+ up[6].getAddress() +",01,255", sync);
				}
			}});
	}
	protected void onDestroy() {
		max = 10;
		barobot.destroy();
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
		Upanel[] up				= barobot.i2c.getUpanels();
		final float div 		= (( (float) current - (float) min)/ (float) max);
		final int norm = (int) (div * 1024);
	//	System.out.println("\t>>>add: " + current);
		if( Math.abs(norm - last) > 6 ){
			float b = div * 255;
			b = Math.min(b, 255);

			final String command1 = ",11," + ((int) b);
			final String command2 = ",22," + ((int) b);
			final String command3 = ",44," + ((int) b);
	//		System.out.println("\t>>>add: " + command);

			barobot.main_queue.add( "L"+ up[1].getAddress() +command3, sync);
			barobot.main_queue.add( "L"+ up[3].getAddress() +command2, sync);
			barobot.main_queue.add( "L"+ up[5].getAddress() +command1, sync);
			barobot.main_queue.add( "L"+ up[7].getAddress() +command1, sync);
			barobot.main_queue.add( "L"+ up[9].getAddress() +command2, sync);
			barobot.main_queue.add( "L"+ up[11].getAddress() +command3, sync);
			/*
			barobot.main_queue.add( "L"+ up[6].getAddress() +command1, sync);
			barobot.main_queue.add( "L"+ up[7].getAddress() +command1, sync);
			barobot.main_queue.add( "L"+ up[9].getAddress() +command1, sync);
			barobot.main_queue.add( "L"+ up[10].getAddress() +command1, sync);
			barobot.main_queue.add( "L"+ up[10].getAddress() +command1, sync);
			barobot.main_queue.add( "L"+ up[11].getAddress() +command1, sync);
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
//			barobot.main_queue.add( "L"+ up[].getAddress() +""+command2, true);
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
