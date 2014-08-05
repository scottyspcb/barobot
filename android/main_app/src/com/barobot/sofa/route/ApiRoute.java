package com.barobot.sofa.route;

import com.barobot.web.route.EmptyRoute;
import com.barobot.web.server.SofaServer;
import com.x5.template.Theme;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;

public class ApiRoute extends EmptyRoute {
	ApiRoute(){
		this.regex = "^/api/";
	}

	@Override
	public String run(String url, SofaServer sofaServer, Theme theme,
			IHTTPSession session) {
		// TODO Auto-generated method stub
		return null;
	}

}
