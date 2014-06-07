
package com.barobot.hardware.serial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.barobot.common.Initiator;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
public class BluetoothChatService {
    private static final String TAG = BluetoothChatService.class.getSimpleName();

	// Intent request codes
	public static final int REQUEST_ENABLE_BT = 103;

	public static final String DEVICE_ADDRESS = "DEVICE_ADDRESS";

	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	// Return Intent extra
	public static String EXTRA_DEVICE_ADDRESS = "device_address";
	public static final int MESSAGE_TOAST = 5;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_READ = 2;
	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int STATE_LISTEN = 1;     // now listening for incoming connections
	public static final int STATE_CONNECTED = 3;  // now connected to a remote device
	public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
	// Constants that indicate the current connection state
	public static final int STATE_NONE = 0;       // we're doing nothing


    // Member fields
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;
    private static boolean hasInstance	= false;
    public boolean is_connected			= false;
	public String bt_connected_device	= null;

	private Activity view;
    /**
     * Constructor. Prepares a new BluetoothChat session.
     * @param view 
     * @param context  The UI Activity Context
     * @param handler  A Handler to send messages back to the UI Activity
     * @param barobotMain 
     * @throws Exception 
     */
    public BluetoothChatService(Activity view, Handler handler) throws Exception {
    	if(hasInstance){
    		throw new Exception("duplikat bluetooth");
    	}
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = BluetoothChatService.STATE_NONE;
        mHandler = handler;
        hasInstance = true;
        this.view = view;

        IntentFilter f1 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        IntentFilter f2 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        view.registerReceiver(this.btEvents, f1);
        view.registerReceiver(this.btEvents, f2);
    }
 
