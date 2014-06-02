package com.barobot.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.barobot.R;
import com.barobot.gui.ArduinoListener;
import com.barobot.gui.dataobjects.Engine;
import com.barobot.gui.dataobjects.Ingredient_t;
import com.barobot.gui.dataobjects.Liquid_t;
import com.barobot.gui.dataobjects.Recipe_t;
import com.barobot.gui.dataobjects.Slot;
import com.barobot.gui.fragment.IngredientListFragment;
import com.barobot.gui.fragment.MenuFragment;
import com.barobot.gui.fragment.RecipeAttributesFragment;
import com.barobot.gui.utils.Distillery;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.hardware.devices.i2c.Upanel;
import com.barobot.parser.Queue;

public class CreatorActivity extends BarobotActivity implements ArduinoListener{
	private int[] slot_nums = {0,0,0,0,0,0,0,0,0,0,0,0,0};
	private int[] ids;
	private int[] drops;
	private int[] dropIds;
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
		
		MenuFragment menuFrag = (MenuFragment) getFragmentManager().findFragmentById(R.id.fragment_menu);
		menuFrag.SetBreadcrumb(MenuFragment.MenuItem.Create);
		
		UpdateData();
	}

	private void UpdateData(){
		SetupBottles();
		SetupDrops();
		UpdateSlots();	
	}
	
	private void SetupBottles()
	{
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
	}
	
	private void SetupDrops()
	{
		drops = new int[13];
		for(int idx=1; idx <= 12 ; idx++)
		{
			drops[idx] = 0;
		}
		
		dropIds = new int[6];
		dropIds[0] = 0;
		dropIds[1] = R.drawable.drop_1;
		dropIds[2] = R.drawable.drop_2;
		dropIds[3] = R.drawable.drop_3;
		dropIds[4] = R.drawable.drop_4;
		dropIds[5] = R.drawable.drop_5;
	}

	private void UpdateSlots() {

		List<Slot> bottles = Engine.GetInstance(this).getSlots();

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
		BarobotConnector barobot = Arduino.getInstance().barobot;
		barobot.setLedsOff("ff");
	}
	
	public void ShowIngredients()
	{
		BarobotConnector barobot = Arduino.getInstance().barobot;
		IngredientListFragment frag = (IngredientListFragment) getFragmentManager().findFragmentById(R.id.fragment_ingredient_list);
		frag.ShowIngredients(ingredients);
		
		RecipeAttributesFragment attrFrag = (RecipeAttributesFragment) getFragmentManager().findFragmentById(R.id.fragment_attributes);
		attrFrag.SetAttributes(Distillery.getSweet(ingredients), Distillery.getSour(ingredients)
				, Distillery.getBitter(ingredients), Distillery.getStrength(ingredients));

		for (int i = 1; i<=12 ; i++){											// 1 - 12
			barobot.bottleBacklight( i-1, slot_nums[i] );		// 0 -11
		}
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
				BarobotConnector barobot = Arduino.getInstance().barobot;
				ingredient.quantity = barobot.getCapacity( slot.position - 1);
				addIngredient(position, ingredient);
			}
			ShowIngredients();
			CalculateDrops();
		}else{
			Log.w("BOTTLE_SETUP", "onBottleClicked called by an unknown view");
		}
	}
	
	public void onClearButtonClicked(View view)
	{
		clear();
	}
	
	
	private void clear(){
		ingredients.clear();
		for (int i = 1; i<=12 ; i++){
			slot_nums[i] = 0;
		}
		BarobotConnector barobot = Arduino.getInstance().barobot;
		barobot.setLedsOff("ff");
		ShowIngredients();
		runOnUiThread(new Runnable() {  
             @Override
             public void run() {
            	 CalculateDrops();
             }});
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

	public void onPourRecipeButtonClicked (View view){
		final Recipe_t tempRecipe = CreateDrink("Unnamed Drink", true);
		final Button xb2 = (Button) this.findViewById(R.id.creator_pour_button);
		xb2.setEnabled(false);

		Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
            	Engine.GetInstance(CreatorActivity.this).Pour(tempRecipe, CreatorActivity.this);
            	clear();
    			runOnUiThread(new Runnable() {
        			@Override
        			public void run() {
        				xb2.setEnabled(true);
        			}
        		});
            }});
		t.start();
	}
	
	private void addIngredient(int position, Ingredient_t ing)
	{
		slot_nums[position]++;
		Ingredient_t existing = findIngredient(ing.liquid);
		if (existing == null){
			ingredients.add(ing);
		}else{
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

	public void CalculateDrops()
	{
		SetupDrops();
		List<Integer> sequence = Engine.GetInstance(this).GenerateSequence(ingredients);

		for(Integer num : sequence) {
			drops[num]++;
		}

		for (int idx=1; idx <= 12 ; idx ++)
		{
			TextView tview = (TextView) findViewById(ids[idx]);
			int counter = drops[idx];
			if (counter > 5) counter = 5;
			
			for(Drawable myOldDrawable : tview.getCompoundDrawables())
			{
				if (myOldDrawable != null) {
					myOldDrawable.setCallback(null);
				}
			}
			tview.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, dropIds[counter]);
		}
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
		recipe.insert();
		Engine ee = Engine.GetInstance(this);
		ee.addRecipe(recipe, ingredients);
		ee.invalidateData();
		return recipe;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public void onQueueFinished() {
		clear();
		new AlertDialog.Builder(this)
	    .setTitle("Success!")
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
