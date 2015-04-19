package com.barobot.sofa.api;

import java.util.ArrayList;
import java.util.List;

import com.barobot.common.Initiator;
import com.barobot.common.constant.Constant;
import com.barobot.gui.database.BarobotData;
import com.barobot.gui.dataobjects.Engine;
import com.barobot.gui.dataobjects.Ingredient_t;
import com.barobot.gui.dataobjects.Recipe_t;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.parser.Queue;
import com.barobot.parser.utils.Decoder;
import com.barobot.web.server.SofaServer;
import com.x5.template.Theme;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;

public class DoCustomDrinkPage extends Page {

	public DoCustomDrinkPage() {
		super();
		this.regex = "^\\/api\\/do_custom_drink$";
	}

	@Override
	public void setHeaders(Response r) {
		super.setHeaders(r);
		r.setMimeType("application/json");
	}

	@Override
	protected JsonResponse runInternal(String Url, SofaServer sofaServer,
			Theme theme, IHTTPSession session) {

		BarobotConnector barobot = Arduino.getInstance().barobot;
		if(barobot.state.getInt("SSERVER_API", 0) > 1 ){
			return new JsonResponseBuilder()
			.status("ERRROR")
			.message("API disabled")
			.build();
		}

		if (!session.getParms().containsKey("liquids")) {
			return new JsonResponseBuilder().status("ERROR")
					.message("liquids not found in request").build();
		}
		if (!session.getParms().containsKey("volume")) {
			return new JsonResponseBuilder().status("ERROR")
					.message("volume not found in request").build();
		}

		String liquids				= session.getParms().get("liquids");
		String volume				= session.getParms().get("volume");
		String[] liquids_array		= liquids.split(",");
		String[] volume_array		= volume.split(",");
		if(liquids_array.length != volume_array.length ){
			return new JsonResponseBuilder().status("ERROR").message("liquids count should be equal to volume count").build();
		}
		Engine engine = Engine.GetInstance();
		if (engine == null) {
			return new JsonResponseBuilder().status("ERROR").message("Cannot connect to barobot engine").build();
		}

		if(barobot.lightManager.demoStarted){
			barobot.lightManager.stopDemo(barobot.main_queue);
		}

		List<Ingredient_t> ingredients = getIngrediends(liquids_array, volume_array ) ;

		Recipe_t recipe = new Recipe_t();
		recipe.name		= Constant.UNNAMED_DRINK;
		recipe.unlisted = true;
		recipe.insert();
		for(Ingredient_t ing : ingredients){
			recipe.addIngredient(ing);
		}
		recipe.refreshList();

		Queue q_ready		= new Queue();	
		barobot.lightManager.carret_color( q_ready, 0, 255, 0 );
		q_ready.addWait(300);
		barobot.lightManager.carret_color( q_ready, 0, 100, 0 );
		Queue q_drink = Engine.GetInstance().Pour(recipe, "api_creator");
		q_ready.add(q_drink);

		Queue q_error		= new Queue();	
		barobot.lightManager.carret_color( q_error, 255, 0, 0 );

		boolean igrq		= barobot.weight.isGlassRequired();
		boolean igrd		= barobot.weight.isGlassReady();

		if(!igrq){
			Initiator.logger.i( "pourStart", "dont need glass");
			barobot.main_queue.add(q_drink);
		}else if(igrd){
			Initiator.logger.i( "pourStart", "is Glass Ready");
			barobot.main_queue.add(q_drink);
		}else{
			Initiator.logger.i( "pourStart", "wait for Glass");
			Queue q = new Queue();
			barobot.weight.waitForGlass( q, q_ready, q_error);
			barobot.main_queue.add(q);
		}
	//	engine.SetMessage("Pouring recipe no. " + recipe.id + ": " + recipe.name);
		return new JsonResponseBuilder().status("OK").build();
	}

	private List<Ingredient_t> getIngrediends(String[] liquids_array,
			String[] volume_array) {
		
		List<Ingredient_t> ret = new ArrayList<Ingredient_t>();

		//drink_size += slot.dispenser_type;

		for( int i=0;i<liquids_array.length;i++){
			int id 		= Decoder.toInt(liquids_array[i], -1);
			int volume 	= Decoder.toInt(volume_array[i], -1);
			if( id == -1 || volume == -1){
				return null;	
			}
			Ingredient_t ingredient = new Ingredient_t();
			ingredient.liquid	= id;
			ingredient.quantity = volume;
			ret.add(ingredient);
		}
		return ret;
	}
}
