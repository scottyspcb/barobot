package com.barobot.other;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.os.PowerManager;
import android.text.format.Formatter;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import com.barobot.common.Initiator;
public class Android {
	public void powerOff( Context c ){
		PowerManager powerManager = (PowerManager)c.getSystemService(Context.POWER_SERVICE);
		powerManager.reboot(null);
		// if doesn't work
		try {
		    Process proc = Runtime.getRuntime().exec(new String[]{ "su", "-c", "reboot -p" });
		    proc.waitFor();
		} catch (Exception e) {
			Initiator.logger.appendError(e);
		}
	}
    public static String loadTextFile(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        int size = inputStream.available();
        byte[] bytes = new byte[size];
        int len = 0;
        while ((len = inputStream.read(bytes)) > 0){
        	Log.i("czytam:", new String(bytes) );
        	byteStream.write(bytes, 0, len);
        }
		Log.i("odczytano:", byteStream.toString() );
        
		return new String(byteStream.toByteArray(), "UTF8");
      }
	public static String readAsset( AssetManager am, String name ){
		//StringBuilder sb = new StringBuilder(); 
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(am.open( name ), "UTF-8")); 
			    String mLine = reader.readLine();
			    while (mLine != null) {
			    	Log.i("czytam2:", new String(mLine) );
			    	mLine = reader.readLine(); 
			    }
			    reader.close();

			InputStream input = am.open(name);
		    String text = loadTextFile(input);
			/*
	        int size = input.available();
	        byte[] buffer = new byte[size];
	        input.read(buffer);
	        input.close();
	 //       String text = new String(buffer);
*/
			return text;
			/*
		   */
		} catch (IOException e) {
			 Initiator.logger.appendError(e);
		}
		return "";
	}
	public static boolean assetExists(AssetManager am, String path) {
	    boolean bAssetOk = false;
	    try {
	        InputStream stream = am.open( path);
	        stream.close();
	        bAssetOk = true;
	    } catch (FileNotFoundException e) {
	    	return false;
	    } catch (IOException e) {
	    	return false;
	    }
	    return bAssetOk;
	}

	public static String readRawTextFile(Context ctx, int resId){
	    InputStream inputStream			= ctx.getResources().openRawResource(resId);
	    InputStreamReader inputreader	= new InputStreamReader(inputStream);
	    BufferedReader buffreader		= new BufferedReader(inputreader);
	    String line;
	    StringBuilder text				= new StringBuilder();
	    try {
	        while (( line = buffreader.readLine()) != null) {
	            text.append(line);
	            text.append('\n');
	        }
	    } catch (IOException e) {
	        return null;
	    }
	    return text.toString();
	}
	
	public static boolean createDirIfNotExists(String path) {
	    boolean ret = true;

	    File file = new File(Environment.getExternalStorageDirectory(), path);
	    if (!file.exists()) {
	        if (!file.mkdirs()) {
	            Log.e("TravellerLog :: ", "Problem creating folder:"+path );
	            ret = false;
	        }
	    }
	    return ret;
	}
	public static void prepareSleep(Context ctx){
		PowerManager pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE); 
		PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Tag"); 
		wl.acquire();
		//do what you need to do
		wl.release();
	}

	public static String getLocalIpAddress() {
		String res = "";
	    try {
	        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
	            NetworkInterface intf = en.nextElement();
	            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
	                InetAddress inetAddress = enumIpAddr.nextElement();
	                if (!inetAddress.isLoopbackAddress()) {
	                	String ip = Formatter.formatIpAddress(inetAddress.hashCode());
	                	res=  inetAddress.getHostAddress().toString();
	                	Log.e("getLocalIpAddress","log:"+res + " / " + ip);
	                }
	            }
	        }
	        return res;
	    } catch (SocketException ex) {
	    	Log.e("getLocalIpAddress","TravellerLog : geting ip problem");
	    }
	    return null;
	}
	
	public static String getLocalIpAddress2() {
	    try {
	        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
	            NetworkInterface intf = en.nextElement();
	            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
	                InetAddress inetAddress = enumIpAddr.nextElement();
	                if (!inetAddress.isLoopbackAddress()) {
	                    String ip = Formatter.formatIpAddress(inetAddress.hashCode());
	                    Log.i("getLocalIpAddress2", "***** IP="+ ip);
	                    return ip;
	                }
	            }
	        }
	    } catch (SocketException ex) {
	        Log.e("getLocalIpAddress2", ex.toString());
	    }
	    return null;
	}

	public static void copyAsset( Context ctx, String filename, String dest ) {
		AssetManager assetManager = ctx.getAssets();
	    InputStream in = null;
        OutputStream out = null;
        /*
        try {
            String[] files = assetManager.list("");
            for(String ffff : files) {
            	Log.e("tag", "Asset file: " + ffff );  
            }
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
        }
        try {
            String[] files = assetManager.list("default_database/");
            for(String ffff : files) {
            	Log.e("tag", "Asset file: " + ffff );  
            }
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
        }
        */
        
        try {
			in = assetManager.open(filename);
		} catch (IOException e) {
			Log.e("tag", "Failed 1 to copy asset file: " + filename, e);
			return;
		} 
        File outFile = new File(dest);
        try {
			out = new FileOutputStream(outFile);
		} catch (FileNotFoundException e) {
			Log.e("tag", "Failed 2 to copy asset file: " + filename, e);
		}    
        try {
          copyFile(in, out);
          in.close();
          in = null;
          out.flush();
          out.close();
          out = null;
        } catch(IOException e) {
            Log.e("tag", "Failed 3 to copy asset file: " + filename, e);
        } 
	}
	private static void copyFile(InputStream in, OutputStream out) throws IOException {
	    byte[] buffer = new byte[1024];
	    int read;
	    while((read = in.read(buffer)) != -1){
	      out.write(buffer, 0, read);
	    }
	}
}
