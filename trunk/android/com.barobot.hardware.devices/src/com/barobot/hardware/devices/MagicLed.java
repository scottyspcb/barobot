package com.barobot.hardware.devices;

public class MagicLed {
	public static int[] bottleUpperToNum = {14,0,15,1,16,2,17,3,18,4,19,5};
	public static int[] bottleDownToNum = {-1,11,-1,10,-1,9,-1,8,-1,7,-1,6};
	public int getUpLedNum( byte bottle_num ){
		if(bottle_num >= bottleUpperToNum.length){
			return -1;
		}
		return bottleUpperToNum[bottle_num];
	}
	public int getDownLedNum( byte bottle_num ){
		if(bottle_num >= bottleDownToNum.length){
			return -1;
		}
		return bottleUpperToNum[bottle_num];
	}
}
