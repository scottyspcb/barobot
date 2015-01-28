package com.barobot.sofa.route;

import java.io.File;

import android.os.Environment;

import com.barobot.common.Initiator;
import com.barobot.common.constant.Constant;
import com.barobot.web.route.EmptyRoute;
import com.barobot.web.server.SofaServer;
import com.x5.template.Chunk;
import com.x5.template.Theme;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;

public class LogPage extends EmptyRoute{
	public LogPage() {
		use_raw_output = false;	
		this.regex = "^\\/log$";
	}
	public String run(String url, SofaServer sofaServer, Theme theme, IHTTPSession session){
		if(theme == null){
			return null;
		}
		Chunk action_chunk	= theme.makeChunk("log#body");
		String clear		= session.getParms().get("clear");
		if("true".equals(clear)){
			String path6 = 	Environment.getExternalStorageDirectory()+ Constant.logFile;
			File fl = new File(path6);
			fl.delete();
		}
		String data = Initiator.logger.getSavedLog();
		action_chunk.set("data", data );  
    	return action_chunk.toString();
	}
}
