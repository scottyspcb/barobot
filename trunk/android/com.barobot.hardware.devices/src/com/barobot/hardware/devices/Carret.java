package com.barobot.hardware.devices;

import com.barobot.parser.Queue;

public class Carret {
	protected int myaddress = 0;
	protected int myindex = 0;
	protected int order = -1;
	public Carret(){
	}
	public Carret(int index, int address ){
		this();	// call default constructor
		this.setAddress(address);
		this.setIndex(index);
	}
	
	public void setLed(Queue q, String selector, int pwm) {
		String command = "L" +myaddress + ","+ selector +"," + pwm;
		q.add( command, true );
	}
	
	public void setAddress(int myaddress) {
		this.myaddress = myaddress;
	}

	public int getAddress() {
		return myaddress;
	}

	public void setIndex(int myindex) {
		this.myindex = myindex;
	}

	public int getIndex() {
		return myindex;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public int getOrder() {
		return order;
	}
}
