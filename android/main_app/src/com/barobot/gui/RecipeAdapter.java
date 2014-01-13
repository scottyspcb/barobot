/**
 * 
 */
package com.barobot.gui;

import java.util.List;

import com.barobot.R;
import com.barobot.gui.dataobjects.Recipe;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * @author Raven
 *
 */
public class RecipeAdapter extends ArrayAdapter<Recipe> {
	private Context context;
	private List<Recipe> recipies;
	
	public RecipeAdapter(Context context, int TextViewResourceId, List<Recipe> objects)
	{
		super(context, TextViewResourceId, objects);
		this.context = context;
		this.recipies = objects;
	}
	
	public View getView(int position, View convertView, ViewGroup parent)
	{
		LayoutInflater inflater = (LayoutInflater) context.
				getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		View rowView = inflater.inflate(R.layout.recipetile, null, false);
		TextView nameTextView = (TextView) rowView.findViewById(R.id.detailed_description);
		
		
		Recipe current = recipies.get(position);
		nameTextView.setText(current.getName());
		
		return rowView;
	}
}
