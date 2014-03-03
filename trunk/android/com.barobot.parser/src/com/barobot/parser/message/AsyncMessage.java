package com.barobot.parser.message;

import com.barobot.parser.Queue;
import com.barobot.parser.output.AsyncDevice;

public class AsyncMessage extends History_item{
	private static final long NO_TIMEOUT = -1;
	protected String name		= "";
	public int timeout			= 20000;		// tyle ms maksymalnie czekaj na zwrotkę zanim pokazać błąd (30s)
	public long send_timestamp	= 0;			// czas wyslania
	//public Runnable isRet		= null;
	public long wait_until		= 0;
	protected String unlocking_command	= "";
	protected boolean blocking			= false;

	public AsyncMessage( String cmd, boolean blocking, boolean dir ){
		this.blocking		= blocking;
		this.command		= cmd;
		this.direction		= dir;
	}
	public AsyncMessage(String cmd, boolean blocking ) {
		this.command		= cmd;
		this.blocking		= blocking;
	}
	public AsyncMessage( boolean dir ) {
		this.direction		= dir;
	}
	public AsyncMessage( boolean blocking, boolean dir ) {
		this.direction		= dir;
		this.blocking		= blocking;
	}
	/*
	public boolean handle( String command ){
		return false;
	}*/
	public void unlockWith( String withCommand ){
		this.unlocking_command = withCommand;
		blocking= false;	
	}

	public void start(AsyncDevice dev) {
		if(this.command != ""){
			dev.send(command);
		}else{
			this.run(dev);
		}
		if(this.isBlocing()){
			this.send_timestamp	= System.currentTimeMillis();
			this.wait_until		= this.send_timestamp + this.getTimeout();
		}
	}
	public AsyncDevice getDevice() {
		int devid		= this.getDeviceId();
		return Queue.getDevice(devid);
	}
	public String toString(){
		String prefix = "";
		if(this.direction){
			prefix = "<-- ";
		}else{
			prefix = "--> ";
		}
		boolean blocing = this.isBlocing();
		if( this.command == null || this.command.equals( "") ){
			if( blocing ){
				return prefix + "blocking logic ("+name+")\t\t\t\t" + unlocking_command;
			}else{
				return prefix + "logic ("+name+")\t\t\t\t" + unlocking_command;
			}
		}else if(unlocking_command!=null){
			return prefix + command +"\t\t\t\t" + unlocking_command;
		}else if(blocing){
			return prefix + command +"\t\t\t\t ???";
		}else{
			return prefix + command;
		}
	}

	// do nadpisania:
	public void run(AsyncDevice dev) {
	}
	public int getDeviceId() {
		return Queue.DFAULT_DEVICE;
	}
	public boolean isBlocing() {
		return this.blocking;
	}
	public boolean isRet(String result) {
		return false;
	}
	public boolean onInput( String input ) {	// some input while waiting for ret
		return false;
	}
	public void onDisconnect() {
	}
	public long getTimeout() {					// in milisec
		return AsyncMessage.NO_TIMEOUT;
	}
	public void afterTimeout(){						// command is expired

	}
	public void onException( String input ) {
	}
}
