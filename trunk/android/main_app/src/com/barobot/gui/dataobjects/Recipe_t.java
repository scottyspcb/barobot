package com.barobot.gui.dataobjects;

import java.util.List;

import org.orman.mapper.EntityList;
import org.orman.mapper.Model;
import org.orman.mapper.annotation.Entity;
import org.orman.mapper.annotation.OneToMany;
import org.orman.mapper.annotation.PrimaryKey;

import com.barobot.other.LangTool;

@Entity
public class Recipe_t extends Model<Recipe_t>{
	@PrimaryKey (autoIncrement = true)
	public int id;
	public String name;
	public boolean favorite;
	public boolean unlisted;
	public int photoId;
	public int counter;

	public String getName() {
		return LangTool.translateName(id, "recipe", name );
	}
	@Override
	public String toString() {
		return getName();
	}

	@OneToMany (toType = Ingredient_t.class, onField = "recipe" )
	public EntityList<Recipe_t, Ingredient_t> ingredients = new EntityList<Recipe_t, Ingredient_t>(Recipe_t.class, Ingredient_t.class, this);

	public List<Ingredient_t> getIngredients()
	{
		if(ingredients.size() == 0 ){
			ingredients.refreshList();
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
			this.ingredients.refreshList();
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

	public static int[] getTaste(List<Ingredient_t> ingredients) {
		int res[]	= {0,0,0,0};
		int value0	= 0;// Sweet
		int value1	= 0;// Sour
		int value2	= 0;// Bitter
		int value3	= 0;// Strength
		int weights	= 0;

		for(Ingredient_t ing : ingredients)
		{
			value0+= (ing.liquid.sweet * ing.quantity);
			value1+= (ing.liquid.sour * ing.quantity);
			value2+= (ing.liquid.bitter * ing.quantity);
			value3+= (ing.liquid.strenght * ing.quantity);
			weights+= ing.quantity;
		}
		if (weights > 0){
			res[0]= value0 / weights;
			res[1]= value1 / weights;
			res[2]= value2 / weights;
			res[3]= value3 / weights;
		}
		return res;	
	}
	
	public int[] getTaste() 
	{
		List<Ingredient_t> ingredients = getIngredients();
		return getTaste(ingredients);
	}
}
