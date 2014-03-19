package com.barobot.parser.output;

public class GlobalInputModifier {
	private String regexp;
	private String changeTo;

	public GlobalInputModifier( String regexp, String changeTo ){
		this.regexp		= regexp;
		this.changeTo 	= changeTo;
	}

	public String replace( String input ) {
		return input.replaceAll(this.regexp, this.changeTo);
	}

	public String getMatchRet() {
		return null;
	}
}
