package com.barobot.sofa.route;

import com.barobot.common.constant.Constant;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.web.route.EmptyRoute;
import com.barobot.web.server.SofaServer;
import com.x5.template.Chunk;
import com.x5.template.Theme;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;

public class SetupPage extends EmptyRoute{
	public SetupPage() {
		use_raw_output = false;	
		this.regex = "^\\/setup$";
	}
	public String run(String url, SofaServer sofaServer, Theme theme, IHTTPSession session){
		if(theme == null){
			return null;
		}
		Chunk action_chunk			= theme.makeChunk("setup_page#mainpage");
		/*		Map<String, List<String>> decodedQueryParameters =sofaServer.decodeParameters(session.getQueryParameterString());

    	StringBuilder sb = new StringBuilder(); 

        sb.append("<h3>Parms</h3><p><blockquote>").
              append(toString(session.getParms())).append("</blockquote></p>");

        action_chunk.set("body2", sb.toString() );*/

		BarobotConnector barobot = Arduino.getInstance().barobot;
		action_chunk.set("version1", ""+Constant.ANDROID_APP_VERSION );
		action_chunk.set("version2", barobot.state.get("ARDUINO_VERSION", "0") );  
		action_chunk.set("version3",""+Constant.WIZARD_VERSION );  

    	return action_chunk.toString();
	}
}
