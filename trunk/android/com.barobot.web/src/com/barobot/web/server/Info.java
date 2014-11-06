package com.barobot.web.server;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;

import android.content.Context;
import android.net.wifi.WifiManager;

import com.barobot.common.Initiator;

public class Info {

	protected String wifiIpAddress(Context context) {
	    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	    int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
	    // Convert little-endian to big-endianif needed
	    if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
	        ipAddress = Integer.reverseBytes(ipAddress);
	    }

	    byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

	    String ipAddressString;
	    try {
	        ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
	    } catch (UnknownHostException ex) {
	    	Initiator.logger.e("wifiIpAddress", "Unable to get host address.");
	        ipAddressString = null;
	    }
	    return ipAddressString;
	}	
}
