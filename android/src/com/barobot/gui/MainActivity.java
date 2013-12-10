package com.barobot.gui;

import java.util.List;

import com.barobot.gui.dataobjects.Engine;
import com.barobot.gui.dataobjects.Ingredient;
import com.barobot.R;
import com.barobot.gui.dataobjects.Recipe;




import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends BarobotActivity 
							implements ArduinoListener {
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
		
		PrepareDrinkList();
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		PrepareDrinkList();
	}
	
	protected void PrepareDrinkList()
	{
		mEngine = Engine.GetInstance(this);
		currentRecipe = -1;
		
		mRecipies = mEngine.getPossibleRecipes();
		rAdapter = new RecipeAdapter(this, R.layout.recipetile, mRecipies);
		
		GridView gridView = (GridView) findViewById(R.id.gridview_recipe);
		gridView.setAdapter(rAdapter);
		gridView.setOnItemClickListener(mMessageClickedHandler);
		
		mMakeButton = (Button) findViewById(R.id.make_button);
		mMakeButton.setVisibility(View.INVISIBLE);
	}
	
	public void make(View view)
	{
		if (currentRecipe != -1) {
			final Recipe recipe = mRecipies.get(currentRecipe);
			
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.glass_reminder_title);
			builder.setMessage(R.string.glass_reminder_message);
			builder.setPositiveButton(R.string.glass_reminder_button_ok, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					
					Button makeButton = (Button) findViewById(R.id.make_button);
					makeButton.setEnabled(false);
					
					mEngine.Prepare(recipe, MainActivity.this);
				}
			});
			builder.setNegativeButton(R.string.glass_reminder_button_cancel, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
				}
			});
			
			AlertDialog ad = builder.create();
			ad.show();
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

	@Override
	public void onQueueFinished() {
		
		
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Button makeButton = (Button) findViewById(R.id.make_button);
				if(makeButton!=null){
					makeButton.setEnabled(true);
				}
			}
		});
		
		
		/*
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.pour_finished_title);
		//builder.setMessage(R.string.pour_finished_message);
		builder.setMessage("Used combination (" + mEngine.Sequence + ")");
		builder.setNeutralButton(R.string.pour_finished_button, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});
		
		AlertDialog ad = builder.create();
		ad.show();*/
	}
}
