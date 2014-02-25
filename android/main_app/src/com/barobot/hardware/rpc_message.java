package com.barobot.hardware;

import com.barobot.utils.Arduino;
import com.barobot.utils.ArduinoQueue;
import com.barobot.utils.History_item;

public class rpc_message extends History_item{
	/**
	 * Klasa wywyłanej wiadomości
	 */
	protected String name		= "";
	private boolean blocing		= false;
	public boolean block		= false;
	public int timeout			= 20000;		// tyle ms maksymalnie czekaj na zwrotkę zanim pokazać błąd (30s)
	public long send_timestamp	= 0;			// czas wyslania
	//public Runnable isRet		= null;
	public long wait_until		= 0;

	public rpc_message( boolean dir, String cmd, boolean wait4ready){
		this.blocing		= wait4ready;
		this.block			= wait4ready;
		this.command		= cmd;
		this.direction		= dir;	// true = na zewnątrz
	}
	public rpc_message( boolean dir ) {
		// wszystko jest domyślne lub w funkcjach
		this.direction	= dir;	// true = na zewnątrz
	}
	public rpc_message(boolean dir, boolean wait4ready) {
		this.direction	= dir;	// true = na zewnątrz
		this.blocing	= wait4ready;
		this.block		= wait4ready;
	}
	public boolean isRet(String result) {	// czy to co przyszło jest zwrotką tej komendy
		result =result.trim().toUpperCase();
		String command2	= command.toUpperCase();

		if( this.blocing){
			if( result.startsWith("RX") && command2.startsWith("X") ){
				unlock(result);
				return true;
			}
			if( result.startsWith("RY") && command2.startsWith("Y") ){
				unlock(result);
				return true;
			}
			if( result.startsWith("RZ") && command2.startsWith("Z") ){
				unlock(result);
				return true;
			}
			if( result.startsWith( "R" + command2 )){
				unlock(result);
				return true;
			}
			if( result.startsWith( "E" + command2)){		// error tez odblokowuje
				unlock(result);
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
		if( this.command == null || this.command == "" ){
			if( blocing ){
				return prefix + "blocing logic ("+name+")\t\t\t\t" + unlocking_command;
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

	public void start(Arduino ar){
		ArduinoQueue	q2	= this.run();
		unlocking_command = "";
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
	public boolean handle( String command ){
		return false;
	}
	public void unlock( String withCommand ) {
		unlocking_command = withCommand;
		block= false;	
	}
}
