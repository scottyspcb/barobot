package com.barobot.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.barobot.BarobotMain;
import com.barobot.R;
import com.barobot.hardware.rpc_message;
import com.barobot.wire.ADB_wire;
import com.barobot.wire.BT_wire;
import com.barobot.wire.Serial_wire;
import com.barobot.wire.Wire;

public class Arduino{
	private static Arduino instance = null;
	private LinkedList<rpc_message> output2 = new LinkedList<rpc_message>();
	private rpc_message wait_for = null;

	//private ArrayList <message> output3 = new ArrayList <message>();
	//private static Queue<String> input = new LinkedList<String>();
	Wire connection = null;
	public boolean stop_autoconnect = false;
	public List<History_item> mConversationHistory;
	public static Arduino getInstance(){
		if (instance == null)
		{
			instance = new Arduino();
		}
		return instance;
	}

	private Arduino() {
		instance		= this;
		mConversationHistory = new ArrayList<History_item>();
		//mConversationArrayAdapter = new ArrayAdapter<History_item>(barobotMain, R.layout.message);
	}
	public void onStart(final BarobotMain barobotMain) {
		if( connection == null ){
			AlertDialog.Builder builder = new AlertDialog.Builder(barobotMain);
			String[] name = new String[3];
			final Wire lowHardware[]=  new Wire[3];  
			lowHardware[0]	= new Serial_wire();
			lowHardware[1]	= new ADB_wire();
			lowHardware[2]	= new BT_wire();
	
			name[0] = lowHardware[0].getName();
			name[1] = lowHardware[1].getName();
			name[2] = lowHardware[2].getName();
			builder.setTitle("Wybierz typ połączenia z robotem");
			builder.setCancelable(false);
			builder.setItems(name, new DialogInterface.OnClickListener() {
				@Override
		     	public void onClick(DialogInterface dialog, int which) {
			          switch(which){
			             case 0:
			            	 prepareConnection(lowHardware[0]);
			            	 break;
			             case 1:
			            	 prepareConnection(lowHardware[1]);
			            	 break;
			             case 2:
			            	 prepareConnection(lowHardware[2]);
			            	 break;
			             default:
			            	 barobotMain.finish();
			            	 break;
			          }
			      }
			});
			builder.show();        
		}
	}

    protected void prepareConnection(Wire lowHardware) {
    	if(connection !=null){
    		connection.destroy();
    	}
   	 	connection = lowHardware;
    	connection.init();
       	if( connection.implementAutoConnect()){
        	this.runTimer();
        }
       	this.sendSomething();
	}

   	private boolean stopping = false;
    private void sendSomething(){
   		stopping = false;

   		Log.d("serial", "sendSomething");
	    Runnable tt = new Runnable(){
	        @Override
	        public void run() {
	            Random generator = new Random( 19580427 );
	            Log.d("serial", "Start writter");
	            while(!stopping && connection != null && connection.isConnected() ){
	                int r = generator.nextInt();
	                String test = "hello arduino "+ r + "\n";
	                send(test);
	                try {
	                    Thread.sleep(500);
	                } catch (InterruptedException e) {
	                    e.printStackTrace();
	                }
	            }
	            Log.d("serial", "koniec writter");
	        }};
	        Thread writer = new Thread(tt);
	        writer.start();
    }

    private void runTimer() {
//    	interval inn = new interval();
//   	inn.run(1000,5000);
//    	this.inters.add(inn);
    	interval inn = new interval(new Runnable() {
    		private int count = 0;
		    public void run() {
		    	Arduino ar = Arduino.getInstance();
		        if( ar.allowAutoconnect()){
		        	count++;
		        	if(count > 2){		// po 10 sek
		//        		Constant.log("RUNNABLE", "3 try autoconnect" );
		        		connection.setAutoConnect( true ); 
		        	}
			    }else{
			    	count = 0;
		        }
		   }
		});
    	inn.run(1000,5000);
    	inn.pause();
    	BarobotMain.getInstance().inters.add(inn);
	}

	public void destroy() {
		this.clear();
	   	stopping = true;
		if(connection!=null){
			connection.destroy();
		}
        Constant.log(Constant.TAG, "--- ON DESTROY ---");
	}
	public void resume() {
		stopping = false;
		if(connection!=null){
			connection.resume();
		}
	}
	public Wire getConnection() {
		return connection;
	}
	
