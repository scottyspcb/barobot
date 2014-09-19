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
import com.barobot.parser.QueueLock;
import com.barobot.parser.interfaces.RetReader;
import com.barobot.parser.utils.Decoder;
import com.barobot.parser.utils.GlobalMatch;

public class Mainboard{
	private static StringBuilder buffer = new StringBuilder();
	private static Map<String, GlobalMatch> globalRegex = new HashMap<String, GlobalMatch>();
	private static Map<String, String> modifiers = new HashMap<String, String>();

//	private LimitedBuffer<String> in_buffer		= new LimitedBuffer<String>(100);	// input
//	private LimitedBuffer<String> out_buffer	= new LimitedBuffer<String>(100);	// output
	public static QueueLock lock				= null;
	private static String separator = "\n";
	private CanSend sender;
	private RetReader retReader;
	private Queue mainQueue = null;
	private HardwareState state;

	public Mainboard( HardwareState state ) {
		this.state			= state;
		lock				= new QueueLock();
		this.addGlobalModifier( "^([0-9][0-9]),", "1$1," );	// add 1 if command num < 100
		this.addGlobalModifier( "^RR", "R" );			// RR => R
	}
	public synchronized void read(String in) {
	//	String s1 = in;

		in = in.replace((char)0,(char)13); 		// null => new line
		buffer.append(in);

//		String s2 = in;
//		System.out.println("in 1: ["+s1+"], in2: [" + s2 + "]");

		int end = buffer.indexOf(separator);
		if( end!=-1){
			while( end != -1 ){		// podziel to na kawalki
				String command	= buffer.substring(0, end);
				buffer			= buffer.delete(0, end+1);
				command			= command.trim();
				analyseInput(command);
				end		= buffer.indexOf(separator);
			}
		}
	}

	private void analyseInput(final String command) {
		Initiator.logger.i("input command: " , command);
	//	History_item hi = new History_item( command, History_item.INPUT );	
	//	new Thread( new Runnable(){		// geto out of the serial port thread
	//		@Override
	//		public void run() {
				try {
					if("".equals(command)){
						//			Log.i(Constant.TAG, "pusta komenda!!!]");
					}else{
						useInput( command );
					}
				} catch (java.lang.NullPointerException e) {
					Initiator.logger.appendError(e);
				} catch (java.lang.NumberFormatException e) {
					Initiator.logger.appendError(e);
				} catch (java.lang.ArrayIndexOutOfBoundsException e) {
					Initiator.logger.appendError(e);
				} catch (Exception e) {
					Initiator.logger.appendError(e);
				}
	//		}
	//	}).start();
	}

	private boolean useInput(String command) {
		boolean handled =false;
		String wait4Command = "";
		String command2 = this.modyfyInput( command );
		if( !command2.equals(command)){
		//	Initiator.logger.e("Mainboard.useInput changed to:", command2 );
			command = command2;
		}
		if( !state.get("show_reading", "0").equals("0") ){
			Initiator.logger.w("Mainboard.useInput", command );
		}
	//	in_buffer.push(command);
		boolean used = false;
		AsyncMessage local_wait_for = null;
		synchronized (QueueLock.lock_wait_for) {
			local_wait_for = lock.wait_for;
	//		Initiator.logger.i("wait_for? ", ( (local_wait_for == null)? "null" : local_wait_for.toString()) );
			if( local_wait_for != null ){
				wait4Command = local_wait_for.command;
				used = true;
	//			Initiator.logger.i("Mainboard.useInput.isRet", command );
				handled = local_wait_for.isRet( command, mainQueue );
				if(handled){
			//		Initiator.logger.i("+unlock: ", command );
					Mainboard.lock.unlock(command, mainQueue);
					return true;
				}
				handled = local_wait_for.onInput( command, this, mainQueue );
				if(handled){
					return true;
				}
				if( this.retReader != null ){
					handled = this.retReader.isRetOf( this, local_wait_for, command, mainQueue );
					if(handled){
						//Initiator.logger.i("+unlock: ", command );
						Mainboard.lock.unlock(command, mainQueue);
						return true;
					}
				}
			}
		}

		if(!used){
		//	Initiator.logger.i("wait_for?: ", ( (lock.wait_for == null)? "null" : "nonull") );
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
					boolean stopnow	= value.run( this, command, wait4Command, lock.wait_for );
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
	    for(Entry<String, String> m : modifiers.entrySet()) {
	    	try {
	    		command		= command.replaceAll(m.getKey(), m.getValue()); 
	    	} catch (PatternSyntaxException e) {
	    		Initiator.logger.appendError(e);
	    	} catch (IllegalArgumentException e) {
	    		Initiator.logger.appendError(e);
	    	} catch (IndexOutOfBoundsException e) {
	    		Initiator.logger.appendError(e);
	    	}
	    }
		return command;
	}
	public boolean send(String command) {
		try {
			if( this.sender.isConnected() ){
				if( state.getInt("show_sending", 0) > 0 ){
					Initiator.logger.e(">>>Mainboard.Send", command.trim());
				}
		//		synchronized(outputStream){
				try {
		//			Initiator.logger.i("Mainboard.Send" , command.trim() );
		//			out_buffer.push(command);
					return this.sender.send(command);
				} catch (IOException e) {
				  Initiator.logger.appendError(e);
				}
			//	}
			}else{
				Initiator.logger.i("Mainboard.Send", "no connect1 [" + command.trim()+"]");
			//	throw new Exception("No connect");
			}
		} catch (NullPointerException e) {	
			Initiator.logger.i("Mainboard.Send", "no connect2 [" + command.trim()+"]");
			Initiator.logger.appendError(e);
		} catch (Exception e) {
			Initiator.logger.appendError(e);
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
					  Initiator.logger.appendError(e);
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

	public void unlockRet(AsyncMessage asyncMessage, String withCommand) {
		lock.unlock(asyncMessage, withCommand, mainQueue);
	}

	public boolean parse(String in) {
		//synchronized(this){
			Initiator.logger.saveLog("unknown command:[" + in+"]");
			Initiator.logger.saveLog("unknown bytes:[" + Decoder.toHexStr(in.getBytes(), in.length())+"]" );
			if(in.matches("^[-a-zA-Z0-9_.,;+]+$")){		// usual characters
			}else{										// unusual characters
				Initiator.logger.saveLog("unknown+unusual:[" + in+"]");
			}

			if( state.getInt("show_unknown", 0) > 0 ){
				if( in.startsWith( "-") ){			// comment
					Initiator.logger.i("Mainboard.unknown.comment", in);
					return true;
				}else if( in.equals("NO_CMD []" ) ){
				}else{
					if(in.matches("^.*[^-a-zA-Z0-9_.,].*")){		// unusual characters
						// log command to db
					}
					Initiator.logger.i("Mainboard.unknown.length", "("+in+") "+ in.length() );
					Initiator.logger.i("Mainboard.unknown.length", "("+in+") "+ in.getBytes().length );
					Initiator.logger.i("Mainboard.unknown", "("+in+") "+Decoder.toHexStr(in.getBytes(), in.length()));
				//	mainQueue.show("Mainboard.parse");
				}
			}
		//}
		return false;
	}
	public void destroy() {
		sender			= null;
		mainQueue		= null;
		state			= null;
		globalRegex		= null;
		modifiers		= null;
		buffer			= null;
		retReader		= null;
		lock			= new QueueLock();
	}

	public void setMainQueue(Queue queue) {
		this.mainQueue = queue;
	}

}
