package com.barobot.gui.fragment;


import com.barobot.R;
import com.barobot.gui.utils.PhotoGallery;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class DrinkImageFragment extends Fragment {
	private PhotoGallery pGallery;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		pGallery = new PhotoGallery();
		return inflater.inflate(R.layout.fragment_drink_image, container, false);
	
		// TODO: Add onClickListener to all buttons on this fragment
	}
	
	public void SetImage(int photoId)
	{
		int id = pGallery.getImageID(photoId);
		
		ImageView iView = (ImageView) getView().findViewById(R.id.drink_image);
		iView.setImageResource(id);
	}
	
	public void ClearImage()
	{
		ImageView iView = (ImageView) getView().findViewById(R.id.drink_image);
		iView.setImageResource(0);
	}
}
