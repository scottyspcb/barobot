package com.barobot.gui.dataobjects;

import java.util.List;

import org.orman.mapper.EntityList;
import org.orman.mapper.Model;
import org.orman.mapper.annotation.Entity;
import org.orman.mapper.annotation.OneToMany;
import org.orman.mapper.annotation.PrimaryKey;

import com.barobot.common.Initiator;
import com.barobot.gui.database.BarobotData;
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
		if (existing == null){
			ing.recipe = this.id;
			ing.insert();
		}else{
			existing.quantity += ing.quantity;
			existing.update();
		}
	}

	private Ingredient_t findIngredient(int liquid_id){
		for(Ingredient_t ing : ingredients){
			if (ing.liquid == liquid_id){
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

		for(Ingredient_t ing : ingredients){
			Liquid_t liquid = ing.getLiquid();
			value0+= (liquid.sweet * ing.quantity);
			value1+= (liquid.sour * ing.quantity);
			value2+= (liquid.bitter * ing.quantity);
			value3+= (liquid.strenght * ing.quantity);
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
	
	public int[] getTaste() {
		List<Ingredient_t> ingredients = getIngredients();
		return getTaste(ingredients);
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

	public void refreshList() {
		this.ingredients.refreshList();
	}
}
