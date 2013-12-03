package com.barobot.gui;

import java.util.List;

import com.barobot.gui.dataobjects.Engine;
import com.barobot.gui.dataobjects.Ingredient;
import com.barobot.R;
import com.barobot.gui.dataobjects.Recipe;




import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends BarobotActivity {
	public final static String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
	private RecipeAdapter rAdapter;
	private List<Recipe> mRecipies;
	private Button mMakeButton;
	private Engine mEngine;
	private int currentRecipe; 
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mEngine = Engine.GetInstance(this);
		currentRecipe = -1;
		
		//mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, stringList);
		mRecipies = mEngine.getRecipes();
		rAdapter = new RecipeAdapter(this, R.layout.recipetile, mRecipies);
		
		GridView gridView = (GridView) findViewById(R.id.gridview_recipe);
		gridView.setAdapter(rAdapter);
		gridView.setOnItemClickListener(mMessageClickedHandler);
		
		mMakeButton = (Button) findViewById(R.id.make_button);
		mMakeButton.setVisibility(View.INVISIBLE);
		
	}
	
	public void make(View view)
	{
		// make a drink
		if (currentRecipe != -1)
		{
			mEngine.Prepare(mRecipies.get(currentRecipe));
			
			TextView textView = (TextView) findViewById(R.id.sequence_text);
			textView.setText(mEngine.Sequence);
		}
		
	}
	
	// Create a message handling object as an anonymous class.
	private OnItemClickListener mMessageClickedHandler = new OnItemClickListener() {
	    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	        TextView textView = (TextView) findViewById(R.id.current_drink_name);
	        String drinkName = mRecipies.get(position).getName();
	        textView.setText(drinkName);
	        
	        TextView description = (TextView) findViewById(R.id.current_drink_description);
	        String drinkDescription = mRecipies.get(position).getDescription();
	        description.setText(drinkDescription);
	        
	        List<Ingredient> ing = mRecipies.get(position).getIngridients();
	        
	        ArrayAdapter<Ingredient> mAdapter = new ArrayAdapter<Ingredient>(MainActivity.this, android.R.layout.simple_list_item_1, ing);
	        ListView listView = (ListView) findViewById(R.id.current_ingridient_list);
	        listView.setAdapter(mAdapter);
	        
	        mMakeButton.setVisibility(View.VISIBLE);
	        
	        currentRecipe = position;
	    }
	};
}
