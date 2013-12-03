package com.barobot.gui.dataobjects;

public class Ingredient {
	private Liquid what;
	private int quantity;
	public Ingredient(Liquid what_, int how_much_)
	{
		what = what_;
		quantity = how_much_;
	}
	
	public Liquid getLiquid()
	{
		return what;
	}
	
	public double getQuantity()
	{
		return quantity;
	}
	
	@Override
	public String toString() {
		String result = "";
		
		if (quantity > 0)
		{
			result += quantity + " ml ";
		}
		result += what.type; 
	
		return result;
	}
}
