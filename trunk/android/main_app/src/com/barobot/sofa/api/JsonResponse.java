package com.barobot.sofa.api;

import java.io.IOException;
import java.io.StringWriter;


import android.util.JsonWriter;

public class JsonResponse {
	private String mStatus;
	private String mMessage;
	private IData mData;
	
	public JsonResponse(String status, String message, IData data)
	{
		mStatus = status;
		mMessage = message;
		mData = data;
	}
	
	public String getJSON() throws IOException 
	{
		StringWriter sWriter = new StringWriter();
		JsonWriter writer = new JsonWriter(sWriter); 
		
		writer.beginObject();
		writer.name("status").value(mStatus);
		writer.name("message").value(mMessage);
			
		if (mData != null)
		{
			mData.writeJson(writer);
		}
		writer.endObject();
		
		writer.close();
		
		return sWriter.toString();	
	}
	
}
