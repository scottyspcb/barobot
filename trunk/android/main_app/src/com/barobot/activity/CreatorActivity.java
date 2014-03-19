package com.barobot.activity;

import java.util.ArrayList;
import java.util.List;

import com.barobot.R;
import com.barobot.gui.ArduinoListener;
import com.barobot.gui.dataobjects.Engine;
import com.barobot.gui.dataobjects.Ingredient_t;
import com.barobot.gui.dataobjects.Liquid_t;
import com.barobot.gui.dataobjects.Recipe_t;
import com.barobot.gui.dataobjects.Slot;
import android.os.Bundle;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class CreatorActivity extends BarobotActivity implements ArduinoListener{

	private int[] ids;
	private List<Ingredient_t> ingredients;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_creator);
		
		ingredients = new ArrayList<Ingredient_t>();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		UpdateData();
	}
	
	private void UpdateData(){
		ids = new int[13];
		ids[1] = R.id.bottle_button1;
		ids[2] = R.id.bottle_button2;
		ids[3] = R.id.bottle_button3;
		ids[4] = R.id.bottle_button4;
		ids[5] = R.id.bottle_button5;
		ids[6] = R.id.bottle_button6;
		ids[7] = R.id.bottle_button7;
		ids[8] = R.id.bottle_button8;
		ids[9] = R.id.bottle_button9;
		ids[10] = R.id.bottle_button10;
		ids[11] = R.id.bottle_button11;
		ids[12] = R.id.bottle_button12;
		UpdateSlots();	
	}
	
	private void UpdateSlots() {
		
		Engine engine = Engine.GetInstance(this);
		List<Slot> bottles = engine.getSlots();

		Log.w("BOTTLE_SETUP length",""+bottles.size());
		
		for(Slot bottle : bottles)
		{
			if (bottle.position > 0 && bottle.position <= ids.length )
			{
				TextView tview = (TextView) findViewById(ids[bottle.position]);
				if (bottle.status == Slot.STATUS_EMPTY) {
					tview.setText(R.string.empty_bottle_string);
				} else {
					tview.setText(bottle.GetName());	
				}	
			}
		}
	}
	
	public void ShowIngredients()
	{
		ArrayAdapter<Ingredient_t> mAdapter = new ArrayAdapter<Ingredient_t>(this, R.layout.ingredient_list_item, ingredients);
		ListView listView = (ListView) findViewById(R.id.ingredient_list);
		listView.setAdapter(mAdapter);
	}
	
	public void onBottleClicked(View view)
	{
		int viewID = view.getId();
		int position = 0;
		for (int i = 1; i<=12 ; i++)
		{
			if (viewID == ids[i])
			{
				position = i;
				break;
			}
		}
		if (position != 0)
		{
			Slot slot = Engine.GetInstance(this).getSlot(position);
			
			if (slot.product != null)
			{
				Ingredient_t ingredient = new Ingredient_t();

				ingredient.liquid = slot.product.liquid;
				ingredient.quantity = 20;
				addIngredient(ingredient);
			}
			ShowIngredients();
		}
		else
		{
			Log.w("BOTTLE_SETUP", "onBottleClicked called by an unknown view");
		}
	}
	
	public void onClearButtonClicked(View view)
	{
		ingredients.clear();
		ShowIngredients();
	}
	
	public void onAddRecipeButtonClicked (View view)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

	    LayoutInflater inflater = getLayoutInflater();
	    final View dialogView = inflater.inflate(R.layout.dialog_add_recipe, null);
	    
	    builder.setView(dialogView)
		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		})
		.setPositiveButton("Add", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				TextView nameView = (TextView) dialogView.findViewById(R.id.recipe_name);
				String name = nameView.getText().toString();
				
				CreateDrink(name);	
			}
		});
	    AlertDialog ad = builder.create();
	    ad.show();
	}
	
	public void onPourRecipeButtonClicked (View view)
	{
		Recipe_t tempRecipe = CreateDrink("Unnamed Drink", true);
		Engine.GetInstance(this).Pour(tempRecipe, this);
	}
	
	public void addIngredient(Ingredient_t ing)
	{
		Ingredient_t existing = findIngredient(ing.liquid);
		if (existing == null)
		{
			ingredients.add(ing);
		}
		else
		{
			existing.quantity += ing.quantity;
		}
	}
	
	Ingredient_t findIngredient(Liquid_t liquid)
	{
		for(Ingredient_t ing : ingredients)
		{
			if (ing.liquid.id == liquid.id)
			{
				return ing;
			}
		}
		return null;
	}
	
	public Recipe_t CreateDrink(String name)
	{
		return CreateDrink(name, false);
	}
	
	public Recipe_t CreateDrink(String name, Boolean unlisted)
	{
		Recipe_t recipe = new Recipe_t();
		recipe.name = name;
		recipe.unlisted = unlisted;
		Engine.GetInstance(this).addRecipe(recipe, ingredients);
		
		return recipe;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}
	
	public void FillIngredientList()
	{
		List<String> list = new ArrayList<String>();
		list.add("Test");
		list.add("Test");
		list.add("Test");
		list.add("Test");
		list.add("Test");
		list.add("Test");
		
		ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(this, R.layout.ingredient_list_item, list);
		ListView listView = (ListView) findViewById(R.id.ingredient_list);
		listView.setAdapter(mAdapter);
		
	}

	@Override
	public void onQueueFinished() {
		// TODO Auto-generated method stub
		new AlertDialog.Builder(this)
	    .setTitle("Success!!!")
	    .setMessage("Finished pouring")
	    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	            // continue with delete
	        }
	     })
	    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	            // do nothing
	        }
	     })
	     .show();
	}

}
