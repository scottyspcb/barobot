package com.barobot.hardware.devices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.barobot.common.Initiator;
import com.barobot.common.constant.Constant;
import com.barobot.common.constant.Methods;
import com.barobot.common.interfaces.HardwareState;
import com.barobot.common.interfaces.serial.CanSend;
import com.barobot.common.interfaces.serial.SerialInputListener;
import com.barobot.common.interfaces.serial.Wire;
import com.barobot.hardware.devices.i2c.I2C;
import com.barobot.parser.Queue;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.message.Mainboard;
import com.barobot.parser.utils.Decoder;

public class BarobotConnector {
	public boolean ledsReady	= false;
	public static boolean pureCrystal = false;
	public MotorDriver driver_x	= null;
	public Mainboard mb			= null;
	public Queue main_queue		= null;
//	public Servo driver_y		= null;
//	public Servo driver_z		= null;
	public HardwareState state	= null;
	public I2C i2c				= null;
	protected int robot_id		= 0;
	public boolean newLeds		= true;
	public boolean robot_id_ready		= false;	
	public boolean robot_id_error		= false;
	public long lastSeenRobotTimestamp	= 0;
	public LightManager lightManager	= null;
	public boolean init_done			= false;
	public Weight weight				= new Weight();
	public Z z							= new Z();
	public Y y							= new Y();

	public BarobotConnector(HardwareState state ){
		this.state		= state;
		if( newLeds ){
			ledsReady = true;
		}else{
	//		use_beta	= true;	// old robot is always beta
		}
		this.loadDefaults();
		this.mb			= new Mainboard( state );
		this.driver_x	= new MotorDriver( state );
	//	this.driver_y	= new Servo( state, "Y" );
	//	this.driver_z	= new Servo( state, "Z" );
		this.main_queue = new Queue( mb );
		this.lightManager = new LightManager(this);

		mb.setMainQueue( this.main_queue );
		this.i2c  		= new I2C();
		this.robot_id	= state.getInt("ROBOT_ID", 0 );
	}

	private void loadDefaults() {
		this.state.set( "STAT1", this.state.getInt( "STAT1", 0 ) + 1 );						// app starts
		this.state.set( "RESET_TIME", this.state.getInt( "RESET_TIME", 200 ) );				// set default
		this.state.set( "NEED_HALL_X", this.state.getInt( "NEED_HALL_X", 1 ) );				// set default
		this.state.set( "NEED_HALL_Y", this.state.getInt( "NEED_HALL_Y", 1 ) );				// set default

		this.state.set( "NEED_GLASS", this.state.getInt( "NEED_GLASS", 0 ) );				// set default
		this.state.set( "WATCH_GLASS", this.state.getInt( "WATCH_GLASS", 0 ) );				// set default	

		this.state.set( "MAX_GLASS_CAPACITY", this.state.getInt( "MAX_GLASS_CAPACITY", 190 ) );		// set default (in ml)
		this.state.set( "GLASS_DIFF", this.state.getInt( "GLASS_DIFF", 50 ) );				// set default
		this.state.set( "LIGH_GLASS_DIFF", this.state.getInt( "LIGH_GLASS_DIFF", 5 ) );		// set default
		this.state.set( "ALLOW_LIGHT_CUP", this.state.getInt( "ALLOW_LIGHT_CUP", 0 ) );		// set default

		this.state.set( "SSERVER", this.state.getInt( "SSERVER", 1 ) );								// set default
		this.state.set( "SSERVER_API", this.state.getInt( "SSERVER_API", 1 ) );						// set default
		this.state.set( "SSERVER_PASS", this.state.get( "SSERVER_PASS", "BAROBOT" ) );				// set default
		this.state.set( "SSERVER_CONFIG_PASS", this.state.getInt( "SSERVER_CONFIG_PASS", 1 ) );		// set default

		this.state.set( "SSERVER_ALLOW_CONFIG", this.state.getInt( "SSERVER_ALLOW_CONFIG", 0 ) );		// set default
		this.state.set( "SSERVER_ALLOW_CREATOR", this.state.getInt( "SSERVER_ALLOW_CREATOR", 1 ) );		// set default
		this.state.set( "SSERVER_ALLOW_LIST", this.state.getInt( "SSERVER_ALLOW_LIST", 1 ) );			// set default

		this.state.set( "SERVOY_HFRONT_POS", 60 );	// add histeresis

		boolean defaultsLoaded	= state.getInt("DEFAULTS", 0 ) > 0;
		if( Constant.use_beta || !defaultsLoaded ){
			if(newLeds){
				// changed in wizard:
				state.set( "SERVOY_FRONT_POS", 890 );		
				state.set( "SERVOZ_UP_POS", 1400 );
				state.set( "SERVOZ_DOWN_POS", 2300 );	
				state.set( "SERVOY_BACK_POS", 1820 );

				// rest
				state.set( "SERVOZ_PAC_POS", 1550 );
				state.set( "SERVOZ_UP_LIGHT_POS", 1400 );
				state.set( "SERVOZ_UP_LIGHT_TIME", 800 );
				state.set( "SERVOZ_TEST_POS", 2200 );
				state.set( "SERVOZ_NEUTRAL", 2400 );
				state.set( "SERVOY_HYSTERESIS", 30 );
				state.set( "SERVOY_TEST_POS", 1000 );
				state.set( "SERVOY_BACK_NEUTRAL", 1200 );
				state.set( "DRIVER_X_SPEED", 3000 );
				state.set( "DRIVER_Y_SPEED", 25 );
			}else{
				state.set( "SERVOZ_PAC_POS", 1880 );
				state.set( "SERVOZ_UP_POS", 2100 );
				state.set( "SERVOZ_UP_LIGHT_POS", 2050 );
				state.set( "SERVOZ_DOWN_POS", 1250 );
				state.set( "SERVOZ_TEST_POS", 1300 );
				state.set( "SERVOZ_NEUTRAL", 1100 );
				state.set( "SERVOY_FRONT_POS", 790 );
				state.set( "SERVOY_HFRONT_POS", 20 );	// add histeresis
				state.set( "SERVOY_BACK_POS", 2200 );
				state.set( "SERVOY_TEST_POS", 1000 );
				state.set( "SERVOY_BACK_NEUTRAL", 1200 );
				state.set( "DRIVER_X_SPEED", 2900 );
				state.set( "DRIVER_Y_SPEED", 30 );
			}
			state.set( "DRIVER_Z_SPEED", 255 );
			state.set( "DRIVER_CALIB_X_SPEED", 1100 );
			state.set( "SERVOZ_PAC_TIME_WAIT", 1300 );
			state.set( "SERVOZ_PAC_TIME_WAIT_VOL", 300 );
			state.set( "SERVOY_REPEAT_TIME", 600 );
			state.set( "SERVOZ_POUR_TIME", 2500 / 20 );			// predkosc nalewania 20ml, dac 3200
			state.set( "SERVOZ_UP_TIME", 1900 );				// czas potrzebny na zajechanie w gore
			state.set( "SERVOZ_UP_TIME_MIN", 250 );

			state.set("show_unknown", 1 );
			state.set("show_sending", 1 );
			state.set("show_reading", 1 );
			state.set( "DEFAULTS", Constant.ANDROID_APP_VERSION );
		}else{
		}
	}

