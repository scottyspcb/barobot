package com.barobot.common;

import java.io.IOException;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;

import com.barobot.common.constant.Constant;
import com.barobot.common.interfaces.serial.IspCommunicator;
import com.barobot.common.interfaces.serial.SerialInputListener;
import com.barobot.common.interfaces.serial.Wire;

public class IspOverSerial implements SerialInputListener, IspCommunicator {
	public boolean enabled = true;
	protected Wire connection;
	SerialInputListener oldListener	= null;
	protected Queue<Byte> qe = null;
	int size = 0;

	public IspOverSerial(Wire connection) {
		this.connection = connection;
	}

	@Override
	public void init() {
		if( this.qe == null){
			this.qe				= new ArrayBlockingQueue<Byte>(2048);
			this.oldListener	= this.connection.getReceiveListener();
			this.connection.setOnReceive(this);
		}
	}
	public void free() {
		connection.setOnReceive( oldListener );
	}

	public boolean open() {
		init();
	//	Initiator.logger.i(Constant.TAG, "IspOverSerial.open");
		clearBuffer();
	//	connection.open();
		return true;
	}
	public synchronized boolean close() {
	//	Initiator.logger.i(Constant.TAG, "IspOverSerial.close");
	//	this.connection.close();
		return true;
	}
    public synchronized int read(byte[] buf, int wantBytes) {
	    if( wantBytes <= size ){
        	for( int i=0; i<wantBytes;i++ ){
        		buf[i] = qe.poll(); 	 // get from head
        		size--;
     //   		Initiator.logger.d("Pl2303.read", "size--");
        	}
      //  	String gg = toHexStr(buf, wantBytes );
     //   	Initiator.logger.d("Pl2303.read", "qsize:(" + qe.size() + "), want("+wantBytes+") : "+gg);
     //   	showQueue("read");
        	return wantBytes;
    	}
        return 0;
    }
    
	public synchronized int write(byte[] buf, int size) {
		try {
			this.connection.send(buf, size);
	//		Initiator.logger.d("Pl2303.write", "qsize(" +qe.size() + "), write("+size+") : "+toHexStr(buf, size));
		} catch (IOException e) {
			Initiator.logger.appendError(e);
		}
		return size;
	}
	public synchronized int write(String s ) {
		try {
			this.connection.send(s);
	//		Initiator.logger.d("Pl2303.write", "qsize(" +size + "), string: "+s.trim());
		} catch (IOException e) {
			Initiator.logger.appendError(e);
		}
		return size;
	}

	public synchronized boolean isConnected() {
		return this.connection.isConnected(); 
	}
    public void reset(boolean b){
	}

	public synchronized void onNewData(byte[] buf, int length) {
		int putLength = length;
    	for( int i=0; i<putLength;i++ ){
    		qe.add( (byte) (buf[i] & 0xff)); // add on end
    		size++;
    		//Initiator.logger.d("Pl2303.add", "qsize("+qe.size()+") : "+toHexStr(buf[i]));
   // 		showQueue("add");
    	}
    	if( qe.size() > 1024 ){
    	}

    //	byte[] dst = Arrays.copyOf(buf, buf.length);
     //   showQueue("onNewData");

     //   String value = new String(buf, 0, length);
    //    Initiator.logger.d("Pl2303.onNewData", "qsize("+qe.size()+") : "+toHexStr(dst, dst.length) + " value: " + value);
	}

	public void onRunError(Exception e) {
	}

	public void showQueue( String name ) {
		if( size > 0 ){
	    	byte[] dst = new byte[size];//Arrays.copyOf(buf, buf.length);
	    	int i=0;
	    	for(Byte s : qe) {
	    		dst[i] = s;
	    		i++;
	    	}
	    //	Initiator.logger.d("Pl2303."+name, "["+Utils.toHexStr(dst, dst.length)+"]");
		}else{
		//	Initiator.logger.d("Pl2303."+name, "[empty]");
		}
	}
	public synchronized void clearBuffer() {	
		showQueue("clearBuffer");
		qe.clear();
		size = 0;
		//Initiator.logger.d("Pl2303.read", "size=0");
	}
	public void destroy() {
		enabled = false;
		connection = null;
		qe.clear();
		size = 0;
	}
	public static String toHexStr(byte[] b, int length) {
        String str="";
        for(int i=0; i<length; i++) {
            str += String.format("%02x ", b[i]);
        }
        return str;
    }

}
