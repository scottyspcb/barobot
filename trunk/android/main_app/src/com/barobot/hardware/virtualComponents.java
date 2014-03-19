package com.barobot.hardware;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.barobot.activity.DebugActivity;
import com.barobot.common.BarobotConnector;
import com.barobot.common.Initiator;
import com.barobot.common.constant.Constant;
import com.barobot.common.constant.Methods;
import com.barobot.parser.Queue;
import com.barobot.parser.devices.MotorDriver;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.output.AsyncDevice;
import com.barobot.parser.utils.Decoder;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
public class virtualComponents {

	public static Activity application;
	private static SharedPreferences myPrefs;
	private static SharedPreferences.Editor config_editor;			// config systemu android
	private static Map<String, String> hashmap = new HashMap<String, String>();

	public static boolean need_glass_up = false;
	public static boolean pac_enabled = true;
	public static final int ANALOG_WAGA = 2;
	public static final int SERVOY_REPEAT_TIME = 2000;

	private static String[] persistant = {
		"POSX",
		"POSY",
		"POSY",
		"X_GLOBAL_MIN",
		"X_GLOBAL_MAX",
		"LENGTHX","LAST_BT_DEVICE",
		"POS_START_X",
		"POS_START_Y",
		"NEUTRAL_POS_Y",
		"NEUTRAL_POS_Z",
		"ENDSTOP_X_MIN",
		"ENDSTOP_X_MAX",
		"ENDSTOP_Y_MIN",
		"ENDSTOP_Y_MAX",
		"ENDSTOP_Z_MIN",
		"ENDSTOP_Z_MAX",
		"BOTTLE_X_0","BOTTLE_Y_0",
		"BOTTLE_X_1","BOTTLE_Y_1",
		"BOTTLE_X_2","BOTTLE_Y_2",
		"BOTTLE_X_3","BOTTLE_Y_3",
		"BOTTLE_X_4","BOTTLE_Y_4",
		"BOTTLE_X_5","BOTTLE_Y_5",
		"BOTTLE_X_6","BOTTLE_Y_6",
		"BOTTLE_X_7","BOTTLE_Y_7",
		"BOTTLE_X_8","BOTTLE_Y_8",
		"BOTTLE_X_9","BOTTLE_Y_9",
		"BOTTLE_X_10","BOTTLE_Y_10",
		"BOTTLE_X_11","BOTTLE_Y_11",
	};

	public static MotorDriver driver_x;
	public static boolean scann_bottles = false;
	public static boolean set_bottle_on = false;
	public static boolean ledsReady = false;
	private static Carret carret;
	
	public static void init( Activity app ){
		application			= app;
		myPrefs				= application.getSharedPreferences(Constant.SETTINGS_TAG, Context.MODE_PRIVATE);
		config_editor		= myPrefs.edit();
		driver_x			= new MotorDriver();
		carret				= new Carret( 2, 10 );
		driver_x.defaultSpeed = BarobotConnector.DRIVER_X_SPEED;
		driver_x.setSPos( virtualComponents.getInt( "POSX", 0 ) );
	}
	public static String get( String name, String def ){
		String ret = hashmap.get(name);
		if( ret == null ){ 
			if((Arrays.asList(persistant).indexOf(name) > -1 )){
				ret = myPrefs.getString(name, def );
			}else{
				ret = def;
			}
		}
		return ret;
	}
	public static int getPourTime( int num ){
		if( num > 0 && num < BarobotConnector.times.length){
			return BarobotConnector.times[num];
		}
		return BarobotConnector.SERVOZ_POUR_TIME;
	}
	
