package com.barobot.common.interfaces;

public interface HardwareState {

	public abstract void set(String name, String value);

	public abstract void set(String name, long value);

	public abstract int getInt(String name, int def);

	public abstract String get(String name, String def);

}
