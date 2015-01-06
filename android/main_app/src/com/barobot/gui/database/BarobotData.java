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

import com.barobot.common.constant.Constant;
import com.barobot.gui.dataobjects.Category;
import com.barobot.gui.dataobjects.Dispenser_type;
import com.barobot.gui.dataobjects.Important_position;
import com.barobot.gui.dataobjects.Ingredient_t;
import com.barobot.gui.dataobjects.Language;
import com.barobot.gui.dataobjects.Liquid_t;
import com.barobot.gui.dataobjects.Log;
import com.barobot.gui.dataobjects.Log_drink;
import com.barobot.gui.dataobjects.Log_start;
import com.barobot.gui.dataobjects.Photo;
import com.barobot.gui.dataobjects.Product;
import com.barobot.gui.dataobjects.Recipe_t;
import com.barobot.gui.dataobjects.Robot;
import com.barobot.gui.dataobjects.Robot_config;
import com.barobot.gui.dataobjects.Slot;
import com.barobot.gui.dataobjects.Translated_name;
import com.barobot.gui.dataobjects.Type;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;

public class BarobotData {
	public static String DATABASE_NAME = Constant.DATABASE_NAME;
	public static int DATABASE_SCHEMA_VERSION = 3;
	public static Database omdb = null;
	public static void StartOrmanMapping(Context context){
		// Setting up ORMAN
		omdb = new SQLiteAndroid(context, DATABASE_NAME, DATABASE_SCHEMA_VERSION);	
		MappingSession.getConfiguration().setCreationPolicy(SchemaCreationPolicy.CREATE_IF_NOT_EXISTS);

		MappingSession.registerEntity(Category.class);
		MappingSession.registerEntity(Dispenser_type.class);
		MappingSession.registerEntity(Important_position.class);
		MappingSession.registerEntity(Ingredient_t.class);
		MappingSession.registerEntity(Language.class);	
		MappingSession.registerEntity(Liquid_t.class);
		MappingSession.registerEntity(Log.class);
		MappingSession.registerEntity(Photo.class);
		MappingSession.registerEntity(Product.class);
		MappingSession.registerEntity(Recipe_t.class);
		MappingSession.registerEntity(Robot.class);
		MappingSession.registerEntity(Robot_config.class);
		MappingSession.registerEntity(Slot.class);
		MappingSession.registerEntity(Translated_name.class);
		MappingSession.registerEntity(Type.class);
		MappingSession.registerEntity(Log_drink.class);
		MappingSession.registerEntity(Log_start.class);
		MappingSession.registerDatabase(omdb);
		MappingSession.start();
	}
	public static void ClearTable (Class<?> cls){
		Model.fetchQuery(ModelQuery.delete().from(cls).getQuery(),cls);
	}
	
	public static List<Recipe_t> GetRecipes(){
		return Model.fetchAll(Recipe_t.class);
	}

	public static List<Recipe_t> GetListedRecipes(){
		return Model.fetchQuery(ModelQuery.select().from(Recipe_t.class).where(C.eq("unlisted", false)).orderBy("Recipe_t.name").getQuery(),Recipe_t.class);
	}
	
	public static List<Recipe_t> GetFavoriteRecipes()
	{
		return Model.fetchQuery(ModelQuery.select().from(Recipe_t.class).where(
				C.and(
						C.eq("unlisted", false),
						C.eq("favorite", true)
						)).orderBy("Recipe_t.name").getQuery(),Recipe_t.class);
	}
	
	public static Recipe_t GetRecipe(int id)
	{
		return Model.fetchSingle(ModelQuery.select().from(Recipe_t.class).where(
				C.eq("id",  id)).getQuery(), Recipe_t.class);
	}

	public static Slot GetSlot(int position)
	{
		BarobotConnector barobot	= Arduino.getInstance().barobot;
		int robotId					= barobot.getRobotId();
		return Model.fetchSingle(ModelQuery.select().from(Slot.class).where(
				C.and(
						C.eq("robot_id", robotId),
						C.eq("position", position)
				)
				).limit(1).getQuery(), Slot.class);
	}

	public static List<Type> GetTypes ()
	{
		return Model.fetchAll(Type.class);
	}

	
	public static Type GetType(long id)
	{
		return Model.fetchSingle(ModelQuery.select().from(Type.class).where(C.eq("id", id)).getQuery(), Type.class);
	}

}
