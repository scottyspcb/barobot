package com.barobot.sofa.api;

import com.barobot.web.route.EmptyRoute;
import com.barobot.web.server.SofaServer;
import com.x5.template.Theme;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;

public abstract class Page extends EmptyRoute {

	public Page()
	{
		use_raw_output=true;
	}
	
	@Override
	public String run(String url, SofaServer sofaServer, Theme theme,
			IHTTPSession session) {
		try {
			return runInternal(url, sofaServer, theme, session).getJSON();
		} catch (Exception e)
		{
			return "{ \"status\" : \"FATAL_ERROR\", \"message\": \""+e.getMessage()+"\"}";
		}
	}
	
	abstract protected JsonResponse runInternal(String Url, SofaServer sofaServer, Theme theme,
			IHTTPSession session);


}
