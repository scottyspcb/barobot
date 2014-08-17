package com.barobot.gui.dataobjects;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.orman.mapper.Model;

import android.content.Context;
import android.os.Environment;

import com.barobot.common.Initiator;
import com.barobot.gui.database.BarobotData;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.other.InternetHelpers;
import com.barobot.other.update_drinks;
import com.barobot.parser.Queue;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.message.Mainboard;

public class Engine {
	private List<Slot> slots;
	private List<Recipe_t> recipes;
	private List<Recipe_t> favoriteRecipes;
	private static Map<Integer, Slot> liquid2slot = null;
	private static Engine instance;
	private static int robotId;

	public static Engine createInstance(Context context){
		if (instance == null){
			instance = new Engine(context);
		}
		return instance;
	}

	public static Engine GetInstance(){
		return instance;
	}

	private Engine(Context context)
	{
		// check there is a valid database
		File ss = context.getDatabasePath( BarobotData.DATABASE_NAME );
		if( !ss.exists() ){
			String resetPath	= 	Environment.getExternalStorageDirectory()+ update_drinks.copyPath;	
			Initiator.logger.e("DB Engine", "no databse file: "+ resetPath );
			try {
				InternetHelpers.copy( resetPath, update_drinks.localDbPath );// copy it from assets
			} catch (IOException e) {
				Initiator.logger.e("DB Engine", "copy error", e );
			}
		}
		BarobotData.StartOrmanMapping(context);
	}

	public List<Slot> loadSlots()
	{
		if (slots == null)
		{
			robotId = BarobotConnector.getRobotId();
			slots = Model.fetchAll(Slot.class);
			liquid2slot = new HashMap<Integer, Slot>();
			for(Slot sl : slots)
			{
				if(sl.product!= null && sl.product.liquid != null ){
					liquid2slot.put(sl.product.liquid.id, sl);
				}
			}
			Initiator.logger.i("robotId : ", ""+robotId );
			Initiator.logger.i("loadSlots size: ", ""+slots.size() );
			Initiator.logger.i("liquid2slot size: ", ""+liquid2slot.size() );
		}
		return slots;
	}

	public void invalidateData()
	{
		slots = null;
		liquid2slot = null;
		recipes = null;
		favoriteRecipes = null;
	}

	public void switchRobotId( int newId ){
		robotId = newId;
		invalidateData();
	}
/*
	public void CacheDatabase()
	{
		invalidateData();
		loadSlots();
		getRecipes();
		getFavoriteRecipes();
	}

	public List<Product> getProducts()
	{
		return Model.fetchAll(Product.class);
	}
	*/
	public List<Type> getTypes() {
		return BarobotData.GetTypes();
	}

	public List<Recipe_t> getRecipes()
	{
		if (recipes == null)
		{
			recipes = Filter(BarobotData.GetListedRecipes()); 
		}
		return recipes;
	}

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
		for(Ingredient_t ing : ingredients)
		{
			ing.insert();
			recipe.ingredients.add(ing);
		}
	}
	public void removeIngredient(Ingredient_t ingredient){
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

	public Boolean Pour(final Recipe_t recipe)
	{
		//	List<Integer> bottleSequence = GenerateSequence(ings);
		List<Ingredient_t> ings = recipe.getIngredients();
		BarobotConnector barobot = Arduino.getInstance().barobot;

		Queue q = new Queue();
		barobot.startDoingDrink(q);

		for(Ingredient_t ing : ings){
			Slot slot = getIngredientSlot(ing);
			if (slot != null){
				int position = slot.position;
				int count = slot.getSequence( ing.quantity );
		//		Log.i("Prepare", ""+position+"/"+count );
				barobot.moveToBottle(q, position-1, false );
				for (int iter = 1; iter <= count ; iter++){
					if( iter > 1){
		//				Log.i("Prepare", "addWait" );
						int repeat_time = barobot.getRepeatTime( position-1, slot.dispenser_type );
						q.addWait( repeat_time  );			// wait for refill
					}
		//			Log.i("Prepare", "pour" );
					barobot.pour(q, slot.dispenser_type, position-1, false);
					saveStats( slot, q);
					saveStats( ing.liquid, q);
				}
			}else{// We could not find some of the ingredients
			}
		}
		q.add( new AsyncMessage( true ) {
			@Override	
			public String getName() {
				return "finish drink" ;
			}
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				this.name		= "moveToStart";
				Queue q2		= new Queue();
				BarobotConnector barobot = Arduino.getInstance().barobot;
				barobot.moveToStart( q2 );		// na koniec
				barobot.onDrinkFinish( q2 );
				return q2;
			}
		} );
	
		q.add( new AsyncMessage( true ) {
			@Override	
			public String getName() {
				return "save recipe stats" ;
			}
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				recipe.counter++;
				recipe.update();
				return null;
			}
		} );
		barobot.main_queue.add(q);
		return true;
	}
	
	private static void saveStats(final Liquid_t liquid, Queue q) {
		q.add( new AsyncMessage( true ) {
			@Override	
			public String getName() {
				return "save liquid stats" ;
			}
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				liquid.counter++;
				liquid.update();
				return null;
			}
		} );
	}

	private static void saveStats(final Slot slot, Queue q) {
		q.add( new AsyncMessage( true ) {
			@Override	
			public String getName() {
				return "save slot stats" ;
			}
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				slot.counter++;
				slot.update();
				return null;
			}
		} );
	}

	public List<Integer> GenerateSequence(List<Ingredient_t> ingredients)
	{
		List<Integer> bottleSequence = new ArrayList<Integer>();
		for(Ingredient_t ing : ingredients)
		{
			Slot slot = getIngredientSlot(ing);
			if (slot == null){
				return null;
			}else{
				int count = slot.getSequence( ing.quantity );
				for (int iter = 1; iter <= count ; iter++){
					bottleSequence.add(slot.position);
				}
			}
		}
		return bottleSequence;
	}
	
	public Slot getIngredientSlot(Ingredient_t ing){
		loadSlots();
		return liquid2slot.get(ing.liquid.id);	// id to be more universal
	}
}
