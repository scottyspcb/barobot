package com.barobot.hardware.serial;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.barobot.common.constant.Constant;
import com.barobot.common.interfaces.HardwareState;
import com.barobot.parser.utils.Decoder;

public class AndroidBarobotState2 implements HardwareState{
	private SharedPreferences.Editor config_editor;			// config systemu android
	private Map<String, String> hashmap = new HashMap<String, String>();
	private SharedPreferences myPrefs;

	private static String[] persistant = {
		"STAT1",
		"POSX",
		"POSY",
		"POSZ",
		"X_GLOBAL_MIN",
		"X_GLOBAL_MAX",
		"LENGTHX",
		"LAST_BT_DEVICE",
		"POS_START_X",
		"POS_START_Y",

		"BOTTLE_OFFSETX_0",
		"BOTTLE_OFFSETX_1",
		"BOTTLE_OFFSETX_2",
		"BOTTLE_OFFSETX_3",
		"BOTTLE_OFFSETX_4",
		"BOTTLE_OFFSETX_5",
		"BOTTLE_OFFSETX_6",
		"BOTTLE_OFFSETX_7",
		"BOTTLE_OFFSETX_8",
		"BOTTLE_OFFSETX_9",
		"BOTTLE_OFFSETX_10",
		"BOTTLE_OFFSETX_11",
		"BOTTLE_OFFSETX_12",

		"BOTTLE_X_0",
		"BOTTLE_X_1",
		"BOTTLE_X_2",
		"BOTTLE_X_3",
		"BOTTLE_X_4",
		"BOTTLE_X_5",
		"BOTTLE_X_6",
		"BOTTLE_X_7",
		"BOTTLE_X_8",
		"BOTTLE_X_9",
		"BOTTLE_X_10",
		"BOTTLE_X_11",
	};
	
	public AndroidBarobotState2( Activity application ){
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
		return Decoder.toInt(get( name, ""+def ), def);
	}
	@Override
	public void set(String name, long value) {
		set(name, "" + value );
	}
	@Override
	public void set( String name, String value ){
		hashmap.put(name, value );
		int remember = Arrays.asList(persistant).indexOf(name);			// czy zapisac w configu tą wartosc?
		if(remember > -1){
			config_editor.putString(name, value);
			config_editor.commit();
		}
	}

	@Override
	public Map<String, String> getAll() {
		Map<String, ?> allEntries 	=  myPrefs.getAll();
		Map<String, String> nMap 	= new HashMap<String, String>();
		for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
		    nMap.put(entry.getKey(), entry.getValue().toString());
		} 
		return nMap;
	}

	@Override
	public void reloadConfig(int robot_Serial) {
		// TODO Auto-generated method stub
	}

	@Override
	public void saveConfig(int robot_Serial) {
		// TODO Auto-generated method stub
		
	}
}
