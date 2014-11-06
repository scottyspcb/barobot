package com.barobot.wizard.helpers;

import java.util.ArrayList;

public class SystemUnitTest {
	@SuppressWarnings("rawtypes")
	protected ArrayList<SystemTestItem> list	= new ArrayList<SystemTestItem>();

	@SuppressWarnings("rawtypes")
	public void put(SystemTestItem value) {
		this.list.add(value);
	}
	public int size() {
		return list.size();
	}
	@SuppressWarnings("rawtypes")
	public SystemTestItem get(int position) {
		return list.get(position);
	}

	@SuppressWarnings("rawtypes")
	public boolean checkAllOk() {
		boolean ok = true;
		for(SystemTestItem item : list){
			ok = ok && item.getResult();
		}
		return ok;
	}
}
