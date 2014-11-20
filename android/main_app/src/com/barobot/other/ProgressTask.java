package com.barobot.other;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import com.barobot.R;

public class ProgressTask extends AsyncTask<String, Void, Object> {
	private ProgressDialog pd = null;
	private UiTask r	= null;
	public ProgressTask(Activity bottleSetupActivity, UiTask runnable){
		String title	= bottleSetupActivity.getResources().getString(R.string.reload_drinks_progress_title);
		String msg		= bottleSetupActivity.getResources().getString(R.string.reload_drinks_progress_message);
		pd				= ProgressDialog.show(bottleSetupActivity, title, msg, true, false);
		r				= runnable; 
	}
    protected Object doInBackground(String... args) {	
    	r.compute();
        return "replace this with your data object";
    }
    protected void onPostExecute(Object result) {
        if (this.pd != null) {
        	this.pd.dismiss();
        }
        r.close();
    }
	public static abstract class UiTask{
		public abstract void compute();
		public abstract void close();
	} 
}
