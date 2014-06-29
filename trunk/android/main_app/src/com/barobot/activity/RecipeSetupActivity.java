package com.barobot.activity;

import java.util.List;

import org.orman.mapper.Model;
import org.orman.mapper.ModelQuery;
import org.orman.sql.C;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.barobot.BarobotMain;
import com.barobot.R;
import com.barobot.gui.dataobjects.Engine;
import com.barobot.gui.dataobjects.Ingredient_t;
import com.barobot.gui.dataobjects.Liquid_t;
import com.barobot.gui.dataobjects.Recipe_t;
import com.barobot.gui.dataobjects.Type;
import com.barobot.gui.utils.LangTool;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;

public class RecipeSetupActivity extends BarobotMain implements OnItemSelectedListener {

	private Type mCurrentType;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recipe_setup);
		UpdateRecipes();
		FillTypesList();
	}
	
	private Recipe_t currentRecipe;

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		currentRecipe = (Recipe_t) parent.getItemAtPosition(position);
		UpdateRecipeDetails();
		FillTypesList();
	}

	public void onAddRecipeButtonClicked (View view)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		System.out.println("onAddRecipeButtonClicked");
		
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
					Recipe_t recipe = new Recipe_t();
					recipe.name = name;
					recipe.unlisted = false;
					System.out.println("onAddRecipeButtonClicked: insert" );
					recipe.insert();
					
					LangTool.InsertTranslation(recipe.id, "recipe", name);
					
					UpdateRecipes(recipe.id);
				}
			});
		AlertDialog ad = builder.create();
		ad.show();
	}

	// usun przepis
	public void removeRecipe(View view) {
		Recipe_t recipe = Model.fetchSingle(ModelQuery.select().from(Recipe_t.class).where(C.eq("id", currentRecipe.id)).limit(1).getQuery(),Recipe_t.class);
		if(recipe!=null){
			Spinner spinner = (Spinner) findViewById(R.id.recipe_spinner);
			int pos = spinner.getSelectedItemPosition();
			recipe.delete();
			Engine.GetInstance(view.getContext()).invalidateData();
			UpdateRecipes();
			FillTypesList();
			if( pos-1 > 0 && pos-1 < spinner.getCount() ){
				spinner.setSelection(pos-1, true);
			}
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
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
	
				Ingredient_t ing = (Ingredient_t) parent.getItemAtPosition(position);
				Engine.GetInstance(RecipeSetupActivity.this).removeIngredient(ing);
				currentRecipe.ingredients.refreshList();
				UpdateRecipeDetails();
			}	
		});
	}
	
	public void UpdateRecipes() {
		UpdateRecipes(0);
	}
	
	public void UpdateRecipes(long selectedRecipeID)
	{
		Spinner spinner = (Spinner) findViewById(R.id.recipe_spinner);
		ArrayAdapter<Recipe_t> recipeAdapter = new ArrayAdapter<Recipe_t>(this, R.layout.spinner_layout);
		
		List<Recipe_t> recipes =  Engine.GetInstance(this).getAllRecipes();
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
	
	
	


	// TODO: Wyodrębnić do oddzielnego fragmentu
	
	public void FillTypesList()
	{
		Engine engine = Engine.GetInstance(this);
		
		List<Type> types = engine.getTypes();

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
	
	public void FillLiquidList()
	{
		List<Liquid_t> liquids = mCurrentType.getLiquids();
		
		ArrayAdapter<Liquid_t> mAdapter = new ArrayAdapter<Liquid_t>(this, android.R.layout.simple_list_item_1, liquids);
		ListView listView = (ListView) findViewById(R.id.recipe_liquids_list);
		listView.setAdapter(mAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {

				Liquid_t liquid = (Liquid_t) parent.getItemAtPosition(position);
				Ingredient_t ing = new Ingredient_t();
				ing.liquid = liquid;
				ing.quantity = 20;

				BarobotConnector barobot = Arduino.getInstance().barobot;
				int a = barobot.getCapacity(8);
				currentRecipe.addIngredient(ing);
				UpdateRecipeDetails();
			}
		});
	}
	
	private void SetCurrentType(Type type){
		mCurrentType = type;
	}

}
