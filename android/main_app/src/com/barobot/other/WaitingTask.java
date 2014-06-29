package com.barobot.other;

import com.barobot.BarobotMain;
import com.barobot.common.Initiator;

import android.app.ProgressDialog;
import android.os.AsyncTask;

public class WaitingTask extends AsyncTask<Void, Void, Integer> {
	private ProgressDialog Dialog = new ProgressDialog(BarobotMain.getInstance());
	private boolean ready = false;

    @Override
    protected void onPreExecute()
    {
        Dialog.setMessage("Preparing drink...");
        Dialog.show();
    }

    @Override
    protected Integer doInBackground(Void... params) 
    {
    	while(!this.ready){
    		try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				Initiator.logger.appendError(e);
			}
    	}
        return 0;
    }

    @Override
    protected void onPostExecute(Integer result)
    {
        Dialog.dismiss();
    }
    public void setReady(){
    	this.ready = true;
    }
}
