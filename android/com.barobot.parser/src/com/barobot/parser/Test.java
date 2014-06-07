package com.barobot.parser;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.barobot.common.Initiator;

public class Test {
	public static Logger logger = null;
	void run(){
		logger = Logger.getLogger(Test.class.getName());
		logger.setLevel(Level.FINE);
		try {
			FileHandler fh = new FileHandler("log_test.txt");
			logger.addHandler(fh);
		} catch (SecurityException e) {
			Initiator.logger.appendError(e);
		} catch (IOException e) {
			Initiator.logger.appendError(e);
		}
		logger.addHandler(new ConsoleHandler());
		logger.log(Level.INFO, "Msg");
	}
}
