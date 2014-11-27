package com.barobot.android;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.orman.dbms.ResultList;
import org.orman.dbms.ResultList.ResultRow;
import org.orman.sql.Query;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.barobot.common.Initiator;
import com.barobot.common.constant.Constant;
import com.barobot.common.interfaces.HardwareState;
import com.barobot.common.interfaces.StateListener;
import com.barobot.gui.database.BarobotData;
import com.barobot.gui.dataobjects.Engine;
import com.barobot.other.Android;
import com.barobot.parser.utils.Decoder;

public class AndroidBarobotState implements HardwareState{
	private SharedPreferences.Editor config_editor;			// config systemu android
	private SharedPreferences myPrefs;
	private int robot_id = 0;
//	private boolean data_ready = false;
	private static String[] notPersistant = {
		"POSX",
		"POSY",
		"POSZ",
		"POS_START_X",
		"NEUTRAL_POS_Y",
		"NEUTRAL_POS_Z",
		"TEMPERATURE",
		"ARDUINO_VERSION",
		"WEIGHT_MIN",
		"WEIGHT_STATE",
		"HX_STATE",
		"HALLX_UNDER",
		"ONCE_PER_APP_START",
		"ONCE_PER_ROBOT_START",
	};
	public AndroidBarobotState( Activity application ){
		myPrefs			= application.getSharedPreferences(Constant.SETTINGS_TAG, Context.MODE_PRIVATE);
		config_editor	= myPrefs.edit();
	//	data_ready		= true;
	}
	@Override
	public int getInt( String name, int def ){
		return Decoder.toInt(get( name, ""+def ), def );
	}
	@Override
	public void set(String name, long value) {
		set(name, "" + value );
	}

	@Override
	public void set( String name, String value ){
		config_editor.putString(name, value);
		config_editor.commit();
		update( name, value );
	}

	@Override
	public String get( String name, String def ){
		return myPrefs.getString(name, def );
	}

	@Override
	public Map<String, String> getAll() {
		Map<String, ?> allEntries 	= myPrefs.getAll();
		Map<String, String> nMap 	= new HashMap<String, String>();
		for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
		    nMap.put(entry.getKey(), entry.getValue().toString());
		} 
		return nMap;
	}
	/*
	 * 
	 * Reload config for specific robot serial id
	 * 
	 * */
	@Override
	public void reloadConfig(int new_robot_id) {
		int old_robot_id	= robot_id;
		robot_id			= new_robot_id;

		// read from db
		Query query3 = new Query("SELECT * FROM `robot_config` WHERE `robot` = '0' OR `robot`= '"+robot_id+"' ORDER BY `robot` ASC");
		Initiator.logger.i("StartOrmanMapping","query3: "+ query3.toString()); 
		ResultList res = BarobotData.omdb.getExecuter().executeForResultList(query3);
		if(res == null){
			Initiator.logger.i("StartOrmanMapping","results: null"); 
		}else{
			int rc = res.getRowCount();
	//		Initiator.logger.i("StartOrmanMapping","results: "+ rc); 
			for(int i=0;i<rc;i++){
				ResultRow rr = res.getResultRow(i);
		//		String aa = (String) rr.getColumn("id");
				String tr = (String) rr.getColumn("config_name");
				String tv = (String) rr.getColumn("value");
				config_editor.putString(tr, tv);
		//		Initiator.logger.i("StartOrmanMapping","res: "+ aa.toString()+ " - "+ tr +" - value: "+ tv); 
			}
			config_editor.commit();
		}
		Android.createRobot( old_robot_id, new_robot_id);
		Engine.GetInstance().invalidateData();
	}

	/*
	 * 
	 * Save config for specific robot serial id
	 * 
	 * */

	@Override
	public void saveConfig(int new_robot_id) {
	//	CREATE UNIQUE INDEX one_conf_per_robot on `robot_config` (`robot`,`config_name`);
		robot_id = new_robot_id;
		// save config with old robot_id
		Map<String, ?> allEntries 	=  myPrefs.getAll();
		for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
			int remember = Arrays.asList(notPersistant).indexOf(entry.getKey());	// czy zapisac w configu tą wartosc?
			if(remember == -1){	//not found = save to db
				// save to db
				Query query3 = new Query("INSERT OR REPLACE INTO `robot_config` (`robot`,`config_name`,`value`) VALUES ('"+robot_id+"','"+entry.getKey()+"','"+entry.getValue().toString()+"')");
				Initiator.logger.i("StartOrmanMapping","save: "+ query3); 
				BarobotData.omdb.getExecuter().executeOnly(query3);
			}
		}
	}

	private void update( String name, String value) {
		for(StateListener listener: listeners){
			listener.onUpdate( this, name, value);
		}
	}

	List<StateListener> listeners = new ArrayList<StateListener>();
	@Override
	public void registerListener(StateListener sl) {
		this.listeners.add(sl);
	}
	@Override
	public void unregisterListener(StateListener sl) {
		this.listeners.remove(sl);
	}
}
