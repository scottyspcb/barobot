package com.barobot.gui.dataobjects;

import java.util.List;

import org.orman.mapper.EntityList;
import org.orman.mapper.Model;
import org.orman.mapper.annotation.Entity;
import org.orman.mapper.annotation.ManyToOne;
import org.orman.mapper.annotation.OneToMany;
import org.orman.mapper.annotation.PrimaryKey;

import com.barobot.gui.utils.LangTool;

@Entity
public class Type extends Model<Type>{
	@PrimaryKey(autoIncrement=true)
	public int id;
	public String name;
	
	@ManyToOne
	public Category category;
	
	@OneToMany (toType = Liquid_t.class, onField = "type" )
	public EntityList<Type, Liquid_t> liquids = new EntityList<Type, Liquid_t>(Type.class, Liquid_t.class, this);
	
	public List<Liquid_t> getLiquids()
	{
		if(liquids.size() == 0 ){
			liquids.refreshList();
		}
		return liquids;
	}
	
	public void invalidateData(){
		liquids.refreshList();
	}

	public String getName() {
		return LangTool.translateName(id, "type", name );
	}
	@Override
	public String toString() {
		return getName();
	}
}
