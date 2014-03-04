package com.barobot.isp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.logging.Level;

import javax.comm.CommPortIdentifier;
import javax.comm.SerialPort;
import javax.comm.SerialPortEvent;
import javax.comm.SerialPortEventListener;
import javax.comm.UnsupportedCommOperationException;

import com.barobot.parser.Parser;
import com.barobot.parser.Queue;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.output.AsyncDevice;
import com.barobot.parser.output.Console;
import com.barobot.parser.output.Mainboard;
import com.barobot.parser.utils.GlobalMatch;

public class Hardware {
	public Hardware(String comPort) {
		this.comPort = comPort;
		init();
	}
	boolean connected		= false;
	public String comPort	= "COM1";
	private OutputStream outputStream;
	private InputStream inputStream; 
	private SerialPort serialPort=null;
	private Queue q = new Queue();

	private static boolean inited = false;
	static int mainboardSource		= 0;


	public void init() {
		if( !Hardware.inited ){
			Mainboard mb	= new Mainboard();
		//	AsyncDevice c	= new Console();
		//	AsyncDevice u	= new MainScreen();

			mb.registerSender( new com.barobot.parser.utils.Sender(){
				public boolean send(String command) {
					if(!connected){
						System.out.println("no connect");
						return false;
					}
					System.out.println("\t>>>Sending: " + command);
					command = command + "\n";
			//		synchronized(outputStream){
						try {
							outputStream.write(command.getBytes());
						} catch (IOException e) {
						  e.printStackTrace();
						}
				//	}
					return false;
				}
			} );
			/*
			mb.addGlobalRegex("^E.+", new GlobalMatch(){
				public boolean run( String in ) {
					Parser.log(Level.INFO, "Mainboard init: " + in);
					return true;
				}});
*/
			mainboardSource = Queue.registerSource( mb );
			Queue.enableDevice( mainboardSource );
			/*
	 * */
			Hardware.inited  = true;
		}
	}
	protected void openPort( String name ) throws Exception {
		Enumeration  portList = CommPortIdentifier.getPortIdentifiers();
		System.out.println("openPort:" + name);		  
		while (portList.hasMoreElements()) {
			CommPortIdentifier portId = (CommPortIdentifier) portList.nextElement();
			if( portId.getPortType() == CommPortIdentifier.PORT_SERIAL && portId.getName().equals(name)){
				serialPort =  (SerialPort) portId.open("BarobotISP", 5000);
			  return;
		  }
		}
		if(serialPort == null){
			throw new Exception("no port");	
		}
	}
	public void send(String command, final String retcmd) {
		System.out.println("\t>>>add 2 send: " + command +" / "+retcmd);
		q.add( mainboardSource, command, retcmd );
	}
	
	public void send(String string) {
		q.add( string, false );
	}
	public void send2(String string) {
		if(!connected){
			System.out.println("no connect");
			return;
		}
		System.out.println("\t>>>Sending: " + string);
		string = string + "\n";
		try {
			outputStream.write(string.getBytes());
		} catch (IOException e) {
		  e.printStackTrace();
		}
	}

	public void connect() {
		if(connected){
			return;
		}
		try {
			this.openPort(this.comPort);
			outputStream		= serialPort.getOutputStream();
			this.setFullSpeed(serialPort);
			this.inputStream	= serialPort.getInputStream();

			SerialPortEventListener lis = new SerialPortEventListener(){
				public void serialEvent(SerialPortEvent arg0) {
			          if(!connected ){
			        	  // bad hack - throw indirect exception
			        	  q.error();
			          }
					if( arg0.getEventType() ==  SerialPortEvent.DATA_AVAILABLE ){
						 byte[] readBuffer = new byte[128];
						 try {
						        while ( inputStream.available() > 0){
						          int bytesRead = inputStream.read(readBuffer);
						          String in = new String(readBuffer, 0, bytesRead); 
						    //      System.out.println("in" + in );
						          q.read( mainboardSource, in );
						          if(!connected ){
						        	  // bad hack - throw indirect exception
						        	  q.error();
						          }
						        }
						      } catch (IOException e) {
						    	  e.printStackTrace(); 
						   }
					}
				}};
			serialPort.addEventListener( lis );
			serialPort.notifyOnDataAvailable(true);
			connected = true;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	void setFullSpeed( SerialPort serialPort){
			if( serialPort == null){
				return;
			}
			try {
				serialPort.setSerialPortParams(IspSettings.fullspeed,
					  SerialPort.DATABITS_8,
					  SerialPort.STOPBITS_1,
					  SerialPort.PARITY_NONE);
			} catch (UnsupportedCommOperationException e) {
				e.printStackTrace();
			}
			System.out.println("Set speed " + IspSettings.fullspeed );
		}
		void setProgrammerSpeed( SerialPort serialPort){
			if( serialPort == null){
				return;
			}
			try {
				serialPort.setSerialPortParams(IspSettings.programmspeed,
					  SerialPort.DATABITS_8,
					  SerialPort.STOPBITS_1,
					  SerialPort.PARITY_NONE);
			} catch (UnsupportedCommOperationException e) {
				e.printStackTrace();
			}
			System.out.println("Set speed " + IspSettings.programmspeed );
		}
		
		public void closeOnReady() {
			q.add( new AsyncMessage( true ){		// na koncu zamknij
				public void run(AsyncDevice dev) {
					close();
				}
			});
		}

		protected void close() {
			if(connected){
				System.out.println("serial close");
/*
				if(q != null){
					q.clear( mainboardSource );
					Queue.disableDevice( mainboardSource );
				}
				if (outputStream != null) {
					try {
						synchronized(outputStream){
							outputStream.notify();
						}
						outputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				//System.out.println("close52");
				if (inputStream != null) {
					try {
						synchronized(outputStream){
							outputStream.notify();
						}
						inputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}*/
			//	System.out.println("close5");

				if (serialPort != null) {
					serialPort.notifyOnDataAvailable(false);
					System.out.println("removeEventListener");
					serialPort.removeEventListener();
					synchronized(serialPort){
						serialPort.notify();
					}
					try {
						serialPort.enableReceiveTimeout(0);
					} catch (UnsupportedCommOperationException e) {
						e.printStackTrace();
					}
					try {
						serialPort.enableReceiveThreshold(0);
					} catch (UnsupportedCommOperationException e) {
						e.printStackTrace();
					}
					serialPort.close();
					System.out.println("close5");
					serialPort = null;
				}
				connected = false;
			}
		}
		
		public Queue getQueue() {
			return q;
		}
}
