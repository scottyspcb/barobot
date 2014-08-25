package com.barobot.sofa.api;


public class JsonResponseBuilder {
	private String status = "OK";
	private String message = "";
	private IData data = null;
	
	public JsonResponseBuilder() {}
	
	public JsonResponse build()
	{
		return new JsonResponse(status, message, data);
	}
	
	public JsonResponseBuilder status (String _status)
	{
		this.status = _status;
		return this;
	}
	
	public JsonResponseBuilder message(String _message)
	{
		this.message = _message;
		return this;
	}
	
	public JsonResponseBuilder data(IData _data)
	{
		this.data = _data;
		return this;
	}
}
