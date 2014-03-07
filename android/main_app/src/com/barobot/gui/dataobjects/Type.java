package com.barobot.gui.dataobjects;

import org.orman.mapper.EntityList;
import org.orman.mapper.Model;
import org.orman.mapper.annotation.Entity;
import org.orman.mapper.annotation.ManyToOne;
import org.orman.mapper.annotation.OneToMany;
import org.orman.mapper.annotation.PrimaryKey;

@Entity
public class Type extends Model<Type>{
	@PrimaryKey(autoIncrement=true)
	public long id;
	public String name;
	
	@ManyToOne
	public Category category;
	
	@OneToMany (toType = Liquid_t.class, onField = "type" )
	public EntityList<Type, Liquid_t> liquids = new EntityList<Type, Liquid_t>(Type.class, Liquid_t.class, this);
	
	@Override
	public String toString() {
		return name;
	}
}
