package com.barobot.gui.dataobjects;

import org.orman.mapper.Model;
import org.orman.mapper.annotation.Entity;
import org.orman.mapper.annotation.ManyToOne;
import org.orman.mapper.annotation.PrimaryKey;

@Entity
public class Slot extends Model<Slot>{
	public static final String STATUS_EMPTY = "Empty"; 
	
	@PrimaryKey(autoIncrement=true)
	public long id;
	public int position;
	public String status;
	public String dispenserType;
	public int currentVolume;
	
	@ManyToOne
	public Product product;
	
	public String GetName()
	{
		if (product != null)
		{
			return product.liquid.name;
		}
		else
		{
			return status;
		}
	}
}
