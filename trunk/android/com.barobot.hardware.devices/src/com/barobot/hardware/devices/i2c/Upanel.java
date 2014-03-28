package com.barobot.hardware.devices.i2c;

import com.barobot.common.IspSettings;
import com.barobot.parser.Queue;

public class Upanel extends I2C_Device_Imp {
	public static final int FRONT = 4;
	public static final int BACK = 3;
	public static int[] upanelIndex2order = {0,2,4,6,8,10,1,3,5,7,9,11};
	public static int[] order2upanelIndex = {0,6,1,7,2,8,3,9,4,10,5,11};
	public static int[][] row2index = {
		{0,0,0,0,0,0},
		{0,0,0,0,0,0},
		{0,0,0,0,0,0},
		{0,2,4,6,8,10},		// BACK
		{1,3,5,7,9,11}		// FRONT
	};

	public Upanel can_reset_me_dev	= null;
	public I2C_Device have_reset_to	= null;
	public int row	= 0;
	private int inRow;

	public Upanel(){
		this.cpuname	= "atmega8";
		this.lfuse		= "0xA4";
		this.hfuse		= "0xC7";
		this.lock		= "0x3F";
		this.efuse		= "";
	}

	public Upanel(int index, int address, Upanel parent ){
		this();	// call default constructor
		this.setAddress(address);
		this.setIndex(index);
		this.can_reset_me_dev	= parent;
		parent.hasResetTo(this);
	}

	public int getBottleNum() {
		if( order > -1 ){
			return order;
		}else if( row < 5 && inRow < 6){
			order = row2index[row][inRow];
			return order;
		}
		return -1;
	}

	public void hasResetTo(I2C_Device child) {
		this.have_reset_to	= child;
	}
	public void canResetMe( Upanel other){
		this.can_reset_me_dev = other;
		other.hasResetTo(this);
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
			return "RESET"+ this.myindex;
		}else if( can_reset_me_dev == null ){
			return "RESET_NEXT" + can_reset_me_dev.getAddress();
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
	public void setRow(int row) {
		this.row = row;
	}
	public void setInRow(int inrow) {
		this.inRow = inrow;
	}
	public int getRow() {
		return this.row;
	}
	public int getInRow() {
		return this.inRow;
	}
}
