package com.barobot.other;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.text.format.Formatter;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.orman.mapper.Model;
import org.orman.mapper.ModelQuery;
import org.orman.sql.C;
import org.orman.sql.Query;

import com.barobot.BarobotMain;
import com.barobot.R;
import com.barobot.activity.BarobotActivity;
import com.barobot.common.Initiator;
import com.barobot.gui.database.BarobotData;
import com.barobot.gui.dataobjects.Robot;
import com.barobot.gui.dataobjects.Slot;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.parser.Queue;
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
	                if (!inetAddress.isLoopbackAddress()) {		// wrong:    fe80::2c6:a1ff:fe00:5ed5%wlan0
	                	String ip = Formatter.formatIpAddress(inetAddress.hashCode());
	                	String address= inetAddress.getHostAddress().toString();
	                	if( !address.contains("%")){
	                		res= address;
		                	Log.e("getLocalIpAddress","log: "+res + " / " + ip + "/" + inetAddress.hashCode());
	                	}
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

	public static boolean copyAsset( Context ctx, String filename, String dest ) {
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
			BarobotMain.lastException = e;
			return false;
		} 
        File outFile = new File(dest);
        try {
			out = new FileOutputStream(outFile);
		} catch (FileNotFoundException e) {
			Log.w("tag", "Failed 2 to copy asset file: " + filename, e);
			BarobotMain.lastException = e;
			return false;
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
            BarobotMain.lastException = e;
            return false;
        } 
        return true;
	}
	private static void copyFile(InputStream in, OutputStream out) throws IOException {
	    byte[] buffer = new byte[1024];
	    int read;
	    while((read = in.read(buffer)) != -1){
	      out.write(buffer, 0, read);
	    }
	}

	public static int isOnline( Context c) {
	    ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	        return netInfo.getType();
	    }
	    return -1;
	}
	public static long getTimestamp() {
		java.util.Date date= new java.util.Date();
		return new java.sql.Timestamp(date.getTime()).getTime();
	}

	public static void createRobot( int old_robot_id, int new_robot_id){
		Query sql = ModelQuery.select().from(Robot.class).where(C.eq("id", new_robot_id)).limit(1).getQuery();
		Robot robot = Model.fetchSingle(sql,Robot.class);
		Initiator.logger.i("Android.createRobot",sql.getExecutableSql()); 
		if(robot==null){
			// FUCK YOU ORMAN ORM. I WANT TO SET ID WITHOUT AUTOINC!!!!
			Query query4 = new Query("INSERT OR REPLACE INTO `robot` (`id`,`serial`,`sversion`,`is_current`) VALUES ('"+new_robot_id+"','"+new_robot_id+"','"+new_robot_id+"','1')");
			BarobotData.omdb.getExecuter().executeOnly(query4);
			/*
			robot = new Robot();
			robot.id		= new_robot_id;		// aaaaaaaaaaaa. doesn't work with OROMAN ORM !!!!!!
			robot.serial	= new_robot_id;
			robot.sversion	= new_robot_id;
			robot.is_current= true;
			robot.insert();
			*/
			robot = Model.fetchSingle(ModelQuery.select().from(Robot.class).where(C.eq("id", new_robot_id)).limit(1).getQuery(),Robot.class);
		}

		for (int i= 1 ; i <= 12; i++)
		{
			Slot slot = Model.fetchSingle(ModelQuery.select().from(Slot.class).where(		// (at possition and (robot_id = robot_id or 0))
					C.and(
							C.eq("robot_id", new_robot_id),
							C.eq("position", i)
					)
				).limit(1).getQuery(), Slot.class);

			if(slot == null){
				Slot slot_old = Model.fetchSingle(ModelQuery.select().from(Slot.class).where(		// (at possition and (robot_id = robot_id or 0))
						C.and(
								C.or(
										C.eq("robot_id", 0),
										C.eq("robot_id", old_robot_id)
								), C.eq("position", i)
						)
					).orderBy("Slot.robot_id").limit(1).getQuery(), Slot.class);	// load old settings

				Slot slot2 = new Slot();
				slot2.position			= i;
				slot2.robot_id			= new_robot_id;
				slot2.dispenser_type 	= (slot_old == null) ? 20 		: slot_old.dispenser_type;
				slot2.status			= (slot_old == null) ? "Empty" 	: slot_old.status;
				slot2.product			= (slot_old == null) ? null 	: slot_old.product;
				slot2.insert();
			}
		}
	}
	public static void pourFromBottle(int position, Queue q) {
		BarobotConnector barobot = Arduino.getInstance().barobot;

		int robot_id = barobot.getRobotId();
		Slot slot = Model.fetchSingle(ModelQuery.select().from(Slot.class).where(
				C.and(
						C.eq("robot_id", robot_id ),
						C.eq("position", position )
				)
			).limit(1).getQuery(), Slot.class);
		if( slot != null ){
			barobot.moveToBottle(q, position, true);
			barobot.pour(q, slot.dispenser_type, position, true);
		}
	}

	public static void createShortcutOnDesktop( Activity act) {
	    Intent shortcutIntent = new Intent();
	    shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, Android.getIntentShortcut());
	    shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "Barobot" );
	    shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(act, R.drawable.app_icon));
	    shortcutIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
	    act.sendBroadcast(shortcutIntent);
	    
	    
	   
	    Intent shortcutIntent4 = new Intent(act.getApplicationContext(),BarobotActivity.class);
	    Intent shortcutIntent2 = new Intent();
	    shortcutIntent2.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent4);
	    shortcutIntent2.putExtra(Intent.EXTRA_SHORTCUT_NAME, "Barobot2" );
	    shortcutIntent2.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(act, R.drawable.app_icon));
	    shortcutIntent2.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
	    act.sendBroadcast(shortcutIntent2);
	      
	    
	    
	    
