package com.barobot.android;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.barobot.activity.DebugActivity;
import com.barobot.common.constant.Constant;
import com.barobot.common.interfaces.HardwareState;
import com.barobot.parser.utils.Decoder;

public class AndroidBarobotState implements HardwareState{
	private SharedPreferences.Editor config_editor;			// config systemu android
	private Map<String, String> hashmap = new HashMap<String, String>();
	private SharedPreferences myPrefs;

	private static String[] persistant = {
		"POSX",
		"POSY",
		"POSY",
		"X_GLOBAL_MIN",
		"X_GLOBAL_MAX",
		"LENGTHX","LAST_BT_DEVICE",
		"POS_START_X",
		"POS_START_Y",
		"NEUTRAL_POS_Y",
		"NEUTRAL_POS_Z",
		"ENDSTOP_X_MIN",
		"ENDSTOP_X_MAX",
		"ENDSTOP_Y_MIN",
		"ENDSTOP_Y_MAX",
		"ENDSTOP_Z_MIN",
		"ENDSTOP_Z_MAX",
		"BOTTLE_X_0","BOTTLE_Y_0",
		"BOTTLE_X_1","BOTTLE_Y_1",
		"BOTTLE_X_2","BOTTLE_Y_2",
		"BOTTLE_X_3","BOTTLE_Y_3",
		"BOTTLE_X_4","BOTTLE_Y_4",
		"BOTTLE_X_5","BOTTLE_Y_5",
		"BOTTLE_X_6","BOTTLE_Y_6",
		"BOTTLE_X_7","BOTTLE_Y_7",
		"BOTTLE_X_8","BOTTLE_Y_8",
		"BOTTLE_X_9","BOTTLE_Y_9",
		"BOTTLE_X_10","BOTTLE_Y_10",
		"BOTTLE_X_11","BOTTLE_Y_11",
	};
	
	public AndroidBarobotState( Activity application ){
		myPrefs			= application.getSharedPreferences(Constant.SETTINGS_TAG, Context.MODE_PRIVATE);
		config_editor	= myPrefs.edit();
	}

	@Override
	public String get( String name, String def ){
		String ret = hashmap.get(name);
		if( ret == null ){ 
			if((Arrays.asList(persistant).indexOf(name) > -1 )){
				ret = myPrefs.getString(name, def );
			}else{
				ret = def;
			}
		}
		return ret;
	}

	@Override
	public int getInt( String name, int def ){
		return Decoder.toInt(get( name, ""+def ));
	}
	@Override
	public void set(String name, long value) {
		set(name, "" + value );
	}
	@Override
	public void set( String name, String value ){
	//	if(name == "POSX"){
	//		Initiator.logger.i("virtualComponents.set","save: "+name + ": "+ value );	
	//	}
		hashmap.put(name, value );
		update( name, value );
		int remember = Arrays.asList(persistant).indexOf(name);			// czy zapisac w configu tą wartosc?
		if(remember > -1){
			config_editor.putString(name, value);
			config_editor.commit();
		}
	}
	private void update(String name, String value) {
		final DebugActivity dialog = DebugActivity.getInstance();
		if(dialog!=null){
			dialog.update(name, value );
		}
	}
}
