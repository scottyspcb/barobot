package com.barobot.gui;

import java.util.List;

import com.barobot.R;
import com.barobot.activity.BarobotActivity;
import com.barobot.gui.dataobjects.Engine;
import com.barobot.gui.dataobjects.Ingredient;
import com.barobot.gui.dataobjects.Liquid;

import android.os.Bundle;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.support.v4.app.NavUtils;

public class OutsideComponentActivity extends BarobotActivity
										implements NoticeDialogListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_outside_component);
		// Show the Up button in the action bar.
		UpdateList();
	}
	
	public void UpdateList() {
		List<Liquid> ing = Engine.GetInstance(this).getOutsideComponent();

		ArrayAdapter<Liquid> mAdapter = new ArrayAdapter<Liquid>(this, android.R.layout.simple_list_item_1, ing);
		ListView listView = (ListView) findViewById(R.id.outside_ingridient_list);
		listView.setAdapter(mAdapter);
	}
	
	public void addIngredient(View view) {
		FragmentTransaction ft = getFragmentManager().beginTransaction();

		// Create and show the dialog.
		SelectLiquidDialogFragment newFragment = SelectLiquidDialogFragment.newInstance();
		newFragment.ShowEmptyButton = false;
		newFragment.ShowAddButton = true;
		newFragment.ShowVolumeReel = false;
		newFragment.show(ft, "dialog");
	}
	
	public void removeIngredients(View view) {
		Engine.GetInstance(this).removeOutsideComponents();
		UpdateList();
	}

	@Override
	public void onDialogEnd(DialogFragment dialog, ReturnStatus status,
			Liquid liquid, int volume) {
		switch(status)
		{
		case OK:
			Engine.GetInstance(this).addOutsideComponent(liquid);
			UpdateList();
			break;
		case Canceled:
			break;
		case NewLiquid:
			Engine engine = Engine.GetInstance(this);
			liquid.id = engine.AddLiquid(liquid);
			
			engine.addOutsideComponent(liquid);
			UpdateList();
			break;
		}
		
	}


}
