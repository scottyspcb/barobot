package com.barobot.sofa.api;

import java.io.IOException;
import java.util.List;

import android.util.JsonWriter;

import com.barobot.gui.database.BarobotData;
import com.barobot.gui.dataobjects.Engine;
import com.barobot.gui.dataobjects.Liquid_t;
import com.barobot.gui.dataobjects.Type;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.web.server.SofaServer;
import com.x5.template.Theme;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;

public class GetLiquidsPage extends Page {
	
	public GetLiquidsPage() {
		super();
		this.regex = "^\\/api\\/get_liquids$";
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
		List<Liquid_t> listAvailable = BarobotData.getLiquids();

		if (listAvailable == null)
		{
			return new JsonResponseBuilder()
						.status("ERRROR")
						.message("Getting recipes list failed")
						.build();
		}
		GetLiquidsData data = new GetLiquidsData(listAvailable);
		return new JsonResponseBuilder()
						.status("OK")
						.data(data)
						.build();
	}

	public class GetLiquidsData implements IData {
		private List<Liquid_t> mList;

		public GetLiquidsData(List<Liquid_t> _list) {
			mList = _list;
		}
		@Override
		public void writeJson(JsonWriter writer) throws IOException {
			WriteList(writer, mList);
		}

		private void WriteList(JsonWriter writer, List<Liquid_t> items) throws IOException
		{
			writer.name("result");
			writer.beginArray();
			for(Liquid_t item : items)
			{
				writer.beginObject();
				writer.name("id").value(item.id);
				writer.name("name").value(item.getName());
				writer.name("type_id").value(item.type);

				// get type
				writer.name("type_name").value(item.getLiquidType().getName());		
				writer.name("counter").value(item.counter);
	
				writer.name("taste");
				writer.beginObject();
				writer.name("sweet").value(item.sweet);
				writer.name("sour").value(item.sour);
				writer.name("bitter").value(item.bitter);
				writer.name("strenght").value(item.strenght);
				writer.endObject();

				writer.endObject();
			}
			writer.endArray();
		}
	}
}
