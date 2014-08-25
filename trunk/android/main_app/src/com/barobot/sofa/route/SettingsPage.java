package com.barobot.sofa.route;

import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.web.route.EmptyRoute;
import com.barobot.web.server.SofaServer;
import com.x5.template.Chunk;
import com.x5.template.Theme;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;

public class SettingsPage extends EmptyRoute{

	SettingsPage(){
		this.regex = "^\\/settings$";
	}

	@Override
	public String run(String url, SofaServer sofaServer, Theme theme,IHTTPSession session) {
		Chunk action_chunk			= theme.makeChunk("settings#settings");
    	BarobotConnector barobot = Arduino.getInstance().barobot;
    	action_chunk.set("options", barobot.state.getAll() );
    	return action_chunk.toString();
	}
}
