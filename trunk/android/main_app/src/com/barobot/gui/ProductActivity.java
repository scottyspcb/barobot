package com.barobot.gui;

import java.util.List;

import com.barobot.R;
import com.barobot.activity.BarobotActivity;
import com.barobot.gui.dataobjects.Engine;
import com.barobot.gui.dataobjects.Product;
import com.barobot.gui.dataobjects.Type;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.support.v4.app.NavUtils;

public class ProductActivity extends BarobotActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_product);
		// Show the Up button in the action bar.
		//setupActionBar();
		
		FillProductList();
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

	}
	
	public void FillProductList()
	{
		Engine engine = Engine.GetInstance(this);
		
		List<Type> types = engine.getTypes();

		ArrayAdapter<Type> mAdapter = new ArrayAdapter<Type>(this, android.R.layout.simple_list_item_1, types);
		ListView listView = (ListView) findViewById(R.id.product_types_list);
		listView.setAdapter(mAdapter);
		
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				
				Type type = (Type) parent.getItemAtPosition(position);
				
				TextView tView = (TextView) findViewById(R.id.product_capacity_text);
				tView.setText(type.toString());
			}
			
		});
			
	}
}
