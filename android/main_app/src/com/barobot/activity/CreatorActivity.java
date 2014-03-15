package com.barobot.activity;

import com.barobot.R;
import com.barobot.R.layout;
import com.barobot.R.menu;
import com.barobot.gui.RecipeFragment;

import android.os.Bundle;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.view.Menu;

public class CreatorActivity extends BarobotActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_creator);
		
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		
		RecipeFragment fragment = new RecipeFragment();
		fragmentTransaction.add(R.id.page_layout, fragment);
		fragmentTransaction.commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

}
