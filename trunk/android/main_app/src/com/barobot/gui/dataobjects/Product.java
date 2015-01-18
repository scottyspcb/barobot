package com.barobot.gui.dataobjects;

import org.orman.mapper.Model;
import org.orman.mapper.annotation.Entity;
import org.orman.mapper.annotation.ManyToOne;
import org.orman.mapper.annotation.PrimaryKey;

import com.barobot.gui.database.BarobotData;

@Entity
public class Product extends Model<Product>{
	@PrimaryKey (autoIncrement = true)
	public long id;
	public int capacity;
	public boolean initNeeded;

	@ManyToOne
	public Liquid_t liquid;

	public String getName() {
		if (liquid == null){
			return String.valueOf(capacity) + "ml";
		}
		String liquidName = liquid.getName();
		if( liquid.type == 0 ){
			return liquidName + "" + String.valueOf(capacity) + "ml";
		}
		return liquidName + " " + liquid.getLiquidType().getName() + " " + String.valueOf(capacity) + "ml";
	}
	@Override
	public String toString() {
		return getName();
	}

	@Override
	public void insert() {
		super.insert();
		BarobotData.reportChange( this.getClass(), this.id );
	}
	@Override
	public void update() {
		super.update();
		BarobotData.reportChange( this.getClass(), this.id );
	}
}

