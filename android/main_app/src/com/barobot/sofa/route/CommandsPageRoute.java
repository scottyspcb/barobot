package com.barobot.sofa.route;

import java.util.List;
import java.util.Map;

import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.web.route.EmptyRoute;
import com.barobot.web.server.SofaServer;
import com.x5.template.Chunk;
import com.x5.template.Theme;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;

public class CommandsPageRoute extends EmptyRoute {

	CommandsPageRoute(){
		this.regex = "^\\/commands$";
	}
	@Override
	public String run(String url, SofaServer sofaServer, Theme theme,IHTTPSession session) {
		if(theme == null){
			return null;
		}
		Chunk action_chunk			= theme.makeChunk("commands#body");
    	action_chunk.set("commands", CommandRoute.geCommands() );
    	
    	BarobotConnector barobot = Arduino.getInstance().barobot;
    	
    	action_chunk.set("options", barobot.state.getAll() );
    	
    	return action_chunk.toString();
	}
}
