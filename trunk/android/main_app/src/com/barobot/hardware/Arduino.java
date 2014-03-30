package com.barobot.hardware;

import java.io.IOException;

import android.app.Activity;

import com.barobot.common.Initiator;
import com.barobot.common.constant.Constant;
import com.barobot.common.constant.Methods;
import com.barobot.common.interfaces.HardwareState;
import com.barobot.common.interfaces.serial.SerialEventListener;
import com.barobot.common.interfaces.serial.SerialInputListener;
import com.barobot.common.interfaces.serial.Wire;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.hardware.serial.BT_wire;
import com.barobot.hardware.serial.Serial_wire;
import com.barobot.parser.Queue;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.message.Mainboard;
import com.barobot.parser.utils.Decoder;
import com.barobot.parser.utils.GlobalMatch;

public class Arduino{
	private final Object lock			= new Object();
	private static Arduino instance		= null;
	private Wire connection				= null;
	private Wire debugConnection		= null;
	public boolean stop_autoconnect		= false;
	
	private AndroidHardwareContext ahc;
	private Activity mainView;
	private BarobotConnector barobot;
	private HardwareState state;

	public static Arduino getInstance(){
		return instance;
	}

	public Arduino(BarobotConnector barobotInstance, HardwareState state) {
		instance				= this;
		this.barobot			= barobotInstance;
		this.state				= state;	
	}

