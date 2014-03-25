package com.barobot.isp;

import com.barobot.common.IspSettings;
import com.barobot.common.interfaces.HardwareState;
import com.barobot.common.interfaces.serial.SerialInputListener;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.parser.Queue;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.output.AsyncDevice;
import com.barobot.tester.connection.DesktopBarobotState;
import com.barobot.tester.connection.WindowsSerialPort;

public class Hardware {
	private WindowsSerialPort serial	= null;
	public String comPort				= "com1";
	public static BarobotConnector barobot;
	public static HardwareState state	= null;

	public Hardware(String comPort) {
		state			= new DesktopBarobotState();	
		barobot			= new BarobotConnector( state );
		Queue.enableDevice( barobot.mainboardSource );
		this.comPort	= comPort;
		serial = new WindowsSerialPort( comPort, IspSettings.fullspeed );
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
			final AsyncDevice mb	= Queue.getDevice( barobot.mainboardSource );
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
		barobot.main_queue.add( new AsyncMessage( true ){		// na koncu zamknij
			@Override
			public Queue run(AsyncDevice dev, Queue queue) {
				close();
				return null;
			}
		});
	}
	protected void close() {
		serial.close();
		if(barobot.main_queue != null){
			barobot.main_queue.clear( barobot.mainboardSource );
			Queue.disableDevice( barobot.mainboardSource );
		}
	}
	public Queue getQueue() {
		return barobot.main_queue;
	}
}
