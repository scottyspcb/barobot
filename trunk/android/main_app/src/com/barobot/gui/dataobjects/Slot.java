package com.barobot.gui.dataobjects;

import org.orman.mapper.Model;
import org.orman.mapper.annotation.Entity;
import org.orman.mapper.annotation.ManyToOne;
import org.orman.mapper.annotation.PrimaryKey;

@Entity
public class Slot extends Model<Slot>{
	public static String STATUS_EMPTY = "Empty"; 

	@PrimaryKey(autoIncrement=true)
	public long id;
	public int position;
	public String status;
	public int currentVolume;
	public int dispenser_type;

	public int row_id;
	public int num_in_row;
	public int margin;
	public int upanel_address;
	public int position_id;

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
