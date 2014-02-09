package com.barobot.isp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Properties;

import org.smslib.helper.CommPortIdentifier;

public class Main {
	
	String config_filename	= "isp_settings.txt";
	Hardware hw;
	
	public static void main(String[] args) {
		Main m = new Main();
		m.start();
		/*
		 for (String s: args) {
	            System.out.println(s);
	     }*/
		m = null;
		// System.getProperty("path.separator"); 
	}

	 private void start() {
		loadProps();
		//String[] comlist = list();
		Wizard w = new Wizard();
		Hardware hw = new Hardware("COM39");
	//	w.preparePCB( hw );
		w.findOrder( hw );
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

	/** Ask the Java Communications API * what ports it thinks it has. 
	 * @return */
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
}
