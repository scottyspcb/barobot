package com.barobot_graph;

import java.util.Timer;
import java.util.TimerTask;

import android.os.Handler;

public class interval {
	private TimerTask scanTask;
	private static Timer t = new Timer();
	private Runnable rrr = null;

	public interval(Runnable r) {
		this.rrr = r;
	}

	public void run(long zaile) {
		run(zaile, 0);
	}

	public void run(long zaile, long coile) {
		final Handler handler = new Handler();
		scanTask = new TimerTask() {
			public void run() {
				handler.post(rrr);
			}
		};
		if (coile > 0) {
			t.schedule(scanTask, zaile, coile); // task, za ile pierwsze, co ile
												// następne
		} else {
			t.schedule(scanTask, zaile); // task, za ile pierwsze
		}
	}

	public void cancel() {
		scanTask.cancel();
	}

	public void pause() {
	}
}
