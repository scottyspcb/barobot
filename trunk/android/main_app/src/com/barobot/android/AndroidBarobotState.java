package com.barobot.android;

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
	private SharedPreferences myPrefs;

	public AndroidBarobotState( Activity application ){
		myPrefs			= application.getSharedPreferences(Constant.SETTINGS_TAG, Context.MODE_PRIVATE);
		config_editor	= myPrefs.edit();
	}

	@Override
	public String get( String name, String def ){
		return myPrefs.getString(name, def );
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
		update( name, value );

		config_editor.putString(name, value);
		config_editor.commit();

	}
	private void update(String name, String value) {
		final DebugActivity dialog = DebugActivity.getInstance();
		if(dialog!=null){
			dialog.update(name, value );
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
}
