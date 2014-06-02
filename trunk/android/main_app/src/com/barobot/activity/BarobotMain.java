package com.barobot.activity;

import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.barobot.AppInvoker;
import com.barobot.R;
import com.barobot.common.Initiator;
import com.barobot.gui.ArduinoListener;
import com.barobot.gui.dataobjects.Engine;
import com.barobot.gui.dataobjects.Ingredient_t;
import com.barobot.gui.dataobjects.Recipe_t;
import com.barobot.gui.fragment.DrinkImageFragment;
import com.barobot.gui.fragment.IngredientListFragment;
import com.barobot.gui.fragment.MenuFragment;
import com.barobot.gui.fragment.RecipeAttributesFragment;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.hardware.devices.i2c.Upanel;
import com.barobot.parser.Queue;

public class BarobotMain extends BarobotActivity implements ArduinoListener {
	
	public static String MODE_NAME = "ActivityMode";
	
	private Mode mode;
	public enum Mode
	{
		Normal,
		Favorite,
		Random
	}

	@Override
	protected void onDestroy() {
		AppInvoker.getInstance().onDestroy();
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		AppInvoker.getInstance().onPause();
		super.onPause();
	}

	private static BarobotMain instance;
	private Recipe_t mCurrentRecipe;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		instance = this; // Set up the window layout
		if (getIntent().hasExtra("bundle") && savedInstanceState == null) {
			savedInstanceState = getIntent().getExtras().getBundle("bundle");
		}
		AppInvoker.createInstance(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.barobot_main);
		AppInvoker.getInstance().onCreate();
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
	}

	@Override
	protected void onResume() {
		int modeInt = getIntent().getIntExtra(MODE_NAME, 0);
		mode = Mode.values()[modeInt];
		
		SetBreadcrumb();
		AppInvoker.getInstance().onResume();
		
		FillRecipeList();
		
		int position = 0;
		ListView listView = (ListView) findViewById(R.id.recipe_list);
		if (mode == Mode.Random) {
			position = (int) (Math.random()*listView.getAdapter().getCount());
		}
		
		listView.setSelection(position);
		listView.setItemChecked(position, true);

		Initiator.logger.i("BarobotMain.onResume", "" + position);

		try {
			mCurrentRecipe = (Recipe_t) listView.getItemAtPosition(position);
		} catch (IndexOutOfBoundsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		FillRecipeDetails();

		super.onResume();
	}
	
	public void SetBreadcrumb()
	{
		MenuFragment menuFrag = (MenuFragment) getFragmentManager().findFragmentById(R.id.fragment_menu);
		switch(mode){
	/*	case Favorite:
			menuFrag.SetBreadcrumb(MenuFragment.MenuItem.Favorite);
			break;*/
		case Normal:
			menuFrag.SetBreadcrumb(MenuFragment.MenuItem.Choose);
			break;
		case Random:
			menuFrag.SetBreadcrumb(MenuFragment.MenuItem.Lucky);
			break;
		default:
			menuFrag.SetBreadcrumb(MenuFragment.MenuItem.Choose);
			break;
		}
	}

	public void FillRecipeList() {
		mCurrentRecipe = null;
		List<Recipe_t> recipes;
		
		if (mode == Mode.Favorite){
			recipes = Engine.GetInstance(this).getFavoriteRecipes();
		}else{
			recipes = Engine.GetInstance(this).getRecipes();
		}

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
		}
		else
		{
			// Set the name
			setTextViewText(mCurrentRecipe.name, R.id.recipe_name_textview);

			// Set ingredients
			
			ingredientFragment.ShowIngredients(mCurrentRecipe.getIngredients());
			
			// Set image
			
			imageFragment.SetImage(mCurrentRecipe.photoID);
			
			// Set drink attributes
			
			attributesFragment.SetAttributes(mCurrentRecipe.GetSweet(), mCurrentRecipe.GetSour(), 
					mCurrentRecipe.GetBitter(), mCurrentRecipe.GetStrength());

			 Thread rr = new Thread(  new Runnable() { 
				 public void run() {
						BarobotConnector barobot = Arduino.getInstance().barobot;
						barobot.setLedsOff("ff"); 
						 List<Ingredient_t> a = mCurrentRecipe.getIngredients();
						 List<Integer> bottleSequence= Engine.GetInstance(BarobotMain.this).GenerateSequence(a); 
						 if(bottleSequence != null){ 
							 Queue q = Arduino.getMainQ();
							 for (Integer i : bottleSequence){ 
								 Upanel u =barobot.i2c.getUpanelByBottle(i-1); 
								 if(u!=null){
									 u.setLed(q, "22", 255);
								 }
							 } }
						 }
			}); 
			rr.start();
		}
	}

	public void onPourButtonClicked(View view) {
		if (mCurrentRecipe != null) {
			final Button xb2 = (Button) this.findViewById(R.id.choose_pour_button);
			xb2.setEnabled(false);
			Thread t = new Thread(new Runnable() {  
	             @Override
	             public void run() {
	            	Engine.GetInstance(BarobotMain.this).Pour(mCurrentRecipe, BarobotMain.this);
	         		runOnUiThread(new Runnable() {
	        			@Override
	        			public void run() {
	        				xb2.setEnabled(true);
	        			}
	        		});

	             }});
			t.start();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_HOME) {
			Log.i("onKeyDown", "KEYCODE_HOME");
			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Log.i("onKeyDown", "KEYCODE_BACK");
			BarobotConnector barobot = Arduino.getInstance().barobot;
			barobot.main_queue.unlock();
			// finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public static BarobotMain getInstance() {
		return instance;
	}

 	@Override
	public void onBackPressed() {
		new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle("Koniec?")
				.setMessage(
						"Czy na pewno zamknąć aplikację i przerwać pracę robota?")
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								finish();
							}
						}).setNegativeButton("No", null).show();
	}

	public void showError() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onQueueFinished() {
		// TODO Auto-generated method stub
	}

}
