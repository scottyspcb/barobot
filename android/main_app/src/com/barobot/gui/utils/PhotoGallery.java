package com.barobot.gui.utils;

import com.barobot.R;

public class PhotoGallery {
	private final int galerySize = 49;
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
		ids[21] = R.drawable.image_drink_21;
		ids[22] = R.drawable.image_drink_22;
		ids[23] = R.drawable.image_drink_23;
		ids[24] = R.drawable.image_drink_24;
		ids[25] = R.drawable.image_drink_25;
		ids[26] = R.drawable.image_drink_26;
		ids[27] = R.drawable.image_drink_27;
		ids[28] = R.drawable.image_drink_28;
		ids[29] = R.drawable.image_drink_29;
		ids[30] = R.drawable.image_drink_30;
		ids[31] = R.drawable.image_drink_31;
		ids[32] = R.drawable.image_drink_32;
		ids[33] = R.drawable.image_drink_33;
		ids[34] = R.drawable.image_drink_34;
		ids[35] = R.drawable.image_drink_35;
		ids[36] = R.drawable.image_drink_36;
		ids[37] = R.drawable.image_drink_37;
		ids[38] = R.drawable.image_drink_38;
		ids[39] = R.drawable.image_drink_39;
		ids[40] = R.drawable.image_drink_40;
		ids[41] = R.drawable.image_drink_41;
		ids[42] = R.drawable.image_drink_42;
		ids[43] = R.drawable.image_drink_43;
		ids[44] = R.drawable.image_drink_44;
		ids[45] = R.drawable.image_drink_45;
		ids[46] = R.drawable.image_drink_46;
		ids[47] = R.drawable.image_drink_47;
		ids[48] = R.drawable.image_drink_48;
		ids[49] = R.drawable.image_drink_49;
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
