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
		return  0;
	}

	@Override
	public void set(String name, long value) {
		set(name, "" + value );
	}
	@Override
	public String get( String name, String def ){
		return hashmap.get(name);
	}
}
