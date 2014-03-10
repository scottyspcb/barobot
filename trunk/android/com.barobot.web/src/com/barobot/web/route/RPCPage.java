package com.barobot.web.route;

import android.util.Log;
import com.barobot.hardware.virtualComponents;
import com.barobot.utils.Arduino;
import com.barobot.utils.ArduinoQueue;
import com.barobot.web.server.SofaServer;
import com.x5.template.Theme;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;

public class RPCPage extends EmptyRoute {
	public static String regex = "^\\/rpc$";
	
	public RPCPage(String uri) {
		super(uri);
		use_raw_output = true;
	}
	@Override
	public String run(SofaServer sofaServer, Theme theme, IHTTPSession session){
		if(session.getParms().containsKey("command")){
			String command = session.getParms().get("command");
			Log.i("RPCPage command", command);
			if(command.equals( "x10") ){
				Arduino ar		= Arduino.getInstance();
				ArduinoQueue q	= new ArduinoQueue();
				int posx		= virtualComponents.getInt("POSX", 0 );
				virtualComponents.moveZDown( q );
				virtualComponents.moveX( q, ( posx +1000));
				ar.send(q);
				return "OK";
			}else if(command.equals( "x-10") ){
				Arduino ar		= Arduino.getInstance();
				ArduinoQueue q	= new ArduinoQueue();
				int posx		= virtualComponents.getInt("POSX", 0 );
				virtualComponents.moveZDown( q );
				virtualComponents.moveX( q, ( posx -1000));
				ar.send(q);
				return "OK";	
			}
		}
		return "-1";
	}
}