//	    act.finish();

	}
	private static Intent getIntentShortcut(){       
	 //   Intent i = new Intent();
	 //   i.setClassName("com.barobot", ".activity.BarobotActivity");
	    
	    Intent i = new Intent("com.barobot.activity.BarobotActivity");
	    
	    
	    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	    return i;
	}	
	public static String tttt( Exception e ){
		String DOUBLE_LINE_SEP = "\n\n";
		String SINGLE_LINE_SEP = "\n";
	
		StackTraceElement[] arr = e.getStackTrace();
        final StringBuffer report = new StringBuffer(e.toString());
        final String lineSeperator = "-------------------------------\n\n";
        report.append(DOUBLE_LINE_SEP);
        report.append("--------- Stack trace ---------\n\n");
        for (int i = 0; i < arr.length; i++) {
            report.append( "    ");
            report.append(arr[i].toString());
            report.append(SINGLE_LINE_SEP);
        }
        report.append(lineSeperator);
        // If the exception was thrown in a background thread inside
        // AsyncTask, then the actual exception can be found with getCause
        report.append("--------- Cause ---------\n\n");
        Throwable cause = e.getCause();
        if (cause != null) {
            report.append(cause.toString());
            report.append(DOUBLE_LINE_SEP);
            arr = cause.getStackTrace();
            for (int i = 0; i < arr.length; i++) {
                report.append("    ");
                report.append(arr[i].toString());
                report.append(SINGLE_LINE_SEP);
            }
        }
        // Getting the Device brand,model and sdk verion details.
        report.append(lineSeperator);
        report.append("--------- Device ---------\n\n");
        report.append("Brand: ");
        report.append(Build.BRAND);
        report.append(SINGLE_LINE_SEP);
        report.append("Device: ");
        report.append(Build.DEVICE);
        report.append(SINGLE_LINE_SEP);
        report.append("Model: ");
        report.append(Build.MODEL);
        report.append(SINGLE_LINE_SEP);
        report.append("Id: ");
        report.append(Build.ID);
        report.append(SINGLE_LINE_SEP);
        report.append("Product: ");
        report.append(Build.PRODUCT);
        report.append(SINGLE_LINE_SEP);
        report.append(lineSeperator);
        report.append("--------- Firmware ---------\n\n");
        report.append("SDK: ");
        report.append(Build.VERSION.SDK);
        report.append(SINGLE_LINE_SEP);
        report.append("Release: ");
        report.append(Build.VERSION.RELEASE);
        report.append(SINGLE_LINE_SEP);
        report.append("Incremental: ");
        report.append(Build.VERSION.INCREMENTAL);
        report.append(SINGLE_LINE_SEP);
        report.append(lineSeperator);

        return report.toString();
	}
	
	
	
	
	public static boolean isConnected(Context context) {
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        return plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB;
    }
	
	
	
	
	
}
