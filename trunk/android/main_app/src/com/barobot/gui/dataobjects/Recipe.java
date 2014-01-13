package com.barobot.gui.dataobjects;

import java.util.List;

public class Recipe {
	

	private long id;
	private String name;
	private String description;
	private List<Ingredient> ingridients;
	
	public Recipe(long id_, String name_, String description_,  List<Ingredient> ingridients_)
	{
		id = id_;
		name = name_;
		description = description_;
		ingridients = ingridients_;
	}
	
	public long getId()
	{
		return id;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getDescription()
	{
		return description;
	}
	
	public List<Ingredient> getIngridients()
	{
		return ingridients;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
