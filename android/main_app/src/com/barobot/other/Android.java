package com.barobot.other;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Debug;
import android.os.Environment;
import android.os.PowerManager;
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
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.message.Mainboard;
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
    public static boolean copy(String src1, String dst1) throws IOException {
		File src = new File(src1);
		File dst = new File(dst1);
	
		InputStream in = new FileInputStream(src);
		OutputStream out = new FileOutputStream(dst);
	
		// Transfer bytes from in to out
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
		return true;
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
	            //    	String ip = Formatter.formatIpAddress(inetAddress.hashCode());
	                	String address= inetAddress.getHostAddress().toString();
	                	if( !address.contains("%")){
	                		res= address;
		            //    	Log.e("getLocalIpAddress","log: "+res + " / " + ip + "/" + inetAddress.hashCode());
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
	    Intent shortcutIntent4 = new Intent(act.getApplicationContext(),BarobotActivity.class);
	    Intent shortcutIntent2 = new Intent();
	    shortcutIntent2.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent4);
	    shortcutIntent2.putExtra(Intent.EXTRA_SHORTCUT_NAME, "Barobot" );
	    shortcutIntent2.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(act, R.drawable.app_icon));
	    shortcutIntent2.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
	    act.sendBroadcast(shortcutIntent2);
//	    act.finish();
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
        report.append("Release: ");
        report.append(Build.VERSION.RELEASE);
        report.append(SINGLE_LINE_SEP);
        report.append("Incremental: ");
        report.append(Build.VERSION.INCREMENTAL);
        report.append(SINGLE_LINE_SEP);
        report.append(lineSeperator);
        return report.toString();
	}

	public static boolean isDcConnected(Context context) {
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        return plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB;
    }

	public static int isOnline( Context c) {
	    ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	        return netInfo.getType();
	    }
	    return -1;
	}
	public static void readTabletTemp(Queue q) {
		q.add( new AsyncMessage( true ) {
			@Override	
			public String getName() {
				return "check  tablet temp" ;
			}
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				Application ba = BarobotMain.getInstance().getApplication();
			    SensorManager mSensorManager = (SensorManager)ba.getSystemService(Context.SENSOR_SERVICE);
			    Sensor mTemperature = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE );
			    int power = Math.round(mTemperature.getPower() * 10000);
				BarobotConnector barobot = Arduino.getInstance().barobot;
				barobot.state.set("TABLET_TEMPERATURE", power );
				return null;
			}
		} );
	}

	public static float readCpuUsage() {
	    try {
	        RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
	        String load = reader.readLine();

	        String[] toks = load.split(" ");

	        long idle1 = Long.parseLong(toks[4]);
	        long cpu1 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[5])
	              + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

	        try {
	            Thread.sleep(360);
	        } catch (Exception e) {}

	        reader.seek(0);
	        load = reader.readLine();
	        reader.close();

	        toks = load.split(" ");

	        long idle2 = Long.parseLong(toks[4]);
	        long cpu2 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[5])
	            + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

	        return (float)(cpu2 - cpu1) / ((cpu2 + idle2) - (cpu1 + idle1));

	    } catch (IOException ex) {
	        ex.printStackTrace();
	    }

	    return 0;
	}
	public static int[] readMemUsage() {
		Debug.MemoryInfo memoryInfo = new Debug.MemoryInfo();
        Debug.getMemoryInfo(memoryInfo);

        String memMessage = String.format("App Memory: Pss=%.2f MB, Private=%.2f MB, Shared=%.2f MB",
            memoryInfo.getTotalPss() / 1024.0,
            memoryInfo.getTotalPrivateDirty() / 1024.0,
            memoryInfo.getTotalSharedDirty() / 1024.0);

        Log.i("log_tag", memMessage);
        //Pss, Private,Shared
		int mem[]	= {memoryInfo.getTotalPss(),memoryInfo.getTotalPrivateDirty(), memoryInfo.getTotalSharedDirty() };
		return mem;
	}
	public static void alertMessage(final Activity activity, final String msg) {
		BarobotMain.getInstance().runOnUiThread(new Runnable() {
			  public void run() {
				  new AlertDialog.Builder(activity).setTitle("Message").setMessage(msg)
				    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int which) { 
				        }
				    })
				    .setIcon(android.R.drawable.ic_dialog_alert).show();
			  }
			});
	}
	public static int checkNewSoftwareVersion(boolean alertResult, Activity act ) {
		int isOnline = Android.isOnline(act);
		if(isOnline > -1 ){	// check ne version of firmware and APK
			final BarobotConnector barobot = Arduino.getInstance().barobot;
			if( barobot.getRobotId() == 0 ){
				Queue q = new Queue();
				barobot.readHardwareRobotId(q);			// check hardware version
				q.add( new AsyncMessage( true ) {		// when version readed
					@Override
					public String getName() {
						return "Check robot_id";
					}
					@Override
					public Queue run(Mainboard dev, Queue queue) {
						if( barobot.getRobotId() == 0 && barobot.robot_id_ready ){						// once again
							int robot_id = UpdateManager.getNewRobotId();		// download new robot_id (init hardware)
							Initiator.logger.w("onResume", "robot_id" + robot_id);
							if( robot_id > 0 ){		// save robot_id to android and arduino
								Queue q = new Queue();
								barobot.setRobotId( q, robot_id);
								return q;	// before all other commands currently in queue
							}
						}
						return null;
					}
				});
				barobot.main_queue.add(q);
			}
			UpdateManager.checkNewVersion( act, alertResult );
			return 1;
		}else{
			if(alertResult){
				alertMessage(act, "No connection");
			}
			return 0;
		}
	} 

	public static void askForTurnOff( Activity act ) {
		Intent i = new Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN");
		i.putExtra("android.intent.extra.KEY_CONFIRM", true);
		act.startActivity(i);
	}
}
