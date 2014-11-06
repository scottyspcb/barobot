package com.barobot.hardware.devices;

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
	public boolean use_beta		= false;
	public boolean robot_id_ready	= false;	
	public long lastSeenRobotTimestamp	= 0;
	public LightManager lightManager	= null;
	public boolean init_done			= false;
	
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
		this.driver_x.defaultSpeed = state.getInt("DRIVER_X_SPEED", 2900 );
	}

	private void loadDefaults() {
		this.state.set( "STAT1", this.state.getInt( "STAT1", 0 ) + 1 );				// app starts
		this.state.set( "RESET_TIME", this.state.getInt( "RESET_TIME", 200 ) );		// set default
		this.state.set( "NEED_HALL_X", this.state.getInt( "NEED_HALL_X", 1 ) );		// set default

		this.state.set( "GLASS_DIFF", this.state.getInt( "GLASS_DIFF", 100 ) );		// set default
		this.state.set( "LIGH_GLASS_DIFF", this.state.getInt( "LIGH_GLASS_DIFF", 3 ) );		// set default
		this.state.set( "ALLOW_LIGHT_CUP", this.state.getInt( "ALLOW_LIGHT_CUP", 0 ) );		// set default

		boolean defaultsLoaded	= this.state.getInt("DEFAULTS", 0 ) > 0;
		if( use_beta || !defaultsLoaded ){
			if(newLeds){
				// changed in wizard:
				state.set( "SERVOY_FRONT_POS", 890 );		
				state.set( "SERVOZ_UP_POS", 1400 );
				state.set( "SERVOZ_DOWN_POS", 2300 );	
				state.set( "SERVOY_BACK_POS", 1820 );

				// rest
				state.set( "SERVOY_HFRONT_POS", 60 );	// add histeresis
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
			state.set( "SERVOY_REPEAT_TIME", 1000 );
			state.set( "SERVOZ_POUR_TIME", 3000 / 20 );			// predkosc nalewania 20ml, dac 3200
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
			//	Log.e("Serial addOnReceive", message);
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
		mb					= null;
		driver_x			= null;
	//	driver_y			= null;
	//	driver_z			= null;
		state				= null;
		main_queue  		= null;
		i2c					= null;
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

	public void moveY( Queue q, final int newpos, final boolean disableOnReady ) {
		this.readHallY(q);
		q.add( new AsyncMessage( true ){
			@Override	
			public String getName() {
				return "moveY logic" ;
			}
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				Queue q4				= new Queue();
				int SERVOY_HYSTERESIS	= state.getInt("SERVOY_HYSTERESIS", 30 );
				int posy				= state.getInt("POSY", 0 );
				int DRIVER_Y_SPEED		= state.getInt("DRIVER_Y_SPEED", 0 );
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
						readHallY(q4);
						q4.add("DY", true );
					}
			//	}
				return q4;
			}
		});
		this.readHallY(q);
	}

	public void moveY( Queue q, String newpos ) {
		int newpos9 = Integer.parseInt(newpos);
		this.moveY(q, newpos9, false);
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

	public void moveZUp( Queue q, final int bottleNum, final boolean disableOnReady ) {
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
					int SERVOZ_UP_POS	= state.getInt("SERVOZ_UP_POS", 0 );
					moveZ(q2,SERVOZ_UP_POS);
				}
				if(disableOnReady){
					disablez(q2);
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

	public void moveZLight(Queue q, final int bottleNum, final boolean disableOnReady) {
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
					moveZ(q2,poszup);
				}
				if(disableOnReady){
					disablez(q2);
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
	public void moveZ(Queue q, int pos) {
		int current_z		= state.getInt("POSZ", 0 );
		int diff			= Math.abs((current_z - pos));
	//	int DRIVER_Z_SPEED	= state.getInt("DRIVER_Z_SPEED", 0 );
		q.addWithDefaultReader("K" + pos);

		int SERVOZ_UP_TIME	= state.getInt("SERVOZ_UP_TIME", 10 );
		int SERVOZ_UP_TIME_MIN	= state.getInt("SERVOZ_UP_TIME_MIN", 10 );
		int time			= SERVOZ_UP_TIME / 1000 * diff;
		time				= Math.max( SERVOZ_UP_TIME_MIN, time );	// no less than 100
	//	time				= Math.min( 800, time );

		Initiator.logger.i("BarobotConnector.moveZ","timer move z:"+time +" current_z:"+ current_z+ " pos: " + pos+ " diff: "+ diff);
		// no more than 800
		q.addWait( time );
	}
	public void moveZDown(Queue q, final boolean disableOnReady ) {
		int SERVOZ_DOWN_POS = state.getInt("SERVOZ_DOWN_POS", 0 );
		moveZ( q, SERVOZ_DOWN_POS );
		if(disableOnReady){
			disablez(q);
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

	public void kalibrcja(Queue q) {
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
		moveZDown( q ,false );
		q.addWait(5);
		int SERVOZ_TEST_POS = state.getInt("SERVOZ_TEST_POS", 0 );
		moveZ( q, SERVOZ_TEST_POS );

		q.addWait(10);
		moveZDown( q ,true );
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
				driver_x.defaultSpeed = state.getInt("DRIVER_CALIB_X_SPEED", 0 );
				Initiator.logger.i("+find_bottles", "up");
				state.set("scann_bottles", 1 );
				return null;
			}
		});
		q.addWait( 100 );
		driver_x.moveTo( q, 20000 );		// go down
		q.addWait( 200 );
		q.add( new AsyncMessage( true ) {
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				this.name		= "scanning back";
				driver_x.defaultSpeed = state.getInt("DRIVER_X_SPEED", 0 );
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
		disabley( q );
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
				moveZDown(q2, true );				
				/*int SERVOZ_DOWN_POS = state.getInt("SERVOZ_DOWN_POS", 0 );
				if( posz != SERVOZ_DOWN_POS ){
					moveZDown(q2, true );
				}*/
				moveToFront( q2 );
				if(posx != sposx ){
					driver_x.moveTo( q2, sposx);
					doHoming( q2, false );
				}
				disablez(q2);
				return q2;
			}
		} );
	    q.add(Constant.GETXPOS, true);
	    q.add(Constant.GETYPOS, true);
	    q.add(Constant.GETZPOS, true);
	}

	public void moveToFront(Queue q2) {
		int posy				= state.getInt("POSY", 0 );			// current pos
		int SERVOY_FRONT_POS	= state.getInt("SERVOY_FRONT_POS", 0 );
		int SERVOY_HYSTERESIS	= state.getInt("SERVOY_HYSTERESIS", 30 );

		if( Math.abs(posy - SERVOY_FRONT_POS) > SERVOY_HYSTERESIS ){
			moveY( q2, SERVOY_FRONT_POS, true );
		}
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
						moveY( q2, SERVOY_TEST_POS, true);
						q2.addWait(100);
						moveY( q2, SERVOY_FRONT_POS, true);
						q2.addWait(100);
					}
					//disabley( q2 );

//					int lengthx19	=  state.getInt("LENGTHX", 60000 );
				//	Initiator.logger.i("+find_bottles", "up");
					moveZDown( q2 ,true );
					if( always || need_homing ){
						if(!thisIsMax){
							int newSpos = driver_x.getSPos() + 100;
							driver_x.moveTo(q2, newSpos);
							//int poshx = driver_x.getHardwarePos();
						//	q2.add("X"+ (poshx+100) +","+ BarobotConnector.this.driver_x.defaultSpeed, true);	// +100
						//	q2.addWait(10);
						}
					}
					if( always ){
						q2.add("X-10000,"+ BarobotConnector.this.driver_x.defaultSpeed, true);
						q2.addWait(100);
						q2.addWithDefaultReader( "IH" );		// reset hardware X pos
						mainQueue.addFirst(q2);
					}else if( need_homing ){
						q2.add("X-10000,"+ BarobotConnector.this.driver_x.defaultSpeed, true);
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
		moveZLight(q, -1, false );					// move up to help
	//	q.addWait(400);
		q.add("DX", true);
		disabley( q );
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
		//		Initiator.logger.i("moveToBottle","(cx == tx && cy == ty)("+cx+" == "+tx+" && "+cy+" == "+ty+")");
				moveZDown(q2, true );
				int SERVOY_FRONT_POS = state.getInt("SERVOY_FRONT_POS", 0 );
				int SERVOY_HFRONT_POS = SERVOY_FRONT_POS + state.getInt("SERVOY_HFRONT_POS", 0 );
				if(cx == tx && cy == ty ){				// not needed
			//		q2.addWait( Constant.SERVOY_REPEAT_TIME );
				}else if(cx != tx && cy == ty && ty <= SERVOY_HFRONT_POS ){	// change X, Y = front
					driver_x.moveTo( q2, tx);

				}else if(cx != tx && cy != ty && ty <= SERVOY_HFRONT_POS  ){	// change X and Y and target = front
					moveY( q2, ty, disableOnReady );
					driver_x.moveTo( q2, tx );

				}else if(cx != tx && cy < ty && cy <= SERVOY_HFRONT_POS  ){	// change X and Y and current = front, target = back
					driver_x.moveTo( q2, tx );
					moveY( q2, ty, disableOnReady );

				}else if(cx == tx && cy != ty ){		// change Y
					moveY( q2, ty, disableOnReady );	

				}else{									// (change X and Y ) or (change X and Y is back)
					int SERVOY_BACK_NEUTRAL = state.getInt("SERVOY_BACK_NEUTRAL", 0 );
					moveY( q2, SERVOY_BACK_NEUTRAL, true);
					driver_x.moveTo( q2, tx );
					moveY( q2, ty, disableOnReady );
				}
				lightManager.color_by_bottle(q2, num, true, 0, 255, 0);
				return q2;
			}
		} );
		//disabley( q );
	    q.add(Constant.GETXPOS, true);
	    q.add(Constant.GETYPOS, true);
	    q.add(Constant.GETZPOS, true);
	}

	public void pour( Queue q, final int capacity, final int bottleNum, boolean disableOnReady ) {			// num 0-11
		int time = getPourTime(bottleNum, capacity);
	//	q.add("A2", true);		// load cell
		q.add("EX", true);

		/*
		if( bottleNum != -1 ){
			int ty  	= getBottlePosY( bottleNum );
			moveY( q, ty, false );
		}
		disabley( q );*/

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
		moveZUp(q, bottleNum, false);
		this.lightManager.color_by_bottle(q, bottleNum, true, 0, 255, 0);
		q.addWait( time/4 );

	//	moveZLight(q, bottleNum, false);

		this.lightManager.setLedsByBottle(q, bottleNum, "04", 0, 0, 0, 0, true);
		q.addWait( time/4 );

		this.lightManager.setLedsByBottle(q, bottleNum, "04", 255, 0, 255, 0, true);
		q.addWait( time/4 );

		this.lightManager.setLedsByBottle(q, bottleNum, "04", 0, 0, 0, 0, true);	
		q.addWait( time/4 );

		this.lightManager.setLedsByBottle(q, bottleNum, "04", 255, 255, 255, 200, true);

		moveZDown(q,false);

		q.add( new AsyncMessage( true ) {
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

					lightManager.setLedsByBottle(q2, bottleNum, "11", 100, 100, 0, 0, true);
					q2.addWait( time );
					//moveZ( q2, SERVOZ_PAC_POS );
					q2.addWithDefaultReader("K" + SERVOZ_PAC_POS);
					q2.addWait(110);

					//moveZDown( q2, true );
					q2.addWithDefaultReader("K" + SERVOZ_DOWN_POS);
					q2.addWait(SERVOZ_UP_TIME/3);
					disablez(q2);
					lightManager.setLedsByBottle(q2, bottleNum, "11", 255, 255, 0, 0, true);
					return q2;
			//	}
			//	return null;
			}
		} );

		if(disableOnReady){
			q.add("DX", true);
		}
	    q.add(Constant.GETXPOS, true);
	    q.add(Constant.GETYPOS, true);
	    q.add(Constant.GETZPOS, true);
	    lightManager.setLedsByBottle(q, bottleNum, "22", 100, 0, 100, 0, true);
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
	public void startDemo() {
		if(this.ledsReady){
			lightManager.startDemo();
		}
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
		if(robot_id != new_robot_id){
			robot_id = new_robot_id;
			if(robot_id!=0 && robot_id!=65535){		// 65535 = 1111111111111111b (empty eeprom)
				this.state.saveConfig(robot_id);
			}
			this.state.reloadConfig(robot_id);
		}
		this.state.set("ROBOT_ID", new_robot_id );
		if(readFromHardware){
			this.robot_id_ready = true;
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

	
	private boolean isBelowBottle(String result, int bottleNum) {
		if(result.matches("^" +  Methods.METHOD_IMPORTANT_ANALOG + ",0,.*" )){	// 125,0,100,0,0,0,255,202,126,1
			int[] parts			= Decoder.decodeBytes( result );
			final int NEED_HALL_X= state.getInt("NEED_HALL_X", 1 );
			boolean defaultRet	 = (NEED_HALL_X == 1 ? false : true );
			boolean allowUp		 = isBottle( parts[2], bottleNum, defaultRet );
			Initiator.logger.w("moveZUp.allowUp", ""+(allowUp? "tak" : "nie") );
			if( defaultRet || allowUp ){
				return true;
			}
		}
		return false;
	}
	private static boolean isBottle(int hallState, int bottleNum, boolean defaultRet) {
		if(bottleNum >= 0 ){
			int expected = Constant.bottle_row[bottleNum];			// expect front of bottom
			if( hallState == Methods.HX_STATE_7 && expected == Constant.BOTTLE_IS_FRONT){		// front bottle is over the carriage 
				Initiator.logger.e("moveZUp.hallx", ""+hallState + ", expected: "+ expected );
				return true;
			}else if( hallState ==Methods.HX_STATE_3 && expected == Constant.BOTTLE_IS_BACK){	// back bottle is over the carriage 
				Initiator.logger.e("moveZUp.hallx", ""+hallState + ", expected: "+ expected );
				return true;
			}else if( bottleNum == 11 && hallState == Methods.HX_STATE_1 && expected == Constant.BOTTLE_IS_FRONT){	// the last bottle is diffrent
				Initiator.logger.e("moveZUp.hallx", ""+hallState + ", expected: "+ expected );
				return true;
			}else{		// error ???
				Initiator.logger.e("moveZUp.hallx", "bottle: " + bottleNum +", no bottle over, state: "+hallState + ", expected: "+ expected );
				return false;
			}
		}else{
			Initiator.logger.w("moveZUp.hallx", ""+hallState + ", allowUP:" +bottleNum );
			return defaultRet;
		}
	}

	public int getLastTemp() {
		return this.state.getInt("TEMPERATURE", 0);
	}
	public void disabley(Queue q) {
		q.add("DY", true);
		q.addWait(300);
	}
	public void disablez(Queue q) {
		q.add("DZ", true);
		q.addWait(300);
	}
	public void readHallY(Queue q) {
		q.add("A1", true);
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
		disabley( mq );			// disable everything is moving
		disablez( mq );
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
}
