package com.barobot.tester.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Queue;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;

import com.barobot.common.Initiator;
import com.barobot.common.IspSettings;
import com.barobot.common.interfaces.serial.CanSend;
import com.barobot.common.interfaces.serial.SerialEventListener;
import com.barobot.common.interfaces.serial.SerialInputListener;
import com.barobot.common.interfaces.serial.Wire;

public class WindowsSerialPort implements Wire, CanSend{
	protected static final String APPNAME = "BarobotTester";
	protected static final int timeout = 5000;
	protected SerialPort serialPort=null;
	protected String comPort = "COM39";
//	protected OutputStream outputStream;
//	protected InputStream inputStream; 
	protected int baud = 115200;
	protected boolean connected	= false;
	protected Queue<SerialInputListener> listener=new LinkedList<SerialInputListener>();
	private SerialEventListener iel = null;
	private boolean fast		= false;

	public WindowsSerialPort(String comPort, int speed, boolean fast ) {
		this.comPort 	= comPort;
		this.baud		= speed;
		this.fast		= fast;		// fats but cant close properly
	}
	public boolean init() {
		return true;
	}

	public boolean open() {
		System.out.println("WindowsSerialPort open " );
		serialPort = new SerialPort(comPort); 
		try {
            serialPort.openPort();//Open port
            serialPort.setParams(baud,  SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            serialPort.setEventsMask(SerialPort.MASK_RXCHAR);
            serialPort.addEventListener(new SerialPortReader());
     //       System.out.println("Set speed " + baud );

        }catch (SerialPortException ex) {
            System.out.println(ex);
            listPorts();
            return false;
        }
/*
		if(this.fast){
	//		try {
			
				SerialPortEventListener lis = new MySerialPortEventListener();
				serialPort.addEventListener( lis );
				int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR;//Prepare mask
		        serialPort.setEventsMask(mask);//Set mask

	//		} catch (TooManyListenersException e) {
	//			e.printStackTrace();
	//			return false;
	//		}
	//		serialPort.notifyOnDataAvailable(true);	
		}else{
			new Thread(new PortReader()).start();
		}
		*/
	//	System.out.println("Set speed " + IspSettings.fullspeed );
		connected			= true;
    	if(iel!=null){
    		iel.onConnect();
    	}
		return true;
	}
	private void listPorts() {
		String[] portNames = SerialPortList.getPortNames();
		for(int i = 0; i < portNames.length; i++){
			System.out.println("Existing port: "+portNames[i]);
		}
	}
	/*protected boolean openPort( String name ) throws Exception {
		return true;
		
		
		Enumeration  portList = (Enumeration) CommPortIdentifier.getPortIdentifiers();
		System.out.println("openPort:" + name);	
		while (portList.hasMoreElements()) {
			CommPortIdentifier portId = (CommPortIdentifier) portList.nextElement();
			//System.out.println("found port :" + portId.getName());		
			if( portId.getPortType() == CommPortIdentifier.PORT_SERIAL && portId.getName().equals(name)){
				serialPort =  (SerialPort) portId.open( WindowsSerialPort.APPNAME , WindowsSerialPort.timeout);
			  return true;
		  }
		}
	}*/
	protected byte doError() {
		byte[] bytes = new byte[1];		// ver very very bad hack to stop listener thread in lib
		return bytes[10];
	}

	public void close() {
		if( serialPort != null && connected){
			System.out.println("WindowsSerialPort, close");	
			synchronized(listener){
				this.listener.clear();
			}
			System.out.println("serial close");
		//	serialPort.notifyOnDataAvailable(false);
		//	serialPort.disableReceiveFraming();
			
		//	serialPort.disableReceiveTimeout();
		//	serialPort.disableReceiveThreshold();
			synchronized(serialPort){
				serialPort.notify();
				serialPort.notifyAll();
			}

			System.out.println("removeEventListener");
			try {
				serialPort.removeEventListener();
			} catch (SerialPortException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			synchronized(serialPort){
				serialPort.notify();
			}
			/*
			try {
				serialPort.enableReceiveTimeout(0);
			} catch (UnsupportedCommOperationException e) {
				e.printStackTrace();
			}*/
			
			try {
				serialPort.closePort();
			} catch (SerialPortException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			serialPort = null;
			/*
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
			}*/
			connected = false;
	    	if(iel!=null){
	    		iel.onClose();
	    	}
		}
	}
	public boolean send(String command) throws IOException{
	//	System.out.println("\t>>>Sending: " + command.trim());
		try {
	//		outputStream.write(command.getBytes());
			serialPort.writeBytes(command.getBytes());
			return true;
		} catch (SerialPortException e) {
			e.printStackTrace();
		}
		return false;
	}
	public boolean send(byte[] buf, int size) throws IOException {
	//	System.out.println("\t>>>Sending: " + command);
		try {
			byte [] subArray = Arrays.copyOfRange(buf, 0, size);
			serialPort.writeBytes(subArray);
			return true;
		} catch (SerialPortException e) {
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
		synchronized(listener){
			this.listener.add( inputListener );
		}
		Initiator.logger.i("serial", "listeners: " +this.listener.size() );
	}
	//@Override
	public void removeOnReceive(SerialInputListener inputListener) {
		synchronized(listener){
			this.listener.remove(inputListener);
		}
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
		System.out.println("WindowsSerialPort, destroy");	
	    listener.clear();
	}

	public void setSearching(boolean active) {}
	public void connectToId(String address) {}

	public void setBaud(int speed) {
	
		try {
			serialPort.setParams(speed,  SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			System.out.println("Set speed " + speed );
		} catch (SerialPortException e) {
			// TODO Auto-generated catch block
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
	/*
	private final class PortReader implements Runnable{
		public void run(){
			try{
				InputStream aStream = serialPort.getInputStream();
				byte[] buff = new byte[100];
				int length = 0;
				while ( true ){
					length = aStream.read(buff);
					if(length > 0 ){
						synchronized(listener){
							for (SerialInputListener il : listener){
								if(il.isEnabled()){
									il.onNewData( buff, length );
						        }
							}
						}
					}else{
						break;
					}
				}
			}catch ( Exception exc ){
				exc.printStackTrace();
			}
		}
	}*/

	class SerialPortReader implements SerialPortEventListener {
        public void serialEvent(SerialPortEvent event) {
            if(event.isRXCHAR()){//If data is available
                if(event.getEventValue() > 0 ){
                    try {
						byte buffer[] = serialPort.readBytes();
						synchronized(listener){
							for (SerialInputListener il : listener){
								if(il.isEnabled()){
									il.onNewData( buffer, buffer.length );
						        }
							}
						}
                    } catch (SerialPortException ex) {
                        System.out.println(ex);
                    }
                }
            }
        }
    }

	/*
	
	
	
	
	public class MySerialPortEventListener implements SerialPortEventListener{
		public void serialEvent(SerialPortEvent arg0) {
			if( arg0.getEventType() ==  SerialPortEvent.RXCHAR  ){
				Thread t = new Thread(new Runnable() {
					public void run() {
						byte[] readBuffer = new byte[128];
						try {
						     while ( connected && inputStream.available() > 0){
						         int bytesRead = inputStream.read(readBuffer);
						    //      System.out.println("in" + in );
							         synchronized(listener){
								        for (SerialInputListener il : listener){
								        	if(il.isEnabled()){
								        		il.onNewData( readBuffer, bytesRead );
								        	}
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
		}}*/
}
