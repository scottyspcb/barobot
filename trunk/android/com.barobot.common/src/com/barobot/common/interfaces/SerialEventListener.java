package com.barobot.common.interfaces;

public interface SerialEventListener {
	public void onConnect();
	public void onClose();
	public void connectedWith(String bt_connected_device, String address);
}
