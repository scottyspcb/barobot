package com.barobot.sofa.route;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.orman.mapper.Model;
import org.orman.mapper.ModelQuery;
import org.orman.sql.C;

import com.barobot.AppInvoker;
import com.barobot.common.Initiator;
import com.barobot.common.constant.Constant;
import com.barobot.gui.dataobjects.Liquid_t;
import com.barobot.gui.dataobjects.Recipe_t;
import com.barobot.gui.dataobjects.Type;
import com.barobot.gui.utils.LangTool;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.other.Android;
import com.barobot.other.Audio;
import com.barobot.parser.Queue;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.message.Mainboard;
import com.barobot.parser.utils.Decoder;
import com.barobot.web.route.EmptyRoute;
import com.barobot.web.server.SofaServer;
import com.x5.template.Theme;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;

public class CommandRoute extends EmptyRoute {
	private String prefix;

	CommandRoute(){
		this.regex = "^\\/command\\/.*";
		this.prefix = "/command/";
		use_raw_output = true;
		this.init();
	}

	private class command_listener {
		public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,
				int posx, int posy) {
			Initiator.logger.i("CommandRoute", " no listener: command_listener");
			return false;
		}
	}

	private static Map<String, command_listener> index = new HashMap<String, command_listener>();

	private static Audio getAudio() {
		Audio a = (Audio) AppInvoker.container.get("Audio");
		if (a == null) {
			a = new Audio();
			AppInvoker.container.put("Audio", a);
		}
		return a;
	}

	public static Map<String, String> geCommands() {
		Map<String, String> index2 = new HashMap<String, String>();
		for (Entry<String, command_listener> entry : index.entrySet()) {
			String translation = LangTool.translateName( entry.getKey().hashCode(), "command", entry.getKey() );
			index2.put(entry.getKey(), translation);
		}
		return index2;
	}

	@Override
	public String run(String url, SofaServer sofaServer, Theme theme,
			IHTTPSession session) {
		String url2 = url.replace(prefix, "");
		boolean ret = runCommand(url2);
		return ret ? "OK":"ERROR";
	}

	public static boolean runCommand( String command ){
		Queue q			= new Queue();
		BarobotConnector barobot = Arduino.getInstance().barobot;
		Queue mq		= barobot.main_queue;
		int posx		= barobot.driver_x.getSPos();
		int posy		= barobot.state.getInt("POSY", 0 );
		command_listener listener = index.get(command);
		if(listener!=null){
			Initiator.logger.i("CommandRoute", "run command: "+ command);	
			boolean res = listener.onCall(q, barobot, mq, posx, posy);
			mq.add(q);
			return res;
		}else{
			Initiator.logger.i("CommandRoute", "brak  listener: "+ command);	
		}
		return false;
	}
	public static boolean runCommand2( String command ){
		return runCommand("command_" + command );
	}

	private void init() {
		index.put("system_test", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq, int posx, int posy) {
				barobot.systemTest();
				return true;
			}
		});

		index.put("command_clear_queue", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,
					int posx, int posy) {
				mq.clear();
				return true;
			}
		});

		index.put("command_reset_serial", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,
					int posx, int posy) {
				mq.clear();
				Arduino.getInstance().resetSerial();
				return true;
			}
		});

		index.put("command_set_x_1000", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,
					int posx, int posy) {
				// Log.i("nextpos-10000", "old: "+posx + " next: "+ ( posx
				// -10000));
				barobot.moveZDown(q, true);
				barobot.driver_x.moveTo(q, (posx - 10000));
				return true;
			}
		});

		index.put("command_set_x_100", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,
					int posx, int posy) {
				// Log.i("nextpos-1000", "old: "+posx + " next: "+ ( posx
				// -1000));
				barobot.moveZDown(q, true);
				barobot.driver_x.moveTo(q, (posx - 1000));
				return true;
			}
		});

		index.put("command_set_x_10", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,
					int posx, int posy) {
				// Log.i("nextpos-100", "old: "+posx + " next: "+ ( posx -100));
				barobot.moveZDown(q, true);
				barobot.driver_x.moveTo(q, (posx - 100));
				return true;
			}
		});
		index.put("command_set_x10", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,
					int posx, int posy) {
				// Log.i("nextpos+100", "old: "+posx + " next: "+ ( posx +100));
				barobot.moveZDown(q, true);
				barobot.driver_x.moveTo(q, (posx + 100));
				return true;
			}
		});

		index.put("command_set_x100", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,
					int posx, int posy) {
				// Log.i("nextpos+1000", "old: "+posx + " next: "+ ( posx
				// +1000));
				barobot.moveZDown(q, true);
				barobot.driver_x.moveTo(q, (posx + 1000));
				return true;
			}
		});

		index.put("command_set_x1000", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,
					int posx, int posy) {
				// Log.i("nextpos+10000", "old: "+posx + " next: "+ ( posx
				// +10000));
				barobot.moveZDown(q, true);
				barobot.driver_x.moveTo(q, (posx + 10000));
				return true;
			}
		});

		index.put("command_set_y_600", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,
					int posx, int posy) {
				barobot.moveZDown(q, true);
				barobot.moveY(q, (posy - 1000), true);
				return true;
			}
		});

		index.put("command_set_y_100", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,
					int posx, int posy) {
				barobot.moveZDown(q, true);
				barobot.moveY(q, (posy - 100), true);
				return true;
			}
		});

		index.put("command_set_y_10", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,
					int posx, int posy) {
				barobot.moveZDown(q, true);
				barobot.moveY(q, (posy - 10), true);
				return true;
			}
		});

		index.put("command_set_y10", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,
					int posx, int posy) {
				barobot.moveZDown(q, true);
				barobot.moveY(q, (posy + 10), true);
				return true;
			}
		});

		index.put("command_set_y100", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,
					int posx, int posy) {
				barobot.moveZDown(q, true);
				barobot.moveY(q, (posy + 100), true);
				return true;
			}
		});

		index.put("command_set_y600", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,
					int posx, int posy) {
				barobot.moveZDown(q, true);
				barobot.moveY(q, (posy + 1000), true);
				return true;
			}
		});

		index.put("command_go_to_neutral_y", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,int posx, int posy) {
				barobot.moveY(q, barobot.state.get("SERVOY_BACK_NEUTRAL", "0"));
				barobot.disabley( q );
				return true;
			}
		});

		index.put("command_kalibrujy", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,
					int posx, int posy) {
				barobot.moveZDown(q, true);
				barobot.moveY(q, 900, false);
				barobot.moveY(q, 2100, false);
				barobot.moveY(q, 900, false);
				return true;
			}
		});

		index.put("command_read_hall_x", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,
					int posx, int posy) {
	
				q.add("A0", true);
				return true;
			}
		});
		index.put("command_read_hall_y", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,
					int posx, int posy) {
	
				q.add("A1", true);
				return true;
			}
		});		
		index.put("command_read_weight", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,
					int posx, int posy) {
				q.add("A2", true);
				return true;
			}
		});

		index.put("command_machajx", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,
					int posx, int posy) {
				barobot.moveZDown(q, true);

				int SERVOY_FRONT_POS = barobot.state.getInt("SERVOY_FRONT_POS",
						1000);

				barobot.moveY(q, SERVOY_FRONT_POS, true);
				int lengthx4 = barobot.state.getInt("LENGTHX", 600);
				for (int i = 0; i < 10; i++) {
					// virtualComponents.moveX( q, (lengthx4/4) );
					// virtualComponents.moveX( q, (lengthx4/4 * 3) );
					barobot.driver_x.moveTo(q, 0);
					q.addWait(50);
					barobot.driver_x.moveTo(q, lengthx4);
				}
				q.add("DX", true);
				return true;
			}
		});
		index.put("command_machajy", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,
					int posx, int posy) {
				barobot.moveZDown(q, true);

				int SERVOY_FRONT_POS2 = barobot.state.getInt(
						"SERVOY_FRONT_POS", 0);
				int SERVOY_BACK_POS = barobot.state.getInt("SERVOY_BACK_POS",
						0);

				for (int i = 0; i < 10; i++) {
					barobot.moveY(q, SERVOY_FRONT_POS2, false);
					barobot.moveY(q, SERVOY_BACK_POS, false);
				}

				barobot.moveY(q, SERVOY_FRONT_POS2, false);
				barobot.disabley( q );
				return true;
			}
		});

		index.put("command_machajz", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,
					int posx, int posy) {
				for (int i = 0; i < 10; i++) {
					barobot.moveZDown(q, true);
					barobot.moveZUp(q, -1, true);
				}
				barobot.moveZDown(q, true);
				return true;
			}
		});

		index.put("command_losujx", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,
					int posx, int posy) {
				Random generator2 = new Random(19580427);
				barobot.moveZDown(q, true);
				int SERVOY_FRONT_POS3 = barobot.state.getInt(
						"SERVOY_FRONT_POS", 1000);
				barobot.moveY(q, SERVOY_FRONT_POS3, true);
				int lengthx5 = barobot.state.getInt("LENGTHX", 600);
				for (int f = 0; f < 20;) {
					int left = generator2.nextInt((int) (lengthx5 / 100 / 2));
					int right = generator2.nextInt((int) (lengthx5 / 100 / 2));
					right += lengthx5 / 100 / 2;
					barobot.driver_x.moveTo(q, (left * 100));
					barobot.driver_x.moveTo(q, (right * 100));
					f = f + 2;
				}
				return true;
			}
		});

		index.put("command_fill5000", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,
					int posx, int posy) {
				barobot.pour(q, 20, -1, true);
				return true;
			}
		});

		index.put("command_max_z", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,
					int posx, int posy) {
				barobot.moveZUp(q, -1, true);
				return true;
			}
		});

		index.put("command_min_z", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,
					int posx, int posy) {

				barobot.moveZDown(q, true);
				return true;
			}
		});

		index.put("command_max_x", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,
					int posx, int posy) {
				barobot.moveZDown(q, true);
				int lengthx2 = barobot.state.getInt("LENGTHX", 600);
				barobot.driver_x.moveTo(q, posx + lengthx2);
				return true;
			}
		});

		index.put("command_max_y", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,
					int posx, int posy) {
				barobot.moveZDown(q, true);
				int SERVOY_BACK_POS2 = barobot.state.getInt("SERVOY_BACK_POS",1000);
				barobot.moveY(q, SERVOY_BACK_POS2, true);
				return true;
			}
		});

		index.put("command_min_x", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,
					int posx, int posy) {
				barobot.moveZDown(q, true);
				int lengthx3 = barobot.state.getInt("LENGTHX", 600);
				barobot.driver_x.moveTo(q, -lengthx3);
				return true;
			}
		});

		index.put("command_min_y", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,
					int posx, int posy) {

				barobot.moveZDown(q, true);
				int SERVOY_FRONT_POS5 = barobot.state.getInt(
						"SERVOY_FRONT_POS", 1000);
				barobot.moveY(q, SERVOY_FRONT_POS5, true);
				return true;
			}
		});

		index.put("command_unlock", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,
					int posx, int posy) {
				mq.unlock();
				return true;
			}
		});

		index.put("command_disablex", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,
					int posx, int posy) {
				q.add("DX", true);
				return true;
			}
		});		
		index.put("command_disablez", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,
					int posx, int posy) {
				barobot.disablez(q);
				return true;
			}
		});

		index.put("command_disabley", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,
					int posx, int posy) {
				barobot.disabley( q );
				return true;
			}
		});

		index.put("command_enablez", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,
					int posx, int posy) {
				q.add("EZ", true);
				return true;
			}
		});
		index.put("command_set_robot_id", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,
					int posx, int posy) {
	
				/*
				// read from db
				Query query3 = new Query("SELECT count(*) FROM `robot`");
				Initiator.logger.i("StartOrmanMapping","query3: "+ query3.toString()); 
				Object res = BarobotData.omdb.getExecuter().executeForSingleValue(query3);
				if(res == null){
					Initiator.logger.i("StartOrmanMapping","results: null"); 
				}else{
					Initiator.logger.i("command_set_robot_id","onCall: "+ res); 
					int rc = Decoder.toInt(""+res, -1);
					if( rc != -1){
						barobot.setRobotId( mq,rc);
					}
				}
*/
				return true;
			}
		});
		index.put("command_reset", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,
					int posx, int posy) {
				mq.add("RESET", false);
				return true;
			}
		});

		index.put("command_reset1", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,
					int posx, int posy) {
				mq.add("RESET1", false);
				return true;
			}
		});

		index.put("command_reset2", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,
					int posx, int posy) {
				mq.add("RESET2", true);
				return true;
			}
		});

		index.put("command_reset3", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,
					int posx, int posy) {
				mq.add("RESET3", true);
				return true;
			}
		});

		index.put("command_reset4", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,
					int posx, int posy) {
				mq.add("RESET4", true);
				return true;
			}
		});

		index.put("command_rb", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,
					int posx, int posy) {
				mq.add("RB", true);
				return true;
			}
		});

		index.put("command_rb2", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,
					int posx, int posy) {
				mq.add("RB2", false);
				return true;
			}
		});

		index.put("command_scann_leds", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,
					int posx, int posy) {
				barobot.lightManager.scann_leds(q);
				return true;
			}
		});

		index.put("command_led_green_on", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,
					int posx, int posy) {
				barobot.lightManager.setAllLeds(q, "22",255, 0, 255,0);
				return true;
			}
		});

		index.put("command_led_blue_on", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,
					int posx, int posy) {
				barobot.lightManager.setAllLeds(q, "44",255, 0, 0,255);
				return true;
			}
		});
		index.put("command_led_off", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,
					int posx, int posy) {
				barobot.lightManager.turnOffLeds(mq);
				return true;
			}
		});	
		
		

		index.put("command_led_red_on", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,
					int posx, int posy) {
				barobot.lightManager.setAllLeds(q, "11",255, 255, 0,0);
				return true;
			}
		});

		index.put("command_reset_margin", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,
					int posx, int posy) {
				barobot.driver_x.setMargin(0);
				barobot.driver_x.setHPos(0);
				return true;
			}
		});

		index.put("command_analogs", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,
					int posx, int posy) {

				Android.readTabletTemp( mq );
				mq.add("x", true);
				mq.add("y", true);
				mq.add("z", true);
				mq.addWithDefaultReader("S");		// temp
				mq.add("A0", true);		// hall x
				mq.add("A1", true);		// hall y
				mq.add("A2", true);		// load cell
				return true;
			}
		});

		index.put("command_move_to_start", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,
					int posx, int posy) {
				barobot.moveToStart( q );
				return true;
			}
		});

		index.put("command_homing", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,
					int posx, int posy) {
				barobot.readHardwareRobotId(q);
				barobot.doHoming(q, true);
				return true;
			}
		});

		index.put("command_options_lights_off", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,
					int posx, int posy) {
				final Audio a = getAudio();
				if (a.isRunning()) {
					Initiator.logger.i( this.getClass().getName(), "getAudio stop1");
					a.stop();
				} else {
					Initiator.logger.i( this.getClass().getName(), "getAudio start");
					a.start(barobot);
					barobot.main_queue.clear();
					barobot.main_queue.unlock();
				}
				return true;
			}
		});

		index.put("command_options_lights_on", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,
					int posx, int posy) {
				final Audio a4 = getAudio();
				if (!a4.isRunning()) {
					Initiator.logger.i( this.getClass().getName(), "getAudio start");
					a4.start(barobot);
				}
				return true;
			}
		});

		index.put("command_find_bottles", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,
					int posx, int posy) {

				barobot.kalibrcja( q );
				return true;
			}
		});

		index.put("command_demo", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,
					int posx, int posy) {
				barobot.startDemo();
				return true;
			}
		});

		index.put("command_index_names", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq, int posx, int posy) {
				Initiator.logger.i(Constant.TAG,"index_names");
				List<Liquid_t> hh =  Model.fetchQuery(ModelQuery.select().from(Liquid_t.class).getQuery(),Liquid_t.class);
				for(Liquid_t liquid : hh)
				{
					boolean exists = LangTool.checkIsTranslated(liquid.id, "liquid", liquid.name);
					if(!exists){
						LangTool.InsertTranslation( liquid.id, "liquid", liquid.name );
					}
				}
				Initiator.logger.i(Constant.TAG,"tłumaczenie liquid" + hh.size());

				List<Type> hh2 =  Model.fetchQuery(ModelQuery.select().from(Type.class).getQuery(),Type.class);
				for(Type tt2 : hh2)
				{
					boolean exists = LangTool.checkIsTranslated(tt2.id, "type", tt2.name);
					if(!exists){
						LangTool.InsertTranslation( tt2.id, "type", tt2.name );
					}
				}
				Initiator.logger.i(Constant.TAG,"tłumaczenie type" + hh2.size());

				List<Recipe_t> hh3 =  Model.fetchQuery(ModelQuery.select().from(Recipe_t.class).where(C.eq("unlisted", false)).getQuery(),Recipe_t.class);
				for(Recipe_t tt3 : hh3)
				{
					boolean exists = LangTool.checkIsTranslated(tt3.id, "recipe", tt3.name);
					if(!exists){
						LangTool.InsertTranslation( tt3.id, "recipe", tt3.name );
					}
				}
				Initiator.logger.i(Constant.TAG,"tłumaczenie recipe" + hh3.size());

				for (Entry<String, command_listener> entry : index.entrySet()) {
					boolean exists = LangTool.checkIsTranslated( entry.getKey().hashCode(), "command", entry.getKey() );
					if(!exists){
						LangTool.InsertTranslation( entry.getKey().hashCode(), "command", entry.getKey() );
					}
				}
				return true;
			}
		});

		// TODO
		index.put("command_auto_repair", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,int posx, int posy) {
				mq.unlock();
				// send new line
				// recieve response
				// if not = error 1
				// if wrong error 2
				// send get analog
				// if no response = error 3
				// if wrong = error 4

				// if error = reset MB & CARRET
				return true;
			}
		});

		index.put("command_wait_for_cup", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,int posx, int posy) {

				Initiator.logger.i("cupFound"," start ");
				final Queue q2			= new Queue();
				
				final Queue q_ready		= new Queue();
				barobot.lightManager.carret_color( q_ready, 0, 100, 100 );
				
				final long timestamp	= Decoder.getTimestamp();
				barobot.lightManager.carret_color( q2, 100, 100, 0);
				q2.addWait(100);
				q2.add("A2", true );	// read load cell
				barobot.lightManager.carret_color( q2, 0, 100, 100 );
				q2.addWait(100);

				AsyncMessage am = new AsyncMessage( true ) {
					@Override
					public String getName() {
						return "Check load cell";
					}
					@Override
					public Queue run(Mainboard dev, Queue mainQueue) {
						boolean cupFound = false;
						Initiator.logger.i("cupFound"," test ");
						long timestamp2 = Decoder.getTimestamp();
						if (timestamp2 - timestamp > 7000 ){			// 5 sec
							cupFound = true;
						}
						if( cupFound ){
							Initiator.logger.i("cupFound"," true ");
						}else{
							Initiator.logger.i("cupFound"," false ");
							return q2.copy();		// do it one more time (now with addWait())
						}
						return null;
					}
				};
				q2.add(am);				// check value

				q.add(q2.copy());		// this queue content will be send

				q2.addWait(2000);		// add to q2 (not to q). Wait will be used in AsyncMessage before next
				barobot.lightManager.carret_color( q2, 0, 0, 0 );

				return true;
			}
		});

		index.put("command_repair_ingredients", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,int posx, int posy) {
				return true;
			}
		});


		index.put("command_bottle_0", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,int posx, int posy) {
				Android.pourFromBottle( 0, q );
				return true;
			}
		});
		index.put("command_bottle_1", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,int posx, int posy) {
				Android.pourFromBottle( 1, q );
				return true;
			}
		});	
		index.put("command_bottle_2", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,int posx, int posy) {
				Android.pourFromBottle( 2, q );
				return true;
			}
		});		
		index.put("command_bottle_3", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,int posx, int posy) {
				Android.pourFromBottle( 3, q );
				return true;
			}
		});	
		index.put("command_bottle_4", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,int posx, int posy) {
				Android.pourFromBottle( 4, q );
				return true;
			}
		});	
		index.put("command_bottle_5", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,int posx, int posy) {
				Android.pourFromBottle( 5, q );
				return true;
			}
		});	
		index.put("command_bottle_6", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,int posx, int posy) {
				Android.pourFromBottle( 6, q );
				return true;
			}
		});		
		index.put("command_bottle_7", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,int posx, int posy) {
				Android.pourFromBottle( 7, q );
				return true;
			}
		});		
		index.put("command_bottle_8", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,int posx, int posy) {
				Android.pourFromBottle( 8, q );
				return true;
			}
		});	
		index.put("command_bottle_8", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,int posx, int posy) {
				Android.pourFromBottle( 8, q );
				return true;
			}
		});		
		index.put("command_bottle_10", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,int posx, int posy) {
				Android.pourFromBottle( 10, q );
				return true;
			}
		});	
		index.put("command_bottle_11", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,int posx, int posy) {
				Android.pourFromBottle( 11, q );
				return true;
			}
		});	

		index.put("command_test_queue", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,int posx, int posy) {
				int pos = 100;
				for(int i=0;i<5;i++){
					barobot.doHoming(q, true);
				}
				barobot.driver_x.moveTo(q, 1000);
				for(int i=0;i<90;i++){
					q.add("X"+pos, true);
					pos +=100;
				}
				barobot.driver_x.moveTo(q, 1000);
				barobot.driver_x.moveTo(q, 10000);

				for(int i=0;i<90;i++){
					q.add("RESET2", true);
				}
				for(int i=0;i<90;i++){
					q.add("RESET3", true);
				}
				for(int i=0;i<90;i++){
					q.add("RESET4", true);
				}
				for(int i=0;i<10;i++){
					q.add("EX", true);
					q.addWait(400);
					q.add("DX", true);
				}
				for(int i=0;i<10;i++){
					barobot.doHoming(q, true);
				}
				return true;
			}
		});

		// TODO
		index.put("command_pour_now", new command_listener() {
			@Override
			public boolean onCall(Queue q, BarobotConnector barobot, Queue mq,int posx, int posy) {
				return true;
			}
		});
	}
}