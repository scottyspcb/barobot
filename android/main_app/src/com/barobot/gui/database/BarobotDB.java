package com.barobot.gui.database;

import java.util.ArrayList;
import java.util.List;

import com.barobot.gui.dataobjects.Bottle;
import com.barobot.gui.dataobjects.Ingredient;
import com.barobot.gui.dataobjects.Liquid;
import com.barobot.gui.dataobjects.Recipe;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class BarobotDB {
	
	private SQLiteDatabase db;
	public BarobotDB(Context context)
	{
		DbHelper dbHelper = new DbHelper(context);
		db = dbHelper.getWritableDatabase();
	}
	
	//-----------------------
	// Liquids table
	//
	
	public long InsertLiquid (Liquid liquid)
	{
		ContentValues cv = new ContentValues();
		cv.put(DataContract.Liquids.COLUMN_NAME_NAME, liquid.name);
		cv.put(DataContract.Liquids.COLUMN_NAME_TYPE, liquid.type);
		cv.put(DataContract.Liquids.COLUMN_NAME_VOLTAGE, liquid.voltage);
		
		return db.insert(DataContract.Liquids.TABLE_NAME, null, cv);
	}
	
	public List<Liquid> getLiquids()
	{
		List<Liquid> result = new ArrayList<Liquid>();
		String[] projections = {
				DataContract.Liquids._ID,
				DataContract.Liquids.COLUMN_NAME_NAME,
				DataContract.Liquids.COLUMN_NAME_TYPE,
				DataContract.Liquids.COLUMN_NAME_VOLTAGE
		};
		
		Cursor c = db.query(DataContract.Liquids.TABLE_NAME,projections, null, null, null, null, null);
		
		if (!c.moveToFirst()) {
			// returning empty dataset
			c.close();
			return result;
		}
		
		do
		{
			result.add(new Liquid(
					c.getInt(c.getColumnIndexOrThrow(DataContract.Liquids._ID)),
					c.getString(c.getColumnIndexOrThrow(DataContract.Liquids.COLUMN_NAME_TYPE)),
					c.getString(c.getColumnIndexOrThrow(DataContract.Liquids.COLUMN_NAME_NAME)),
					c.getFloat(c.getColumnIndexOrThrow(DataContract.Liquids.COLUMN_NAME_VOLTAGE))
					));
			
		} while (c.moveToNext());
		
		c.close();
		return result;
	}
	
	public void DeleteLiquids()
	{
		db.delete(DataContract.Liquids.TABLE_NAME, null, null);
	}
	
	//-----------------------
	// Slots table
	//
	
	public void InsertSlot(int position, Bottle bottle)
	{
		Log.w("InsertSlotPos",""+position);
		ContentValues cv = new ContentValues();
		cv.put(DataContract.Slots.COLUMN_NAME_POSITION, position);
		long liquidId = 0;
		if (bottle == null){
			Log.w("InsertSlot", "no bottle");
			return;
		}else{
			liquidId = bottle.getLiquid().id;
		}
		cv.put(DataContract.Slots.COLUMN_NAME_LIQUIDID, liquidId);
		cv.put(DataContract.Slots.COLUMN_NAME_CAPACITY, 0);
		db.insert(DataContract.Slots.TABLE_NAME, null, cv);
	}
	
	public void UpdateSlot(int position, Bottle bottle)
	{
		ContentValues cv = new ContentValues();
		long liquidId = 0;
		if (bottle != null)
		{
			liquidId = bottle.getLiquid().id;
		}
		cv.put(DataContract.Slots.COLUMN_NAME_LIQUIDID, liquidId);
		cv.put(DataContract.Slots.COLUMN_NAME_CAPACITY, 0);
		
		String where = DataContract.Slots.COLUMN_NAME_POSITION + "=?";
		
		String[] whereArgs = {
				Integer.toString(position)
		};
		
		db.update(DataContract.Slots.TABLE_NAME, cv, where, whereArgs);
	}
	
	public Bottle[] GetSlots()
	{
		final int NUMBER_OF_BOTTLES = 12;
		
		String query = "SELECT * FROM " + DataContract.Slots.TABLE_NAME
				+ " INNER JOIN " + DataContract.Liquids.TABLE_NAME
				+ " ON " + DataContract.Slots.TABLE_NAME+"."+DataContract.Slots.COLUMN_NAME_LIQUIDID
				+ "=" + DataContract.Liquids.TABLE_NAME+"."+DataContract.Liquids._ID 
				+ " WHERE " + DataContract.Slots.COLUMN_NAME_POSITION + "<=?";
		
		Bottle[] result = new Bottle[13];
		String[] args = new String[]{String.valueOf(NUMBER_OF_BOTTLES)};
		
		Cursor c = db.rawQuery(query, args);

		if (!c.moveToFirst()) {
			// returning empty dataset
			Log.w("BOTTLE_SETUP","empty");
			c.close();
			return result;
		}
		do
		{
			int position = c.getInt(c.getColumnIndexOrThrow(DataContract.Slots.COLUMN_NAME_POSITION));
			
			
			result[position] = new Bottle(new Liquid(
					c.getInt(c.getColumnIndexOrThrow(DataContract.Liquids._ID)),
					c.getString(c.getColumnIndexOrThrow(DataContract.Liquids.COLUMN_NAME_TYPE)),
					c.getString(c.getColumnIndexOrThrow(DataContract.Liquids.COLUMN_NAME_NAME)),
					c.getFloat(c.getColumnIndexOrThrow(DataContract.Liquids.COLUMN_NAME_VOLTAGE))
					), 0);
			
		} while (c.moveToNext());
		
		c.close();
		return result;
	}
	
	public List<Liquid> getOutsideComponents(int outsideId)
	{
		List<Liquid> result = new ArrayList<Liquid>();
		
		String query = "SELECT * FROM " + DataContract.Slots.TABLE_NAME
				+ " INNER JOIN " + DataContract.Liquids.TABLE_NAME
				+ " ON " + DataContract.Slots.TABLE_NAME+"."+DataContract.Slots.COLUMN_NAME_LIQUIDID
				+ "=" + DataContract.Liquids.TABLE_NAME+"."+DataContract.Liquids._ID 
				+ " WHERE " + DataContract.Slots.COLUMN_NAME_POSITION + "=?";
			
		String[] args = new String[]{String.valueOf(outsideId)};
		
		Cursor c = db.rawQuery(query, args);
		
		if (!c.moveToFirst()) {
			// returning empty dataset
			c.close();
			return result;
		}
		
		do
		{
			result.add(new Liquid(
					c.getInt(c.getColumnIndexOrThrow(DataContract.Liquids._ID)),
					c.getString(c.getColumnIndexOrThrow(DataContract.Liquids.COLUMN_NAME_TYPE)),
					c.getString(c.getColumnIndexOrThrow(DataContract.Liquids.COLUMN_NAME_NAME)),
					c.getFloat(c.getColumnIndexOrThrow(DataContract.Liquids.COLUMN_NAME_VOLTAGE))
					));
			
		} while (c.moveToNext());
		
		c.close();
		return result;
	}
	
	public void removeOusideComponents(int outsideId) {
		String where = DataContract.Slots.COLUMN_NAME_POSITION + "=?";
		String[] args = new String[]{String.valueOf(outsideId)};
		
		db.delete(DataContract.Slots.TABLE_NAME, where, args);
	}
	
	public void DeleteSlots()
	{
		db.delete(DataContract.Slots.TABLE_NAME, null, null);
	}
	
	//-----------------------
	// Recipes table
	//
	
	public long InsertRecipe(Recipe recipe)
	{
		ContentValues recipeValues = new ContentValues();
		recipeValues.put(DataContract.Recipes.COLUMN_NAME_NAME, recipe.getName());
		recipeValues.put(DataContract.Recipes.COLUMN_NAME_DESCRIPTION, recipe.getDescription());
		
		long recipeID = db.insert(DataContract.Recipes.TABLE_NAME,  null, recipeValues);
		
		for (Ingredient i : recipe.getIngridients())
		{
			InsertIngredient(recipeID, i);
		}
		
		return recipeID;
	}
	
	public void DeleteRecipe(long recipeId) {
		String whereRecipe = DataContract.Recipes._ID + "=?"; 
		String []args = new String[]{String.valueOf(recipeId)};
		db.delete(DataContract.Recipes.TABLE_NAME, whereRecipe, args);
		
		DeleteIngredients(recipeId);
	}
	
	public void DeleteIngredients(long recipeId) {
		String whereIngredient = DataContract.Ingredients.COLUMN_NAME_RECIPEID + "=?";
		String []args = new String[]{String.valueOf(recipeId)};
		db.delete(DataContract.Ingredients.TABLE_NAME, whereIngredient, args);
	}
	
	public void DeleteRecipes()
	{
		db.delete(DataContract.Recipes.TABLE_NAME, null, null);
	}
	
	public List<Recipe> getRecipes()
	{
		List<Recipe> result = new ArrayList<Recipe>();
		String[] projections = {
				DataContract.Recipes._ID,
				DataContract.Recipes.COLUMN_NAME_NAME,
				DataContract.Recipes.COLUMN_NAME_DESCRIPTION
		};
		
		Cursor c = db.query(DataContract.Recipes.TABLE_NAME, projections, null, null, null, null, null);
		
		if (!c.moveToFirst()) {
			// returning empty dataset
			c.close();
			return result;
		}
		
		do
		{
			int recipeID = c.getInt(c.getColumnIndexOrThrow(DataContract.Recipes._ID));
			String recipeName = c.getString(c.getColumnIndexOrThrow(DataContract.Recipes.COLUMN_NAME_NAME));
			String recipeDesc = c.getString(c.getColumnIndexOrThrow(DataContract.Recipes.COLUMN_NAME_DESCRIPTION));
			
			result.add(new Recipe(recipeID, recipeName, recipeDesc, getIngerdients(recipeID)));
			
		} while (c.moveToNext());
		
		c.close();
		return result;
	}
	
	//-----------------------
	// Ingredients table
	//
	
	public void InsertIngredient(long recipeID, Ingredient i)
	{
		ContentValues iValues = new ContentValues();
		iValues.put(DataContract.Ingredients.COLUMN_NAME_LIQUIDID, i.getLiquid().id);
		iValues.put(DataContract.Ingredients.COLUMN_NAME_RECIPEID, recipeID);
		iValues.put(DataContract.Ingredients.COLUMN_NAME_QUANTITY, i.getQuantity());
		
		db.insert(DataContract.Ingredients.TABLE_NAME, null, iValues);
	}
	
	public List<Ingredient> getIngerdients(int recipeID)
	{
		List<Ingredient> result = new ArrayList<Ingredient>();
		
		String query = "SELECT * FROM " + DataContract.Ingredients.TABLE_NAME
				+ " LEFT JOIN " + DataContract.Liquids.TABLE_NAME
				+ " ON " + DataContract.Ingredients.TABLE_NAME+"."+DataContract.Ingredients.COLUMN_NAME_LIQUIDID
				+ "=" + DataContract.Liquids.TABLE_NAME+"."+DataContract.Liquids._ID
				+ " WHERE " + DataContract.Ingredients.COLUMN_NAME_RECIPEID + "= ?";
		
		String[] args = new String[] {Integer.toString(recipeID)};
		
		Cursor c = db.rawQuery(query, args);
		
		
		if (!c.moveToFirst()) {
			// returning empty dataset
			c.close();
			return result;
		}
		
		do
		{
			Liquid liquid = new Liquid(
					c.getInt(c.getColumnIndexOrThrow(DataContract.Liquids._ID)),
					c.getString(c.getColumnIndexOrThrow(DataContract.Liquids.COLUMN_NAME_TYPE)),
					c.getString(c.getColumnIndexOrThrow(DataContract.Liquids.COLUMN_NAME_NAME)),
					c.getFloat(c.getColumnIndexOrThrow(DataContract.Liquids.COLUMN_NAME_VOLTAGE))
					);
			int quantity = c.getInt(c.getColumnIndexOrThrow(DataContract.Ingredients.COLUMN_NAME_QUANTITY));
			result.add(new Ingredient(liquid, quantity));
			
		} while (c.moveToNext());
		
		c.close();
		return result;
	}
	
	public void DeleteIngredients()
	{
		db.delete(DataContract.Ingredients.TABLE_NAME, null, null);
	}
}
