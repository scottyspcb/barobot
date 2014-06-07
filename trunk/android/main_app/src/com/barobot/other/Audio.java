package com.barobot.other;

import java.util.HashMap;
import java.util.Map;

import android.media.AudioFormat;
import android.media.MediaRecorder;

import com.barobot.audio.DetectorThread;
import com.barobot.common.constant.Pwm;
import com.barobot.common.interfaces.OnSignalsDetectedListener;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.hardware.devices.i2c.Upanel;
import com.barobot.parser.Queue;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.message.Mainboard;

public class Audio implements OnSignalsDetectedListener{
	private DetectorThread detectorThread;
	private AndroidRecorderThread recorderThread;

	long last				= 0;
	public long max			= 100;
	public long min			= 10000;
	public boolean sync		= false;
	BarobotConnector barobot = null;
	Upanel[] up				= null;

	public void start( final BarobotConnector barobot2 ) {
		this.barobot = barobot2;
		Upanel[] up2		= barobot.i2c.getUpanels();
		if(up2.length < 12){
			barobot.scann_leds();
		}
		Queue q = barobot.main_queue;
		q.add( new AsyncMessage( true ) {
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				this.name		= "turnoff";
				up				= barobot.i2c.getUpanels();
				for( int i=0;i<up.length ;i++){
					queue.add( "L"+ up[i].getAddress() + ",ff,0", true );
				}
				return null;
			}
		} );
		q.add( new AsyncMessage( true ) {
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				this.name		= "scanning";
				if(up.length >= 12){
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
				}
				return null;
			}
		} );
	}

	public void stop() {
		barobot.main_queue.add( new AsyncMessage( true ) {
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				this.name		= "Audio.stop";
				if (recorderThread != null) {
					recorderThread.stopRecording();
					recorderThread = null;
				}
				if (detectorThread != null) {
					detectorThread.stopDetection();
					detectorThread = null;
				}
				max = 10;

				Queue q = new Queue();
				for( int i=0;i<up.length ;i++){
					queue.add( "B"+ up[i].getAddress() + ",ff,0", true );
				}
				return q;
			}
		} );
	}

	@Override
	public void peek(final float averageAbsValue) {
		//final int current = (int) averageAbsValue;
		final long current = (long) (averageAbsValue * (averageAbsValue));
		if( current > max ){
			max = current;
		}
		if( current < min ){
			min = current;
		}
		float overmin			= current - min;
		float scope				= max - min;
		final float div 		= ( overmin / scope);
		final int norm			= (int) (div * 1024);
	//	System.out.println("\t>>>add: " + current);
		if( Math.abs(norm - last) > 6 || norm <= 1 ){
	//		System.out.println("\t>>>add: " + Math.abs(norm - last)+ "/ "+ norm );
			float b = div * 255;
			b = Math.min(b, 255);
			int val1 = (int) b;
			int val2 = Pwm.linear2log((int) b, 1 );
	//		
			final String command1f = ",11," + val2;
			final String command2f = ",22," + val2;
			final String command3f = ",44," + val2;
			final String command4f = ",80," + val2;

			final String command1b = ",11," + val1;
			final String command2b = ",22," + val1;
			final String command3b = ",44," + val1;
			
			Queue q = barobot.main_queue;
			
			q.add( "B"+ "10" +command4f, sync);
			q.add( "B"+ up[1].getAddress() +command3f, sync);
			q.add( "B"+ up[3].getAddress() +command2f, sync);
			q.add( "B"+ up[5].getAddress() +command1f, sync);
			q.add( "B"+ up[7].getAddress() +command1f, sync);
			q.add( "B"+ up[9].getAddress() +command2f, sync);
			q.add( "B"+ up[11].getAddress() +command3f, sync);

			q.add( "B"+ up[0].getAddress() +command1b, sync);
			q.add( "B"+ up[2].getAddress() +command1b, sync);
			q.add( "B"+ up[4].getAddress() +command1b, sync);
			q.add( "B"+ up[6].getAddress() +command1b, sync);
			q.add( "B"+ up[8].getAddress() +command1b, sync);
			q.add( "B"+ up[10].getAddress() +command1b, sync);
			min+=1;		// auto ajdist
			max = (long) (max * 0.95);		// auto ajdist
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
		return detectorThread == null ? false : (detectorThread.isRunning && detectorThread.isAlive());
	}
}
