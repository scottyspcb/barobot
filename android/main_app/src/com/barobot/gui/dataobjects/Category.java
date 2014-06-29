package com.barobot.gui.dataobjects;

import org.orman.mapper.EntityList;
import org.orman.mapper.Model;
import org.orman.mapper.annotation.Entity;
import org.orman.mapper.annotation.OneToMany;
import org.orman.mapper.annotation.PrimaryKey;

import com.barobot.gui.utils.LangTool;

@Entity
public class Category extends Model<Category>{
	@PrimaryKey(autoIncrement=true)
	public int id;
	public String name;
	public String genre;
	
	@OneToMany(toType = Type.class, onField = "category")
	public EntityList<Category, Type> types = new EntityList<Category, Type>(Category.class, Type.class, this);
	public String getName() {
		return LangTool.translateName(id, "category", name );
	}
	@Override
	public String toString() {
		return getName();
	}
}
