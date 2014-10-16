package com.barobot.hardware.devices;

import com.barobot.common.Initiator;
import com.barobot.common.constant.Constant;
import com.barobot.common.constant.Methods;
import com.barobot.common.interfaces.HardwareState;
import com.barobot.common.interfaces.serial.CanSend;
import com.barobot.common.interfaces.serial.SerialInputListener;
import com.barobot.common.interfaces.serial.Wire;
import com.barobot.hardware.devices.i2c.Carret;
import com.barobot.hardware.devices.i2c.I2C;
import com.barobot.hardware.devices.i2c.Upanel;
import com.barobot.parser.Queue;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.message.Mainboard;
import com.barobot.parser.utils.Decoder;

public class BarobotConnector {
	public boolean ledsReady = false;
	public static boolean pureCrystal = false;
	public MotorDriver driver_x	= null;
	public Mainboard mb			= null;
	public Queue main_queue		= null;
	public Servo driver_y		= null;
	public Servo driver_z		= null;
	public HardwareState state	= null;
	public I2C i2c				= null;
	protected int robot_id		= 0;
	public boolean newLeds		= true;
	public boolean use_beta		= true;	

	public BarobotConnector(HardwareState state ){
		this.state		= state;
		if( newLeds ){
	//		robot_id = 2;
	//		state.reloadConfig(robot_id);
		}
		this.loadDefaults();
		this.mb			= new Mainboard( state );
		this.driver_x	= new MotorDriver( state );
		this.driver_y	= new Servo( state, "Y" );
		this.driver_z	= new Servo( state, "Z" );
		this.main_queue = new Queue( mb );
		this.i2c  		= new I2C();
		this.robot_id	= state.getInt("ROBOT_ID", 0 );
		this.driver_x.defaultSpeed = state.getInt("DRIVER_X_SPEED", 2900 );
	}

	private void loadDefaults() {
		this.state.set( "STAT1", this.state.getInt( "STAT1", 0 ) + 1 );				// app starts
		this.state.set( "RESET_TIME", this.state.getInt( "RESET_TIME", 200 ) );		// set default
		this.state.set( "NEED_HALL_X", this.state.getInt( "NEED_HALL_X", 1 ) );		// set default
		if(newLeds){
			if(this.state.getInt("DEFAULTS", 0 ) == 0 ){
				this.state.set( "SERVOZ_PAC_POS", 1500 );
				this.state.set( "SERVOZ_UP_POS", 1250 );
				this.state.set( "SERVOZ_UP_LIGHT_POS", 1300 );

				this.state.set( "SERVOZ_UP_LIGHT_TIME", 800 );
				this.state.set( "SERVOZ_DOWN_POS", 2300 );
				this.state.set( "SERVOZ_TEST_POS", 2000 );

				this.state.set( "SERVOY_FRONT_POS", 750 );
				this.state.set( "SERVOY_HFRONT_POS", 750 +70 );	// add histeresis

				this.state.set( "SERVOY_BACK_POS", 1700 );
				this.state.set( "SERVOY_TEST_POS", 1000 );
				this.state.set( "SERVOY_BACK_NEUTRAL", 1600 );

				this.state.set( "DRIVER_CALIB_X_SPEED", 1000 );
				this.state.set( "DRIVER_X_SPEED", 3000 );
				this.state.set( "DRIVER_Y_SPEED", 20 );
				this.state.set( "DRIVER_Z_SPEED", 255 );

				this.state.set( "SERVOZ_POUR_TIME", 3200 / 20 );		// predkosc nalewania 20ml

				this.state.set( "SERVOY_REPEAT_TIME", 1000 );
				this.state.set( "SERVOZ_PAC_TIME_WAIT", 1300 );
				this.state.set( "SERVOZ_PAC_TIME_WAIT_VOL", 300 );
				this.state.set( "SERVOZ_UP_TIME", 400 );
			}
		}else{
			if(this.state.getInt("DEFAULTS", 0 ) == 0 ){
				this.state.set( "SERVOZ_PAC_POS", 1880 );
				this.state.set( "SERVOZ_UP_POS", 2100 );
				this.state.set( "SERVOZ_UP_LIGHT_POS", 2050 );
				this.state.set( "SERVOZ_DOWN_POS", 1250 );
				this.state.set( "SERVOZ_TEST_POS", 1300 );
	
				this.state.set( "SERVOY_FRONT_POS", 790 );
				this.state.set( "SERVOY_HFRONT_POS", 790 +20 );	// add histeresis
	
				this.state.set( "SERVOY_BACK_POS", 2200 );
				this.state.set( "SERVOY_TEST_POS", 1000 );
				this.state.set( "SERVOY_BACK_NEUTRAL", 1200 );
	
				this.state.set( "DRIVER_CALIB_X_SPEED", 1000 );
				this.state.set( "DRIVER_X_SPEED", 2900 );
				this.state.set( "DRIVER_Y_SPEED", 30 );
				this.state.set( "DRIVER_Z_SPEED", 255 );
	
				this.state.set( "SERVOZ_POUR_TIME", 3200 / 20 );		// predkosc nalewania 20ml

				this.state.set( "SERVOY_REPEAT_TIME", 1000 );
				this.state.set( "SERVOZ_PAC_TIME_WAIT", 1300 );
				this.state.set( "SERVOZ_PAC_TIME_WAIT_VOL", 300 );
				this.state.set( "SERVOZ_UP_TIME", 400 );
				this.state.set( "DEFAULTS", 1 );
			}
		}
		this.state.set( "SERVOZ_UP_TIME", 400 );
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
		mb					= null;
		driver_x			= null;
		driver_y			= null;
		driver_z			= null;
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
			mq.add("Z" + poszdown, false );
			mq.add("DX", false );
			mq.add("DY", false );
			mq.add("DZ", false );
			mq.add(Constant.GETXPOS, false );
		}

