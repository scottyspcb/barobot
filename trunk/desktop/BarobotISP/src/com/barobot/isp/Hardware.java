package com.barobot.isp;


import com.barobot.common.IspSettings;
import com.barobot.common.interfaces.SerialInputListener;
import com.barobot.parser.Queue;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.output.AsyncDevice;
import com.barobot.parser.output.Mainboard;
import com.barobot.tester.connection.WindowsSerialPort;

public class Hardware {
	private WindowsSerialPort serial	= null;
	private Queue q						= new Queue( true );
	public String comPort				= "com1";
	static int mainboardSource			= 0;

	public Hardware(String comPort) {
		init();
		this.comPort = comPort;
		serial = new WindowsSerialPort( comPort, IspSettings.fullspeed );
	}
	public void init() {
		Mainboard mb	= new Mainboard();
		/*
			mb.addGlobalRegex("^E.+", new GlobalMatch(){
				public boolean run( String in ) {
					Parser.log(Level.INFO, "Mainboard init: " + in);
					return true;
				}});
*/
		mainboardSource = q.registerSource( mb );
		Queue.enableDevice( mainboardSource );
	}

	public void connect() {
		if(serial.isConnected()){
			return;
		}
		boolean res = serial.open();
		if(!res ){
			System.exit(-1);
		}
		try {
			final AsyncDevice mb	= Queue.getDevice( mainboardSource );
			mb.registerSender( serial );
			SerialInputListener listener = new SerialInputListener() {
				public void onRunError(Exception e) {
					// TODO Auto-generated method stub
				}
				public void onNewData(byte[] data, int length) {
					String in = new String(data, 0, length);
					mb.read( in );
				}
				public boolean isEnabled() {
					return true;
				}
			};
			serial.addOnReceive( listener );

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	public void closeOnReady() {
		q.add( new AsyncMessage( true ){		// na koncu zamknij
			@Override
			public Queue run(AsyncDevice dev, Queue queue) {
				close();
				return null;
			}
		});
	}
	protected void close() {
		serial.close();
		if(q != null){
			q.clear( mainboardSource );
			Queue.disableDevice( mainboardSource );
		}
	}
	public Queue getQueue() {
		return q;
	}
}
