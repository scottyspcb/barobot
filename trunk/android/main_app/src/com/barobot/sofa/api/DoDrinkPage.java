package com.barobot.sofa.api;

import com.barobot.gui.dataobjects.Engine;
import com.barobot.gui.dataobjects.Recipe_t;
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
		engine.Pour(recipe, "api");
	//	engine.SetMessage("Pouring recipe no. " + recipe.id + ": " + recipe.name);

		return new JsonResponseBuilder().status("OK").build();
	}
}
