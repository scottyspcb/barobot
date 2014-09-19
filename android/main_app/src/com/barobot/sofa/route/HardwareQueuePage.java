package com.barobot.sofa.route;


import java.util.LinkedList;
import java.util.ListIterator;

import com.barobot.common.Initiator;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.message.LimitedBuffer;
import com.barobot.web.route.EmptyRoute;
import com.barobot.web.server.SofaServer;
import com.x5.template.Chunk;
import com.x5.template.Theme;
import com.x5.util.DataCapsule;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;

public class HardwareQueuePage extends EmptyRoute{

	HardwareQueuePage(){
		this.regex = "^\\/hardware_queue$";
	}

	@Override
	public String run(String url, SofaServer sofaServer, Theme theme,IHTTPSession session) {
		Chunk action_chunk			= theme.makeChunk("hardware_queue");
    	BarobotConnector barobot	= Arduino.getInstance().barobot;
    	LimitedBuffer<AsyncMessage> h = barobot.main_queue.getHistory();
    	LinkedList<AsyncMessage> ha		= h.getAll();
   

    	String res = "]];";
  
    	for(AsyncMessage y : ha) {
    		String sss = y.render();
    	//	Initiator.logger.i("for", sss);
    		res = sss + "," + res;
    	}
    	res = "[[title,direction,command,unlockingcommand,blocking,name,waitingforme,wasstarted,timeout]," + res;

  //  	action_chunk.set("queue", 		ha );	
   // 	action_chunk.set("history", 	ha );
    	action_chunk.set("res", 		res );
    //	DataCapsule[] widgets = getWidgets();
    //	action_chunk.set("widgets", widgets );

    	Item item = new Item("Best Cat Videos",9.99,"DVD");
    	action_chunk.set("item", item);

    	return action_chunk.toString();
	}
}
