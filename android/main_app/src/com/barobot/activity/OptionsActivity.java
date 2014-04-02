package com.barobot.activity;

import com.barobot.R;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;

public class OptionsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_options);
	}
	
	public void onOptionsButtonClicked(View view)
	{
		Intent serverIntent = null;
		switch(view.getId())
		{
		case R.id.options_bottle_setup_button:
			serverIntent = new Intent(this, BottleSetupActivity.class);
			break;
			
		case R.id.options_recipe_setup_button:
			serverIntent = new Intent(this, RecipeSetupActivity.class);
			break;
			
		case R.id.options_advanced_button:
			serverIntent  = new Intent(this, DebugActivity.class);
			break;
		}
		
		if(serverIntent!=null){
    		serverIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
    		startActivity(serverIntent);
    	}
	}
}
