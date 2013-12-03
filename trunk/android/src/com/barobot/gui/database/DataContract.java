package com.barobot.gui.database;

import android.provider.BaseColumns;

public final class DataContract {
	public DataContract () {}
	
	private static final String TEXT_TYPE = " TEXT";
	private static final String FLOAT_TYPE = " FLOAT";
	private static final String INT_TYPE = " INTEGER";
	private static final String COMMA_SEP = ",";
	
	//---------------------------------
	// Liquids
	//
	public static final String SQL_CREATE_LIQUIDS =
		    "CREATE TABLE " + Liquids.TABLE_NAME + " (" +
		    Liquids._ID + INT_TYPE + " PRIMARY KEY," +
		    Liquids.COLUMN_NAME_NAME + TEXT_TYPE + COMMA_SEP +
		    Liquids.COLUMN_NAME_TYPE + TEXT_TYPE + COMMA_SEP +
		    Liquids.COLUMN_NAME_VOLTAGE + FLOAT_TYPE + 
		    " )";
	public static final String SQL_DELETE_LIQUIDS = 
			"DROP TABLE IF EXISTS " + Liquids.TABLE_NAME;
	
	public static abstract class Liquids implements BaseColumns
	{
		public static final String TABLE_NAME = "liquids";
		public static final String COLUMN_NAME_NAME = "name";
		public static final String COLUMN_NAME_TYPE = "type";
		public static final String COLUMN_NAME_VOLTAGE = "voltage";
	}
	
	//---------------------------------
	// Recipes
	//
	public static final String SQL_CREATE_RECIPES =
			"CREATE TABLE " + Recipes.TABLE_NAME + " (" +
			Recipes._ID + INT_TYPE + " PRIMARY KEY," +
			Recipes.COLUMN_NAME_NAME + TEXT_TYPE + COMMA_SEP +
			Recipes.COLUMN_NAME_DESCRIPTION + TEXT_TYPE + 
			" )";
	
	public static final String SQL_DELETE_RECIPES = 
			"DROP TABLE IF EXISTS " + Recipes.TABLE_NAME;
		
	public static abstract class Recipes implements BaseColumns
		{
			public static final String TABLE_NAME = "recipes";
			public static final String COLUMN_NAME_NAME = "name";
			public static final String COLUMN_NAME_DESCRIPTION = "description";
		}
	
	//---------------------------------
	// Ingredients
	//
	public static final String SQL_CREATE_INGREDIENTS =
			"CREATE TABLE " + Ingredients.TABLE_NAME + " (" +
			Ingredients._ID + INT_TYPE + " PRIMARY KEY," +
			Ingredients.COLUMN_NAME_RECIPEID + INT_TYPE + COMMA_SEP +
			Ingredients.COLUMN_NAME_LIQUIDID + INT_TYPE + COMMA_SEP +
			Ingredients.COLUMN_NAME_QUANTITY + INT_TYPE + 
			" )";
	
	public static final String SQL_DELETE_INGREDIENTS = 
			"DROP TABLE IF EXISTS " + Ingredients.TABLE_NAME;
		
	public static abstract class Ingredients implements BaseColumns
	{
		public static final String TABLE_NAME = "ingredients";
		public static final String COLUMN_NAME_RECIPEID = "recipeId";
		public static final String COLUMN_NAME_LIQUIDID = "liquidId";
		public static final String COLUMN_NAME_QUANTITY = "quantity";
	}
	
	//---------------------------------
	// Slots
	//
	public static final String SQL_CREATE_SLOTS =
			"CREATE TABLE " + Slots.TABLE_NAME + " (" +
			Slots._ID + INT_TYPE + " PRIMARY KEY," +
			Slots.COLUMN_NAME_POSITION + INT_TYPE + COMMA_SEP +
			Slots.COLUMN_NAME_LIQUIDID + INT_TYPE + COMMA_SEP +
			Slots.COLUMN_NAME_CAPACITY + INT_TYPE + 
			" )";
	
	public static final String SQL_DELETE_SLOTS = 
			"DROP TABLE IF EXISTS " + Slots.TABLE_NAME;
	
	public static abstract class Slots implements BaseColumns
	{
		public static final String TABLE_NAME = "slots";
		public static final String COLUMN_NAME_POSITION = "position";
		public static final String COLUMN_NAME_LIQUIDID = "liquidId";
		public static final String COLUMN_NAME_CAPACITY = "capacity";
	}
	
	
}