	public static int getInt( String name, int def ){
		return Decoder.toInt(virtualComponents.get( name, ""+def ));
	}
	public static void set(String name, long value) {
		virtualComponents.set(name, "" + value );
	}
	public static void set( String name, String value ){
	//	if(name == "POSX"){
	//		Initiator.logger.i("virtualComponents.set","save: "+name + ": "+ value );	
	//	}
		hashmap.put(name, value );
		virtualComponents.update( name, value );

		int remember = Arrays.asList(persistant).indexOf(name);			// czy zapisac w configu tą wartosc?
		if(remember > -1){
			config_editor.putString(name, value);
			config_editor.commit();
		}
	}
	public static int getBottlePosX( int i ) {
		return virtualComponents.getInt("BOTTLE_X_" + i, BarobotConnector.b_pos_x[i]);
	}
	public static int getBottlePosY( int i ) {
		return virtualComponents.getInt("BOTTLE_Y_" + i, BarobotConnector.b_pos_y[i]);
	}
	private static void update(String name, String value) {
		final DebugActivity dialog = DebugActivity.getInstance();
		if(dialog!=null){
			dialog.update(name, value );
		}
	}
	// zapisz ze tutaj jest butelka o danym numerze
	public static void hereIsBottle(int i, int posx, int posy) {
		//Constant.log(Constant.TAG,"zapisuje pozycje:"+ i + " " +posx+ " " + posy );
		virtualComponents.set("BOTTLE_X_" + i, ""+posx );
		virtualComponents.set("BOTTLE_Y_" + i, ""+posy );
	}
	// zapisz ze tutaj jest butelka o danym numerze
	public static void hereIsBottle(int i) {
		String posx		=  virtualComponents.get("POSX", "0" );	
		String posy		=  virtualComponents.get("POSY", "0" );
	//	Constant.log(Constant.TAG,"zapisuje pozycje:"+ i + " " +posx+ " " + posy );
		virtualComponents.set("BOTTLE_X_" + i, posx );
		virtualComponents.set("BOTTLE_Y_" + i, posy );
	}
	public static boolean hasGlass() {
		return false;
	}
	public static void pacpac() {
		Queue q = Arduino.getInstance().getMainQ();
		Initiator.logger.i(Constant.TAG,"pac");
	//	q.add( moveX );
		q.add("EX", true);
//		q.add("EY", true);
//		q.add("EZ", true);
		q.add("Z" + BarobotConnector.SERVOZ_PAC_POS+","+BarobotConnector.DRIVER_Z_SPEED, true);
		Initiator.logger.i("pacpac","Z" + BarobotConnector.SERVOZ_PAC_POS+","+BarobotConnector.DRIVER_Z_SPEED);
		virtualComponents.moveZDown(q, true );
		q.add("DY", true);
		q.add("DX", true);
		q.addWait(200);
		q.add("DZ", true);
	}

	public static void cancel_all() {
		Queue mq = Arduino.getInstance().getMainQ();
		mq.clearAll();
		mq.add("LIVE A OFF", false );
//		add("EZ");
		int poszdown	=  virtualComponents.getInt("ENDSTOP_Z_MIN", BarobotConnector.SERVOZ_DOWN_POS );
		mq.add("Z" + poszdown, false );		// zwraca początek operacji a nie koniec
		mq.add("DX", false );
		mq.add("DY", false );
		mq.add("DZ", false );
		mq.add(Constant.GETXPOS, false );
	}
	public static void stop_all() {
		Queue mq = Arduino.getInstance().getMainQ();
		mq.clearAll();
	}

