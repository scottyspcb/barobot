package com.barobot.hardware.devices.i2c;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class I2C{
	private List<Upanel> list		= new ArrayList<Upanel>();
	private static Map<Integer, Upanel> bybottle = new HashMap<Integer, Upanel>();

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
