package com.barobot.isp;

import com.barobot.common.EmptyBarobotState;
import com.barobot.common.IspSettings;
import com.barobot.common.interfaces.HardwareState;
import com.barobot.common.interfaces.serial.SerialInputListener;
import com.barobot.common.interfaces.serial.Wire;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.hardware.devices.MyRetReader;
import com.barobot.parser.Queue;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.message.Mainboard;
import com.barobot.tester.connection.WindowsSerialPort;

public class Hardware {
	private Wire connection					= null;
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
		MyRetReader mrr = new MyRetReader( barobot );
		barobot.mb.setRetReader( mrr );
	}
	SerialInputListener listener = null;

	public void connectIfDisconnected() {
		if(connection != null && connection.isConnected()){
			return;
		}
		this.connection	= new WindowsSerialPort( comPort, IspSettings.fullspeed );
		boolean res = connection.open();
		if(!res ){
			System.exit(-1);
		}
		listener = barobot.willReadFrom( connection );
		barobot.willWriteThrough( connection );
	}
	public void closeNow() {
		if(barobot.main_queue != null){
	//		barobot.main_queue.clear();
		}
		if(connection!=null){
			connection.close();
			connection = null;
		}
	}
	public void closeOnReady() {
		barobot.main_queue.add( new AsyncMessage( true ){		// na koncu zamknij
			@Override
			public String getName() {
				return "close on ready";
			}
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				closeNow();
				return null;
			}
		});
	}
	public Queue getQueue() {
		return barobot.main_queue;
	}
	public void synchro() {
	//	Initiator.logger.i("synchro",  "synchro" );
		barobot.main_queue.add( "\n", false );
		barobot.main_queue.add( "\n", false );
		barobot.main_queue.add( "PING", "RPONG" );
		barobot.main_queue.add( "PING", "RPONG" );
	}
	public Wire getConnection(){
		return connection;
	}
}
