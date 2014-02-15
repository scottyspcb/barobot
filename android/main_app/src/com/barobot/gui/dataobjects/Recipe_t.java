package com.barobot.gui.dataobjects;

import org.orman.mapper.Model;
import org.orman.mapper.annotation.Entity;
import org.orman.mapper.annotation.PrimaryKey;

@Entity
public class Recipe_t extends Model<Recipe_t>{
	@PrimaryKey (autoIncrement = true)
	public long id;
	public String name;
	public String Description;
	public boolean favorite;
}
