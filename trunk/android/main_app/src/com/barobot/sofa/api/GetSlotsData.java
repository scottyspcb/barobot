package com.barobot.sofa.api;

import java.io.IOException;
import java.util.List;

import org.orman.mapper.annotation.ManyToOne;

import com.barobot.gui.dataobjects.Ingredient_t;
import com.barobot.gui.dataobjects.Liquid_t;
import com.barobot.gui.dataobjects.Product;
import com.barobot.gui.dataobjects.Recipe_t;
import com.barobot.gui.dataobjects.Slot;

import android.util.JsonWriter;

public class GetSlotsData implements IData {

	private List<Slot> mList;
	
	public GetSlotsData(List<Slot> slots) {
		mList = slots;
	}
	@Override
	public void writeJson(JsonWriter writer) throws IOException {
		WriteList(writer, mList);
	}
	
	private void WriteList(JsonWriter writer, List<Slot> mList2) throws IOException
	{
		writer.name("result");
		writer.beginArray();
		for(Slot slot : mList2)
		{
			writer.beginObject();
			writer.name("id").value(slot.id);
			writer.name("name").value(slot.getName());
			writer.name("position").value(slot.position);
			writer.name("currentVolume").value(slot.currentVolume);
			writer.name("dispenser_type").value(slot.getCapacity());
			writer.name("counter").value(slot.counter);

			if( slot.product == null ){
				writer.name("product_id").value(0);
				writer.name("product_capacity").value(0);
				writer.name("product_init_needed").value(false);
				writer.name("taste");
				writer.beginObject();
				writer.name("sweet").value(0);
				writer.name("sour").value(0);
				writer.name("bitter").value(0);
				writer.name("strenght").value(0);
				writer.endObject();	
			}else{	
				writer.name("product_id").value(slot.product.id);
				writer.name("product_capacity").value(slot.product.capacity);
				writer.name("product_init_needed").value(slot.product.initNeeded);
				writer.name("taste");
				writer.beginObject();
				
				Liquid_t liquid = slot.product.liquid;
				writer.name("sweet").value(liquid.sweet);
				writer.name("sour").value(liquid.sour);
				writer.name("bitter").value(liquid.bitter);
				writer.name("strenght").value(liquid.strenght);
				writer.endObject();
			}
			writer.endObject();
		}
		writer.endArray();
	}
}
