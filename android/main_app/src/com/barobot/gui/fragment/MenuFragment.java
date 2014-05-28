package com.barobot.gui.fragment;


import java.util.Dictionary;
import java.util.Hashtable;

import com.barobot.R;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class MenuFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		buttons = new Hashtable<MenuFragment.MenuItem, Integer>();
	//	buttons.put(MenuItem.Favorite, R.id.menu_favorite);
		buttons.put(MenuItem.Choose, R.id.menu_choose);
		buttons.put(MenuItem.Lucky, R.id.menu_lucky);
		buttons.put(MenuItem.Create, R.id.menu_create);
		buttons.put(MenuItem.Options, R.id.menu_options);

		return inflater.inflate(R.layout.fragment_menu, container, false);
	
		// TODO: Add onClickListener to all buttons on this fragment
	}
	
	private Dictionary<MenuItem, Integer> buttons;
	
	public enum MenuItem
	{
	//	Favorite,
		Choose,
		Lucky,
		Create,
		Options
	}
	
	public void SetBreadcrumb(MenuItem item)
	{
		ClearBreadcrubs();
		LightUp(item);
	}
	
	private void LightUp(MenuItem item)
	{
	    setBackground(item, R.drawable.button_menu_selected);	
	}
	
	private void ClearBreadcrubs()
	{
		for(MenuItem item : MenuItem.values())
		{
			ClearBreadcrumb(item);
		}
	}
	
	private void ClearBreadcrumb(MenuItem item)
	{
		    setBackground(item, R.drawable.button_menu);
	}
	
	private void setBackground(MenuItem item, int drawableId)
	{
		Button button = (Button) getView().findViewById(buttons.get(item));
		
		int bottom = button.getPaddingBottom();
	    int top = button.getPaddingTop();
	    int right = button.getPaddingRight();
	    int left = button.getPaddingLeft();
	    
		button.setBackgroundResource(drawableId);
		
		button.setPadding(left, top, right, bottom);
	}
}
