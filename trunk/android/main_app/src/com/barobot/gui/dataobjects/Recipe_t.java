package com.barobot.gui.dataobjects;

import java.util.List;

import org.orman.mapper.EntityList;
import org.orman.mapper.Model;
import org.orman.mapper.annotation.Entity;
import org.orman.mapper.annotation.OneToMany;
import org.orman.mapper.annotation.PrimaryKey;

import com.barobot.gui.database.BarobotData;

@Entity
public class Recipe_t extends Model<Recipe_t>{
	@PrimaryKey (autoIncrement = true)
	public long id;
	public String name;
	public boolean favorite;
	public boolean unlisted;
	
	@Override
	public String toString() {
		return name;
	}
	
	@OneToMany (toType = Ingredient_t.class, onField = "recipe" )
	public EntityList<Recipe_t, Ingredient_t> ingredients = new EntityList<Recipe_t, Ingredient_t>(Recipe_t.class, Ingredient_t.class, this);
	
	public List<Ingredient_t> getIngredients()
	{
		ingredients.refreshList();
		return ingredients;
	}

	// array of 1-12
	public List<Ingredient_t> getSlots()
	{
		ingredients.refreshList();
		List<Slot> slots = BarobotData.GetSlots();
		for(Ingredient_t ing : ingredients)
		{
			int s = Engine.getIngredientPosition(slots,ing);
		}
		return ingredients;
	}
	
	public void addIngredient(Ingredient_t ing)
	{
		Ingredient_t existing = findIngredient(ing.liquid);
		if (existing == null)
		{
			ing.insert();
			this.ingredients.add(ing);
			this.update();
		}
		else
		{
			existing.quantity += ing.quantity;
			existing.update();
		}
	}
	
	Ingredient_t findIngredient(Liquid_t liquid)
	{
		for(Ingredient_t ing : ingredients)
		{
			if (ing.liquid.id == liquid.id)
			{
				return ing;
			}
		}
		return null;
	}
	
	
}
