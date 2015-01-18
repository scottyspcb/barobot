package com.barobot.gui.dataobjects;

import java.util.List;

import org.orman.mapper.EntityList;
import org.orman.mapper.Model;
import org.orman.mapper.ModelQuery;
import org.orman.mapper.annotation.Entity;
import org.orman.mapper.annotation.ManyToOne;
import org.orman.mapper.annotation.OneToMany;
import org.orman.mapper.annotation.PrimaryKey;
import org.orman.sql.C;

import com.barobot.gui.database.BarobotData;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.other.LangTool;

@Entity
public class Liquid_t extends Model<Liquid_t> {
	@PrimaryKey (autoIncrement = true)
	public int id;
	public String name;
	
	// properties range 0-100 (%)
	public int sweet;
	public int sour;
	public int bitter;
	public int strenght;
	public int counter;
	public int type;		// to Type class
	
	@OneToMany (toType = Product.class, onField="liquid")
	public EntityList<Liquid_t, Product> products = new EntityList<Liquid_t, Product>(Liquid_t.class, Product.class, this);

	public List<Product> getProducts(){
		products.refreshList();
		return products;
	}

	public String getName() {
		return LangTool.translateName(id, "liquid", name );
	}
	@Override
	public String toString() {
		return getName();
	}

	public Type getLiquidType() {
		return BarobotData.getOneObject( Type.class, this.type );
	}

	@Override
	public void insert() {
		super.insert();
		BarobotData.reportChange( this.getClass(), this.id );
	}
	@Override
	public void update() {
		super.update();
		BarobotData.reportChange( this.getClass(), this.id );
	}
}
