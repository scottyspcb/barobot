package com.barobot.hardware;

import java.util.LinkedList;

public class Device {
	protected int type = 0;
	public LinkedList<Device> contains = new LinkedList<Device>();

	public Device( int type){
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
