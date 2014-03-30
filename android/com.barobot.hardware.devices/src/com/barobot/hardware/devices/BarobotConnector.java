package com.barobot.hardware.devices;

import com.barobot.common.interfaces.HardwareState;
import com.barobot.common.interfaces.serial.CanSend;
import com.barobot.common.interfaces.serial.SerialInputListener;
import com.barobot.common.interfaces.serial.Wire;
import com.barobot.hardware.devices.i2c.I2C;
import com.barobot.parser.Queue;
import com.barobot.parser.message.Mainboard;

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

	//	private static final int SERVOZ_PAC_TIME_UP = 600;
	public static final int SERVOZ_PAC_POS = 1850;
	public static final int SERVOZ_PAC_TIME_WAIT = 800;
	public static final int SERVOZ_POUR_TIME = 3500 / 20;		// predkosc nalewania 20ml

	// pozycje butelek, sa aktualizowane w trakcie
	public static int[] margin_x = {
		-70,			// 0, num 1,back
		-150,			// 1, num 2,front		
		-60,			// 2, num 3,back
		-150,			// 3, num 4,front		
		-40,			// 4, num 5,back		
		-140,			// 5, num 6,front
		-30,			// 6, num 7,back
		-150,			// 7, num 8,front
		-40,			// 8, num 9,back
		-140,			// 9, num 10,front
		20,				// 10, num 11,back		
		-100,			// 11, num 12,front
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
	// pozycje butelek, sa aktualizowane w trakcie
	public static int[] capacity = {
		20,		
		20, 
		20,
		20,
		20,
		20,
		20,
		20, 
		20,
		20,
		50,
		20
	};

	public MotorDriver driver_x	= null;
	public Mainboard mb			= null;
	public Queue main_queue		= null;
	public Servo driver_y		= null;
	public Servo driver_z		= null;
	public HardwareState state	= null;
	public I2C i2c				= null;

	public BarobotConnector(HardwareState state ){
		mb			= new Mainboard( state );
		driver_x	= new MotorDriver( state );
		driver_y	= new Servo( state, "Y" );
		driver_z	= new Servo( state, "Z" );
		main_queue  = new Queue( mb );
		i2c  		= new I2C();
		driver_x.defaultSpeed = BarobotConnector.DRIVER_X_SPEED;
	}

	public SerialInputListener willReadFrom(Wire connection) {
		SerialInputListener listener = new SerialInputListener() {
			public void onRunError(Exception e) {
			}
			public void onNewData(byte[] data, int length) {
				String in = new String(data, 0, length);
			//	Log.e("Serial addOnReceive", message);
				mb.read( in );
			//	debug( message );
			}
			public boolean isEnabled() {
				return true;
			}
		};
		connection.addOnReceive( listener );
		return listener;
	}

	public void willWriteThrough(CanSend connection) {
		mb.registerSender(connection);
	}

	public void destroy() {
		main_queue.destroy();
		mb					= null;
		driver_x			= null;
		driver_y			= null;
		driver_z			= null;
		state				= null;
		main_queue  		= null;
		i2c					= null;
	}
}
