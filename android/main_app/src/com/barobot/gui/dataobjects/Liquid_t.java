package com.barobot.gui.dataobjects;

import org.orman.mapper.Model;
import org.orman.mapper.annotation.Entity;
import org.orman.mapper.annotation.ManyToOne;
import org.orman.mapper.annotation.PrimaryKey;

@Entity
public class Liquid_t extends Model<Liquid_t> {
	@PrimaryKey (autoIncrement = true)
	public long id;
	public String name;
	public float voltage;
	
	@ManyToOne
	public Type type;
}
