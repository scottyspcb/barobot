package com.barobot.i2c;

import com.barobot.common.constant.Methods;
import com.barobot.hardware.devices.i2c.Upanel;
import com.barobot.isp.Main;
import com.barobot.parser.Queue;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.utils.Decoder;

public class UpanelIsp extends Upanel {
	private boolean hasNext = false;

	public UpanelIsp(int index, int address ){
		super(index, address);
	}
	public UpanelIsp(int index, int address, Upanel parent ){
		super(index, address, parent);
	}
	public UpanelIsp(int index, int address, int order ){
		super(index, order);
		setAddress(address);
	}
	public boolean readHasNext( Queue q ) {
		hasNext = false;
		String command = "n" + this.myaddress;
		q.add( new AsyncMessage( command, true ){
			@Override
			public boolean isRet(String result, Queue q) {
				if(result.startsWith("" + Methods.METHOD_I2C_SLAVEMSG + ",")){		//	122,1,188,1
					int[] bytes = Decoder.decodeBytes(result);
					if(bytes[2] == Methods.METHOD_CHECK_NEXT ){
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

	public int resetNextAndReadI2c(Queue q) {
		have_reset_address = -1;
		String command = "RESET_NEXT"+ this.myaddress;
		q.add( new AsyncMessage( command, true ){
			@Override
			public boolean isRet(String result, Queue q) {
				if(result.startsWith(""+ Methods.METHOD_DEVICE_FOUND +",")){		//	112,18,19,1
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

	public int resetAndReadI2c( Queue q ) {
		myaddress = -1;
		String command = this.reset( q, false );
		q.add( new AsyncMessage( command, true ){
			@Override
			public boolean isRet(String result, Queue q) {
				if(result.startsWith(""+ Methods.METHOD_DEVICE_FOUND +",")){		//	112,18,19,1
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
}
