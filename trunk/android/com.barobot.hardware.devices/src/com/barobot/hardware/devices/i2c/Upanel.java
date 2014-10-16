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
		this.setRow(index);
		this.canBeResetedBy	= parent;
		parent.isResetedBy(this);
	}
	public int getBottleNum() {
		if( row >=0 && row < 5 && numInRow < 6 && numInRow >= 0){
			return row2index[row][numInRow];
		}
		return -1;
	}
	public void isp_next(Queue q) {	// pod��czony do mnie
		q.add( "p"+ getAddress(), "SISP" );
	}
	public void reset_next(Queue q) {
		if( this.myaddress > 0 ){
			q.add("RESET_NEXT"+ this.myaddress, true );
		}
	}
	public String getHexFile() {
		return IspSettings.upHexPath;
	}
}
