package com.barobot.activity;

import com.barobot.R;
import com.barobot.gui.fragment.MenuFragment;
import com.barobot.hardware.virtualComponents;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;

public class OptionsActivity extends BarobotActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_options);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		MenuFragment menuFrag = (MenuFragment) getFragmentManager().findFragmentById(R.id.fragment_menu);
		menuFrag.SetBreadcrumb(MenuFragment.MenuItem.Options);
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

		case R.id.options_calibrate_button:
			virtualComponents.barobot.kalibrcja();
			break;

		case R.id.options_demo_button:
			virtualComponents.barobot.startDemo();
			break;
		}
		if(serverIntent!=null){
    		serverIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
    		startActivity(serverIntent);
    	}
	}
}
