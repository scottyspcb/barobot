package com.barobot.gui.dataobjects;

import org.orman.mapper.Model;
import org.orman.mapper.annotation.Entity;
import org.orman.mapper.annotation.ManyToOne;
import org.orman.mapper.annotation.PrimaryKey;

@Entity
public class Slot extends Model<Slot>{
	@PrimaryKey
	public long position;
	public String status;
	public String dispenserType;
	public int currentVolume;
	
	@ManyToOne
	public Product product;
}
