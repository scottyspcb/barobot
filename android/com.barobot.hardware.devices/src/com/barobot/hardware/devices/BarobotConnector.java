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
import com.barobot.parser.Queue;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.message.Mainboard;
import com.barobot.parser.utils.Decoder;

public class BarobotConnector {	
	public int pcb_type 				= 0;		// 1= old black monolyth, 2 = with magic leds, 3 = with figrelli actuators only
	public boolean ledsReady			= false;	// for pcb type 1
	public Mainboard mb					= null;
	public Queue main_queue				= null;
	public HardwareState state			= null;
	protected int robot_id				= 0;

	public boolean robot_id_ready		= false;	
	public boolean robot_id_error		= false;
	public long lastSeenRobotTimestamp	= 0;
	public LightManager lightManager	= null;
	public boolean init_done			= false;
	public Weight weight				= new Weight();
	public Z z							= new Z();
	public Y y							= new Y();
	public MotorDriver x				= null;
	public PcbType pcb;
	public static int received			= 0;

	public BarobotConnector(HardwareState state ){
		this.state			= state;
		this.pcb		 	= new PcbType(this, pcb_type );
		state.setDefaults( this.pcb );
		this.loadDefaults();
		this.mb				= new Mainboard( state );
		this.x				= new MotorDriver( state );
		this.main_queue 	= new Queue( mb );
		this.lightManager 	= new LightManager(this);

		mb.setMainQueue( this.main_queue );
		this.robot_id		= state.getInt("ROBOT_ID", 0 );
	}

