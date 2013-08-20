package com.barobot;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonObject.Member;
import com.eclipsesource.json.JsonValue;

import android.app.ProgressDialog;
import android.util.Log;
import android.widget.TextView;

public class drinks_database {
	private String metadata	= "http://arczi.info/barobot/database.json";
	private String address	= "http://arczi.info/barobot/drinks.json";	
	private String errorlog	= "http://arczi.info/barobot/error.php";

	private interface Runnable2 extends Runnable{
		@Override
		public void run();
		public void sendSource( String source );
	}

	public void load(){
		doDownload(metadata, "database.json");
		doDownload(address, "drinks.json");
	
		doDownload(address, "drinks.json", new Runnable2() {
			private String source;
			public void sendSource( String source ) {
				this.source = source;
			}
		    @Override
			public void run() {
		    	parseJson( this.source );
			}
		});
	}
	protected void parseJson(String source) {
		JsonObject jsonObject = JsonObject.readFrom( source );
		
		for( Member member : jsonObject ) {
			String name		= member.getName();
			JsonValue value = member.getValue();
			Constant.log("parseJson", "json: "+name +"/"+value);
		}
	}
	private void doDownload(String address2, String string, Runnable2 runnable) {
		runnable.sendSource("");
		runnable.run();
		
	}
	protected void doDownload(final String urlLink, final String fileName) {
		// instantiate it within the onCreate method
		final ProgressDialog mProgressDialog;
		mProgressDialog = new ProgressDialog(BarobotMain.getInstance());
		BarobotMain.getInstance().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mProgressDialog.setMessage("A message");
				mProgressDialog.setIndeterminate(false);
				mProgressDialog.setMax(100);
				mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			}
		});

        Thread dx = new Thread() {
            public void run() {
        	  File root = android.os.Environment.getExternalStorageDirectory();               
              File dir = new File (root.getAbsolutePath() + "/Content2/"); 
              if(dir.exists()==false) {
                      dir.mkdirs();
                 }
             try {
                    URL url = new URL(urlLink);
                    Log.i("FILE_NAME", "File name is "+fileName);
                    Log.i("FILE_URLLINK", "File URL is "+url);
                    URLConnection connection = url.openConnection();
                    connection.connect();
                    // this will be useful so that you can show a typical 0-100% progress bar
                    int fileLength = connection.getContentLength();

                    // download the file
                    InputStream input = new BufferedInputStream(url.openStream());
                    OutputStream output = new FileOutputStream(dir+"/"+fileName);

                    byte data[] = new byte[1024];
                    long total = 0;
                    int count;
                    while ((count = input.read(data)) != -1) {
                        total += count;
                        output.write(data, 0, count);
                        publishProgress((int) (total * 100 / fileLength));
                    }
                    output.flush();
                    output.close();
                    input.close();
                } catch (Exception e) {
                	 e.printStackTrace();
                	 Log.i("ERROR ON DOWNLOADING FILES", "ERROR IS" +e);
                }
            }
        };
        dx.start();      
    }
	protected void publishProgress(int i) {
		
		
	}
}

