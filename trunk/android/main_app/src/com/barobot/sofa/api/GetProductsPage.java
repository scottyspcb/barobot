package com.barobot.sofa.api;


import java.util.List;

import com.barobot.gui.database.BarobotData;
import com.barobot.gui.dataobjects.Engine;
import com.barobot.gui.dataobjects.Product;
import com.barobot.gui.dataobjects.Recipe_t;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.web.server.SofaServer;
import com.x5.template.Theme;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;

public class GetProductsPage extends Page {
	
	public GetProductsPage() {
		super();
		this.regex = "^\\/api\\/get_products$";
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

		List<Product> listAvailable = BarobotData.getProducts();

		if (listAvailable == null)
		{
			return new JsonResponseBuilder()
						.status("ERRROR")
						.message("Getting recipes list failed")
						.build();
		}
		
		GetProductsData data = new GetProductsData(listAvailable);
		
		return new JsonResponseBuilder()
						.status("OK")
						.data(data)
						.build();
	}

}
