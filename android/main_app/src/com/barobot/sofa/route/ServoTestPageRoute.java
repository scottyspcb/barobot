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

			this.nalej(session.getParms());
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

	    	action_chunk.set("DRIVER_Y_SPEED",		barobot.state.get("DRIVER_Y_SPEED", "1000"));
	    	action_chunk.set("DRIVE_X",				"0");
	    	action_chunk.set("SERVOY_FRONT_POS",	barobot.state.get("SERVOY_FRONT_POS", "1000"));
	    	action_chunk.set("SERVOY_BACK_POS",		barobot.state.get("SERVOY_BACK_POS", "1000"));
	    	action_chunk.set("DRIVER_Y_SPEED",		barobot.state.get("DRIVER_Y_SPEED", "1000"));
	    	action_chunk.set("XPOS1",				"0");
	    	action_chunk.set("REPEAT_Z",			"0");
	    	action_chunk.set("REPEAT_X",			"0");
	    	action_chunk.set("DRIVER_X_SPEED",		barobot.state.get("DRIVER_X_SPEED", "1000"));


	    	action_chunk.set("UP_TIME", 			lt*3/4 );	
	    	action_chunk.set("LIGHT_TIME", 			lt*1/4 );	
	    	action_chunk.set("PAC_WAIT_DOWN_TIME",	1500);
	    	action_chunk.set("PAC_WAIT_UP_TIME", 	0);

	    	action_chunk.set("REPEAT", 0 );
	    	action_chunk.set("WAIT_AFTER",			barobot.state.get("SERVOY_REPEAT_TIME", "1000") );

	    	return action_chunk.toString();
		}
	}
	private void nalej(Map<String, String> parms) {
		int up		= Decoder.toInt(parms.get("UP_POS") );
		int dp		= Decoder.toInt(parms.get("DOWN_POS") );
		int us		= Decoder.toInt(parms.get("UP_SPEED") );
		int ds		= Decoder.toInt(parms.get("DOWN_SPEED") );

		int utime	= Decoder.toInt(parms.get("UP_TIME") );

		int lp		= Decoder.toInt(parms.get("LIGHT_POS") );
		int lt		= Decoder.toInt(parms.get("LIGHT_TIME") );

		int pp		= Decoder.toInt(parms.get("PAC_POS") );
		int pus		= Decoder.toInt(parms.get("PAC_UP_SPEED") );
		int pwdt	= Decoder.toInt(parms.get("PAC_WAIT_DOWN_TIME") );
		int pwut	= Decoder.toInt(parms.get("PAC_WAIT_UP_TIME") );
		int pds		= Decoder.toInt(parms.get("PAC_DOWN_SPEED") );

		int repeatx	= Decoder.toInt(parms.get("REPEAT_X") );
		int repeatz	= Decoder.toInt(parms.get("REPEAT_Z") );
		int wa		= Decoder.toInt(parms.get("WAIT_AFTER") );

		int dx		= Decoder.toInt(parms.get("DRIVE_X") );
		int sfp		= Decoder.toInt(parms.get("SERVOY_FRONT_POS") );
		int sbp		= Decoder.toInt(parms.get("SERVOY_BACK_POS") );
		int xpos1	= Decoder.toInt(parms.get("XPOS1") );
		int dys		= Decoder.toInt(parms.get("DRIVER_Y_SPEED") );
		int dxs		= Decoder.toInt(parms.get("DRIVER_X_SPEED") );
		

		Queue q						= new Queue();
		BarobotConnector barobot	= Arduino.getInstance().barobot;

		if(dx > 0 ){							// go to front
			q.add("Z" + dp +","+ds, true);		// go down
			q.add("DZ", true);
			this.goFront(q, parms);
		}
		boolean yIsFront = true;
		while( repeatx-- >= 0 ){
			while( repeatz-- >= 0 ){
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
				q.add("DZ", true);
				q.addWait(100);
				q.add("DX", true);
		//		q.add("DY", true);
				if( repeatz >= 1 ){
					q.addWait(wa);
				}
			}
			if(dx > 0 ){
				if(yIsFront){
					this.moveX(q, parms, true );
					this.goBack(q, parms);
				}else{
					this.goFront(q, parms);
					this.moveX(q, parms, false );
				}
				yIsFront = !yIsFront;
			}else{
				break;
			}
		}

		if(dx > 0 ){					// go to front
			this.goFront(q, parms);
		}
		barobot.main_queue.add(q);
	}

	private void moveX(Queue q, Map<String, String> parms, boolean add) {
		int xpos1	= Decoder.toInt(parms.get("XPOS1") );		
		int dxs		= Decoder.toInt(parms.get("DRIVER_X_SPEED") );

		BarobotConnector barobot	= Arduino.getInstance().barobot;
		int poshx = barobot.driver_x.getHardwarePos();
		int newpos = add ? poshx+ xpos1 : poshx - xpos1;

		q.add("X"+ (newpos) +","+ dxs, true);
	
	}
	private void goBack(Queue q, Map<String, String> parms) {
		int sfp		= Decoder.toInt(parms.get("SERVOY_FRONT_POS") );
		int dys		= Decoder.toInt(parms.get("DRIVER_Y_SPEED") );
		q.add("Y" + sfp +","+dys, true);	// go back
		q.add("DY", true);
	}

	private void goFront(Queue q, Map<String, String> parms) {
		int dys		= Decoder.toInt(parms.get("DRIVER_Y_SPEED") );
		int sbp		= Decoder.toInt(parms.get("SERVOY_BACK_POS") );
		q.add("Y" + sbp +","+dys, true);	// go front
		q.add("DY", true);
	}
}
