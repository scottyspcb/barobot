package com.barobot.common.interfaces;

public interface OnSignalsDetectedListener{
	public abstract void peek(float averageAbsValue);
	public abstract void changeBPM(float newbpm);
	public abstract void notify(String string, double averageEnergy);
}