	private void loadDefaults() {
		state.set( "STAT1", state.getInt( "STAT1", 0 ) + 1 );							// app starts
		state.set( "NEED_HALL_X", state.getInt( "NEED_HALL_X", 0 ) );					// app starts

		boolean defaultsLoaded	= state.getInt("DEFAULTS", 0 ) > 0;
		if( Constant.use_beta || !defaultsLoaded ){
			state.set( "SSERVER_PASS", "BAROBOT" );
			state.set( "DEFAULTS", Constant.ANDROID_APP_VERSION );
		}else{
		}
	}
	public SerialInputListener willReadFrom(Wire connection) {
		SerialInputListener listener = new SerialInputListener() {
			public void onRunError(Exception e) {
			}
			public void onNewData(byte[] data, int length) {
				received++;
				String in = new String(data, 0, length);	
			//	Initiator.logger.i(Constant.TAG,"Serial["+ in+"]" );
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
		// save stats
		int a = Mainboard.sent + state.getInt("COMMANDS_SENT", 0 );
		int b = received + state.getInt("COMMANDS_RECEIVED", 0 );
		state.set("COMMANDS_SENT", a );
		state.set("COMMANDS_RECEIVED", b );

		main_queue.destroy();
		mb.destroy();
		mb			= null;
		x			= null;	
		state		= null;
		main_queue  = null;
	}

	public void cancel_all() {
		Queue mq = main_queue;
		mq.clear();
		mq.add("LIVE A OFF", false );
//		add("EZ");
		int poszdown	=  state.getInt("SERVOZ_DOWN_POS", 9 );
		mq.add("K" + poszdown, false );
		mq.add("DX", false );
		y.disable( mq, false );
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
			return state.getInt( "SERVOY_BACK_POS", 1910 );
		}else{										// Constant.bottle_row[ i ] == Constant.BOTTLE_IS_BACK
			return state.getInt( "SERVOY_FRONT_POS", 810 );
		}
	}

	// zapisz ze tutaj jest butelka o danym numerze
	public void hereIsBottle(int num, int posx, int posy){
		Initiator.logger.i(Constant.TAG,"zapisuje pozycje:"+ num + " / " +posx+ " / " + posy );
		state.set("BOTTLE_X_" + num, posx );
	}

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
		for(int i=0;i<12;i++){
			state.set("BOTTLE_TEMP_X_" + i, "0" );
		}
		this.readHardwareRobotId(q);
		state.set("POS_START_X", "0" );
		z.moveDown( q, false );

		int SERVOZ_NEUTRAL = state.getInt("SERVOZ_NEUTRAL", 0 );
		int z_pos_known 	= state.getInt("z_pos_known", 0 );
		if( z_pos_known == 0 ){	
			q.addWait(5);
			z.move( q, SERVOZ_NEUTRAL, false );
			q.addWait(10);
			z.moveDown( q, true );
			q.addWait(10);
		}

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
				Initiator.logger.i("+find_bottles", "up");
				state.set("scann_bottles", 1 );
				return null;
			}
		});
		q.addWait( 100 );	// todo test without this line
		int calib_pos =  state.getInt("DRIVER_CALIB_X_POS", 12000 );
		x.moveTo( q, calib_pos, state.getInt("DRIVER_CALIB_X_SPEED", 0 ) );		// go up
		q.addWait( 200 );	// todo test without this line
		q.add( new AsyncMessage( true ) {
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				this.name		= "scanning back";
				Initiator.logger.i("+find_bottles", "down kalibracja");
				return null;
			}
		} );
		x.moveTo( q, -calib_pos );		// go down
		q.add( new AsyncMessage( true ) {
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				this.name		= "end scanning";
				Initiator.logger.i("+find_bottles", "koniec kalibrcja");
				state.set("scann_bottles", 0 );

				/*
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
				*/
				return null;
			}
		});
		q.add("DX", true);
		y.disable( q, true );
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
				int posx		= x.getSPos();		// czy ja juz jestem na tej pozycji?	
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
					x.moveTo( q2, sposx);
					doHoming( q2, false );
				}
				z.disable(q2, true);
				return q2;
			}
		} );
	}
	public void doHoming(Queue q, final boolean always) {
		final int minpos = -10000;

		q.add(Constant.GETXPOS, true);
		q.add(Constant.GETYPOS, true);
		q.add(Constant.GETZPOS, true);	
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
				//	int SERVOZ_DOWN_POS		= state.getInt("SERVOZ_DOWN_POS", 0 );
					int SERVOY_READ_HYSTERESIS	= state.getInt("SERVOY_READ_HYSTERESIS", 20 );
				//	int SERVOZ_READ_HYSTERESIS	= state.getInt("SERVOZ_READ_HYSTERESIS", 20 ) * 2;
					int posy				= state.getInt("POSY", 0 );
				//	int posz				= state.getInt("POSY", 0 );					

					z.moveDown( q2, true );
					int y_pos_known = state.getInt("y_pos_known", 0 );
					if( y_pos_known == 0 ){
						if( Math.abs( posy - SERVOY_FRONT_POS ) > SERVOY_READ_HYSTERESIS ){
							q2.addWait(100);
							y.move( q2, SERVOY_TEST_POS, 100, true, true);
							q2.addWait(100);
							y.move( q2, SERVOY_FRONT_POS, 100, true, true);
						}
					}else{
						y.move( q2, SERVOY_FRONT_POS, 100, true, true);
					}

					if( always || need_homing ){
						if(!thisIsMax){
							int newSpos = x.getSPos() + 100;
							x.moveTo(q2, newSpos);
							//int poshx = driver_x.getHardwarePos();
						//	q2.add("X"+ (poshx+100) +","+ driver_x.defaultSpeed, true);	// +100
						//	q2.addWait(10);
						}
					}
					int speed = state.getInt("DRIVER_X_SPEED", 0 );
					if( always ){
						q2.add("X"+minpos+","+ speed, true);
						q2.addWait(100);
						q2.addWithDefaultReader( "IH" );		// reset hardware X pos
						mainQueue.addFirst(q2);
					}else if( need_homing ){
						q2.add("X"+minpos+","+ speed, true);
						q2.addWait(100);
						q2.addWithDefaultReader( "IH" );		// reset hardware X pos
						mainQueue.addFirst(q2);
					}
				}
				return false;
			}
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
		q.add("DX", true);
		int moveUpAfterEnd = state.getInt("FINISH_UP", 0 );
		if( moveUpAfterEnd > 0){
			z.moveLight(q, -1, false );					// move up to help
		}
	//	q.addWait(400);
		y.disable( q, false );
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
		q.add(Constant.GETXPOS, true);
	    q.add(Constant.GETYPOS, true);
		q.add( new AsyncMessage( true ){
			@Override	
			public String getName(){
				return "moveToBottle" ;
			}
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				this.name	= "check position";
				int cx		= x.getSPos();		// czy ja juz jestem na tej pozycji?	
				int cy		= state.getInt("POSY", 0 );
				int tx 		= getBottlePosX( num );
				int ty  	= getBottlePosY( num );
				if( tx == 0 && ty == 0 ){
					return null;
				}
				Queue	q2	= new Queue();
				lightManager.color_by_bottle(q2, num, true, 255, 0, 0);
				Initiator.logger.i("moveToBottle","bottle " + num);
			//	Initiator.logger.i("moveToBottle","(cx == tx && cy == ty)("+cx+" == "+tx+" && "+cy+" == "+ty+")");
				z.moveDown(q2, true );

				int SERVOY_FRONT_POS		= state.getInt("SERVOY_FRONT_POS", 0 );
				int SERVOY_BACK_POS			= state.getInt("SERVOY_BACK_POS", 0 );
				int SERVOY_READ_HYSTERESIS	= state.getInt("SERVOY_READ_HYSTERESIS", 20 );

				int type = 0;

				//boolean moveLeft 		= cx > tx;
				//boolean moveRight		= cx < tx;
				boolean isNearFront		= Math.abs( SERVOY_FRONT_POS - cy ) <= SERVOY_READ_HYSTERESIS;
				boolean	isNearBack		= Math.abs( SERVOY_BACK_POS - cy ) <= SERVOY_READ_HYSTERESIS;

				boolean toBeFront		= Math.abs( SERVOY_FRONT_POS - ty ) <= SERVOY_READ_HYSTERESIS;
				boolean	toBeBack		= Math.abs( SERVOY_BACK_POS - ty ) <= SERVOY_READ_HYSTERESIS;				

				if( cx == tx && cy == ty ){				// not needed
					type = 1;
				}else if( cx == tx && isNearFront && toBeFront ){				// not needed
					type = 2;
				}else if( cx == tx && isNearBack && toBeBack ){					// not needed	
					type = 3;
				}else if( cx == tx && isNearFront && toBeBack ){				// just move back
					y.move( q2, ty, 100, true, true );
					type = 4;
				}else if( cx == tx && isNearBack && toBeFront ){				// just move front
					y.move( q2, ty, 100, true, true );
					type = 5;
				}else if( cx != tx && isNearFront && toBeFront ){				// move X
					x.moveTo( q2, tx);
					type = 6;
				}else if( cx != tx && isNearBack && toBeBack ){					// move X
					type = 7;
					int SERVOY_BACK_NEUTRAL = state.getInt("SERVOY_BACK_NEUTRAL", 0 );
					y.move( q2, SERVOY_BACK_NEUTRAL, 50, true, true);
					x.moveTo( q2, tx );
					y.move( q2, ty, 50, true, true );

				}else if( cx != tx && isNearFront && toBeBack ){				// move X and then Y
					x.moveTo( q2, tx );
					y.move( q2, ty, 100, true, true );
					type = 8;
				}else if( cx != tx && isNearBack && toBeFront ){				// move Y and then Y
					y.move( q2, ty, 100, true, true );
					x.moveTo( q2, tx );
					type = 9;
				}else{
					type = 10;
					Initiator.logger.e("moveToBottle","undefined case, isNearFront:"+ isNearFront 
							+", isNearBack:"+ isNearBack 
							+", toBeFront:"+ toBeFront 
							+", toBeBack:"+ toBeBack +"");

					Initiator.logger.e("moveToBottle","undefined case:(cx == tx && cy == ty)("+cx+" == "+tx+" && "+cy+" == "+ty+")");
				}
/*
				int front_and_hyster = SERVOY_FRONT_POS + SERVOY_READ_HYSTERESIS;
				if(cx == tx && cy == ty ){				// not needed
				}else if(cx != tx && cy == ty && ty <= front_and_hyster ){	// change X, Y = front
					x.moveTo( q2, tx);
					type = 2;
				}else if(cx != tx && cy != ty && ty <= front_and_hyster ){	// change X and Y and target = front
					y.move( q2, ty, 100, true, true );
					x.moveTo( q2, tx );
					type = 3;
				}else if(cx != tx && cy < ty && cy <= front_and_hyster ){	// change X and Y and current = front, target = back
					x.moveTo( q2, tx );
					y.move( q2, ty, 100, true, true );
					type = 4;
				}else if(cx == tx && cy != ty ){		// change Y
					y.move( q2, ty, 100, true, true );
					type = 5;
				}else{									// (change X and Y ) or (change X and Y is back)
					int SERVOY_BACK_NEUTRAL = state.getInt("SERVOY_BACK_NEUTRAL", 0 );
					y.move( q2, SERVOY_BACK_NEUTRAL, 50, true, true);
					x.moveTo( q2, tx );
					y.move( q2, ty, 50, true, true );
				}*/
				Initiator.logger.i("moveToBottle","type"+ type);
				lightManager.color_by_bottle(q2, num, true, 0, 255, 0);
				return q2;
			}
		} );
		//disabley( q );
	}

	public void pour( Queue q, final int capacity, final int bottleNum, boolean disableOnReady, boolean needGlass ) {			// num 0-11
		int time		= getPourTime(bottleNum, capacity);
		int pac_time	= getPacWaitTime( bottleNum, capacity );	
		
		Queue q_ok		= new Queue();
		Queue q_error	= new Queue();
		lightManager.setAllLeds(q_error, "11", 255, 255, 0, 0);q_error.addWait( 300 );
		lightManager.setAllLeds(q_error, "11", 00, 0, 0, 0);q_error.addWait( 300 );
		lightManager.setAllLeds(q_error, "11", 00, 255, 0, 0);q_error.addWait( 300 );
		lightManager.setAllLeds(q_error, "11", 00, 0, 0, 0);q_error.addWait( 300 );

		q_ok.add("EX", true);

		z.moveUp(q_ok, bottleNum, false);
		lightManager.color_by_bottle(q_ok, bottleNum, true, 0, 255, 0);
		q_ok.addWait( time/4 );		// 1/4

	//	moveZLight(q, bottleNum, false);

		lightManager.setLedsByBottle(q_ok, bottleNum, "04", 0, 0, 0, 0);
		q_ok.addWait( time/4 );		// 2/4

		lightManager.setLedsByBottle(q_ok, bottleNum, "04", 255, 0, 255, 0);
		q_ok.addWait( time/4 );		// 3/4

		lightManager.setLedsByBottle(q_ok, bottleNum, "04", 0, 0, 0, 0);	
		q_ok.addWait( time/4 );		// 4/4

		lightManager.setLedsByBottle(q_ok, bottleNum, "04", 255, 255, 255, 200);
		z.moveDown(q_ok, false);

		addPac(q_ok,bottleNum, pac_time );

		if(disableOnReady){
			q_ok.add("DX", true);
		}
	    lightManager.setLedsByBottle(q_ok, bottleNum, "22", 100, 0, 100, 0);
	    if(needGlass){
	    	Queue checkWeight = this.weight.check( q_ok, q_error );		// check weight - no less than on start
			q.add(checkWeight);
	    }else{
	    	q.add(q_ok);
	    }
	}
	private void addPac(Queue q_ok, final int bottleNum, final int pac_time) {
		q_ok.add( new AsyncMessage( true ) {
			@Override	
			public String getName() {
				return "pac pac" ;
			}
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				Queue	q2				= new Queue();	
				int SERVOZ_PAC_POS		= state.getInt("SERVOZ_PAC_POS", 0 );
				int SERVOZ_PAC_BACK_DIFF= state.getInt("SERVOZ_PAC_BACK_DIFF", 0 );
				
				int pos					= SERVOZ_PAC_POS;
				if( bottleNum %2 == 0){		// pair numbers = 0,2,4,6,8,10
					pos += SERVOZ_PAC_BACK_DIFF;
				}
				int SERVOZ_DOWN_POS		= state.getInt("SERVOZ_DOWN_POS", 0 );
				int SERVOZ_UP_TIME		= state.getInt("SERVOZ_UP_TIME", 2000 );
				int z_pos_known			= state.getInt("z_pos_known", 0 );
				lightManager.setLedsByBottle(q2, bottleNum, "11", 100, 100, 0, 0);
				q2.addWait( pac_time );
				if( z_pos_known == 0){
					q2.addWithDefaultReader("K" + pos);
					q2.addWait(110);
					//moveZDown( q2, true );
					q2.addWithDefaultReader("K" + SERVOZ_DOWN_POS);
					q2.addWait(SERVOZ_UP_TIME/3);
					z.disable(q2, true);
				}else{
					z.move( q2, pos, false );
					z.move( q2, SERVOZ_DOWN_POS, true );
				}
				lightManager.setLedsByBottle(q2, bottleNum, "11", 255, 255, 0, 0);
				return q2;
			}
		} );
	}

	// todo move to slot
	public int getPourTime( int num, int capacity ){			// 0 - 11
		int SERVOZ_POUR_TIME = state.getInt("SERVOZ_POUR_TIME", 0 );
		return capacity * SERVOZ_POUR_TIME;
	}
	public int getRepeatTime(int num, int capacity) {
		int time 		= state.getInt("SERVOY_REPEAT_TIME", 0 );
		int base_time	= time/ 20;		// 20 ml
		return base_time * capacity;
	}
	public int getPacWaitTime(int num, int capacity) {
		int base_time 	= state.getInt("SERVOZ_PAC_TIME_WAIT", 0 );
		int time 		= state.getInt("SERVOZ_PAC_TIME_WAIT_VOL", 0 );
		return base_time + (time / 20 * capacity);		// 20 ml
	}

	public void setSlotMarginX( int num, int newx ) {
		state.set("BOTTLE_OFFSETX_" + num, newx );	
	}

	public int getSlotMarginX(int num) {
		int margin = state.getInt("BOTTLE_OFFSETX_" + num, 0 );
		return margin;			
	}

	public int getRobotId() {
		//Initiator.logger.w("getRobotId is=",""+robot_id );		
		return robot_id;
	}

	public void changeRobotId( int new_robot_id, boolean readFromHardware, int pcb_type2 ) {
		Initiator.logger.w("changeRobotId", "id:"+new_robot_id+"/pcb_type:"+ pcb_type2 );

		if( new_robot_id > 0 && new_robot_id < 65535 ){				// 65535 = 1111111111111111b (empty eeprom)
			if(robot_id != new_robot_id){
				if(robot_id!=0 && robot_id!=65535){					// 65535 = 1111111111111111b (empty eeprom)
					state.saveConfig(robot_id);
				}
				robot_id = new_robot_id;
				boolean usePrev	= (pcb_type2 == this.pcb_type) ? true : false;		// use previous settings if the same PCB_TYPE
				Initiator.logger.w("changeRobotId", "usePrev:"+ usePrev );
				state.reloadConfig(robot_id, usePrev );
			}
			state.set("ROBOT_ID", new_robot_id );
			if(readFromHardware){
				this.robot_id_ready = true;
				this.robot_id_error = false;
			}
		}else{
			robot_id_error = true;
			this.robot_id_ready = false;
			state.set("ROBOT_ID", 0 );
			robot_id = 0;
		}
		changeRobotPcb( pcb_type2 );
	}

	public void readHardwareRobotId( Queue q ) {
		//q.addWithDefaultReader("m"+ Decoder.toHexByte(Methods.EEPROM_ROBOT_ID_HIGH));
		//q.addWithDefaultReader("m"+ Decoder.toHexByte(Methods.EEPROM_ROBOT_ID_LOW));
		q.addWithDefaultReader("S");	// version and other stats
	}

	public void setRobotId( Queue q, int robot_id, int pcb_type2 ) {	// 16bit max
		int robot_id_high	= (byte) ((robot_id >> 8) & 0xFF);
		int robot_id_low	= (byte) (robot_id & 0xFF);
		Initiator.logger.w("setRobotId", ""+robot_id + ", low: "+ robot_id_low + ", high: "+  robot_id_high );		
		q.add("M"+ Decoder.toHexByte(Methods.EEPROM_ROBOT_ID_HIGH) + Decoder.toHexByte(robot_id_high), true);
		q.add("M"+ Decoder.toHexByte(Methods.EEPROM_ROBOT_ID_LOW) + Decoder.toHexByte(robot_id_low), true);
		this.changeRobotId( robot_id, true, pcb_type2 );
	}

	public void setLastSeen(int millis) {
		java.util.Date date= new java.util.Date();
		lastSeenRobotTimestamp = new java.sql.Timestamp(date.getTime()).getTime();
	}

	public int getLastTemp() {
		return state.getInt("TEMPERATURE", 0);
	}

	public boolean isAvailable() {
		if( lastSeenRobotTimestamp == 0 ){
			return false;
		}
		if( x == null ){
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

	
	public void onFirstStatReady(Queue mq ) {
	}

	public void onConnected(Queue mq, boolean robotExistsForSure ) {
		Initiator.logger.w("barobot.onConnected", "start" );
		mq.add( new AsyncMessage( true ) {
			@Override
			public String getName() {
				return "Check robot start";
			}
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				setInitDone( false );
				return null;
			}
		});
		mq.add( "\n", false );	// clean up input
		mq.add( "\n", false );
		readHardwareRobotId(mq);
	
		mq.add( new AsyncMessage( true ) {
			@Override
			public String getName() {
				return "Robot start";
			}
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				if( pcb_type == 0 || robot_id_ready == false ){		// robot is not ready, do nothing	
					Initiator.logger.w("barobot.onConnected", "robot is not ready. pcb_type= "+ pcb_type + ", robot_id: " + robot_id );
					showError( "Robot don't have ROBOT_ID, update firmware (in calibration/software)" );
					return null;
				}
				boolean oncePerAppStart		= state.getInt("ONCE_PER_APP_START", 0) < Constant.ANDROID_APP_VERSION ;
				boolean oncePerRobotLife	= state.getInt("ONCE_PER_ROBOT_LIFE", 0) == 0;

				Queue q = new Queue();
				y.disable( q, true );			// disable everything is moving
				q.add("DX", true);
				lightManager.scann_leds( q );
				q.add(Constant.GETXPOS, true);		// get positions
				q.add(Constant.GETYPOS, true);
				q.add(Constant.GETZPOS, true);
				q.add( new AsyncMessage( true ) {
					@Override
					public String getName() {
						return "Check robot end";
					}
					@Override
					public Queue run(Mainboard dev, Queue queue) {
						int starts2					= state.getInt("ARDUINO_STARTS", 0);
						int starts					= state.getInt("LAST_ROBOT_STARTS", 0);
						boolean can_move			= (state.getInt("ROBOT_CAN_MOVE", 0) >= Constant.WIZARD_VERSION);
						boolean oncePerRobotStart	= state.getInt("ONCE_PER_ROBOT_START", 0) == 0;
						if( starts != starts2){
							state.set("LAST_ROBOT_STARTS", starts);
							oncePerRobotStart = true;
						}
						setInitDone( true );
						Queue q2 = new Queue();
						if( oncePerRobotStart ){
							if(can_move){
								lightManager.setAllLeds(q2, "22", 0, 100, 0, 0);
								doHoming( q2, false );
							}
							lightManager.setAllLeds(q2, "44", 255, 0, 255, 0 );
							state.set("ONCE_PER_ROBOT_START", Constant.ANDROID_APP_VERSION );	
						}
						return q2;
					}
				});
				if( oncePerRobotLife ){
					state.set("ONCE_PER_ROBOT_LIFE", Constant.ANDROID_APP_VERSION );
				}else{
				}
				if( oncePerAppStart ){
					state.set("ONCE_PER_APP_START", Constant.ANDROID_APP_VERSION );
				}
				lightManager.setAllLeds(q, "44", 255, 0, 100, 0 );
				Initiator.logger.w("barobot.onConnected", "end" );
				return q;
			}
		});
	}

	protected void showError(String string) {
		// i cant do anything here, look at inherits class
	}

	protected void setInitDone(boolean ready ) {
		Initiator.logger.w("barobot.setInitDone", ""+ready );
		this.init_done = ready;
	}

	public class Y{
		public void move( Queue q, final int newpos, final int speed_ratio, final boolean disableOnReady, final boolean hysteresis ) {
			readHallY(q);
			q.add( new AsyncMessage( true ){
				@Override
				public String getName() {
					return "moveY logic" ;
				}
				@Override
				public Queue run(Mainboard dev, Queue queue) {
					//Initiator.logger.w("Y.distance", ""+distance );
					//Initiator.logger.w("time1", ""+time );
					//Initiator.logger.w("time2", ""+(DRIVER_Y_SPEED * distance * DRIVER_Y_TIME/100) );
					Queue q4				= new Queue();
					int SERVOY_HYSTERESIS	= state.getInt("SERVOY_HYSTERESIS", 20 );
					int posy				= state.getInt("POSY", 0 );
					int speed				= state.getInt("DRIVER_Y_SPEED", 20 ) * speed_ratio / 100;
					int y_pos_known			= state.getInt("y_pos_known", 0 );
					if( y_pos_known == 0){
						q4.add("Y" + newpos+ ","+speed, true);	
						int distance		= Math.abs(newpos - posy);
						int DRIVER_Y_TIME	= state.getInt("DRIVER_Y_TIME", 100 );		// percent
						int time			= (speed * distance * DRIVER_Y_TIME/100) / 300 + 100;	
						q4.addWait(time);
					}else{
						int SERVOY_READ_HYSTERESIS	= state.getInt("SERVOY_READ_HYSTERESIS", 20 );
						if( Math.abs( posy - newpos ) > SERVOY_READ_HYSTERESIS ){	// diff is bigger than hysteresis
							q4.add("Y" + newpos+ ","+speed, true);	
						}else{
							q4.add("DY", true );		// no move is needed
							return q4;
						}
					}
					if( hysteresis && SERVOY_HYSTERESIS > 0 ){
						if( newpos > posy ){	// forward
							q4.add("Y" + (newpos - SERVOY_HYSTERESIS)+ ","+speed, true);	
						}else{	// backwad
							q4.add("Y" + (newpos + SERVOY_HYSTERESIS)+ ","+speed, true);	
						}
					}
					if(disableOnReady){
						if( y_pos_known == 0){
//							q4.addWait(100);
							q4.add("DY", true );
						}else{
							q4.add("DY", true );
						}
					}
					return q4;
				}
			});
		}

		public void moveToFront(Queue q2) {
			int posy					= state.getInt("POSY", 0 );			// current pos
			int pos						= state.getInt("SERVOY_FRONT_POS", 0 );
			int SERVOY_READ_HYSTERESIS	= state.getInt("SERVOY_READ_HYSTERESIS", 20 );
			if( Math.abs(posy - pos) > SERVOY_READ_HYSTERESIS ){	// diff is bigger than hysteresis
				move( q2, pos, 100, true, true );
			}
		}
		public void disable(Queue q, boolean wait_after) {
			q.add("DY", true);
			if(wait_after){
				int time = state.getInt("y_off_timeout", 300 );
				q.addWait(time);		// wait for power off - to be sure
			}
		}
		public void readHallY(Queue q) {
			q.addWithDefaultReader("A1");
		}
	}

	public class Z{
		private boolean isBottle(int hallState, int bottleNum, boolean defaultRet) {
			// -1 = true
			if(bottleNum >= 0 ){
				int expected = Constant.bottle_row[bottleNum];			// expect front of bottom
				if( hallState == Methods.HX_STATE_7 && expected == Constant.BOTTLE_IS_FRONT){		// front bottle is over the carriage 
					Initiator.logger.e("moveZUp.hallx1", ""+hallState + ", expected: "+ expected );
					return true;
				}else if( hallState ==Methods.HX_STATE_3 && expected == Constant.BOTTLE_IS_BACK){	// back bottle is over the carriage 
					Initiator.logger.e("moveZUp.hallx2", ""+hallState + ", expected: "+ expected );
					return true;
				}else if( bottleNum == 11 && expected == Constant.BOTTLE_IS_FRONT && (hallState == Methods.HX_STATE_1 || hallState == Methods.HX_STATE_2)){	// the last bottle is diffrent
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

		private boolean isRetFromAnalog(String result, int bottleNum) {
			if(result.matches("^" +  Methods.METHOD_IMPORTANT_ANALOG + ",0,.*" )){	// 125,0,100,0,0,0,255,202,126,1
				return true;
			}
			return false;
		}
		private boolean isBelowBottle(String result, int bottleNum) {
			if(result.matches("^" +  Methods.METHOD_IMPORTANT_ANALOG + ",0,.*" )){	// 125,0,100,0,0,0,255,202,126,1
				boolean allowUpX		 	= true;
				boolean allowUpY		 	= true;
				final int NEED_HALL_X		= state.getInt("NEED_HALL_X", 1 );				
				final int NEED_HALL_Y		= state.getInt("NEED_HALL_Y", 1 );
				int hallY					= state.getInt("HALLY", 0);
				int hyst					= state.getInt("SERVOY_HYSTERESIS", 1) / 2;		// be close
				if(NEED_HALL_Y == 0){
					allowUpY = true;
				}else if( bottleNum > 0 && bottleNum < 12 ){
					int bottleY = getBottlePosY( bottleNum );
			//		Initiator.logger.w("moveZUp", "allowUpY.hall"+hallY );
					Initiator.logger.w("moveZUp", "allowUpY.diff"+(hallY - bottleY) );
					if( Math.abs(hallY - bottleY) < hyst ){
						allowUpY = true;		
					}	
				}
				int[] parts				= Decoder.decodeBytes( result );
				boolean defaultRet	 	= false;
				if(NEED_HALL_X == 0){
					allowUpX			= true;
				}else{
					allowUpX			= isBottle( parts[2], bottleNum, defaultRet );
				}
				Initiator.logger.w("moveZUp", "allowUp.hallY"+(allowUpY? "tak" : "nie") );
				Initiator.logger.w("moveZUp", "allowUp.hallX"+(allowUpX? "tak" : "nie") );
				
				if( defaultRet || (allowUpX && allowUpY) ){
					return true;
				}
			}
			return false;
		}

		public void moveLight(Queue q, final int bottleNum, final boolean disableOnReady) {
			q.addWithDefaultReader("A2");		// read hall Y	
			q.add( new AsyncMessage( "A0", true ) {// check i'm over endstop (neodymium magnet)
				@Override
				public String getName() {
					return "Check magnet before Z LIGHT UP";
				}
				@Override
				public boolean isRet(String result, Queue mainQueue) {
					boolean isRet	= isRetFromAnalog(result, bottleNum);
					if(isRet){
						boolean correctRet	= isBelowBottle(result, bottleNum);
						Queue	q2			= new Queue(); 
						if(correctRet){
							int SERVOZ_UP_LIGHT_POS	= state.getInt("SERVOZ_UP_LIGHT_POS", 0 );
							move(q2, SERVOZ_UP_LIGHT_POS, true );
						}else if(disableOnReady){
							disable(q2, true );
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

		public void moveDown(Queue q, final boolean disableOnReady ) {
			int pos = state.getInt("SERVOZ_DOWN_POS", 0 );
			move( q, pos, disableOnReady );
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

		public void move(Queue q, final int pos, final boolean disableOnReady) {
			q.add(Constant.GETZPOS, true);
			q.add( new AsyncMessage( true ){
				@Override	
				public String getName() {
					return "moveZDown logic" ;
				}
				@Override
				public Queue run(Mainboard dev, Queue queue) {
					Queue q				= new Queue();
					int current_z		= state.getInt("POSZ", 0 );
					int z_pos_known		= state.getInt("z_pos_known", 0 );
					if(z_pos_known == 0){
						q.addWithDefaultReader("K" + pos);
						int SERVOZ_UP_TIME		= state.getInt("SERVOZ_UP_TIME", 1900 );
						int SERVOZ_UP_TIME_MIN	= state.getInt("SERVOZ_UP_TIME_MIN", 250 );
						int diff				= Math.abs((current_z - pos));
						int time				= SERVOZ_UP_TIME / 1000 * diff;
						time					= Math.max( SERVOZ_UP_TIME_MIN, time );	// no less than 100
					//	time					= Math.min( 800, time );
						Initiator.logger.i("BarobotConnector.moveZ","timer move z:"+time +" current_z:"+ current_z+ " pos: " + pos+ " diff: "+ diff);
						q.addWait( time );		// wait for position
						if(disableOnReady){
							disable(q, true);
						}
					}else{
						int SERVOZ_READ_HYSTERESIS	= state.getInt("SERVOZ_READ_HYSTERESIS", 20 );
						if( Math.abs(current_z - pos) > SERVOZ_READ_HYSTERESIS ){	// diff is bigger than hysteresis
							q.addWithDefaultReader("Z" + pos);
							if(disableOnReady){
								disable(q, true);
							}
						}else if(disableOnReady){
							disable(q, true);					
						}
					}
					return q;
				}
			});
		}
		public void moveWithCheck(Queue q, final int pos, final boolean disableOnReady, final int bottleNum) {
			q.addWithDefaultReader("A2");		// read hall Y
			q.add( new AsyncMessage( "A0", true ) {// check i'm over endstop (neodymium magnet)
				@Override
				public String getName() {
					return "Check magnet before move Z";
				}
				@Override
				public boolean isRet(String result, Queue mainQueue) {
					boolean isRet	= isRetFromAnalog(result, bottleNum);
					if(isRet){
						boolean correctRet	= isBelowBottle(result, bottleNum);
						Queue	q2			= new Queue(); 
						if(correctRet){
							move(q2, pos, disableOnReady);
						}else if(disableOnReady){
							disable(q2, true );
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

		public void moveUp( Queue q, final int bottleNum, final boolean disableOnReady ) {
			q.addWithDefaultReader("A2");		// read hall Y	
			q.add( new AsyncMessage( "A0", true ) {			// check i'm over endstop (neodymium magnet)
				@Override
				public String getName() {
					return "Check magnet before Z UP";
				}
				@Override
				public boolean isRet(String result, Queue mainQueue) {
					boolean isRet	= isRetFromAnalog(result, bottleNum);
					if(isRet){
						boolean correctRet	= isBelowBottle(result, bottleNum);
						Queue	q2			= new Queue();
						if(correctRet){
							int SERVOZ_UP_POS	= state.getInt("SERVOZ_UP_POS", 0 );
							move(q2, SERVOZ_UP_POS, disableOnReady );
						}else if(disableOnReady){
							disable(q2, true);
						}
						mainQueue.addFirst(q2);
					}
					return false;
				}
				@Override
				public boolean onInput(String input, Mainboard dev, Queue mainQueue) {
					if(input.equals("RA0")){		// its me
					//	return true;
					}else{
						
					}
					return false;
				}
			});
		}
		public void disable(Queue q, boolean wait_after) {
			q.add("DZ", true);
			if(wait_after){
				int time = state.getInt("z_off_timeout", 300 );
				q.addWait(time);		// wait for power off - to be sure	
			}
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
				min_diff_up	= state.getInt("LIGHT_GLASS_DIFF", 5 );	
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
				}else if( weight - min_weight > min_diff ){				// more than the empty tray
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
					}else if( timestamp2 - timestamp > max_time ){		// to long without glass
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
	public void changeRobotPcb(int new_pcb_type ) {
		Initiator.logger.e("changeRobotPcb", "pcb_type: " + new_pcb_type );
		if( pcb_type != new_pcb_type ){
			this.pcb		 	= new PcbType(this, new_pcb_type );
			this.pcb_type		= new_pcb_type;
			state.setDefaults( this.pcb );
		}
		state.set("PCB_TYPE", new_pcb_type );
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
