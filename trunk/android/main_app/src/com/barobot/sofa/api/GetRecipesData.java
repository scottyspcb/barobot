package com.barobot.sofa.api;

import java.io.IOException;
import java.util.List;

import com.barobot.gui.dataobjects.Ingredient_t;
import com.barobot.gui.dataobjects.Recipe_t;

import android.util.JsonWriter;

public class GetRecipesData implements IData {

	private List<Recipe_t> mList;
	
	public GetRecipesData(List<Recipe_t> _list) {
		mList = _list;
	}
	@Override
	public void writeJson(JsonWriter writer) throws IOException {
		WriteList(writer, mList);
	}
	
	private void WriteList(JsonWriter writer, List<Recipe_t> recipes) throws IOException
	{
		writer.name("result");
		writer.beginArray();
		for(Recipe_t recipe : recipes)
		{
			writer.beginObject();
			writer.name("Id").value(recipe.id);
			writer.name("Name").value(recipe.name);
			WriteIngredients(writer, recipe.ingredients);
			writer.endObject();
		}
		writer.endArray();
	}
	
	private void WriteIngredients(JsonWriter writer, List<Ingredient_t> ingredients) throws IOException
	{
		writer.name("Ingredients");
		writer.beginArray();
		for (Ingredient_t ing : ingredients)
		{
			writer.beginObject();
			writer.name("Name").value(ing.liquid.getName());
			writer.name("Quantity").value(ing.quantity);
			writer.endObject();
		}
		writer.endArray();
	}

}