	public void onStart(Activity mainView) {
		this.mainView = mainView;
		if( connection != null ){
			connection.close();
			connection = null;
		}
		connection		= new Serial_wire( mainView );
		connection.init();
		connection.setSerialEventListener( new SerialEventListener() {
			@Override
			public void onConnect() {
				barobot.main_queue.add( "\n", false );	// clean up input
				barobot.main_queue.add( "\n", false );
			}
			@Override
			public void onClose() {
			}
			@Override
			public void connectedWith(String bt_connected_device, String address) {
			}
		});

		SerialInputListener listener = barobot.willReadFrom( connection );
		barobot.willWriteThrough( connection );

	//		prepareDebugConnection();

	//	ahc = new AndroidHardwareContext( barobot, state );
		BarobotEventListener bel = new AndroidEventListener( barobot );

		final MyRetReader mrr = new MyRetReader( bel, barobot );

		barobot.mb.addGlobalRegex( new GlobalMatch(){
			@Override
			public String getMatchRet() {
				return "^Rx\\d+$";
			}
			@Override
			public boolean run(Mainboard asyncDevice, String fromArduino, String wait4Command, AsyncMessage wait_for) {
				Initiator.logger.i("Arduino.GlobalMatch.RX", fromArduino);
				fromArduino = fromArduino.replace("Rx", "");
				int hpos = Decoder.toInt(fromArduino);
				int spos = barobot.driver_x.hard2soft(hpos);
				barobot.driver_x.setSPos( spos );	

				return true;
			}
			@Override
			public String getMatchCommand() {
				return null;		// all
			}
		} );

		barobot.mb.addGlobalRegex( new GlobalMatch(){
			@Override
			public boolean run(Mainboard asyncDevice, String fromArduino, String wait4Command, AsyncMessage wait_for) {
			//	Initiator.logger.i("Arduino.GlobalMatch.METHOD_IMPORTANT_ANALOG", fromArduino);
				mrr.importantAnalog(asyncDevice, wait_for, fromArduino, false );
				return true;
			}
			@Override
			public String getMatchRet() {
				return "^" +  Methods.METHOD_IMPORTANT_ANALOG + ",.*";
			}
			@Override
			public String getMatchCommand() {
				return null;		// all
			}
		} );

		barobot.mb.addGlobalRegex( new GlobalMatch(){
			@Override
			public boolean run(Mainboard asyncDevice, String fromArduino, String wait4Command, AsyncMessage wait_for) {
			//	Initiator.logger.i("Arduino.GlobalMatch.RETURN_PIN_VALUE", fromArduino);
				//{METHOD_I2C_SLAVEMSG,my_address, RETURN_PIN_VALUE, pin,value}
				int[] parts = Decoder.decodeBytes( fromArduino );
				byte my_address = (byte) parts[1];
				byte pin		= (byte) parts[3];
				byte value		= (byte) parts[4];
				Initiator.logger.i("Arduino.POKE-BUTTON", "Address:" + my_address + ", pin: " + pin+ ", value: " + value );

				Queue q = barobot.main_queue;
				if(value > 0 ){
					q.add("L"+my_address+",ff,0", true);
				}else{
					q.add("L"+my_address+",ff,200", true);
				}
				return true;
			}
			@Override
			public String getMatchRet() {
				return "^" +  Methods.METHOD_I2C_SLAVEMSG + ",\\d+," + Methods.RETURN_PIN_VALUE + ",.*";
			}
			@Override
			public String getMatchCommand() {
				return null;		// all
			}
		} );
		barobot.mb.addGlobalRegex( new GlobalMatch(){		// METHOD_DEVICE_FOUND
			@Override
			public boolean run(Mainboard asyncDevice, String fromArduino, String wait4Command, AsyncMessage wait_for) {				
				String decoded = "Arduino.GlobalMatch.METHOD_DEVICE_FOUND";
				int[] parts = Decoder.decodeBytes( fromArduino );
				// byte ttt[4] = {METHOD_DEVICE_FOUND,addr,type,ver};
				// byte ttt[4] = {METHOD_DEVICE_FOUND,I2C_ADR_MAINBOARD,MAINBOARD_DEVICE_TYPE,MAINBOARD_VERSION};

				boolean scanning = true;
				if( scanning ){
		/*
					byte pos = getResetOrder(buffer[1]);
					i2c_reset_next( buffer[1], false );       // reset next (next to slave)
					i2c_reset_next( buffer[1], true );
					}else if( scann_order ){ 
						if( pos == 0xff ){        // nie ma na liscie?
							order[nextpos++]  = buffer[1];            // na tm miejscu slave o tym adresie
						}else{
							scann_order  =  false;
						}
					}
				*/
				}
				if(parts[2] == Constant.MAINBOARD_DEVICE_TYPE ){
					decoded += "/MAINBOARD_DEVICE_TYPE";
					int cx		= barobot.driver_x.getSPos();;
					barobot.driver_x.setM(cx);	// ostatnia znana pozycja jest marginesem
					state.set("MARGINX", cx);
					Queue mq = barobot.main_queue;
					mq.clear();
				}else if(parts[2] == Constant.UPANEL_DEVICE_TYPE ){		// upaneld
					decoded += "/UPANEL_DEVICE_TYPE";
				}else if(parts[2] == Constant.IPANEL_DEVICE_TYPE ){		// wozek
					decoded += "/IPANEL_DEVICE_TYPE";
				}
				//Initiator.logger.i("MyRetReader.decoded", decoded);
				return true;

			}
			@Override
			public String getMatchRet() {
				return "^" +  Methods.METHOD_DEVICE_FOUND + ",.*";
			}
			@Override
			public String getMatchCommand() {
				return null;
			}
		} );

		barobot.mb.addGlobalRegex(  new GlobalMatch(){		// METHOD_TEST_SLAVE
			@Override
			public boolean run(Mainboard asyncDevice, String fromArduino, String wait4Command, AsyncMessage wait_for) {
				Initiator.logger.i("Arduino.GlobalMatch.METHOD_TEST_SLAVE", fromArduino);
				return true;
			}
			@Override
			public String getMatchRet() {
				return "^" +  Methods.METHOD_TEST_SLAVE + ",.*";
			}
			@Override
			public String getMatchCommand() {
				return null;
			}
		} );

		barobot.mb.setRetReader( mrr );
	}
    protected void prepareDebugConnection() {
		SerialInputListener btl = new SerialInputListener() {
		    @Override
		    public void onRunError(Exception e) {
		    }
		    @Override
		    public void onNewData(final byte[] data, int length) {
		    	String message = new String(data, 0, length);
		  //  	Log.e("Serial input", message);
		    	barobot.main_queue.read( message );
				try {
					Arduino.getInstance().low_send(message);
				} catch (IOException e) {
					e.printStackTrace();
				}
		    }
			@Override
			public boolean isEnabled() {
				return true;
			}
		};
    	if(debugConnection !=null){
    		debugConnection.close();
    	}
		debugConnection = new BT_wire(this.mainView);
		debugConnection.setSerialEventListener( new SerialEventListener() {
			@Override
			public void onConnect() {
			}
			@Override
			public void onClose() {
			}
			@Override
			public void connectedWith(String bt_connected_device, String address) {
                state.set( "LAST_BT_DEVICE", address );    	// remember device ID
			}
		});	
		debugConnection.addOnReceive(btl);	
		debugConnection.init();
       	if( debugConnection.implementAutoConnect()){
      //  	this.runTimer(debugConnection);
        }
    //   	this.sendSomething();
	}
	public void destroy() {
		Initiator.logger.i("Arduino.destroy", "--- ON DESTROY1 ---");
		Initiator.logger.i("Arduino.destroy", "--- ON DESTROY2 ---");
		new Thread( new Runnable(){
			@Override
			public void run() {
				Initiator.logger.i("Arduino.destroy", "--- ON DESTROY3 ---");
				ahc					= null;
				Initiator.logger.i("Arduino.destroy", "--- ON DESTROY4 ---");
				barobot.destroy();
				Initiator.logger.i("Arduino.destroy", "--- ON DESTROY5 ---");
				instance			= null;
				if(connection!=null){
					connection.destroy();
				}
				Initiator.logger.i("Arduino.destroy", "--- ON DESTROY6 ---");
			}}).start();
		Initiator.logger.i("Arduino.destroy", "--- ON DESTROY7 ---");
		if(debugConnection!=null){
			debugConnection.destroy();
		}
		Initiator.logger.i("Arduino.destroy", "--- ON DESTROY8 ---");
	}
	public void resume() {
		if(connection!=null){
			connection.resume();
		}
		if(debugConnection!=null){
			debugConnection.resume();
		}
	}
	public boolean allowAutoconnect() {
		if( debugConnection == null ){
			//	Constant.log(Constant.TAG, "nie autoconnect bo juz połączony");
				return false;
		}
		if( debugConnection.isConnected() ){
		//	Constant.log(Constant.TAG, "nie autoconnect bo juz połączony");
			return false;
		}
		if( !debugConnection.implementAutoConnect() ){
			Initiator.logger.i(Constant.TAG, "nie autoconnect bo !canAutoConnect");
			return false;
		}
		if( !debugConnection.canConnect() ){
			Initiator.logger.i(Constant.TAG, "nie autoconnect bo !canConnect");
			return false;
		}
		if (stop_autoconnect == true ) {
			Initiator.logger.i(Constant.TAG, "nie autoconnect bo STOP");
			return false;
		}
		return true;
	}

