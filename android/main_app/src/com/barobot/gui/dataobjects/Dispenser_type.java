package com.barobot.gui.dataobjects;


import org.orman.mapper.Model;
import org.orman.mapper.annotation.Entity;
import org.orman.mapper.annotation.PrimaryKey;

import com.barobot.other.LangTool;

@Entity
public class Dispenser_type extends Model<Dispenser_type>{

	@PrimaryKey(autoIncrement=true)
	public int id;
	public int capacity;
	public int pour_time;
	public String allow_capacity;
	public String name;

	public String getName() {
		return LangTool.translateName(id, "dispenser_type", name );
	}

	@Override
	public String toString() {
		return getName();
	}
}
