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
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.barobot.common.interfaces.serial.CanSend;
import com.barobot.common.interfaces.serial.SerialEventListener;
import com.barobot.common.interfaces.serial.SerialInputListener;
import com.barobot.common.interfaces.serial.Wire;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

public class Serial_wire implements CanSend, Wire {
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
	private int baud = 115200;
	protected Queue<SerialInputListener> listener=new LinkedList<SerialInputListener>();
    private SerialInputOutputManager.Listener mListener = null;
	private SerialEventListener iel = null;

	public Serial_wire(Activity mainActivity) {
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
	//	    	Log.e("Serial_wire.onNewData", new String(data, 0, data.length) );
		        for (SerialInputListener il : listener){
		        	if(il.isEnabled()){
		        		il.onNewData( data, data.length );
		        	}
		        }
		    }
		};
	}

	@Override
	public boolean open() {
		Log.e("Serial_wire.resume", "Resumed, sDriver=" + sPort);
        if ( sPort == null ) {
        	Log.e("Serial_wire.resume","No serial device.");
        	mHandler.sendEmptyMessage(MESSAGE_REFRESH);
        } else if( !sPort.isOpen() ){
        	Log.e("Serial", "Resumed openPort");
        	openPort();
        }else{
        	Log.e("Serial", "Resume nothing");
        }
		return true;
	}
	public void setBaud( int baud ) {
		this.baud = baud;
		Log.e("Serial_wire", "setBaud " + baud );
		if(sPort!=null && sPort.isOpen() ){
			try {
				sPort.setParameters(baud, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public boolean init() {
		mUsbManager = (UsbManager) this.view.getSystemService(Context.USB_SERVICE);
		mHandler.sendEmptyMessage(MESSAGE_REFRESH);
		Log.e("Serial_wire.init", "MESSAGE_REFRESH");
		return true;
	}

	public void setSearching(boolean active) {
		if(active){
			mHandler.sendEmptyMessage(MESSAGE_REFRESH);
		}else{
			mHandler.removeMessages(MESSAGE_REFRESH);
		}
	}

	@Override
	public void resume() {
		//this.open();
        if (sPort == null) {
        } else {
            try {
                sPort.open(mUsbManager);
                sPort.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            } catch (RuntimeException e) {
            	 e.printStackTrace();
            	 
            } catch (IOException e) {
                try {
                    sPort.close();
                } catch (IOException e2) {
                    // Ignore.
                }
                sPort = null;
                return;
            }
        }
        onDeviceStateChange();
	}
	
    @Override
	public void onPause() {
        stopIoManager();
        if (sPort != null) {
            try {
                sPort.close();
            } catch (IOException e) {
                // Ignore.
            }
            sPort = null;
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
	public void close() {
		mHandler.removeMessages(MESSAGE_REFRESH);
		stopIoManager();
		this.listener.clear();
		if (sPort != null) {
			if(sPort.isOpen()){
				try {
	        		sPort.close();
				} catch (IllegalStateException e2) {
			    	Log.e("Serial_wire.close", "IllegalStateException", e2);
			    } catch (IOException e) {
			    }
        	}
		    sPort = null;
		}
    	if(iel!=null){
    		iel.onClose();
    	}
	}
	@Override
	public synchronized boolean send(String message) {
        if(mSerialIoManager!=null){
        	byte data[] = message.getBytes(); 
     //       mSerialIoManager.writeAsync(data);
            try {
                mSerialIoManager.writeSync(data);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                errors++;
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
                e.printStackTrace();
                errors++;
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
	@Override
	public void destroy() {
		mHandler.removeMessages(MESSAGE_REFRESH);
		close();
        if(mPermissionReceiver_activated){
            mPermissionReceiver_activated = false;
            try {
            	 this.view.unregisterReceiver(mPermissionReceiver);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
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
		return false;
	}

	@Override
	public void connectToId(String address) {
	}

	@Override
	public String getName() {
		return "Android Serial Port";
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

                    	view.registerReceiver(mPermissionReceiver, new IntentFilter(
                                ACTION_USB_PERMISSION));
                        mPermissionReceiver_activated = true;
                          
                        if (!mUsbManager.hasPermission(device)){
                            final PendingIntent pi = PendingIntent.getBroadcast( view, 0, new Intent(
                                    ACTION_USB_PERMISSION), 0);
                            mUsbManager.requestPermission(device, pi);
                            Log.w("Serial_wire.refreshDeviceList", "requestPermission");
                        }else{
                            for (int i = 0; i < driver.getPortCount(); ++i) {	
                            	Log.w("Serial_wire.refreshDeviceList", "ready device: " + i +": "+ device.getVendorId()+" - "+ device.getProductId() );
                //                result.add(new DeviceEntry(device, driver, i));
                                if( i == 0 ){
                                	connectWith(device);
                                }
                            }
                        };
                    }
                }
				return null;
            }
        }.execute((Void) null);

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
    	if( sPort == null ){
    		Log.e("Serial.openPort", "sPort was null");
    	}else if( mUsbManager == null ){
        	Log.e("Serial.openPort", "mUsbManager is null");
    	}else if(sPort.isOpen()){
    		Log.e("Serial.openPort", "isOpen is opened");
    		mHandler.removeMessages(MESSAGE_REFRESH);
    		onDeviceStateChange();
    	}else{
    		Log.e("Serial.openPort", "openPort");
    		mHandler.removeMessages(MESSAGE_REFRESH);
			try {
	            sPort.open(mUsbManager);
	            sPort.setParameters(baud, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
	            onDeviceStateChange();
	            Log.i("Serial.openPort", "opened " +baud);
	        } catch (IOException e) {
	            Log.e("Serial.openPort", "Error setting up device: " + e.getMessage(), e);
	            close();
	            return;
	        }
    	}
    	if(iel!=null){
    		iel.onConnect();
    	}
        Log.i("Serial.openPort", "Type: "+ sPort.getClass().getSimpleName());
	}
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_REFRESH:
                	Log.i("Serial.handleMessage", "trying to connect");
                    refreshDeviceList();
                    mHandler.removeMessages(MESSAGE_REFRESH);		// usun duplikaty
                    mHandler.sendEmptyMessageDelayed(MESSAGE_REFRESH, REFRESH_TIMEOUT_MILLIS);
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    };
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
    private void onDeviceStateChange() {
        stopIoManager();
        startIoManager();
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
	@Override
	public void reset() {
		mHandler.removeMessages(MESSAGE_REFRESH);
		this.close();
        if(mPermissionReceiver_activated){
            mPermissionReceiver_activated = false;
            try {
            	 this.view.unregisterReceiver(mPermissionReceiver);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
        }
        mUsbManager = null;
	    mSerialIoManager = null;
		init();
	}
}
