package com.barobot.isp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.smslib.helper.CommPortIdentifier;

import com.barobot.parser.Operation;
import com.barobot.parser.Parser;
import com.barobot.parser.Queue;
import com.barobot.parser.output.AsyncDevice;
import com.barobot.parser.output.Console;
import com.barobot.parser.output.MainScreen;
import com.barobot.parser.output.Mainboard;
import com.barobot.parser.utils.CopyStream;
import com.barobot.parser.utils.HasLogger;

public class Main implements HasLogger{
	public static Thread mt;
	public String config_filename	= "isp_settings.txt";
	public Hardware hw;
	public static Main main = null;

	public static void main(String[] args) {
		Main.logger = Logger.getLogger(Hardware.class.getName());
		Main.logger.setLevel(Level.FINE);
	//	Main.logger.addHandler(new ConsoleHandler());
		Main.logger.log(Level.INFO, "Start");

		mt = Thread.currentThread();
		main = new Main();
		main.start();
		/*
		 for (String s: args) {
	            System.out.println(s);
	     }*/
		// System.getProperty("path.separator"); 
	}

	 private void start() {
		Parser.registerLogger(this);
		loadProps();
		//String[] comlist = list();
		Wizard w	= new Wizard();
		Hardware hw = new Hardware("COM40");

		IspSettings.safeMode = false;
		IspSettings.setFuseBits = false;
		IspSettings.verbose = 5;
		IspSettings.setHex	= true;
		IspSettings.force = false;

	//	w.fast_close_test( hw );
	//	w.prepareSlaveMB( hw );
	//	w.prepareMB( hw );
	//	w.prepareMBManualReset( hw );
	//	w.fast_close_test( hw );
	//	w.prepareCarret( hw );	
	//	w.checkCarret( hw );
	//	w.prepare1Upanel( hw, 4 );
	//	w.prepareUpanel( hw, 3 );
	//	w.prepareUpanel( hw, 4 );
	//	w.test( hw );
		w.findOrder( hw, 4 );
		w.findOrder( hw, 3 );

		Macro mm  = new Macro();
	//	mm.promo1( hw );
	//	mm.resetuj( hw );
	//	mm.testBpm( hw );
	//	mm.promo_carret( hw );
	//	w.test( hw );
	//	w.test_proc( hw );	
	//	w.swing( hw, 3, 1000, 5000 );
	//	w.test( hw, 4 );
		
	//	w.findOrder( hw, 3 );
	//	w.showOrder();
		
	//	Wizard.wait(1000);
	//	w.clearUpanel( hw );
	//	w.illumination1( hw );
		
		w.ilumination3( hw, "88", 255, "00", 2 );
		
	//	w.fadeButelka( hw, 4, 200 );

	//	w.mrygaj( hw, 10 );
	//	w.zapal( hw );
	//	w.zgas( hw );

	//	hw.close();
		hw.closeOnReady();
		System.out.println("koniec");
	}
	public Properties p;
	private void loadProps() {
        FileInputStream propFile;
        p =  new Properties(System.getProperties());
		try {
			propFile = new FileInputStream( config_filename );
			p.load(propFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	   // p.setProperty("Height", "200");
	   // p.put("Width", "1500");
		saveProps();
	}
	private void saveProps() {
		String comments = "PropertiesDemo";
		OutputStream out;
		try {
			out = new FileOutputStream(new File(config_filename));
			p.store(out, comments);	
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	  protected String[] list() {
		String[] list = {};
		int i=0;
	    Enumeration pList = CommPortIdentifier.getPortIdentifiers();
	    while (pList.hasMoreElements()) {
	      CommPortIdentifier cpi = (CommPortIdentifier) pList.nextElement();
	     // list[i++] = cpi.getName();
	      System.out.println( cpi.getName());
	    }
	    return list; 
	  }

		void run(String command, Hardware closeSerial ) {
	        Process p;
	        if(closeSerial != null ){
	        	System.out.println("bclose");
	        	closeSerial.close();
	        	System.out.println("wait");
	        	System.out.println("aclose");
	        }
	        CopyStream ct;
	        CopyStream ce;
			try {
				System.out.println("-----------------------------------------------");
				System.out.println("\t>>>Running " + command);
				p = Runtime.getRuntime().exec(command);
				BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));  

				System.out.println("\t>>>RESULT ");
				ct = new CopyStream(p.getInputStream(), System.out);
				ct.start();

				ce = new CopyStream(p.getErrorStream(), System.out);
				ce.start();	

				try {
					p.waitFor();
				//	p.getInputStream().close();
				//	p.getErrorStream().close();
					System.out.println("\t>>>RETURN FROM TASK");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		        input.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			if(closeSerial != null ){
				closeSerial.connect();
		    }
		}

	public static Logger logger = null;
	public Logger getLogger() {
		return logger;
	}

	public static void wait(int ms) {
		try {
			 Thread.sleep(ms);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}
}
