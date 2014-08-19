package com.barobot.sofa.api;

import java.io.IOException;

import android.util.JsonWriter;

public interface IData {
	public void writeJson (JsonWriter writer) throws IOException;
}
