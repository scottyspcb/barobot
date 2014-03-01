package com.barobot.parser;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.barobot.parser.utils.HasLogger;

public class Parser {
	public static Logger logger = null;

	public static int last_found_device = 0;
	public static int last_has_next		= -1;

	public static void registerLogger( HasLogger hl ){
		Parser.logger = hl.getLogger();
		System.out.println("set log");
	}
	public static void log(Level info, String in) {
		if(Parser.logger==null){
			System.out.println("no log");
		}else{
			Parser.logger.log( info, in);
		}
	}
}
