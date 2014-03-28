package com.barobot.isp;

import com.barobot.common.EmptyBarobotState;
import com.barobot.common.IspSettings;
import com.barobot.common.interfaces.HardwareState;
import com.barobot.common.interfaces.serial.SerialInputListener;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.parser.Queue;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.message.Mainboard;
import com.barobot.tester.connection.WindowsSerialPort;
import com.barobot.tester.connection.WindowsSerialPort2;

public class Hardware {
	private WindowsSerialPort connection	= null;
	public String comPort					= "com1";
	public BarobotConnector barobot			= null;
	public HardwareState state				= null;

	public Hardware(String comPort) {
		this.state		= new EmptyBarobotState();	
		this.barobot	= new BarobotConnector( state );
		this.state.set("show_unknown", 1 );
		this.state.set("show_sending", 1 );
		this.state.set("show_reading", 1 );

		this.comPort	= comPort;
		this.connection	= new WindowsSerialPort( comPort, IspSettings.fullspeed );
	}
	public void connect() {
		if(connection.isConnected()){
			return;
		}
		boolean res = connection.open();
		if(!res ){
			System.exit(-1);
		}
		SerialInputListener listener = barobot.willReadFrom( connection );
		barobot.willWriteThrough( connection );
	}
	public void closeOnReady() {
		barobot.main_queue.add( new AsyncMessage( true ){		// na koncu zamknij
			@Override
			public String getName() {
				return "close on ready";
			}
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				close();
				return null;
			}
		});
	}
	protected void close() {
		connection.close();
		if(barobot.main_queue != null){
			barobot.main_queue.clear();
		}
	}
	public Queue getQueue() {
		return barobot.main_queue;
	}
}