	public SerialInputListener willReadFrom(Wire connection) {
		SerialInputListener listener = new SerialInputListener() {
			public void onRunError(Exception e) {
			}
			public synchronized void onNewData(byte[] data, int length) {
				String in = new String(data, 0, length);	
			//	Initiator.logger.i(Constant.TAG,"Serial addOnReceive: "+ in );
				mb.read( in );
			//	debug( message );
			}
		};
		connection.setOnReceive( listener );
		return listener;
	}

	public void willWriteThrough(CanSend connection) {
		mb.registerSender(connection);
	}

	public void destroy() {
		main_queue.destroy();
		mb.destroy();
		mb				= null;
		driver_x		= null;
	//	driver_y		= null;
	//	driver_z		= null;
		state			= null;
		main_queue  	= null;
		i2c				= null;
	}

	public void cancel_all() {
		Queue mq = main_queue;
		mq.clear();
		mq.add("LIVE A OFF", false );
//		add("EZ");
		int poszdown	=  state.getInt("SERVOZ_DOWN_POS", 9 );
		mq.add("K" + poszdown, false );
		mq.add("DX", false );
		mq.add("DY", false );
		mq.add("DZ", false );
		mq.add(Constant.GETXPOS, false );
	}

	public void hereIsStart( int posx, int posy) {
		//Initiator.logger.i(Constant.TAG,"zapisuje start:" +posx+ " " + posy );
		state.set("POS_START_X", posx );
	}

	public int getBottlePosX( int num ) {
		return state.getInt("BOTTLE_X_" + num, 0 ) + this.getSlotMarginX( num );
	}

	public int getBottlePosY( int i ) {
		if ( i > Constant.bottle_row.length || Constant.bottle_row[ i ] == Constant.BOTTLE_IS_BACK ){
			return this.state.getInt( "SERVOY_BACK_POS", 1910 );
		}else{										// Constant.bottle_row[ i ] == Constant.BOTTLE_IS_BACK
			return this.state.getInt( "SERVOY_FRONT_POS", 810 );
		}
	}

	// zapisz ze tutaj jest butelka o danym numerze
	public void hereIsBottle(int num, int posx, int posy){
		Initiator.logger.i(Constant.TAG,"zapisuje pozycje:"+ num + " / " +posx+ " / " + posy );
		state.set("BOTTLE_X_" + num, posx );
	}
/*
	// zapisz ze tutaj jest butelka o danym numerze
	public void hereIsBottle(int i) {
		int posx		=  driver_x.getSPos();
		int posy		=  state.getInt("POSY", 0 );
	//	Initiator.logger.i(Constant.TAG,"zapisuje pozycje:"+ i + " " +posx+ " " + posy );
		state.set("BOTTLE_X_" + i, posx );
	}
*/
	public void startDoingDrink( Queue q) {// homing
		lightManager.carret_color(q, 255, 0,0);
		q.addWithDefaultReader( "S" );			// read temp
		lightManager.setAllLeds( q, "40", 255, 0, 0,255 );
		doHoming(q, false);
	}

