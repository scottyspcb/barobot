package com.barobot.gui.dataobjects;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.orman.mapper.Model;
import org.orman.mapper.ModelQuery;
import org.orman.sql.C;
import org.orman.sql.Query;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;

import com.barobot.BarobotMain;
import com.barobot.android.Android;
import com.barobot.common.Initiator;
import com.barobot.common.constant.Constant;
import com.barobot.gui.database.BarobotData;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.other.StartupException;
import com.barobot.parser.Queue;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.message.Mainboard;
import com.barobot.parser.utils.Decoder;

public class Engine {
	private List<Slot> slots;
	private List<Recipe_t> recipes;
	private static Map<Integer, Slot> liquid2slot = null;
	private static Engine instance;

	public static Engine createInstance(Context context) throws StartupException{
		if (instance == null){
			instance = new Engine(context);
		}
		return instance;
	}

	public static Engine GetInstance(){
		return instance;
	}

	private Engine(Context context) throws StartupException
	{
		try {
			String appPath2 	= context.getPackageManager().getPackageInfo(context.getPackageName(), 0).applicationInfo.dataDir;
			String dbPath 		= appPath2+"/databases/" + BarobotData.DATABASE_NAME;

		//	Initiator.logger.i("Engine.app path", appPath2 );	// /data/data/com.barobot/databases/
			Initiator.logger.i("Engine.db path", dbPath );	// /data/data/com.barobot/databases/ + DATABASE_NAME;

			// check there is a valid database
			File ss = context.getDatabasePath( BarobotData.DATABASE_NAME );
			if( ss.exists() ){
				Initiator.logger.i("Engine DB", "db exists in: " + ss.getAbsolutePath() );
			}else{
				String resetPath	= 	Environment.getExternalStorageDirectory()+ Constant.copyPath;	
				File src = new File(resetPath);
				if(src.exists()){
					try {
						Initiator.logger.i("Engine DB ", "copy from SD card: "+ resetPath );
						Android.copy( resetPath, dbPath );
					} catch (IOException e) {
						Initiator.logger.e("Engine DB ", "copy error", e );
						throw new StartupException( "BarobotOrman Error", e );
					}
				}else{
					Initiator.logger.e("Engine DB File not exists:", resetPath );
					Initiator.logger.e("Engine DB File", "copy from assets: "+ "/BarobotOrman.db" );
					if(Android.copyAsset( context, "BarobotOrman.db", Constant.localDbPath ) == false){
						Initiator.logger.e("Engine.Engine", "BarobotOrman Erro");
						throw new StartupException( "BarobotOrman Error" );
					}
				}
			}
			BarobotData.StartOrmanMapping(context);

		} catch (NameNotFoundException e1) {
			Initiator.logger.w("Engine.Engine", "NameNotFoundException", e1);
			BarobotMain.lastException = e1;
			throw new StartupException( "BarobotOrman Error", e1 );
		//	return false;
		}
		//return true;
	}
	public List<Slot> loadSlots()
	{
		if (slots == null)
		{
			BarobotConnector barobot	= Arduino.getInstance().barobot;
			int robotId					= barobot.getRobotId();
			Query q						= ModelQuery.select().from(Slot.class).where(C.eq("robot_id", robotId)).getQuery();
		//	Initiator.logger.w("Engine.loadSlots.sql", q.toString());
			slots						= Model.fetchQuery(q, Slot.class);
			liquid2slot					= new HashMap<Integer, Slot>();		// slots in robot with robotId
			for(Slot sl : slots){
				if(sl.product!= null && sl.product.liquid != null ){
					liquid2slot.put(sl.product.liquid.id, sl);
				}
			}
		}
		return slots;
	}

	public void invalidateData()
	{
		slots			= null;
		liquid2slot		= null;
		recipes			= null;
	}
	public Engine invalidateRecipes()
	{
		recipes			= null;
		return this;
	}

	public void invalidateSlots()
	{
		slots		= null;
		liquid2slot = null;
	}

	public List<Recipe_t> getRecipes(){
		if (recipes == null){
			recipes = Filter(BarobotData.GetListedRecipes());
		}
		return recipes;
	}

	private List<Recipe_t> Filter(List<Recipe_t> recipes){
		List<Recipe_t> result = new ArrayList<Recipe_t>();
		
		for(Recipe_t recipe : recipes){
			if (CheckRecipe(recipe)){
				result.add(recipe);
			}
		}
		return result;
	}

