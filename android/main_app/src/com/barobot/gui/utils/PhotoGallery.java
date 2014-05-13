package com.barobot.gui.utils;

import com.barobot.R;

public class PhotoGallery {
	private final int galerySize = 21;
	private int[] ids;
	private int defaultImageID;
	
	public PhotoGallery()
	{
		Setup();
	}
	
	private void Setup()
	{
		defaultImageID = R.drawable.image_drink_default;
		ids = new int [galerySize+1];
		ids[0] = R.drawable.image_drink_default;
		ids[1] = R.drawable.image_drink_1;
		ids[2] = R.drawable.image_drink_2;
		ids[3] = R.drawable.image_drink_3;
		ids[4] = R.drawable.image_drink_4;
		ids[5] = R.drawable.image_drink_5;
		ids[6] = R.drawable.image_drink_6;
		ids[7] = R.drawable.image_drink_7;
		ids[8] = R.drawable.image_drink_8;
		ids[9] = R.drawable.image_drink_9;
		ids[10] = R.drawable.image_drink_10;
		ids[11] = R.drawable.image_drink_11;
		ids[12] = R.drawable.image_drink_12;
		ids[13] = R.drawable.image_drink_13;
		ids[14] = R.drawable.image_drink_14;
		ids[15] = R.drawable.image_drink_15;
		ids[16] = R.drawable.image_drink_16;
		ids[17] = R.drawable.image_drink_17;
		ids[18] = R.drawable.image_drink_18;
		ids[19] = R.drawable.image_drink_19;
		ids[20] = R.drawable.image_drink_20;	
	}

	public int getImageID(int photoId)
	{
		if (photoId < 1 || photoId > galerySize)
		{
			return defaultImageID;
		}
		return ids[photoId];
	}
}
