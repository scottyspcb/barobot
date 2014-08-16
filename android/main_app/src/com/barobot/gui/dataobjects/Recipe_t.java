package com.barobot.gui.dataobjects;

import java.util.List;

import org.orman.mapper.EntityList;
import org.orman.mapper.Model;
import org.orman.mapper.annotation.Entity;
import org.orman.mapper.annotation.OneToMany;
import org.orman.mapper.annotation.PrimaryKey;

import com.barobot.gui.database.BarobotData;
import com.barobot.gui.utils.Distillery;
import com.barobot.gui.utils.LangTool;

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
	
	public int GetSweet()
	{
		return Distillery.getSweet(getIngredients());
	}
	
	public int GetSour()
	{
		return Distillery.getSour(getIngredients());
	}
	
	public int GetBitter()
	{
		return Distillery.getBitter(getIngredients());
	}
	
	public int GetStrength()
	{
		return Distillery.getStrength(getIngredients());
	}
}
