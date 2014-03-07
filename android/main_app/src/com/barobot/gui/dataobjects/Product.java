package com.barobot.gui.dataobjects;

import org.orman.mapper.Model;
import org.orman.mapper.annotation.Entity;
import org.orman.mapper.annotation.ManyToOne;
import org.orman.mapper.annotation.PrimaryKey;

@Entity
public class Product extends Model<Product>{
	@PrimaryKey (autoIncrement = true)
	public long id;
	public int capacity;
	public boolean initNeeded;
	
	@ManyToOne
	public Liquid_t liquid;
	
	@Override
	public String toString() {
		String liquidName = "";
		String typeName  = "";
		
		if (liquid != null)
		{
			liquidName = liquid.name;
			
			if (liquid.type != null)
			{
				typeName = liquid.type.name;
			}
		}
		return String.valueOf(capacity) + " " + liquidName + " " + typeName;
	}
}
