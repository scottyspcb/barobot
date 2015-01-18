package com.barobot.sofa.api;

import java.io.IOException;
import java.util.List;

import android.util.JsonWriter;

import com.barobot.common.Initiator;
import com.barobot.gui.database.BarobotData;
import com.barobot.gui.dataobjects.Engine;
import com.barobot.gui.dataobjects.Ingredient_t;
import com.barobot.gui.dataobjects.Recipe_t;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.web.server.SofaServer;
import com.x5.template.Theme;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;

public class GetRecipesPage extends Page {

	public GetRecipesPage() {
		super();
		this.regex = "^\\/api\\/get_recipes$";
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
		if( barobot == null || barobot.state.getInt("SSERVER_API", 0) > 1 ){
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

		if (!session.getParms().containsKey("filter")) {
		}


		boolean showUnlisted = false; 
		boolean showUnnamed = false; 
		boolean showAll = false; 	

		String unlisted = session.getParms().get("unlisted");
		String unnamed	= session.getParms().get("unnamed");
		String all		= session.getParms().get("all");

		if( unlisted != null && unlisted.equals("true")){
			showUnlisted = true;
		}
		if( unnamed != null && unnamed.equals("true")){
			showUnnamed = true;
		}
		if( all != null && all.equals("true")){
			showAll = true;
		}
		List<Recipe_t> listAvailable = null;
		if(showAll){
			listAvailable = BarobotData.GetRecipes();
		}else{
			int[] b = BarobotData.getReciepeIds( showUnlisted, showUnnamed );
			listAvailable = BarobotData.getReciepesById( b );
		}
		//	listAvailable = engine.getRecipes();		// only available
		if (listAvailable == null)
		{
			return new JsonResponseBuilder()
						.status("ERRROR")
						.message("Getting recipes list failed")
						.build();
		}
		GetRecipesData data = new GetRecipesData(listAvailable );
		return new JsonResponseBuilder()
						.status("OK")
						.data(data)
						.build();
	}


public class GetRecipesData implements IData {

	private List<Recipe_t> mList;
	
	public GetRecipesData(List<Recipe_t> _list) {
		mList = _list;
	}
	@Override
	public void writeJson(JsonWriter writer) throws IOException {
		long startTime = System.currentTimeMillis();
		WriteList(writer, mList);
		long difference = System.currentTimeMillis() - startTime;
		Initiator.logger.i("GetRecipesData.writeJson.time",""+(difference));
	}

	private void WriteList(JsonWriter writer, List<Recipe_t> recipes) throws IOException
	{
		writer.name("result");
		writer.beginArray();
		for(Recipe_t item : recipes)
		{
			writer.beginObject();
			writer.name("id").value(item.id);
			writer.name("name").value(item.getName());
			writer.name("photoId").value(item.photoId);
			writer.name("counter").value(item.counter);
			writer.name("unlisted").value(item.unlisted);	

			int[] taste = item.getTaste();
			writer.name("taste");
			writer.beginObject();
			writer.name("sweet").value(taste[0]);
			writer.name("sour").value(taste[1]);
			writer.name("bitter").value(taste[2]);
			writer.name("strenght").value(taste[3]);
			writer.endObject();
		//	if(){
				WriteIngredients(writer, item.getIngredients());
		//	}
			writer.endObject();
		}
		writer.endArray();
	}
	
	private void WriteIngredients(JsonWriter writer, List<Ingredient_t> ingredients) throws IOException
	{
		writer.name("ingredients");
		writer.beginArray();
		for (Ingredient_t ing : ingredients)
		{
			writer.beginObject();
			writer.name("id").value(ing.id);
			writer.name("name").value(ing.getLiquid().getName());
			writer.name("quantity").value(ing.quantity);
			writer.name("liquid_id").value(ing.liquid);
			writer.endObject();
		}
		writer.endArray();
	}

}

}
