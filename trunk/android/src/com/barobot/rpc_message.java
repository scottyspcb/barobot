package com.barobot;

class rpc_message{
	/**
	 * Klasa wywyłanej wiadomości
	 */
	public String command ="";
	public boolean wait_for_ready = false;
	public String wait_for_command = "";
	public int timeout = 20;				// tyle maksymalnie czekaj na zwrotkę zanim pokazać błąd
	public long send_timestamp = 0;			// czas wyslania
	public rpc_message( String command, boolean wait){
		this.command = command;
		if(wait){
			this.wait_for_ready		= true;
			this.wait_for_command	= "RET " + command;
		}
	}
	public boolean isRet(String message) {	// czy to co przyszło jest zwrotką tej komendy
		if( this.wait_for_ready){ 
			if( message.equals(this.wait_for_command)){
				return true;
			}
			if( message.equals("ERROR " + this.command)){
				return true;
			}
		}
		return false;
	}
}