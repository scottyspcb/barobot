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
	
	public int getQuantity()
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
		result += what.toString(); 
	
		return result;
	}
}
