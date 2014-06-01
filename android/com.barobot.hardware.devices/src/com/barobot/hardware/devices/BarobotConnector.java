package com.barobot.hardware.devices;

import com.barobot.common.Initiator;
import com.barobot.common.constant.Constant;
import com.barobot.common.interfaces.HardwareState;
import com.barobot.common.interfaces.serial.CanSend;
import com.barobot.common.interfaces.serial.SerialInputListener;
import com.barobot.common.interfaces.serial.Wire;
import com.barobot.hardware.devices.i2c.I2C;
import com.barobot.hardware.devices.i2c.Upanel;
import com.barobot.parser.Queue;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.message.Mainboard;

public class BarobotConnector {
	public boolean ledsReady = false;

	// pozycje butelek, sa aktualizowane w trakcie
	public static int[] margin_x = {
		-70,		// 0, num 1,back
		-150,		// 1, num 2,front		
		-60,		// 2, num 3,back
		-150,		// 3, num 4,front		
		-40,		// 4, num 5,back		
		-140,		// 5, num 6,front
		-100,		// 6, num 7,back
		-150,		// 7, num 8,front
		-20,		// 8, num 9,back
		-140,		// 9, num 10,front
		-20,		// 10, num 11,back		
		-100,		// 11, num 12,front
	};
	
	public static boolean pureCrystal = false;

	// pozycje butelek, sa aktualizowane w trakcie
	public static int[] capacity = {
		20,			// 0, num 1,back
		50, 		// 1, num 2,front
		50,			// 2, num 3,back
		20,			// 3, num 4,front		
		20,			// 4, num 5,back		
		20,			// 5, num 6,front
		20,			// 6, num 7,back
		20, 		// 7, num 8,front
		10,			// 8, num 9,back
		20,			// 9, num 10,front
		20,			// 10, num 11,back
		50			// 11, num 12,front
	};

	public MotorDriver driver_x	= null;
	public Mainboard mb			= null;
	public Queue main_queue		= null;
	public Servo driver_y		= null;
	public Servo driver_z		= null;
	public HardwareState state	= null;
	public I2C i2c				= null;

