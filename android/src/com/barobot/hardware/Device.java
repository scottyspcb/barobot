package com.barobot.hardware;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Device {
	protected int type			= 0;
	public String typeName		= "";
	public String name			= "";
	private static Map<String, String> feature = new HashMap<String, String>();
	private static Map<String, String> config = new HashMap<String, String>();
	public LinkedList<Device> contains = new LinkedList<Device>();
	public boolean is_active	= true;

	public Device(){
	}
	public int getType(){
		return type;
	}
	public String getName(){
		return this.typeName +" / "+ this.name;
	}
	public boolean isImplement( int feature ){
		return false;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isType( int checkType ){
		return ( checkType == this.type);
	}
	public void setTypeName(String type2) {
		this.typeName = type2;
	}
	public void addFeature(String feat_name, String value) {
		feature.put(feat_name, value);
		DeviceSet.indexFeature(feat_name, this);
	}
	public boolean hasFature(String feat_name, String value ) {
		if(feature.containsKey(feat_name)){
			if(value == null){
				return true;
			}else{
				String f = feature.get(feat_name);
				if( f == feat_name ){		// todo a co z nullami?
					return true;
				}
			}
		}
		return false;
	}
	public void addChild(Device c) {
		contains.add(c);
	}
	public void addConfig(String key, String value, String size, String persistent) {
		config.put(key, value);
	}
	public void setActive(boolean isChecked) {
		this.is_active = isChecked;
	}
}
