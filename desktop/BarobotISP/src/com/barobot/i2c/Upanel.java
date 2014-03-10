package com.barobot.i2c;

import java.util.ArrayList;
import java.util.List;

import com.barobot.isp.Hardware;
import com.barobot.isp.IspSettings;
import com.barobot.isp.Main;
import com.barobot.parser.Queue;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.utils.Decoder;

public class Upanel extends I2C_Device_Imp {
	public Upanel can_reset_me_dev	= null;
	public I2C_Device have_reset_to	= null;
	public int have_reset_address	= -1;
	
	public static List<Upanel> list	= new ArrayList<Upanel>();
	public static int findByI2c(int device_add) {
		for (I2C_Device s : list){
			if(s.getAddress() == device_add ){
				return Upanel.list.indexOf(s);
			}
		}
		return -1;
	}
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
	private void hasResetTo(I2C_Device child) {
		this.have_reset_to	= child;
	}
	public void canResetMe( Upanel current_dev){
		this.can_reset_me_dev = current_dev;
	}

	public String reset(Hardware hw, boolean execute ) {
		String command = "";
		if(getIndex() > 0 ){
			command = "RESET"+ this.myindex;
			
		}else if( can_reset_me_dev == null ){
			command = "RESET_NEXT"+ can_reset_me_dev.getAddress();
		}
		if(execute){
			hw.send( command );
		}
		return command;
	}
	public void reset_next(Hardware hw) {
		if( this.myaddress > 0 ){
			hw.send("RESET_NEXT"+ this.myaddress );
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

	public void isp_next(Hardware hw) {	// pod³¹czony do mnie
		hw.send( "p"+ getAddress() );
	}
	

	private boolean hasNext = false;
	public boolean readHasNext(Hardware hw, Queue q ) {
		hasNext = false;
		String command = "h" + this.myaddress;
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
	//	System.out.println("has next?" + (hasNext ? "1" : "0"));
		return hasNext;
	}

	public int resetNextAndReadI2c(Hardware hw, Queue q) {
		have_reset_address = -1;
		String command = "RESET_NEXT"+ this.myaddress;
		q.add( new AsyncMessage( command, true ){
			public boolean isRet(String result) {
				if(result.startsWith("12,")){		//	12,18,19,1
					int[] bytes = Decoder.decodeBytes(result);	// HELLO, ADDRESS, TYPE, VERSION
					have_reset_address = bytes[1];
					return true;
				}
				return false;
			}
		});
		q.addWaitThread(Main.mt);
		return have_reset_address;
	}

	public int resetAndReadI2c(Hardware hw, Queue q ) {
		myaddress = -1;
		String command = this.reset( hw, false );
		q.add( new AsyncMessage( command, true ){
			public boolean isRet(String result) {
				if(result.startsWith("12,")){		//	12,18,19,1
					int[] bytes = Decoder.decodeBytes(result);	// HELLO, ADDRESS, TYPE, VERSION
					myaddress = bytes[1];
					return true;
				}
				return false;
			}
		});
		q.addWaitThread(Main.mt);
		return myaddress;
	}

	public String getHexFile() {
		return IspSettings.upHexPath;
	}

}
