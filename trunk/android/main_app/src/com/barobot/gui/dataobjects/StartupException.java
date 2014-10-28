package com.barobot.gui.dataobjects;

import android.content.pm.PackageManager.NameNotFoundException;

public class StartupException extends Exception {
	public String message;
	private Exception exc;

	public StartupException(String string) {
		this.message = string;
	}
	

	public StartupException(String string, Exception e1) {
		this.message = string;
		this.exc = e1;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
