package com.barobot.gui;

import java.util.List;
import java.util.ArrayList;

import com.barobot.gui.dataobjects.Engine;
import com.barobot.gui.dataobjects.Ingredient;
import com.barobot.gui.dataobjects.Liquid;
import com.barobot.R;
import com.barobot.gui.dataobjects.Recipe;

import android.os.Bundle;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

public class RecipeSetupActivity extends BarobotActivity 
									implements OnItemSelectedListener,
										NoticeDialogListener {

	private Recipe currentRecipe;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recipe_setup);
		
		
		Spinner spinner = (Spinner) findViewById(R.id.recipe_spinner);
		
		ArrayAdapter<Recipe> recipeAdapter = new ArrayAdapter<Recipe>(this, R.layout.spinner_layout);
		Engine engine = Engine.GetInstance(this);
		recipeAdapter.addAll(engine.getRecipes());
		
		spinner.setAdapter(recipeAdapter);
		spinner.setOnItemSelectedListener(this);

	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		currentRecipe = (Recipe) parent.getItemAtPosition(position);
		SetupRecipeDetails();
	}
	
	public void addRecipe(View view)
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
					
					TextView descriptionView = (TextView) dialogView.findViewById(R.id.recipe_description);
					String description = descriptionView.getText().toString();
					
					Recipe recipe = new Recipe(0, name, description, new ArrayList<Ingredient>());
					
					Engine engine = Engine.GetInstance(RecipeSetupActivity.this);
					long recipeId = engine.AddRecipe(recipe);
					
					UpdateRecipes(recipeId);
					
					
				}
			});
		AlertDialog ad = builder.create();
		ad.show();
	}
	
	public void removeRecipe(View view) {
		Engine.GetInstance(this).RemoveRecipe(currentRecipe.getId());
		UpdateRecipes();
	}


	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}
	
	public void SetupRecipeDetails()
	{
		TextView descriptionText = (TextView) findViewById(R.id.recipe_description);
		descriptionText.setText(currentRecipe.getDescription());


		List<Ingredient> ing = currentRecipe.getIngridients();

		ArrayAdapter<Ingredient> mAdapter = new ArrayAdapter<Ingredient>(this, android.R.layout.simple_list_item_1, ing);
		ListView listView = (ListView) findViewById(R.id.recipe_ingridient_list);
		listView.setAdapter(mAdapter);
	}
	
	public void UpdateRecipes() {
		UpdateRecipes(0);
	}
	
	public void UpdateRecipes(long selectedRecipeID)
	{
		Spinner spinner = (Spinner) findViewById(R.id.recipe_spinner);
		
		ArrayAdapter<Recipe> recipeAdapter = new ArrayAdapter<Recipe>(this, R.layout.spinner_layout);
		Engine engine = Engine.GetInstance(this);
		
		ArrayList<Recipe> recipes = (ArrayList<Recipe>) engine.getRecipes();
		recipeAdapter.addAll(engine.getRecipes());
		
		spinner.setAdapter(recipeAdapter);
		spinner.setOnItemSelectedListener(this);
		
		if (selectedRecipeID != 0) {
			for(Recipe rec: recipes)
			{
				if (rec.getId() == selectedRecipeID ) {
					spinner.setSelection(recipes.indexOf(rec));
					break;
				}
			}	
		}
	}
	
	public void addIngredient(View view)
	{		    
		FragmentTransaction ft = getFragmentManager().beginTransaction();

		// Create and show the dialog.
		SelectLiquidDialogFragment newFragment = SelectLiquidDialogFragment.newInstance();
		newFragment.ShowEmptyButton = false;
		newFragment.ShowAddButton = false;
		newFragment.ShowVolumeReel = true;
		newFragment.show(ft, "dialog");
	}
	public void removeIngredients(View view)
	{
		Engine.GetInstance(this).RemoveIngredients(currentRecipe.getId());
		UpdateRecipes(currentRecipe.getId());
	}

	@Override
	public void onDialogEnd(DialogFragment dialog, ReturnStatus status,
			Liquid liquid, int volume) {
		switch(status)
		{
		case OK:
			Engine.GetInstance(this).addIngredient(currentRecipe.getId(), new Ingredient(liquid, volume));
			UpdateRecipes(currentRecipe.getId());
			break;
		case Canceled:
			break;
		case NewLiquid:
			break;
		}
		
	}

}
