package com.barobot.utils;

import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import com.barobot.hardware.rpc_message;
import com.barobot.hardware.virtualComponents;

import android.os.Handler;

public class ArduinoQueue {
	public LinkedList<rpc_message> output = new LinkedList<rpc_message>();

//	public void add(Runnable runnable ) {
//	}
	public rpc_message add( String message, boolean doWwait ){
		if( message == null || message== "" ){
			return null;
		}
		rpc_message m = new rpc_message( true, message, doWwait );
		output.add( m );
		return m;
	}
	public rpc_message add(rpc_message rpcLogic) {
		output.add( rpcLogic );	
		return rpcLogic;
	}
	public void addWait(final int time) {
		final rpc_message m2 = new rpc_message( true, true ) {
			@Override
			public ArduinoQueue run() {
				this.name				= "wait " + time;
				final Arduino ar 		= Arduino.getInstance();
				final rpc_message m3	= this;
				/*
				final Handler handler	= new Handler();
				handler.postDelayed(new Runnable() {
				  @Override
				  public void run() {
					  ar.unlock( m3 );
				  }
				}, time);			
				*/
				new Timer().schedule(new TimerTask() {          
				    public void run() {
				    	 ar.unlock( m3 );
				    }
				}, time);// odczekaj tyle czasu, odblokuj kolejkę i jedz dalej
				return null;
			}
		};
		output.add( m2 );		
	}

	public void addThrow(final String string) {		// throw exception in this moment
		final rpc_message m2 = new rpc_message( true, false) {
			@Override
			public ArduinoQueue run() {
				Arduino.getInstance().throwError(string);
				return null;
			}
		};
		output.add( m2 );
	}

	public void addWaitGlass() {
		final rpc_message wait4glass = new rpc_message( true, true ) {
			@Override
			public ArduinoQueue run() {
				this.name				= "wait glass 2";
				final rpc_message m3	= this;
				final Handler handler	= new Handler();
				handler.postDelayed(new Runnable() {
				  @Override
				  public void run() {
					  Arduino ar 		= Arduino.getInstance();  
					  ar.unlock( m3 );
				  }
				}, 15000);						// przestan skanować po 15 sek

				return null;
			}
			public boolean isRet(String message) {	// czy to co przyszło jest zwrotką tej komendy
				int glass_weight = virtualComponents.getInt( "GLASS", 0 );
				int noglass_weight = virtualComponents.getInt( "NOGLASS_WEIGHT", 0 );
				if( noglass_weight + virtualComponents.weigh_min_diff < glass_weight ){		// jesc ciężej 
					return true;	
				}else{
					return false;
				}
			}
		};
		final rpc_message finishGlass = new rpc_message( true, false ) {
			@Override
			public ArduinoQueue run() {
				this.name				= "finish glass";
				int glass_weight = virtualComponents.getInt( "GLASS", 0 );
				int noglass_weight = virtualComponents.getInt( "NOGLASS_WEIGHT", 0 );
				if( noglass_weight + virtualComponents.weigh_min_diff < glass_weight ){		// jest ciężej 
				}else{		// nie ma szklanki wiec przerwij
					// error = nie ma szklanki
					boolean dd = true;
					if(dd){		// przerwij gdy nie ma szklanki
						Arduino.getInstance().clear();
					}else{			// wyświetl komunikat i poproś jeszcze raz
					}
				}
			//	final rpc_message m3	= this;
				return null;
			}
		};

		final rpc_message m2 = new rpc_message( true, true ) {
			@Override
			public ArduinoQueue run() {
				this.name		= "wait glass";
				ArduinoQueue q2	= new ArduinoQueue();
				q2.add("LIVE WEIGHT ON", true);
				q2.add("EX", true);
		//		q2.add("EY", true);
				q2.addWait(10);
		//		q2.add("SET LED5 ON", true);
				q2.add( wait4glass );					// jest szklanka lub nie ma
				q2.add("LIVE WEIGHT OFF", true);
				q2.add("DX", true);
				q2.add("DY", true);
				q2.add( finishGlass );					// jest szklanka lub nie ma
				return q2;
			}
		};
		// narazie nie sprawdzamy wagi
//		output.add( m2 );
/*
				
		int unsigned waga = read_szklanka();
		send2android("GLASS " + String(waga));
		if( waga < waga_zero + min_diff ){          // rozni się o mniej niż WAGA_MIN_DIFF
			unsigned int repeat = 0;
			while( repeat < WAGA_REPEAT_COUNT && (waga < waga_zero + min_diff) ){          // powtarzaj WAGA_REPEAT_CONUT razy
				digitalWrite( STATUS_GLASS_LED, HIGH );        // zapal ze chce szklankę
				delay(400);
				digitalWrite( STATUS_GLASS_LED, LOW );         // zgaś
				delay(300);
				waga = read_szklanka();
				send2android("GLASS " + String(waga));
			}
			//jak juz jest ok to i tak poczekaj sekundę na usunięcie ręki wkłądającej szklankę
			delay(2000);
			if(waga < waga_zero + min_diff ){      //nadal jest za mało
				return 0;
			}
		}
	*/
	}
	public void addWait(final rpc_message waitx) {	
		// czekaj na wykonanie tej komendy
		final rpc_message m2 = new rpc_message( true, true ) {
			@Override
			public ArduinoQueue run() {
				ArduinoQueue q2	= new ArduinoQueue();
				this.name		= "wait for " + waitx.toString();
			//	if(waitx){
			//	}
				return q2;
			}
		};
		output.add( m2 );
	}

}
