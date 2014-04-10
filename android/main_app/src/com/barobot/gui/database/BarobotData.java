package com.barobot.gui.database;

import java.util.List;

import org.orman.dbms.Database;
import org.orman.dbms.sqliteandroid.SQLiteAndroid;
import org.orman.mapper.MappingSession;
import org.orman.mapper.Model;
import org.orman.mapper.ModelQuery;
import org.orman.mapper.SchemaCreationPolicy;
import org.orman.sql.C;

import android.content.Context;

import com.barobot.gui.dataobjects.Category;
import com.barobot.gui.dataobjects.Ingredient_t;
import com.barobot.gui.dataobjects.Liquid_t;
import com.barobot.gui.dataobjects.Product;
import com.barobot.gui.dataobjects.Recipe_t;
import com.barobot.gui.dataobjects.Slot;
import com.barobot.gui.dataobjects.Type;

public class BarobotData {
	public static String DATABASE_NAME = "BarobotOrman.db";
	public static int DATABASE_SCHEMA_VERSION = 3;
	public static void StartOrmanMapping(Context context){
		// Setting up ORMAN
		Database omdb = new SQLiteAndroid(context, DATABASE_NAME, DATABASE_SCHEMA_VERSION);	
		MappingSession.getConfiguration().setCreationPolicy(SchemaCreationPolicy.CREATE);
		MappingSession.registerEntity(Category.class);
		MappingSession.registerEntity(Type.class);
		MappingSession.registerEntity(Liquid_t.class);
		MappingSession.registerEntity(Product.class);
		MappingSession.registerEntity(Slot.class);
		MappingSession.registerEntity(Ingredient_t.class);
		MappingSession.registerEntity(Recipe_t.class);
		MappingSession.registerDatabase(omdb);
		MappingSession.start();
	}
	
	public static void ClearTable (Class<?> cls)
	{
		Model.fetchQuery(ModelQuery.delete().from(cls).getQuery(),cls);
	}
	
	public static void ClearAllTables()
	{
		ClearTable(Slot.class);
		ClearTable(Product.class);
		ClearTable(Recipe_t.class);
		ClearTable(Ingredient_t.class);
		ClearTable(Liquid_t.class);
		ClearTable(Type.class);
		ClearTable(Category.class);
	}

	public static List<Recipe_t> GetRecipes()
	{
		return Model.fetchAll(Recipe_t.class);
	}
	
	public static List<Recipe_t> GetListedRecipes()
	{
		return Model.fetchQuery(ModelQuery.select().from(Recipe_t.class).where(C.eq("unlisted", false)).getQuery(),Recipe_t.class);
	}
	
	public static List<Recipe_t> GetFavoriteRecipes()
	{
		return Model.fetchQuery(ModelQuery.select().from(Recipe_t.class).where(
				C.and(
						C.eq("unlisted", false),
						C.eq("favorite", true)
						)).getQuery(),Recipe_t.class);
	}

	public static List<Slot> GetSlots()
	{
		return Model.fetchAll(Slot.class);
	}
	
	public static Slot GetSlot(int position)
	{
		return Model.fetchSingle(ModelQuery.select().from(Slot.class).where(C.eq("position", position)).getQuery(), Slot.class);
	}
	
	public static List<Type> GetTypes ()
	{
		return Model.fetchAll(Type.class);
	}
	
	public static List<Product> GetProduct()
	{
		return Model.fetchAll(Product.class);
	}
	
	public static Type GetType(long id)
	{
		return Model.fetchSingle(ModelQuery.select().from(Type.class).where(C.eq("id", id)).getQuery(), Type.class);
	}

}