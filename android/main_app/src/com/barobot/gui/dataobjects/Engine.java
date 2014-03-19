package com.barobot.gui.dataobjects;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.barobot.common.constant.Constant;
import com.barobot.gui.ArduinoListener;
import com.barobot.gui.database.BarobotDB;
import com.barobot.gui.database.BarobotData;
import com.barobot.gui.database.BarobotDataStub;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.virtualComponents;
import com.barobot.parser.Queue;
import com.barobot.parser.message.AsyncMessage;

public class Engine {
	private final int OUTSIDE_POSITION = 13;
	private static Engine instance;
	public static Engine GetInstance(Context context){
		if (instance == null){
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
	
	private BarobotDB db;
	
	private Engine(Context context)
	{
		BarobotData.StartOrmanMapping(context);
		db = new BarobotDB(context);
		
		//setupDatabase();
		
		BarobotDataStub.SetupDatabase();
	}
	
	
	
	public List<Slot> getSlots()
	{
		return BarobotData.GetSlots();
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
	}
	
	public void emptySlot(int position)
	{
		Slot slot = BarobotData.GetSlot(position);
		slot.product = null;
		slot.status = "Empty";
		slot.currentVolume = 0;
		slot.update();
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
	
	public List<Recipe_t> getRecipes()
	{
		List<Recipe_t> recipes = BarobotData.GetRecipes();
		List<Recipe_t> result = new ArrayList<Recipe_t>();
		
		for(Recipe_t recipe : recipes)
		{
			if (CheckRecipe(recipe) && recipe.unlisted == false)
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
	
	public Boolean Pour(Recipe_t recipe, final ArduinoListener mListener)
	{
		
		
		List<Integer> bottleSequence = GenerateSequence(recipe.getIngredients());
		if (bottleSequence == null)
		{
			return false; // We could not find some of the ingredients
		}
		
		Sequence = "";
		for (Integer i : bottleSequence){
			Sequence += i + " ";
		}
		virtualComponents.startDoingDrink();
		Log.i("Prepare Sequence:", Sequence );
		for (Integer i : bottleSequence){
			Log.i("Prepare", ""+i );
			virtualComponents.moveToBottle( i-1, false );
			virtualComponents.nalej(i);
		}
		virtualComponents.moveToStart();

		
		/*Queue q	= Arduino.getInstance().getMainQ();
		q.add( new AsyncMessage( true ) {
			@Override
			public Queue run() {
				this.name		= "onQueueFinished";
				mListener.onQueueFinished();
				return null;
			}
		} );
		ar.send(q);*/
		return true;
	}
	
	private List<Integer> GenerateSequence(List<Ingredient_t> ingredients)
	{
		final int VOLUME_DIVIDER = 20;
		
		List<Integer> bottleSequence = new ArrayList<Integer>();
		for(Ingredient_t ing : ingredients)
		{
			int position = getIngredientPosition(ing);
			if (position != -1)
			{
				int num = (int) ing.quantity/VOLUME_DIVIDER;
				for (int iter = 1; iter <= num ; iter++){
					bottleSequence.add(position);
				}
			}
			else
			{
				return null;
			}
		}
		
		return bottleSequence;
	}
	
	public int getIngredientPosition(Ingredient_t ing)
	{
		List<Slot> slots = getSlots();
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
	
	//---------------------------
	// Obsolete old code
	//
	
	
	public void UpdateBottleSlot(int position, Bottle bottle){
		
		
		
		if (position <= 0 || position >12)
			throw new IllegalArgumentException("Position of the bottle is outside the set boundaries");

		//if(){
		//	db.InsertSlot(position, bottle);
		//}else{
			db.UpdateSlot(position, bottle);
	//	}

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
		/*for(Ingredient i : ingredients)
		{
			Boolean ingridientFound = false;
			for (int idx = 1; idx <= 12; idx++)
			{
		//		if (bottleSet[idx] == null)
				{
					continue;
				}
			//	if (bottleSet[idx].getType().equalsIgnoreCase(i.getLiquid().type))
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
		*/
		return true;
	}
	
	public long AddLiquid(Liquid liquid) {
		return db.InsertLiquid(liquid);
	}
	
	public String Sequence;
	public Boolean Prepare(Recipe recipe, final ArduinoListener mListener)
	{
		/*List<Integer> bottleSequence = new ArrayList<Integer>();
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
		for (Integer i : bottleSequence){
			Sequence += i + " ";
		}
		Constant.log("Prepare Sequence:", Sequence );
		for (Integer i : bottleSequence){
			Constant.log("Prepare", ""+i );
			virtualComponents.moveToBottle( i-1, false );
			virtualComponents.nalej(i);
		}
		virtualComponents.moveToStart();

		Arduino ar		= Arduino.getInstance();
		ArduinoQueue q	= new ArduinoQueue();
		q.add( new rpc_message( true ) {
			@Override
			public ArduinoQueue run() {
				this.name		= "onQueueFinished";
				mListener.onQueueFinished();
				return null;
			}
		} );
		ar.send(q);*/
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
