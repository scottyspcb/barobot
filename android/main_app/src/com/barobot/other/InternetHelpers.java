package com.barobot.other;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.barobot.common.Initiator;
import com.barobot.common.constant.Constant;
import com.barobot.common.interfaces.OnDownloadReadyRunnable;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;
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
				//  File root = android.os.Environment.getExternalStorageDirectory();    
				//  Initiator.logger.i("FILE_NAME", "root getAbsolutePath is "+root.getAbsolutePath());
				try {
					URL url = new URL(urlLink);
					Initiator.logger.i("FILE_NAME", "Path is "+path);
					Initiator.logger.i("FILE_URLLINK", "File URL is "+url);
					URLConnection connection = url.openConnection();
					connection.connect();
					// this will be useful so that you can show a typical 0-100% progress bar
					int fileLength = connection.getContentLength();

					// download the file
					File dest =  new File(path);
					if(dest.exists()){				// rename old file NNN.EEE to NNN-date.EEE
						Date dNow				= new Date( );
						SimpleDateFormat dd		= new SimpleDateFormat ("yyyy.MM.dd.hh.mm.ss");
						String fileName 		= dest.getName().replaceFirst("[.][^.]+$", "");
						String extension		= "";
						int i = fileName.lastIndexOf('.');
						if (i > 0) {
						    extension = fileName.substring(i+1);
						}
						File newFile			= new File(dest.getParent(), fileName+"-"+ dd.format(dNow)+"." + extension);
						dest.renameTo(newFile);
					}

					InputStream input	= new BufferedInputStream(url.openStream());
					OutputStream output	= new FileOutputStream( path);
					byte data[]			= new byte[4096];
					int total			= 0;
					int count;
					while ((count = input.read(data)) != -1) {
						total += count;
						output.write(data, 0, count);
						if(fileLength!=0){
							runnable.sendProgress( (int) (total * 100 / fileLength) );
						}
					}
					output.flush();
					output.close();
					input.close();
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
	/*
	public static void doDownload(final String urlLink, final String fileName) {
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
			//		int fileLength = connection.getContentLength();

					// download the file
					InputStream input = new BufferedInputStream(url.openStream());
					OutputStream output = new FileOutputStream(dir+"/"+fileName);

					byte data[] = new byte[4096];
			//		long total = 0;
					int count;
					while ((count = input.read(data)) != -1) {
			//			total += count;
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
	}*/


	public static String response2String(HttpResponse response) {
		HttpEntity httpEntity = response.getEntity();
		try {
			InputStream is = httpEntity.getContent();
			BufferedReader reader;
			reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
			StringBuilder sb = new StringBuilder();

			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			return sb.toString().trim();
		} catch (UnsupportedEncodingException e) {
			Initiator.logger.e("update_drinks.response2String", "UnsupportedEncodingException", e );
		} catch (IllegalStateException e) {
			Initiator.logger.e("update_drinks.response2String", "IllegalStateException", e );
		} catch (IOException e) {
			Initiator.logger.e("update_drinks.response2String", "IOException", e );
		}
		return null;
	}

	public static void downloadNewestVersionNumber( final OnDownloadReadyRunnable runnable) {
		Thread dx = new Thread() {
			public void run() {
				HttpClient httpclient = new DefaultHttpClient();

				HttpPost httppost = new HttpPost(Constant.version_index);
				httppost.setHeader("User-Agent", "IE 10 Mozilla compatible");
				try {
					BarobotConnector barobot = Arduino.getInstance().barobot;

					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
					nameValuePairs.add(new BasicNameValuePair("app_version", ""+Constant.ANDROID_APP_VERSION ));
					nameValuePairs.add(new BasicNameValuePair("firmware_version", barobot.state.get("ARDUINO_VERSION", "0") ));
					nameValuePairs.add(new BasicNameValuePair("robot_id", ""+barobot.getRobotId() ));
					nameValuePairs.add(new BasicNameValuePair("stat1", barobot.state.get("STAT1", "0") ));
					nameValuePairs.add(new BasicNameValuePair("stat2", barobot.state.get("STAT2", "0") ));
					nameValuePairs.add(new BasicNameValuePair("lang", barobot.state.get("LANG", "pl" ) ));
					nameValuePairs.add(new BasicNameValuePair("starts", barobot.state.get("ARDUINO_STARTS", "0") ));
					nameValuePairs.add(new BasicNameValuePair("temp", ""+barobot.getLastTemp() ));
					nameValuePairs.add(new BasicNameValuePair("ip", ""+Android.getLocalIpAddress() ));

					httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
					HttpResponse response = httpclient.execute(httppost);	        // Execute HTTP Post Request
					String ret = InternetHelpers.response2String(response);
					runnable.sendSource(ret);	
					runnable.sendProgress( 100 );
					runnable.run(); 
				} catch (ClientProtocolException e) {
					Initiator.logger.e("update_drinks.downloadNewestVersionNumber", "ClientProtocolException", e );
				} catch (IOException e) {
					Initiator.logger.e("update_drinks.downloadNewestVersionNumber", "IOException", e );
				}
			}
		};
		dx.start();    
	}

	static String downloadRobotId() {
		HttpClient httpclient	= new DefaultHttpClient();
		HttpPost httppost		= new HttpPost(Constant.robot_id_manager);
		String ip				= Android.getLocalIpAddress();
		httppost.setHeader("User-Agent", "IE 10 Mozilla compatible");
		BarobotConnector barobot = Arduino.getInstance().barobot;
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair("address", ip ));
		nameValuePairs.add(new BasicNameValuePair("stat1", barobot.state.get("STAT1", "0") ));
		nameValuePairs.add(new BasicNameValuePair("stat2", barobot.state.get("STAT2", "0") ));

		try {
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = httpclient.execute(httppost);	        // Execute HTTP Post Request
			return InternetHelpers.response2String(response);
		} catch (ClientProtocolException e) {
			Initiator.logger.e("update_drinks.downloadRobotId", "ClientProtocolException", e );
		} catch (IOException e) {
			Initiator.logger.e("update_drinks.downloadRobotId", "IOException", e );
		}
		return null;
	}

	public static void raportError(Exception lastException, final String msg) {
		Thread dx = new Thread() {
			public void run() {
			HttpClient httpclient	= new DefaultHttpClient();
			HttpPost httppost		= new HttpPost(Constant.raport_manager);
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			String ip				= Android.getLocalIpAddress();
			httppost.setHeader("User-Agent", "IE 10 Mozilla compatible");
			if(ip!=null){
				nameValuePairs.add(new BasicNameValuePair("address", ip ));
			}
			if( Arduino.getInstance() != null  ){
				BarobotConnector barobot = Arduino.getInstance().barobot;
				if(barobot != null){
					nameValuePairs.add(new BasicNameValuePair("app_version", ""+Constant.ANDROID_APP_VERSION ));
					nameValuePairs.add(new BasicNameValuePair("firmware_version", barobot.state.get("ARDUINO_VERSION", "0") ));
					nameValuePairs.add(new BasicNameValuePair("robot_id", ""+barobot.getRobotId() ));
					if( barobot.state != null){
						nameValuePairs.add(new BasicNameValuePair("stat1", barobot.state.get("STAT1", "0") ));
						nameValuePairs.add(new BasicNameValuePair("stat2", barobot.state.get("STAT2", "0") ));
						nameValuePairs.add(new BasicNameValuePair("lang", barobot.state.get("LANG", "pl" ) ));
						nameValuePairs.add(new BasicNameValuePair("starts", barobot.state.get("ARDUINO_STARTS", "0") ));
						nameValuePairs.add(new BasicNameValuePair("temp", ""+barobot.getLastTemp() ));
					}
				}
			}
			nameValuePairs.add(new BasicNameValuePair("error", msg ));
			try {
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = httpclient.execute(httppost);	        // Execute HTTP Post Request
				InternetHelpers.response2String(response);
			} catch (ClientProtocolException e) {
				Initiator.logger.e("InternetHelpers.raportError", "ClientProtocolException", e );
			} catch (IOException e) {
				Initiator.logger.e("InternetHelpers.raportError", "IOException", e );
			}	
		}};
		dx.start();
	}
}
