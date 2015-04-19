package com.barobot.common.interfaces;

import java.util.Map;

public interface HardwareState {

	public abstract void set(String name, String value);

	public abstract void set(String name, long value);

	public abstract int getInt(String name, int def);

	public abstract String get(String name, String def);
	
	public abstract Map<String, String> getAll();
	
	public abstract void reloadConfig( int robot_id, boolean useOld);

	void saveConfig(int robot_Serial);

	public abstract void registerListener(StateListener sl);

	public abstract void unregisterListener( StateListener sl);

	public abstract void setDefaults(DefaultState pcb);

	public abstract void resetToDefault(String string);

	public abstract void resetAll();
}
