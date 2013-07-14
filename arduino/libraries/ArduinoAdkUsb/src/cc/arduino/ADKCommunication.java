package cc.arduino;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.ParcelFileDescriptor;
import android.util.Log;

//import com.android.future.usb.UsbAccessory;
//import com.android.future.usb.UsbManager;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;

public class ADKCommunication implements Runnable {
	/* ADK items */
	private static final String ACTION_USB_PERMISSION = "com.google.android.DemoKit.action.USB_PERMISSION";

	private UsbManager mUsbManager;
	private PendingIntent mPermissionIntent;
	private boolean mPermissionRequestPending;

	private UsbAccessory mAccessory;
	private ParcelFileDescriptor mFileDescriptor;
	private FileInputStream mInputStream;
	private FileOutputStream mOutputStream;

	/* Byte buffers */
	private byte[] rawbuffer;
	private int bytes = 0;
	private byte[] buffer;

	/* The sketch's context, needed to get system services */
	private Context context;

	/**
	 * Constructor...
	 */
	public ADKCommunication(Context context) {
		this.context = context;

		/* Instantiate the UsbManager */
		mUsbManager = UsbManager.getInstance(context);
	}

	/**
	 * Returns the read bytes from the arduino.
	 * 
	 * @return
	 */
	public byte[] getRawBuffer() {
		return rawbuffer;
	}

	/**
	 * Tries to connect to a UsbAccessory, basically this just sets up the
	 * streams for the communication.
	 */
	public void connect(UsbAccessory accessory) {

		/* If the accessory is null, we can't connect! */
		if (accessory == null) {
			// TODO possibly render some warning here...?
			Log.i("System.out",
					"Accessory was null, had to quit the connect attempt");
			return;
		}

		Log.i("System.out",
				"Trying to connect to accessory " + accessory.getDescription());

		/* If the streams already exist, the connection is "up" */
		if (mInputStream != null && mOutputStream != null) {
			Log.i("System.out",
					"Streams already made... let's not do it again. ");
			return;
		}

		/* If the UsbManager isn't instantiated, do it! */
		if (mUsbManager == null) {
			mUsbManager = UsbManager.getInstance(context);
			Log.i("System.out", "UsbManager was null, tried to make it again ("
					+ (mUsbManager != null ? "success" : "fail"));
		}

		/* The user needs to grant permission to connect to the accessory */
		if (mUsbManager.hasPermission(accessory)) {
			Log.i("System.out", "Trying to open the accessory");
			openAccessory(accessory);
		} else {
			/*
			 * Or, if the permission hasn't been granted yet we need to ask for
			 * permission
			 */
			synchronized (mUsbReceiver) {
				if (!mPermissionRequestPending) {
					mUsbManager.requestPermission(accessory, mPermissionIntent);
					mPermissionRequestPending = true;
				}
			}
		}
	}

	public boolean isConnected() {
		return (mAccessory != null ? true : false);
	}

	/**
	 * Writes the character to the outputstream
	 * 
	 * @param c
	 * @throws IOException
	 */
	public void write(byte buffer) throws IOException {
		if (mOutputStream != null)
			mOutputStream.write(buffer);

		// TODO some sort of report handling here?
	}

	/**
	 * Returns a list of all connected accessories.
	 * 
	 * @return
	 */
	public UsbAccessory[] getAccessories() {
		/* If the UsbManager isn't instantiated, do it! */
		if (mUsbManager == null)
			mUsbManager = UsbManager.getInstance(context);

		/* Get the list of connected accessories */
		UsbAccessory[] accessories = mUsbManager.getAccessoryList();

		/* If there are no connected accessories, return nothing */
		if (accessories == null) {
			// TODO, possibly render some sort of error message here?
			return null;
		}

		return accessories;
	}

	private void openAccessory(UsbAccessory accessory) {
		/* Try to open the accessory */
		mFileDescriptor = mUsbManager.openAccessory(accessory);

		Log.i("test", "mFileDescriptor? "
				+ (mFileDescriptor != null ? "ok!" : "not ok!"));

		/* If the open didn't fail, continue to create streams for com. */
		if (mFileDescriptor != null) {
			/* Set the current accessory */
			mAccessory = accessory;

			/* Open the streams */
			FileDescriptor fd = mFileDescriptor.getFileDescriptor();
			mInputStream = new FileInputStream(fd);
			mOutputStream = new FileOutputStream(fd);
			Thread thread = new Thread(null, this, "AndroidAdkUsb");
			thread.start();
		} else {
			// TODO something to notice we failed?
		}
	}

	private void closeAccessory() {
		try {
			if (mFileDescriptor != null) {
				mFileDescriptor.close();
			}
		} catch (IOException e) {
			// TODO do some better handling, or at least some notices here?
		} finally {
			mFileDescriptor = null;
			mAccessory = null;
		}
	}

	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (ACTION_USB_PERMISSION.equals(action)) {
				synchronized (this) {
					UsbAccessory accessory = UsbManager.getAccessory(intent);
					if (intent.getBooleanExtra(
							UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						Log.i("System.out",
								"Opening " + accessory.getDescription());
						openAccessory(accessory);
					} else {
						Log.i("System.out", "Permission denied for "
								+ accessory.getDescription());
					}
					mPermissionRequestPending = false;
				}
			} else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
				UsbAccessory accessory = UsbManager.getAccessory(intent);
				if (accessory != null && accessory.equals(mAccessory)) {
					closeAccessory();
				}
			}
		}
	};

	/**
	 * Returns the number of bytes available in the stream.
	 * 
	 * @return
	 */
	public int available() {
		return bytes;
	}

	@Override
	public void run() {
		rawbuffer = new byte[128];
		buffer = new byte[128];

		while (bytes >= 0) {
			try {
				/* Read the buffer from Arduino */
				bytes = mInputStream.read(rawbuffer);

				/* Clone the raw buffer */
				buffer = rawbuffer.clone();
			} catch (IOException e) {
				/* Set available to 0 */
				bytes = 0;
				break;
			}
		}
	}

	/**
	 * Returns the first available byte in "buffer" and then removes it.
	 * 
	 * @return
	 */
	public byte readByte() {
		/* Get the first byte */
		byte b = buffer[0];

		/* Remove the read byte from the buffer */
		if (bytes > 0) {
			/* This essentially copies the buffer to itself*/
			System.arraycopy(buffer, 1, buffer, 0, buffer.length - 1);
			
			/* Decrease the available bytes */
			bytes--;
		} else {
			/* Clone the raw buffer */
			buffer = rawbuffer.clone();
		}

		return (byte)(b&0xFF);
	}

}
