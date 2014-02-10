package com.barobot.isp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;

import org.smslib.helper.CommPortIdentifier;
import org.smslib.helper.SerialPort;
import org.smslib.helper.SerialPortEvent;
import org.smslib.helper.SerialPortEventListener;

import com.barobot.isp.parser.LineReader;
import com.barobot.isp.parser.SerialInputBuffer;

public class Hardware implements LineReader {
	public Hardware(String comPort) {
		this.comPort = comPort;
		SerialInputBuffer.lr =this;
	}
	boolean connected		= false;
	public String comPort	= "COM1";

	private OutputStream outputStream;
	private InputStream inputStream; 
	private BufferedReader  is;
	private SerialPort serialPort=null;
	InputListener il;

	public void send(String string) {
		if(!connected){
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
			this.setFullSpeed(serialPort);
			this.inputStream = serialPort.getInputStream();
			/*
			serialPort.addEventListener( new SerialPortEventListener(){
				public void serialEvent(SerialPortEvent arg0) {
					System.out.println("serialEvent:" + arg0.getEventType());	
				}} );&*/
			this.startReader( inputStream );
			connected = true;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	protected void openPort( String name ) throws Exception {
		  Enumeration  portList = CommPortIdentifier.getPortIdentifiers();
		  System.out.println("openPort:" + name);
		  while (portList.hasMoreElements()) {
			  CommPortIdentifier portId = (CommPortIdentifier) portList.nextElement();
			  if( portId.getPortType() == CommPortIdentifier.PORT_SERIAL && 
					  portId.getName().equals(name)){

				  serialPort = (SerialPort) portId.open("SimpleWriteApp", 2000);
				  outputStream = serialPort.getOutputStream();
				  return;
			  }
		  }
		if(serialPort == null){
			throw new Exception("no port");	
		}
	  }
	  protected void startReader( InputStream inputStream ) {	
			il = new InputListener(inputStream, this );
			il.start();
		}
	protected void close() {
		SerialInputBuffer.clear();
		System.out.println("serial close");
		if (il != null) {
			il.close();
			il = null;
		}
		if (is != null) {
			try {
				//System.out.println("close3");
				is.close();
				//System.out.println("close4");
			} catch (IOException e) {
				 e.printStackTrace();
			}
		}
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
		}
		connected = false;
		//System.out.println("close28");
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
			 if( in.startsWith( "12") ){			// device found
				 int[] parts = Parser.decodeBytes( in );
				 // Znaleziono urzadzenie
				 int address = parts[1];
				 IspSettings.last_found_device	= address;
			 }else if( in.startsWith( "RL") ){			// RLEDS
				 synchronized (Main.main) { Main.main.notify(); }
				 
			 }
		}
}
