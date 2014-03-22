package com.barobot.other;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import com.barobot.R;
import com.barobot.activity.UpdateActivity;
import com.barobot.common.Initiator;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonObject.Member;
import com.eclipsesource.json.JsonValue;

import android.util.Log;

public class update_drinks {
	private String metadata	= "http://strych.arczi.info/barobot/database.json";
	private String drinks	= "http://strych.arczi.info/barobot/drinks.json";	
	private String errorlog	= "http://strych.arczi.info/barobot/error.php";
	private UpdateActivity updateActivity;

	private interface Runnable2 extends Runnable{
		@Override
		public void run();
		public void sendSource( String source );
	}

	public void load(){
		doDownload(drinks, "drinks.json", new Runnable2() {
			private String source;
			public void sendSource( String source ) {
				this.source = source;
				Initiator.logger.i("sendSource", source);
			}
		    @Override
			public void run() {
		    	parseJson( this.source );
			}
		});
	}
	protected void parseJson(String source) {
		updateActivity.setText(	R.id.update_message, source);		
		JsonObject jsonObject = JsonObject.readFrom( source );
		this.parseJsonObject(jsonObject, 0 );
	}
	protected void parseJsonObject(JsonObject in, int level ) {
		Initiator.logger.i("Json " +level + " IN", "json Object: "+in);	

		for( Member member : in ) {
			String name		= member.getName();
			JsonValue value = member.getValue();
			if(value.isObject()){
				Initiator.logger.i("Json " + (level+1), "isObject: "+name);
				this.parseJsonObject(value.asObject(), level+1);

			}else if(value.isArray()){
				JsonArray jsa =  value.asArray();
				for( JsonValue amember : jsa ) {
					if(amember.isObject()){
						Initiator.logger.i("Json " + (level +1), "isObject: "+name);
						this.parseJsonObject(amember.asObject(), level+1);
					}else if(value.isNumber()){
						Initiator.logger.i("Json "+level, "number: "+name +"/"+amember);
					}else if(value.isString()){
						Initiator.logger.i("Json "+level, "string: "+name +"/"+amember);
					}else{
						Initiator.logger.i("Json "+level, "none: "+name +"/"+amember);
					}
				}
				Initiator.logger.i("Json "+level, "Array: "+name +"/"+jsa);
			}else if(value.isString()){
				Initiator.logger.i("Json "+level, "string: "+name +"/"+value);	
			}else if(value.isNumber()){
				Initiator.logger.i("Json "+level, "int: "+name +"/"+value);
			}else{
				Initiator.logger.i("Json "+level, "none: "+name +"/"+value);	
			}
		}
	}
	
	private void doDownload(final String urlLink, final String fileName, final Runnable2 runnable) {
		Thread dx = new Thread() {
            public void run() {
        	  File root = android.os.Environment.getExternalStorageDirectory();    

        	  Log.i("FILE_NAME", "root getAbsolutePath is "+root.getAbsolutePath());
        	  
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
                    InputStream input	= new BufferedInputStream(url.openStream());
                    OutputStream output	= new FileOutputStream(dir+"/"+fileName);
                    byte data[]			= new byte[1024];
                    String strFileContents="";
                    int total			= 0;
                    int count;
                    while ((count = input.read(data)) != -1) {
                        total += count;
                        output.write(data, 0, count);
                        publishProgress((int) (total * 100 / fileLength));
                        strFileContents = strFileContents + (new String(data, 0, count));
                    }
                    output.flush();
                    output.close();
                    input.close();
                    runnable.sendSource(strFileContents);
            		runnable.run();
             	} catch (FileNotFoundException e) {
             		 e.printStackTrace();
                } catch (Exception e) {
                	 e.printStackTrace();
                	 Log.i("ERROR ON DOWNLOADING FILES", "ERROR IS" +e);
                }
            }
        };
        dx.start();    
	}
	
	protected void doDownload(final String urlLink, final String fileName) {
		// instantiate it within the onCreate method
		/*
		final ProgressDialog mProgressDialog;
		mProgressDialog = new ProgressDialog(this.updateActivity);

		this.updateActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mProgressDialog.setMessage("A message");
				mProgressDialog.setIndeterminate(false);
				mProgressDialog.setMax(100);
				mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			}
		});
*/
        Thread dx = new Thread() {
            public void run() {
        	  File root = android.os.Environment.getExternalStorageDirectory();    

        	  Log.i("FILE_NAME", "root getAbsolutePath is "+root.getAbsolutePath());
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

	public void setActivity(UpdateActivity updateActivity) {
		this.updateActivity = updateActivity;
	}
	public void stop() {
	}
}

