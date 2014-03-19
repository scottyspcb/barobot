package com.barobot.common;

public class BarobotConnector {
	public static final int SERVOZ_UP_POS = 2100;
	public static final int SERVOZ_UP_LIGHT_POS = 2050;
	public static final int SERVOZ_DOWN_POS = 1250;
	public static final int SERVOZ_TEST_POS = 1300;
	
	public static final int SERVOY_FRONT_POS = 800;
	public static final int SERVOY_BACK_POS = 2200;
	public static final int SERVOY_TEST_POS = 1000;
	public static final int SERVOY_BACK_NEUTRAL = 1200;
	
	public static final int BOTTLE_IS_BACK = 2;
	public static final int BOTTLE_IS_FRONT = 4;
	
	public static final int DRIVER_X_SPEED = 2500;
	public static final int DRIVER_Y_SPEED = 30;
	public static final int DRIVER_Z_SPEED = 250;
	public static int[] upanels = {
		12,20,23,19,18,17,15,16,21,22,13,14,
	};
	public static int[] front_upanels = {
		23,19,13,17,15,14,20,18,12,16,21,22,
	};
	//config
	//	private static final int SERVOZ_PAC_TIME_UP = 600;
	public static final int SERVOZ_PAC_POS = 1850;
	public static final int SERVOZ_PAC_TIME_WAIT = 800;
	public static final int SERVOZ_POUR_TIME = 3500;

	// pozycje butelek, sa aktualizowane w trakcie
	public static int[] margin_x = {
		-50,				// 0, num 1,back
		-150,				// 1, num 2,front		
		-40,				// 2, num 3,back
		-150,				// 3, num 4,front		
		-20,				// 4, num 5,back		
		-150,				// 5, num 6,front
		-20,				// 6, num 7,back
		-150,				// 7, num 8,front
		-20,				// 8, num 9,back
		-150,				// 9, num 10,front
		20,					// 10, num 11,back		
		-100,				// 11, num 12,front
	};
	public static int[] magnet_order = {0,2,1,4,3,6,5,8,7,10,9,11 };	// numer butelki, odj¹c 1 aby numer ID
	public static int[] bottle_row = {
		BOTTLE_IS_BACK,					// 0, num 1
		BOTTLE_IS_FRONT,				// 1, num 2
		BOTTLE_IS_BACK,					// 2, num 3
		BOTTLE_IS_FRONT,				// 3, num 4
		BOTTLE_IS_BACK,					// 4, num 5
		BOTTLE_IS_FRONT,				// 5, num 6
		BOTTLE_IS_BACK,					// 6, num 7
		BOTTLE_IS_FRONT,				// 7, num 8
		BOTTLE_IS_BACK,					// 8, num 9
		BOTTLE_IS_FRONT,				// 9, num 10
		BOTTLE_IS_BACK,					// 10, num 11
		BOTTLE_IS_FRONT,				// 11, num 12
	};
	public static int[] b_pos_y = {
		SERVOY_BACK_POS,					// 0, num 1
		SERVOY_FRONT_POS,					// 1, num 2
		SERVOY_BACK_POS,					// 2, num 3
		SERVOY_FRONT_POS,					// 3, num 4
		SERVOY_BACK_POS,					// 4, num 5
		SERVOY_FRONT_POS,					// 5, num 6
		SERVOY_BACK_POS,					// 6, num 7
		SERVOY_FRONT_POS,					// 7, num 8
		SERVOY_BACK_POS,					// 8, num 9
		SERVOY_FRONT_POS,					// 9, num 10
		SERVOY_BACK_POS,					// 10, num 11
		SERVOY_FRONT_POS,					// 11, num 12
	};
	public static int[] b_pos_x = {207,207, 394,394,581,581,768,768, 955,955,1142,1142};
	// pozycje butelek, sa aktualizowane w trakcie
	public static int[] times = {
		SERVOZ_POUR_TIME,		
		SERVOZ_POUR_TIME, 
		SERVOZ_POUR_TIME,
		SERVOZ_POUR_TIME,
		SERVOZ_POUR_TIME,
		SERVOZ_POUR_TIME,
		SERVOZ_POUR_TIME,
		SERVOZ_POUR_TIME, 
		SERVOZ_POUR_TIME,
		SERVOZ_POUR_TIME,
		SERVOZ_POUR_TIME*2,
		SERVOZ_POUR_TIME
	};

}
