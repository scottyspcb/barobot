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

		/*
		 * debugConnection = lowHardware2; debugConnection.init(); if(
		 * debugConnection.implementAutoConnect()){
		 * this.runTimer(debugConnection); }
		 */
		// this.sendSomething();
	}

	private boolean stopping = false;

	public void destroy() {
		this.clear();
		stopping = true;
		if (connection != null) {
			connection.destroy();
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
					rpc_message m = output2.pop();
					String command = m.command + input_parser.separator;
					connection.send(command);
				}
			} catch (IOException e) {
				Constant.log(Constant.TAG, "problem sending TCP message", e);
			}
		}
	}
	public void clear() {
		synchronized (this.lock) {
			this.output2.clear();
		}
	}
}

