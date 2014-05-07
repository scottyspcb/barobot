package com.barobotweb;

import com.x5.template.Theme;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
public class TemplateRendering extends EmptyRoute {
	public static String regex = "^\\/tpl$";
	public TemplateRendering(String uri) {
		super(uri);
		use_raw_output = true;
	}
	@Override
	public String run(SofaServer sofaServer, Theme theme, IHTTPSession session){
		String tpl = session.getParms().get("command");
		// Log.i("RPCPage command", tpl);
		
		return tpl;
		/*
		Chunk action_chunk			= theme.makeChunk(tpl);			
		Map<String, List<String>> decodedQueryParameters =sofaServer.decodeParameters(session.getQueryParameterString());
        for (Map.Entry<String, List<String>> entry : decodedQueryParameters.entrySet()) {
        	if(entry.getValue().size() == 1 ){
        		action_chunk.set(entry.getKey(), entry.getValue().get(0));		// string
        	}else{
        		action_chunk.set(entry.getKey(), entry.getValue() );			// array
        	}
        }
		//action_chunk.set("list", new String[]{"apples","bananas","carrots","durian"} );
		return action_chunk.toString();
	*/
	}
}
