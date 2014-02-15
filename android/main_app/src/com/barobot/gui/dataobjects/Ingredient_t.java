package com.barobot.gui.dataobjects;

import org.orman.mapper.Model;
import org.orman.mapper.annotation.Entity;
import org.orman.mapper.annotation.ManyToOne;

@Entity
public class Ingredient_t extends Model<Ingredient_t>{
	@ManyToOne
	public Recipe_t recipe;
	
	@ManyToOne
	public Liquid_t liquid;
	
	public int quantity;
	public int ind;
}
