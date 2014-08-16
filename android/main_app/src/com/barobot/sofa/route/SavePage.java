package com.barobot.sofa.route;

import org.orman.mapper.Model;
import org.orman.mapper.ModelQuery;
import org.orman.sql.C;

import android.util.Log;

import com.barobot.gui.dataobjects.Translated_name;
import com.barobot.gui.dataobjects.Type;
import com.barobot.gui.utils.LangTool;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.web.route.EmptyRoute;
import com.barobot.web.server.SofaServer;
import com.x5.template.Theme;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;

public class SavePage extends EmptyRoute {

	public SavePage() {
		use_raw_output = true;
		this.regex = "^\\/save$";
	}

	@Override
	public String run(String url, SofaServer sofaServer, Theme theme, IHTTPSession session){
		Log.i("RPCPage command", url);
		if(session.getParms().containsKey("id") && session.getParms().containsKey("name")&& session.getParms().containsKey("type")){
			String id = session.getParms().get("id");
			String type = session.getParms().get("type");
			String value = session.getParms().get("name");
			Class<Translated_name> t = null;
			if( type.equals( "Translated_name")){
				t = Translated_name.class;
				Translated_name item;
				if( t != null ){
					item = Model.fetchSingle(ModelQuery.select().from(t).where(C.eq("id", id)).getQuery(), t);
					if(item != null ){
						item.translated = value;
						item.update();
						LangTool.resetCache(item.id, item.language_id, item.table_name);
						return "OK";
					}else{
						return "ERROR3";
					}
				}	
			}else if( type.equals( "options")){
				BarobotConnector barobot = Arduino.getInstance().barobot;
				barobot.state.set(id, value);
				return "OK";
			}
			return "ERROR2";	
		}
		return "ERROR1";
	}
}
