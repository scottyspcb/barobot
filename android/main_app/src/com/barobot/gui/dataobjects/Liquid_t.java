package com.barobot.gui.dataobjects;

import java.util.List;

import org.orman.mapper.EntityList;
import org.orman.mapper.Model;
import org.orman.mapper.annotation.Entity;
import org.orman.mapper.annotation.ManyToOne;
import org.orman.mapper.annotation.OneToMany;
import org.orman.mapper.annotation.PrimaryKey;

@Entity
public class Liquid_t extends Model<Liquid_t> {
	@PrimaryKey (autoIncrement = true)
	public long id;
	public String name;
	public float voltage;
	
	@ManyToOne
	public Type type;
	
	@OneToMany (toType = Product.class, onField="liquid")
	public EntityList<Liquid_t, Product> products = new EntityList<Liquid_t, Product>(Liquid_t.class, Product.class, this);
	
	public List<Product> getProducts()
	{
		products.refreshList();
		return products;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
