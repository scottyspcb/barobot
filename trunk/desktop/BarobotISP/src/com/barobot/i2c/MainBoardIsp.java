package com.barobot.i2c;

import com.barobot.common.constant.Constant;
import com.barobot.common.constant.Methods;
import com.barobot.hardware.devices.i2c.MainboardI2c;
import com.barobot.isp.Main;
import com.barobot.parser.Queue;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.utils.Decoder;

public class MainBoardIsp extends MainboardI2c {
	public MainBoardIsp() {
		super(Constant.mdefault_index, Constant.mdefault_address);
	}
	public MainBoardIsp(int index, int address) {
		super(index, address);
	}

	private boolean hasNext = false;
	public boolean readHasNext( Queue q, int index) {
		hasNext = false;
		String command = "N" + index;
		q.add( new AsyncMessage( command, true ){
			@Override
			public boolean isRet(String result, Queue q) {
				if(result.startsWith("" + Methods.METHOD_I2C_SLAVEMSG + ",")){		//	122,1,188,1
					int[] bytes = Decoder.decodeBytes(result);
					if(bytes[2] == Methods.METHOD_CHECK_NEXT  ){
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
}
