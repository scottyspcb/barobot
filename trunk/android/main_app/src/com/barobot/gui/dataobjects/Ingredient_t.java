package com.barobot.gui.dataobjects;

import java.util.List;

import org.orman.mapper.Model;
import org.orman.mapper.annotation.Entity;
import org.orman.mapper.annotation.PrimaryKey;

import com.barobot.gui.database.BarobotData;

@Entity
public class Ingredient_t extends Model<Ingredient_t>{
	@PrimaryKey(autoIncrement=true)
	public long id;
	public int recipe;		// @ManyToOne to Recipe_t
	public int liquid;			// @ ManyToOne to Liquid_t
	public int quantity;
	public int ind;
	
	public String getName() {
		return String.valueOf(quantity)+ "ml " + getLiquid().getName();
	}
	@Override
	public String toString() {
		return getName();
	}
	public static int getSize(List<Ingredient_t> ingredients) {
		int size	= 0;
		for(Ingredient_t ing : ingredients){
			Slot slot = Engine.GetInstance().getIngredientSlot(ing);
			if (slot == null){
				size		+= ing.quantity;
			}else{
				int count = slot.getSequence( ing.quantity );
				size		+= count * slot.dispenser_type;
			}
		}
		return size;
	}
	public Recipe_t getRecipe() {
		return BarobotData.getOneObject(Recipe_t.class, this.recipe );
	}
	public Liquid_t getLiquid() {
		return BarobotData.getOneObject(Liquid_t.class, this.liquid );
	}
	@Override
	public void update() {
		BarobotData.reportChange( this.getClass(), this.id );
		super.update();
	}
}
