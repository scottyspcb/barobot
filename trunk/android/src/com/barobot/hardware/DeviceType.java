package com.barobot.hardware;


public class DeviceType {
	protected int type = 0;
	protected int features = 0;
	
	public DeviceType( int type){
		this.type = type;
	}
	public int getType(){
		return type;
	}
	public boolean isImplement( int feature ){
		return false;
	}
	public boolean isType( int checkType ){
		return ( checkType == this.type);
	}
	
	

}
