package com.barobot.audio.example;

import java.util.Map;

import com.barobot.common.interfaces.SampleAudioRecorder;

import android.media.AudioRecord;

public class AndroidRecorderThread extends Thread implements SampleAudioRecorder{
	private AudioRecord audioRecord;
	private int frameByteSize = 0;
	short[] buffer;

	public AndroidRecorderThread(Map<String, Integer> config){
		frameByteSize = config.get("frameByteSize");
		int recBufSize = AudioRecord.getMinBufferSize(
				config.get("sampleRate"), 
				config.get("channelDef"), 
				config.get("audioEncoding") ); // need to be larger than size of a frame

		System.out.println("recBufSize: " + recBufSize );
		audioRecord = new AudioRecord( 
				config.get("source"),
				config.get("sampleRate"),
				config.get("channelDef"), 
				config.get("audioEncoding"),
				recBufSize );
		//System.out.println("frameByteSize: " + frameByteSize );
	
		buffer = new short[frameByteSize];
	}
	public void startRecording(){
		try{
			audioRecord.startRecording();
	//		isRecording = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void stopRecording(){
		try{
			audioRecord.stop();
		//	isRecording = false;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public short[] getFrameBytes(){
		audioRecord.read(buffer, 0, frameByteSize);
		return buffer;
	}
	public void run() {
		startRecording();
	}
}