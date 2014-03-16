package com.barobot.parser.output;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.barobot.common.Initiator;
import com.barobot.common.interfaces.CanSend;
import com.barobot.parser.Queue;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.utils.GlobalMatch;

public abstract class AsyncDevice {
	static StringBuilder buffer = new StringBuilder();
	private static Map<String, GlobalMatch> globalRegex = new HashMap<String, GlobalMatch>();
	public String name = "";
	public boolean enabled = true;
	public static String separator = "\n";
	private AsyncMessage wait_for = null;
	private Queue waiting_queue;
	private CanSend sender;
	private RetReader retReader;

	public AsyncDevice(String name) {
		this.name = name;
	}
	public void read(String in) {
		synchronized (buffer) {
			buffer.append(in);
			int end = buffer.indexOf(separator);
			if( end!=-1){
				while( end != -1 ){		// podziel to na kawalki
					String command	= buffer.substring(0, end);
					buffer			= buffer.delete(0, end+1);
					command			= command.trim();
		//			Initiator.logger.i("command: " , command);
					if("".equals(command)){
			//			Log.i(Constant.TAG, "pusta komenda!!!]");
					}else{
					//	History_item hi = new History_item( command, History_item.INPUT );
						this.useInput( command );
					}
					end		= buffer.indexOf(separator);
				}
			}
        }
	}
	private boolean useInput(String command) {
		boolean handled =false;
		Initiator.logger.e("AsyncDevice.useInput", command );
		synchronized (this) {
		//	Initiator.logger.i("wait_for?: ", ( (this.wait_for == null)? "null" : "nonull") );
			if( this.wait_for == null ){
				if( this.retReader != null ){
					handled = this.retReader.isRetOf( this, null, command );
					if(handled){
						return true;
					}
				}	
			}else{
		//		Initiator.logger.i("?isRet: ", command );
				handled = this.wait_for.isRet( command );
				if(handled){
			//		Initiator.logger.i("+unlock: ", command );
					this.unlockRet( command );
					return true;
				}
				if( this.retReader != null ){
					handled = this.retReader.isRetOf( this, this.wait_for, command );
					if(handled){
						//Initiator.logger.i("+unlock: ", command );
						this.unlockRet( command );
						return true;
					}
				}
				handled = this.wait_for.onInput( command );
				if(handled){
					return true;
				}
			}
		}
		this.machGlobal( command );
		handled = this.parse(command);
		if(handled){
			return true;
		}
		//	Log.i("command", command);
		Initiator.logger.i("AsyncDevice.useInput.nohandler", command);
		return false;
	}
	public void setRetReader(RetReader retReader) {
		this.retReader = retReader;
	}
	private boolean machGlobal(String command) {
		String wait4Command = "";
		if(this.wait_for != null ){
			wait4Command = this.wait_for.command;
		}
		Initiator.logger.i("AsyncDevice.machGlobal", command + "/" + wait4Command);
	    for(Entry<String, GlobalMatch> e : globalRegex.entrySet()) {
	        String regex		= e.getKey();	// String matchRet		= value.getMatchRet();
	 //       Initiator.logger.i("AsyncDevice.machGlobal.matches", regex + "=" + command );
	        if( command.matches(regex)){
	        	Initiator.logger.i("AsyncDevice.machGlobal ok: ", regex + "=" + command );
	        	GlobalMatch value	= e.getValue();
	        	String matchCommand	= value.getMatchCommand();
	        	Initiator.logger.i("AsyncDevice.machGlobal ok1: ", regex + "=" + command );
	        	if( matchCommand == null || wait4Command.matches(matchCommand)){
	        		Initiator.logger.i("AsyncDevice.machGlobal ok2", wait4Command );
					boolean stopnow	= value.run( this, command, wait4Command, wait_for );
					if(stopnow){
						return true;
					}
	        	}else{
	        		Initiator.logger.i("AsyncDevice.machGlobal.matches no ok2", wait4Command );
	        	}
	        	Initiator.logger.i("AsyncDevice.machGlobal ok3 ", regex + "=" + command );
		    }
	    }
		return false;
	}
	public void clear() {
		synchronized (buffer) {
			buffer =  new StringBuilder();
		}
	}
	public void addGlobalRegex( GlobalMatch globalMatch ){
		Initiator.logger.i("AsyncDevice.addGlobalRegex", globalMatch.getMatchRet() );
		globalRegex.put(globalMatch.getMatchRet(), globalMatch);
	}
	public boolean send(String command) {
		try {
			Initiator.logger.e(">>>AsyncDevice.Send", command.trim());
			if( this.sender.isConnected() ){
		//		synchronized(outputStream){
				try {
		//			Initiator.logger.i(registerSender send" , command.trim() );
					return this.sender.send(command);
				} catch (IOException e) {
				  e.printStackTrace();
				}
			//	}
			}else{
				Initiator.logger.i("AsyncDevice.Send", "no connect");
			//	throw new Exception("No connect");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	public void registerSender(final CanSend connection) {
		this.sender = connection;
		/*
		this.sender = new Sender(){
			@Override
			public boolean send(String command){
				if( connection.isConnected() ){
			//		synchronized(outputStream){
					try {
			//			Initiator.logger.i("registerSender send",  command.trim() );
						return connection.send(command);
					} catch (IOException e) {
					  e.printStackTrace();
					}
				//	}
				}else{
					Initiator.logger.i("no connect");
				//	throw new Exception("No connect");
				}
				return true;
			}
		};*/
	}
	public void registerSender(final OutputStream outputStream) {
		this.sender = new CanSend(){
			public boolean send(String command) {
		//		synchronized(outputStream){
					try {
						byte[] bytes = command.getBytes();
						outputStream.write(bytes);
						return true;
					} catch (IOException e) {
					  e.printStackTrace();
					}
			//	}
				return false;
			}
			@Override
			public boolean isConnected() {
				return true;
			}
		};
	}
	public void waitFor(AsyncMessage m, Queue queue) {
	//	Parser.logger.log(Level.INFO, "waitFor: " +m.toString() );
		synchronized (this) {
			this.wait_for		= m;
			this.waiting_queue	= queue;	
		}
	}
	public void unlockRet(String withCommand){
		synchronized (this) {
			if(this.wait_for!=null){
				Initiator.logger.i(">>>AsyncDevice.unlockRet", this.wait_for.toString() +" with: "+ withCommand.trim());
				this.wait_for.unlockWith(withCommand);
				this.wait_for = null;
				waiting_queue.unlock();
			}
		}
	}
	abstract public boolean parse(String in);

	public void disable() {
		enabled = false;
	}
	public void enable() {
		enabled = true;
	}
	public void destroy() {
		synchronized (buffer) {
			buffer		= new StringBuilder();
		}
		globalRegex.clear();
		sender			= null;
		wait_for		= null;
		waiting_queue	= null;
	}
}
