package com.barobot.sofa.route;

import android.util.Log;

import com.barobot.web.route.EmptyRoute;
import com.barobot.web.server.SofaServer;
import com.x5.template.Theme;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;

public class RPCPage extends EmptyRoute {

	public RPCPage() {
		use_raw_output = true;
		this.regex = "^\\/rpc$";
	}
	@Override
	public String run(String url, SofaServer sofaServer, Theme theme, IHTTPSession session){
		if(session.getParms().containsKey("command")){
			String command = session.getParms().get("command");
			Log.i("RPCPage command", command);
			if(command.equals( "x10") ){
				return "OK";
			}else if(command.equals( "x-10") ){
				return "OK";	
			}
		}
		return "-1";
	}
}
