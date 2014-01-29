package com.barobot_graph;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import android.widget.ArrayAdapter;

import com.barobot_graph.wire.BT_wire;
import com.barobot_graph.wire.Serial_wire;
import com.barobot_graph.wire.Wire;

public class Arduino {
	private final Object lock = new Object();
	private static Arduino instance = null;
	private LinkedList<rpc_message> output2 = new LinkedList<rpc_message>();
	Wire connection = null;
	Wire debugConnection = null;
	public boolean stop_autoconnect = false;
	public static Arduino getInstance() {
		if (instance == null) {
			instance = new Arduino();
		}
		return instance;
	}

	private Arduino() {
		instance = this;
		// mConversationArrayAdapter = new
		// ArrayAdapter<History_item>(barobotMain, R.layout.message);
	}

	public void onStart(final BGraph barobotMain) {
		if (connection != null) {
			connection.disconnect();
			connection.destroy();
			connection = null;
		}
		if (connection == null) {
			final Wire lowHardware[] = new Wire[1];
			lowHardware[0] = new Serial_wire();
			prepareConnection(lowHardware[0], new BT_wire());
		}
	}

	protected void prepareConnection(Wire lowHardware, Wire lowHardware2) {
		if (connection != null) {
			connection.destroy();
		}
		connection = lowHardware;
		connection.init();

		if (debugConnection != null) {
			debugConnection.destroy();
		}

		/*
		 * debugConnection = lowHardware2; debugConnection.init(); if(
		 * debugConnection.implementAutoConnect()){
		 * this.runTimer(debugConnection); }
		 */
		// this.sendSomething();
	}

	private boolean stopping = false;

	/*
	 * 
	 * private void sendSomething(){ stopping = false;
	 * 
	 * Log.d("serial", "sendSomething"); Runnable tt = new Runnable(){
	 * 
	 * @Override public void run() { Random generator = new Random( 19580427 );
	 * Log.d("serial", "Start writter"); while(!stopping && connection != null
	 * ){ if( connection.isConnected()){ int r = generator.nextInt(); String
	 * test = "hello arduino "+ r + "\n"; send(test); try { Thread.sleep(500); }
	 * catch (InterruptedException e) { e.printStackTrace(); } } }
	 * Log.d("serial", "koniec writter"); }}; Thread writer = new Thread(tt);
	 * writer.start(); }
	 * 
	 * private void runTimer( final Wire connection ) { // interval inn = new
	 * interval(); // inn.run(1000,5000); // this.inters.add(inn); interval inn
	 * = new interval(new Runnable() { private int count = 0; public void run()
	 * { Arduino ar = Arduino.getInstance(); if( ar.allowAutoconnect()){
	 * count++; if(count > 2){ // po 10 sek // Constant.log("RUNNABLE",
	 * "3 try autoconnect" ); connection.setAutoConnect( true ); } }else{ count
	 * = 0; } } }); inn.run(1000,5000); inn.pause();
	 * AppInvoker.getInstance().inters.add(inn); }
	 */
	public void destroy() {
		this.clear();
		stopping = true;
		if (connection != null) {
			connection.destroy();
		}
		if (debugConnection != null) {
			debugConnection.destroy();
		}
		output2.clear();
		output2.clear();

		Constant.log(Constant.TAG, "--- ON DESTROY ---");
	}

	public void resume() {
		stopping = false;
		if (connection != null) {
			connection.resume();
		}
		if (debugConnection != null) {
			debugConnection.resume();
		}
	}

	/*
	 * public Wire getConnection() { return connection; }
	 */
	public boolean allowAutoconnect() {
		if (debugConnection == null) {
			// Constant.log(Constant.TAG, "nie autoconnect bo juz połączony");
			return false;
		}
		if (debugConnection.isConnected()) {
			// Constant.log(Constant.TAG, "nie autoconnect bo juz połączony");
			return false;
		}
		if (!debugConnection.implementAutoConnect()) {
			Constant.log(Constant.TAG, "nie autoconnect bo !canAutoConnect");
			return false;
		}
		if (!debugConnection.canConnect()) {
			Constant.log(Constant.TAG, "nie autoconnect bo !canConnect");
			return false;
		}
		if (stop_autoconnect == true) {
			Constant.log(Constant.TAG, "nie autoconnect bo STOP");
			return false;
		}
		return true;
	}

	public boolean checkBT() {
		return debugConnection != null && debugConnection.canConnect();
	}

	public void setupBT(BGraph barobotMain) {
		/*
		 * if(connection!=null){ connection.setup();
		 * if(this.allowAutoconnect()){ connection.setAutoConnect( true ); } }
		 */
		if (debugConnection != null) {
			debugConnection.setup();
			if (this.allowAutoconnect()) {
				debugConnection.setAutoConnect(true);
			}
		}
	}

	public void send(String message) {
		rpc_message m = new rpc_message(message);
		output2.add(m);
		Constant.log("send1", message);
		exec();
	}

	private synchronized void exec() {
		synchronized (this.lock) {
			if (connection == null) {
				return; // jestem w trakcie oczekiwania
			}
			try {
				Constant.log("exec", "" + output2.size());

				while (!output2.isEmpty()) {
					Constant.log("while", "start");
					rpc_message m = output2.pop();
					Constant.log("while", "start2" + m.command );
					Constant.log("serial send", m.command);
					Constant.log("while", "start3" + m.command );

					String command = m.command + input_parser.separator;
					Constant.log("while", "start4");
					connection.send(command);
			//		debug(command);
				}
			} catch (IOException e) {
				Constant.log(Constant.TAG, "problem sending TCP message", e);
			}
		}
	}

	public synchronized void low_send(String command) throws IOException { // wyslij														// interpretacji
		if (connection == null) {
			return; // jestem w trakcie oczekiwania
		}
		Constant.log("low_send", command);
		connection.send(command);
	}

	public synchronized void debug(String command) { // wyslij bez interpretacji
		if (debugConnection != null) {
			try {
				debugConnection.send(command);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void clear() {
		synchronized (this.lock) {
			this.output2.clear();
		}
	}

	public void connectId(String address) {
		Constant.log(Constant.TAG, "autoconnect z: " + address);
		if (debugConnection != null) {
			debugConnection.connectToId(address);
		}
	}
	
}

