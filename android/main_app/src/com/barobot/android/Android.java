package com.barobot.android;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import android.R.drawable;
import android.R.string;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Debug;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;

import org.orman.mapper.Model;
import org.orman.mapper.ModelQuery;
import org.orman.sql.C;
import org.orman.sql.Query;

import com.barobot.BarobotMain;
import com.barobot.R;
import com.barobot.activity.BarobotActivity;
import com.barobot.activity.OptionsActivity;
import com.barobot.common.Initiator;
import com.barobot.common.constant.Constant;
import com.barobot.gui.database.BarobotData;
import com.barobot.gui.dataobjects.Robot;
import com.barobot.gui.dataobjects.Slot;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.hardware.devices.PcbType;
import com.barobot.other.UpdateManager;
import com.barobot.parser.Queue;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.message.Mainboard;

public class Android {
	public static void powerOff( Context c ){
		try {
			PowerManager powerManager = (PowerManager)c.getSystemService(Context.POWER_SERVICE);
			powerManager.reboot(null);
		} catch (Exception e) {
			Initiator.logger.appendError(e);
		}
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
		//Query sql = ModelQuery.select().from(Robot.class).where(C.eq("id", new_robot_id)).limit(1).getQuery();
		//Robot robot = Model.fetchSingle(sql,Robot.class);
		Robot robot =  BarobotData.getOneObject(Robot.class, new_robot_id );
		//Initiator.logger.i("Android.createRobot",sql.getExecutableSql());
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
			robot =  BarobotData.getOneObject(Robot.class, new_robot_id );
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

	        
	        
	        Log.i("log_tag", ""+ cpu1 +"/"+cpu2 + " - " + idle1 + "/" + idle2 );
	        
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
		activity.runOnUiThread(new Runnable() {
			  public void run() {
				  Builder bb = new AlertDialog.Builder(activity).setTitle("Message").setMessage(msg)
				    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int which) { 
				        }
				    });
				  
				  bb.setIcon(android.R.drawable.ic_dialog_alert);
				  if (!activity.isFinishing()) {
					  bb.show();
				  }
			  }
			});
	}
	public static void askForTurnOff( final Activity activity ) {
		activity.runOnUiThread(new Runnable() {
			  public void run() {
				  Intent i = new Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN");
					i.putExtra("android.intent.extra.KEY_CONFIRM", true);
					activity.startActivity(i);
			  }
			});
	}
	public static String MD5Hash(String toHash) throws RuntimeException {
		   try{
		       return String.format("%032x", // produces lower case 32 char wide hexa left-padded with 0
		      new BigInteger(1, // handles large POSITIVE numbers 
		           MessageDigest.getInstance("MD5").digest(toHash.getBytes())));
		   } catch (NoSuchAlgorithmException e) {
		      // do whatever seems relevant
		   }
		   return null;
		}
	
	public static void askForTurnOff2( final Activity activity ) {
		activity.runOnUiThread(new Runnable() {
			  public void run() {
				  Window w = activity.getWindow();
					WindowManager.LayoutParams lp = w.getAttributes();
					lp.screenBrightness =.005f;
					w.setAttributes (lp);
			  }
			});

	}

	public static void shutdown_sys( )	{
	    Process chperm;
	    try {
	        chperm=Runtime.getRuntime().exec("su");
	          DataOutputStream os = 
	              new DataOutputStream(chperm.getOutputStream());
	              os.writeBytes("shutdown\n");
	              os.flush();
	              chperm.waitFor();
	    } catch (IOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    } catch (InterruptedException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    }
	}

	public static void restartApp(Context context, int delay) {
	    if (delay == 0) {
	        delay = 1;
	    }
	    Log.e("", "restarting app");
	    Intent restartIntent = context.getPackageManager()
	            .getLaunchIntentForPackage(context.getPackageName() );
	    PendingIntent intent = PendingIntent.getActivity(
	            context, 0,
	            restartIntent, Intent.FLAG_ACTIVITY_CLEAR_TOP);
	    AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	    manager.set(AlarmManager.RTC, System.currentTimeMillis() + delay, intent);
	    System.exit(2);
	}
	public static void pleaseResetApp( final Activity dbw ) {
		dbw.runOnUiThread(new Runnable() {
			  public void run() {
				  new AlertDialog.Builder(dbw)
				  .setTitle("Message").setMessage("Please restart application")
				    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int which) { 
				        }
				    })
				    .setIcon(android.R.drawable.ic_dialog_alert).show();
			  }
			});
	}
	public static void alertOk(final Activity dbw) {
		dbw.runOnUiThread(new Runnable() {
			  public void run() {
				  new AlertDialog.Builder(dbw).setTitle("Success").setMessage(R.string.upload_firmware_success)
				    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int which) { 
				        }
				    })
				    .setIcon(android.R.drawable.ic_dialog_alert).show();
			  }
			});
	}
	public static void askForWifiEnabled(final Activity activity) {
		final WifiManager wifiManager = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE); 
		final boolean wifiEnabled = wifiManager.isWifiEnabled();
		if(!wifiEnabled){
			activity.runOnUiThread(new Runnable() {
				  public void run() {
					  new AlertDialog.Builder(activity).setTitle("WIFI").setMessage(R.string.do_you_want_enable_wifi)
					    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					        public void onClick(DialogInterface dialog, int which) { 
					    		if(wifiEnabled){
					    			wifiManager.setWifiEnabled(true);
					    			//wifiManager.setWifiEnabled(false);
					    		}
					        }
					    })
					    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
					        public void onClick(DialogInterface dialog, int which) { 
					        }
					    })
					    .setIcon(android.R.drawable.ic_dialog_alert).show();
				  }
				});
		}
	}
	
	

	static Integer selectedOption = 0;
	
	public static void burnFirmware( final Activity dbw, final boolean manual ) {
		final BarobotConnector barobot	= Arduino.getInstance().barobot;
		barobot.state.getInt("BURN_PCB_TYPE", 0);

		final String[] options = new String[]{
        	"Servos ",
        	"Actuators L1650 (with 2 little holes on front panel)",
        	"Set new Robot ID only"
        };
		int default_option = Math.max(0, barobot.pcb_type - 2);			// 2 = 0, 3 = 1, 0 = 0 
		selectedOption = default_option;
		final Builder a = new AlertDialog.Builder(dbw)
        .setIconAttribute(android.R.attr.alertDialogIcon)
        .setTitle("Which Barobot version do you have? If you are not sure click Cancel.")
        .setSingleChoiceItems(options, default_option, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                /* User clicked on a radio button do some stuff */
	            selectedOption = whichButton;
	            Initiator.logger.w("button_click.firmware_download", "whichButton: " + whichButton);
            }
        })
        .setPositiveButton("I know what I'm doing. Burn firmware now", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
             	if( selectedOption == options.length - 1){
             		Android.setNewRobotId(dbw, barobot);
            	}else{
	            	int pcb_type1 = selectedOption + 2;		// 0 = 2, 1 = 3
	           //	int pcb_type1 = barobot.state.getInt("BURN_PCB_TYPE", 3);
		       //     barobot.state.getInt("BURN_PCB_TYPE", whichButton + 2);
	            	if( pcb_type1 == 2 || pcb_type1 == 3 ){
	            		UpdateManager.downloadAndBurnFirmware( dbw, pcb_type1, manual );
	            	}
            	}
            }
        })
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                /* User clicked No so do some stuff */
            }
        });
		BarobotMain.getInstance().runOnUiThread(new Runnable() {
			  public void run() {
				  a.create().show();
			  }
			});
	}
	public static void setNewRobotId(final Activity dbw, final BarobotConnector barobot) {
		int isOnline = Android.isOnline(dbw);
		if(isOnline > -1 ){
			Queue q = new Queue();
			barobot.readHardwareRobotId(q);			// check hardware version
			q.add( new AsyncMessage( true ) {		// when version readed
				@Override
				public String getName() {
					return "Check robot_id";
				}
				@Override
				public Queue run(Mainboard dev, Queue queue) {
					if( barobot.pcb_type == 0 && barobot.robot_id_ready){
						throw new RuntimeException("Brak pcb_type");
					}
					int robot_id = UpdateManager.getNewRobotId();		// download new robot_id (init hardware)
					Initiator.logger.w("Android.new_robot_id", "robot_id: " + robot_id);
					if( robot_id > 0 ){		// save robot_id to android and arduino
						Queue q = new Queue();
						barobot.setRobotId( q, robot_id, barobot.pcb_type );
						Android.alertMessage(dbw, "New ID= "+ robot_id + ", PCB type = " + barobot.pcb_type );
						return q;	// before all other commands currently in queue
					}
					return null;
				}
			});
			barobot.main_queue.add(q);
		}else{
			Android.alertMessage(dbw, "No internet connection");
		}
	}
}
