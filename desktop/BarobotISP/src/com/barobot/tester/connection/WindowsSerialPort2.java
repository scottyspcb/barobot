package com.barobot.tester.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;


import com.engidea.winjcom.CommPortIdentifier;
import com.engidea.winjcom.SerialPort;
import com.engidea.winjcom.SerialPortEvent;
import com.engidea.winjcom.SerialPortEventListener;
import com.engidea.winjcom.exception.UnsupportedCommOperationException;

import com.barobot.common.Initiator;
import com.barobot.common.IspSettings;
import com.barobot.common.interfaces.serial.CanSend;
import com.barobot.common.interfaces.serial.SerialEventListener;
import com.barobot.common.interfaces.serial.SerialInputListener;
import com.barobot.common.interfaces.serial.Wire;

public class WindowsSerialPort2 implements Wire, CanSend{
	protected static final String APPNAME = "BarobotTester";
	protected static final int timeout = 5000;
	protected SerialPort serialPort=null;
	protected String comPort = "COM39";
	protected OutputStream outputStream;
	protected InputStream inputStream; 
	protected int baud = 115200;
	protected boolean connected	= false;
	protected Queue<SerialInputListener> listener=new LinkedList<SerialInputListener>();
	private SerialEventListener iel = null;

	public WindowsSerialPort2(String comPort, int speed ) {
		this.comPort 	= comPort;
		this.baud		= speed;
	}
	public boolean init() {
		return true;
	}

