package com.barobot.sofa.route;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.orman.mapper.Model;
import org.orman.mapper.ModelQuery;

import android.util.Log;

import com.barobot.common.Initiator;
import com.barobot.common.constant.Constant;
import com.barobot.gui.dataobjects.Language;
import com.barobot.gui.dataobjects.Liquid_t;
import com.barobot.gui.dataobjects.Translated_name;
import com.barobot.gui.utils.LangTool;
import com.barobot.web.route.EmptyRoute;
import com.barobot.web.server.SofaServer;
import com.x5.template.Chunk;
import com.x5.template.Theme;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;

public class MainPage extends EmptyRoute{
	public MainPage() {
		use_raw_output = false;	
		this.regex = "^\\/$";
	}

	public String run(String url, SofaServer sofaServer, Theme theme, IHTTPSession session){
		if(theme == null){
			return null;
		}
		Chunk action_chunk			= theme.makeChunk("start#mainpage");
		Map<String, List<String>> decodedQueryParameters =sofaServer.decodeParameters(session.getQueryParameterString());

    	StringBuilder sb = new StringBuilder(); 

        sb.append("<h3>Parms</h3><p><blockquote>").
              append(toString(session.getParms())).append("</blockquote></p>");

        action_chunk.set("body2", sb.toString() );
    	return action_chunk.toString();
	}

    private String toString(Map<String, ? extends Object> map) {
        if (map.size() == 0) {
            return "";
        }
        return unsortedList(map);
    }

    private String unsortedList(Map<String, ? extends Object> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("<ul>");
        for (Map.Entry entry : map.entrySet()) {
            sb.append("<li><code><b>").append(entry.getKey()).append("</b> = ").append(entry.getValue()).append("</code></li>");
        }
        sb.append("</ul>");
        return sb.toString();
    }
}
