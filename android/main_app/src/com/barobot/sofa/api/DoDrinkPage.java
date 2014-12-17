package com.barobot.sofa.api;

import com.barobot.common.Initiator;
import com.barobot.gui.dataobjects.Engine;
import com.barobot.gui.dataobjects.Recipe_t;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.parser.Queue;
import com.barobot.web.server.SofaServer;
import com.x5.template.Theme;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;

public class DoDrinkPage extends Page {

	public DoDrinkPage() {
		super();
		this.regex = "^\\/api\\/do_drink$";
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

		if (!session.getParms().containsKey("recipe_id")) {
			return new JsonResponseBuilder().status("ERROR")
					.message("recipe_id not found in request").build();
		}

		String recipeID = session.getParms().get("recipe_id");

		int id;
		try {
			id = Integer.parseInt(recipeID);
		} catch (NumberFormatException e) {
			return new JsonResponseBuilder().status("ERROR")
					.message("recipe_id is not a number").build();
		}

		Engine engine = Engine.GetInstance();
		if (engine == null) {
			return new JsonResponseBuilder().status("ERROR")
					.message("Cannot connect to barobot engine").build();
		}

		Recipe_t recipe = engine.getRecipe(id);
		if (recipe == null) {
			return new JsonResponseBuilder().status("ERROR")
					.message("recipe_id=" + id + " does not exist").build();
		}
		
		if (engine.CheckRecipe(recipe)== false)
		{
			return new JsonResponseBuilder().status("ERROR")
					.message("You lack appropriate ingredients").build();
		}


		Queue q_ready		= new Queue();	
		barobot.lightManager.carret_color( q_ready, 0, 255, 0 );
		q_ready.addWait(200);
		barobot.lightManager.carret_color( q_ready, 0, 100, 0 );
	  	Queue q_drink = engine.Pour(recipe, "api");
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
}
