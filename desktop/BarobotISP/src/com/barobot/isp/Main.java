package com.barobot.isp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import jssc.SerialPortList;

import com.barobot.common.DesktopLogger;
import com.barobot.common.Initiator;
import com.barobot.common.IspSettings;
import com.barobot.common.interfaces.onReadyListener;
import com.barobot.common.interfaces.serial.Wire;
import com.barobot.hardware.devices.LightManager;
import com.barobot.hardware.devices.UploadFirmware;
import com.barobot.hardware.devices.i2c.Upanel;
import com.barobot.parser.Queue;
import com.barobot.parser.utils.CopyStream;

public class Main implements onReadyListener{
	public static Thread mt;
	public String config_filename	= "isp_settings.txt";
	public static boolean allowClose = true;
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
	//	loadProps();
		//String[] comlist = list();
		Wizard w	= new Wizard();
		Hardware hw = new Hardware("COM4");

		IspSettings.safeMode	= false;
		IspSettings.verbose		= 2;
		IspSettings.setFuseBits = false;
		IspSettings.setHex		= true;
		IspSettings.force		= false;

		UploadCode uc			= new UploadCode();
		Macro mm				= new Macro();
		MetaRendering mr		= new MetaRendering();
		Queue q					= hw.getQueue();
		LightManager lm			= new LightManager();
		hw.connectIfDisconnected();
		
	//	hw.barobot.main_queue.add("X2000", false);
		//hw.barobot.kalibrcja();
	//	hw.barobot.scann_leds();
/*
		for( int i =0; i<255;i++){
			int p = com.barobot.common.constant.Pwm.linear2log(i );
			System.out.println(""+i + "," + p);
		}
		*/
		/*
		w.fast_close_test( hw );
		uc.prepareSlaveMB( hw );
		uc.prepareMB( hw );
		uc.prepareMBManualReset( hw );
*/
	//	UploadFirmware uf			= new UploadFirmware();
	//	Wire oldConnection			= hw.getConnection();
	//	allowClose = false;
		//uf.prepareMB2( hw.getQueue(), oldConnection, this );

		uc.prepareCarret( hw );
		
		/*
		
		mr.createContstans();
		uc.prepareUpanels( hw );
		uc.prepareUpanelNextTo( hw, 18 );
		w.fast_close_test( hw );
		uc.prepare1Upanel( hw, hw.barobot, Upanel.FRONT );

		q.addWaitThread( Main.main );
		q.addWaitThread( Main.main );

		I2C_Device[] list = hw.barobot.i2c.getDevicesWithLeds();
		if(list.length == 0 ){
			System.out.println("Pusto" );
			return;
		}*/

		/*
		mm.promo_carret( hw );
		mm.promo1( hw );
		mm.testBpm( hw );
		w.test_proc( hw );	
		w.swing( hw, 3, 1000, 5000 );
		w.mrygaj_po_butelkach( hw, 100 );
		w.mrygaj_grb( hw, 30 );
		w.illumination1( hw );

		q.addWaitThread( Main.main );
		q.addWaitThread( Main.main );
		lm.flaga( hw.barobot, q, 6, 100 );

	//	q.addWaitThread( Main.main );
		lm.mrygajRGB( hw.barobot, q, 60, 50 );
		lm.nazmiane( hw.barobot, q, 6, 100 );
		lm.loading(hw.barobot, q, 6);
		lm.linijka( hw.barobot, q, 6, 100 );
		lm.tecza( hw.barobot, q, 6 );
		lm.strobo( hw.barobot, q, 109 );
		lm.zapal(hw.barobot, q);
		w.koniec( hw );
	*/
		
	//	lm.startDemo(hw.barobot);
		
	//	w.findStops(hw);
	//	hw.connectIfDisconnected();	
	//	
	//	w.mrygaj(hw, 10);
	//	w.mrygaj_po_butelkach( hw, 100 );
/*
		w.fadeButelka( hw, 5, 20 );
		w.ilumination2( hw );
		w.ilumination3( hw, "88", 255, "00", 2 );
		w.zamrugaj(hw, 200, 50 );
		w.fadeAll( hw, 4 );
*/

	//	hw.close();

		closeIfReady( hw, q );
	}
	private void closeIfReady( Hardware hw, Queue q ) {
		if(allowClose){
			hw.closeOnReady();
			q.addWaitThread( Main.main );
			System.out.println("Main koniec");
		}else{
			Main.wait(1000);
			closeIfReady(hw, q);
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
				Initiator.logger.appendError(e);
			}
	        input.close();
		} catch (IOException e1) {
			Initiator.logger.appendError(e1);
		}
		if(hw != null ){
			hw.connectIfDisconnected();
	    }
	}
	public static void wait(int ms) {
		try {
			 Thread.sleep(ms);
		} catch (InterruptedException e) {
			Initiator.logger.appendError(e);
		}
	}

	@Override
	public void onReady() {
		 Main.allowClose		= true;
	}
}
