package com.barobot.gui.fragment;


import com.barobot.R;
import com.barobot.gui.TextProgressBar;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class RecipeAttributesFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		return inflater.inflate(R.layout.fragment_recipe_attributes, container, false);
	
		// TODO: Add onClickListener to all buttons on this fragment
	}
	
	public void SetAttributes(int sweet, int sour, int bitter, int strength)
	{
		TextProgressBar sweetPB = (TextProgressBar) getView().findViewById(R.id.progress_bar_sweet);
		TextProgressBar sourPB = (TextProgressBar) getView().findViewById(R.id.progress_bar_sour);
		TextProgressBar bitterPB = (TextProgressBar) getView().findViewById(R.id.progress_bar_bitter);
		TextProgressBar strengthPB = (TextProgressBar) getView().findViewById(R.id.progress_bar_strength);
		
		sweetPB.setProgress(Normalize(sweet));
		sourPB.setProgress(Normalize(sour));
		bitterPB.setProgress(Normalize(bitter));
		strengthPB.setProgress(Normalize(strength));
	}
	
	public void ClearAttributes()
	{
		TextProgressBar sweetPB = (TextProgressBar) getView().findViewById(R.id.progress_bar_sweet);
		TextProgressBar sourPB = (TextProgressBar) getView().findViewById(R.id.progress_bar_sour);
		TextProgressBar bitterPB = (TextProgressBar) getView().findViewById(R.id.progress_bar_bitter);
		TextProgressBar strengthPB = (TextProgressBar) getView().findViewById(R.id.progress_bar_strength);
		
		sweetPB.setProgress(0);
		sourPB.setProgress(0);
		bitterPB.setProgress(0);
		strengthPB.setProgress(0);
	}
	
	private int Normalize(int value)
	{
		if (value > 100) return 100;
		if (value < 0) return 0;
		return value;
	}
}
