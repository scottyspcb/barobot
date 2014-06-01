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

	public long max			= 100;
	public long min			= 10000;
	public boolean sync		= false;
	BarobotConnector barobot = null;

	protected void start( final BarobotConnector barobot2 ) {
		this.barobot = barobot2;
		barobot.scann_leds();
		barobot.main_queue.add( new AsyncMessage( true ) {
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				this.name		= "turnoff";
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
				detectorThread.setOnSignalsDetectedListener(Audio.this);
				detectorThread.start();

				return null;
			}
		} );
	}

	protected void stop( BarobotConnector barobot) {
		max = 10;
		if (recorderThread != null) {
			recorderThread.stopRecording();
			recorderThread = null;
		}
		if (detectorThread != null) {
			detectorThread.stopDetection();
			detectorThread = null;
		}
	}
	long last = 0;

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
		Upanel[] up				= barobot.i2c.getUpanels();
		final float div 		= (( (float) current - (float) min)/ (float) max);
		final int norm = (int) (div * 1024);
	//	System.out.println("\t>>>add: " + current);
		if( Math.abs(norm - last) > 6 ){
			float b = div * 255;
			b = Math.min(b, 255);

			int val1 = (int) b;
			int val2 = Pwm.linear2log((int) b, 1 );

			final String command1f = ",11," + val2;
			final String command2f = ",22," + val2;
			final String command3f = ",44," + val2;
			final String command4f = ",88," + val2;

			final String command1b = ",11," + val1;
			final String command2b = ",22," + val1;
			final String command3b = ",44," + val1;

	//		System.out.println("\t>>>add: " + command);

			barobot.main_queue.add( "L"+ "10" +command4f, sync);
			barobot.main_queue.add( "L"+ up[1].getAddress() +command3f, sync);
			barobot.main_queue.add( "L"+ up[3].getAddress() +command2f, sync);
			barobot.main_queue.add( "L"+ up[5].getAddress() +command1f, sync);
			barobot.main_queue.add( "L"+ up[7].getAddress() +command1f, sync);
			barobot.main_queue.add( "L"+ up[9].getAddress() +command2f, sync);
			barobot.main_queue.add( "L"+ up[11].getAddress() +command3f, sync);

			barobot.main_queue.add( "L"+ up[0].getAddress() +command3b, sync);
			barobot.main_queue.add( "L"+ up[2].getAddress() +command2b, sync);
			barobot.main_queue.add( "L"+ up[4].getAddress() +command1b, sync);
			barobot.main_queue.add( "L"+ up[6].getAddress() +command1b, sync);
			barobot.main_queue.add( "L"+ up[8].getAddress() +command2b, sync);
			barobot.main_queue.add( "L"+ up[10].getAddress() +command3b, sync);
			min++;		// auto ajdist
			max--;		// auto ajdist
			last= norm;
		}
	}
	@Override
	public void notify(String string, final double value) {
	}

	@Override
	public void changeBPM(float newbpm) {
	}
}
