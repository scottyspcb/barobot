package com.barobot.common;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import com.barobot.common.interfaces.serial.IspCommunicator;
import com.barobot.common.interfaces.serial.SerialInputListener;
import com.barobot.common.interfaces.serial.Wire;

public class IspOverSerial implements SerialInputListener, IspCommunicator {
	public boolean enabled = true;
	protected Wire connection;
	protected Queue<Byte> qe;
	int size = 0;

	public IspOverSerial(Wire connection) {
		qe				= new ArrayBlockingQueue<Byte>(1024);
		qe.clear();
		this.connection = connection;
		this.connection.addOnReceive(this);
	}
	public boolean open() {
		clearBuffer();
		return true;
	}
	public synchronized boolean close() {
		this.connection.close();
		return true;
	}
    public synchronized int read(byte[] buf, int wantBytes) {
	    if( wantBytes <= size ){
        	for( int i=0; i<wantBytes;i++ ){
        		buf[i] = qe.poll(); 	 // get from head
        		size--;
        		//Log.d("Pl2303.read", "size--");
        	}
        //	String gg = Utils.toHexStr(buf, wantBytes );
        //	Log.d("Pl2303.read", "qsize:(" + qe.size() + "), want("+wantBytes+") : "+gg);
        	showQueue("read");
        	return wantBytes;
    	}
        return 0;
    }

	public synchronized int write(byte[] buf, int size) {
		try {
			this.connection.send(buf, size);
	//		Log.d("Pl2303.write", "qsize(" +qe.size() + "), write("+size+") : "+Utils.toHexStr(buf, size));
		} catch (IOException e) {
			e.printStackTrace();
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
    	//	Log.d("Pl2303.add", "qsize("+qe.size()+") : "+Utils.toHexStr(buf[i]));
    		showQueue("add");
    	}
    	if( qe.size() > 1024 ){
    	}
    	//byte[] dst = Arrays.copyOf(buf, buf.length);
        showQueue("onNewData");
    	//Log.d("Pl2303.onNewData", "qsize("+qe.size()+") : "+Utils.toHexStr(dst, dst.length));
	}

	public void onRunError(Exception e) {
	}

	public synchronized boolean isEnabled() {
		return enabled;
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
}
