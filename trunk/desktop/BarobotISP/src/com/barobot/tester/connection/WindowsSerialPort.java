package com.barobot.tester.connection;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;

import com.barobot.common.Initiator;
import com.barobot.common.constant.LowHardware;
import com.barobot.common.interfaces.serial.CanSend;
import com.barobot.common.interfaces.serial.SerialEventListener;
import com.barobot.common.interfaces.serial.SerialInputListener;
import com.barobot.common.interfaces.serial.Wire;

public class WindowsSerialPort implements Wire, CanSend{
	protected static final String APPNAME = "BarobotTester";
	protected static final int timeout = 5000;
	protected SerialPort serialPort=null;
	public String comPort = "COM39"; 
	protected int baud = LowHardware.MAINBOARD_SERIAL0_BOUND;//115200;
	protected boolean connected	= false;
	protected SerialInputListener listener=null;
	private SerialEventListener iel = null;

	public WindowsSerialPort(String comPort, int speed ) {
		this.comPort 	= comPort;
		this.baud		= speed;
	}
	public boolean init() {
		return true;
	}

	@Override
	public WindowsSerialPort newInstance() {
		WindowsSerialPort sw = new WindowsSerialPort( this.comPort, this.baud );
		return sw;
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
	protected byte doError() {
		byte[] bytes = new byte[1];		// ver very very bad hack to stop listener thread in lib
		return bytes[10];
	}

	public void close() {
		if( serialPort != null && connected){
			System.out.println("WindowsSerialPort, close");	
			synchronized(listener){
				this.listener = null;
			}
			System.out.println("serial close");
			synchronized(serialPort){
				serialPort.notify();
				serialPort.notifyAll();
			}

			System.out.println("removeEventListener");
			try {
				serialPort.removeEventListener();
			} catch (SerialPortException e) {
				Initiator.logger.appendError(e);
			}
			synchronized(serialPort){
				serialPort.notify();
			}			
			try {
				serialPort.closePort();
			} catch (SerialPortException e) {
				Initiator.logger.appendError(e);
			}
			serialPort = null;
			connected = false;
	    	if(iel!=null){
	    		iel.onClose();
	    	}
		}
	}
	public boolean send(String command) throws IOException{
		try {
			serialPort.writeBytes(command.getBytes());
			return true;
		} catch (SerialPortException e) {
			Initiator.logger.appendError(e);
		}
		return false;
	}
	public boolean send(byte[] buf, int size) throws IOException {
		try {
			byte [] subArray = Arrays.copyOfRange(buf, 0, size);
			serialPort.writeBytes(subArray);
			return true;
		} catch (SerialPortException e) {
			Initiator.logger.appendError(e);
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
	public void setOnReceive(SerialInputListener inputListener) {
		this.listener =  inputListener;
	}
	public void resume() {
		// TODO Auto-generated method stub
	}
	public boolean setAutoConnect(boolean active) {
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
	    listener= null;
	}

	public void setSearching(boolean active) {}
	public void connectToId(String address) {}

	public void setBaud(int speed) {
		baud = speed;
		if( serialPort != null && serialPort.isOpened() ){
			try {
				serialPort.setParams(speed,  SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
				System.out.println("Set speed " + speed );
			} catch (SerialPortException e) {
				// TODO Auto-generated catch block
				Initiator.logger.appendError(e);
			}
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
	
	@Override
	public SerialInputListener getReceiveListener() {
		return this.listener;
	}

	class SerialPortReader implements SerialPortEventListener {
        public void serialEvent(SerialPortEvent event) {
            if(event.isRXCHAR()){//If data is available
                if(event.getEventValue() > 0 ){
                    try {
						byte buffer[] = serialPort.readBytes();
						synchronized(listener){
						//	System.out.println(new String(buffer));
							listener.onNewData( buffer, buffer.length );
						}
                    } catch (SerialPortException ex) {
                        System.out.println(ex);
                    }
                }
            }
        }
    }

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
	}
}
