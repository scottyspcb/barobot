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
		
		liq = new Liquid_t();
		liq.name = "Finlandia";
		liq.type = type;
		liq.insert();
		
		liq = new Liquid_t();
		liq.name = "Finlandia";
		liq.type = type;
		liq.insert();
		
		liq = new Liquid_t();
		liq.name = "Finlandia";
		liq.type = type;
		liq.insert();
		
		liq = new Liquid_t();
		liq.name = "Finlandia";
		liq.type = type;
		liq.insert();
		
		liq = new Liquid_t();
		liq.name = "Finlandia";
		liq.type = type;
		liq.insert();
		
		liq = new Liquid_t();
		liq.name = "Finlandia";
		liq.type = type;
		liq.insert();
		
		liq = new Liquid_t();
		liq.name = "Finlandia";
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
		
		Slot slot = BarobotData.GetSlot(1);
		slot.product = prod;
		slot.status = "OK";
		slot.currentVolume = prod.capacity;
		slot.update();
	}
}
