package com.barobot.hardware.devices;

import java.util.HashMap;
import java.util.Map;

import com.barobot.common.Initiator;
import com.barobot.common.interfaces.DefaultState;

public class PcbType implements DefaultState {
	public static final Integer FORWARD = 1;
	public static final Integer BACKWARD = -1;

	BarobotConnector barobot = null;
	Map<String, Integer> myMap = new HashMap<String, Integer>();
	public int type = 2;

	public PcbType(BarobotConnector barobotConnector, int pcb_type) {
		barobot = barobotConnector;
		type = pcb_type;				//barobot.state.getInt("PCB_TYPE", pcbType );
		loadType();
	}
	private void loadType() {
		myMap.put( "PCB_TYPE", type );

		myMap.put("ONCE_PER_APP_START", 0 );
		myMap.put("ONCE_PER_ROBOT_START", 0 );
		myMap.put("ONCE_PER_ROBOT_LIFE", 0 );
		myMap.put("ROBOT_CAN_MOVE", 0 );
		myMap.put("ROBOT_ID", 0 );
		myMap.put("INIT", 0 );		

		myMap.put( "MAX_GLASS_CAPACITY", 190 );

		myMap.put( "SSERVER", 1 );
		myMap.put( "SSERVER_API", 1 );

		myMap.put( "SSERVER_CONFIG_PASS", 1 );
		myMap.put( "SSERVER_ALLOW_CONFIG", 0 );
		myMap.put( "SSERVER_ALLOW_CREATOR", 1 );
		myMap.put( "SSERVER_ALLOW_LIST", 1 );

		myMap.put( "ALLOW_LANGBAR", 1 );
		myMap.put( "NEED_HALL_X", 1 );
		myMap.put( "NEED_HALL_Y", 1 );

		myMap.put( "SERVOZ_UP_TIME_MIN", 250 );
		myMap.put( "SERVOZ_UP_TIME", 1900 );				// czas potrzebny na zajechanie w gore
		myMap.put( "SERVOY_REPEAT_TIME", 600 );
		
		myMap.put( "RESET_TIME", 200 );

		myMap.put( "NEED_GLASS", 0 );
		myMap.put( "WATCH_GLASS", 0 );
		myMap.put( "GLASS_DIFF", 50 );
		myMap.put( "LIGHT_GLASS_DIFF", 5 );
		myMap.put( "ALLOW_LIGHT_CUP", 0 );

		myMap.put( "show_unknown", 1 );
		myMap.put( "show_sending", 1 );
		myMap.put( "show_reading", 1 );
	
		myMap.put( "DRIVER_X_SPEED", 3000);
		myMap.put( "DRIVER_Z_SPEED", 255);
		myMap.put( "DRIVER_CALIB_X_SPEED", 1100);
		myMap.put( "DRIVER_CALIB_X_POS", 20000 );
		myMap.put( "SERVOZ_POUR_TIME", 2500 / 20 );			// predkosc nalewania 20ml, dac 3200		
		myMap.put( "SERVOZ_PAC_TIME_WAIT", 1300 );
		myMap.put( "SERVOZ_PAC_TIME_WAIT_VOL", 300);

		myMap.put( "z_up_step", 20);
		myMap.put( "z_down_step", 20);
		myMap.put( "y_front_step", 15);	
		
		myMap.put( "SERVOZ_PAC_BACK_DIFF", 0 );

		if( type == 1 || type ==2 ){
			myMap.put( "z_pos_known", 0 );
			myMap.put( "y_pos_known", 0 );	
			myMap.put( "DRIVER_Y_SPEED", 25);

			myMap.put( "z_neutral", 1000);
			myMap.put( "z_delta_min", 300);	

			myMap.put( "z_direction", PcbType.FORWARD );
			myMap.put( "z_physical_min", 700);
			myMap.put( "z_physical_max", 2600);	

			
			myMap.put( "y_front_min", 780);	
			myMap.put( "y_front_max", 1200);	
			myMap.put( "y_back_step", 15);	
			myMap.put( "y_back_min", 1600);	
			myMap.put( "y_back_max", 2600);	
			myMap.put( "y_neutral", 1000);	
			myMap.put( "y_delta_min", 300);	
			myMap.put( "y_physical_min", 750);
			myMap.put( "y_physical_max", 2600);	
			myMap.put( "y_direction", PcbType.FORWARD );
			myMap.put( "y_off_timeout", 300);
			myMap.put( "z_off_timeout", 300);
			
			myMap.put( "SERVOY_TEST_POS", 1000);
			myMap.put( "SERVOY_HYSTERESIS", 20 );
			myMap.put( "SERVOZ_HYSTERESIS", 20 );

			myMap.put( "SERVOY_READ_HYSTERESIS", 30 );
			myMap.put( "SERVOZ_READ_HYSTERESIS", 30 );	
			myMap.put( "SERVOY_BACK_NEUTRAL", 1200);
		}

		if( type == 1 ){
			myMap.put( "z_up_min", 1100);
			myMap.put( "z_up_max", 1700);

			myMap.put( "z_down_min", 1700);
			myMap.put( "z_down_max", 2600);
			myMap.put( "z_direction", PcbType.BACKWARD );

			// changed in wizard:
			myMap.put( "SERVOY_FRONT_POS", 790 );		
			myMap.put( "SERVOZ_UP_POS", 2100 );
			myMap.put( "SERVOZ_DOWN_POS", 1250 );	
			myMap.put( "SERVOY_BACK_POS", 2200 );
			myMap.put( "SERVOZ_PAC_POS", 1880 );

			// rest
			myMap.put( "SERVOZ_UP_LIGHT_POS", 2050 );
			myMap.put( "SERVOZ_NEUTRAL", 1100 );

		}else if( type == 2){
			myMap.put( "z_up_min", 1100);
			myMap.put( "z_up_max", 1700);
			myMap.put( "z_direction", PcbType.BACKWARD );
			myMap.put( "y_direction", PcbType.FORWARD );

			myMap.put( "z_down_min", 1700);
			myMap.put( "z_down_max", 2600);
			// default positions (changed in wizard)

			myMap.put( "SERVOZ_PAC_POS", 1550 );
			myMap.put( "SERVOZ_UP_POS", 1400 );
			myMap.put( "SERVOZ_UP_LIGHT_POS", 1400 );
			myMap.put( "SERVOZ_DOWN_POS", 2300 );
			myMap.put( "SERVOZ_NEUTRAL", 2400 );
			myMap.put( "SERVOY_FRONT_POS", 890 );
			myMap.put( "SERVOY_BACK_POS", 1820 );
			myMap.put( "SERVOZ_PAC_BACK_DIFF", 0 );

		}else if( type == 3 ){
			myMap.put( "z_pos_known", 1 );
			myMap.put( "y_pos_known", 1 );

			myMap.put( "z_up_min", 200);
			myMap.put( "z_up_max", 720);
			myMap.put( "z_physical_min", 130);
			myMap.put( "z_physical_max", 720);

			myMap.put( "z_down_min", 130);
			myMap.put( "z_down_max", 720);

			// default positions (changed in wizard)
			myMap.put( "SERVOY_BACK_NEUTRAL", 0);
			myMap.put( "SERVOZ_PAC_POS", 460 );
			myMap.put( "SERVOZ_UP_POS", 600 );
			myMap.put( "SERVOZ_UP_LIGHT_POS", 600 );
			myMap.put( "SERVOZ_DOWN_POS", 140 );
			myMap.put( "SERVOZ_NEUTRAL", 250 );
			myMap.put( "SERVOY_FRONT_POS", 950 );
			myMap.put( "SERVOY_BACK_POS", 20 );
			myMap.put( "DRIVER_Y_SPEED", 255);

			myMap.put( "z_neutral", 250);
			myMap.put( "z_delta_min", 80);	

			myMap.put( "y_front_min", 600);
			myMap.put( "y_front_max", 1000);
			myMap.put( "y_back_step", 15);
			myMap.put( "y_back_min", 0);
			myMap.put( "y_back_max", 300);
			myMap.put( "y_neutral", 500);	
			myMap.put( "y_delta_min", 80);	
			myMap.put( "y_physical_min", 0);
			myMap.put( "y_physical_max", 1010);
			myMap.put( "y_off_timeout", 0 );
			myMap.put( "z_off_timeout", 0 );
			myMap.put( "SERVOY_TEST_POS", 900);
			myMap.put( "SERVOY_HYSTERESIS", 0 );
			myMap.put( "SERVOZ_HYSTERESIS", 16 );
			myMap.put( "SERVOY_READ_HYSTERESIS", 20 );
			myMap.put( "SERVOZ_READ_HYSTERESIS", 20 );
			myMap.put( "SERVOZ_PAC_BACK_DIFF", 5 );			// add 5
		}
	}
	@Override
	public String getDefault(String key, String def){
		if( myMap.containsKey(key)){
			return ""+myMap.get(key);
		}else{
			return def;
		}
	}
	@Override
	public int getDefault(String key, int def) {
		if( myMap.containsKey(key)){
			return myMap.get(key);
		}else{
			return def;
		}
	}

	@Override
	public int getDefault(String key){
		if( myMap.containsKey(key)){
			return myMap.get(key);
		}else{
			Initiator.logger.e("PcbType no config", "key = " + key );
			return 0;
		}
	}

}
