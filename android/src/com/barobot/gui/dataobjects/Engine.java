package com.barobot.gui.dataobjects;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;

import com.barobot.gui.ArduinoListener;
import com.barobot.gui.database.BarobotDB;
import com.barobot.gui.database.DataContract;
import com.barobot.hardware.virtualComponents;

public class Engine {
	
	private final int OUTSIDE_POSITION = 13;
	
	private static Engine instance;
	
	public static Engine GetInstance(Context context)
	{
		if (instance == null)
		{
			instance = new Engine(context);
		}
		
		return instance;
	}
	
	public static class Liquids
	{
		public static Liquid whiteRum = new Liquid("white rum", "Bacardi", (float) 37.5);
		public static Liquid cocaCola = new Liquid("coke", "Coca-Cola", 0);
		public static Liquid sparklingWater = new Liquid("sparkling water", "", 0);
		public static Liquid coffeeLiquoir = new Liquid("coffee liquoir", "Kahlua", 36);
		
		public static void SetupLiquids (BarobotDB db)
		{
			Liquids.whiteRum.id =  db.InsertLiquid(Liquids.whiteRum);
			Liquids.cocaCola.id = db.InsertLiquid(Liquids.cocaCola);
			Liquids.sparklingWater.id  = db.InsertLiquid(Liquids.sparklingWater);
			Liquids.coffeeLiquoir.id  = db.InsertLiquid(Liquids.coffeeLiquoir);
		}
	}
	//bottle configuration
	private Bottle[] bottleSet;
	;
	private BarobotDB db;
	
	private Engine(Context context)
	{
		db = new BarobotDB(context);
		
		//setupDatabase();
		
		refreshBottleSet();
	}
	
	private void refreshBottleSet(){
		bottleSet = db.GetSlots();
	}
	
	public Bottle[] getBottleSlots() {
		refreshBottleSet();
		return bottleSet;
	}
	
	public void UpdateBottleSlot(int position, Bottle bottle){
		if (position <= 0 || position >12)
			throw new IllegalArgumentException("Position of the bottle is outside the set boundaries");

		//if(){
		//	db.InsertSlot(position, bottle);
		//}else{
			db.UpdateSlot(position, bottle);
	//	}
		refreshBottleSet();
	}
	
	public List<Recipe> getRecipes()
	{
		return db.getRecipes();
	}
	
	public List<Recipe> getPossibleRecipes()
	{
		List<Recipe> recipes = db.getRecipes();
		List<Recipe> result = new ArrayList<Recipe>();
		for(Recipe recipe : recipes)
		{
			List<Ingredient> ing = recipe.getIngridients();
			if (!ing.isEmpty() && checkIngredients(ing)){
				result.add(recipe);
			}
		}
		
		return result;
	}
	
	private boolean checkIngredients(List<Ingredient> ingredients)
	{
		for(Ingredient i : ingredients)
		{
			Boolean ingridientFound = false;
			for (int idx = 1; idx <= 12; idx++)
			{
				if (bottleSet[idx] == null)
				{
					continue;
				}
				if (bottleSet[idx].getType().equalsIgnoreCase(i.getLiquid().type))
				{
					ingridientFound = true;
					break;
				}
			}
			if (!ingridientFound)
			{
				// Checking outside list
				boolean foundOutside = false;
				List<Liquid> outsideCompontents = getOutsideComponent();
				for (Liquid liquid: outsideCompontents)
				{
					if (liquid.type.equalsIgnoreCase(i.getLiquid().type))
					{
						foundOutside = true;
						break;
					}
				}
				if (!foundOutside)
				{
					return false;
				}
			}
		}
		
		return true;
	}
	
	public long AddLiquid(Liquid liquid) {
		return db.InsertLiquid(liquid);
	}
	
