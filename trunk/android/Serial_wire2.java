package com.barobot.hardware.serial;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.barobot.common.Initiator;
import com.barobot.common.interfaces.serial.CanSend;
import com.barobot.common.interfaces.serial.SerialEventListener;
import com.barobot.common.interfaces.serial.SerialInputListener;
import com.barobot.common.interfaces.serial.Wire;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

public class Serial_wire2 implements CanSend, Wire {
	private static final Object lock			= new Object();
	private static final int MESSAGE_REFRESH = 101;
    private static final long REFRESH_TIMEOUT_MILLIS = 10000;
	protected static final String ACTION_USB_PERMISSION = "com.hoho.android.usbserial.USB";
    private static UsbSerialPort sPort = null;

    private UsbManager mUsbManager;
    private boolean mPermissionReceiver_activated = true;
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private SerialInputOutputManager mSerialIoManager;
    private int errors = 0;
	private Activity view;
	private int baud = 57600;//115200;
	protected Queue<SerialInputListener> listener=new LinkedList<SerialInputListener>();
    private SerialInputOutputManager.Listener mListener = null;
	private SerialEventListener iel = null;

	public Serial_wire2(Activity mainActivity) {
		super();
		this.view = mainActivity;
		mListener = new SerialInputOutputManager.Listener() {
		    @Override
		    public synchronized void onRunError(Exception e) {
		        for (SerialInputListener il : listener){
		        	if(il.isEnabled()){
		        		il.onRunError( e );
		        	}
		        }
		        stopIoManager();
		    }
		    @Override
		    public synchronized void onNewData( byte[] data) {
		 //   	Log.e("Serial_wire.onNewData", new String(data, 0, data.length) );
		        for (SerialInputListener il : listener){
		        	if(il.isEnabled()){
		        		il.onNewData( data, data.length );
		        	}
		        }
		    }
		};
	}
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_REFRESH:
                	Log.i("Serial.handleMessage", "trying to connect");
                	mHandler.removeMessages(MESSAGE_REFRESH);		// usun duplikaty
                    if(!refreshDeviceList()){
                    	mHandler.sendEmptyMessageDelayed(MESSAGE_REFRESH, REFRESH_TIMEOUT_MILLIS);
                    }
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
	        }else{
	        	Log.e("Serial", "Resume isOpen");
	        }
		}
		return true;
	}
	
	public boolean init() {
		mUsbManager = (UsbManager) this.view.getSystemService(Context.USB_SERVICE);
		setSearching(true);
		return true;
	}
	public void setSearching(boolean active) {
		if(active){
			Log.w("Serial_wire.setSearching", "true");
			mHandler.sendEmptyMessage(MESSAGE_REFRESH);
		}else{
			mHandler.removeMessages(MESSAGE_REFRESH);	// remove all
		}
	}

	@Override
	public void resume() {
		//this.open();
		synchronized(lock){
	        if (sPort == null) {
	        	setSearching(true);
	        } else {
	            UsbDeviceConnection connection = mUsbManager.openDevice(sPort.getDriver().getDevice());
	            if (connection == null) {
	      //          mTitleTextView.setText("Opening device failed");
	                return;
	            }
	            try {
	                sPort.open(connection);
	                sPort.setParameters(baud, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE); 
	                onDeviceStateChange();
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
        stopIoManager();
        synchronized(lock){
	        if (sPort != null) {
	        	closePort();
	            sPort = null;
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
		this.listener.clear();
		synchronized(lock){
			if (sPort != null) {
				closePort();
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
	    listener.clear();
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
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (device != null) {
                        Log.w("Serial_wire.mPermissionReceiver","Permission granted: "+ device.getVendorId() );
                        UsbDeviceConnection connection = mUsbManager.openDevice(mDriver.getDevice());
                        connectWith(connection, device, mDriver.getPorts().get(0));
                    } else {
                        Log.e("Serial_wire.mPermissionReceiver","device not present!");
                    }
                }
            }
        }
    };
	private UsbSerialDriver mDriver;

    private boolean refreshDeviceList() {
    	if(sPort != null){
    		Log.i("Serial.refreshDeviceList", "sPort exists");
    		return false;
    	}
    	List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager);
    	if (availableDrivers.isEmpty()) {
    		Log.w("Serial.refreshDeviceList", "isEmpty");
    		return false;
    	}
    	for ( int i=0; i<availableDrivers.size();i++) {
    		UsbSerialDriver driver = availableDrivers.get(i);
    		UsbDevice device = driver.getDevice();
    		if(driver.getPorts().size() > 0){
	    		view.registerReceiver(mPermissionReceiver, new IntentFilter( ACTION_USB_PERMISSION));
	    		mPermissionReceiver_activated = true;
	            if (mUsbManager.hasPermission(device)){
	            	UsbDeviceConnection connection = mUsbManager.openDevice(driver.getDevice());
	    	    	if (connection != null) {
	    	    		connectWith(connection, device, driver.getPorts().get(0));
	    	    		return true;
	    	    	}
	            }else{
	            	Intent inte = new Intent( ACTION_USB_PERMISSION);
	            	final PendingIntent pi = PendingIntent.getBroadcast( view, 0,inte, 0);
	            	this.mDriver = driver;
	                mUsbManager.requestPermission(device, pi);
	                Log.w("Serial_wire.refreshDeviceList", "requestPermission");
	                break;
	            };
    		}
    	}
    	return false;
    
	    /*
        new AsyncTask<Void, Void, List<String>>() {
            @Override
            protected List<String> doInBackground(Void... params) {   
                Log.d("Serial_wire.refreshDeviceList", "Refreshing device list ...");
   
                for (final UsbDevice device : mUsbManager.getDeviceList().values()) {
                    final UsbSerialDriver driver =  UsbSerialProber.probeSingleDevice(device);
                    if (driver == null) {
//                       Log.d("serial", "  - No UsbSerialDriver available.");
//                       result.add(new DeviceEntry(device, null, 0));
                    } else {
        //                Log.d(TAG, "  + " + driver + ", " + driver.getPortCount() + " ports.");

                    	view.registerReceiver(mPermissionReceiver, new IntentFilter( ACTION_USB_PERMISSION));
                        mPermissionReceiver_activated = true;

                        if (mUsbManager.hasPermission(device)){
                            for (int i = 0; i < driver.getPortCount(); ++i) {	
                            	Log.w("Serial_wire.refreshDeviceList", "ready device: " + i +": "+ device.getVendorId()+" - "+ device.getProductId() );
                //                result.add(new DeviceEntry(device, driver, i));
                                if( i == 0 ){
                                	connectWith(device);
                                }
                            }
                        }else{
                        	 final PendingIntent pi = PendingIntent.getBroadcast( view, 0, new Intent( ACTION_USB_PERMISSION), 0);
                             mUsbManager.requestPermission(device, pi);
                             Log.w("Serial_wire.refreshDeviceList", "requestPermission");
                        
                        };
                    }
                }
				return null;
            }
        }.execute((Void) null);
*/
    }
    protected boolean connectWith(UsbDeviceConnection connection, UsbDevice device, UsbSerialPort port ) {    	// polacz...
    	synchronized(lock){
	    	if (port == null) {
	            Log.d("serial.connectWith", "No port.");
	        }else{
	        	Log.w("Serial.connectWith", "openPort connectWith");
	        	try {
	        		port.open(connection);
	        		port.setParameters(baud, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
	    			sPort = port;
	    			setSearching(false);
	    			onDeviceStateChange();
	    	    	if(iel!=null){
	    	    		iel.onConnect();
	    	    	}
	    		} catch (IOException e) {
	    			try {
	    				port.close();
	    			} catch (IOException e2) {
	    				// Ignore.
	    			}
	    		}
	        }
    	}
		return false;
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
				closePort();
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
		return "Android Serial Port";
	}

	@Override
	public synchronized boolean send(String message) {
        if(mSerialIoManager!=null){
        	byte data[] = message.getBytes(); 
     //       mSerialIoManager.writeAsync(data);

               mSerialIoManager.writeAsync(data);
               errors =0;
               return true;
            
        }
		return false;
	}
	@Override
	public synchronized boolean send(byte[] data, int size) throws IOException {
		if(mSerialIoManager!=null){
			byte [] subArray = Arrays.copyOfRange(data, 0, size);
            mSerialIoManager.writeAsync(subArray);
            errors =0;
            return true;
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
			if(sPort!=null ){
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
	/*	if (!sPort.isOpen()) {
			Log.e("Serial_wire.isConnected", "isOpen null" );
			return false;
		}*/
		return true;
	}
	
	@Override
	public void addOnReceive(SerialInputListener inputListener) {
		this.listener.add( inputListener );
		Log.i("serial", "listeners: " +this.listener.size() );
	}
	@Override
	public void removeOnReceive(SerialInputListener inputListener) {
		this.listener.remove(inputListener);
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
