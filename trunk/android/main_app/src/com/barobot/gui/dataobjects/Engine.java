package com.barobot.gui.dataobjects;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.barobot.gui.ArduinoListener;
import com.barobot.gui.database.BarobotData;
import com.barobot.gui.database.BarobotDataStub;
import com.barobot.hardware.virtualComponents;
import com.barobot.parser.Queue;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.message.Mainboard;

public class Engine {
	private static Engine instance;
	public static Engine GetInstance(Context context){
		if (instance == null){
			instance = new Engine(context);
		}
		return instance;
	}
	
	private List<Slot> slots;
	
	private Engine(Context context)
	{
		BarobotData.StartOrmanMapping(context);
	//	BarobotDataStub.SetupDatabase();
	}
	
	public List<Slot> getSlots()
	{
		if (slots == null)
		{
			slots = BarobotData.GetSlots(); 
		}
		return slots;
	}
	
	public Slot getSlot(int position)
	{
		return BarobotData.GetSlot(position);
	}
	
	public Product getProduct(int position)
	{
		return BarobotData.GetSlot(position).product;
	}
	
	public void updateSlot(int position, Product prod)
	{
		if (prod != null)
		{
			Slot slot = BarobotData.GetSlot(position);
			slot.product = prod;
			slot.status = "OK";
			slot.currentVolume = prod.capacity;
		
			slot.update();
		}
		invalidateData();
	}
	
	public void emptySlot(int position)
	{
		Slot slot = getSlot(position);
		slot.product = null;
		slot.status = "Empty";
		slot.currentVolume = 0;
		slot.update();
		invalidateData();
	}
	
	private void invalidateData()
	{
		slots = null;
		recipes = null;
		favoriteRecipes = null;
	}
	
	public void CacheDatabase()
	{
		invalidateData();
		getSlots();
		getRecipes();
		getFavoriteRecipes();
	}
	
	public List<Product> getProducts()
	{
		return BarobotData.GetProduct();
	}
	
	public List<Type> getTypes() {
		return BarobotData.GetTypes();
	}
	
	public void addType(Type type)
	{
		type.insert();
	}
	
	public void addLiquid(Liquid_t liquid)
	{
		liquid.insert();
	}
	
	public void addProduct(Product product) 
	{
		product.insert();	
	}
	
	private List<Recipe_t> recipes;
	
	public List<Recipe_t> getRecipes()
	{
		if (recipes == null)
		{
			recipes = Filter(BarobotData.GetListedRecipes()); 
		}
		return recipes;
	}
	
	private List<Recipe_t> favoriteRecipes;
	
	public List<Recipe_t> getFavoriteRecipes()
	{
		if (favoriteRecipes == null)
		{
			favoriteRecipes =Filter(BarobotData.GetFavoriteRecipes()); 
		}
		return favoriteRecipes;
	}
	
	private List<Recipe_t> Filter(List<Recipe_t> recipes)
	{
		List<Recipe_t> result = new ArrayList<Recipe_t>();
		
		for(Recipe_t recipe : recipes)
		{
			if (CheckRecipe(recipe))
			{
				result.add(recipe);
			}
		}
		return result;
	}
	
	public List<Recipe_t> getAllRecipes()
	{
		return BarobotData.GetRecipes();
	}
	
	public void addRecipe(Recipe_t recipe, List<Ingredient_t> ingredients)
	{
		recipe.insert();
		for(Ingredient_t ing : ingredients)
		{
			ing.insert();
			recipe.ingredients.add(ing);
		}
	}
	
	public void removeIngredient(Ingredient_t ingredient)
	{
		ingredient.delete();
	}
	
	public Boolean CheckRecipe(Recipe_t recipe)
	{
		List<Integer> bottleSequence = GenerateSequence(recipe.getIngredients());
		if (bottleSequence == null)
		{
			return false; // We could not find some of the ingredients
		}
		return true;
	}
	
	public Boolean Pour(Recipe_t recipe,  final ArduinoListener mListener)
	{
		List<Integer> bottleSequence = GenerateSequence(recipe.getIngredients());
		if (bottleSequence == null){
			return false; // We could not find some of the ingredients
		}
		String Sequence = "";
		for (Integer i : bottleSequence){
			Sequence += i + " ";
		}
	//	virtualComponents.startDoingDrink();
		Log.i("Prepare Sequence:", Sequence );
		for (Integer i : bottleSequence){
			Log.i("Prepare", ""+i );
			virtualComponents.moveToBottle( i-1, false );
			virtualComponents.nalej(i-1);
		}
		virtualComponents.moveToStart();

		Queue q	= virtualComponents.getMainQ();
		q.add( new AsyncMessage( true ) {
			@Override	
			public String getName() {
				return "on drink ready";
			}
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				this.name		= "onQueueFinished";
				mListener.onQueueFinished();
				return null;
			}
		} );

		return true;
	}
	
	public List<Integer> GenerateSequence(List<Ingredient_t> ingredients)
	{
		int VOLUME_DIVIDER = 20;
		List<Integer> bottleSequence = new ArrayList<Integer>();
		List<Slot> slots = getSlots();
		for(Ingredient_t ing : ingredients)
		{
			int position = getIngredientPosition(slots, ing);
			if (position != -1){
				int num = (int) ing.quantity/VOLUME_DIVIDER;
				for (int iter = 1; iter <= num ; iter++){
					bottleSequence.add(position);
				}
			}else{
				return null;
			}
		}
		return bottleSequence;
	}
	
	public static int getIngredientPosition(List<Slot> slots, Ingredient_t ing){
		for(Slot sl : slots)
		{
			if (sl.product != null)
			{
				if (sl.product.liquid.id == ing.liquid.id)
				{
					return sl.position;
				}
			}
		}
		
		return -1; // Indicating that ingredient was not found
	}
}
