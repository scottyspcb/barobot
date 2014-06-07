package com.barobot.hardware.serial;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;

import com.barobot.common.interfaces.CanLog;

import android.os.Environment;
import android.util.Log;

public class AndroidLogger implements CanLog {
	File myFile;
	FileOutputStream fos;
	
	public AndroidLogger() {
		File external = Environment.getExternalStorageDirectory();
		String sdcardPath = external.getPath();
		File dir = new File(sdcardPath + "/barobot");
		dir.mkdirs();
        myFile = new File(sdcardPath + "/barobot/barobot.error.txt");
        //     myFile.createNewFile();
        try {
			fos = new FileOutputStream(myFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
	}

	@Override
	public void println(int priority, String tag, String msg) {
		Log.println(priority, tag, msg);
	}

	@Override
	public void d(String tag, String msg) {
		Log.d( tag, msg);
	}

	@Override
	public void d(String tag, String msg, Throwable tr) {
		Log.d( tag, msg, tr);
		appendError( tr );
	}

	@Override
	public void e(String tag, String msg) {
		Log.e( tag, msg);
	}

	@Override
	public void e(String tag, String msg, Throwable tr) {
		Log.e( tag, msg, tr);
		appendError( tr );
	}

	@Override
	public void i(String tag, String msg) {
		Log.i( tag, msg);
	}

	@Override
	public void i(String tag, String msg, Throwable tr) {
		Log.i( tag, msg, tr);
		appendError( tr );
	}

	@Override
	public void v(String tag, String msg) {
		Log.v( tag, msg);
	}

	@Override
	public void v(String tag, String msg, Throwable tr) {
		Log.v( tag, msg, tr);
		appendError( tr );
	}

	@Override
	public void w(String tag, Throwable tr) {
		Log.w( tag, tr);
		appendError( tr );
	}

	@Override
	public void w(String tag, String msg, Throwable tr) {
		Log.w( tag, msg, tr);
		appendError( tr );
	}

	@Override
	public void w(String tag, String msg) {
		Log.w( tag, msg);
	}

	@Override
	public void appendError(Throwable tr) {
		tr.printStackTrace();
		PrintStream ps = new PrintStream(fos);  
		try {
            tr.printStackTrace( ps );
        } catch (Exception e) {
        } finally {
        	 ps.close();
        }
	}

	public void appendError( String s ) {
		try {
            FileOutputStream fOut = new FileOutputStream(myFile);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(s);
            myOutWriter.close();
            fOut.close();
        } catch (Exception e) {
        }
	}
}
