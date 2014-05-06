package com.barobot.isp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Properties;

import jssc.SerialPortList;

import com.barobot.common.DesktopLogger;
import com.barobot.common.IspSettings;
import com.barobot.parser.utils.CopyStream;

public class Main{
	public static Thread mt;
	public String config_filename	= "isp_settings.txt";
	public Hardware hw;
	public static Main main = null;

	public static void main(String[] args) {
		String[] portNames = SerialPortList.getPortNames();
        for(int i = 0; i < portNames.length; i++){
            System.out.println(portNames[i]);
        }

		DesktopLogger dl = new DesktopLogger();
		com.barobot.common.Initiator.setLogger( dl );

		mt		= Thread.currentThread();
		main	= new Main();
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
		Wizard w	= new Wizard();
		Hardware hw = new Hardware("COM3");

		IspSettings.safeMode = false;
		IspSettings.verbose = 2;
		IspSettings.setFuseBits = false;
		IspSettings.setHex	= true;
		IspSettings.force = false;

		UploadCode uc = new UploadCode();
		Macro mm  = new Macro();

	//	hw.connectIfDisconnected();
//		uc.prepareUpanels( hw );
		
	//	
	//	w.fast_close_test( hw );

	//	uc.prepareSlaveMB( hw );
	//	uc.prepareMB( hw );
	//	uc.prepareMB2( hw );
	//	uc.prepareMBManualReset( hw );
	//	uc.prepareCarret( hw );

	//	w.fast_close_test( hw );
	//	w.checkCarret( hw );
	//	w.prepare1Upanel( hw, 4 );

	//	w.prepareUpanel( hw, 3 );
	//	w.prepareUpanel( hw, 4 );
	//	w.test( hw );
		w.findOrder( hw );

	//	mm.promo1( hw );
		
	//	MetaRendering mr = new MetaRendering();
	//	mr.createContstans();	
	//	MetaRendering mr = new MetaRendering();
	//	mr.createContstans();
	//	mm.resetuj( hw );
	//	mm.testBpm( hw );
	//	mm.promo_carret( hw );
	

	//	w.test_proc( hw );	
//		w.swing( hw, 3, 1000, 5000 );
	//	w.test( hw );
	//	w.findOrder( hw, 3 );
	//	w.showOrder();
	//	Wizard.wait(1000);
	//	w.clearUpanel( hw );
	//	w.mrygaj( hw, 10 );
	//	w.illumination1( hw );
	////	w.ilumination2( hw );

	//	w.ilumination3( hw, "88", 255, "00", 2 );
	//	w.zamrugaj(hw, 10, 10 );

	//w.fadeButelka( hw, 4, 200 );
	//	w.fadeAll( hw, 4 );
	//	w.zapal( hw );
	//	w.zgas( hw );
		
		w.zapalPrzod( hw );
		
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
	public void runCommand(String command, Hardware hw ) {
        Process p;
        if(hw != null ){
        	System.out.println("bclose");
        	hw.closeNow();
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
		if(hw != null ){
			hw.connectIfDisconnected();
	    }
	}
	public static void wait(int ms) {
		try {
			 Thread.sleep(ms);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}
}
