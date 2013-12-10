
package com.barobot.wire;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.barobot.BarobotMain;
import com.barobot.drinks.RunnableWithData;
import com.barobot.hardware.virtualComponents;
import com.barobot.utils.Constant;
import com.barobot.utils.input_parser;


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

    // Member fields
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;
    private static boolean hasInstance	= false;
    public boolean is_connected			= false;
	public String bt_connected_device	= null;
    /**
     * Constructor. Prepares a new BluetoothChat session.
     * @param context  The UI Activity Context
     * @param handler  A Handler to send messages back to the UI Activity
     * @param barobotMain 
     * @throws Exception 
     */
    public BluetoothChatService(Handler handler) throws Exception {
    	if(hasInstance){
    		throw new Exception("duplikat bluetooth");
    	}
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = Constant.STATE_NONE;
        mHandler = handler;
        hasInstance = true;

        BarobotMain barobotMain =  BarobotMain.getInstance();
        IntentFilter f1 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        IntentFilter f2 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        barobotMain.registerReceiver(this.btEvents, f1);
        barobotMain.registerReceiver(this.btEvents, f2);
    }
 
	// The BroadcastReceiver that listens for discovered devices and
    // changes the title when discovery is finished
    public final BroadcastReceiver btEvents = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
            	Constant.log(Constant.TAG,"ACTION_ACL_DISCONNECTED");
                is_connected						= false;
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
            	Constant.log(Constant.TAG,"ACTION_ACL_DISCONNECT_REQUESTED");
                is_connected						= false;
            }
        }
    };

    /**
     * Set the current state of the chat connection
     * @param state  An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        //if (Constant.D) Log.d(Constant.TAG2, "setState() " + mState + " -> " + state);
        mState = state;
        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(Constant.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
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
      //  if (Constant.D) Log.d(Constant.TAG2, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.destroy(); mConnectedThread = null;}
        setState(Constant.STATE_LISTEN);
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    public synchronized void connect(BluetoothDevice device) {
        if (mState == Constant.STATE_CONNECTING) {        // Cancel any thread attempting to make a connection
            if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        }
        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.destroy(); mConnectedThread = null;}

        mConnectThread = new ConnectThread(device);        // Start the thread to connect with the given device
        mConnectThread.start();
        setState(Constant.STATE_CONNECTING);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice
            device, final String socketType) {
        if (Constant.D) Log.d(Constant.TAG2, "connected, Socket Type:" + socketType);
        
        // Cancel the thread that completed the connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.destroy(); mConnectedThread = null;}

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket, socketType);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(Constant.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(Constant.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        setState(Constant.STATE_CONNECTED);
        virtualComponents.set( "LAST_BT_DEVICE",device.getAddress());    	// remember device ID
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        if (Constant.D) Log.d(Constant.TAG2, "stop");
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mConnectedThread != null) {
            mConnectedThread.destroy();
            mConnectedThread = null;
        }
        setState(Constant.STATE_NONE);
    }
	public void destroy() {
    	BluetoothChatService.hasInstance = false;
		try {
			BarobotMain.getInstance().unregisterReceiver(this.btEvents);
		} catch (IllegalArgumentException e) {
		}
		this.stop();
	}
	

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != Constant.STATE_CONNECTED) {
            	return;
            }
            r = mConnectedThread;
        }
        /*
		try {
			String str = new String(out, "UTF-8");
			if (Constant.D) Log.d(Constant.TAG2, "Wysylam write: " + str);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
        // Perform the write unsynchronized
        r.write(out);
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
          //      Constant.log(Constant.TAG2, "Socket Type: " +  "Secure" + "create() failed", e);
            } catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
            mmSocket = tmp;
        }

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
                    Constant.log(Constant.TAG2, "unable to close() " +  "Secure" +
                            " socket during connection failure", e2);
                }
                connectionFailed();
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
                Constant.log(Constant.TAG2, "close() of connect " +  "Secure" + " socket failed", e);
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
            Log.d(Constant.TAG, "create ConnectedThread: " + socketType);
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Constant.log(Constant.TAG, "temp sockets not created", e);
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }
        public void run() {
            synchronized (ConnectedThread.this) {
	        	Log.i(Constant.TAG, "BEGIN mConnectedThread");
	            // Keep listening to the InputStream while connected
	            while (reading) {
	                try {
	                    byte[] buffer = new byte[ 512 ];
	                    int bytes = mmInStream.read(buffer);
	                    // construct a string from the valid bytes in the buffer
	                    String readMessage = new String(buffer, 0, bytes);
	                    //Log.i(Constant.TAG, "buffer read " + readMessage );
	    				//input_parser.readInput(readMessage);
	                 //   mReader.sendData(readMessage);
	                 //   mReader.run();
	                    mHandler.obtainMessage(Constant.MESSAGE_READ, bytes, -1, readMessage).sendToTarget();   // Send the obtained bytes to the UI Activity
	                } catch (IOException e) {
	                    Constant.log(Constant.TAG, "disconnected", e);
	                    connectionLost();
	                    BluetoothChatService.this.start();    // Start the service over to restart listening mode
	                    break;
	                }
	            }
            }
        }
        /**
         * Write to the connected OutStream.
         * @param buffer  The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
            //    String str = new String(buffer, "UTF-8");
           //     Log.i(Constant.TAG, "buffer write " + str );
                // Share the sent message back to the UI Activity
            } catch (IOException e) {
                Constant.log(Constant.TAG, "Exception during write", e);
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
					Constant.log(Constant.TAG, "destroy() failed", e);
				}
  //      	}
        }
    }
	public void connectBTDeviceId(String address) {    	
        BluetoothDevice device = mAdapter.getRemoteDevice(address);        // Get the BluetoothDevice object    	
        this.connect(device);		//Attempt to connect to the device
	}

	public boolean initBt() {
        Constant.log(Constant.TAG, "++ ON START ++");
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
