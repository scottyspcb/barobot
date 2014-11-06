package com.barobot.hardware.serial;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.barobot.common.Initiator;
import com.barobot.common.constant.LowHardware;
import com.barobot.common.interfaces.serial.CanSend;
import com.barobot.common.interfaces.serial.SerialEventListener;
import com.barobot.common.interfaces.serial.SerialInputListener;
import com.barobot.common.interfaces.serial.Wire;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

public class Serial_wire2 implements CanSend, Wire{
	private static final Object lock			= new Object();
	private static final int MESSAGE_REFRESH = 101;
 //   private static final long REFRESH_TIMEOUT_MILLIS = 5000;
	protected static final String ACTION_USB_PERMISSION = "com.hoho.android.usbserial.USB";
    private static UsbSerialPort sPort = null;

    private UsbManager mUsbManager;
    private boolean mPermissionReceiver_activated = true;
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private SerialInputOutputManager mSerialIoManager;
	private Activity view;
	private int baud = LowHardware.MAINBOARD_SERIAL0_BOUND;
	protected SerialInputListener listener=null;
    private SerialInputOutputManager.Listener mListener = null;
	private SerialEventListener iel = null;
    private UsbDevice sDevice;

	public Serial_wire2(Activity mainActivity) {
		super();
		this.view = mainActivity;
		mListener = new SerialInputOutputManager.Listener() {
		    @Override
		    public synchronized void onRunError(Exception e) {
		       	listener.onRunError( e );
		        stopIoManager();
		    }
		    @Override
		    public synchronized void onNewData( byte[] data) {
	//	    	Log.e("Serial_wire.onNewData", new String(data, 0, data.length) );
		       	listener.onNewData( data, data.length );
		    }
		};
	}

