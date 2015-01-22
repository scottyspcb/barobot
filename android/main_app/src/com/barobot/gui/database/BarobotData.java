package com.barobot.gui.database;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.orman.dbms.Database;
import org.orman.dbms.ResultList;
import org.orman.dbms.ResultList.ResultRow;
import org.orman.dbms.sqliteandroid.SQLiteAndroid;
import org.orman.mapper.MappingSession;
import org.orman.mapper.Model;
import org.orman.mapper.ModelQuery;
import org.orman.mapper.SchemaCreationPolicy;
import org.orman.sql.C;
import org.orman.sql.Query;
import org.orman.util.logging.LoggingLevel;
import org.orman.util.logging.StandardLogger;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.barobot.R;
import com.barobot.common.Initiator;
import com.barobot.common.constant.Constant;
import com.barobot.gui.dataobjects.Category;
import com.barobot.gui.dataobjects.Dispenser_type;
import com.barobot.gui.dataobjects.Engine;
import com.barobot.gui.dataobjects.Important_position;
import com.barobot.gui.dataobjects.Ingredient_t;
import com.barobot.gui.dataobjects.Language;
import com.barobot.gui.dataobjects.Liquid_t;
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
import com.barobot.parser.utils.Decoder;

public class BarobotData {
	public static String DATABASE_NAME = Constant.DATABASE_NAME;
	public static int DATABASE_SCHEMA_VERSION = 3;
	public static Database omdb = null;
	public static void StartOrmanMapping(Context context){
		// Setting up ORMAN
		omdb = new SQLiteAndroid(context, DATABASE_NAME, DATABASE_SCHEMA_VERSION);	
		
	//	org.orman.util.logging.Log.setLogger(new StandardLogger());
    //  org.orman.util.logging.Log.setLevel(LoggingLevel.TRACE);
		
		MappingSession.getConfiguration().setCreationPolicy(SchemaCreationPolicy.CREATE_IF_NOT_EXISTS);

		MappingSession.registerEntity(Category.class);
		MappingSession.registerEntity(Dispenser_type.class);
		MappingSession.registerEntity(Important_position.class);
		MappingSession.registerEntity(Ingredient_t.class);
		MappingSession.registerEntity(Language.class);	
		MappingSession.registerEntity(Liquid_t.class);
		MappingSession.registerEntity(com.barobot.gui.dataobjects.Log.class);
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
		return Model.fetchQuery(
				ModelQuery.select().from(Recipe_t.class).where(
				C.eq("unlisted", false)
			).orderBy("Recipe_t.name").getQuery(),Recipe_t.class);
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

	public static List<Type> GetTypes (){
		return Model.fetchAll(Type.class);
	}
	public static List<Ingredient_t> getIngredients() {
		return Model.fetchAll(Ingredient_t.class);
	}
	public static List<Product> getProducts() {
		return Model.fetchAll(Product.class);
	}
	public static List<Liquid_t> getLiquids() {
		return Model.fetchAll(Liquid_t.class);
	}

	public static int[] getReciepeIds( boolean showUnlisted, boolean showUnnamed ) {
		long startTime = System.currentTimeMillis();
		BarobotConnector barobot	= Arduino.getInstance().barobot;
		int robotId					= barobot.getRobotId();
		int[] ids					= {};

		// if count of existing ingredients is equal to all recipe's ingredients = recipe is available
		String que = "SELECT count(1) as ings"+ ", r.id as recipe_id"
		//		+ ", r.name as name, r.unlisted as unlisted"
				+ "	FROM slot s, product p, ingredient_t i, recipe_t r"
				+ " WHERE s.robot_id = "+robotId+" AND s.product = p.id AND i.liquid = p.liquid AND s.product!=0 AND r.id = i.recipe"
				+ ((showUnlisted) ? "" : " AND r.unlisted = 0")
				+ ((showUnnamed) ? "" : " AND r.name != '' AND r.name != 'Unnamed Drink'")
				+ " GROUP BY i.recipe"
				+ " HAVING ings >= (SELECT count(1) as ings2 FROM recipe_t r2, ingredient_t i2 WHERE i2.recipe= r2.id AND r2.id = r.id);";

	//	Initiator.logger.i("StartOrmanMapping","query3: "+ que); 
		Query query3	= new Query(que);
		ResultList res	= BarobotData.omdb.getExecuter().executeForResultList(query3);
		if(res == null){
			Initiator.logger.i("getReciepeIds","results: null"); 
		}else{
			int rc = res.getRowCount();
		//	Initiator.logger.i("getReciepeIds","results: "+ rc); 
			ids = new int[rc];
			for(int i=0;i<rc;i++){
				ResultRow rr = res.getResultRow(i);
				ids[i] = Decoder.toInt( rr.getColumn("recipe_id").toString(), -1);
			}
		}
		long difference = System.currentTimeMillis() - startTime;
		Initiator.logger.i("getReciepeIds.time",""+(difference));
		return ids;
	}

	public static List<Recipe_t> getReciepesById(int[] b) {
		long startTime = System.currentTimeMillis();
		List<Recipe_t> r = Model.fetchQuery(ModelQuery.select().from(Recipe_t.class).where(
				C.in("id", b)
			).orderBy("Recipe_t.name").getQuery(),Recipe_t.class);

		long difference = System.currentTimeMillis() - startTime;
		Initiator.logger.i("getReciepesById.time",""+(difference)); 
		return r;
	}

	private static Map<String, Object> obj_cache = new HashMap<String, Object>();
	public static <T> T getOneObject(Class<T> class1, long id ) {
		if( id == 0){
			return null;
		}
		String key = class1.getSimpleName()+id;
		if(obj_cache.containsKey(key)){
			@SuppressWarnings("unchecked")
			T obj = (T) obj_cache.get(key);		// it is OK if key is well build
			return obj;
		}
		T obj = Model.fetchSingle(ModelQuery.select().from(class1).where(
				C.eq("id", id)
				).limit(1).getQuery(), class1);

		if( !obj_cache.equals("")){		// remember
			obj_cache.put(key, obj);
		}
		return obj;
	}
	public static void reportChange(Class<? extends Model<?>> class1, long id) {
		if( id == 0){
			return;
		}
		String key = class1.getSimpleName()+id;
		obj_cache.remove(key);
	}
	

	public static void deleteRecipe( Activity act, final Recipe_t mCurrentRecipe, final boolean reload ) {
    	new AlertDialog.Builder(act).setTitle(R.string.are_you_sure_delete_drink).setMessage(mCurrentRecipe.getName())
	    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) {
	        	mCurrentRecipe.delete();
				Engine.GetInstance().invalidateRecipes();
				if(reload){
					Engine.GetInstance().getRecipes();
				}
	        }
	    })
	    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) {}
	    }).setIcon(android.R.drawable.ic_dialog_alert).show();
	}

	
	
}
