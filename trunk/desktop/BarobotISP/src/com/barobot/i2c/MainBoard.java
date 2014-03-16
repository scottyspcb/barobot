package com.barobot.i2c;

import com.barobot.common.IspSettings;

import com.barobot.isp.Main;
import com.barobot.parser.Queue;
import com.barobot.parser.devices.I2C_Device_Imp;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.utils.Decoder;

public class MainBoard extends I2C_Device_Imp {
	private int default_address	= 0x01;
	private int default_index	= 1;

	public MainBoard(){
		this.cpuname		= "m328p";
		this.myaddress		= default_address;
		this.myindex		= default_index;
		this.protocol		= "arduino";
	//	this.bspeed			= 57600;		// arduino bootloader
		this.bspeed			= 115200;		// optiboot can faster
	}
	public String setFuseBits(Queue q) {
		return "";
	}
	public String getReset() {
		return "RESET"+ this.myindex;
	}
	public String getIsp() {
		return "RESET"+ this.myindex;
	}
	public String getHexFile() {
		return IspSettings.mbHexPath;
	}

	private boolean hasNext = false;
	public boolean readHasNext( Queue q, int index) {
		hasNext = false;
		String command = "N" + index;
		q.add( new AsyncMessage( command, true ){
			public boolean isRet(String result) {
				if(result.startsWith("122,")){		//	122,1,188,1
					int[] bytes = Decoder.decodeBytes(result);
					if(bytes[2] == 188){
						if(bytes[3] == 1 ){							// has next
							hasNext = true;
						}
						return true;
					}
				}
				return false;
			}
		});
		q.addWaitThread(Main.mt);
		System.out.println("has next?" + (hasNext ? "1" : "0"));
		return hasNext;
	}
	public void moveX(int max) {
	}
}
