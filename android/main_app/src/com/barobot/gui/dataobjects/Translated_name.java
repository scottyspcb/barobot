package com.barobot.gui.dataobjects;

import org.orman.mapper.Model;
import org.orman.mapper.annotation.Entity;
import org.orman.mapper.annotation.PrimaryKey;

@Entity
public class Translated_name extends Model<Translated_name>{
	@PrimaryKey(autoIncrement=true)
	public int id;
	public int element_id;
	public String table_name;
	public int language_id;
	public String translated;

	public String getName() {
		return translated;
	}
	@Override
	public String toString() {
		return getName();
	}
}
