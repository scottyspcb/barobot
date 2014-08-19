package com.barobot.sofa.api;


public class ResponseBuilder {
	private String status = "OK";
	private String message = "";
	private IData data = null;
	
	public ResponseBuilder() {}
	
	public Response build()
	{
		return new Response(status, message, data);
	}
	
	public ResponseBuilder status (String _status)
	{
		this.status = _status;
		return this;
	}
	
	public ResponseBuilder message(String _message)
	{
		this.message = _message;
		return this;
	}
	
	public ResponseBuilder data(IData _data)
	{
		this.data = _data;
		return this;
	}
}
