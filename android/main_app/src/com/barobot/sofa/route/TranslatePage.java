package com.barobot.sofa.route;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.orman.mapper.Model;
import org.orman.mapper.ModelQuery;

import com.barobot.gui.dataobjects.Translated_name;
import com.barobot.web.route.EmptyRoute;
import com.barobot.web.server.SofaServer;
import com.x5.template.Chunk;
import com.x5.template.Theme;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;

public class TranslatePage extends EmptyRoute{
	public TranslatePage() {
		use_raw_output = false;	
		this.regex = "^/translate$";
	}

	public String run(String url, SofaServer sofaServer, Theme theme, IHTTPSession session){
		if(theme == null){
			return null;
		}
		Chunk action_chunk			= theme.makeChunk("settings#translations");
	//	Map<String, List<String>> decodedQueryParameters =sofaServer.decodeParameters(session.getQueryParameterString());
		List<Translated_name> tn = Model.fetchQuery(ModelQuery.select().from(Translated_name.class).orderBy("Translated_name.table_name","Translated_name.element_id").getQuery(),Translated_name.class);
		List<Map<String, String>> data = new ArrayList<Map<String, String>>();
		for (Translated_name item : tn) {
			data.add( item.toHashMap());
		}
		action_chunk.set("names",data);
    	return action_chunk.toString();
	}
}
