package com.barobot.sofa.api;


import java.util.List;

import com.barobot.gui.dataobjects.Engine;
import com.barobot.gui.dataobjects.Recipe_t;
import com.barobot.web.server.SofaServer;
import com.x5.template.Theme;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;

public class GetRecipesPage extends Page {
	
	public GetRecipesPage() {
		super();
		this.regex = "^\\/api\\/get_recipes$";
	}
	
	@Override
	protected Response runInternal(String Url, SofaServer sofaServer,
			Theme theme, IHTTPSession session) {
		
		Engine engine = Engine.GetInstance();
		if (engine == null)
		{
			return new ResponseBuilder()
						.status("ERROR")
						.message("Engine not initialized")
						.build();
		}
		List<Recipe_t> listAvailable = engine.getRecipes();
		
		if (listAvailable == null)
		{
			return new ResponseBuilder()
						.status("ERRROR")
						.message("Getting recipes list failed")
						.build();
		}
		
		GetRecipesData data = new GetRecipesData(listAvailable);
		
		return new ResponseBuilder()
						.status("OK")
						.data(data)
						.build();
	}

}
