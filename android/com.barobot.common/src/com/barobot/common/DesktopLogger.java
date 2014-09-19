package com.barobot.common;

import com.barobot.common.interfaces.CanLog;

public class DesktopLogger implements CanLog {

	@Override
	public void println(int priority, String tag, String msg) {
		System.out.println( tag +" / "+ msg );
	}

	@Override
	public void d(String tag, String msg) {
		System.out.println( tag +" / "+ msg );
	}

	@Override
	public void d(String tag, String msg, Throwable tr) {
		System.out.println( tag +" / "+ msg );
		tr.printStackTrace();
	}

	@Override
	public void e(String tag, String msg) {
		System.out.println( tag +" / "+ msg );
	}

	@Override
	public void e(String tag, String msg, Throwable tr) {
		System.out.println( tag +" / "+ msg );
		tr.printStackTrace();
	}

	@Override
	public void i(String tag, String msg) {
		System.out.println( tag +" / "+ msg );
	}

	@Override
	public void i(String tag, String msg, Throwable tr) {
		System.out.println( tag +" / "+ msg );
		tr.printStackTrace();
	}

	@Override
	public void v(String tag, String msg) {
		System.out.println( tag +" / "+ msg );
	}

	@Override
	public void v(String tag, String msg, Throwable tr) {
		System.out.println( tag +" / "+ msg );
		tr.printStackTrace();
	}

	@Override
	public void w(String tag, Throwable tr) {
		System.out.println( tag  );
		tr.printStackTrace();
	}

	@Override
	public void w(String tag, String msg, Throwable tr) {
		System.out.println( tag  );
		tr.printStackTrace();
	}

	@Override
	public void w(String tag, String msg) {
		System.out.println( tag +" / "+ msg );
	}

	@Override
	public void appendError(Throwable tr) {
		tr.printStackTrace();
	}

	@Override
	public void saveLog(String text) {
		System.out.println( text );
	}
}
