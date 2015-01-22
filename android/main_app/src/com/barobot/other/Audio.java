package com.barobot.other;

import java.util.HashMap;
import java.util.Map;

import android.graphics.Color;
import android.media.AudioFormat;
import android.media.MediaRecorder;

import com.barobot.activity.StartupActivity;
import com.barobot.audio.DetectorThread;
import com.barobot.common.Initiator;
import com.barobot.common.constant.Pwm;
import com.barobot.common.interfaces.OnSignalsDetectedListener;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.parser.Queue;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.message.Mainboard;

public class Audio implements OnSignalsDetectedListener{
	private static Audio instance = null;
	private DetectorThread detectorThread;
	private AndroidRecorderThread recorderThread;

	long last				= 0;
	public long max			= 100;
	public long min			= 10000;
	public boolean sync		= false;
	BarobotConnector barobot = null;
	private boolean disabled	= false;

	public void start( final BarobotConnector barobot2 ) {
		barobot = barobot2;	
		barobot.lightManager.demoStarted	= true;
		disabled = false;
		Queue q = barobot.main_queue;
		q.add( new AsyncMessage( true ) {
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				this.name		= "turnoff";
				Queue q = new Queue();
				barobot.lightManager.turnOffLeds(q);
				return q;
			}
		} );
		q.add( new AsyncMessage( true ) {
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
				detectorThread.setOnSignalsDetectedListener(Audio.this);
				detectorThread.start();

				return null;
			}
		} );
	}

	public void stop() {
		disabled = true;

		if (recorderThread != null) {
			recorderThread.stopRecording();
			recorderThread = null;
		}
		if (detectorThread != null) {
			detectorThread.stopDetection();
			detectorThread = null;
		}
		max = 10;
		barobot.lightManager.demoStarted	= false;
		StartupActivity.changeStartupColor(Color.WHITE);
	}

	float div2 = 0;
	
	
	@Override
	public void peek(final float averageAbsValue) {
		final long current = (long) (averageAbsValue * (averageAbsValue));
		if( current > max ){
			max = current;
		}
		if( current < min ){
			min = current;
		}
		float overmin		= Math.max(1, current - min);
		float scope			= Math.max(1, max - min);
		float div 			= ( overmin / scope );
		int norm			= (int) (div * 1024);
		div2				= (div2*3 + div) / 4;
	//	Initiator.logger.i( this.getClass().getName(), "\t>>>add: " + current);
		if( Math.abs(norm - last) >  3) {
			Queue q		= barobot.main_queue;
	//		Initiator.logger.i( this.getClass().getName(), "\t>>>add: " + Math.abs(norm - last)+ "/ "+ norm );
		//	float b		= div * 255;
		//	b			= Math.min(b, 255);
			//		int val1	= (int) b;
		//	int val2	= Pwm.linear2log((int) b, 1 );
			int[] arr 		= barobot.lightManager.floatToHSV( q, div2 );

			int rgb = (255 << 8) + arr[0];			// non trasparent
			rgb = (rgb << 8) + arr[1];
			rgb = (rgb << 8) + arr[2];

			StartupActivity.changeStartupColor(rgb);
	
	//		Initiator.logger.i( this.getClass().getName(), "" + (div * 100) );
		//	barobot.lightManager.setAllLeds(q, "44", val2, 0, 0, val2);
			min		+=1;		// auto ajdist
			max		= (long) (max * 0.95);		// auto ajdist
			last= norm;
		}
	}

	@Override
	public void notify(String string, final double value) {
	}

	@Override
	public void changeBPM(float newbpm) {
	}

	public boolean isRunning() {
		Initiator.logger.i("Audio.isRunning1", ""+(detectorThread == null) );
		if(detectorThread != null){
			Initiator.logger.i("Audio.isRunning2", ""+(detectorThread.isRunning) );
			Initiator.logger.i("Audio.isRunning2", ""+(detectorThread.isAlive()) );
		}
		if(detectorThread == null){
			return false;
		}
		return (detectorThread.isRunning || detectorThread.isAlive());
	}

	public static Audio getInstance() {
		if( instance == null ){
			Initiator.logger.i("Audio.getInstance", "new instance");
			instance = new Audio();
		}
		return instance;
	}
}
