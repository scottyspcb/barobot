package com.barobot.utils;


public class rpc_message extends History_item{
	/**
	 * Klasa wywyłanej wiadomości
	 */
	public boolean wait_for_ready = false;
	public int timeout = 20;				// tyle maksymalnie czekaj na zwrotkę zanim pokazać błąd
	public long send_timestamp = 0;			// czas wyslania

	public rpc_message( String cmd, boolean dir, boolean wait4ready){
		this.wait_for_ready		= wait4ready;
		this.command			= cmd;
		this.direction			= dir;	// true = na zewnątrz
	}
	public boolean isRet(String message) {	// czy to co przyszło jest zwrotką tej komendy
		message =message.trim();
		if( this.wait_for_ready){ 
			if( message.equals( "RET " + this.command )){
				ret = message;
				return true;
			}
			if( message.equals("ERROR " + this.command)){
				ret = message;
				return true;
			}
			if( message.startsWith("RET READY AT ") ){	// np "RET READY AT 0,0,0"
				if( command.startsWith("SET Y ") ){
					ret = message;
					return true;
				}
				if( command.startsWith("SET X ") ){
					ret = message;
					return true;
				}	
			}
			if( message.startsWith("RET POS ") ){	// np "POS 0,0,0"
				if( command.startsWith("GET CARRET")){
					ret = message;
					return true;
				}
			}			
		}
		return false;
	}
}