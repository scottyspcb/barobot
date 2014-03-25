package com.barobot.common.interfaces.serial;

public interface SerialEventListener {
	public void onConnect();
	public void onClose();
	public void connectedWith(String bt_connected_device, String address);
}
