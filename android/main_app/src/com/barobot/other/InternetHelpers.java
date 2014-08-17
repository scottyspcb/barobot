package com.barobot.other;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import android.os.Environment;

import com.barobot.common.Initiator;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.eclipsesource.json.JsonObject.Member;

public class InternetHelpers {

	public static void parseJson(String source) {	
		JsonObject jsonObject = JsonObject.readFrom( source );
		parseJsonObject(jsonObject, 0 );
	}
	public static void parseJsonObject(JsonObject in, int level ) {
		Initiator.logger.i("Json " +level + " IN", "json Object: "+in);	

		for( Member member : in ) {
			String name		= member.getName();
			JsonValue value = member.getValue();
			if(value.isObject()){
				Initiator.logger.i("Json " + (level+1), "isObject: "+name);
				parseJsonObject(value.asObject(), level+1);

			}else if(value.isArray()){
				JsonArray jsa =  value.asArray();
				for( JsonValue amember : jsa ) {
					if(amember.isObject()){
						Initiator.logger.i("Json " + (level +1), "isObject: "+name);
						parseJsonObject(amember.asObject(), level+1);
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
	
	

	public static void doDownload(final String urlLink, final String path, final OnDownloadReadyRunnable runnable) {
		Thread dx = new Thread() {
            public void run() {
        	  File root = android.os.Environment.getExternalStorageDirectory();    

        	  Initiator.logger.i("FILE_NAME", "root getAbsolutePath is "+root.getAbsolutePath());

              try {
                    URL url = new URL(urlLink);
                    Initiator.logger.i("FILE_NAME", "Path is "+path);
                    Initiator.logger.i("FILE_URLLINK", "File URL is "+url);
                    URLConnection connection = url.openConnection();
                    connection.connect();
                    // this will be useful so that you can show a typical 0-100% progress bar
        //            int fileLength = connection.getContentLength();

                    // download the file
                    InputStream input	= new BufferedInputStream(url.openStream());
                    OutputStream output	= new FileOutputStream( path);
                    byte data[]			= new byte[1024];
                    String strFileContents="";
                    int total			= 0;
                    int count;
                    while ((count = input.read(data)) != -1) {
                        total += count;
                        output.write(data, 0, count);
            //            publishProgress((int) (total * 100 / fileLength));
                        strFileContents = strFileContents + (new String(data, 0, count));
                    }
                    output.flush();
                    output.close();
                    input.close();
                    runnable.sendSource(strFileContents);
            		runnable.run();
	
             	} catch (FileNotFoundException e) {
             		 Initiator.logger.appendError(e);
                } catch (Exception e) {
                	 Initiator.logger.appendError(e);
                	 Initiator.logger.i("ERROR ON DOWNLOADING FILES", "ERROR IS" +e);
                }
            }
        };
        dx.start();    
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
	public static void doDownload(final String urlLink, final String fileName) {
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

        	  Initiator.logger.i("FILE_NAME", "root getAbsolutePath is "+root.getAbsolutePath());
              File dir = new File (root.getAbsolutePath() + "/Content2/"); 
              if(dir.exists()==false) {
                      dir.mkdirs();
                 }
              try {
                    URL url = new URL(urlLink);
                    Initiator.logger.i("FILE_NAME", "File name is "+fileName);
                    Initiator.logger.i("FILE_URLLINK", "File URL is "+url);
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
            //            publishProgress((int) (total * 100 / fileLength));
                    }
                    output.flush();
                    output.close();
                    input.close();
                } catch (Exception e) {
                	 Initiator.logger.appendError(e);
                	 Initiator.logger.i("ERROR ON DOWNLOADING FILES", "ERROR IS" +e);
                }
            }
        };
        dx.start();      
    }
	
	
}
