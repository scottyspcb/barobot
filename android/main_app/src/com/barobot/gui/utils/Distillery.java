package com.barobot.gui.utils;

import java.util.List;

import com.barobot.gui.dataobjects.Ingredient_t;

public class Distillery {
	public static int getSweet(List<Ingredient_t> ingredients) 
	{
		int value=0;
		int weights=0;
		for(Ingredient_t ing : ingredients)
		{
			value+= (ing.liquid.sweet * ing.quantity);
			weights+= ing.quantity;
		}
		
		if (weights == 0)
			return 0;
		
		return (int) value / weights;
	}
	
	public static int getSour(List<Ingredient_t> ingredients) 
	{
		int value=0;
		int weights=0;
		for(Ingredient_t ing : ingredients)
		{
			value+= (ing.liquid.sour * ing.quantity);
			weights+= ing.quantity;
		}
		
		if (weights == 0)
			return 0;
		
		return (int) value / weights;
	}
	
	public static int getBitter(List<Ingredient_t> ingredients) 
	{
		int value=0;
		int weights=0;
		for(Ingredient_t ing : ingredients)
		{
			value+= (ing.liquid.bitter * ing.quantity);
			weights+= ing.quantity;
		}
		
		if (weights == 0)
			return 0;
		
		return (int) value / weights;
	}
	
	public static int getStrength(List<Ingredient_t> ingredients) 
	{
		int value=0;
		int weights=0;
		for(Ingredient_t ing : ingredients)
		{
			value+= (ing.liquid.strenght * ing.quantity);
			weights+= ing.quantity;
		}
		
		if (weights == 0)
			return 0;
		
		return (int) value / weights;
	}
	
	
}
