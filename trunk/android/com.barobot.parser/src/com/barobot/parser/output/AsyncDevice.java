package com.barobot.parser.output;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.PatternSyntaxException;

import com.barobot.common.Initiator;
import com.barobot.common.interfaces.CanSend;
import com.barobot.parser.Queue;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.utils.GlobalMatch;

public abstract class AsyncDevice {
	static StringBuilder buffer = new StringBuilder();
	private static Map<String, GlobalMatch> globalRegex = new HashMap<String, GlobalMatch>();
	private static Map<String, GlobalInputModifier> modifiers = new HashMap<String, GlobalInputModifier>();
	public String name = "";
	public boolean enabled = true;
	public static String separator = "\n";
	private AsyncMessage wait_for = null;
	private Queue waiting_queue;
	private CanSend sender;
	private RetReader retReader;
	private Queue mainQueue = null;

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
						final String theCommand = command;
					//	new Thread( new Runnable(){		// geto out of the serial port thread
					//		@Override
					//		public void run() {
								try {
									useInput( theCommand );
								} catch (java.lang.NullPointerException e) {
									e.printStackTrace();
								} catch (java.lang.NumberFormatException e) {
									e.printStackTrace();
								} catch (java.lang.ArrayIndexOutOfBoundsException e) {
									e.printStackTrace();
								} catch (Exception e) {
									e.printStackTrace();
								}
					//		}
					//	}).start();
					}
					end		= buffer.indexOf(separator);
				}
			}
        }
	}
	private boolean useInput(String command) {
		boolean handled =false;
		Initiator.logger.w("AsyncDevice.useInput", command );
		String command2 = this.modyfyInput( command );
		if( !command2.equals(command)){
			Initiator.logger.e("AsyncDevice.useInput changed to:", command2 );
			command = command2;
		}
		boolean used = false;
		synchronized (this) {
		//	Initiator.logger.i("wait_for?: ", ( (this.wait_for == null)? "null" : "nonull") );
			if( this.wait_for != null ){
				used = true;
		//		Initiator.logger.i("AsyncDevice.useInput.isRet: ", command );
				handled = this.wait_for.isRet( command, mainQueue );
				if(handled){
			//		Initiator.logger.i("+unlock: ", command );
					this.unlockRet( command );
					return true;
				}
				handled = this.wait_for.onInput( command, this, mainQueue );
				if(handled){
					return true;
				}
				if( this.retReader != null ){
					handled = this.retReader.isRetOf( this, this.wait_for, command, mainQueue );
					if(handled){
						//Initiator.logger.i("+unlock: ", command );
						this.unlockRet( command );
						return true;
					}
				}
			}
		}
		if(!used){
		//	Initiator.logger.i("wait_for?: ", ( (this.wait_for == null)? "null" : "nonull") );
			if( this.retReader != null ){
				handled = this.retReader.isRetOf( this, null, command, mainQueue );
				if(handled){
					return true;
				}
			}
		}
		handled =  this.machGlobal( command );
		if(handled){
			return true;
		}
		handled = this.parse(command);
		if(handled){
			return true;
		}
		//	Log.i("command", command);
	//	Initiator.logger.i("AsyncDevice.useInput.nohandler", command);
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
	//	Initiator.logger.i("AsyncDevice.machGlobal", command + "/" + wait4Command);
	    for(Entry<String, GlobalMatch> e : globalRegex.entrySet()) {
	        String regex		= e.getKey();	// String matchRet		= value.getMatchRet();
	 //       Initiator.logger.i("AsyncDevice.machGlobal.matches", regex + "=" + command );
	        if( command.matches(regex)){
	      //  	Initiator.logger.i("AsyncDevice.machGlobal ok: ", regex + "=" + command );
	        	GlobalMatch value	= e.getValue();
	        	String matchCommand	= value.getMatchCommand();
	       // 	Initiator.logger.i("AsyncDevice.machGlobal ok1: ", regex + "=" + command );
	        	if( matchCommand == null || wait4Command.matches(matchCommand)){
	        //		Initiator.logger.i("AsyncDevice.machGlobal ok2", wait4Command );
					boolean stopnow	= value.run( this, command, wait4Command, wait_for );
					if(stopnow){
						return true;
					}
	        	}else{
	       // 		Initiator.logger.i("AsyncDevice.machGlobal.matches no ok2", wait4Command );
	        	}
	       // 	Initiator.logger.i("AsyncDevice.machGlobal ok3 ", regex + "=" + command );
		    }
	    }
		return false;
	}
	public void clear() {
		synchronized (buffer) {
			buffer =  new StringBuilder();
			
		}
	}
	public void addGlobalModifier( GlobalInputModifier globalModifier ){
		modifiers.put(globalModifier.getMatchRet(), globalModifier);
	}
	public void addGlobalRegex( GlobalMatch globalMatch ){
		globalRegex.put(globalMatch.getMatchRet(), globalMatch);
	}

	private String modyfyInput(String command) {
	    for(Entry<String, GlobalInputModifier> e : modifiers.entrySet()) {
	    	try {
	        	GlobalInputModifier mod	= e.getValue();
	        	command			= mod.replace(command);	
	    	} catch (PatternSyntaxException ex) {
	    		ex.printStackTrace();
	    	} catch (IllegalArgumentException ex) {
	    		ex.printStackTrace();
	    	} catch (IndexOutOfBoundsException ex) {
	    		ex.printStackTrace();
	    	}
	     //   String regex		= e.getKey();
	 //       Initiator.logger.i("AsyncDevice.machGlobal.matches", regex + "=" + command );
	    }
		return command;
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
	public void unlockRet(AsyncMessage asyncMessage, String withCommand) {
		synchronized (this) {
			if(this.wait_for == asyncMessage ){
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
		sender			= null;
		mainQueue		= null;
		wait_for		= null;
		waiting_queue	= null;
		globalRegex.clear();
		modifiers.clear();
	}
	public void setMainQueue(Queue queue) {
		this.mainQueue = queue;
	}
}