	public Boolean CheckRecipe(Recipe_t recipe){
		Map<Integer, Integer> bottleSequence = GenerateBottleUsage(recipe.getIngredients());
		if (bottleSequence == null){
			return false; // We could not find some of the ingredients
		}
		return true;
	}

	public Queue Pour(final Recipe_t recipe, String orderSource)
	{
		List<Ingredient_t> ings = recipe.getIngredients();
		final BarobotConnector barobot = Arduino.getInstance().barobot;
		final Log_drink ld	= new Log_drink();
		int pours			= 0;
		int quantity		= 0;
		int real_quantity	= 0;

		ld.robot_id			= barobot.getRobotId();
		ld.order_source		= orderSource;
		ld.datetime			= Decoder.getTimestamp();
		ld.id_drink			= recipe.id;
		ld.ingredients		= ings.size(); 
		ld.size				= pours;
		ld.size_ml			= quantity;
		ld.size_real_ml		= real_quantity;
		ld.error_code		= 0;
		Queue q				= new Queue();
		q.add( new AsyncMessage( true ) {
			@Override	
			public String getName() {
				return "start drink" ;
			}
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				ld.temp_before	= barobot.getLastTemp();
				barobot.state.set( "DOING_DRINK", recipe.id );
				return null;
			}
		} );
		barobot.startDoingDrink(q);

		for(Ingredient_t ing : ings){
			Slot slot = getIngredientSlot(ing);
			if (slot != null){
				int position	= slot.position;
				int count		= slot.getSequence( ing.quantity );
				quantity		+=ing.quantity;
				// ing.liquid.strenght >=40 		// TODO. alcohol don't do pacpac

				real_quantity	+=slot.dispenser_type * count;
				pours 			+=count;
				barobot.moveToBottle(q, position-1, true );
				for (int iter = 1; iter <= count ; iter++){
					if( iter > 1){
						int repeat_time = barobot.getRepeatTime( position-1, slot.dispenser_type );
					//	Log.i("Prepare", "addWait"+repeat_time );
						q.addWait( repeat_time  );			// wait for refill
					}
		//			Log.i("Prepare", "pour" );
					barobot.pour(q, slot.dispenser_type, position-1, false, true );
				}
				saveStats( slot, q, count );
				saveStats( ing.getLiquid(), q, count);

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
				barobot.moveToStart( q2 );		// na koniec
				barobot.onDrinkFinish( q2 );
				return q2;
			}
		} );
		Android.readTabletTemp( q );
		q.addWithDefaultReader("S" );		// read temp after
		q.add( new AsyncMessage( true ) {
			@Override	
			public String getName() {
				return "save recipe stats" ;
			}
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				recipe.counter++;
				recipe.update();
				barobot.state.set( "DOING_DRINK", 0 );
				ld.temp_after	= barobot.getLastTemp();
				ld.time			= Decoder.getTimestamp() - ld.datetime;		// time diff in sec
				ld.insert();
				return null;
			}
		} );
		return q;
	}
	
	private static void saveStats(final Liquid_t liquid, Queue q, final int count) {
		q.add( new AsyncMessage( true ) {
			@Override	
			public String getName() {
				return "save liquid stats" ;
			}
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				liquid.counter+=count;
				liquid.update();
				return null;
			}
		} );
	}

	private static void saveStats(final Slot slot, Queue q, final int count) {
		q.add( new AsyncMessage( true ) {
			@Override	
			public String getName() {
				return "save slot stats" ;
			}
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				slot.counter+=count;
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
	public Map<Integer, Integer> GenerateBottleUsage(List<Ingredient_t> ingredients)
	{
		Map<Integer, Integer> nMap 	= new HashMap<Integer, Integer>();
		loadSlots();
		for(Ingredient_t ing : ingredients)
		{
			if(ing.liquid == 0){
				return null;	
			}
			Slot slot = liquid2slot.get(ing.liquid);
			if (slot == null){
				return null;
			}else{
				int count = slot.getSequence( ing.quantity );
			//	int newQuantity	= count * slot.dispenser_type;
				nMap.put(slot.position, count);
			}
		}
		return nMap;
	}

	public Slot getIngredientSlot(Ingredient_t ing){
		loadSlots();
		if(ing.liquid == 0){
			return null;	
		}
		return liquid2slot.get(ing.liquid);	// id to be more universal
	}
}
