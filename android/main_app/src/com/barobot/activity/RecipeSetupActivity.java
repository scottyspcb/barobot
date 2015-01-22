package com.barobot.activity;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.barobot.BarobotMain;
import com.barobot.R;
import com.barobot.common.Initiator;
import com.barobot.gui.database.BarobotData;
import com.barobot.gui.dataobjects.Engine;
import com.barobot.gui.dataobjects.Ingredient_t;
import com.barobot.gui.dataobjects.Liquid_t;
import com.barobot.gui.dataobjects.Recipe_t;
import com.barobot.gui.dataobjects.Type;
import com.barobot.other.LangTool;
import com.barobot.other.ProgressTask;
import com.barobot.other.ProgressTask.UiTask;

public class RecipeSetupActivity extends BarobotMain implements OnItemSelectedListener {

	private Recipe_t currentRecipe;
	private Type mCurrentType;
	private boolean wasChanged = false;
	private boolean stopListener = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recipe_setup);
		int recipe_id = 0;
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			recipe_id	= extras.getInt(RecipeListActivity.RECIPE_ID_PARAM, 0);
			if(recipe_id>0){
				currentRecipe = BarobotData.getOneObject(Recipe_t.class, recipe_id);
			}
		}
		UpdateRecipes(recipe_id);
		FillTypesList();

		ToggleButton show_hide = (ToggleButton) findViewById(R.id.show_hide);
		show_hide.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					Initiator.logger.i("ToggleButton.onCheckedChanged",""+(isChecked));
					if( !stopListener ){
						if(currentRecipe != null ){
							if (isChecked) {
								currentRecipe.unlisted = false;
							} else {
								currentRecipe.unlisted = true;
							}
							currentRecipe.update();
							wasChanged = true;
						}
					}
				}
			});
	}

	public void onAddRecipeButtonClicked (View view){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    LayoutInflater inflater		= getLayoutInflater();
	    final View dialogView 		= inflater.inflate(R.layout.dialog_add_recipe, null); 

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
					Recipe_t recipe = new Recipe_t();
					recipe.name = name;
					recipe.unlisted = false;
					recipe.insert();
					wasChanged = true;
					LangTool.InsertTranslation(recipe.id, "recipe", name);
					UpdateRecipes(recipe.id);
				}
			});
		AlertDialog ad = builder.create();
		ad.show();
	}

	public void changeName(View view){

	}
	public void changePhoto(View view){
		
	}

	// usun przepis
	public void removeRecipe(View view) {
		if(currentRecipe!=null){
	    	new AlertDialog.Builder(this).setTitle(R.string.are_you_sure_delete_drink).setMessage(currentRecipe.getName())
		    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) {
		        	Spinner spinner = (Spinner) findViewById(R.id.recipe_spinner);
					int pos = spinner.getSelectedItemPosition();
					currentRecipe.delete();
					currentRecipe = null;
					Engine.GetInstance().invalidateData();
					UpdateRecipes(0);
					FillTypesList();
					if( pos-1 > 0 && pos-1 < spinner.getCount() ){
						spinner.setSelection(pos-1, true);
					}
					wasChanged = true;
		        }
		    })
		    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) {}
		    }).setIcon(android.R.drawable.ic_dialog_alert).show();
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
	}
	public void UpdateRecipeDetails()
	{
		/*TextView descriptionText = (TextView) findViewById(R.id.recipe_description);
		descriptionText.setText(currentRecipe.getDescription());
		*/
		List<Ingredient_t> ing = currentRecipe.getIngredients();
		ArrayAdapter<Ingredient_t> mAdapter = new ArrayAdapter<Ingredient_t>(this, android.R.layout.simple_list_item_1, ing);
		ListView listView = (ListView) findViewById(R.id.recipe_ingridient_list);
		listView.setAdapter(mAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Ingredient_t ing = (Ingredient_t) parent.getItemAtPosition(position);
				ing.delete();
				wasChanged = true;
				currentRecipe.refreshList();
				UpdateRecipeDetails();
			}	
		});
		ToggleButton show_hide = (ToggleButton) findViewById(R.id.show_hide);
		show_hide.setChecked( !currentRecipe.unlisted );
		
	}

	public void UpdateRecipes(long selectedRecipeID)
	{
		Spinner spinner = (Spinner) findViewById(R.id.recipe_spinner);
		ArrayAdapter<Recipe_t> recipeAdapter = new ArrayAdapter<Recipe_t>(this, R.layout.spinner_layout);
		List<Recipe_t> recipes =  BarobotData.GetRecipes();
		recipeAdapter.addAll(recipes);
		spinner.setAdapter(recipeAdapter);
		spinner.setOnItemSelectedListener(this);
		
		if (selectedRecipeID != 0) {
			for(Recipe_t rec: recipes)
			{
				if (rec.id == selectedRecipeID ) {
					spinner.setSelection(recipes.indexOf(rec));
					break;
				}
			}	
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		currentRecipe = (Recipe_t) parent.getItemAtPosition(position);
		UpdateRecipeDetails();
		FillTypesList();
	}
	
	
	// TODO: Wyodrębnić do oddzielnego fragmentu
	public void FillTypesList()
	{
		List<Type> types = BarobotData.GetTypes();
		ArrayAdapter<Type> mAdapter = new ArrayAdapter<Type>(this, android.R.layout.simple_list_item_1, types);
		ListView listView = (ListView) findViewById(R.id.recipe_types_list);
		listView.setAdapter(mAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				
				Type type = (Type) parent.getItemAtPosition(position);
				SetCurrentType(type);
				FillLiquidList();
			}
		});	
	}

	public void FillLiquidList(){
		List<Liquid_t> liquids			= mCurrentType.getLiquids();
		ArrayAdapter<Liquid_t> mAdapter = new ArrayAdapter<Liquid_t>(this, android.R.layout.simple_list_item_1, liquids);
		ListView listView				= (ListView) findViewById(R.id.recipe_liquids_list);
		listView.setAdapter(mAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
				Liquid_t liquid		= (Liquid_t) parent.getItemAtPosition(position);
				Ingredient_t ing	= new Ingredient_t();
				ing.liquid			= liquid.id;
				ing.quantity		= 20;
		//		BarobotConnector barobot = Arduino.getInstance().barobot;
		//		int a				= barobot.getCapacity(8);
				wasChanged = true;
				currentRecipe.addIngredient(ing);
				currentRecipe.refreshList();
				UpdateRecipeDetails();
			}
		});
	}

	private void SetCurrentType(Type type){
		mCurrentType = type;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if(keyCode == KeyEvent.KEYCODE_BACK) {	    //Handle the back button
	    	gotoMainMenu(null);
	        return true;
	    }else {
	        return super.onKeyDown(keyCode, event);
	    }
	}
	public void gotoMainMenu(View view){
		UiTask tt = new UiTask() {
			@Override
			public void compute() {
				Engine engine = Engine.GetInstance();
				if(wasChanged){
					engine.invalidateRecipes();
					engine.getRecipes();
				}
			}
			@Override
			public void close() {
				runOnUiThread(new Runnable() {
					  public void run() {
						finish();
						overridePendingTransition(R.anim.push_down_in,R.anim.push_down_out);
					  }
				});
			}
		};
		ProgressTask dd = new ProgressTask( this, tt );
		dd.execute();
	}
}
