package com.barobot.audio;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.barobot.common.interfaces.OnSignalsDetectedListener;
import com.barobot.common.interfaces.SampleAudioProcessor;

public class BpmProcessor implements SampleAudioProcessor {
    private int sampleRate = 44100;
    private int sampleSize =  2048;
    private int bufferLength = 103;
    private Logger log = Logger.getLogger(BpmProcessor.class.getName());

    public BpmProcessor(Map<String, Integer> config) {
    	sampleRate = config.get("sampleRate");
    	sampleSize = config.get("sampleSize");
	}
    private float samples = 0;
    private float beats = 0;
	private Queue<Long> energyBuffer = new LinkedList<Long>();
    private static int beatThreshold = 3;
    private int beatTriggers = 0;
    private List<Float> bpmList = new LinkedList<Float>();
    private List<Float> bpmList2 = new LinkedList<Float>();
    private double C = 1.3; //a * variance + b;
	private OnSignalsDetectedListener onSignalsDetectedListener;

    int maxBufferSize1 = 10;
    int maxBufferSize2 = 4;
	private boolean stop = false;

    public void process(long sample ) {
    	if(this.stop ){
    		return;
    	} 	
    //	   log.log(Level.INFO, "process: " +sample);
        energyBuffer.offer(sample);
      //  System.out.println("sample: " +sample);
        samples++;
     //   log.log(Level.INFO, "process: " +samples);
        if(energyBuffer.size() > bufferLength) {
            energyBuffer.poll();
            double averageEnergy = 0;
            for(long l : energyBuffer){
                averageEnergy += l;
            }
            averageEnergy /= bufferLength;
            boolean beat = sample > C * averageEnergy;
        //    System.out.println("\tverageEnergy: " +averageEnergy);
       //     System.out.println("averageEnergy: " + ((long)averageEnergy) + " / "+ sample );
            onSignalsDetectedListener.notify( "energy", averageEnergy );
            if(beat) {
                if(++beatTriggers == beatThreshold){
                    beats++;
                    onSignalsDetectedListener.notify( "beat", beats );
                }
            }else{
                beatTriggers = 0;
            }
            if(samples > sampleRate * 5 / sampleSize) {
            	float bbb = getInstantBPM();
                beats	= 0;
                samples = 0;
         //   	log.log(Level.INFO, "add getInstantBPM: " +bbb + " / " + ((beats * frequency)));
            //	System.out.println("\t\tbeat: " +sample);
            	onSignalsDetectedListener.notify( "local_bpm", bbb );
                bpmList.add(bbb);
                bpmList2.add(bbb);
            }
        }else{
        	log.log(Level.INFO, "empty process: "+ energyBuffer.size());
        }
    }
    public float getInstantBPM() {
        return (float)((beats * sampleRate * 60) / (samples * sampleSize));
    }
    public float getBPM( int local ) {
    	float res = 0;
    	if( local == 0 ){
            Collections.sort(bpmList);
            int size = bpmList.size();
      //      log.log(Level.INFO, "getBPMs: " +size);
            if(size == 0 ){
            	return 0;
            }
            res = bpmList.get(size/ 2);		// IN HALF
            if( bpmList.size() > maxBufferSize1 ){
            //	bpmList.clear();
            	bpmList = bpmList.subList( maxBufferSize1/3,  maxBufferSize1/2 );
            }
    	}else if( local == 1 ){
            Collections.sort(bpmList2);
            int size = bpmList2.size();
            if(size == 0 ){
            	return 0;
            }
            res = bpmList2.get(size / 2);		// IN HALF
            if( bpmList2.size() > maxBufferSize2 ){
            //	bpmList.clear();
            	bpmList2 = bpmList2.subList( maxBufferSize2/2,  maxBufferSize2/2+1 );
            }	
    	}
        return res;
    }
	public void setOnSignalsDetectedListener(OnSignalsDetectedListener onSignalsDetectedListener2) {
		onSignalsDetectedListener = onSignalsDetectedListener2;
	}
	public void stop() {
		this.stop = true;
		onSignalsDetectedListener = null;
	}
}
