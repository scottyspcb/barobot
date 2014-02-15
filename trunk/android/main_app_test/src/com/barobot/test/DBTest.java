package com.barobot.test;

import java.util.List;

import org.orman.mapper.Model;
import org.orman.mapper.ModelQuery;

import com.barobot.gui.database.BarobotDB;
import com.barobot.gui.dataobjects.*;

import android.test.AndroidTestCase;

public class DBTest extends AndroidTestCase {
	
	protected static boolean setupDone = false;
	@Override
	protected void setUp() throws Exception 
	{
		if (!setupDone)
		{
			BarobotDB.StartOrmanMapping(this.getContext());
			setupDone = true;
		}
		
		
	}
	
	@Override
	protected void tearDown() throws Exception
	{
		BarobotDB.ClearTable(Product.class);
		BarobotDB.ClearTable(Liquid_t.class);
		BarobotDB.ClearTable(Type.class);
		BarobotDB.ClearTable(Category.class);
		
	}

	public void testLiquid() {
		BarobotDB target;
		
		target = new BarobotDB(this.mContext);
		target.DeleteLiquids();
		
		String type = "Typ";
		String name = "Nazwa";
		float voltage = 2;
		Liquid liq = new Liquid(type, name, voltage);
		
		target.InsertLiquid(liq);
		List<Liquid> liquids = target.getLiquids();
		
		assertEquals("Liquid table should contain only one element", liquids.size(), 1);
		
		Liquid newLiq = liquids.get(0);
		
		assertEquals(newLiq.name, name);
		assertEquals(newLiq.type, type);
		assertEquals(newLiq.voltage, voltage);
		
		target.DeleteLiquids();
		liquids = target.getLiquids();
		assertEquals("Liquid table should contain only one element", liquids.size(), 0);
	}
	
	public void testCategory()
	{			
		Category cat = new Category();
		cat.name = "CategoryName1";
		cat.genre = "Juice";
		
		cat.insert();
		
		List<Category> categories = Model.fetchAll(Category.class);
		
		assertEquals(1, categories.size());
	}
	
	public void testType()
	{
		Category cat = new Category();
		cat.name = "CategoryName1";
		cat.genre = "Juice";
		cat.insert();
		
		Type t1 = new Type();
		t1.name = "name1";
		t1.category = cat;
		t1.insert();
		
		Type t2 = new Type();
		t2.name = "name2";
		t2.category = cat;
		t2.insert();
		
		
		Category cat2 = new Category();
		cat2.name = "CategoryName2";
		cat2.genre = "Soda";
		cat2.insert();
		
		Type t3 = new Type();
		t3.name = "name3";
		t3.insert();
		
		cat2.types.add(t3);
		
		List<Category> cats = Model.fetchAll(Category.class);
		
		assertEquals(2, cats.size());
		
		Category newCat1 = cats.get(0);
		Category newCat2 = cats.get(1);
		
		assertEquals(2, newCat1.types.size());
		assertEquals(1, newCat2.types.size());
	}

	
	public void testLiquidNew ()
	{
		Category cat = getCategory("");
		cat.insert();
		
		Type t1 = getType("1");
		t1.category = cat;
		t1.insert();
		
		Type t2 = getType("2");
		t2.category = cat;
		t2.insert();
		
		Liquid_t liq1 = getLiquid("1");
		liq1.type = t1;
		liq1.insert();
		
		Liquid_t liq2 = getLiquid("2");
		liq2.type = t1;
		liq2.insert();
		
		Liquid_t liq3 = getLiquid("3");
		liq3.type = t2;
		liq3.insert();
		
		assertEquals(3, Model.fetchAll(Liquid_t.class).size());
		
		List<Type> types = Model.fetchAll(Type.class);
		
		assertEquals(2, types.size());
		
		Type newt1 = types.get(0);
		Type newt2 = types.get(1);
		
		assertEquals(2, newt1.liquids.size());
		assertEquals(1, newt2.liquids.size());
	}
	
	public void testProduct()
	{
		Category cat = getCategory("");
		cat.insert();
		
		Type t = getType("");
		t.category = cat;
		t.insert();
		
		Liquid_t liq1 = getLiquid("1");
		liq1.type = t;
		liq1.insert();
		
		Liquid_t liq2 = getLiquid("2");
		liq2.type = t;
		liq2.insert();
		
		Product prod1 = getProduct("1");
		prod1.liquid = liq1;
		prod1.insert();
		
		Product prod2 = getProduct("2");
		prod2.liquid = liq2;
		prod2.insert();
		
		List<Product> productList = Model.fetchAll(Product.class);
		assertEquals(2, productList.size());
		
		
		assertEquals(liq1.id, productList.get(0).liquid.id);;
		assertEquals(liq2.id, productList.get(1).liquid.id);;
	}
	
	// Helper classes
	private Category getCategory(String suffix)
	{
		Category cat = new Category();
		cat.name = "CatName" + suffix;
		
		return cat;
	
	}
	
	private Type getType(String suffix)
	{
		Type type = new Type();
		type.name = "TypeName" + suffix;
		
		return type;
	}
	
	private Liquid_t getLiquid(String suffix)
	{
		Liquid_t liq = new Liquid_t();
		liq.name = "LiquidName" + suffix;
		liq.voltage = 2;
		
		return liq;
	}
	
	private Product getProduct(String suffix)
	{
		Product prod = new Product();
		prod.capacity = 500;
		prod.initNeeded = true;
		
		return prod;
	}
}
