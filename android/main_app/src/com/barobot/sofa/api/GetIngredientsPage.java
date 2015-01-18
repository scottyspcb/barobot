package com.barobot.sofa.api;


import java.io.IOException;
import java.util.List;

import android.util.JsonWriter;

import com.barobot.gui.database.BarobotData;
import com.barobot.gui.dataobjects.Engine;
import com.barobot.gui.dataobjects.Ingredient_t;
import com.barobot.gui.dataobjects.Liquid_t;
import com.barobot.gui.dataobjects.Recipe_t;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.web.server.SofaServer;
import com.x5.template.Theme;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;

public class GetIngredientsPage extends Page {
	
	public GetIngredientsPage() {
		super();
		this.regex = "^\\/api\\/get_ingredients$";
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

		Engine engine = Engine.GetInstance();
		if (engine == null)
		{
			return new JsonResponseBuilder()
						.status("ERROR")
						.message("Engine not initialized")
						.build();
		}
		List<Ingredient_t> ings =BarobotData.getIngredients();

		if (ings == null)
		{
			return new JsonResponseBuilder()
						.status("ERRROR")
						.message("Getting recipes list failed")
						.build();
		}
		GetIngredientData data = new GetIngredientData(ings);
		return new JsonResponseBuilder()
						.status("OK")
						.data(data)
						.build();
	}
	
	public class GetIngredientData implements IData {

		public GetIngredientData(List<Ingredient_t> ings) {
			mList = ings;
		}
		private List<Ingredient_t> mList;
		
		@Override
		public void writeJson(JsonWriter writer) throws IOException {
			WriteList(writer, mList);
		}

		private void WriteList(JsonWriter writer, List<Ingredient_t> mList2) throws IOException
		{
			writer.name("result");
			writer.beginArray();
			for(Ingredient_t ing : mList2)
			{
				writer.beginObject();
				writer.name("id").value(ing.id);
				writer.name("quantity").value(ing.quantity);
				writer.name("recipe_id").value( ing.recipe );
				writer.name("liquid_id").value(ing.liquid);

				Recipe_t r = ing.getRecipe();
				if(r == null ){
					writer.name("recipe_name").value("");		
				}else{
					writer.name("recipe_name").value(r.getName());
				}
				Liquid_t liquid = ing.getLiquid();
				if( liquid == null ){
					writer.name("liquid_name").value("");	
				}else{
					writer.name("liquid_name").value(liquid.getName());
				}
				writer.endObject();
			}
			writer.endArray();
		}


	}

}
