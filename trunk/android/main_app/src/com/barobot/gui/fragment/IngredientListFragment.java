package com.barobot.gui.fragment;


import java.util.List;

import com.barobot.R;
import com.barobot.gui.dataobjects.Ingredient_t;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class IngredientListFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		return inflater.inflate(R.layout.fragment_ingredient_list, container, false);
	
		// TODO: Add onClickListener to all buttons on this fragment
	}
	
	public void ShowIngredients(List<Ingredient_t> ingredients)
	{
		ArrayAdapter<Ingredient_t> mAdapter = new ArrayAdapter<Ingredient_t>(getActivity(), R.layout.ingredient_list_item, ingredients);
		
		//ListView view = (ListView)  getView();
		ListView listView = (ListView) getView().findViewById(R.id.ingredient_list);
		
		//view.setAdapter(mAdapter);
		if( listView!= null ){
			listView.setAdapter(mAdapter);
		}
	}
	
	public void ClearIngredients() {
		// ListView view = (ListView) getView();
		ListView listView = (ListView) getView().findViewById(
				R.id.ingredient_list);

		// view.setAdapter(mAdapter);
		if (listView != null) {
			listView.setAdapter(null);
		}
	}
}