	@Override
	public Serial_wire2 newInstance() {
		Serial_wire2 sw = new Serial_wire2(this.view );
		return sw;
	}

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_REFRESH:
                	mHandler.removeMessages(MESSAGE_REFRESH);		// usun duplikaty
                	if(sPort == null){
                		Log.i("Serial.handleMessage", "trying to connect");
                        refreshDeviceList();
                	}else if(sPort.isOpen()){
                		Log.i("Serial.handleMessage", "sPort Opened");
                	}else{
                		Log.i("Serial.handleMessage", "sPort not Opened");
                		if(sDevice == null){
                			Log.i("Serial.handleMessage", "sDevice null");
                		}else{
	                        UsbSerialDriver driver =  UsbSerialProber.probeSingleDevice(sDevice);
	                        if (driver == null) {
	                        	Log.i("Serial.handleMessage", "no sDevice");
	                        } else {
	                        	Log.i("Serial.handleMessage", "getPortCount"+ driver.getPortCount());
	                        	Log.i("Serial.handleMessage", "getPortCount"+ driver.getShortDeviceName());
	                        	Log.i("Serial.handleMessage", "getPortCount"+ driver.getDevice().getInterfaceCount());
	                        }
						}
                	}
           //         mHandler.sendEmptyMessageDelayed(MESSAGE_REFRESH, REFRESH_TIMEOUT_MILLIS);
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    };
	@Override
	public boolean open() {
		synchronized(lock){
			Log.e("Serial_wire.resume", "Resumed, sDriver=" + sPort);
	        if ( sPort == null ) {
	        	Log.e("Serial_wire.resume","No serial device.");
	        	setSearching(true);
	        } else if( !sPort.isOpen() ){
	        	Log.e("Serial", "Resumed openPort");
	        	openPort();
	        }else{
	        	Log.e("Serial", "Resume isOpen");
	        }
		}
		return true;
	}
	
	public boolean init() {
		mUsbManager = (UsbManager) this.view.getSystemService(Context.USB_SERVICE);
		if( mUsbManager == null ){
			Log.e("Serial.init", "no mUsbManager!!!");
    	}
		setSearching(true);
		return true;
	}
	public void setSearching(boolean active) {
		if(active){
			Log.w("Serial_wire.setSearching", "true");
			mHandler.sendEmptyMessage(MESSAGE_REFRESH);
		}else{
	//		mHandler.removeMessages(MESSAGE_REFRESH);
		}
	}

	@Override
	public void resume() {
		Initiator.logger.w("Serial_wire.resume","resume");
		//this.open();
		synchronized(lock){
	        if (sPort == null) {
	        	Initiator.logger.i("Serial_wire.resume","setSearching");
	        	setSearching(true);
	        }else if(sPort.isOpen()){
	        	Initiator.logger.i("Serial_wire.resume","opened");
	        }else{
	        	Initiator.logger.i("Serial_wire.resume","try open");
	            try {
				sPort.open(mUsbManager);
				sPort.setParameters(baud, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE); 
				onDeviceStateChange();
				if(iel!=null){
					iel.onConnect();
				}
	            } catch (IllegalStateException e) {
	             Initiator.logger.e("Serial_wire.resume","IllegalStateException");
	           	 Initiator.logger.appendError(e);  
	     //      	 opened = oneMoreTime();
	            } catch (RuntimeException e) {
	            	Initiator.logger.e("Serial_wire.resume","RuntimeException");
	            	Initiator.logger.appendError(e);
	            } catch (IOException e) {
	            	closePort();
	                sPort = null;
	                return;
	            }
        	}
		}
	}

	@Override
	public void onPause() {
		Log.i("serial", "onPause");
        synchronized(lock){
	        if (sPort != null) {
	        //	closePort();
	         //   sPort = null;
	         //   stopIoManager();
	        }
        }
    }

	private void closePort() {
		try {
    		sPort.close();
		} catch (NullPointerException e2) {
			Log.e("Serial_wire.close", "NullPointerException", e2);
		} catch (IllegalStateException e2) {
	    	Log.e("Serial_wire.close", "IllegalStateException", e2);
	    } catch (IOException e) {
	    }
	}

	@Override
	public void close() {
		setSearching(false);
		stopIoManager();
		this.listener = null;
		synchronized(lock){
			if (sPort != null) {
				if(sPort.isOpen()){
					closePort();
	        	}
			    sPort = null;
			}
		}
    	if(iel!=null){
    		iel.onClose();
    	}
	}

	@Override
	public void destroy() {
		mHandler.removeMessages(MESSAGE_REFRESH);
		close();
        if(mPermissionReceiver_activated){
            mPermissionReceiver_activated = false;
            try {
            	 this.view.unregisterReceiver(mPermissionReceiver);
			} catch (IllegalArgumentException e) {
				Initiator.logger.appendError(e);
			}
        }
        mUsbManager = null;
	    mSerialIoManager = null;
	    view = null;
	    listener=null;
	    mListener = null;
	}
	@Override
	public boolean setAutoConnect(boolean active) {
		setSearching(active);
		return false;
	}

    private final BroadcastReceiver mPermissionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
                if (!intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    Log.e("Serial_wire.mPermissionReceiver","Permission not granted :(");
                } else {
                    UsbDevice dev = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (dev != null) {
                        Log.w("Serial_wire.mPermissionReceiver","Permission granted: "+ dev.getVendorId() );
                        connectWith(dev);
                    } else {
                        Log.e("Serial_wire.mPermissionReceiver","device not present!");
                    }
                }
            }
        }
    };

    private void refreshDeviceList() {
    	Log.d("Serial_wire.refreshDeviceList", "Refreshing device list ...");
    	if( mUsbManager == null ){
    		Log.e("Serial_wire.refreshDeviceList", "mUsbManager is null");
    		return;
    	}
    	Collection<UsbDevice> vs = mUsbManager.getDeviceList().values();
        for (UsbDevice device : vs ) {
            UsbSerialDriver driver =  UsbSerialProber.probeSingleDevice(device);
            if (driver == null) {
//               Log.d("serial", "  - No UsbSerialDriver available.");
//               result.add(new DeviceEntry(device, null, 0));
            } else {
//                Log.d(TAG, "  + " + driver + ", " + driver.getPortCount() + " ports.");
            	view.registerReceiver(mPermissionReceiver, new IntentFilter(  ACTION_USB_PERMISSION));
                mPermissionReceiver_activated = true;
                if (!mUsbManager.hasPermission(device)){
                    final PendingIntent pi = PendingIntent.getBroadcast( view, 0, new Intent( ACTION_USB_PERMISSION), 0);
                    mUsbManager.requestPermission(device, pi);
                    Log.w("Serial_wire.refreshDeviceList", "requestPermission");
                }else{
                    for (int i = 0; i < driver.getPortCount(); ++i) {	
                    	Log.w("Serial_wire.refreshDeviceList", "ready device: " + i +": "+ device.getVendorId()+" - "+ device.getProductId() );
        //                result.add(new DeviceEntry(device, driver, i));
                        if( i == 0 ){
                        	sDevice = device;
                        	connectWith(device);
                        }
                    }
                };
            }
        }
    	
    	/*
        new AsyncTask<Void, Void, List<String>>() {
			@Override
            protected List<String> doInBackground(Void... params) {   
				return null;
            }
        }.execute((Void) null);*/
    }

    protected boolean connectWith(UsbDevice device) {    	// polacz...
		final UsbSerialDriver driver =  UsbSerialProber.probeSingleDevice(device);
        UsbSerialPort port = driver.getPort( 0 );
        if (port == null) {
            Log.d("serial.connectWith", "No port.");
        }else{
        	sPort = port;
        	Log.w("Serial.connectWith", "openPort connectWith");
        	openPort();
        }
		return false;
	}
    private void openPort() {
    	synchronized(lock){
	    	if( sPort != null && sPort.isOpen()){
	    		Log.e("Serial.openPort", "isOpen is opened");
	    	}else{
	    		Log.e("Serial.openPort", "openPort");
				try {
		            sPort.open(mUsbManager);
		            sPort.setParameters(baud, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
		            onDeviceStateChange();
		            setSearching(false);
		            Log.i("Serial.openPort", "opened " +baud);
		        	if(iel!=null){
		        		iel.onConnect();
		        	}
		        } catch (IOException e) {
		            Log.e("Serial.openPort", "Error setting up device: " + e.getMessage(), e);
		            knowIsClosed();
		            return;
		        }
	    	}
    	}
        Log.i("Serial.openPort", "Type: "+ sPort.getClass().getSimpleName());
	}

    private void knowIsClosed() {
    	Log.i("serial", "Notice serial closed 1");
    	setSearching(false);
		stopIoManager();
		synchronized(lock){
			if (sPort != null) {
				if(sPort.isOpen()){
					closePort();
	        	}
			    sPort = null;
			}
		}
    	if(iel!=null){
    		iel.onClose();
    	}
    	// 
    	Log.i("serial", "Notice serial closed 2");
    	startIoManager();
	}

	private void onDeviceStateChange() {
        stopIoManager();
        startIoManager();
    }
    private void stopIoManager() {
        if (mSerialIoManager != null) {
            Log.i("serial", "Stopping io manager ..");
            mSerialIoManager.stop();
            mSerialIoManager = null;
        }
    }
    private void startIoManager() {
        if (sPort != null) {
            Log.i("serial", "Starting io manager ..");
            mSerialIoManager = new SerialInputOutputManager(sPort, mListener);
            mExecutor.submit(mSerialIoManager);
        }
    }

	@Override
	public void reset() {
		setSearching(false);
		stopIoManager();
		synchronized(lock){
			if (sPort != null) {
				if(sPort.isOpen()){
					closePort();
	        	}
			    sPort = null;
			}
		}
    	if(iel!=null){
    		iel.onClose();
    	}
		/*
        if(mPermissionReceiver_activated){
            mPermissionReceiver_activated = false;
            try {
            	 this.view.unregisterReceiver(mPermissionReceiver);
			} catch (IllegalArgumentException e) {
				Initiator.logger.appendError(e);
			}
        }*/
        mUsbManager = null;
	    mSerialIoManager = null;
		init();
		open();
	}
	
	
	
	

	@Override
	public void connectToId(String address) {
	}

	@Override
	public String getName() {
		return "Android-Barobot Serial Port Driver";
	}

	@Override
	public synchronized boolean send(String message) {
        if(mSerialIoManager!=null){
        	byte data[] = message.getBytes(); 
            try {
                mSerialIoManager.writeSync(data);
                return true;
            } catch (IOException e) {
            	Initiator.logger.e("Serial_wire.send", "IOException");
                Initiator.logger.appendError(e);
                knowIsClosed();
            }
        }
		return false;
	}
	@Override
	public synchronized boolean send(byte[] data, int size) throws IOException {
		if(mSerialIoManager!=null){
			byte [] subArray = Arrays.copyOfRange(data, 0, size);
            try {
                mSerialIoManager.writeSync(subArray);
                return true;
            } catch (IOException e) {
                Initiator.logger.appendError(e);
                knowIsClosed();
            }
        }
		return false;
	}
	@Override
	public boolean canConnect() {
		return true;
	}
	@Override
	public boolean implementAutoConnect() {
		return false;
	}
	public void setBaud( int baud ) {
		this.baud = baud;
		synchronized(lock){
			Log.e("Serial_wire", "setBaud " + baud );
			if(sPort!=null && sPort.isOpen() ){
				try {
					sPort.setParameters(baud, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
				} catch (IOException e) {
					Initiator.logger.appendError(e);
				}
			}
		}
	}
	@Override
	public boolean isConnected() {
		if (sPort == null) {
			Log.e("Serial_wire.isConnected", "sPort null" );
			return false;	
		}
		if(mSerialIoManager==null){
			Log.e("Serial_wire.isConnected", "mSerialIoManager null" );
			return false;
		}
		if (!sPort.isOpen()) {
			Log.e("Serial_wire.isConnected", "isOpen null" );
			return false;
		}
		return true;
	}
	
	@Override
	public void setOnReceive(SerialInputListener inputListener) {
		this.listener =  inputListener;
	}
	
	@Override
	public SerialInputListener getReceiveListener() {
		return this.listener;
	}

	@Override
	public SerialEventListener getSerialEventListener() {
		return iel;
	}
	@Override
	public void setSerialEventListener(SerialEventListener iel) {
		this.iel  = iel;
	}
}
