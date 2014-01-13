package com.barobotweb;

import com.x5.template.Theme;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;

public abstract class EmptyRoute {
	public static String regex = "-";
	public boolean use_raw_output = false;
	protected String url;
	public EmptyRoute(String uri) {
		this.url = uri;
	}
	abstract public String run(SofaServer sofaServer, Theme theme, IHTTPSession session);
}