	// The BroadcastReceiver that listens for discovered devices and
    // changes the title when discovery is finished
    public final BroadcastReceiver btEvents = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
            	Log.d(TAG,"ACTION_ACL_DISCONNECTED");
                is_connected						= false;
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
            	Log.d(TAG,"ACTION_ACL_DISCONNECT_REQUESTED");
                is_connected						= false;
            }
        }
    };

    /**
     * Set the current state of the chat connection
     * @param state  An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;
        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(BluetoothChatService.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * Return the current connection state. */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume() */
    public synchronized void start() {
        Log.d(TAG, "synchronized start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
        	mConnectThread.cancel(); 
        	mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
        	mConnectedThread.destroy();
        	mConnectedThread = null;
        }
        setState(BluetoothChatService.STATE_LISTEN);
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    public synchronized void connect(BluetoothDevice device) {
        if (mState == BluetoothChatService.STATE_CONNECTING) {        // Cancel any thread attempting to make a connection
            if (mConnectThread != null) {
            	Log.d(TAG, "force connect - STATE_CONNECTING");
            	return;
            	//mConnectThread.cancel();
            	//mConnectThread = null;
            }
        }
        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
        	mConnectedThread.destroy();
        	Log.d(TAG, "force connect - make new2");
        	mConnectedThread = null;
        }

        mConnectThread = new ConnectThread(device);        // Start the thread to connect with the given device
        mConnectThread.start();
        setState(BluetoothChatService.STATE_CONNECTING);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice
            device, final String socketType) {
        Log.d(TAG, "connected, Socket Type:" + socketType);
        
        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
        	mConnectThread.cancel(); 
        	mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
        	mConnectedThread.destroy();
        	mConnectedThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket, socketType);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(BluetoothChatService.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(BluetoothChatService.DEVICE_NAME, device.getName());
        bundle.putString(BluetoothChatService.DEVICE_ADDRESS, device.getAddress());
        msg.setData(bundle);
        
        setState(BluetoothChatService.STATE_CONNECTED);
        
        mHandler.sendMessage(msg);
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        Log.d(TAG, "stop");
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mConnectedThread != null) {
            mConnectedThread.destroy();
            mConnectedThread = null;
        }
        setState(BluetoothChatService.STATE_NONE);
    }
	public void destroy() {
    	BluetoothChatService.hasInstance = false;
		try {
			this.view.unregisterReceiver(this.btEvents);
		} catch (IllegalArgumentException e) {
		}
		this.stop();
	}
	

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     * @param out The bytes to write
     * @param length 
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out, int length) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != BluetoothChatService.STATE_CONNECTED) {
            	return;
            }
            r = mConnectedThread;
        }
        /*
		try {
			String str = new String(out, "UTF-8");
			Log.d(TAG, "Wysylam write: " + str);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			Initiator.logger.appendError(e);
		}*/
        // Perform the write unsynchronized
        r.write(out, length);
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        // Send a failure message back to the Activity
/*
        Message msg = mHandler.obtainMessage(Constant.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constant.TOAST, "Nie dało się połączyć");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
*/
        // Start the service over to restart listening mode
        Log.d(TAG, "connectionFailed2");
        BluetoothChatService.this.start();
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        // Send a failure message back to the Activity
    	/*
        Message msg = mHandler.obtainMessage(Constant.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constant.TOAST, "Device connection was lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
*/
        bt_connected_device			= null;
        is_connected				= false;

        Log.d(TAG, "connectionLost2");
        // Start the service over to restart listening mode
        BluetoothChatService.this.start();
    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            // Get a BluetoothSocket for a connection with the given BluetoothDevice
            try {
               //tmp = device.createRfcommSocketToServiceRecord(Constant.MY_UUID_SECURE);
               Method m = device.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
               tmp = (BluetoothSocket) m.invoke(device, 1);

         //   } catch (IOException e) {
          //      Log.d(TAG, "Socket Type: " +  "Secure" + "create() failed", e);
            } catch (NoSuchMethodException e) {
				Initiator.logger.appendError(e);
			} catch (IllegalArgumentException e) {
				Initiator.logger.appendError(e);
			} catch (IllegalAccessException e) {
				Initiator.logger.appendError(e);
			} catch (InvocationTargetException e) {
				Initiator.logger.appendError(e);
			}
            mmSocket = tmp;
        }

        @Override
    	public void run() {
            setName("ConnectThread" +  "Secure");
            if(mAdapter.isDiscovering()){            // Always cancel discovery because it will slow down a connection
            	mAdapter.cancelDiscovery();
            }
            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                try {		// Close the socket
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.d(TAG, "unable to close() " +  "Secure" +
                            " socket during connection failure", e2);
                }
                connectionFailed();
                Initiator.logger.appendError(e);
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothChatService.this) {
                mConnectThread = null;
            }
            // Start the connected thread
            connected(mmSocket, mmDevice,  "Secure");
        }
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.d(TAG, "close() of connect " +  "Secure" + " socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private boolean reading = true;

        public ConnectedThread(BluetoothSocket socket, String socketType) {
            Log.d(TAG, "create ConnectedThread: " + socketType);
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;        
            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.d(TAG, "temp sockets not created", e);
                is_connected = false;
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            if( socket.isConnected()){
            	is_connected = false;
            }
        }
        @Override
    	public void run() {
        	while (reading){
        		synchronized (ConnectedThread.this) {
        		if(is_connected){
		        	Log.i(TAG, "BEGIN mConnectedThread");
		            // Keep listening to the InputStream while connected
		                try {
		                    byte[] buffer = new byte[ 512 ];
		                    int bytes = mmInStream.read(buffer);
		                    // construct a string from the valid bytes in the buffer
		                    String readMessage = new String(buffer, 0, bytes);
		                    //Log.i(Constant.TAG, "buffer read " + readMessage );
		    				//input_parser.readInput(readMessage);
		                 //   mReader.sendData(readMessage);
		                 //   mReader.run();
		                    mHandler.obtainMessage(BluetoothChatService.MESSAGE_READ, bytes, -1, readMessage).sendToTarget();   // Send the obtained bytes to the UI Activity
		                } catch (IOException e) {
		                    Log.d(TAG, "disconnected", e);
		                    connectionLost();
		                    BluetoothChatService.this.start();    // Start the service over to restart listening mode
		                    break;
		                }
		            }
        		}
            }
        }
        /**
         * Write to the connected OutStream.
         * @param buffer  The bytes to write
         */
        public void write(byte[] buffer, int length) {
            try {
                mmOutStream.write(buffer, 0, length);
            //    String str = new String(buffer, "UTF-8");
           //     Log.i(TAG, "buffer write " + str );
                // Share the sent message back to the UI Activity
            } catch (IOException e) {
                Log.d(TAG, "Exception during write", e);
            }
        }  
        public void destroy() {
        	reading = true;
//        	synchronized (ConnectedThread.this) {
	        	try {
					mmSocket.close();
		        	mmInStream.close();
		        	mmOutStream.close();
	        	} catch (IOException e) {
					Log.d(TAG, "destroy() failed", e);
				}
  //      	}
        }
    }
	public void connectBTDeviceId(String address) {    	
        BluetoothDevice device = mAdapter.getRemoteDevice(address);        // Get the BluetoothDevice object    	
        this.connect(device);		//Attempt to connect to the device
	}

	public boolean initBt() {
        Log.d(TAG, "++ ON START ++");
        if(mAdapter==null){
        	return false;
        }
        if (mAdapter.isEnabled()) {
            return true;
        }else{
        	return false;
        }
	}
}
