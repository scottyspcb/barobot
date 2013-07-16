/**
 * you can put a one sentence description of your library here.
 *
 * ##copyright##
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA  02111-1307  USA
 * 
 * @author		##author##
 * @modified	##date##
 * @version		##version##
 */

package cc.arduino;

import java.io.IOException;

import android.content.Context;

//import com.android.future.usb.UsbAccessory;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;

/**
 * This is a template class and can be used to start a new processing library or
 * tool. Make sure you rename this class as well as the name of the example
 * package 'template' to your own lobrary or tool naming convention.
 * 
 * @example Hello
 * 
 *          (the tag @example followed by the name of an example included in
 *          folder 'examples' will automatically include the example in the
 *          javadoc.)
 * 
 */

/* TODO */
// available() *
// connect() *
// disconnect()
// readChar() *
// readBytes()
// readBytesUntil()
// readString()
// readStringUntil()
// buffer() *
// bufferUntil()
// last()
// lastChar()
// write() *
// clear()
// stop()

public class ArduinoAdkUsb {

	// myParent is a reference to the parent sketch
	private Context myParent;

	public final static String VERSION = "##version##";

	/* ADKCommunication */
	private ADKCommunication comm;

	/**
	 * a Constructor, usually called in the setup() method in your sketch to
	 * initialize and start the library.
	 * 
	 * @example Hello
	 * @param theParent
	 */
	public ArduinoAdkUsb(Context theParent) {
		myParent = theParent;
		welcome();

		/* Comm */
		comm = new ADKCommunication(myParent);
	}

	/**
	 * Returns >0 if any bytes are available in the buffer.
	 * 
	 * @return
	 */
	public int available() {
		return comm.available();
	}

	/**
	 * Returns the raw byte buffer.
	 * 
	 * @return
	 */
	public byte[] buffer() {
		return comm.getRawBuffer();
	}

	/**
	 * Gets the first byte in line
	 * 
	 * @return
	 */
	public byte readByte() {
		// TODO, should reformat this as 0 to 255 instead of the current -128 to
		// 128
		return comm.readByte();
	}

	/**
	 * Gets the first byte in line as char
	 * 
	 * @return
	 */
	public char readChar() {
		return (char) comm.readByte();
	}

	/**
	 * Connects to the selected accessory
	 * 
	 * @param accessory
	 */
	public void connect(UsbAccessory accessory) {
		try {
			comm.connect(accessory);
		} catch (Exception e) {
			// TODO better error handling...
		}
	}

	/**
	 * Returns the status of the connection.
	 * 
	 * @return
	 */
	public boolean isConnected() {
		return comm.isConnected();
	}

	public void write(char c) {
		try {
			comm.write((byte) c);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void write(byte b) {
		try {
			comm.write(b);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public UsbAccessory[] list() {
		return comm.getAccessories();
	}

	private void welcome() {
		System.out.println("##name## ##version## by ##author##");
	}

	/**
	 * return the version of the library.
	 * 
	 * @return String
	 */
	public static String version() {
		return VERSION;
	}
}
