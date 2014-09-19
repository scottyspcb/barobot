package com.barobot.other;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.orman.mapper.Model;
import org.orman.mapper.ModelQuery;
import org.orman.sql.C;

import android.os.Environment;

import com.barobot.common.Initiator;
import com.barobot.common.interfaces.HardwareState;
import com.barobot.gui.database.BarobotData;
import com.barobot.hardware.devices.BarobotConnector;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
 

public class update_drinks {
//	private static String metadata	= "http://barobot.com/android_data/database.json";
	private static String drinks	= "http://barobot.com/android_data/drinks.json";	
//	private static String errorlog	= "http://barobot.com/android_data/error.php";
	private static String upload	= "http://barobot.com/android_data/store.php";
	public static String fulldb		= "http://barobot.com/android_data/" + BarobotData.DATABASE_NAME;

	public static String firmware		= "http://barobot.com/android_data/barobot.hex";
	public static String localDbPath	= "/data/data/com.barobot/databases/" + BarobotData.DATABASE_NAME;
	public static String sourcepath		= "/storage/emulated/0/download/" + BarobotData.DATABASE_NAME;
	public static String copyPath		= "/Barobot/" + BarobotData.DATABASE_NAME;
	public static String logFile		= "/Barobot/log.log";
	public static String backupPath		= "/Barobot/BarobotOrman%DATE%.db";

	public void load(){
	
	  	  File dir = new File(Environment.getExternalStorageDirectory(), "Barobot");
	  	  if (!dir.exists()) {
	  		  Android.createDirIfNotExists("Barobot");
	  	  }
	  	  String path = 	dir.getAbsolutePath()+"/"+"drinks.json";
	  	  
		InternetHelpers.doDownload(drinks, path, new OnDownloadReadyRunnable() {
			private String source;
			public void sendSource( String source ) {
				this.source = source;
				Initiator.logger.i("sendSource", source);
			}
		    @Override
			public void run() {
		    	InternetHelpers.parseJson( this.source );
			}
		});
	}

	public void sendAllLogs( BarobotConnector barobot ){
		boolean success = false;
		List<com.barobot.gui.dataobjects.Log> logs = 
				Model.fetchQuery(ModelQuery.select()
						.from(com.barobot.gui.dataobjects.Log.class)
						.where(C.eq("send_time", 0))
						.orderBy("Log.time")
						.limit(100)
						.getQuery(),com.barobot.gui.dataobjects.Log.class);

		JsonArray jsonArray = new JsonArray().add( "John" ).add( 23 );
		for(com.barobot.gui.dataobjects.Log log : logs)
		{
			JsonObject c = log.getJson();
			jsonArray.add(c);
		}
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		String data				= jsonArray.toString();
		try {
			builder.addPart("log", new StringBody( data ));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return;
		}
		success = prepare_connection(barobot,upload, builder, true);

		if(success == true){
			for(com.barobot.gui.dataobjects.Log log : logs)
			{
				String c = log.content;
				log.send_time  = 1;
				log.update();
			}
		}
	}

	public void upload_drinks( BarobotConnector barobot ){
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		File file				= new File(localDbPath);
		builder.addPart("myFile", new FileBody(file));
		prepare_connection(barobot,upload, builder, true);
	}

	public static boolean prepare_connection( BarobotConnector barobot, String address, MultipartEntityBuilder builder, boolean addLogin ){
		HttpClient httpClient	= new DefaultHttpClient();
		HttpPost httpPost		= new HttpPost(address);	
		builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

		if(addLogin){
			Charset chars			= Charset.forName("UTF-8");
			HardwareState state		= barobot.state;
			try {
				builder.addPart("who", new StringBody( state.get( "MAIN_LOGIN","anonymous"), chars));
				builder.addPart("who2", new StringBody( state.get( "MAIN_PASSWORD","anonymous"), chars));
			} catch (UnsupportedEncodingException e) {
				Initiator.logger.w("upload_drinks UnsupportedEncodingException", e);
			}
		}
		HttpEntity entity = builder.build();
		httpPost.setEntity(entity);
		try {
			HttpResponse response = httpClient.execute(httpPost);
			HttpEntity resEntity = response.getEntity();
			if (resEntity == null) { 
				return false;
			}else{
			    String responseStr = EntityUtils.toString(resEntity).trim();
			    Initiator.logger.i("upload_drinks result: ", responseStr);
			}
		} catch (ClientProtocolException e) {
			Initiator.logger.w("prepare_connection ClientProtocolException", e);
			return false;
		} catch (IOException e) {
			Initiator.logger.w("prepare_connection IOException", e);
			return false;
		}
		return true;
	}
}
