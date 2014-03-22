package com.barobot.hardware;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.widget.ArrayAdapter;
import com.barobot.common.Initiator;
import com.barobot.common.constant.Constant;
import com.barobot.common.constant.Methods;
import com.barobot.common.interfaces.SerialEventListener;
import com.barobot.common.interfaces.SerialInputListener;
import com.barobot.common.interfaces.Wire;
import com.barobot.hardware.serial.BT_wire;
import com.barobot.hardware.serial.Serial_wire;
import com.barobot.parser.History_item;
import com.barobot.parser.Queue;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.output.AsyncDevice;
import com.barobot.parser.output.Mainboard;
import com.barobot.parser.utils.Decoder;
import com.barobot.parser.utils.GlobalMatch;

public class Arduino{
	private final Object lock			= new Object();
	private static Arduino instance		= null;
	private Wire connection				= null;
	private Wire debugConnection		= null;
	public boolean stop_autoconnect		= false;
	private Queue main_queue			= null;
	public List<History_item> mConversationHistory;
	private int mainboardSource;
	private AndroidHardwareContext ahc;
	Mainboard mb					= null;
	private Activity mainView;

	public static Arduino getInstance(){
		if (instance == null){
			instance = new Arduino();
		}
		return instance;
	}
	private Arduino() {
		instance			= this;
		main_queue  		= new Queue( true );
		ahc					= new AndroidHardwareContext();
		mConversationHistory = new ArrayList<History_item>();
		mb					= new Mainboard();
	}
	public void onStart(Activity mainView) {
		this.mainView = mainView;
		if( connection != null ){
			connection.close();
			connection = null;
		}
		if(connection !=null){
			connection.close();
		}
		mainboardSource = main_queue.registerSource( mb );
		connection		= new Serial_wire( mainView );
		connection.addOnReceive( new SerialInputListener(){
			@Override
			public void onNewData(byte[] data, int length) {
				String message = new String(data, 0, length);
		//		Log.e("Serial addOnReceive", message);
				mb.read( message );
	//			debug( message );
			}
			@Override
			public void onRunError(Exception e) {
			}
			@Override
			public boolean isEnabled() {
				return true;
			}});
		connection.init();

		connection.setSerialEventListener( new SerialEventListener() {
			@Override
			public void onConnect() {
				main_queue.add( "\n", false );	// clean up input
				main_queue.add( "\n", false );
			}
			@Override
			public void onClose() {
			}
			@Override
			public void connectedWith(String bt_connected_device, String address) {
			}
		});

		mb.registerSender( connection );		
		Queue.enableDevice( mainboardSource );
	//		prepareDebugConnection();
		

		final MyRetReader mrr = new MyRetReader( ahc );

		mb.addGlobalRegex( new GlobalMatch(){
			@Override
			public String getMatchRet() {
				return "^Rx\\d+$";
			}
			@Override
			public boolean run(AsyncDevice asyncDevice, String fromArduino, String wait4Command, AsyncMessage wait_for) {
				Initiator.logger.i("Arduino.GlobalMatch.RX", fromArduino);
				fromArduino = fromArduino.replace("Rx", "");
				int hpos = Decoder.toInt(fromArduino);
				int spos = virtualComponents.driver_x.hard2soft(hpos);
				virtualComponents.saveXPos( spos );
				int lx	=  virtualComponents.state.getInt("LENGTHX", 600 );
				if( spos > lx){		// Pozycja wieksza niz długosc? Zwieksz długosc
					virtualComponents.state.set( "LENGTHX", "" + spos);
				}
				return true;
			}
			@Override
			public String getMatchCommand() {
				return null;		// all
			}
		} );

		mb.addGlobalRegex( new GlobalMatch(){
			@Override
			public boolean run(AsyncDevice asyncDevice, String fromArduino, String wait4Command, AsyncMessage wait_for) {
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

		mb.addGlobalRegex( new GlobalMatch(){
			@Override
			public boolean run(AsyncDevice asyncDevice, String fromArduino, String wait4Command, AsyncMessage wait_for) {
			//	Initiator.logger.i("Arduino.GlobalMatch.RETURN_PIN_VALUE", fromArduino);
				//{METHOD_I2C_SLAVEMSG,my_address, RETURN_PIN_VALUE, pin,value}
				int[] parts = Decoder.decodeBytes( fromArduino );
				byte my_address = (byte) parts[1];
				byte pin		= (byte) parts[3];
				byte value		= (byte) parts[4];
				Initiator.logger.i("Arduino.POKE-BUTTON", "Address:" + my_address + ", pin: " + pin+ ", value: " + value );

				Queue q = Arduino.getInstance().getMainQ();
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
		mb.addGlobalRegex( new GlobalMatch(){		// METHOD_DEVICE_FOUND
			@Override
			public boolean run(AsyncDevice asyncDevice, String fromArduino, String wait4Command, AsyncMessage wait_for) {
				mrr.deviceFound(asyncDevice, wait_for, fromArduino);
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

		mb.addGlobalRegex(  new GlobalMatch(){		// METHOD_TEST_SLAVE
			@Override
			public boolean run(AsyncDevice asyncDevice, String fromArduino, String wait4Command, AsyncMessage wait_for) {
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

		mb.setRetReader( mrr );
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
		    	main_queue.read( mainboardSource, message );
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
				// TODO Auto-generated method stub
			}
			@Override
			public void onClose() {
				// TODO Auto-generated method stub
			}
			@Override
			public void connectedWith(String bt_connected_device, String address) {
                virtualComponents.state.set( "LAST_BT_DEVICE", address );    	// remember device ID
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
		if(debugConnection!=null){
			debugConnection.destroy();
		}
		Initiator.logger.i("Arduino.destroy", "--- ON DESTROY2 ---");
		new Thread( new Runnable(){
			@Override
			public void run() {
				Initiator.logger.i("Arduino.destroy", "--- ON DESTROY3 ---");
				mConversationHistory = null;
				ahc					= null;
				mb					= null;
				instance			= null;
				Initiator.logger.i("Arduino.destroy", "--- ON DESTROY4a ---");
				main_queue.destroy();
				Initiator.logger.i("Arduino.destroy", "--- ON DESTROY4 ---");
				main_queue = null;
				Initiator.logger.i("Arduino.destroy", "--- ON DESTROY5 ---");
				if(connection!=null){
					connection.destroy();
				}
				Initiator.logger.i("Arduino.destroy", "--- ON DESTROY6 ---");
			}}).start();
		Initiator.logger.i("Arduino.destroy", "--- ON DESTROY5 ---");
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

    public boolean log_active	= true;
	private ArrayAdapter<History_item> mConversation;
	public void addToList(final History_item m) {
		if(log_active){
			synchronized (this.lock) {
				mConversationHistory.add( m );
			}
			if(this.mConversation !=null){
				this.mainView.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if(Arduino.this.mConversation !=null){
				 			Arduino.this.mConversation.add(m);
				 			Arduino.this.mConversation.notifyDataSetChanged();
						}
					}
				});
			}
		}
	}

	public void addToList(final String string, final boolean direction ) {
		if(log_active){
			this.mainView.runOnUiThread(new Runnable() {
			     public void run() {
			//    	Log.i(Constant.TAG, "addtohist:[" + string +"]"); 
			    	History_item hi = new History_item( string.trim(), direction);
		    		mConversationHistory.add( hi );
			 		if(Arduino.this.mConversation !=null){
			 			Arduino.this.mConversation.add(hi);
			 			Arduino.this.mConversation.notifyDataSetChanged();
					} 
			    }
			});
		}
	}
	public void clearHistory() {
		if(log_active){
			mConversationHistory.clear();
			if(this.mConversation !=null){
				this.mConversation.clear();
			}
		}
	}
	public List<History_item> getHistory(){
		return mConversationHistory;
	}
	public void connectId(String address) {
		Initiator.logger.i("Arduino.connectId", "autoconnect z: " +address);
		if(debugConnection!=null){
			debugConnection.connectToId(address);
		}
	}
	public void getHistory(ArrayAdapter<History_item> mConversation) {
		this.mConversation = mConversation;
		this.mConversation.addAll(mConversationHistory);
	}

	public Queue getMainQ() {
		return main_queue;
	}
	public void resetSerial() {
		if( connection != null ){
			connection.close();
			connection		= null;
			connection		= new Serial_wire( this.mainView );
			connection.addOnReceive( new SerialInputListener(){
				@Override
				public void onNewData(byte[] data, int length) {
					String message = new String(data, 0, length);
			//		Log.e("Serial addOnReceive", message);
					mb.read( message );
		//			debug( message );
				}
				@Override
				public void onRunError(Exception e) {
				}
				@Override
				public boolean isEnabled() {
					return true;
				}});
			connection.init();
		}
	}
}

/*
public boolean addRetToList( final String last, final String ret ) {
	final DebugWindow dd = DebugWindow.getInstance();
	if(dd!=null){
		int count = dd.mConversationArrayAdapter.getCount();
		for(int i =count-1; i>=0;i--){
			History_item hi = dd.mConversationArrayAdapter.getItem(i);
		//	Constant.log("+addRetToList", last + "/" + ret + "/" + i +"/"+ hi.getCommand() );
			if( hi.direction && hi.getCommand().equals(last)){
				hi.setRet(ret);
	//			Constant.log("+addRetToList","ustawiam " + i + " na " + hi.toString() );
				dd.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						dd.mConversationArrayAdapter.notifyDataSetChanged();
					}
				});
				return true;
			}			
		}
	}
	return false;
}
*/

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