    public boolean checkBT() {
    	if(debugConnection!= null){
    		return debugConnection.canConnect();
    	}
    	return false;
    }
	public void setupBT() {
		/*
		if(connection!=null){
			connection.setup();
			if(this.allowAutoconnect()){
				connection.setAutoConnect( true ); 
			}
		}*/
		if(debugConnection!=null){
			if(this.allowAutoconnect()){
				debugConnection.setAutoConnect( true ); 
			}
		}
	}
    public synchronized void low_send( String command ) throws IOException {		// wyslij bez interpretacji
		if(connection == null){
			return;		// jestem w trakcie oczekiwania
		}
		Initiator.logger.i("Arduino.low_send", command );
    	connection.send(command);
    }
    public synchronized void debug( String command ){		// wyslij bez interpretacji
		if(debugConnection!=null ){
			try {
				debugConnection.send(command);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
    }

	public void connectId(String address) {
		Initiator.logger.i("Arduino.connectId", "autoconnect z: " +address);
		if(debugConnection!=null){
			debugConnection.connectToId(address);
		}
	}
	
	public void resetSerial() {
		if( connection != null ){
			connection.reset();
		}
	}
}

/*
AlertDialog.Builder builder = new AlertDialog.Builder(barobotMain);
String[] name = new String[1];
//	lowHardware[1]	=
//		lowHardware[2]	= new ADB_wire();

name[0] = lowHardware[0].getName();
//name[1] = lowHardware[1].getName();
//name[2] = lowHardware[2].getName();
builder.setTitle("Wybierz typ połączenia z robotem");
builder.setCancelable(false);
builder.setItems(name, new DialogInterface.OnClickListener() {
	@Override
 	public void onClick(DialogInterface dialog, int which) {
          switch(which){
             case 0:
            	 prepareConnection(lowHardware[0],  new BT_wire());
            	 break;
            case 1:
            	 prepareConnection(lowHardware[1], lowHardware[1]);
            	 break;
            	
             case 2:
            	 prepareConnection(lowHardware[2], lowHardware[1]);
            	 break;
           	default:
            	 barobotMain.finish();
            	 break;
          }
      }
});
builder.show();
*/

/*
    private void sendSomething(){
   		stopping = false;

   		Log.d("serial", "sendSomething");
	    Runnable tt = new Runnable(){
	        @Override
	        public void run() {
	            Random generator = new Random( 19580427 );
	            Log.d("serial", "Start writter");
	            while(!stopping && connection != null ){
	            	if( connection.isConnected()){
		                int r = generator.nextInt();
		                String test = "hello arduino "+ r + "\n";
		                send(test);
		                try {
		                    Thread.sleep(500);
		                } catch (InterruptedException e) {
		                    e.printStackTrace();
		                }
	            	}
	            }
	            Log.d("serial", "koniec writter");
	        }};
	        Thread writer = new Thread(tt);
	        writer.start();
    }

    private void runTimer( final Wire connection ) {
//    	interval inn = new interval();
//   	inn.run(1000,5000);
//    	this.inters.add(inn);
    	interval inn = new interval(new Runnable() {
    		private int count = 0;
		    public void run() {
		    	Arduino ar = Arduino.getInstance();
		        if( ar.allowAutoconnect()){
		        	count++;
		        	if(count > 2){		// po 10 sek
		//        		Constant.log("RUNNABLE", "3 try autoconnect" );
		        		connection.setAutoConnect( true ); 
		        	}
			    }else{
			    	count = 0;
		        }
		   }
		});
    	inn.run(1000,5000);
    	inn.pause();
    	AppInvoker.getInstance().inters.add(inn);
	}
*/