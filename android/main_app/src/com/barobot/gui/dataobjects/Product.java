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

	public String getName() {
		String liquidName = "";
		String typeName  = "";
		
		if (liquid != null)
		{
			liquidName = liquid.getName();
			
			if (liquid.type != null)
			{
				typeName = liquid.type.getName();
			}
		}
		return String.valueOf(capacity) + " " + liquidName + " " + typeName;
	}
	@Override
	public String toString() {
		return getName();
	}
}
