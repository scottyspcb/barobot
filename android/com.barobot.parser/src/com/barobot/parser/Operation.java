package com.barobot.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Operation {
	String name = "";
	private static Map<String, Object> params = new HashMap<String, Object>();

	public Operation(String name) {
		this.name = name;
	}
	public void needParam(String key, Object i) {
		params.put( key, i );
	}
	public void needParam(String key) {
		params.put( key, null );
	}
	public Object getParam(String key, String type ) {
		Object p = params.get(key);
		if(p == null){
			return null;
		}
		if(p.getClass() != null){
			if(Parser.logger!=null){
				Parser.logger.log(Level.INFO, "" + p.getClass());
			}
		}
		return p;
	}
}
