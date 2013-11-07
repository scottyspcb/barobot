package com.barobot.wire;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

import com.barobot.BarobotMain;
import com.barobot.utils.Arduino;
import com.barobot.utils.input_parser;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

public class Serial_wire implements Wire {
	private static final int MESSAGE_REFRESH = 101;
    private static final long REFRESH_TIMEOUT_MILLIS = 5000;
	protected static final String ACTION_USB_PERMISSION = "com.hoho.android.usbserial.USB";

    private UsbManager mUsbManager;
    private boolean mPermissionReceiver_activated = true;
    private static UsbSerialPort sPort = null;
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private SerialInputOutputManager mSerialIoManager;

    private int errors = 0;

	public boolean init() {
		mUsbManager = (UsbManager) BarobotMain.getInstance().getSystemService(Context.USB_SERVICE);
		mHandler.sendEmptyMessage(MESSAGE_REFRESH);
		return false;
	}

	public void setOnReceive() {
	}

	public void setSearching(boolean active) {
	}

	@Override
	public void pause() {
		mHandler.removeMessages(MESSAGE_REFRESH);
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
	public void resume() {
		Log.e("Serial", "Resumed, sDriver=" + sPort);
        if (sPort == null) {
        	Log.e("Serial","No serial device.");
        	mHandler.sendEmptyMessage(MESSAGE_REFRESH);
        } else {
        	openPort();
        }
	}
	@Override
	public boolean isConnected() {
		if (sPort == null) {
			return false;	
		}
		if(mSerialIoManager!=null){
			return false;
		}
		if (!sPort.isOpen()) {
			return false;
		}
		return true;
	}


	@Override
	public void disconnect() {
		stateHasChanged();
	}

	@Override
	public boolean send(String message) {
        if(mSerialIoManager!=null){
            byte data[] = message.getBytes(); 
            mSerialIoManager.writeAsync(data);
            try {
                mSerialIoManager.writeSync(data);
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
	public void stateHasChanged() {
		Arduino ar =  Arduino.getInstance();
		ar.clear();
	}

	@Override
	public void destroy() {
		mHandler.removeMessages(MESSAGE_REFRESH);
        if(mPermissionReceiver_activated){
            mPermissionReceiver_activated = false;
            BarobotMain.getInstance().unregisterReceiver(mPermissionReceiver);
        }
        if (sPort != null && sPort.isOpen()) {
		    try {
		        sPort.close();
		    } catch (IOException e) {
		        // Ignore.
		    }
		    sPort = null;
		}
	}

	@Override
	public void setup() {
		// TODO Auto-generated method stub
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
		return "Port szeregowy";
	}

    private final BroadcastReceiver mPermissionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
                if (!intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    Log.e("serial","Permission not granted :(");
                } else {
                    UsbDevice dev = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (dev != null) {
                        Log.w("serial","Permission granted: "+ dev.getVendorId() );
                        connectWith(dev);
                    } else {
                        Log.e("serial","device not present!");
                    }
                }
            }
        }
    };

    private void refreshDeviceList() {
        new AsyncTask<Void, Void, List<String>>() {
            @Override
            protected List<String> doInBackground(Void... params) {   
                Log.d("serial", "Refreshing device list ...");
   
                for (final UsbDevice device : mUsbManager.getDeviceList().values()) {
                    final UsbSerialDriver driver =  UsbSerialProber.probeSingleDevice(device);
                    if (driver == null) {
//                       Log.d("serial", "  - No UsbSerialDriver available.");
//                       result.add(new DeviceEntry(device, null, 0));
                    } else {
        //                Log.d(TAG, "  + " + driver + ", " + driver.getPortCount() + " ports.");
                        mPermissionReceiver_activated = true;
                        BarobotMain.getInstance().registerReceiver(mPermissionReceiver, new IntentFilter(
                                ACTION_USB_PERMISSION));

                        if (!mUsbManager.hasPermission(device)){
                            final PendingIntent pi = PendingIntent.getBroadcast(BarobotMain.getInstance(), 0, new Intent(
                                    ACTION_USB_PERMISSION), 0);
                            mUsbManager.requestPermission(device, pi);
                            Log.w("serial", "requestPermission");
                        }else{
                            for (int i = 0; i < driver.getPortCount(); ++i) {	
                            	Log.w("serial", "ready device: " + i +": "+ device.getVendorId()+" - "+ device.getProductId() );
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
    protected boolean connectWith(UsbDevice device) {    	// połącz...
		final UsbSerialDriver driver =  UsbSerialProber.probeSingleDevice(device);
        UsbSerialPort port = driver.getPort( 0 );
        if (port == null) {
            Log.d("serial", "No port.");
        }else{
        	sPort = port;
        	openPort();
        }
		return false;
	}
    public void openPort() {
    	if( sPort == null ){
    		Log.e("Serial", "sPort is null");
    	}else if( mUsbManager == null ){
        	Log.e("Serial", "mUsbManager is null");
    	}else if(sPort.isOpen()){
    		Log.e("Serial", "isOpen is opened");
    		mHandler.removeMessages(MESSAGE_REFRESH);
    		onDeviceStateChange();
    	}else{
    		Log.e("Serial", "openPort");
    		mHandler.removeMessages(MESSAGE_REFRESH);
			try {
	            sPort.open(mUsbManager);
	            sPort.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
	            onDeviceStateChange();
	            Log.i("Serial", "opened 115200");
	        } catch (IOException e) {
	            Log.e("Serial", "Error setting up device: " + e.getMessage(), e);
	            try {
	                sPort.close();
	            } catch (IOException e2) {
	                // Ignore.
	            }
	            sPort = null;
	            return;
	        }
    	}
        Log.i("Serial", "Type:"+ sPort.getClass().getSimpleName());
	}

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_REFRESH:
                    refreshDeviceList();
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

    private final SerialInputOutputManager.Listener mListener =
        new SerialInputOutputManager.Listener() {

	    @Override
	    public void onRunError(Exception e) {
	        Log.d(getName(), "Runner stopped.");
	        stopIoManager();
	    }
	    @Override
	    public void onNewData(final byte[] data) {
	    	String message = new String(data);
	    	Log.e("Serial input", message);
	    	input_parser.readInput(message);
	    }
	};
    private void onDeviceStateChange() {
        stopIoManager();
        startIoManager();
    }
}
