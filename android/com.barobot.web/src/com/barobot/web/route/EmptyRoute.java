package com.barobot.web.route;

import com.barobot.web.server.SofaServer;
import com.x5.template.Theme;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;

public abstract class EmptyRoute {
	public String regex = "####";
	public boolean use_raw_output = false;

	public boolean match(String uri) {
		return uri.matches(regex);
	}

	abstract public String run(String url,SofaServer sofaServer, Theme theme, IHTTPSession session);

	public void setHeaders(Response r) {
		// TODO Auto-generated method stub
	}
}
