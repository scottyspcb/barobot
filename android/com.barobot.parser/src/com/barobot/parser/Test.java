package com.barobot.parser;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.barobot.parser.output.AsyncDevice;
import com.barobot.parser.output.Console;
import com.barobot.parser.output.MainScreen;
import com.barobot.parser.output.Mainboard;

public class Test {
	public static Logger logger = null;
	void run(){
		logger = Logger.getLogger(Test.class.getName());
		logger.setLevel(Level.FINE);

		try {
			FileHandler fh = new FileHandler("log_test.txt");
			logger.addHandler(fh);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		logger.addHandler(new ConsoleHandler());
		 
		logger.log(Level.INFO, "Msg");
		 
		AsyncDevice mb	= new Mainboard();
		AsyncDevice c	= new Console();
		AsyncDevice u	= new MainScreen();

		Queue.registerOutput( "Mainboard", mb );
		Queue.registerOutput( "Console", c );
		Queue.registerOutput( "User", u );

		
		Operation  op	= new Operation( "runTo" );
		op.needParam("x", 10 );
		op.needParam("y" );
		op.needParam("z", 20 );
		op.needParam("sth", null );		
	
		
		
		
	}
}
