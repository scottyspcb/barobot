package com.barobot.hardware.devices.i2c;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.barobot.common.constant.Constant;
import com.barobot.common.constant.LowHardware;
import com.barobot.common.constant.Methods;
import com.barobot.parser.Queue;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.message.Mainboard;

public class I2C{
	public Carret carret			= null;
	private List<Upanel> list		= new ArrayList<Upanel>();
	private static Map<Integer, Upanel> bybottle = new HashMap<Integer, Upanel>();

	public I2C() {
		super();
		carret		= new Carret(Constant.cdefault_index, Constant.cdefault_address);	
	}

	public Upanel getUpanelByBottle( int num ) {
		return bybottle.get(num);
	}
	public void add(Upanel u) {
		this.list.add(u);
		u.onchange( new Runnable(){
			@Override
			public void run() {
				reloadIndex();
			}
		});
		reloadIndex();
	}

	public Upanel[] getUpanels() {
		Upanel[] a = {};
		return bybottle.values().toArray(a);
	}
	public I2C_Device[] getDevicesWithLeds() {
		I2C_Device[] a = {};
		return list.toArray(a);
	}
	public void clear() {		
		list.clear();
		bybottle.clear();
	}

	public void reloadIndex() {
		bybottle.clear();
		for (I2C_Device u2 : list){
			if(  u2 instanceof Upanel ){
				Upanel uu = (Upanel)u2;
				bybottle.put( uu.getBottleNum(), uu );	// index by BUTTLE NUM
			}
		}
	}
}
