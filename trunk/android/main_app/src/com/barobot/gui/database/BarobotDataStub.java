package com.barobot.gui.database;

import com.barobot.gui.dataobjects.*;
import com.barobot.hardware.devices.BarobotConnector;

public class BarobotDataStub {
	
	private static void SetupSlots()
	{
		for (int i= 1 ; i <= 12; i++)
		{	
			Slot slot = new Slot();
			slot.position = i;
			slot.dispenser_type = 20;
			slot.status = "Empty";
			//slot.product = null;
			slot.insert();	
		}

		Type type = new Type();
		type.name = "Strong";
		type.insert();

		Liquid_t liq = new Liquid_t();
		liq.name = "Wyborowa";
		liq.type = type;
		liq.sweet = 10;
		liq.insert();
		
		liq = new Liquid_t();
		liq.name = "Rum";
		liq.type = type;
		liq.sweet = 25;
		liq.insert();
		
		Liquid_t liq2 = new Liquid_t();
		liq2.name = "Samo Dobro";
		liq2.type = type;
		liq2.sweet = 75;
		liq2.insert();
		
		Liquid_t liq3 = new Liquid_t();
		liq3.name = "Finlandia";
		liq3.type = type;
		liq3.insert();
		
		Product prod = new Product();
		prod.capacity = 700;
		prod.liquid = liq;
		prod.insert();
		
		Product prod2 = new Product();
		prod2.capacity = 500;
		prod2.liquid = liq2;
		prod2.insert();
		
		Product prod3 = new Product();
		prod3.capacity = 100;
		prod3.liquid = liq3;
		prod3.insert();
		
		Ingredient_t ing = new Ingredient_t();
		ing.liquid = liq;
		ing.quantity = 40;
		ing.insert();
		
		Ingredient_t ing2 = new Ingredient_t();
		ing2.liquid = liq2;
		ing2.quantity = 60;
		ing2.insert();
		
		Ingredient_t ing3 = new Ingredient_t();
		ing3.liquid = liq3;
		ing3.quantity = 20;//BarobotConnector.getCapacity();
		ing3.insert();

		Recipe_t rec = new Recipe_t();
		rec.name = "A long drink name to see how it fits";
		rec.insert();
		rec.ingredients.add(ing);
		rec.ingredients.add(ing2);
		rec.ingredients.add(ing3);
		
		rec = new Recipe_t();
		rec.name = "My favorite drink";
		rec.favorite = true;
		rec.photoId = 2;
		rec.insert();
		rec.ingredients.add(ing2);
		
		rec = new Recipe_t();
		rec.name = "You should not see me!";
		rec.unlisted = true;
		rec.insert();
		rec.ingredients.add(ing3);
		
		Slot slot = BarobotData.GetSlot(1);
		slot.product = prod;
		slot.status = "OK";
		slot.currentVolume = prod.capacity;
		slot.update();
		
		Slot slot3 = BarobotData.GetSlot(3);
		slot3.product = prod2;
		slot3.status = "OK";
		slot3.currentVolume = prod.capacity;
		slot3.update();
		
		Slot slot6 = BarobotData.GetSlot(6);
		slot6.product = prod3;
		slot6.status = "OK";
		slot6.currentVolume = prod.capacity;
		slot6.update();
		
	}
}
