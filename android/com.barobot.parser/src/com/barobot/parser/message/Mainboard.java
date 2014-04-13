package com.barobot.parser.message;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.PatternSyntaxException;

import com.barobot.common.Initiator;
import com.barobot.common.interfaces.HardwareState;
import com.barobot.common.interfaces.serial.CanSend;
import com.barobot.parser.Queue;
import com.barobot.parser.interfaces.RetReader;
import com.barobot.parser.utils.Decoder;
import com.barobot.parser.utils.GlobalMatch;

public class Mainboard{
	private static StringBuilder buffer = new StringBuilder();
	private static Map<String, GlobalMatch> globalRegex = new HashMap<String, GlobalMatch>();
	private static Map<String, String> modifiers = new HashMap<String, String>();

	public static String separator = "\n";
	private AsyncMessage wait_for = null;
	private CanSend sender;
	private RetReader retReader;
	private Queue mainQueue = null;
	private HardwareState state;

	public Mainboard( HardwareState state ) {
		this.state		= state;
		this.addGlobalModifier( "^25", "125" );		// add 1 if num < 100
		this.addGlobalModifier( "^RR", "R" );		// RR => R
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
						new Thread( new Runnable(){		// geto out of the serial port thread
							@Override
							public void run() {
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
							}
						}).start();
					}
					end		= buffer.indexOf(separator);
				}
			}
        }
	}
	private boolean useInput(String command) {
		boolean handled =false;
		String wait4Command = "";

		String command2 = this.modyfyInput( command );
		if( !command2.equals(command)){
	//		Initiator.logger.e("Mainboard.useInput changed to:", command2 );
			command = command2;
		}

		//	if( state.getInt("show_reading", 0) > 0 ){
				Initiator.logger.w("Mainboard.useInput", command );
		//	}

		boolean used = false;
		synchronized (this) {
		//	Initiator.logger.i("wait_for?: ", ( (this.wait_for == null)? "null" : "nonull") );
			if( this.wait_for != null ){
				wait4Command = this.wait_for.command;
				used = true;
		//		Initiator.logger.i("Mainboard.useInput.isRet: ", command );
				handled = this.wait_for.isRet( command, mainQueue );
				if(handled){
			//		Initiator.logger.i("+unlock: ", command );
					this.unlockRet( command, true );
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
						this.unlockRet( command, true );
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
		handled =  this.machGlobal( command, wait4Command );
		if(handled){
			return true;
		}
		handled = this.parse(command);
		if(handled){
			return true;
		}
		//	Log.i("command", command);
	//	Initiator.logger.i("Mainboard.useInput.nohandler", command);
		return false;
	}
	public void setRetReader(RetReader retReader) {
		this.retReader = retReader;
	}
	private boolean machGlobal(String command, String wait4Command) {
	//	Initiator.logger.i("Mainboard.machGlobal", command + "/" + wait4Command);
	    for(Entry<String, GlobalMatch> e : globalRegex.entrySet()) {
	        String regex		= e.getKey();	// String matchRet		= value.getMatchRet();
	 //       Initiator.logger.i("Mainboard.machGlobal.matches", regex + "=" + command );
	        if( command.matches(regex)){
	      //  	Initiator.logger.i("Mainboard.machGlobal ok: ", regex + "=" + command );
	        	GlobalMatch value	= e.getValue();
	        	String matchCommand	= value.getMatchCommand();
	       // 	Initiator.logger.i("Mainboard.machGlobal ok1: ", regex + "=" + command );
	        	if( matchCommand == null || wait4Command.matches(matchCommand)){
	        //		Initiator.logger.i("Mainboard.machGlobal ok2", wait4Command );
					boolean stopnow	= value.run( this, command, wait4Command, wait_for );
					if(stopnow){
						return true;
					}
	        	}else{
	       // 		Initiator.logger.i("Mainboard.machGlobal.matches no ok2", wait4Command );
	        	}
	       // 	Initiator.logger.i("Mainboard.machGlobal ok3 ", regex + "=" + command );
		    }
	    }
		return false;
	}
	public void clear() {
		synchronized (buffer) {
			buffer =  new StringBuilder();
		}
	}
	public void addGlobalModifier(String string, String string2) {
		modifiers.put(string, string2 );
	}
	public void addGlobalRegex( GlobalMatch globalMatch ){
		globalRegex.put(globalMatch.getMatchRet(), globalMatch);
	}
	private String modyfyInput(String command) {
	    for(Entry<String, String> e : modifiers.entrySet()) {
	    	try {
	    		command		= command.replaceAll(e.getKey(), e.getValue()); 
	    	} catch (PatternSyntaxException ex) {
	    		ex.printStackTrace();
	    	} catch (IllegalArgumentException ex) {
	    		ex.printStackTrace();
	    	} catch (IndexOutOfBoundsException ex) {
	    		ex.printStackTrace();
	    	}
	    }
		return command;
	}
	public boolean send(String command) {
		try {
			if( state.getInt("show_sending", 0) > 0 ){
				Initiator.logger.e(">>>Mainboard.Send", command.trim());
			}
			if( this.sender.isConnected() ){
		//		synchronized(outputStream){
				try {
					Initiator.logger.i("Mainboard.Send" , command.trim() );
					return this.sender.send(command);
				} catch (IOException e) {
				  e.printStackTrace();
				}
			//	}
			}else{
				Initiator.logger.i("Mainboard.Send", "no connect");
			//	throw new Exception("No connect");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	public void registerSender(final CanSend connection) {
		this.sender = connection;
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
	public void waitFor(AsyncMessage m) {
	//	Parser.logger.log(Level.INFO, "waitFor: " +m.toString() );
		synchronized (this) {
			this.wait_for		= m;
		}
	}
	public void unlockRet(String withCommand, boolean unlockQueue ){
		synchronized (this) {
			if(this.wait_for!=null){
		//		Initiator.logger.i(">>>Mainboard.unlockRet", this.wait_for.toString() +" with: "+ withCommand.trim());
				this.wait_for.unlockWith(withCommand);
				this.wait_for = null;
				mainQueue.unlock();
			}
		}
	}
	public void unlockRet(AsyncMessage asyncMessage, String withCommand) {
		synchronized (this) {
			if(this.wait_for == asyncMessage ){
			//	Initiator.logger.i(">>>Mainboard.unlockRet", this.wait_for.toString() +" with: "+ withCommand.trim());
				this.wait_for.unlockWith(withCommand);
				this.wait_for = null;
				mainQueue.unlock();
			}
		}
	}
	public boolean parse(String in) {
		if( state.getInt("show_unknown", 0) > 0 ){
			if( in.startsWith( "-") ){			// comment
				Initiator.logger.i("Mainboard.parse.comment", in);
				return true;
			}else if( in.equals("NO_CMD []" ) ){
			}else{
				if(in.matches("^.*[^-a-zA-Z0-9_.,].*")){		// unusual characters
					// log command to db
				}
				Initiator.logger.i("Mainboard.parse", in);	
				Initiator.logger.i("Mainboard.parse", Decoder.toHexStr(in.getBytes(), in.length()));
			//	mainQueue.show("Mainboard.parse");
			}
		}
		return false;
	}

	public void destroy() {
		sender			= null;
		mainQueue		= null;
		wait_for		= null;
		state			= null;
		globalRegex		= null;
		modifiers		= null;
		buffer			= null;
		retReader		= null;
	}
	public void setMainQueue(Queue queue) {
		this.mainQueue = queue;
	}
	public String showWaiting() {
		synchronized (this) {
			if(this.wait_for!=null){
				return this.wait_for.toString();
			}
		}
		return "";
	}
}