	public static void moveToBottle(final int num, final boolean disableOnReady ){
		Arduino ar		= Arduino.getInstance();
		Queue q			= new Queue();
		final Upanel up	= getUpanelBottle(num);
		moveZDown( q ,true );
		q.add( new AsyncMessage( true ){
			@Override
			public Queue run(AsyncDevice dev, Queue queue) {
				this.name	= "check position";
				int cx		= virtualComponents.driver_x.getSPos();		// czy ja juz jestem na tej pozycji?	
				int cy		= virtualComponents.getInt("POSY", 0 );
				int tx 		= getBottlePosX( num );
				int ty  	= getBottlePosY( num );
				Queue	q2	= new Queue();
				if( up != null ){
					up.setLed( q2, "ff", 0 );
					up.setLed( q2, "11", 200 );
				}
				Initiator.logger.i("moveToBottle","(cx == tx && cy == ty)("+cx+" == "+tx+" && "+cy+" == "+ty+")");
				if(cx == tx && cy == ty ){		// nie musze jechac
					q2.addWait( virtualComponents.SERVOY_REPEAT_TIME );
				}else if(cx != tx && cy == ty ){		// jade tylem lub przodem
					virtualComponents.moveZDown(q2, disableOnReady );
					if( cy > BarobotConnector.SERVOY_BACK_NEUTRAL ){
						virtualComponents.moveY( q2, BarobotConnector.SERVOY_BACK_NEUTRAL, true);
					}else{
						virtualComponents.moveY( q2, BarobotConnector.SERVOY_FRONT_POS, true);	
					}
					virtualComponents.moveX( q2, tx);
					virtualComponents.moveY( q2, ty, disableOnReady);		
				}else{	// jade przodem
					virtualComponents.moveZDown(q2, disableOnReady );
					virtualComponents.moveY( q2, BarobotConnector.SERVOY_FRONT_POS, true);
					virtualComponents.moveX( q2, tx);
					virtualComponents.moveY( q2, ty, disableOnReady);
				}
				if( up != null ){
					up.setLed( q2, "ff", 0 );
					up.setLed( q2, "44", 200 );
				}
				return q2;
			}
		} );
		//q.add("DY", true);
	    q.add(Constant.GETXPOS, true);
	    q.add(Constant.GETYPOS, true);
	    q.add(Constant.GETZPOS, true);
	    ar.getMainQ().add(q);
	}

