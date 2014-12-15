package com.barobot.sofa.api;

import java.io.IOException;
import java.util.List;

import org.orman.mapper.annotation.ManyToOne;

import com.barobot.gui.dataobjects.Ingredient_t;
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
			writer.name("Id").value(slot.id);
			writer.name("Name").value(slot.getName());
			writer.name("Position").value(slot.position);
			writer.name("CurrentVolume").value(slot.currentVolume);
			writer.name("Dispenser_type").value(slot.getCapacity());
			writer.name("Counter").value(slot.counter);
			writer.name("Product_capacity").value(slot.product.capacity);
			writer.name("Product_id").value(slot.product.id);

			writer.name("taste");
			writer.beginObject();
			writer.name("sweet").value(slot.product.liquid.sweet);
			writer.name("sour").value(slot.product.liquid.sour);
			writer.name("bitter").value(slot.product.liquid.bitter);
			writer.name("strenght").value(slot.product.liquid.strenght);
			writer.endObject();

			writer.endObject();
		}
		writer.endArray();
	}
}