	public void moveY( Queue q, final int newpos, final boolean disableOnReady ) {
		q.add( new AsyncMessage( true ){
			@Override	
			public String getName() {
				return "moveY logic" ;
			}
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				Queue q4		= new Queue();
				int margin		= 30;
				int posy		=  state.getInt("POSY", 0 );
				int DRIVER_Y_SPEED = state.getInt("DRIVER_Y_SPEED", 0 );
			//	if( posy != newpos && posy != newpos -margin && posy != newpos +margin){
					q4.add("Y" + newpos+ ","+DRIVER_Y_SPEED, true);
					q4.addWait(100);
					if( newpos > posy ){	// forward
						q4.add("Y" + (newpos - margin)+ ","+DRIVER_Y_SPEED, true);	
					}else{	// backwad
						q4.add("Y" + (newpos + margin)+ ","+DRIVER_Y_SPEED, true);	
					}
					if(disableOnReady){
						q4.addWait(100);
						q4.add("DY", true );
					}
			//	}
				return q4;
			}
		});
	}

	public void moveY( Queue q, String newpos ) {
		int newpos9 = Integer.parseInt(newpos);
		this.moveY(q, newpos9, false);
	}

	public void hereIsStart( int posx, int posy) {
		//Initiator.logger.i(Constant.TAG,"zapisuje start:" +posx+ " " + posy );
		state.set("POS_START_X", posx );
		state.set("POS_START_Y", posy );
	}

	public int getBottlePosX( int num ) {
		return state.getInt("BOTTLE_X_" + num, 0 ) + this.getSlotMarginX( num );
	}

	public int getBottlePosY( int i ) {
		return state.getInt("BOTTLE_Y_" + i, 0 );
	}

	// zapisz ze tutaj jest butelka o danym numerze
	public void hereIsBottle(int num, int posx, int posy){
		//Initiator.logger.i(Constant.TAG,"zapisuje pozycje:"+ i + " " +posx+ " " + posy );
		state.set("BOTTLE_X_" + num, posx );
		state.set("BOTTLE_Y_" + num, posy );
	}
