package com.barobot.gui.dataobjects;

import org.orman.mapper.Model;
import org.orman.mapper.annotation.Entity;
import org.orman.mapper.annotation.ManyToOne;
import org.orman.mapper.annotation.PrimaryKey;

@Entity
public class Ingredient_t extends Model<Ingredient_t>{
	@PrimaryKey(autoIncrement=true)
	public long id;
	
	@ManyToOne
	public Recipe_t recipe;
	
	@ManyToOne
	public Liquid_t liquid;
	
	public int quantity;
	public int ind;
	
	@Override
	public String toString() {
		return String.valueOf(quantity)+ "ml " + liquid.name;
	}
}
