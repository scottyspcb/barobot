package com.barobot.parser.output;

public class Console extends AsyncDevice{
	public Console() {
		super("Console");
	}
	@Override
	public boolean parse(String in) {
		return false;	
	}
}
