package com.barobot.sofa.route;


import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.web.route.EmptyRoute;
import com.barobot.web.server.SofaServer;
import com.x5.template.Chunk;
import com.x5.template.Theme;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;

public class HardwareQueuePage extends EmptyRoute{

	HardwareQueuePage(){
		this.regex = "^\\/hardware_queue$";
	}

	@Override
	public String run(String url, SofaServer sofaServer, Theme theme,IHTTPSession session) {
		Chunk action_chunk			= theme.makeChunk("hardware_queue");
    	BarobotConnector barobot	= Arduino.getInstance().barobot;
    	
    	/*
    	queue
    	history
    	
    	Map<String, ?> allEntries 	=  myPrefs.getAll();
		Map<String, String> nMap 	= new HashMap<String, String>();
		for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
		    nMap.put(entry.getKey(), entry.getValue().toString());
		} 
		return nMap;
		*/
		
		
    	return action_chunk.toString();
	}
}
