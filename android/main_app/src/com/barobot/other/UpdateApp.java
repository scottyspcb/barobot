package com.barobot.other;

// not used

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.barobot.common.Initiator;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;

public class UpdateApp extends AsyncTask<String,Void,Void>{
	private Context context;
	private File outputFile; 
	public UpdateApp(Context contextf,String destPath ){
	    context		= contextf;
		outputFile	= new File(Environment.getExternalStorageDirectory() + destPath );
        if(outputFile.exists()){
            outputFile.delete();
        }
	}	

	@Override
	protected Void doInBackground(String... arg0) {
	      try {
			URL url = new URL(arg0[0]);  
			Initiator.logger.e("UpdateApp.doInBackground", "start: "+ outputFile+ " | " + url.toString());

			HttpURLConnection c		= (HttpURLConnection) url.openConnection();
			c.setRequestMethod("GET");
			c.setDoOutput(true);
			c.connect();

			FileOutputStream fos	= new FileOutputStream(outputFile);
			InputStream is			= c.getInputStream();
			byte[] buffer			= new byte[1024];
			int len1 = 0;
			while ((len1 = is.read(buffer)) != -1) {
			    fos.write(buffer, 0, len1);
			}
			fos.close();
			is.close();
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile( outputFile ), "application/vnd.android.package-archive");
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // without this flag android returned a intent error!
			context.startActivity(intent);
	        } catch (Exception e) {
	            Initiator.logger.e("UpdateAPP", "Update error! " + e.getMessage(), e);
	        }
	    return null;
	} 
}