	public String Sequence;
	public Boolean Prepare(Recipe recipe, ArduinoListener mListener)
	{
		List<Integer> bottleSequence = new ArrayList<Integer>();
		List<Ingredient> ingridients = recipe.getIngridients();
		for(Ingredient i : ingridients)
		{
			Boolean ingridientFound = false;
			for (int idx = 1; idx <= 12; idx++)
			{
				if (bottleSet[idx] == null)
				{
					continue;
				}
				if (bottleSet[idx].getType().equalsIgnoreCase(i.getLiquid().type))
				{
					int num = (int) i.getQuantity()/20;
					for (int iter = 1; iter <= num ; iter++){
						bottleSequence.add(idx);
					}
					ingridientFound = true;
					break;
				}
			}
			if (!ingridientFound)
			{
				// Checking outside list
				boolean foundOutside = false;
				List<Liquid> outsideCompontents = getOutsideComponent();
				for (Liquid liquid: outsideCompontents)
				{
					if (liquid.type.equalsIgnoreCase(i.getLiquid().type))
					{
						foundOutside = true;
						break;
					}
				}
				if (!foundOutside)
				{
					return false;
				}
			}
		}
		
		
		Sequence = "";
		for (Integer i : bottleSequence)
		{
			// this should contain calls to Arduino
			//virtualComponents.moveToBottle(i);
			//virtualComponents.nalej(0);
			
			Sequence += i + " ";
		}
		
		mListener.onQueueFinished();
		return true;
	}
	
	public List<Liquid> getAllLiquids()
	{
		return db.getLiquids();
	}
	
	public long AddRecipe(Recipe recipe) {
		return db.InsertRecipe(recipe);
	}
	
	public void RemoveRecipe(long recipeId) {
		db.DeleteRecipe(recipeId);
	}
	
	public void RemoveIngredients(long recipeId) {
		db.DeleteIngredients(recipeId);
	}
	
	public void addIngredient(long recipeId, Ingredient ingredient)
	{
		db.InsertIngredient(recipeId, ingredient);
	}
	
	public void addOutsideComponent(Liquid liquid)
	{
		db.InsertSlot(OUTSIDE_POSITION, new Bottle(liquid, 0));
	}
	
	public void removeOutsideComponents()
	{
		db.removeOusideComponents(OUTSIDE_POSITION);
	}
	
	public List<Liquid> getOutsideComponent() {
		return db.getOutsideComponents(OUTSIDE_POSITION);
	}
	
	private void setupDatabase()
	{
		
		db.DeleteSlots();
		db.DeleteIngredients();
		db.DeleteRecipes();
		db.DeleteLiquids();
		
		for (int i= 1 ; i <= 12; i++)
		{
			db.InsertSlot(i, null);
		}
		
		Liquids.SetupLiquids(db);
		
		db.UpdateSlot(1, new Bottle(Liquids.whiteRum, 0));
		db.UpdateSlot(2, new Bottle(Liquids.cocaCola, 0));
		db.UpdateSlot(3, new Bottle(Liquids.sparklingWater, 0));
		db.UpdateSlot(4, new Bottle(Liquids.coffeeLiquoir, 0));
		
		List<Recipe> recipes = PrepareTestRecipies();
		
		for(Recipe recipe : recipes)
		{
			db.InsertRecipe(recipe);
		}
		
		addOutsideComponent(Liquids.cocaCola);
	}
	
	public static List<Recipe> PrepareTestRecipies()
	{
		List<Recipe> result = new ArrayList<Recipe>();
		
		// Cuba Libre 
		
		List<Ingredient> cubaLibre = new ArrayList<Ingredient>();
		cubaLibre.add(new Ingredient(Engine.Liquids.whiteRum, 60));
		cubaLibre.add(new Ingredient(Engine.Liquids.cocaCola, 160));
		
		result.add(new Recipe(1, "Cuba Libre", "Zajebisty drink", cubaLibre));
		
		// Mojito
		List<Ingredient> mojito = new ArrayList<Ingredient>();
		mojito.add(new Ingredient(Engine.Liquids.whiteRum, 40));
		mojito.add(new Ingredient(Engine.Liquids.sparklingWater, 200));
		
		result.add(new Recipe(2, "Mojito", "Zajebisty drink 2", mojito));
		
		// White russian
		List<Ingredient> whiteRussian = new ArrayList<Ingredient>();
		whiteRussian.add(new Ingredient(Engine.Liquids.whiteRum, 60));
		whiteRussian.add(new Ingredient(Engine.Liquids.coffeeLiquoir, 20));
		
		result.add(new Recipe(3, "White Russian", "Koledzy zza buga", whiteRussian));
		
		return result;
	}
}
