package com.barobot.sofa.route;

import android.content.Context;

import com.barobot.BarobotMain;
import com.barobot.R;
import com.barobot.common.Initiator;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.sofa.api.JsonResponseBuilder;
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
		Context c			= BarobotMain.getInstance().getApplicationContext();
		String msg_list		= c.getResources().getString(R.string.menu_choose);
		String msg_crator	= c.getResources().getString(R.string.menu_create);
		String msg_settings	= c.getResources().getString(R.string.action_settings);

		Chunk action_chunk			= theme.makeChunk("start_page#mainpage");
		action_chunk.set("msg_list", msg_list );
		action_chunk.set("msg_crator", msg_crator );
		action_chunk.set("msg_settings", msg_settings );

		BarobotConnector barobot = Arduino.getInstance().barobot;
		int allow_config	= barobot.state.getInt("SSERVER_ALLOW_CONFIG", 0);
		int allow_list		= barobot.state.getInt("SSERVER_ALLOW_LIST", 0);
		int allow_creator	= barobot.state.getInt("SSERVER_ALLOW_CREATOR", 0);

		Initiator.logger.i("MainPage.run.allow_config", ""+allow_config);

		action_chunk.set("allow_config", allow_config );	
		action_chunk.set("allow_list", allow_list );
		action_chunk.set("allow_creator", allow_creator );

		/*		Map<String, List<String>> decodedQueryParameters =sofaServer.decodeParameters(session.getQueryParameterString());

    	StringBuilder sb = new StringBuilder(); 

        sb.append("<h3>Parms</h3><p><blockquote>").
              append(toString(session.getParms())).append("</blockquote></p>");

        action_chunk.set("body2", sb.toString() );*/
    	return action_chunk.toString();
	}

/*
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
*/
}
