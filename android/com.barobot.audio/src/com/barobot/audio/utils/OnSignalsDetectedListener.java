package com.barobot.audio.utils;

public interface OnSignalsDetectedListener{
	public abstract void peek(float averageAbsValue);
	public abstract void notify(String string, double averageEnergy);
}