package com.barobot.activity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.barobot.BarobotMain;
import com.barobot.R;
import com.barobot.common.Initiator;
import com.barobot.gui.dataobjects.Engine;
import com.barobot.gui.dataobjects.Ingredient_t;
import com.barobot.gui.dataobjects.Recipe_t;
import com.barobot.gui.fragment.DrinkImageFragment;
import com.barobot.gui.fragment.IngredientListFragment;
import com.barobot.gui.fragment.RecipeAttributesFragment;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.hardware.devices.i2c.Upanel;
import com.barobot.parser.Queue;

public class RecipeListActivity extends BarobotMain{

	public static String MODE_NAME = "ActivityMode";
	private Recipe_t mCurrentRecipe;
	private int drink_size	= 0;

	private Mode mode;
	public enum Mode
	{
		Normal,
		Favorite,
		Random
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (getIntent().hasExtra("bundle") && savedInstanceState == null) {
			savedInstanceState = getIntent().getExtras().getBundle("bundle");
		}
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recipe);
		setFullScreen();
	}

	@Override
	protected void onResume() {
		super.onResume();
		int modeInt = getIntent().getIntExtra(MODE_NAME, 0);
		mode = Mode.values()[modeInt];	
		FillRecipeList();

		int position = 0;
		ListView listView = (ListView) findViewById(R.id.recipe_list);
		if (mode == Mode.Random) {
			position = (int) (Math.random()*listView.getAdapter().getCount());
		}

		listView.setSelection(position);
		listView.setItemChecked(position, true);

		try {
			mCurrentRecipe = (Recipe_t) listView.getItemAtPosition(position);
		} catch (IndexOutOfBoundsException e) {
			Initiator.logger.appendError(e);
		}
		FillRecipeDetails();
	}

	public void FillRecipeList() {
		mCurrentRecipe = null;
		List<Recipe_t> recipes;
		
	//	if (mode == Mode.Favorite){
	//		recipes = Engine.GetInstance().getFavoriteRecipes();
	//	}else{
			recipes = Engine.GetInstance().getRecipes();
	//	}

		ArrayAdapter<Recipe_t> mAdapter = new ArrayAdapter<Recipe_t>(this,
				R.layout.recipe_list_item_layout, recipes);
		ListView listView = (ListView) findViewById(R.id.recipe_list);
		listView.setAdapter(mAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				view.setSelected(true);
				mCurrentRecipe = (Recipe_t) parent.getItemAtPosition(position);
				FillRecipeDetails();
			}
		});
	}

	public void FillRecipeDetails() {
		IngredientListFragment ingredientFragment = (IngredientListFragment) getFragmentManager()
				.findFragmentById(R.id.fragment_ingredient_list);
		
		DrinkImageFragment imageFragment = (DrinkImageFragment) getFragmentManager()
				.findFragmentById(R.id.fragment_drink_image);
		
		RecipeAttributesFragment attributesFragment = (RecipeAttributesFragment) getFragmentManager()
				.findFragmentById(R.id.fragment_attributes);
		
		if (mCurrentRecipe == null) {
			setTextViewText("", R.id.recipe_name_textview);
			ingredientFragment.ClearIngredients();
			imageFragment.ClearImage();
			attributesFragment.ClearAttributes();
			
			drink_size = 0;
			TextView drinkSizeBox = (TextView) findViewById(R.id.drink_size);
			drinkSizeBox.setText("" + drink_size + "ml");
			drinkSizeBox.setVisibility(View.INVISIBLE);
		}
		else
		{
			// Set the name
			setTextViewText(mCurrentRecipe.getName(), R.id.recipe_name_textview);

			// Set image
			imageFragment.SetImage(mCurrentRecipe.photoId);

			List<Ingredient_t> a = mCurrentRecipe.getIngredients();
			final Map<Integer, Integer> usage = Engine.GetInstance().GenerateBottleUsage(a);
			drink_size	= Ingredient_t.getSize( a );

			// Set drink attributes
			attributesFragment.SetAttributes(mCurrentRecipe.GetSweet(), mCurrentRecipe.GetSour(), 
					mCurrentRecipe.GetBitter(), mCurrentRecipe.GetStrength());

			// Set ingredients
			ingredientFragment.ShowIngredients(mCurrentRecipe.getIngredients());

			// set drink size
			TextView drinkSizeBox = (TextView) findViewById(R.id.drink_size);
			if( drink_size > 0 ){
				drinkSizeBox.setText("" + drink_size + "ml");
				drinkSizeBox.setVisibility(View.VISIBLE);
			}else{
				drinkSizeBox.setVisibility(View.INVISIBLE);
			}

			// set leds
			Thread rr = new Thread(  new Runnable() {
				 public void run() {
					BarobotConnector barobot = Arduino.getInstance().barobot;
					barobot.turnOffLeds(barobot.main_queue);
					Queue q = barobot.main_queue;
					for(Entry<Integer, Integer> entry : usage.entrySet()) {
						    Integer key = entry.getKey();
						    Integer value = entry.getValue();
						    barobot.bottleBacklight(q, key-1, value);
					}
				 }
			}); 
			rr.start();
		}
	}

	public void onPourButtonClicked(View view) {
		if (mCurrentRecipe != null) {
			pourStart();
		}
	}

	public void pourStart() {
		final ProgressDialog progress = new ProgressDialog(this);
		progress.setTitle("Preparing drink...");
		progress.setMessage("Please wait");
		progress.show();

		Thread t = new Thread(new Runnable() {  
	         @Override
	         public void run() {
	        	  	Engine.GetInstance().Pour(mCurrentRecipe, "list");
	        	  	progress.dismiss();
	        	  	gotoMainMenu(null);
	         }});
		t.start();
	}
	public void gotoMainMenu(View view){
		this.finish();
		overridePendingTransition(R.anim.push_right_in,R.anim.push_right_out);
	}
}
