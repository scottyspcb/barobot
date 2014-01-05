package com.barobot.hardware;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.PowerManager;
import android.util.Log;

import java.io.ByteArrayOutputStream;
public class Android {
	
	public void powerOff( Context c ){
		PowerManager powerManager = (PowerManager)c.getSystemService(Context.POWER_SERVICE);
		powerManager.reboot(null);

		// if doesn't work
		try {
		    Process proc = Runtime.getRuntime().exec(new String[]{ "su", "-c", "reboot -p" });
		    proc.waitFor();
		} catch (Exception ex) {
		    ex.printStackTrace();
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
			 e.printStackTrace();
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
}
