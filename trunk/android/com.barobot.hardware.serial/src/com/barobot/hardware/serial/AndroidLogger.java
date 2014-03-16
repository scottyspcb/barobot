package com.barobot.hardware.serial;

import com.barobot.common.interfaces.CanLog;

import android.util.Log;

public class AndroidLogger implements CanLog {

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
	}

	@Override
	public void e(String tag, String msg) {
		Log.e( tag, msg);
	}

	@Override
	public void e(String tag, String msg, Throwable tr) {
		Log.e( tag, msg, tr);
	}

	@Override
	public void i(String tag, String msg) {
		Log.i( tag, msg);
	}

	@Override
	public void i(String tag, String msg, Throwable tr) {
		Log.i( tag, msg, tr);
	}

	@Override
	public void v(String tag, String msg) {
		Log.v( tag, msg);
	}

	@Override
	public void v(String tag, String msg, Throwable tr) {
		Log.v( tag, msg, tr);
	}

	@Override
	public void w(String tag, Throwable tr) {
		Log.w( tag, tr);
	}

	@Override
	public void w(String tag, String msg, Throwable tr) {
		Log.w( tag, msg, tr);
	}

	@Override
	public void w(String tag, String msg) {
		Log.w( tag, msg);
	}
}