	public BarobotConnector(HardwareState state ){
		this.state		= state;
		this.mb			= new Mainboard( state );
		this.driver_x	= new MotorDriver( state );
		this.driver_y	= new Servo( state, "Y" );
		this.driver_z	= new Servo( state, "Z" );
		this.main_queue = new Queue( mb );
		this.i2c  		= new I2C();
		this.driver_x.defaultSpeed = Constant.DRIVER_X_SPEED;
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

	public void scann_leds(){
		Queue q			= main_queue;
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
							+ " w wierszu " + uu.getRow()
							+ " pod numerem " + uu.getNumInRow()
							+ " o indeksie " + uu.getRow()
							+ " ma adres " + uu.getAddress() );
				}
				i2c.rememberStructure();
				ledsReady	= true;
				Queue q3	= new Queue();
				Queue q1	= new Queue();
				Queue q2	= new Queue();
				for(int i =0; i<up.length;i++){
					up[i].addLed(q1, "ff", 200);
					up[i].addLed(q2, "ff", 0);
				}
				q3.add(q1);
				q3.addWait(200);
				q3.add(q2);
				return q3;
			}
		});
	}

	
	
	public void cancel_all() {
			Queue mq = main_queue;
			mq.clear();
			mq.add("LIVE A OFF", false );
	//		add("EZ");
			int poszdown	=  state.getInt("ENDSTOP_Z_MIN", Constant.SERVOZ_DOWN_POS );
			mq.add("Z" + poszdown, false );		// zwraca pocz¹tek operacji a nie koniec
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
				Queue q4 = new Queue();
				int margin		= 20;
				int posy		=  state.getInt("POSY", 0 );
			//	if( posy != newpos && posy != newpos -margin && posy != newpos +margin){
					q4.add("Y" + newpos+ ","+Constant.DRIVER_Y_SPEED, true);
					q4.addWait(100);
					if( newpos > posy ){	// forward
						q4.add("Y" + (newpos - margin)+ ","+Constant.DRIVER_Y_SPEED, true);	
					}else{	// backwad
						q4.add("Y" + (newpos + margin)+ ","+Constant.DRIVER_Y_SPEED, true);	
					}
					if(disableOnReady){
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

	// zapisz ze tutaj jest butelka o danym numerze
	public void hereIsBottle(int i) {
		int posx		=  driver_x.getSPos();
		int posy		=  state.getInt("POSY", 0 );
	//	Initiator.logger.i(Constant.TAG,"zapisuje pozycje:"+ i + " " +posx+ " " + posy );
		state.set("BOTTLE_X_" + i, posx );
		state.set("BOTTLE_Y_" + i, posy );
	}

	public void startDoingDrink() {
		Queue q = main_queue;
		i2c.carret.setLed( q, "11", 250 );
		Queue q1		= new Queue();	
		Upanel[] up		= i2c.getUpanels();
		for(int i =0; i<up.length;i++){
			up[i].setLed(q1, "ff", 200);
		}
		q.add(q1);
	}

	public void moveZUp( Queue q, boolean disableOnReady ) {
	//		q.add("EZ", true);
			int poszup	=  Constant.SERVOZ_UP_POS;
			q.add("Z" + poszup+","+Constant.DRIVER_Z_SPEED, true);
		//	q.addWait( virtualComponents.SERVOZ_UP_TIME );	// wiec trzeba poczekaæ
			if(disableOnReady){
				q.addWait(300);
				q.add("DZ", true);
			}
		}

	public void moveZLight(Queue q, boolean disableOnReady) {
//		q.add("EZ", true);
		int poszup	=  Constant.SERVOZ_UP_LIGHT_POS;
		q.add("Z" + poszup+","+Constant.DRIVER_Z_SPEED, true);
	//	q.addWait( virtualComponents.SERVOZ_UP_TIME );	// wiec trzeba poczekaæ
		if(disableOnReady){
			q.addWait(300);
			q.add("DZ", true);
		}
	}

	public void moveZ(Queue q, int pos) {
		q.add("Z" + pos +","+Constant.DRIVER_Z_SPEED, true);
		q.addWait(300);
	}

	public void moveZDown(Queue q, boolean disableOnReady) {
		int py =  state.getInt("POSY", 0 );
		if( py != Constant.SERVOZ_DOWN_POS ){
			int poszdown	=  state.getInt("ENDSTOP_Z_MIN", Constant.SERVOZ_DOWN_POS );
			moveZ(q, poszdown );
			q.add("DZ", true);
		}
	}

	public void kalibrcja() {
		Queue q			= main_queue;
		q.add( "\n", false );
		q.add( "\n", false );
		setLeds( "ff", 0 );
		int posx		= driver_x.getSPos();
		for(int i=0;i<12;i++){
			state.set("BOTTLE_X_" + i, "0" );
			state.set("BOTTLE_Y_" + i, "0" );
		}
		state.set("POS_START_X", "0" );
		state.set("POS_START_Y", "0" );

		Initiator.logger.i("+find_bottles", "start");
		q.add("EX", true );
		moveZDown( q ,true );
		q.addWait(100);
		moveZ( q, Constant.SERVOZ_TEST_POS );
		q.addWait(100);
		moveZDown( q ,true );
		q.addWait(200);
		moveY( q, Constant.SERVOY_TEST_POS, true);
		q.addWait(200);
		moveY( q, Constant.SERVOY_FRONT_POS, true);
		q.addWait(200);
		int lengthx19	=  state.getInt("LENGTHX", 60000 );
		
		Initiator.logger.i("+find_bottles", "up");
		driver_x.moveTo( q, posx + 1000);
		q.addWait(100);
		driver_x.moveTo( q, -70000 );

		q.addWait(100);
		// scann Triggers
		q.add( new AsyncMessage( true ) {			// go up
			@Override	
			public String getName() {
				return "kalibrcja" ;
			}
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				this.name		= "scanning up";
				driver_x.defaultSpeed = 1000;
				Initiator.logger.i("+find_bottles", "up");
				state.set("scann_bottles", 1 );
				return null;
			}
		});
		driver_x.moveTo( q, 30000);		// go down
		q.add( new AsyncMessage( true ) {
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				this.name		= "scanning back";
				driver_x.defaultSpeed = Constant.DRIVER_X_SPEED;
				Initiator.logger.i("+find_bottles", "down kalibrcja");
				return null;
			}
		} );
		driver_x.moveTo( q, -lengthx19);
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
		} );
		//virtualComponents.scann_leds();
	}

	public void setLeds(String string, int value ) {
		if(!ledsReady){
			scann_leds();
		}
		Queue q1		= new Queue();	
		Upanel[] up		= i2c.getUpanels();
		for(int i =0; i<up.length;i++){
			up[i].setLed(q1, string, value);
		}
		main_queue.add(q1);
	}	

	public void moveToStart() {
		Queue q = main_queue;
		moveZDown( q ,true );
		q.add( new AsyncMessage( true ) {
			@Override	
			public String getName() {
				return "moveToStart" ;
			}
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				this.name		= "check position";
				int posx		= driver_x.getSPos();;		// czy ja juz jestem na tej pozycji?	
				int posy		= state.getInt("POSY", 0 );
				int sposx		= state.getInt("POS_START_X", 0 );		// tu mam byc
				int sposy		= state.getInt("POS_START_X", 0 );

				if(posx != sposx || posy != sposy ){		// musze jechac?
					Queue	q2	= new Queue();
					moveZDown(q2, true );
					//virtualComponents.moveY( q2, virtualComponents.get("NEUTRAL_POS_Y", "0" ));
					moveY( q2, Constant.SERVOY_FRONT_POS, true );
					driver_x.moveTo( q2, sposx);
					moveY( q2, sposy, true );
					return q2;
				}
				return null;
			}
		} );
	    q.add(Constant.GETXPOS, true);
	    q.add(Constant.GETYPOS, true);
	    q.add(Constant.GETZPOS, true);

	}
	public void onDrinkFinish() {
		Queue q = main_queue;
	    i2c.carret.setLed( q, "22", 250 );
	    setLeds( "ff", 0 );
		Queue q1			= new Queue();
		Upanel[] up 		= i2c.getUpanels();
		for(int i =up.length-1; i>=0;i--){
			up[i].addLed(q1, "22", 200);
			q1.addWait(100);
			up[i].addLed(q1, "22", 0);
		}
		q.add(q1);
		q.addWait(100);
		setLeds( "88", 100 );
		setLeds( "22", 200 );
		q.addWait(200);
		i2c.carret.addLed( q, "22", 20 );
		Queue q2			= new Queue();
		for(int i =up.length-1; i>=0;i--){
			up[i].addLed(q1, "88", 200);
			up[i].addLed(q1, "04", 50);
			up[i].addLed(q1, "10", 50);
			up[i].addLed(q1, "08", 50);
		}
		q.add(q2);
		q.addWait(500);
		i2c.carret.addLed( q, "22", 250 );	
		moveZUp(q2, true);					// go up to help
	}

	public void moveToBottle(final int num, final boolean disableOnReady ){
		Queue q			= new Queue();
		final Upanel up	= i2c.getUpanelByBottle(num);
		moveZDown( q ,true );
		q.add( new AsyncMessage( true ){
			@Override	
			public String getName() {
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
				if( up != null ){
					up.setLed( q2, "11", 200 );
				}
				Initiator.logger.i("moveToBottle","(cx == tx && cy == ty)("+cx+" == "+tx+" && "+cy+" == "+ty+")");
				if(cx == tx && cy == ty ){		// nie musze jechac
					q2.addWait( Constant.SERVOY_REPEAT_TIME );
				}else if(cx != tx && cy == ty ){		// jade tylem lub przodem
					moveZDown(q2, disableOnReady );
					if( cy > Constant.SERVOY_BACK_NEUTRAL ){
						moveY( q2, Constant.SERVOY_BACK_NEUTRAL, true);
					}else{
						moveY( q2, Constant.SERVOY_FRONT_POS, true);	
					}
					driver_x.moveTo( q2, tx);
					moveY( q2, ty, disableOnReady);		
				}else{	// jade przodem
					moveZDown(q2, disableOnReady );
					moveY( q2, Constant.SERVOY_FRONT_POS, true);
					driver_x.moveTo( q2, tx);
					moveY( q2, ty, disableOnReady);
				}
				if( up != null ){
					up.setLed( q2, "44", 200 );
				}
				return q2;
			}
		} );
		//q.add("DY", true);
	    q.add(Constant.GETXPOS, true);
	    q.add(Constant.GETYPOS, true);
	    q.add(Constant.GETZPOS, true);
	    main_queue.add(q);
	}

	public void nalej(int num) {			// num 0-11
		Queue q = main_queue;
		int time = getPourTime(num);
		Initiator.logger.i("nalej bottle: "+ num, "czas: "+ time);

		final Upanel up	= i2c.getUpanelByBottle(num);
		q.add("EX", true);
		q.addWait(10);

		if( num != -1 ){
			int ty  	= getBottlePosY( num );
			moveY( q, ty, false );
		}

//		q.add("EZ", true);
		q.add("DY", true);
		moveZUp(q, false);
		if( up == null ){
			q.addWait( time/4 );
		//	virtualComponents.moveZLight(q, false);
			q.addWait( 3* time/4 );
		}else{
			up.setLed( q, "04", 110 );
			q.addWait( time/4 );
			moveZLight(q, false);
			q.add("DY", true);
			up.addLed( q, "04", 0 );
			q.addWait( time/4 );
			up.addLed( q, "04", 110 );
			q.addWait( time/4 );
			up.addLed( q, "04", 0 );
			q.addWait( time/4 );
			up.addLed( q, "20", 255 );
			up.addLed( q, "80", 100 );
		}
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
					if( up != null ){
						up.addLed( q2, "11", 100 );
					}
					q2.addWait( Constant.SERVOZ_PAC_TIME_WAIT );
					q2.add("Z" + Constant.SERVOZ_PAC_POS+",255", true);	
					moveZDown(q2, true );
					if( up != null ){
						up.addLed( q2, "11", 0 );
					}
					return q2;
			//	}
			//	return null;
			}
		} );
		q.add("DX", true);
	    q.add("DY", true);
	    q.addWait(100);
	    q.add("DZ", true);
	    q.add(Constant.GETXPOS, true);
	    q.add(Constant.GETYPOS, true);
	    q.add(Constant.GETZPOS, true);
	    if( up != null ){
			up.addLed( q, "ff", 0 );
		}
	}

	// todo move to slot
	public int getPourTime( int num ){			// 0 - 11
		int capacity	= getCapacity( num );
		return capacity * Constant.SERVOZ_POUR_TIME;
	}

	// todo move to slot
	public int getCapacity( int num ){			// 0 - 11
		if( num >= 0 && num < capacity.length){
			return state.getInt("BOTTLE_CAP_" + num, BarobotConnector.capacity[ num ] );
		}else{
			return state.getInt("BOTTLE_CAP_" + num, 20 );
		}
	}

	public void startDemo() {
		if(!this.ledsReady){
			this.scann_leds();
		}
		LightManager lm = new LightManager();
		lm.startDemo( this );
	}

	public void bottleBacklight( final int bottleNum, final int count ) {
		Queue q	= main_queue;
		Upanel u = i2c.getUpanelByBottle(bottleNum);
		if(u!=null){
			if(count == 1 ){
				u.setLed(q, "22", 255);			// green
			}else if( count == 2 ){
				u.setLed(q, "44", 255);			// blue
			}else if( count == 3 ){
				u.setLed(q, "66", 255);			// green + blue
			}else if( count == 4 ){
				u.setLed(q, "77", 255);			// green + blue + red
			}else if( count == 5 ){
				u.setLed(q, "88", 255);			// white
			}else if( count == 6 ){
				u.setLed(q, "ff", 255);			// white
			}else{
				u.setLed(q, "ff", 0);
			}
		}
	}
	public void setSlotMarginX( int num, int newx ) {
		this.state.set("BOTTLE_OFFSETX_" + num, newx );	
	}
	public int getSlotMarginX(int num) {
		int margin = state.getInt("BOTTLE_OFFSETX_" + num, BarobotConnector.margin_x[ num ] );
		return margin;			
	}

	public void setCapacity(int num, int cap) {
		this.state.set("BOTTLE_CAP_" + num, cap );	
	}
	
	public void setLedsOff(String string ) {
		Queue q1		= new Queue();	
		Upanel[] up		= i2c.getUpanels();
		for(int i =0; i<up.length;i++){
			up[i].addLed(q1, "ff", 0);
		}
		main_queue.add(q1);
	}
	public void setColor(String leds, int red, int green, int blue, int white) {
		Queue q1	= new Queue();
		Upanel[] up = i2c.getUpanels();
		for(int i =0; i<up.length;i++){
			up[i].setRgbw(q1, red,green,blue,white);
		}
		main_queue.add(q1);
	}
}
