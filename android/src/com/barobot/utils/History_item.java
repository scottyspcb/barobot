package com.barobot.utils;

public class History_item{
	public String command;
	protected String ret;
	public boolean direction;

	public History_item() {
	}
	public History_item(String cmd, boolean dir) {
		this.command = cmd;
		this.direction = dir;	// true = na zewnątrz
	}
	public String toString(){
		String prefix = "";
		if(this.direction){
			prefix = "<-- ";
		}else{
			prefix = "--> ";
		}
		if(ret==null){
			return prefix + command;
		}
		return prefix + command +"\t\t\t\t" + ret; 
	}
}
