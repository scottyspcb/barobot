package com.barobot.hardware.devices.i2c;

import java.util.ArrayList;
import java.util.List;

import com.barobot.common.IspSettings;
import com.barobot.parser.Queue;

public class Upanel extends I2C_Device_Imp {
	public Upanel can_reset_me_dev	= null;
	public I2C_Device have_reset_to	= null;

	public static List<Upanel> list	= new ArrayList<Upanel>();
	
	public Upanel(){
		this.cpuname	= "atmega8";
		this.lfuse		= "0xA4";
		this.hfuse		= "0xC7";
		this.lock		= "0x3F";
		this.efuse		= "";
	}
	public Upanel(int index, int address ){
		this();	// call default constructor
		this.setAddress(address);
		this.setIndex(index);
	}
	public Upanel(int index, int address, Upanel parent ){
		this();	// call default constructor
		this.setAddress(address);
		this.setIndex(index);
		this.can_reset_me_dev	= parent;
		parent.hasResetTo(this);
	}
	public void hasResetTo(I2C_Device child) {
		this.have_reset_to	= child;
	}
	public void canResetMe( Upanel current_dev){
		this.can_reset_me_dev = current_dev;
	}
	public String reset(Queue q, boolean execute ) {
		String command = "";
		if(getIndex() > 0 ){
			command = "RESET"+ this.myindex;
		}else if( can_reset_me_dev == null ){
			command = "RESET_NEXT"+ can_reset_me_dev.getAddress();
		}
		if(execute){
			q.add( command, true );
		}
		return command;
	}
	public void reset_next(Queue q) {
		if( this.myaddress > 0 ){
			q.add("RESET_NEXT"+ this.myaddress, true );
		}
	}
	public String getReset() {
		if(getIndex() > 0 ){
			return "P"+ this.myindex;
		}else if( can_reset_me_dev == null ){
			return "p"+ can_reset_me_dev.getAddress();
		}
		return "";
	}
	public String getIsp() {
		return "RESET"+ this.myindex;
	}

	public void isp_next(Queue q) {	// pod³¹czony do mnie
		q.add( "p"+ getAddress(), false );
	}

	public String getHexFile() {
		return IspSettings.upHexPath;
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
	public static int findByI2c(int device_add) {
		for (I2C_Device s : list){
			if(s.getAddress() == device_add ){
				return Upanel.list.indexOf(s);
			}
		}
		return -1;
	}
}
