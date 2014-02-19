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
	public static Thread mt;
	public String config_filename	= "isp_settings.txt";
	public Hardware hw;
	public static Main main = null;

	public static void main(String[] args) {
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
		loadProps();
		//String[] comlist = list();
		Wizard w = new Wizard();
		Hardware hw = new Hardware("COM40");

		IspSettings.safeMode = false;
		IspSettings.setFuseBits = false;
		IspSettings.verbose = 0;
		IspSettings.setHex	= true;
		IspSettings.force = false;

	//	w.prepareSlaveMB( hw );
	//	w.test( hw );
	//	w.findOrder( hw, 4 );
	//	Wizard.wait(1000);

	//	w.prepareMB( hw );
	//	w.prepareCarret( hw );
//		w.prepareUpanel(hw);

	//	w.prepareCarret( hw );
	//	w.checkCarret( hw );
	//	w.prepare1Upanel( hw, 4 );
		w.prepareUpanel( hw, 3 );
//		w.prepareUpanel( hw, 4 );

	//	
	//	w.clearUpanel( hw );
	//	
		//w.ilumination1( hw );
	//	w.ilumination2( hw );

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
