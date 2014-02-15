package com.barobot.gui.dataobjects;

import org.orman.mapper.EntityList;
import org.orman.mapper.Model;
import org.orman.mapper.annotation.Entity;
import org.orman.mapper.annotation.OneToMany;
import org.orman.mapper.annotation.PrimaryKey;

@Entity
public class Category extends Model<Category>{
	@PrimaryKey(autoIncrement=true)
	public long id;
	public String name;
	public String genre;
	
	@OneToMany(toType = Type.class, onField = "category")
	public EntityList<Category, Type> types = new EntityList<Category, Type>(Category.class, Type.class, this);
}
