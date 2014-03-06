package com.barobot.audio;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.barobot.audio.utils.OnSignalsDetectedListener;
import com.barobot.audio.utils.SampleProcessor;

public class BpmProcessor implements SampleProcessor {
    private int sampleRate = 44100;
    private int sampleSize =  2048;
    private int bufferLength = 43;
    private Logger log = Logger.getLogger(BpmProcessor.class.getName());

    public BpmProcessor(Map<String, Integer> config) {
    	sampleRate = config.get("sampleRate");
    	sampleSize = config.get("sampleSize");
	}
    private long samples = 0;
    private long beats = 0;
	private Queue<Long> energyBuffer = new LinkedList<Long>();
    private static int beatThreshold = 3;
    private int beatTriggers = 0;
    private List<Integer> bpmList = new LinkedList<Integer>();
    private double C = 1.3; //a * variance + b;
	private OnSignalsDetectedListener onSignalsDetectedListener;

    public void process(long sample ) {
    //	   log.log(Level.INFO, "process: " +sample);
        energyBuffer.offer(sample);
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
                beats = 0;
                samples = 0;
            	int bbb = getInstantBPM();
         //   	log.log(Level.INFO, "add getInstantBPM: " +bbb + " / " + ((beats * frequency)));
            //	System.out.println("\t\tbeat: " +sample);
            	onSignalsDetectedListener.notify( "local_bpm", bbb );
                bpmList.add(bbb);
            }
        }else{
        	log.log(Level.INFO, "empty process: "+ energyBuffer.size());
        }
    }
    public int getInstantBPM() {
        return (int)((beats * sampleRate * 60) / (samples * sampleSize));
    }
    public int getBPM() {
        Collections.sort(bpmList);
        int size = bpmList.size();
    //    log.log(Level.INFO, "getBPMs: " +size);
        if(size == 0 ){
        	return 0;
        }
        int res = bpmList.get(size/ 2);
     //   bpmList.clear();
        return res;
    }

	public void setOnSignalsDetectedListener(OnSignalsDetectedListener onSignalsDetectedListener2) {
		onSignalsDetectedListener = onSignalsDetectedListener2;
	}
}
