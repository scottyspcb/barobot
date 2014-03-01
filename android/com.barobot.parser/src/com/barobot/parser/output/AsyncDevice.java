package com.barobot.parser.output;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;


import com.barobot.parser.Parser;
import com.barobot.parser.Queue;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.utils.GlobalMatch;
import com.barobot.parser.utils.Sender;

public abstract class AsyncDevice {
	static StringBuilder buffer = new StringBuilder();
	private static Map<String, GlobalMatch> globalRegex = new HashMap<String, GlobalMatch>();

	public String name = "";
	public boolean enabled = true;
	private Sender sender;
	public static String separator = "\n";
	private AsyncMessage wait_for = null;
	private Queue waiting_queue;
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
		//			System.out.println("command: " + command);
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
		synchronized (this) {
			if( this.wait_for != null ){
				handled = this.wait_for.isRet( command );
				if(handled){
					unlockRet( command );
					return true;
				}
				handled = this.wait_for.onInput( command );
				if(handled){
					return true;
				}
			}
		}
		handled = this.parse(command);
		if(handled){
			return true;
		}
		//	Log.i("command", command);
		//	Parser.logger.log(  , command);
		return this.machGlobal(command);
	}

	private boolean machGlobal(String command) {
	    for(Entry<String, GlobalMatch> e : globalRegex.entrySet()) {
	        String regex = e.getKey();
	    	if(command.matches(regex)){
	    		 GlobalMatch value = e.getValue();  	
	    		 boolean stopnow =  value.run(command);
	    		 if(stopnow){
	    			 return true;
	    		 }
		    }
	    }
		return false;
	}
	public void clear() {
		synchronized (buffer) {
			buffer =  new StringBuilder();
		}
	}
	public void addGlobalRegex(String match, GlobalMatch globalMatch ){
		globalRegex.put(match, globalMatch);
	}
	public boolean send(String command) {
		return this.sender.send(command);
	}
	public void registerSender(Sender sender) {
		this.sender = sender;
	}
	public void waitFor(AsyncMessage m, Queue queue) {
		synchronized (this) {
			this.wait_for		= m;
			this.waiting_queue	= queue;	
		}
	}
	public void unlockRet(String withCommand){
		synchronized (this) {
			if(this.wait_for!=null){
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
}
