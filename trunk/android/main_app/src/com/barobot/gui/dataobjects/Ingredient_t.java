package com.barobot.gui.dataobjects;

import java.util.List;

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
	
	public String getName() {
		return String.valueOf(quantity)+ "ml " + liquid.getName();
	}
	@Override
	public String toString() {
		return getName();
	}
	public static int getSize(List<Ingredient_t> ingredients) {
		int size	= 0;
		for(Ingredient_t ing : ingredients)
		{
			Slot slot = Engine.GetInstance().getIngredientSlot(ing);
			if (slot != null){
				int count = slot.getSequence( ing.quantity );
				size		+= count * slot.dispenser_type;
			}
		}
		return size;
	}
}
