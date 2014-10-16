package com.barobot.gui.dataobjects;

import org.orman.mapper.Model;
import org.orman.mapper.annotation.Entity;
import org.orman.mapper.annotation.PrimaryKey;

@Entity
public class Version extends Model<Version>{

	@PrimaryKey(autoIncrement=true)
	public long id;
	public String name;
	public int value;

	public String getName(){
		return name;
	}
	@Override
	public String toString() {
		return getName();
	}
}
