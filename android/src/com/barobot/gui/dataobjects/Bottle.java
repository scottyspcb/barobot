/**
 * 
 */
package com.barobot.gui.dataobjects;

/**
 * @author Raven
 * Class representing a bottle of liquid
 */
public class Bottle {
	
	public Bottle(Liquid liquid_, double cap)
	{
		// As default we consider the bottle full
		this(liquid_, cap, cap);
	}
	
	public Bottle(Liquid liquid_, double cap, double level)
	{
		liquid = liquid_;
		capacity = cap;
		currentLevel = level;
	}

	private Liquid liquid;
	private double capacity;
	private double currentLevel;
	
	public Liquid getLiquid()
	{
		return liquid;
	}
	
	public String getType()
	{
		return liquid.type;
	}
	
	public double GetFullfillmentPercentage()
	{
		return currentLevel*100/capacity;
	}
	
	
	@Override
	public String toString() {
		return liquid.type;
	}
}
