package com.barobot.common.interfaces;

public interface DefaultState {
	String getDefault(String key, String def);
	int getDefault(String key, int def);
	int getDefault(String key);
	
}
