package com.barobot.gui.utils;

import java.util.ArrayList;
import java.util.List;

import com.barobot.gui.dataobjects.Product;

public class CapacityProductWrapper
{
	private Product mProduct;
	public CapacityProductWrapper(Product prod)
	{
		mProduct = prod;
	}
	
	public Product getProduct()
	{
		return mProduct;
	}
	
	public static List<CapacityProductWrapper> WrapList(List<Product> products)
	{
		List<CapacityProductWrapper> result =  new ArrayList<CapacityProductWrapper>();
		for(Product prod : products)
		{
			result.add(new CapacityProductWrapper(prod));
		}
		
		return result;
	}
	
	@Override
	public String toString() {
		return String.valueOf(mProduct.capacity) + "ml";
	}
}
