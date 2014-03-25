package com.barobot.parser.message;

import com.barobot.common.Initiator;

public class History_item{
	public static final boolean OUTOUT = true;
	public static final boolean INPUT = false;

	public String command			= "";
	protected boolean direction		= History_item.OUTOUT;

	public History_item() {
	}
	public History_item(String cmd, boolean dir) {
		this.command	= cmd;
		this.direction	= dir;
		/*
		if(dir){
			Initiator.logger.d("OUTOUT: ", cmd);
		}else{
			Initiator.logger.d("INPUT: ", cmd);
		}*/
	}

	public String toString(){
		String prefix = "";
		if(this.direction){
			prefix = "<-- ";
		}else{
			prefix = "--> ";
		}
		return prefix + command;
	}
}
