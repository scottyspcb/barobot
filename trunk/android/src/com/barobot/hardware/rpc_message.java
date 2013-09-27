package com.barobot.hardware;

import com.barobot.utils.Arduino;
import com.barobot.utils.History_item;


public class rpc_message extends History_item{
	/**
	 * Klasa wywyłanej wiadomości
	 */
	protected String name		= "";
	private boolean blocing		= false;
	public int timeout			= 20;				// tyle maksymalnie czekaj na zwrotkę zanim pokazać błąd
	public long send_timestamp	= 0;			// czas wyslania
	public Runnable isRet		= null;

	public rpc_message( String cmd, boolean dir, boolean wait4ready){
		this.blocing			= wait4ready;
		this.command			= cmd;
		this.direction			= dir;	// true = na zewnątrz
	}
	public rpc_message( boolean dir ) {
		// wszystko jest domyślne lub w funkcjach
		this.direction			= dir;	// true = na zewnątrz
	}
	public boolean isRet(String message) {	// czy to co przyszło jest zwrotką tej komendy
		message =message.trim();
		if( this.blocing){
			if( message.startsWith("R READY AT ") ){	// np "R READY AT 0,0,0"
				if( command.startsWith("SET Y ") ){
					ret = message;
					return true;
				}
				if( command.startsWith("SET X ") ){
					ret = message;
					return true;
				}
				return false;
			}
			if( message.startsWith("R POS ") ){	// np "POS 0,0,0"
				if( command.startsWith("GET CARRET")){
					ret = message;
					return true;
				}
			}
			if( message.equals( "R " + this.command )){
				ret = message;

				return true;
			}
			if( message.equals("ERROR " + this.command)){
				ret = message;
				return true;
			}
		}
		return false;
	}
	public String toString(){
		String prefix = "";
		if(this.direction){
			prefix = "<-- ";
		}else{
			prefix = "--> ";
		}
		boolean blocing = this.isBlocing();
		if( this.command == null){
			if( blocing ){
				return prefix + "blocing logic ("+name+")\t\t\t\t" + ret;
			}else{
				return prefix + "logic ("+name+")\t\t\t\t" + ret;
			}
		}else if(ret!=null){
			return prefix + command +"\t\t\t\t" + ret;
		}else if(blocing){
			return prefix + command +"\t\t\t\t ???";
		}else{
			return prefix + command;
		}
	}

	public void start(Arduino ar){
		ArduinoQueue	q2	= this.run();
		ret = "";
		if(q2 != null){
			ar.sendFirst( q2 );			// wykonaj przed następnymi działaniami
		}
	}
	// do nadpisania
	public ArduinoQueue run() {
		return null;
	}
	public boolean isBlocing() {
		return this.blocing;
	}
}
