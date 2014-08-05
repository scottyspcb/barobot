package com.barobot.common;
import java.util.HashMap;
import java.util.Map;

import com.barobot.common.interfaces.HardwareState;

public class EmptyBarobotState implements HardwareState {
	private Map<String, String> hashmap = new HashMap<String, String>();

	@Override
	public void set( String name, String value ){
		hashmap.put(name, value );
	}
	@Override
	public int getInt( String name, int def ){
		if(  hashmap.get(name) == null){
			return 0;
		}
		return Integer.parseInt( hashmap.get(name));
	}
	@Override
	public void set(String name, long value) {
		set(name, "" + value );
	}
	@Override
	public String get( String name, String def ){
		return hashmap.get(name);
	}
	@Override
	public Map<String, String> getAll() {
		Map<String, String> nMap 	= new HashMap<String, String>();
		return nMap;
	}
}
