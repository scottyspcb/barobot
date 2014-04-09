package com.barobot.gui.utils;

public class PhotoGallery {
	private final int galerySize = 5;
	private int[] ids;
	private int defaultImageID;
	
	public PhotoGallery()
	{
		Setup();
	}
	
	private void Setup()
	{
		defaultImageID = com.barobot.R.drawable.image_drink_03;
		ids = new int [galerySize+1];
		ids[1] = com.barobot.R.drawable.image_drink_03;
		ids[2] = com.barobot.R.drawable.image_drink_08;
		ids[3] = com.barobot.R.drawable.image_drink_09;
		ids[4] = com.barobot.R.drawable.image_drink_14;
		ids[5] = com.barobot.R.drawable.image_drink_16;
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