/*
	// zapisz ze tutaj jest butelka o danym numerze
	public void hereIsBottle(int i) {
		int posx		=  driver_x.getSPos();
		int posy		=  state.getInt("POSY", 0 );
	//	Initiator.logger.i(Constant.TAG,"zapisuje pozycje:"+ i + " " +posx+ " " + posy );
		state.set("BOTTLE_X_" + i, posx );
		state.set("BOTTLE_Y_" + i, posy );
	}
*/
	public void startDoingDrink( Queue q) {// homing
		carret_color(q, 255,0,0);
		q.add( "S", true );			// read temp
		setAllLeds( q, "40", 255, 0,0,255 );
		doHoming(q, false);
	}

	public void moveZUp( Queue q, final int bottleNum, final boolean disableOnReady ) {
		q.add( new AsyncMessage( "A0", true ) {// check i'm over endstop (neodymium magnet)
			@Override
			public String getName() {
				return "Check magnet before Z UP";
			}
			@Override
			public boolean isRet(String result, Queue mainQueue) {
				boolean correctRet = isBelowBottle(result, bottleNum);
				if(correctRet){
					Queue	q2			= new Queue(); 
					int SERVOZ_UP_POS	= state.getInt("SERVOZ_UP_POS", 0 );
					int DRIVER_Z_SPEED	= state.getInt("DRIVER_Z_SPEED", 0 );
					q2.add("Z" + SERVOZ_UP_POS+","+DRIVER_Z_SPEED, true);
					if(disableOnReady){
						int SERVOZ_UP_TIME	= state.getInt("SERVOZ_UP_TIME", 10 );
						q2.addWait( SERVOZ_UP_TIME );
						q2.add("DZ", true);
					}
					mainQueue.addFirst(q2);
				}
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
				boolean correctRet = isBelowBottle(result, bottleNum);
				if(correctRet){
					Queue	q2			= new Queue(); 
					int SERVOZ_UP_LIGHT_POS	= state.getInt("SERVOZ_UP_LIGHT_POS", 0 );
					int DRIVER_Z_SPEED		= state.getInt("DRIVER_Z_SPEED", 0 );
					int poszup				=  SERVOZ_UP_LIGHT_POS;
					q2.add("Z" + poszup+","+DRIVER_Z_SPEED, true);
					if(disableOnReady){
						int SERVOZ_UP_TIME	= state.getInt("SERVOZ_UP_TIME", 10 );
						q2.addWait(SERVOZ_UP_TIME);
						q2.add("DZ", true);
					}
					mainQueue.addFirst(q2);
				}
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
		int DRIVER_Z_SPEED = state.getInt("DRIVER_Z_SPEED", 0 );
		q.add("Z" + pos +","+DRIVER_Z_SPEED, true);
	}
	public void moveZDown(Queue q, final boolean disableOnReady ) {
		q.add( new AsyncMessage( true ){
			@Override	
			public String getName() {
				return "moveZDown logic" ;
			}
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				Queue q4 = new Queue();
				int py =  state.getInt("POSZ", 0 );
				int SERVOZ_DOWN_POS = state.getInt("SERVOZ_DOWN_POS", 0 );
				if( py != SERVOZ_DOWN_POS ){
					moveZ( q4, SERVOZ_DOWN_POS );
					if(disableOnReady){
						q4.addWait( 400 );
						q4.add("DZ", true);
					}
				}
				return q4;
			}
		});
	}

	public void kalibrcja(Queue q) {
		q.add( "\n", false );
		q.add( "\n", false );
		turnOffLeds(q);
//		int posx		= driver_x.getSPos();
		for(int i=0;i<12;i++){
			state.set("BOTTLE_X_" + i, "0" );
			state.set("BOTTLE_Y_" + i, "0" );
		}
		this.readHardwareRobotId(q);
		state.set("POS_START_X", "0" );
		state.set("POS_START_Y", "0" );
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
					int ypos = state.getInt("BOTTLE_Y_" + i, 0 );
					if(xpos ==0 || ypos == 0 ){
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
	    q.add("DY", true);
	    q.addWait(100);
	    q.add("DZ", true);
	}

	public void moveToStart(Queue q) {
		moveZDown( q ,true );
		setAllLeds( q, "11", 100, 100,0,00 );
		q.add( new AsyncMessage( true ) {
			@Override	
			public String getName() {
				return "moveToStart" ;
			}
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				this.name		= "check position";
				int posx		= driver_x.getSPos();		// czy ja juz jestem na tej pozycji?	
				int posy		= state.getInt("POSY", 0 );
				int posz		= state.getInt("POSZ", 0 );
				int sposx		= state.getInt("POS_START_X", 0 );		// tu mam byc
				int sposy		= state.getInt("POS_START_Y", 0 );

				Queue	q2	= new Queue();
				int SERVOZ_DOWN_POS = state.getInt("SERVOZ_DOWN_POS", 0 );
				if( posz != SERVOZ_DOWN_POS ){
					moveZDown(q2, false );
				}
				if( posy != sposy ){
					moveY( q2, sposy, true );
				}
				if(posx != sposx ){
					driver_x.moveTo( q2, sposx);
					doHoming( q2, false );
				}
				q2.add("DZ", true);
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
					int SERVOY_HFRONT_POS	= state.getInt("SERVOY_HFRONT_POS", 0 );
					int posy				= state.getInt("POSY", 0 );
					if(posy >= SERVOY_HFRONT_POS ){
						moveY( q2, SERVOY_TEST_POS, true);
						q2.addWait(100);
						moveY( q2, SERVOY_FRONT_POS, true);
						q2.addWait(100);
					}
					q2.add("DZ", true);
					q2.add("DY", true);

//					int lengthx19	=  state.getInt("LENGTHX", 60000 );
				//	Initiator.logger.i("+find_bottles", "up");
					if( always || need_homing ){
						moveZDown( q2 ,true );
						if(!thisIsMax){
							int poshx = driver_x.getHardwarePos();
							q2.add("X"+ (poshx+100) +","+ BarobotConnector.this.driver_x.defaultSpeed, true);	// +100
							q2.addWait(10);
						}
					}
					if( always ){
						q2.add("X-10000,"+ BarobotConnector.this.driver_x.defaultSpeed, true);
						q2.addWait(100);
						q2.add( "IH", true );		// reset hardware X pos
						mainQueue.addFirst(q2);
					}else if( need_homing ){
						q2.add("X-10000,"+ BarobotConnector.this.driver_x.defaultSpeed, true);
						q2.addWait(100);
						q2.add( "IH", true );		// reset hardware X pos
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
		q.addWait(400);
		q.add("DX", true);
	    q.add("DY", true);
	    q.add("DZ", true);
	    carret_color(q, 0,255,0);
	    turnOffLeds(q);
    	setAllLeds(q, "22", 50, 0, 50, 0);
    	q.addWait(500);
    	setAllLeds(q, "22", 0, 0, 0, 0);
    	q.addWait(500);
    	setAllLeds(q, "22", 100, 0, 100, 0);
    	q.addWait(500);
    	setAllLeds(q, "22", 0, 0, 0, 0);
    	q.addWait(900);
    	setAllLeds(q, "22", 255, 0, 255, 0);	
		carret_color(q, 0, 255, 0);
		q.addWait(1500);
	}

	public void moveToBottle(Queue q, final int num, final boolean disableOnReady ){
		moveZDown( q ,true );
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
				color_by_bottle(q2, num, true, 255, 0, 0);
		//		Initiator.logger.i("moveToBottle","(cx == tx && cy == ty)("+cx+" == "+tx+" && "+cy+" == "+ty+")");

				int SERVOY_HFRONT_POS = state.getInt("SERVOY_HFRONT_POS", 0 );
				if(cx == tx && cy == ty ){				// not needed
			//		q2.addWait( Constant.SERVOY_REPEAT_TIME );
				}else if(cx != tx && cy == ty && ty <= SERVOY_HFRONT_POS ){	// change X, Y = front
					moveZDown(q2, true );
					driver_x.moveTo( q2, tx);

				}else if(cx != tx && cy != ty && ty <= SERVOY_HFRONT_POS  ){	// change X and Y and target = front
					moveZDown(q2, true );
					moveY( q2, ty, disableOnReady );
					driver_x.moveTo( q2, tx );

				}else if(cx != tx && cy < ty && cy <= SERVOY_HFRONT_POS  ){	// change X and Y and current = front, target = back
					moveZDown(q2, true );
					driver_x.moveTo( q2, tx );
					moveY( q2, ty, disableOnReady );
		
				}else if(cx == tx && cy != ty ){		// change Y
					moveZDown(q2, true );
					moveY( q2, ty, disableOnReady );	

				}else{									// (change X and Y ) or (change X and Y is back)
					moveZDown(q2, true );
					int SERVOY_BACK_NEUTRAL = state.getInt("SERVOY_BACK_NEUTRAL", 0 );
					moveY( q2, SERVOY_BACK_NEUTRAL, true);
					driver_x.moveTo( q2, tx );
					moveY( q2, ty, disableOnReady );
				}
				color_by_bottle(q2, num, true, 0, 255, 0);
				return q2;
			}
		} );
		//q.add("DY", true);
	    q.add(Constant.GETXPOS, true);
	    q.add(Constant.GETYPOS, true);
	    q.add(Constant.GETZPOS, true);
	}

	public void pour( Queue q, final int capacity, final int bottleNum, boolean disableOnReady ) {			// num 0-11
		int time = getPourTime(bottleNum, capacity);
		q.add("EX", true);
		q.addWait(10);
		if( bottleNum != -1 ){
			int ty  	= getBottlePosY( bottleNum );
			moveY( q, ty, false );
		}
		q.add("DY", true);
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
		this.color_by_bottle(q, bottleNum, true, 0, 255, 0);
		q.addWait( time/4 );

		moveZLight(q, bottleNum, false);

		this.setLedsByBottle(q, bottleNum, "04", 0, 0, 0, 0, true);
		q.addWait( time/4 );

		this.setLedsByBottle(q, bottleNum, "04", 255, 0, 255, 0, true);
		q.addWait( time/4 );

		this.setLedsByBottle(q, bottleNum, "04", 0, 0, 0, 0, true);	
		q.addWait( time/4 );

		this.setLedsByBottle(q, bottleNum, "04", 255, 255, 255, 200, true);
		q.add("DY", true);
		moveZDown(q,false);

		q.add( new AsyncMessage( true ) {
			@Override	
			public String getName() {
				return "pac pac" ;
			}
			@Override
			public Queue run(Mainboard dev, Queue queue) {
			//	if(virtualComponents.pac_enabled){
					Queue	q2	= new Queue();		
			//		if( up != null ){
			//			up.addLed( q2, "11", 255 );
			//		}	
					setLedsByBottle(q2, bottleNum, "11", 255, 255, 0, 0, true);
					int time = getPacWaitTime( bottleNum, capacity );
					q2.addWait( time );
					int SERVOZ_PAC_POS = state.getInt("SERVOZ_PAC_POS", 0 );
					q2.add("Z" + SERVOZ_PAC_POS+",255", true);
					moveZDown( q2, false );
					q2.addWait( 200 );
					setLedsByBottle(q2, bottleNum, "11", 0, 0, 0, 0, true);
					return q2;
			//	}
			//	return null;
			}
		} );
		if(disableOnReady){
			q.add("DX", true);
		    q.add("DY", true);
		    q.addWait(100);
		    q.add("DZ", true);
		}
	    q.add(Constant.GETXPOS, true);
	    q.add(Constant.GETYPOS, true);
	    q.add(Constant.GETZPOS, true);
	    setLedsByBottle(q, bottleNum, "22", 100, 0, 100, 0, true);
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
			LightManager lm = new LightManager();
			lm.startDemo( this );
		}
	}

	public void bottleBacklight(  Queue q, final int bottleNum, final int count ) {
		if(count == 0 ){
			color_by_bottle( q, bottleNum, true, 0, 0, 0 );
		}else if(count == 1 ){
			color_by_bottle( q, bottleNum, true, 0, 0, 255 );		// blue
		}else if( count == 2 ){
			color_by_bottle( q, bottleNum, true, 0, 255, 0 );		// green
		}else if( count == 3 ){
			color_by_bottle( q, bottleNum, true, 255, 255, 0 );		// red + green
		}else if( count == 4 ){
			color_by_bottle( q, bottleNum, true, 255, 0, 255 );		// blue + red
		}else if( count == 5 ){
			color_by_bottle( q, bottleNum, true, 100,  100, 100 );	// white
		}else{
			color_by_bottle( q, bottleNum, true, 255, 255, 255 );	// all
		}
	}
	public void setSlotMarginX( int num, int newx ) {
		this.state.set("BOTTLE_OFFSETX_" + num, newx );	
	}
	public int getSlotMarginX(int num) {
		int margin = state.getInt("BOTTLE_OFFSETX_" + num, 0 );
		return margin;			
	}
	public void systemTest() {
		// todo
	}
	public int getRobotId() {
		//Initiator.logger.w("getRobotId is=",""+robot_id );		
		return robot_id;
	}
	public void changeRobotId( int new_robot_id ) {
		Initiator.logger.w("changeRobotId", ""+new_robot_id );	
		if(robot_id != new_robot_id){
			robot_id = new_robot_id;
			if(robot_id!=0 && robot_id!=65535){		// 65535 = 1111111111111111b (empty eeprom)
				this.state.saveConfig(robot_id);
			}
			this.state.reloadConfig(robot_id);
		}
	}
	public void readHardwareRobotId( Queue q ) {
		q.add("m"+ Decoder.toHexByte(Methods.EEPROM_ROBOT_ID_HIGH), true);
		q.add("m"+ Decoder.toHexByte(Methods.EEPROM_ROBOT_ID_LOW), true);
		q.add("S", true);	// version and other stats
	}
	public void setRobotId( Queue q, int robot_id ) {	// 16bit max
		int robot_id_high	= (byte) ((robot_id >> 8) & 0xFF);
		int robot_id_low	= (byte) (robot_id & 0xFF);
		Initiator.logger.w("setRobotId", ""+robot_id + ", low: "+ robot_id_low + ", high: "+  robot_id_high );		
		q.add("M"+ Decoder.toHexByte(Methods.EEPROM_ROBOT_ID_HIGH) + Decoder.toHexByte(robot_id_high), true);
		q.add("M"+ Decoder.toHexByte(Methods.EEPROM_ROBOT_ID_LOW) + Decoder.toHexByte(robot_id_low), true);
		this.changeRobotId( robot_id );
	}
	public void turnOffLeds(Queue q){
		this.setAllLeds(q, "ff", 0, 0, 0, 0);
	}
	public void setAllLeds(Queue q, String string, int value, int red, int green, int blue ) {
		if( this.newLeds ){
			String command = "Q00"
					+ String.format("%02x", red ) 
					+ String.format("%02x", green )
					+ String.format("%02x", blue  );
			q.add(command, true);
		}else{
			Queue q1		= new Queue();	
			Upanel[] up		= i2c.getUpanels();
			for(int i =0; i<up.length;i++){
				up[i].setLed(q1, string, value);
			}
			q.add(q1);
		}
	}

	public static int[] top_index 	= {10,23,11,22,12,21,13,20,14,19,15,18};			// numery butelek na numery ledow
	public static int[] bottom_index = {-1,24,-1,25,-1,26,-1,27,-1,28,-1,29};			// numery butelek na numery ledow
	private long lastSeenRobotTimestamp = 0;

	public void setLedsByBottle(Queue q, int bottleNum, String string, int value, int red, int green, int blue, boolean addToQueue ) {
		if( this.newLeds ){
			String color = String.format("%02x", red ) 
					+ String.format("%02x", green )
					+ String.format("%02x", blue );

			if( bottleNum >= 0 && bottleNum < top_index.length ){
				int id_top	= top_index[bottleNum];
				int id_bt	= bottom_index[bottleNum];
				if(id_top > -1 ){
					String command = "l" + id_top + "," + color;
					if(addToQueue){
						q.add(command, true);
					}else{
						q.sendNow(command+"\n");
					}
				}
				if(id_bt > -1 ){
					String command = "l" + id_bt + "," + color;
					if(addToQueue){
						q.add(command, true);
					}else{
						q.sendNow(command+"\n");
					}
				}
			}
		}else if(addToQueue){
			Upanel up	= i2c.getUpanelByBottle(bottleNum);
			if(up!=null){
				up.setLed(q, string, value);
			}
		}
	}

	public void color_by_bottle( Queue q, int bottleNum, boolean topBottom, int red, int green, int blue ){
		if( this.newLeds ){
			int id = -1;
			if( bottleNum >= 0 && bottleNum < bottom_index.length ){
				if( topBottom == true ){		// top
					id = top_index[bottleNum];
				}else{
					id = bottom_index[bottleNum];
				}
				if(id != 0){				// lnn,color i.e:   l0100FFFFFF
					String command = "l" + id + "," 
							+ String.format("%02x", red ) 
							+ String.format("%02x", green )
							+ String.format("%02x", blue  );
					q.add(command, true);
				}
			}
		}else{
			Upanel up	= i2c.getUpanelByBottle(bottleNum);
			if( up != null ){
				up.setColor(q, topBottom, red, green, blue, 0);
			}
		}
	}
	public void carret_color( Queue q, int red, int green, int blue ){
		if( this.newLeds ){
			String color = String.format("%02x", red ) 			// 24 bit color
					+ String.format("%02x", green )
					+ String.format("%02x", blue );

			q.add("l00," + color, true);// 00 = led address left		
			q.add("l01," + color, true);// 01 = led address right
		}else{
			Carret cc =  new Carret(Constant.cdefault_index, Constant.cdefault_address);	
			if(red == 0 && green == 0 && blue == 0 ){
				cc.addLed(q, "ff", 0 );
			}else{
				cc.addLed(q, "ff", 255 );
			}
		}
	}

	public void scann_leds( Queue q ){
		boolean isNewVersion = true;
		if(isNewVersion){

		}else{
			LedOrder lo = new LedOrder();
			lo.asyncStart(this, q);
			q.add( new AsyncMessage( true ){
				@Override	
				public String getName() {
					return "onReady LedOrder" ;
				}
				@Override
				public Queue run(Mainboard dev, Queue queue) {
					System.out.println(" run scann_leds");
					Upanel[] up		= i2c.getUpanels();
					for(int i =0; i<up.length;i++){
						Upanel uu = up[i];
						System.out.println("+Upanel "
								+ "dla butelki: " + uu.getBottleNum() 
								//+ " w wierszu " + uu.getRow()
								+ " pod numerem " + uu.getNumInRow()
								//+ " o indeksie " + uu.getRow()
								+ " ma adres " + uu.getAddress() );
					}
					i2c.reloadIndex();
					ledsReady	= true;
					Queue q3	= new Queue();
					setAllLeds(q3, "ff", 255, 255, 255, 255);
					q3.addWait(200);
					setAllLeds(q3, "ff", 0, 0, 0, 0);
					return q3;
				}
			});
		}
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
			}else{		// error ???
				Initiator.logger.e("moveZUp.hallx", "no bottle over: "+hallState + ", expected: "+ expected );
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
}
