package com.barobot.hardware.devices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.barobot.common.constant.Constant;
import com.barobot.common.interfaces.HardwareState;
import com.barobot.common.interfaces.serial.CanSend;
import com.barobot.common.interfaces.serial.SerialInputListener;
import com.barobot.common.interfaces.serial.Wire;
import com.barobot.hardware.devices.i2c.Carret;
import com.barobot.hardware.devices.i2c.I2C_Device;
import com.barobot.hardware.devices.i2c.MainboardI2c;
import com.barobot.hardware.devices.i2c.Upanel;
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
	public static int[] upanels = {
		13,20,23,14,16,17,19,21,18,22,15,12,
	};
	public static int[] front_upanels = {
		13,20,23,14,16,17,19,21,18,22,15,12,
	};
	//config
	//	private static final int SERVOZ_PAC_TIME_UP = 600;
	public static final int SERVOZ_PAC_POS = 1850;
	public static final int SERVOZ_PAC_TIME_WAIT = 800;
	public static final int SERVOZ_POUR_TIME = 3500;

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

	public Carret carret		= null;
	public MotorDriver driver_x	= null;
	public Mainboard mb			= null;
	public Queue main_queue		= null;
	public Servo driver_y		= null;
	public Servo driver_z		= null;
	public HardwareState state	= null;
	
	public List<I2C_Device> i2c = new ArrayList<I2C_Device>();
	
	public BarobotConnector(HardwareState state ){
		state.set("show_unknown", 1 );

		carret				= new Carret(Constant.cdefault_index, Constant.cdefault_address);
		mb					= new Mainboard( state );
		driver_x			= new MotorDriver( state );
		driver_y			= new Servo( state, "Y" );
		driver_z			= new Servo( state, "Z" );
		main_queue  		= new Queue( mb );
		driver_x.defaultSpeed = BarobotConnector.DRIVER_X_SPEED;

		MainboardI2c MainBoard	= new MainboardI2c(Constant.mdefault_index, Constant.mdefault_address);

		I2C_Device UpanelF0 = new Upanel( 3, 0 );
		I2C_Device UpanelF1 = new Upanel( 0, 1 );
		I2C_Device UpanelF2 = new Upanel( 0, 2 );
		I2C_Device UpanelF3 = new Upanel( 0, 3 );
		I2C_Device UpanelF4 = new Upanel( 0, 4 );
		I2C_Device UpanelF5 = new Upanel( 0, 5 );
		I2C_Device UpanelB0 = new Upanel( 4, 6 );
		I2C_Device UpanelB1 = new Upanel( 0, 7 );
		I2C_Device UpanelB2 = new Upanel( 0, 8 );
		I2C_Device UpanelB3 = new Upanel( 0, 9 );
		I2C_Device UpanelB4 = new Upanel( 0, 10 );
		I2C_Device UpanelB5 = new Upanel( 0, 11 );

		MainBoard.hasResetTo( 2, carret );
		MainBoard.hasResetTo( 3, UpanelF0 );
		MainBoard.hasResetTo( 4, UpanelB0 );
		MainBoard.hasResetTo( 1, MainBoard );

		UpanelB0.hasResetTo( UpanelB1 );
		UpanelB1.hasResetTo( UpanelB2 );
		UpanelB2.hasResetTo( UpanelB3 );
		UpanelB3.hasResetTo( UpanelB4 );
		UpanelB4.hasResetTo( UpanelB5 );

		UpanelF0.hasResetTo( UpanelF1 );
		UpanelF1.hasResetTo( UpanelF2 );
		UpanelF2.hasResetTo( UpanelF3 );
		UpanelF3.hasResetTo( UpanelF4 );
		UpanelF4.hasResetTo( UpanelF5 );

		i2c.add( MainBoard );
		i2c.add( carret );
		i2c.add( UpanelF0 );
		i2c.add( UpanelF1 );
		i2c.add( UpanelF2 );
		i2c.add( UpanelF3 );
		i2c.add( UpanelF4 );
		i2c.add( UpanelF5 );
		i2c.add( UpanelB0 );
		i2c.add( UpanelB1 );
		i2c.add( UpanelB2 );
		i2c.add( UpanelB3 );	
		i2c.add( UpanelB4 );
		i2c.add( UpanelB5 );
	}

	public Upanel getUpanelBottle(int num) {
		for (I2C_Device u2 : i2c){
			if(u2.getOrder() == num ){
				return (Upanel) u2;
			}
		}
		return null;
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
		carret				= null;
		mb					= null;
		driver_x			= null;
		driver_y			= null;
		driver_z			= null;
		state				= null;
		main_queue.destroy();
		main_queue  		= null;
	}

	public I2C_Device getByAddress( int address ){
		I2C_Device t = null;
		for (I2C_Device v : i2c){
		    System.out.print(v + " ");
		}
		return t;
	}
	
}
