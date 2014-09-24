package com.barobot.sofa.route;

import com.barobot.common.Initiator;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.parser.Queue;
import com.barobot.web.route.EmptyRoute;
import com.barobot.web.server.SofaServer;
import com.x5.template.Theme;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;

public class MessageRoute extends EmptyRoute {

	MessageRoute(){
		this.regex = "^\\/message$";
		use_raw_output = true;
	}

	@Override
	public String run(String url, SofaServer sofaServer, Theme theme,
			IHTTPSession session) {

		if(session.getParms().containsKey("message")){
			String message		= session.getParms().get("message");
			boolean bypass		= session.getParms().get("bypass").equals("1");
			boolean blocking	= session.getParms().get("blocking").equals("1");
			BarobotConnector barobot = Arduino.getInstance().barobot;
			Queue mq			= barobot.main_queue;
			Initiator.logger.i( this.getClass().getName(), "add message " + message + " bypass:" +bypass+ " blocking" + blocking );
			if(bypass){
				mq.sendNow(message+"\n");
			}else{
				mq.add(message, blocking);
			}
		}
		return "OK";
	}
}