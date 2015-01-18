package com.barobot.sofa.api;

import java.io.IOException;
import java.util.List;

import com.barobot.gui.dataobjects.Ingredient_t;
import com.barobot.gui.dataobjects.Product;
import com.barobot.gui.dataobjects.Recipe_t;

import android.util.JsonWriter;

public class GetProductsData implements IData {

	private List<Product> mList;
	
	public GetProductsData(List<Product> listAvailable) {
		mList = listAvailable;
	}
	@Override
	public void writeJson(JsonWriter writer) throws IOException {
		WriteList(writer, mList);
	}

	private void WriteList(JsonWriter writer, List<Product> recipes) throws IOException
	{
		writer.name("result");
		writer.beginArray();
		for(Product item : recipes)
		{
			writer.beginObject();
			writer.name("id").value(item.id);
			writer.name("name").value(item.getName());
			writer.name("initNeeded").value(item.initNeeded);
			writer.name("capacity").value(item.capacity);
			if( item.liquid == null){
				writer.name("liquid_id").value(0);
				writer.name("liquid_name").value("");	
			}else{
				writer.name("liquid_id").value(item.liquid.id);
				writer.name("liquid_name").value(item.liquid.getName());
			}
			writer.endObject();
		}
		writer.endArray();
	}

}
