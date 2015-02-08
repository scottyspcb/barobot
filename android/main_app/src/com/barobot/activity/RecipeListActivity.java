package com.barobot.activity;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.barobot.BarobotMain;
import com.barobot.R;
import com.barobot.android.InternetHelpers;
import com.barobot.common.Initiator;
import com.barobot.common.constant.Constant;
import com.barobot.common.interfaces.OnDownloadReadyRunnable;
import com.barobot.gui.database.BarobotData;
import com.barobot.gui.dataobjects.Engine;
import com.barobot.gui.dataobjects.Ingredient_t;
import com.barobot.gui.dataobjects.Recipe_t;
import com.barobot.gui.fragment.DrinkImageFragment;
import com.barobot.gui.fragment.IngredientListFragment;
import com.barobot.gui.fragment.RecipeAttributesFragment;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.parser.Queue;

public class RecipeListActivity extends BarobotMain{

	public static final String RECIPE_ID_PARAM = "RECIPE_ID_PARAM";
	public static String MODE_NAME = "ActivityMode";
	private Recipe_t mCurrentRecipe;
	private int drink_size	= 0;

	//private Mode mode;
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
	//	int modeInt = getIntent().getIntExtra(MODE_NAME, 0);
	//	mode = Mode.values()[modeInt];	
		FillRecipeList();

		ListView listView = (ListView) findViewById(R.id.recipe_list);
		int position = (int) (Math.random()*listView.getAdapter().getCount());

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
		List<Recipe_t> recipes = Engine.GetInstance().getRecipes();

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
			
			attributesFragment.SetTaste(mCurrentRecipe.getTaste());

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
					Queue q = barobot.main_queue;
					barobot.lightManager.turnOffLeds(q);
					int dd = barobot.state.getInt( "DOING_DRINK", 0 );
					if( dd == 0 ){
						for(Entry<Integer, Integer> entry : usage.entrySet()) {
							    Integer key = entry.getKey();
							    Integer value = entry.getValue();
							    barobot.lightManager.bottleBacklight(q, key-1, value);
						}
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
		String title	= getResources().getString(R.string.preparing_drink_title);
		String msg		= getResources().getString(R.string.preparing_drink_message);
		progress.setTitle(title);
		progress.setMessage(msg);
		progress.show();

		final Queue q_ready		= new Queue();	
	 	final BarobotConnector barobot = Arduino.getInstance().barobot;

		barobot.lightManager.carret_color( q_ready, 0, 255, 0 );
		q_ready.addWait(200);
		barobot.lightManager.carret_color( q_ready, 0, 100, 0 );
	  	
		final Queue q_error		= new Queue();	
		barobot.lightManager.carret_color( q_error, 255, 0, 0 );

		Thread t = new Thread(new Runnable() {  
	         @Override
	         public void run() {
	     			final Queue q_drink = Engine.GetInstance().Pour(mCurrentRecipe, "list");
	     			q_ready.add(q_drink);
					boolean igrq		= barobot.weight.isGlassRequired();
					boolean igrd		= barobot.weight.isGlassReady();
					if(!igrq){
						Initiator.logger.i( "pourStart", "dont need glass");
						barobot.main_queue.add(q_drink);
						gotoMainMenu(null);
					}else if(igrd){
						Initiator.logger.i( "pourStart", "is Glass Ready");
						barobot.main_queue.add(q_drink);
						gotoMainMenu(null);
					}else{
						Queue q = new Queue();
						barobot.weight.waitForGlass( q, q_ready, q_error);
						remainderAndDo(q);
					}
	        	  	progress.dismiss();
	         }});
		t.start();
	}
	protected void remainderAndDo(final Queue q) {
		final BarobotConnector barobot = Arduino.getInstance().barobot;

		runOnUiThread(new Runnable() {
			  public void run() {
				  new AlertDialog.Builder(RecipeListActivity.this)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle("")
					.setMessage(R.string.glass_reminder_message)
					.setPositiveButton(R.string.preparing_drink_start,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,int which) {
									Initiator.logger.i( "pourStart", "wait for Glass");
									barobot.main_queue.add(q);
									gotoMainMenu(null);
								}
					}).setNegativeButton(R.string.preparing_drink_cancel, null).show();	
			  }
		});
	}
	public void gotoMainMenu(View v){
		this.finish();
		overridePendingTransition(R.anim.push_right_in,R.anim.push_right_out);
	}
	int configKey = KeyEvent.KEYCODE_VOLUME_DOWN;
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ( keyCode == configKey) {
			Initiator.logger.i("onKeyDown","KEYCODE_VOLUME"); 
			showOptions();
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}

	private void showOptions() {
		if(mCurrentRecipe == null){
			return;
		}
		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		ab.setTitle(R.string.dialog_recipe_options_title);
		ab.setItems(R.array.recipe_option, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface d, int choice) {
				int length =getResources().getStringArray(R.array.recipe_option).length;
				if( choice == length - 1){
					d.dismiss();
				}else{
					if (choice == 0) {				// Edit recipe
						Intent intent = new Intent(RecipeListActivity.this, RecipeSetupActivity.class);
			    		intent.putExtra(RecipeListActivity.RECIPE_ID_PARAM, mCurrentRecipe.id);
			    		startActivity(intent);
					} else if (choice == 1) {		// Hide recipe
						mCurrentRecipe.unlisted = true;
						mCurrentRecipe.update();
						RecipeListActivity.this.finish();
						Engine.GetInstance().invalidateRecipes().getRecipes();
					} else if (choice == 2) {		// Delete recipe
						BarobotData.deleteRecipe( RecipeListActivity.this, mCurrentRecipe, true );
					}
				}
			}
		});
		ab.show();
	}

}