	public boolean open() {
		System.out.println("WindowsSerialPort open " );
		try {
			boolean res = this.openPort(this.comPort);
			if(!res){
				throw new Exception("no port " + this.comPort );
			}
			this.outputStream	= serialPort.getOutputStream();
			this.inputStream	= serialPort.getInputStream();

			serialPort.setSerialPortParams( baud,
				  SerialPort.DATABITS_8,
				  SerialPort.STOPBITS_1,
				  SerialPort.PARITY_NONE);

		} catch (UnsupportedCommOperationException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		SerialPortEventListener lis = new SerialPortEventListener(){
			public void serialEvent(SerialPortEvent arg0) {
				if( arg0.getEventType() ==  SerialPortEvent.DATA_AVAILABLE ){
					Thread t = new Thread(new Runnable() {
						public void run() {
							byte[] readBuffer = new byte[128];
							try {
							     while ( connected && inputStream.available() > 0){
							         int bytesRead = inputStream.read(readBuffer);
							          System.out.println("in" + new String(readBuffer,0,bytesRead ) );
								        for (SerialInputListener il : listener){
								        	if(il.isEnabled()){
								        		il.onNewData( readBuffer, bytesRead );
								        	}
								        }
							         if(!connected ){ 
							          doError(); // bad hack - throw indirect exception
							         }
							     }
							 } catch (IOException e) {
							   e.printStackTrace(); 
							 }
						}
					});
					t.start();
				}
			}};

		serialPort.addEventListener( new EventListener() );

		System.out.println("Set speed " + IspSettings.fullspeed );
		serialPort.notifyOnDataAvailable(true);
		connected			= true;

	    new Thread(new PortReader()).start();

    	if(iel!=null){
    		iel.onConnect();
    	}
		return true;
	}
	protected boolean openPort( String name ) throws Exception {
		Iterator<CommPortIdentifier> iter = CommPortIdentifier.getPortIdentifiers();


	    while ( iter.hasNext() ) {
	    	CommPortIdentifier identifier = (CommPortIdentifier)iter.next();
	    	System.out.println("Available port name="+identifier.getName()+" type="+identifier.getPortType());
	    	if( identifier.getPortType() == CommPortIdentifier.PORT_SERIAL && identifier.getName().equals(name)){
				serialPort = (SerialPort) identifier.open( WindowsSerialPort2.APPNAME , WindowsSerialPort2.timeout);
				return true;
	    	}  
	      }	



		return false;
	}
	protected byte doError() {
		byte[] bytes = new byte[1];		// ver very very bad hack to stop listener thread in lib
		return bytes[10];
	}

	public void close() {
		if( serialPort != null && connected){
			this.listener.clear();
			System.out.println("serial close");
			serialPort.notifyOnDataAvailable(false);
	//		serialPort.disableReceiveFraming();
			serialPort.disableReceiveTimeout();
	//		serialPort.disableReceiveThreshold();
			synchronized(serialPort){
				serialPort.notify();
				serialPort.notifyAll();
			}
			serialPort.notifyOnDataAvailable(false);
			System.out.println("removeEventListener");
			serialPort.removeEventListener();
			synchronized(serialPort){
				serialPort.notify();
			}
			serialPort.close();
			serialPort = null;
			if (outputStream != null) {
				try {
					synchronized(outputStream){
						outputStream.notify();
					}
					outputStream.close();
					outputStream = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (inputStream != null) {
				try {
					synchronized(inputStream){
						inputStream.notify();
					}
					inputStream.close();
					inputStream = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			connected = false;
	    	if(iel!=null){
	    		iel.onClose();
	    	}
		}
	}
	public boolean send(String command) throws IOException{
		System.out.println("\t>>>Sending1: " + command.trim());
		try {
			outputStream.write(command.getBytes());
			return true;
		} catch (IOException e) {
		  e.printStackTrace();
		}
		return false;
	}
	public boolean send(byte[] buf, int size) throws IOException {
		System.out.println("\t>>>Sending2: " + new String(buf,0,size ));
		try {
			outputStream.write(buf, 0, size);
			return true;
		} catch (IOException e) {
		  e.printStackTrace();
		}
		return false;
	}
	public boolean isConnected() {
		return connected;
	}

	public String getName() {
		return "COM Serial Port";
	}

	//@Override
	public void addOnReceive(SerialInputListener inputListener) {
		this.listener.add( inputListener );
		Initiator.logger.i("serial", "listeners: " +this.listener.size() );
	}
	//@Override
	public void removeOnReceive(SerialInputListener inputListener) {
		this.listener.remove(inputListener);
	}

	public void resume() {
		// TODO Auto-generated method stub
	}
	public boolean setAutoConnect(boolean active) {
		// TODO Auto-generated method stub
		/*
			String[] list = {};
			int i=0;
			Enumeration pList = CommPortIdentifier.getPortIdentifiers();
			while (pList.hasMoreElements()) {
				CommPortIdentifier cpi = (CommPortIdentifier) pList.nextElement();
		   // list[i++] = cpi.getName();
				System.out.println( cpi.getName());
			}
			return list; 
		*/
		return false;
	}

	public boolean canConnect() {
		return true;
	}
	public boolean implementAutoConnect() {
		// TODO Auto-generated method stub
		return false;
	}

	public void destroy() {
		close();
	    listener.clear();
	}

	public void setSearching(boolean active) {}
	public void connectToId(String address) {}

	public void setBaud(int speed) {
		try {
			serialPort.setSerialPortParams(speed,
					  SerialPort.DATABITS_8,
					  SerialPort.STOPBITS_1,
					  SerialPort.PARITY_NONE);
			System.out.println("Set speed " + speed );
		} catch (UnsupportedCommOperationException e) {
			e.printStackTrace();
		}
	}
	public SerialEventListener getSerialEventListener() {
		return iel;
	}
	public void setSerialEventListener(SerialEventListener iel) {
		this.iel  = iel;
	}
	public void reset() {
		// TODO Auto-generated method stub
	}

	private final class EventListener implements SerialPortEventListener{
		public void serialEvent(SerialPortEvent ev){
			if( ev.getEventType() ==  SerialPortEvent.DATA_AVAILABLE ){
				byte[] readBuffer = new byte[128];
				try {
				     while ( connected && inputStream.available() > 0){
				         int bytesRead = inputStream.read(readBuffer);
				         System.out.println("in" + new String(readBuffer,0,bytesRead ) );
					        for (SerialInputListener il : listener){
					        	if(il.isEnabled()){
					        		il.onNewData( readBuffer, bytesRead );
					        	}
					        }
				         if(!connected ){ 
				          doError(); // bad hack - throw indirect exception
				         }
				     }
				 } catch (IOException e) {
				   e.printStackTrace(); 
				 }
			}
	    }
	}	
	private final class PortReader implements Runnable{
		public void run(){
			try{
				InputStream aStream = serialPort.getInputStream();
				byte[] buff = new byte[100];
				int length = 0;
				while ( true ){	
				//	System.out.println("aaaaa start");	
					length = aStream.read(buff, 0, 1 );
				//	System.out.println("in" + new String(buff,0,length ) );
					if(length > 0 ){
						for (SerialInputListener il : listener){
							if(il.isEnabled()){
								il.onNewData( buff, length );
					        }
						}	
					}else{
						break;
					}
				}
			}
			catch ( Exception exc ){
				exc.printStackTrace();
			}
		}
	}
}