	static Upanel getUpanelBottle(int num) {
		if( num >= 0 && num < BarobotConnector.upanels.length ){
			int addr = BarobotConnector.upanels[ num ];
			return new Upanel( 0 , addr);
		}
		return null;
	}
	public static void nalej(int num) {			// num 0-11
		Queue q = Arduino.getInstance().getMainQ();
		int time = getPourTime(num);
		final Upanel up	= getUpanelBottle(num);
		q.add("EX", true);
//		q.add("EY", true);
//		q.add("EZ", true);
		virtualComponents.moveZUp(q, false);
		if( up == null ){
			q.addWait( time/4 );
			virtualComponents.moveZLight(q, false);
			q.add("DY", true);
			q.addWait( 3* time/4 );
		}else{
			up.setLed( q, "ff", 0 );
			up.setLed( q, "04", 20 );
			q.addWait( time/4 );
			virtualComponents.moveZLight(q, false);
			q.add("DY", true);
			up.setLed( q, "04", 50 );
			q.addWait( time/4 );
			up.setLed( q, "04", 100 );
			q.addWait( time/4 );
			up.setLed( q, "04", 200 );
			q.addWait( time/4 );
			up.setLed( q, "20", 255 );
			up.setLed( q, "80", 100 );
		}
		q.add("DY", true);
		virtualComponents.moveZDown(q,false);
		q.add( new AsyncMessage( true ) {
			@Override
			public Queue run(AsyncDevice dev, Queue queue) {
				this.name		= "pacpac";
				if(virtualComponents.pac_enabled){
					Queue	q2	= new Queue();	
					q2.addWait( BarobotConnector.SERVOZ_PAC_TIME_WAIT );
					q2.add("Z" + BarobotConnector.SERVOZ_PAC_POS+",255", true);	
					virtualComponents.moveZDown(q2, true );
					return q2;
				}
				return null;
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
			up.setLed( q, "ff", 0 );
		}
	}

	public static void enable_analog( Queue q, int pin, int time, int repeat) {
		q.add("LIVE A "+pin+","+time+","+repeat, false);		// repeat pomiary co time na porcie pin
	}
	public static void disable_analog(Queue q, int analogWaga) {
		q.add("LIVE A OFF", false);
	}

	public static void moveX( final Queue q, int pos ) {
		final int newx		= driver_x.soft2hard(pos);
		final int currentx	= driver_x.getSPos();

		q.add( new AsyncMessage( true, true ) {
			@Override
			public boolean isRet(String result, Queue mainQueue) {
				return false;
			}
			@Override
			public Queue run(AsyncDevice dev, Queue queue){
				this.name		= "Check Hall X";
				q.sendNow(Queue.DFAULT_DEVICE, "A0");
				return null;
			}
			@Override
			public boolean onInput(String input, AsyncDevice dev, Queue mainQueue) {
				Initiator.logger.w("MotorDriver.movoTo.AsyncMessage.onInput", input );
				if(input.matches("^" +  Methods.METHOD_IMPORTANT_ANALOG + ",0,.*" )){		//	224,0,66,0,208,7,15,2
					int[] parts = Decoder.decodeBytes( input );
					boolean can = true;
					if( parts[2] == Methods.HX_STATE_9 ){		// this is max	//	224,0,100,0,204,3,185,1
						if(newx < currentx ){		// move backward
							can = false;
							Initiator.logger.w("MotorDriver.movoTo.AsyncMessage.onInput", "OVER max" );
						}
					}else if( parts[2] == Methods.HX_STATE_1 ){		// this is min
						if( newx > currentx ){		// move forward
							can = false;
							Initiator.logger.w("MotorDriver.movoTo.AsyncMessage.onInput", "BELOW MIN" );
						}
					}
					if( can ){
						Initiator.logger.w("MotorDriver.movoTo.AsyncMessage.onInput", "MOVE" );
						Queue	q2	= new Queue(); 
						q2.add("X" + newx+ ","+virtualComponents.driver_x.defaultSpeed, true);
						mainQueue.addFirst(q2);
						dev.unlockRet(this, "A0 OK");
						return true;
					}
					dev.unlockRet(this, "A0 FAIL");
				}
				return false;
			}
		});
	}
	public static void moveX( Queue q, String pos ) {
		moveX(q, Decoder.toInt(pos));
	}
	public static void moveY( Queue q, int pos, boolean disableOnReady ) {
		q.add("Y" + pos+ ","+BarobotConnector.DRIVER_Y_SPEED, true);
		if(disableOnReady){
			q.add("DY", true );
		}
	}
	public static void moveY( Queue q, String pos ) {
		q.add("Y" + pos+ ","+BarobotConnector.DRIVER_Y_SPEED, true);	
	}
	public static void hereIsStart( int posx, int posy) {
		//Constant.log(Constant.TAG,"zapisuje start:" +posx+ " " + posy );
		virtualComponents.set("POS_START_X", posx );
		virtualComponents.set("POS_START_Y", posy );
	}
	public static void moveZDown(Queue q, boolean disableOnReady) {
		int poszdown	=  virtualComponents.getInt("ENDSTOP_Z_MIN", BarobotConnector.SERVOZ_DOWN_POS );
		moveZ(q, poszdown );
		q.add("DZ", true);
	}
	private static void moveZ(Queue q, int pos) {
		q.add("Z" + pos +","+BarobotConnector.DRIVER_Z_SPEED, true);
		q.addWait(300);
	}

	private static void moveZLight(Queue q, boolean disableOnReady) {
//		q.add("EZ", true);
		int poszup	=  BarobotConnector.SERVOZ_UP_LIGHT_POS;
		q.add("Z" + poszup+","+BarobotConnector.DRIVER_Z_SPEED, true);
	//	q.addWait( virtualComponents.SERVOZ_UP_TIME );	// wiec trzeba poczekać
		if(disableOnReady){
			q.addWait(300);
			q.add("DZ", true);
		}
	}
	public static void moveZUp( Queue q, boolean disableOnReady ) {
//		q.add("EZ", true);
		int poszup	=  BarobotConnector.SERVOZ_UP_POS;
		q.add("Z" + poszup+","+BarobotConnector.DRIVER_Z_SPEED, true);
	//	q.addWait( virtualComponents.SERVOZ_UP_TIME );	// wiec trzeba poczekać
		if(disableOnReady){
			q.addWait(300);
			q.add("DZ", true);
		}
	}

	public static void moveToStart() {
		Queue q = Arduino.getInstance().getMainQ();
		moveZDown( q ,true );
		q.add( new AsyncMessage( true ) {
			@Override
			public Queue run(AsyncDevice dev, Queue queue) {
				this.name		= "check position";
				int posx		= driver_x.getSPos();;		// czy ja juz jestem na tej pozycji?	
				int posy		= virtualComponents.getInt("POSY", 0 );
				int sposx		= virtualComponents.getInt("POS_START_X", 0 );		// tu mam byc
				int sposy		= virtualComponents.getInt("POS_START_X", 0 );

				if(posx != sposx || posy != sposy ){		// musze jechac?
					Queue	q2	= new Queue();
					virtualComponents.moveZDown(q2, true );
					//virtualComponents.moveY( q2, virtualComponents.get("NEUTRAL_POS_Y", "0" ));
					virtualComponents.moveY( q2, BarobotConnector.SERVOY_FRONT_POS, true );
					virtualComponents.moveX( q2, sposx);
					virtualComponents.moveY( q2, sposy, true );
					return q2;
				}
				return null;
			}
		} );
	    q.add(Constant.GETXPOS, true);
	    q.add(Constant.GETYPOS, true);
	    q.add(Constant.GETZPOS, true);

		carret.setLed( q, "ff", 0 );
		carret.setLed( q, "22", 250 );

	    virtualComponents.setLeds( "ff", 0 );
	    q.addWait(500);
		Queue q1			= new Queue();
		for(int i =BarobotConnector.front_upanels.length-1; i>=0;i--){
			q1.add("L"+ BarobotConnector.upanels[i] +",22,200", true);
			q1.addWait(100);
			q1.add("L"+ BarobotConnector.upanels[i] +",22,0", true);
		}
		q.add(q1);
		q.addWait(100);
	    virtualComponents.setLeds( "88", 100 );
	    virtualComponents.setLeds( "22", 200 );
		q.addWait(500);
		carret.setLed( q, "22", 20 );
		q.addWait(500);
		carret.setLed( q, "22", 250 );
		q.addWait(500);
		carret.setLed( q, "22", 20 );
		q.addWait(500);
		carret.setLed( q, "22", 250 );

		virtualComponents.setLeds( "88", 0 );
	}
	
	public static void startDoingDrink() {
		Queue q = Arduino.getInstance().getMainQ();
		carret.setLed( q, "ff", 0 );
		carret.setLed( q, "11", 250 );
		Queue q1		= new Queue();	
		for(int i =0; i<BarobotConnector.upanels.length;i++){
			q1.add("L"+ BarobotConnector.upanels[i] +",ff,0", true);
			q1.add("L"+ BarobotConnector.upanels[i] +",ff,200", true);
		}
		q.add(q1);
	}

	public static void kalibrcja() {
		Queue q			= Arduino.getInstance().getMainQ();
		q.add( "\n", false );
		q.add( "\n", false );
		virtualComponents.setLeds( "ff", 0 );
		int posx		= driver_x.getSPos();
		for(int i=0;i<12;i++){
			virtualComponents.set("BOTTLE_X_" + i, "0" );
			virtualComponents.set("BOTTLE_Y_" + i, "0" );
		}
		virtualComponents.set("POS_START_X", "0" );
		virtualComponents.set("POS_START_Y", "0" );

		Initiator.logger.i("+find_bottles", "start");
		q.add("EX", true );
		virtualComponents.moveZDown( q ,true );
		q.addWait(100);
		virtualComponents.moveZ( q, BarobotConnector.SERVOZ_TEST_POS );
		q.addWait(100);
		virtualComponents.moveZDown( q ,true );
		q.addWait(200);
		virtualComponents.moveY( q, BarobotConnector.SERVOY_TEST_POS, true);
		q.addWait(200);
		virtualComponents.moveY( q, BarobotConnector.SERVOY_FRONT_POS, true);
		q.addWait(200);
		int lengthx19	=  virtualComponents.getInt("LENGTHX", 60000 );	
		
		Initiator.logger.i("+find_bottles", "up");
		virtualComponents.moveX( q, posx + 2000);
		q.addWait(100);
		virtualComponents.moveX( q, -70000 );	// read margin
		q.addWait(100);
		// scann Triggers
		q.add( new AsyncMessage( true ) {			// go up
			@Override
			public Queue run(AsyncDevice dev, Queue queue) {
				this.name		= "scanning up";
				virtualComponents.driver_x.defaultSpeed = 1000;
				Initiator.logger.i("+find_bottles", "up");
				virtualComponents.scann_bottles = true;
				return null;
			}
		} );
		virtualComponents.moveX( q, 30000 );		// go down
		
		q.add( new AsyncMessage( true ) {
			@Override
			public Queue run(AsyncDevice dev, Queue queue) {
				this.name		= "scanning back";
				virtualComponents.driver_x.defaultSpeed = BarobotConnector.DRIVER_X_SPEED;
				Initiator.logger.i("+find_bottles", "down kalibrcja");
				return null;
			}
		} );
		virtualComponents.moveX( q, -lengthx19);			// down to 0
		q.add( new AsyncMessage( true ) {
			@Override
			public Queue run(AsyncDevice dev, Queue queue) {
				this.name		= "end scanning";
				Initiator.logger.i("+find_bottles", "koniec kalibrcja");
				virtualComponents.scann_bottles = false;
				boolean error = false;
				for(int i=0;i<12;i++){
					int xpos = virtualComponents.getInt("BOTTLE_X_" + i, 0 );
					int ypos = virtualComponents.getInt("BOTTLE_Y_" + i, 0 );
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
	public static void scann_leds() {
		Queue q			= Arduino.getInstance().getMainQ();
		findOrder( q, 3 );
		findOrder( q, 4 );

		Queue q1			= new Queue();
		Queue q2			= new Queue();		
		for(int i =0; i<BarobotConnector.upanels.length;i++){
			q1.add("L"+ BarobotConnector.upanels[i] +",ff,200", true);
			q2.add("L"+ BarobotConnector.upanels[i] +",ff,0", true);
		}

		q.add(q1);
		q.addWait(1000);
		q.add(q2);
		ledsReady = true;	
	}


	private static void findOrder(Queue q, int index) {
		int current_index		= 0;

		final Thread t = new Thread( new Runnable(){
			@Override
			public void run() {	
				
				
				
			}
		});
		
		
		
		String command = "N" + index;
		q.add( new AsyncMessage( command, true ){
			@Override
			public boolean isRet(String result, Queue q) {
				if(result.startsWith("" + Methods.METHOD_I2C_SLAVEMSG + ",")){		//	122,1,188,1
					int[] bytes = Decoder.decodeBytes(result);
					if(bytes[2] == Methods.METHOD_CHECK_NEXT){
						if(bytes[3] == 1 ){							// has next
							System.out.println("has next? 1");
						}else{
							System.out.println("has next? 0");
						}
						return true;
					}
				}
				return false;
			}
		});
	}

	
	public static void saveXPos(int spos) {
		virtualComponents.set( "POSX", "" + spos);
		driver_x.setSPos( spos );	
	}
	public static void setLeds(String string, int value ) {
		Queue q1			= new Queue();
		Queue q2			= new Queue();		
		for(int i =0; i<BarobotConnector.upanels.length;i++){
			q1.add("L"+ BarobotConnector.upanels[i] +",ff,0", true);
			q1.add("L"+ BarobotConnector.upanels[i] +","+ string +"," + value, true);
		}
		Queue q			= Arduino.getInstance().getMainQ();
		q.add(q1);
		q.addWait(1000);
		q.add(q2);
		ledsReady = true;
	}
}
