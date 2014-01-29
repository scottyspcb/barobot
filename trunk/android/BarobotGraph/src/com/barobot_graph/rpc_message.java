package com.barobot_graph;

public class rpc_message {
	protected String name = "";
	public rpc_message(String cmd ) {
		this.command = cmd; // true = na zewnątrz
	}
	public String toString() {
		return command;
	}
	public String command = "";
}
