package com.barobot.isp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import org.smslib.helper.CommPortIdentifier;
import org.smslib.helper.SerialPort;
import org.smslib.helper.SerialPortEvent;
import org.smslib.helper.SerialPortEventListener;


public class Hardware  {
	public Hardware(String comPort) {
		this.comPort = comPort;
	}
	boolean connected		= false;
	public String comPort	= "COM1";

	private OutputStream outputStream;
	private InputStream inputStream; 
//	private BufferedReader  is;
	private SerialPort serialPort=null;
	//InputListener il;

	protected void openPort( String name ) throws Exception {
		
		  Enumeration  portList = CommPortIdentifier.getPortIdentifiers();
		  System.out.println("openPort:" + name);		  
		  while (portList.hasMoreElements()) {
			  CommPortIdentifier portId = (CommPortIdentifier) portList.nextElement();
			  if( portId.getPortType() == CommPortIdentifier.PORT_SERIAL && 
					  portId.getName().equals(name)){

				  serialPort = (SerialPort) portId.open("BarobotISP", 5000);
				  return;
			  }
		  }
		if(serialPort == null){
			throw new Exception("no port");	
		}
	  }
	public void send(String string) {
		if(!connected){
			System.out.println("no connect");
			return;
		}
		System.out.println("\t>>>Sending: " + string);
		string = string + "\n";

		try {
			outputStream.write(string.getBytes());
			/*
			try {
				 Thread.sleep(100);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}*/
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
			outputStream = serialPort.getOutputStream();
			this.setFullSpeed(serialPort);
			this.inputStream = serialPort.getInputStream();

			SerialPortEventListener lis = new SerialPortEventListener(){
				public void serialEvent(SerialPortEvent arg0) {
					if(  arg0.getEventType() ==  SerialPortEvent.DATA_AVAILABLE ){
						 byte[] readBuffer = new byte[128];
						 try {
						        while (inputStream.available() > 0) {
						          int bytesRead = inputStream.read(readBuffer);
						          String in = new String(readBuffer, 0, bytesRead);
				//		          System.out.print("!serialEvent" +in);
					//	          SerialInputBuffer.readInput(in);	

						        }
						      } catch (IOException e) {
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

	protected void close() {
	//	SerialInputBuffer.clear();
		System.out.println("serial close");
		//System.out.println("close52");
		if (inputStream != null) {
			try {
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	//	System.out.println("close5");
		if (outputStream != null) {
			try {
				outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (serialPort != null) {
			serialPort.close();
			serialPort = null;
		}
		connected = false;
	}

	  void setFullSpeed( SerialPort serialPort){
			if( serialPort == null){
				return;
			}
			serialPort.setSerialPortParams(IspSettings.fullspeed,
				  SerialPort.DATABITS_8,
				  SerialPort.STOPBITS_1,
				  SerialPort.PARITY_NONE);
			 System.out.println("Set speed " + IspSettings.fullspeed );
		}
		void setProgrammerSpeed( SerialPort serialPort){
			if( serialPort == null){
				return;
			}
			serialPort.setSerialPortParams(IspSettings.programmspeed,
				  SerialPort.DATABITS_8,
				  SerialPort.STOPBITS_1,
				  SerialPort.PARITY_NONE);
			 System.out.println("Set speed " + IspSettings.programmspeed );
		}

		public void read_line(String in) {
			System.out.println("IN: "+ in);
			
		}
		public void send(String string, String string2) {
			// TODO Auto-generated method stub
			
		}
		public void closeOnReady() {
			// TODO Auto-generated method stub
			
		}
}