	public boolean allowAutoconnect() {
		if( connection.isConnected() ){
		//	Constant.log(Constant.TAG, "nie autoconnect bo juz połączony");
			return false;
		}
		if( !connection.implementAutoConnect() ){
			Constant.log(Constant.TAG, "nie autoconnect bo !canAutoConnect");
			return false;
		}
		if( !connection.canConnect() ){
			Constant.log(Constant.TAG, "nie autoconnect bo !canConnect");
			return false;
		}
		if (stop_autoconnect == true ) {
			Constant.log(Constant.TAG, "nie autoconnect bo STOP");
			return false;
		}
		return true;
	}

    public boolean checkBT() {
    	return connection.canConnect();
    }
	public void setupBT(BarobotMain barobotMain) {
		if(connection!=null){
			connection.setup();
			if(this.allowAutoconnect()){
				connection.setAutoConnect( true ); 
			}
		}
	}
	public void send( String message ){
		rpc_message m = new rpc_message( message, true, false );
		output2.add( m );
		exec();
	}
	public void sendFirst(ArduinoQueue q2) {
		this.output2.addAll( 0, q2.output);		// dodja na począku, reszte przesun dalej
		exec();		
	}
	public void send(ArduinoQueue q) {
		this.output2.addAll(q.output);
		BarobotMain.getInstance().cm.doPhoto();
		exec();
	}
	public boolean read_ret(String retm) {	// czy moze to jest zwrotka
		boolean is_ret = false;
		if( this.wait_for != null){
			//Constant.log("isRet?", "["+message+"][" +  this.wait_for.command+"]");
			is_ret = true;
			if(this.wait_for.isRet(retm)){
				this.wait_for = null;
				if(output2.isEmpty()){
		           	addToList( "--------------------------------------------------", true );
				}else{
					exec();		// wyslij wszystko co jest dalej
				}
			}
		}
		return is_ret;
	}
	private synchronized void exec(){
		if(this.wait_for != null){
			return;		// jestem w trakcie oczekiwania
		}
		if(connection == null){
			return;		// jestem w trakcie oczekiwania
		}
		try	{
			boolean wasEmpty = output2.isEmpty();
			while (!output2.isEmpty()) {
				rpc_message m = output2.pop();
				if( m.command == null){
					m.start( this );
				}else{
					connection.send(m.command);
				}
		        addToList( m );
				if(m.isBlocing()){		// czekam na zwrotkę tej komendy zanim wykonam coś dalej
					m.send_timestamp	= System.nanoTime();
                	this.wait_for		= m;
                	return;					// przerwij do czasu otrzymania zwrotki lub odblokowania
                }else{
                	this.wait_for = null;
                }
			}
			if(!wasEmpty){
            	addToList( "--------------------------------------------------", true );
			}
		} catch (IOException e)	{
			Constant.log(Constant.TAG, "problem sending TCP message",e);
		}
	}
    public void unlock() {
    	if(wait_for!=null){
    		Constant.log("+unlock", "czekalem na [" + wait_for.command+"] w kolejce: " +output2.size() );
    		this.wait_for = null;
    		this.exec();
    	}
    }
	public void unlock(rpc_message m) {
		if( this.wait_for != null && this.wait_for.equals(m)){
			this.wait_for = null;
			this.exec();
		}
	}
    public void clear() {
		this.output2.clear();
		this.wait_for = null;
	}

    public boolean log_active	= true;

	public void addToList(final rpc_message m) {
		if(log_active){
			mConversationHistory.add( m );
		}
	}
	public void addToList(final String string, final boolean direction ) {
		if(log_active){
			BarobotMain.getInstance().runOnUiThread(new Runnable() {
			     public void run() {
			    	 mConversationHistory.add( new History_item( string.trim(), direction) );
			    }
			});
			
		}
	}
	public void clearHistory() {
		if(log_active){
			mConversationHistory.clear();
		}
	}
	
	public List<History_item> getHistory(){
		return mConversationHistory;
	}
	public void connectId(String address) {
		Constant.log(Constant.TAG, "autoconnect z: " +address);
		if(connection!=null){
			connection.connectToId(address);
		}
	}
}

/*
public boolean addRetToList( final String last, final String ret ) {
	final DebugWindow dd = DebugWindow.getInstance();
	if(dd!=null){
		int count = dd.mConversationArrayAdapter.getCount();
		for(int i =count-1; i>=0;i--){
			History_item hi = dd.mConversationArrayAdapter.getItem(i);
		//	Constant.log("+addRetToList", last + "/" + ret + "/" + i +"/"+ hi.getCommand() );
			if( hi.direction && hi.getCommand().equals(last)){
				hi.setRet(ret);
	//			Constant.log("+addRetToList","ustawiam " + i + " na " + hi.toString() );
				dd.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						dd.mConversationArrayAdapter.notifyDataSetChanged();
					}
				});
				return true;
			}			
		}
	}
	return false;
}
*/
