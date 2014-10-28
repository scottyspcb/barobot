package com.barobot.parser.message;

import com.barobot.common.Initiator;
import com.barobot.parser.Queue;

public class AsyncMessage extends History_item implements Cloneable{
	protected static final long NO_TIMEOUT		= -1;
	protected static final long DEFAULT_TIME	= 15;
	public String unlockingcommand	= "";
	public boolean blocking			= false;
	public String name				= "";
	public boolean waitingforme 	= false;
	public boolean wasstarted		= false;

	public int timeout			= 20000;		// tyle ms maksymalnie czekaj na zwrotkę zanim pokazać błąd (30s)
//	private long send_timestamp	= 0;			// czas wyslania
	//public Runnable isRet		= null;
//	private long wait_until		= 0;
//	private boolean addSufix	= true;
	
	public AsyncMessage( boolean dir ) {
		this.direction		= dir;
	}

	public AsyncMessage( String cmd, boolean blocking, boolean dir ){
		this.blocking		= blocking;
		this.command		= cmd;
		this.direction		= dir;
	}

	public AsyncMessage(String cmd, boolean blocking ) {
		if(blocking){
	//		System.out.println("new blocking AsyncMessage()" + cmd );
		}
		this.command		= cmd;
		this.blocking		= blocking;
	}


	public AsyncMessage( boolean blocking, boolean dir ) {
		this.blocking		= blocking;
		this.direction		= dir;
	}

	public String getName(){
		return this.name;
	}

	public void unlockWith( String withCommand ){
	//	System.out.println("unlockWith "+withCommand);
	//	System.out.println("\t\t>>>AsyncMessage.unlockWith: " + this.command +" with: "+ withCommand.trim());
		this.unlockingcommand = withCommand;
		blocking= false;
	}

	public final Queue start(Mainboard dev, Queue mainQueue) {
		Queue nextq = null;
		this.wasstarted = true;
		if(this.command != ""){
			dev.send( addSufix() ? ( command + "\n") : command );
		}else{
			nextq = this.run(dev, mainQueue);
		}
		if(this.wait4Finish()){
		//	this.send_timestamp	= System.currentTimeMillis();
		//	this.wait_until		= this.send_timestamp + this.getTimeout();
		}
		return nextq;
	}

	@Override
	public String toString(){
		String prefix = "";
		if(this.direction){
			prefix = "<-- ";
		}else{
			prefix = "--> ";
		}
		boolean blocing = this.wait4Finish();
		if( this.command == null || this.command.equals( "") ){
			if( blocing ){
				return prefix + "blocking logic ("+getName()+")\t\t\t\t" + unlockingcommand;
			}else{
				return prefix + "logic ("+getName()+")\t\t\t\t" + unlockingcommand;
			}
		}else if(unlockingcommand!=null){
			String isblocking = blocking ? "(blocking)" : "";
			return prefix + command +"\t"+ isblocking +"\t\t\t" + unlockingcommand;
		}else if(blocing){
			return prefix + command +" (blocking)\t\t\t\t ???";
		}else{
			return prefix + command + "(no blocking)";
		}
	}

	// do nadpisania:
	public Queue run(Mainboard dev, Queue queue) {
		return null;
	}

	public boolean wait4Finish() {
		return this.blocking;
	}

	public boolean isRet(String result, Queue mainQueue) {
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

	public boolean addSufix() {			// add new line after command
		return true;
	}

	public boolean onInput(String input, Mainboard dev, Queue mainQueue) {	// some input while waiting for ret
		return false;
	}
	public void setWaiting(boolean b) {
		this.waitingforme	= b;
	}

	public String getCommand() {
		return command;
	}

	public String render() {
		String sss =  "["+this.toString()
				+"#"+(direction? "true" : "false" )
				+"#"+command
				+"#"+unlockingcommand
				+"#"+(blocking? "true" : "false" )
				+"#"+name
				+"#"+(waitingforme? "true" : "false" )
				+"#"+(wasstarted? "true" : "false" )
				+"#"+timeout
				+"]";
		
		sss = sss.replace(",", ";");
		sss = sss.replace("#", ",");
		return sss;
	}

	public AsyncMessage copy() {			// make copy of message to run it one more time, copy must be done borefo command is run
	//	AsyncMessage am = new AsyncMessage( this.direction );
		AsyncMessage am = null;
		try {
			am = (AsyncMessage) this.clone();
			if(am==null){
				Initiator.logger.i("AsyncMessage.copy", "is null" );
			}else{
				am.unlockingcommand		= this.unlockingcommand;
				am.blocking				= this.blocking;
				am.name					= this.name;
				am.waitingforme			= this.waitingforme;
				am.name					= this.name;
				am.timeout				= this.timeout;
				am.waitingforme			= this.waitingforme;
				am.wasstarted			= this.wasstarted;
				am.command				= this.command;
				am.direction			= this.direction;
			}
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			Initiator.logger.e("AsyncMessage.copy", "CloneNotSupportedException", e );
		}
		return am;
	}
}
