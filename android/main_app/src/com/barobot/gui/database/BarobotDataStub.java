package com.barobot.gui.database;

import com.barobot.gui.dataobjects.*;

public class BarobotDataStub {
	
	public static void SetupDatabase()
	{
		SetupSlots();
	}
	
	private static void SetupSlots()
	{
	
		for (int i= 1 ; i <= 12; i++)
		{	
			Slot slot = new Slot();
			slot.position = i;
			slot.status = "Empty";
			//slot.product = null;
			
			slot.insert();	
		}
		
		Type type = new Type();
		type.name = "Vodka";
		type.insert();
		
		Liquid_t liq = new Liquid_t();
		liq.name = "Wyborowa";
		liq.type = type;
		liq.insert();
		
		liq = new Liquid_t();
		liq.name = "Żubrówka";
		liq.type = type;
		liq.insert();
		
		liq = new Liquid_t();
		liq.name = "Smirnoff";
		liq.type = type;
		liq.insert();
		
		liq = new Liquid_t();
		liq.name = "Finlandia";
		liq.type = type;
		liq.insert();
		
		Product prod = new Product();
		prod.capacity = 700;
		prod.liquid = liq;
		prod.insert();
		
		Recipe_t rec = new Recipe_t();
		rec.name = "White Rav";
		rec.insert();
		
		rec = new Recipe_t();
		rec.name = "Lorem";
		rec.insert();
		
		rec = new Recipe_t();
		rec.name = "Ipsum";
		rec.insert();
		
		rec = new Recipe_t();
		rec.name = "Dolor";
		rec.insert();
		
		rec = new Recipe_t();
		rec.name = "Mortus";
		rec.insert();
		
		rec = new Recipe_t();
		rec.name = "Ignis";
		rec.insert();
		
		rec = new Recipe_t();
		rec.name = "Lumen";
		rec.insert();
		
		Slot slot = BarobotData.GetSlot(1);
		slot.product = prod;
		slot.status = "OK";
		slot.currentVolume = prod.capacity;
		slot.update();
	}
}
