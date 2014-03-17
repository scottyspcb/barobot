package com.barobot.activity;

import java.util.List;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.barobot.AppInvoker;
import com.barobot.R;
import com.barobot.gui.dataobjects.Engine;
import com.barobot.gui.dataobjects.Recipe_t;
import com.barobot.gui.dataobjects.Type;

public class BarobotMain extends BarobotActivity {

	private static BarobotMain instance;
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	instance = this;
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.barobot_main);
        AppInvoker.createInstance( this ).onCreate();

        FillRecipeList();
    }
    
    public void FillRecipeList()
    {
    	List<Recipe_t> recipeList = Engine.GetInstance(this).getRecipes();
    	
    	ArrayAdapter<Recipe_t> mAdapter = new ArrayAdapter<Recipe_t>(this, R.layout.item_layout, recipeList);
		ListView listView = (ListView) findViewById(R.id.recipe_list);
		listView.setAdapter(mAdapter);
    }
    
    public static BarobotMain getInstance()
    {
    	return instance;
    }
    
    public void showError()
    {
    }
}