	public void calibration(Queue q) {
		q.add( "\n", false );
		q.add( "\n", false );
		lightManager.turnOffLeds(q);
//		int posx		= driver_x.getSPos();
		/*
		for(int i=0;i<12;i++){
			state.set("BOTTLE_X_" + i, "0" );
		}*/
		this.readHardwareRobotId(q);
		state.set("POS_START_X", "0" );
	//	Initiator.logger.i("+find_bottles", "start");
		z.moveDown( q, false );
		q.addWait(5);
		int SERVOZ_TEST_POS = state.getInt("SERVOZ_TEST_POS", 0 );
		z.move( q, SERVOZ_TEST_POS );

		q.addWait(10);
		z.moveDown( q, true );
		q.addWait(10);
		doHoming(q, true );
		// scann Triggers
		q.add( new AsyncMessage( true ) {			// go up
			@Override	
			public String getName() {
				return "kalibrcja" ;
			}
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				this.name		= "scanning up";
		//		driver_x.defaultSpeed = state.getInt("DRIVER_CALIB_X_SPEED", 0 );

				Initiator.logger.i("+find_bottles", "up");
				state.set("scann_bottles", 1 );
				return null;
			}
		});
		q.addWait( 100 );
		driver_x.moveTo( q, 20000, state.getInt("DRIVER_CALIB_X_SPEED", 0 ) );		// go down
		q.addWait( 200 );
		q.add( new AsyncMessage( true ) {
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				this.name		= "scanning back";
			//	driver_x.defaultSpeed = state.getInt("DRIVER_X_SPEED", 0 );
				Initiator.logger.i("+find_bottles", "down kalibracja");
				return null;
			}
		} );
		driver_x.moveTo( q, -20000);
		q.add( new AsyncMessage( true ) {
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				this.name		= "end scanning";
				Initiator.logger.i("+find_bottles", "koniec kalibrcja");
				state.set("scann_bottles", 0 );
				boolean error = false;
				for(int i=0;i<12;i++){
					int xpos = state.getInt("BOTTLE_X_" + i, 0 );
					if(xpos ==0){
						error = true;
					}
				}
				if(error){
					Initiator.logger.i("+find_bottles", "show error");
				//	BarobotMain.getInstance().showError();
				}
				return null;
			}
		});
		q.add("DX", true);
		q.add("DZ", true);
		y.disable( q );
	}

	public void moveToStart(Queue q) {
		lightManager.setAllLeds( q, "11", 100, 100, 0,00 );
		q.add( new AsyncMessage( true ) {
			@Override	
			public String getName() {
				return "moveToStart" ;
			}
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				this.name		= "check position";
				int posx		= driver_x.getSPos();		// czy ja juz jestem na tej pozycji?	
			//	int posz		= state.getInt("POSZ", 0 );
				int sposx		= state.getInt("POS_START_X", 0 );		// tu mam byc

				Queue	q2	= new Queue();
				z.moveDown(q2, true );				
				/*int SERVOZ_DOWN_POS = state.getInt("SERVOZ_DOWN_POS", 0 );
				if( posz != SERVOZ_DOWN_POS ){
					moveZDown(q2, true );
				}*/
				y.moveToFront( q2 );
				if(posx != sposx ){
					driver_x.moveTo( q2, sposx);
					doHoming( q2, false );
				}
				z.disable(q2);
				return q2;
			}
		} );
	    q.add(Constant.GETXPOS, true);
	    q.add(Constant.GETYPOS, true);
	    q.add(Constant.GETZPOS, true);
	}
	public void doHoming(Queue q, final boolean always) {
		q.add(Constant.GETXPOS, true);	
		q.add( new AsyncMessage( "A0", true ) {// check i'm over endstop (neodymium magnet)
			@Override
			public String getName() {
				return "homing";
			}
			@Override
			public boolean isRet(String result, Queue mainQueue) {
			//	Initiator.logger.w("startDoingDrink.onInput", result );
				if(result.matches("^" +  Methods.METHOD_IMPORTANT_ANALOG + ",0,.*" )){	// 125,0,100,0,0,0,255,202,126,1
					int[] parts = Decoder.decodeBytes( result );
					boolean need_homing		= false;
					boolean thisIsMax		= false;
		//			Initiator.logger.w("startDoingDrink.input:", ""+parts[2]  );
					if( parts[2] == Methods.HX_STATE_9 ){			// this is max
						// ok it is home
					}else if( parts[2] ==Methods.HX_STATE_0 ||  parts[2] ==Methods.HX_STATE_1){
						thisIsMax  	= true;
					}else{
						need_homing = true;
					}

					Queue	q2	= new Queue(); 
					int SERVOY_TEST_POS		= state.getInt("SERVOY_TEST_POS", 0 );
					int SERVOY_FRONT_POS	= state.getInt("SERVOY_FRONT_POS", 0 );
					int SERVOY_HFRONT_POS	= SERVOY_FRONT_POS + state.getInt("SERVOY_HFRONT_POS", 0 );
					int posy				= state.getInt("POSY", 0 );
					if(posy >= SERVOY_HFRONT_POS ){
						y.move( q2, SERVOY_TEST_POS, true);
						q2.addWait(100);
						y.move( q2, SERVOY_FRONT_POS, true);
						q2.addWait(100);
					}
					//disabley( q2 );

//					int lengthx19	=  state.getInt("LENGTHX", 60000 );
				//	Initiator.logger.i("+find_bottles", "up");
					z.moveDown( q2, true );
					if( always || need_homing ){
						if(!thisIsMax){
							int newSpos = driver_x.getSPos() + 100;
							driver_x.moveTo(q2, newSpos);
							//int poshx = driver_x.getHardwarePos();
						//	q2.add("X"+ (poshx+100) +","+ BarobotConnector.this.driver_x.defaultSpeed, true);	// +100
						//	q2.addWait(10);
						}
					}
					int speed = state.getInt("DRIVER_X_SPEED", 0 );
					if( always ){
						q2.add("X-10000,"+ speed, true);
						q2.addWait(100);
						q2.addWithDefaultReader( "IH" );		// reset hardware X pos
						mainQueue.addFirst(q2);
					}else if( need_homing ){
						q2.add("X-10000,"+ speed, true);
						q2.addWait(100);
						q2.addWithDefaultReader( "IH" );		// reset hardware X pos
					//	return true;
						mainQueue.addFirst(q2);
					}
				}
				return false;
			}
			/*
			@Override
			public Queue run(Mainboard dev, Queue queue){
				this.name		= "Check Hall X";
				queue.sendNow("A0");
				return null;
			}*/
			@Override
			public boolean onInput(String input, Mainboard dev, Queue mainQueue) {
			//	Initiator.logger.w("startDoingDrink.onInput", input );
				if(input.equals("RA0")){		// its me
					return true;
				}else{
					return false;
				}
			}
		});
	}

	public void onDrinkFinish(Queue q){
		z.moveLight(q, -1, false );					// move up to help
	//	q.addWait(400);
		q.add("DX", true);
		y.disable( q );
	//	disablez(q);
		lightManager.carret_color(q, 0, 255,0);
	    lightManager.turnOffLeds(q);
    	lightManager.setAllLeds(q, "22", 50, 0, 50, 0);
    	q.addWait(500);
    	lightManager.setAllLeds(q, "22", 0, 0, 0, 0);
    	q.addWait(500);
    	lightManager.setAllLeds(q, "22", 100, 0, 100, 0);
    	q.addWait(500);
    	lightManager.setAllLeds(q, "22", 0, 0, 0, 0);
    	q.addWait(900);
    	lightManager.setAllLeds(q, "22", 255, 0, 255, 0);	
		lightManager.carret_color(q, 0, 255, 0);
		q.addWait(1000);
	}

	public void moveToBottle(Queue q, final int num, final boolean disableOnReady ){
	//	moveZDown( q ,true );
		q.add(Constant.GETXPOS, true);
	    q.add(Constant.GETYPOS, true);
	    q.add(Constant.GETZPOS, true);
		q.add( new AsyncMessage( true ){
			@Override	
			public String getName(){
				return "moveToBottle" ;
			}
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				this.name	= "check position";
				int cx		= driver_x.getSPos();		// czy ja juz jestem na tej pozycji?	
				int cy		= state.getInt("POSY", 0 );
				int tx 		= getBottlePosX( num );
				int ty  	= getBottlePosY( num );
				if( tx == 0 && ty == 0 ){
					return null;
				}
				Queue	q2	= new Queue();
				lightManager.color_by_bottle(q2, num, true, 255, 0, 0);
				Initiator.logger.i("moveToBottle","(cx == tx && cy == ty)("+cx+" == "+tx+" && "+cy+" == "+ty+")");
				z.moveDown(q2, true );
				int SERVOY_FRONT_POS = state.getInt("SERVOY_FRONT_POS", 0 );
				int SERVOY_HFRONT_POS = SERVOY_FRONT_POS + state.getInt("SERVOY_HFRONT_POS", 0 );
				int type = 0;
				if(cx == tx && cy == ty ){				// not needed
			//		q2.addWait( Constant.SERVOY_REPEAT_TIME );
					type = 1;
				}else if(cx != tx && cy == ty && ty <= SERVOY_HFRONT_POS ){	// change X, Y = front
					driver_x.moveTo( q2, tx);
					type = 2;
				}else if(cx != tx && cy != ty && ty <= SERVOY_HFRONT_POS  ){	// change X and Y and target = front
					y.move( q2, ty, disableOnReady );
					driver_x.moveTo( q2, tx );
					type = 3;
				}else if(cx != tx && cy < ty && cy <= SERVOY_HFRONT_POS  ){	// change X and Y and current = front, target = back
					driver_x.moveTo( q2, tx );
					y.move( q2, ty, disableOnReady );
					type = 4;
				}else if(cx == tx && cy != ty ){		// change Y
					y.move( q2, ty, disableOnReady );	
					type = 5;
				}else{									// (change X and Y ) or (change X and Y is back)
					int SERVOY_BACK_NEUTRAL = state.getInt("SERVOY_BACK_NEUTRAL", 0 );
					y.move( q2, SERVOY_BACK_NEUTRAL, true);
					driver_x.moveTo( q2, tx );
					y.move( q2, ty, disableOnReady );
				}
				Initiator.logger.i("moveToBottle","type"+ type);
				lightManager.color_by_bottle(q2, num, true, 0, 255, 0);
				return q2;
			}
		} );
		//disabley( q );
	    q.add(Constant.GETXPOS, true);
	    q.add(Constant.GETYPOS, true);
	    q.add(Constant.GETZPOS, true);
	}

	public void pour( Queue q, final int capacity, final int bottleNum, boolean disableOnReady, boolean needGlass ) {			// num 0-11
		int time = getPourTime(bottleNum, capacity);

		Queue q_ok		= new Queue();
		Queue q_error	= new Queue();
		this.lightManager.setAllLeds(q_error, "11", 255, 255, 0, 0);q_error.addWait( 300 );
		this.lightManager.setAllLeds(q_error, "11", 00, 0, 0, 0);q_error.addWait( 300 );
		this.lightManager.setAllLeds(q_error, "11", 00, 255, 0, 0);q_error.addWait( 300 );
		this.lightManager.setAllLeds(q_error, "11", 00, 0, 0, 0);q_error.addWait( 300 );

		q_ok.add("EX", true);

		z.moveUp(q_ok, bottleNum, false);
		this.lightManager.color_by_bottle(q_ok, bottleNum, true, 0, 255, 0);
		q_ok.addWait( time/4 );

	//	moveZLight(q, bottleNum, false);

		this.lightManager.setLedsByBottle(q_ok, bottleNum, "04", 0, 0, 0, 0);
		q_ok.addWait( time/4 );

		this.lightManager.setLedsByBottle(q_ok, bottleNum, "04", 255, 0, 255, 0);
		q_ok.addWait( time/4 );

		this.lightManager.setLedsByBottle(q_ok, bottleNum, "04", 0, 0, 0, 0);	
		q_ok.addWait( time/4 );

		this.lightManager.setLedsByBottle(q_ok, bottleNum, "04", 255, 255, 255, 200);

		z.moveDown(q_ok, false);

		q_ok.add( new AsyncMessage( true ) {
			@Override	
			public String getName() {
				return "pac pac" ;
			}
			@Override
			public Queue run(Mainboard dev, Queue queue) {
			//	if(virtualComponents.pac_enabled){
					Queue	q2				= new Queue();	
					int SERVOZ_PAC_POS		= state.getInt("SERVOZ_PAC_POS", 0 );
					int SERVOZ_DOWN_POS		= state.getInt("SERVOZ_DOWN_POS", 0 );
					int time				= getPacWaitTime( bottleNum, capacity );	
					int SERVOZ_UP_TIME		= state.getInt("SERVOZ_UP_TIME", 2000 );

					lightManager.setLedsByBottle(q2, bottleNum, "11", 100, 100, 0, 0);
					q2.addWait( time );
					//moveZ( q2, SERVOZ_PAC_POS );
					q2.addWithDefaultReader("K" + SERVOZ_PAC_POS);
					q2.addWait(110);

					//moveZDown( q2, true );
					q2.addWithDefaultReader("K" + SERVOZ_DOWN_POS);
					q2.addWait(SERVOZ_UP_TIME/3);
					z.disable(q2);
					lightManager.setLedsByBottle(q2, bottleNum, "11", 255, 255, 0, 0);
					return q2;
			//	}
			//	return null;
			}
		} );

		if(disableOnReady){
			q_ok.add("DX", true);
		}
		q_ok.add(Constant.GETXPOS, true);
	    q_ok.add(Constant.GETYPOS, true);
	    q_ok.add(Constant.GETZPOS, true);
	    lightManager.setLedsByBottle(q_ok, bottleNum, "22", 100, 0, 100, 0);

	    if(needGlass){
	    	Queue checkWeight = this.weight.check( q_ok, q_error );		// check weight - no less than on start
			q.add(checkWeight);
	    }else{
	    	q.add(q_ok);
	    }
	}
	// todo move to slot
	public int getPourTime( int num, int capacity ){			// 0 - 11
		int SERVOZ_POUR_TIME = state.getInt("SERVOZ_POUR_TIME", 0 );
		return capacity * SERVOZ_POUR_TIME;
	}
	public int getRepeatTime(int num, int capacity) {
		int time 		= state.getInt("SERVOY_REPEAT_TIME", 0 );
		int base_time	= time/ 20;
		return base_time * capacity;
	}
	public int getPacWaitTime(int num, int capacity) {
		int base_time 	= state.getInt("SERVOZ_PAC_TIME_WAIT", 0 );
		int time 		= state.getInt("SERVOZ_PAC_TIME_WAIT_VOL", 0 );
		return base_time + (time / 20 * capacity);
	}

	public void bottleBacklight(  Queue q, final int bottleNum, final int count ) {
		if(count == 0 ){
			lightManager.color_by_bottle( q, bottleNum, true, 0, 0, 0 );
		}else if(count == 1 ){
			lightManager.color_by_bottle( q, bottleNum, true, 0, 0, 255 );		// blue
		}else if( count == 2 ){
			lightManager.color_by_bottle( q, bottleNum, true, 0, 255, 0 );		// green
		}else if( count == 3 ){
			lightManager.color_by_bottle( q, bottleNum, true, 255, 255, 0 );		// red + green
		}else if( count == 4 ){
			lightManager.color_by_bottle( q, bottleNum, true, 255, 0, 255 );		// blue + red
		}else if( count == 5 ){
			lightManager.color_by_bottle( q, bottleNum, true, 100, 100,  100 );	// white
		}else{
			lightManager.color_by_bottle( q, bottleNum, true, 255, 255, 255 );	// all
		}
	}

	public void setSlotMarginX( int num, int newx ) {
		this.state.set("BOTTLE_OFFSETX_" + num, newx );	
	}

	public int getSlotMarginX(int num) {
		int margin = state.getInt("BOTTLE_OFFSETX_" + num, 0 );
		return margin;			
	}

	public int getRobotId() {
		//Initiator.logger.w("getRobotId is=",""+robot_id );		
		return robot_id;
	}

	public void changeRobotId( int new_robot_id, boolean readFromHardware ) {
		Initiator.logger.w("changeRobotId", ""+new_robot_id );
		if( new_robot_id > 0 && new_robot_id < 65535 ){				// 65535 = 1111111111111111b (empty eeprom)
			if(robot_id != new_robot_id){
				robot_id = new_robot_id;
				if(robot_id!=0 && robot_id!=65535){					// 65535 = 1111111111111111b (empty eeprom)
					this.state.saveConfig(robot_id);
				}
				this.state.reloadConfig(robot_id);
			}
			this.state.set("ROBOT_ID", new_robot_id );
			if(readFromHardware){
				this.robot_id_ready = true;
				this.robot_id_error = false;
			}
		}else{
			robot_id_error = true;
			this.robot_id_ready = false;
			this.state.set("ROBOT_ID", 0 );
			robot_id = 0;
		}
	}

	public void readHardwareRobotId( Queue q ) {
		q.addWithDefaultReader("m"+ Decoder.toHexByte(Methods.EEPROM_ROBOT_ID_HIGH));
		q.addWithDefaultReader("m"+ Decoder.toHexByte(Methods.EEPROM_ROBOT_ID_LOW));
		q.addWithDefaultReader("S");	// version and other stats
	}

	public void setRobotId( Queue q, int robot_id ) {	// 16bit max
		int robot_id_high	= (byte) ((robot_id >> 8) & 0xFF);
		int robot_id_low	= (byte) (robot_id & 0xFF);
		Initiator.logger.w("setRobotId", ""+robot_id + ", low: "+ robot_id_low + ", high: "+  robot_id_high );		
		q.add("M"+ Decoder.toHexByte(Methods.EEPROM_ROBOT_ID_HIGH) + Decoder.toHexByte(robot_id_high), true);
		q.add("M"+ Decoder.toHexByte(Methods.EEPROM_ROBOT_ID_LOW) + Decoder.toHexByte(robot_id_low), true);
		this.changeRobotId( robot_id, true );
	}

	public void setLastSeen(int millis) {
		java.util.Date date= new java.util.Date();
		lastSeenRobotTimestamp = new java.sql.Timestamp(date.getTime()).getTime();
	}

	public int getLastTemp() {
		return this.state.getInt("TEMPERATURE", 0);
	}
	public void systemTest() {
		// TODO Auto-generated method stub
	}	

	public boolean isAvailable() {
		if( lastSeenRobotTimestamp == 0 ){
			return false;
		}
		if( driver_x == null ){
			return false;
		}
		if( !robot_id_ready ){
			return false;
		}
		if( robot_id == 0 ){
			return false;
		}
		return true;
	}

	public void onConnected(Queue mq, boolean robotExistsForSure ) {
		Initiator.logger.w("barobot.onConnected", "start" );

		boolean oncePerAppStart		= state.getInt("ONCE_PER_APP_START", 0) == 0;
		boolean oncePerRobotStart	= state.getInt("ONCE_PER_ROBOT_START", 0) == 0;
		boolean oncePerRobotLife	= state.getInt("ONCE_PER_ROBOT_LIFE", 0) == 0;
		boolean can_move			= state.getInt("ROBOT_CAN_MOVE", 0) == 0;

		mq.add( new AsyncMessage( true ) {
			@Override
			public String getName() {
				return "Check robot start";
			}
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				BarobotConnector.this.setInitDone( false );
				return null;
			}
		});
		mq.add( "\n", false );	// clean up input
		mq.add( "\n", false );
		y.disable( mq );			// disable everything is moving
		z.disable( mq );
		mq.add("DX", true);
		if( ledsReady || newLeds ){
			lightManager.setAllLeds(mq, "22", 0, 100, 100, 0);
		}else{
			lightManager.scann_leds( mq );
		}
		mq.add(Constant.GETXPOS, true);		// get positions
		mq.add(Constant.GETYPOS, true);
		mq.add(Constant.GETZPOS, true);
		readHardwareRobotId(mq);
		mq.add( new AsyncMessage( true ) {
			@Override
			public String getName() {
				return "Check robot end";
			}
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				BarobotConnector.this.setInitDone( true );
				return null;
			}
		});
		if( oncePerRobotStart ){
			if(can_move){
				lightManager.setAllLeds(mq, "22", 0, 100, 0, 0);
				doHoming( mq, false );
			}
			lightManager.setAllLeds(mq, "44", 255, 0, 255, 0 );
			state.set("ONCE_PER_ROBOT_START", Constant.ANDROID_APP_VERSION );
		}
		if( oncePerRobotLife ){
			state.set("ONCE_PER_ROBOT_LIFE", Constant.ANDROID_APP_VERSION );
		}else{
		}
		if( oncePerAppStart ){
			state.set("ONCE_PER_APP_START", Constant.ANDROID_APP_VERSION );
		}
		if( oncePerRobotStart ){
			state.set("ONCE_PER_ROBOT_START", Constant.ANDROID_APP_VERSION );	
		}
		lightManager.setAllLeds(mq, "44", 255, 0, 100, 0 );
		Initiator.logger.w("barobot.onConnected", "end" );
	}

	protected void setInitDone(boolean ready ) {
		Initiator.logger.w("barobot.setInitDone", ""+ready );
		this.init_done = ready;
	}

	
	public class Y{
		public void move( Queue q, final int newpos, final boolean disableOnReady ) {
			BarobotConnector.this.y.readHallY(q);
			q.add( new AsyncMessage( true ){
				@Override	
				public String getName() {
					return "moveY logic" ;
				}
				@Override
				public Queue run(Mainboard dev, Queue queue) {
					Queue q4				= new Queue();
					int SERVOY_HYSTERESIS	= BarobotConnector.this.state.getInt("SERVOY_HYSTERESIS", 30 );
					int posy				= BarobotConnector.this.state.getInt("POSY", 0 );
					int DRIVER_Y_SPEED		= BarobotConnector.this.state.getInt("DRIVER_Y_SPEED", 0 );
				//	if( posy != newpos && posy != newpos -margin && posy != newpos +margin){
						q4.add("Y" + newpos+ ","+DRIVER_Y_SPEED, true);
						q4.addWait(120);
						if( newpos > posy ){	// forward
							q4.add("Y" + (newpos - SERVOY_HYSTERESIS)+ ","+DRIVER_Y_SPEED, true);	
						}else{	// backwad
							q4.add("Y" + (newpos + SERVOY_HYSTERESIS)+ ","+DRIVER_Y_SPEED, true);	
						}
						if(disableOnReady){
							q4.addWait(100);
							BarobotConnector.this.y.readHallY(q4);
							q4.add("DY", true );
						}
				//	}
					return q4;
				}
			});
			BarobotConnector.this.y.readHallY(q);
		}

		public void move( Queue q, String newpos ) {
			int newpos9 = Integer.parseInt(newpos);
			move(q, newpos9, false);
		}
		public void moveToFront(Queue q2) {
			int posy				= BarobotConnector.this.state.getInt("POSY", 0 );			// current pos
			int SERVOY_FRONT_POS	= BarobotConnector.this.state.getInt("SERVOY_FRONT_POS", 0 );
			int SERVOY_HYSTERESIS	= BarobotConnector.this.state.getInt("SERVOY_HYSTERESIS", 30 );
			if( Math.abs(posy - SERVOY_FRONT_POS) > SERVOY_HYSTERESIS ){
				move( q2, SERVOY_FRONT_POS, true );
			}
		}
		public void disable(Queue q) {
			q.add("DY", true);
			q.addWait(300);
		}
		public void readHallY(Queue q) {
			q.add("A1", true);
		}
	}

	public class Z{
		private boolean isBottle(int hallState, int bottleNum, boolean defaultRet) {
			if(bottleNum >= 0 ){
				int expected = Constant.bottle_row[bottleNum];			// expect front of bottom
				if( hallState == Methods.HX_STATE_7 && expected == Constant.BOTTLE_IS_FRONT){		// front bottle is over the carriage 
					Initiator.logger.e("moveZUp.hallx1", ""+hallState + ", expected: "+ expected );
					return true;
				}else if( hallState ==Methods.HX_STATE_3 && expected == Constant.BOTTLE_IS_BACK){	// back bottle is over the carriage 
					Initiator.logger.e("moveZUp.hallx2", ""+hallState + ", expected: "+ expected );
					return true;
				}else if( bottleNum == 11 && hallState == Methods.HX_STATE_1 && expected == Constant.BOTTLE_IS_FRONT){	// the last bottle is diffrent
					Initiator.logger.e("moveZUp.hallx3", ""+hallState + ", expected: "+ expected );
					return true;
				}else{		// error ???
					Initiator.logger.e("moveZUp.hallx4", "bottle: " + bottleNum +", no bottle over, state: "+hallState + ", expected: "+ expected );
					return false;
				}
			}else{
				Initiator.logger.w("moveZUp.hallx5", ""+hallState + ", allowUP:" +bottleNum );
				return true;
			}
		}

		private boolean isBelowBottle(String result, int bottleNum) {
			if(result.matches("^" +  Methods.METHOD_IMPORTANT_ANALOG + ",0,.*" )){	// 125,0,100,0,0,0,255,202,126,1
				int[] parts			= Decoder.decodeBytes( result );
				final int NEED_HALL_X= state.getInt("NEED_HALL_X", 1 );
				boolean defaultRet	 = (NEED_HALL_X == 1 ? false : true );
				boolean allowUp		 = isBottle( parts[2], bottleNum, defaultRet );

				// hall ?
				int hallY = state.getInt("HALLY", 0);
				Initiator.logger.w("moveZUp.allowUp.hallY", ""+hallY );

				Initiator.logger.w("moveZUp.allowUp.hallX", ""+(allowUp? "tak" : "nie") );
				if( defaultRet || allowUp ){
					return true;
				}
			}
			return false;
		}

		public void moveLight(Queue q, final int bottleNum, final boolean disableOnReady) {
			q.add("A1", true);  // hall Y
			q.add( new AsyncMessage( "A0", true ) {// check i'm over endstop (neodymium magnet)
				@Override
				public String getName() {
					return "Check magnet before Z LIGHT UP";
				}
				@Override
				public boolean isRet(String result, Queue mainQueue) {
					Queue	q2			= new Queue(); 
					boolean correctRet = isBelowBottle(result, bottleNum);
					if(correctRet){
						int SERVOZ_UP_LIGHT_POS	= state.getInt("SERVOZ_UP_LIGHT_POS", 0 );
						int poszup				=  SERVOZ_UP_LIGHT_POS;
						z.move(q2, poszup);
					}
					if(disableOnReady){
						z.disable(q2);
					}
					mainQueue.addFirst(q2);
					return false;
				}
				@Override
				public boolean onInput(String input, Mainboard dev, Queue mainQueue) {
					if(input.equals("RA0")){		// its me
						return true;
					}else{
						return false;
					}
				}
			});
		}

		public void moveDown(Queue q, final boolean disableOnReady ) {
			int SERVOZ_DOWN_POS = state.getInt("SERVOZ_DOWN_POS", 0 );
			z.move( q, SERVOZ_DOWN_POS );
			if(disableOnReady){
				z.disable(q);
			}
			/*
			q.add( new AsyncMessage( true ){
				@Override	
				public String getName() {
					return "moveZDown logic" ;
				}
				@Override
				public Queue run(Mainboard dev, Queue queue) {
					Queue q4 = new Queue();
				//	int py =  state.getInt("POSZ", 0 );
					int SERVOZ_DOWN_POS = state.getInt("SERVOZ_DOWN_POS", 0 );
				//	if( py != SERVOZ_DOWN_POS ){
						moveZ( q4, SERVOZ_DOWN_POS );
						if(disableOnReady){
							disablez(q4);
						}
				//	}
					return q4;
				}
			});*/
		}

		public void move(Queue q, final int pos) {
			q.add( new AsyncMessage( true ){
				@Override	
				public String getName() {
					return "moveZDown logic" ;
				}
				@Override
				public Queue run(Mainboard dev, Queue queue) {
					Queue q				= new Queue();
					int current_z		= BarobotConnector.this.state.getInt("POSZ", 0 );
					int diff			= Math.abs((current_z - pos));
				//	int DRIVER_Z_SPEED	= state.getInt("DRIVER_Z_SPEED", 0 );
					q.addWithDefaultReader("K" + pos);
					int SERVOZ_UP_TIME		= BarobotConnector.this.state.getInt("SERVOZ_UP_TIME", 10 );
					int SERVOZ_UP_TIME_MIN	= BarobotConnector.this.state.getInt("SERVOZ_UP_TIME_MIN", 10 );
					int time				= SERVOZ_UP_TIME / 1000 * diff;
					time					= Math.max( SERVOZ_UP_TIME_MIN, time );	// no less than 100
				//	time					= Math.min( 800, time );
					Initiator.logger.i("BarobotConnector.moveZ","timer move z:"+time +" current_z:"+ current_z+ " pos: " + pos+ " diff: "+ diff);
					q.addWait( time );
					return q;
				}
			});
		}
		public void moveUp( Queue q, final int bottleNum, final boolean disableOnReady ) {
			q.add("A1", true);  // hall Y
			q.add( new AsyncMessage( "A0", true ) {			// check i'm over endstop (neodymium magnet)
				@Override
				public String getName() {
					return "Check magnet before Z UP";
				}
				@Override
				public boolean isRet(String result, Queue mainQueue) {
					boolean correctRet = isBelowBottle(result, bottleNum);
					Queue	q2			= new Queue();
					if(correctRet){
						int SERVOZ_UP_POS	= BarobotConnector.this.state.getInt("SERVOZ_UP_POS", 0 );
						move(q2, SERVOZ_UP_POS);
					}
					if(disableOnReady){
						BarobotConnector.this.z.disable(q2);
					}
					mainQueue.addFirst(q2);
					return false;
				}
				@Override
				public boolean onInput(String input, Mainboard dev, Queue mainQueue) {
					if(input.equals("RA0")){		// its me
						return true;
					}else{
						return false;
					}
				}
			});
		}
		public void disable(Queue q) {
			q.add("DZ", true);
			q.addWait(300);
		}
	}
	public class Weight{	
		private int weight_grow = 0;
		private int weight_stay_high = 0;
		private int local_min = 0;
		private int removedDiff = 50;

		public static final int EMPTY = 1;
		public static final int MAYBE_GLASS =2;
		public static final int NEW_GLASS = 32;
		public static final int GLASS = 64;
		
		int down_limit = 1000;
		int maybe_counter = 0;
		int min_diff_down = 0;
		int start_level = 1000;
		
		public int tray = EMPTY;

		Map<Long, List<Integer>> aMap = new HashMap<Long, List<Integer>>();

		public void start(){
	//		state.set("START_WEIGHT", 1 );
			aMap.clear();
		}
		public Queue check(final Queue q_ok, final Queue q_error) {
			boolean watch = state.getInt("WATCH_GLASS", 1) > 0;
			if(!watch){
				return q_ok;
			}
			Queue q = new Queue();
			q.add("A2", true );			// read load cell
			q.add( new AsyncMessage( true ) {
				@Override	
				public String getName() {
					return "check weight";
				}
				@Override
				public Queue run(Mainboard dev, Queue queue) {
					int start_weight	= state.getInt("START_WEIGHT", 1);
					int last_weight		= state.getInt("LAST_WEIGHT", 1);
			//		int min_weight 		= state.getInt( "WEIGHT_MIN", 1000 );
					int weight_state 	= state.getInt( "WEIGHT_STATE", 0 );
					int start_glass	 	= state.getInt( "START_GLASS_WEIGHT", 1000 );
					Initiator.logger.i("Weight.check","min: "+ (start_weight + (start_glass / 2)) );
				//	int  minDiff = Math.max((last_glass / 2), min_weight );
					if( last_weight < start_weight + (start_glass / 2) ){		// less than with glass
						return q_error;
					}
					return q_ok;
				}
			} );
			return q;
		}
		public void stop(){
			state.set("START_WEIGHT", 0 );
		}
		public boolean isGlassReady(){
		//	return false;
			return (tray == NEW_GLASS || tray == GLASS );
		}
		public void newValue( int weight ){
			int prev_weight		= state.getInt( "LAST_WEIGHT", 1 );
			int allow_light_cup	= state.getInt( "ALLOW_LIGHT_CUP", 1 );
			int min_weight 		= state.getInt( "WEIGHT_MIN", 1000 );
			int zero 			= state.getInt( "WEIGHT_WITHOUT_TRAY", 0 );
			int withtray		= state.getInt( "WEIGHT_WITH_TRAY", 0 );
			int min_diff_up		= 1000;
			if( allow_light_cup == 1 ){
				min_diff_up	= state.getInt("LIGH_GLASS_DIFF", 5 );	
			}else{
				min_diff_up	= state.getInt("GLASS_DIFF", 5 );		
			}
			
			state.set( "LAST_WEIGHT", weight );
		//	addValue( weight );

			if( weight <= zero ){		// no glass
				tray = EMPTY;
				start_level = weight;
			}else if(tray == MAYBE_GLASS && weight < down_limit ){
				tray = EMPTY;
				start_level = weight;
			}else{
				if( start_level < prev_weight - min_diff_down ){			// zdjeto szklankę
					tray = EMPTY;
					start_level = weight;
					state.set("WEIGHT_MIN", weight );
				}
				if( weight > prev_weight + min_diff_up ){			// dodano szklankę lub dolano cieczy	
					min_diff_down = Math.max(min_diff_up , ((weight - prev_weight)*3 / 4)); 
					if(tray == MAYBE_GLASS ){
						tray = NEW_GLASS;
						start_level = weight;
						state.set( "LAST_GLASS_WEIGHT", weight - Math.max(withtray,min_weight) );
					}else if(tray == EMPTY ){
						tray = MAYBE_GLASS;
						down_limit = weight-min_diff_up/2;
						maybe_counter = 0;
						start_level = weight;
					}else if(tray == NEW_GLASS ){
						tray = GLASS;
						start_level = weight;
						state.set( "LAST_GLASS_WEIGHT", weight - Math.max(withtray,min_weight) );
					}
				}
				if( tray == MAYBE_GLASS ){
					maybe_counter++;
					if( maybe_counter > 5 ){			// next 5 measures
						tray = NEW_GLASS;
						start_level = weight;
					}
				}
			}
			state.set( "WEIGHT_STATE", tray );

/*
			int start_weight = state.getInt( "START_WEIGHT", 0 );
			if( start_weight > 0 ){
				int confirm_glass	= 3;
				Initiator.logger.w("MyRetReader.new_weight", "add: "+ (weight - prev_weight)+ ", weight:" + weight+ ", prev_weight:" + prev_weight+ ", min_weight:" + min_weight );
				local_min			= Math.min(local_min, weight );
				if( weight < min_weight ){
					state.set("WEIGHT_MIN", (weight + min_weight * 3) / 4 );
					Initiator.logger.w("MyRetReader.new_weight", "new WEIGHT_MIN" );
					weight_grow = 0;
					weight_stay_high = 0;
				}else if( prev_weight - weight > min_diff ){			// less than the last one - no glass?
					Initiator.logger.w("MyRetReader.new_weight", "less than PREV" );
					weight_grow = 0;
					weight_stay_high = 0;
				}else if( weight - prev_weight > min_diff ){			// more than the last one
					Initiator.logger.w("MyRetReader.new_weight", "more than PREV" );
					weight_grow++;
				}else if( weight - min_weight > min_diff ){				// more than the empty try
					Initiator.logger.w("MyRetReader.new_weight", "more than MIN" );
					if( weight_grow > 0 ){
						if(weight_stay_high == 0 ){
							weight_stay_high +=weight_grow;
						}
						weight_stay_high++;
						if(weight_stay_high > confirm_glass ){
							Initiator.logger.e("MyRetReader.new_weight", "this is glass" + weight_stay_high );
				//			lightManager.carret_color(main_queue, 00, 33, 0);
						}
					}
					if( weight_grow == 0 && weight_stay_high == 0 ){
						Initiator.logger.e("MyRetReader.new_weight", "no glass" );
			//			lightManager.carret_color(main_queue, 33, 00, 0);
					}
				}else{		// no changes, less than glass min_diff
				}
			}*/
		}
		public Long addValue( int weight ){
			Long time = Decoder.getTimestamp()/1000;
			if(aMap.containsKey(time)){
				List<Integer> array = aMap.get(time);
				array.add(weight);
			}else{
				List<Integer> array = new ArrayList<Integer>();
				aMap.put(time, array);
			}
			return time;
		}
		public int sum( Long time ){
			if(aMap.containsKey(time)){
				int sum = 0;
				List<Integer> array = aMap.get(time);
				for (Integer c : array) {
		            sum +=c;
		        }
				return sum / array.size();
			}else{
				return -1;
			}
		}
		public void waitForGlass(Queue q, final Queue q_ready, final Queue q_error) {
			final long timestamp	= Decoder.getTimestamp();
			final int max_time 		= state.getInt("WEIGHT_TIME_MAX", 7000 ) ;
			weight.start();
			final Queue q2			= new Queue();
			q2.add("A2", true );	// read load cell
			AsyncMessage am = new AsyncMessage( true ) {
				@Override
				public String getName() {
					return "Check load cell";
				}
				@Override
				public Queue run(Mainboard dev, Queue mainQueue) {
					boolean igr		= weight.isGlassReady();
					long timestamp2 = Decoder.getTimestamp();
					if ( igr ){
						weight.stop();
						Initiator.logger.i("cupFound"," true ");

						int glass_weight = state.getInt( "LAST_GLASS_WEIGHT", 1 );
						state.set( "START_GLASS_WEIGHT", glass_weight );

						return q_ready;
					}else if( timestamp2 - timestamp > max_time ){
						weight.stop();
						Initiator.logger.i("cupFound"," false ");
						return q_error;
					}else{			// one more time	
						Queue beforeOtherInMainQueue = new Queue();
						int phase = state.getInt("WEIGHT_PHASE", 0 ) ;
						if(phase%10 == 0 ){
							lightManager.carret_color_left( beforeOtherInMainQueue, 0, 5, 0);
							lightManager.carret_color_right( beforeOtherInMainQueue, 0, 100, 0);
						}else if(phase%10 == 5 ){
							lightManager.carret_color_left( beforeOtherInMainQueue, 0, 100, 0 );
							lightManager.carret_color_right( beforeOtherInMainQueue, 0, 5, 0 );
						}
						phase++;
						state.set("WEIGHT_PHASE", phase) ;

						beforeOtherInMainQueue.add(q2.copy());
						return beforeOtherInMainQueue;		// do it one more time (now with addWait())
					}
				}
			};
			q2.add(am);				// check value
			Queue copy = q2.copy();	// do copy 
			q.add(copy);			// this queue will be send
		}
		public boolean isGlassRequired() {
			return ("1".equals(state.get("NEED_GLASS", "0" ))) ;		// if NEED_GLASS == 1
		}
	}
}
/*
AsyncCondition ss = new AsyncCondition("Check magnet before Z UP"){
	public boolean doBefore(){
		return true;
	}
	public boolean condition(){
		return true;
	}
	@Override
	public void onTrue(){	
	}
	@Override
	public void onFalse(){
	}
	@Override
	public void onError( String ret, int errorCode){
	}
};
q.add(ss);
*/
