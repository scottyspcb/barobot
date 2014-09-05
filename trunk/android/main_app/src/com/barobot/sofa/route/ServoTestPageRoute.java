package com.barobot.sofa.route;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.barobot.common.Initiator;
import com.barobot.common.constant.Constant;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.hardware.devices.i2c.Upanel;
import com.barobot.parser.Queue;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.message.Mainboard;
import com.barobot.parser.utils.Decoder;
import com.barobot.web.route.EmptyRoute;
import com.barobot.web.server.SofaServer;
import com.x5.template.Chunk;
import com.x5.template.Theme;
import com.x5.util.TableData;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;

public class ServoTestPageRoute extends EmptyRoute {

	ServoTestPageRoute(){
		this.regex = "^\\/servo$";
	}
	@Override
	public String run(String url, SofaServer sofaServer, Theme theme,IHTTPSession session) {
		if(theme == null){
			return null;
		}
		if(session.getParms().containsKey("position") && session.getParms().containsKey("servo")&& session.getParms().containsKey("speed")){
			this.use_raw_output = true;
			BarobotConnector barobot	= Arduino.getInstance().barobot;
			String position = session.getParms().get("position");
			String servo = session.getParms().get("servo");
			String speed = session.getParms().get("speed");
			boolean disableOnReady = session.getParms().get("disableOnReady").equals("true");
			int pos		=  Decoder.toInt(position);
			int sp		=  Decoder.toInt(speed);

			Initiator.logger.i( this.getClass().getName(), "run servo " + servo + " position:" +position+ " speed" + speed + " disableOnReady" + session.getParms().get("disableOnReady") );
			Queue q = new Queue();
			if(servo.equals("y")){
				barobot.moveY(q, pos, disableOnReady);
			}else if(servo.equals("z")){
				q.add("Z" + pos+","+sp, true);
				if(disableOnReady){
					q.addWait(300);
					q.add("DZ", true);
				}
			}
			barobot.main_queue.add(q);
			return "OK";
		}else if(session.getParms().containsKey("nalej")){
			int up		= Decoder.toInt(session.getParms().get("UP_POS") );
			int dp		= Decoder.toInt(session.getParms().get("DOWN_POS") );
			int us		= Decoder.toInt(session.getParms().get("UP_SPEED") );
			int ds		= Decoder.toInt(session.getParms().get("DOWN_SPEED") );

			int utime	= Decoder.toInt(session.getParms().get("UP_TIME") );

			int lp		= Decoder.toInt(session.getParms().get("LIGHT_POS") );
			int lt		= Decoder.toInt(session.getParms().get("LIGHT_TIME") );

			int pp		= Decoder.toInt(session.getParms().get("PAC_POS") );
			int pus		= Decoder.toInt(session.getParms().get("PAC_UP_SPEED") );
			int pwdt	= Decoder.toInt(session.getParms().get("PAC_WAIT_DOWN_TIME") );
			int pwut	= Decoder.toInt(session.getParms().get("PAC_WAIT_UP_TIME") );
			int pds		= Decoder.toInt(session.getParms().get("PAC_DOWN_SPEED") );

			int repeat	= Decoder.toInt(session.getParms().get("REPEAT") );
			int wa		= Decoder.toInt(session.getParms().get("WAIT_AFTER") );
			
			Queue q						= new Queue();
			BarobotConnector barobot	= Arduino.getInstance().barobot;

			while( repeat-- >= 0 ){
				q.add("EX", true);
				q.add("Z" + up+","+us, true);		// go up
				q.addWait( lt );

				q.add("Z" + lp+","+us, true);		// go up
				q.addWait( utime );	

				q.add("DY", true);
				q.add("Z" + dp +","+ds, true);		// go down
				q.addWait( pwdt );
				q.add("Z" + pp+","+pus , true);		// do up
				q.addWait( pwut );
				q.add("Z" + dp +","+pds, true);		// go down
				q.addWait( 100 );
				q.add("DX", true);
				q.add("DY", true);
				q.addWait(100);
				q.add("DZ", true);
				if( repeat >= 1 ){
					q.addWait(wa);
				}
			}
			barobot.main_queue.add(q);
			return "OK";
		}else{
			this.use_raw_output = false;
			Chunk action_chunk			= theme.makeChunk("servos#body");
			Map<String, String> positions = new HashMap<String, String>();
			BarobotConnector barobot	= Arduino.getInstance().barobot;
			positions.put("SERVOZ_UP_POS", barobot .state.get("SERVOZ_UP_POS", "1000"));
			positions.put("SERVOZ_UP_LIGHT_POS", barobot .state.get("SERVOZ_UP_LIGHT_POS", "1000"));
			positions.put("SERVOZ_DOWN_POS", barobot .state.get("SERVOZ_DOWN_POS", "1000"));
			positions.put("SERVOZ_PAC_POS", barobot .state.get("SERVOZ_PAC_POS", "1000"));
	
			positions.put("SERVOZ_TEST_POS", barobot .state.get("SERVOZ_TEST_POS", "1000"));	
			positions.put("SERVOY_FRONT_POS", barobot .state.get("SERVOY_FRONT_POS", "1000"));		
			positions.put("SERVOY_BACK_POS", barobot .state.get("SERVOY_BACK_POS", "1000"));
			positions.put("SERVOY_BACK_NEUTRAL", barobot .state.get("SERVOY_BACK_NEUTRAL", "1000"));
	
	    	action_chunk.set("positions", positions );
	    	
	    	int lt = barobot.state.getInt("SERVOZ_POUR_TIME", 20) * 20;
	    	action_chunk.set("UP_POS",				barobot.state.get("SERVOZ_UP_POS", "1000"));
	    	action_chunk.set("LIGHT_POS",			barobot.state.get("SERVOZ_UP_LIGHT_POS", "1000"));
	    	action_chunk.set("DOWN_POS",			barobot.state.get("SERVOZ_DOWN_POS", "1000"));
	    	action_chunk.set("PAC_POS",				barobot.state.get("SERVOZ_PAC_POS", "1000"));
	    	action_chunk.set("PAC_UP_SPEED",		barobot.state.get("DRIVER_Z_SPEED", "1000"));
	    	action_chunk.set("UP_SPEED",			barobot.state.get("DRIVER_Z_SPEED", "1000"));
	    	action_chunk.set("DOWN_SPEED",			barobot.state.get("DRIVER_Z_SPEED", "1000"));
	    	action_chunk.set("PAC_DOWN_SPEED",		barobot.state.get("DRIVER_Z_SPEED", "1000"));


	    	action_chunk.set("UP_TIME", 			lt*3/4 );	
	    	action_chunk.set("LIGHT_TIME", 			lt*1/4 );	
	    	action_chunk.set("PAC_WAIT_DOWN_TIME",	1500);
	    	action_chunk.set("PAC_WAIT_UP_TIME", 	0);

	    	action_chunk.set("REPEAT", 0 );
	    	action_chunk.set("WAIT_AFTER",			barobot.state.get("SERVOY_REPEAT_TIME", "1000") );

	    	return action_chunk.toString();
		}
	}

}
