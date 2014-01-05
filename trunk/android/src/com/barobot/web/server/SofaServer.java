package com.barobot.web.server;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import com.barobot.hardware.Android;
import com.barobot.hardware.virtualComponents;
import com.barobot.utils.Arduino;
import com.barobot.utils.ArduinoQueue;
import com.x5.template.Chunk;
import com.x5.template.Theme;
import com.x5.template.providers.AndroidTemplates;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.ServerRunner;

public class SofaServer extends NanoHTTPD {
    private static SofaServer ins;
	private Theme theme;
	private AssetManager am;

	public SofaServer() {
    	super(8000);
    }
	public static void main(String[] args) {
        ServerRunner.run(SofaServer.class);
    }

    @Override public Response serve(IHTTPSession session) {
		String uri					= session.getUri();
		//Log.i("---otwieram--- ", uri );
		Chunk chunk					= null;
		String system_action_res	= "";
		if(uri.equals("/x10")){
			chunk					= theme.makeChunk("main#header");
			system_action_res = this.move_x_page( session, chunk);
		}else if( uri.equals("/") ){
			chunk					= theme.makeChunk("main#header");
			system_action_res		= this.default_page(session, chunk);
		}else{
			String path				= uri.substring(1);
			if(Android.assetExists(am, path)){
	//			String data =Android.readAsset(am, path);
				String etag = getEtag(path);
				int dotpos = path.lastIndexOf(".");
	        	String ext = path.substring(dotpos+1);
	        	if(MIME_TYPES.containsKey(ext)){
	        		String mime = MIME_TYPES.get(ext) + ";encoding=utf-8;charset=UTF-8";
	        		InputStream mbuffer = null;
	        		try {
						mbuffer = am.open(uri.substring(1));
						Response r =  new NanoHTTPD.Response(Status.OK, mime, mbuffer);
						r.addHeader("Cache-Control", "no-transform,public,max-age=3000,s-maxage=900");
				//		r.addHeader("Expires", "Thu, 03 Jan 2019 14:42:16 GMT");
		    	//		r.addHeader("Age", "10000");
		    	//		r.addHeader("Connection", "keep-alive");
		    	//		r.addHeader("Keep-Alive", "timeout=2, max=99");
		    	//		r.addHeader("Vary", "Accept-Encoding,User-Agent");
		    			r.addHeader("Server", "Apache/2.2.16");
		    			r.addHeader("X-Cache", "HIT");
		    			r.addHeader("Last-Modified", "Wed, 05 Dec 2012 13:23:44 GMT");
		    			Log.i("etag :", path +" " + etag );
		    			r.addHeader("Etag", "\""+ etag + "\"");	
						return r;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	    		//	Response r = new NanoHTTPD.Response(Status.OK, mime, data);
	    		//	Response r = new NanoHTTPD.Response(data);
	    		//	return r;
	        	}
			}else{
				Log.i("SofaServer nie istnieje:", path );
			}
/*
			try {
			//	Log.i("otwieram: ", path );
				java.io.InputStream in = am.open(path);
				java.util.Scanner s = new java.util.Scanner(in, "UTF-8").useDelimiter("\\A");
				int dotpos = path.lastIndexOf(".");
	        	String ext = path.substring(dotpos+1);
		        if(s.hasNext()){
		     //   	Log.i("otwieram ext :", ext );
		        	if(MIME_TYPES.containsKey(ext)){
		        		String mime = MIME_TYPES.get(ext);
		        		String data = s.next();
		      //  		Log.i("otwieram mime :", ext );
		        		
		        		/*
		        		String hashtext = DigestUtils.md5Hex(md5);
		        		byte[] md5 = Files.getDigest(data, md);
		        		

		    			Response r = new NanoHTTPD.Response(Status.OK, mime, data);
		    			r.addHeader("Expires", "Thu, 03 Jan 2019 14:42:16 GMT");
		    			r.addHeader("Age", "10000");
		    	//		r.addHeader("Keep-Alive", "timeout=2, max=99");
		    			r.addHeader("Vary", "Accept-Encoding,User-Agent");
		    			r.addHeader("Server", "Apache/2.2.16");
		    			r.addHeader("Last-Modified", "Wed, 05 Dec 2012 13:23:44 GMT");
		    			r.addHeader("Cache-Control", "public, max-age=31536000");
		    			Log.i("etag :", etag );
		    			r.addHeader("Etag", etag );
		    			
		        		return r;
		        	}
		        }else{
		     //   	Log.i("nie otwieram ext :", ext );
		        }
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			*/
			return new NanoHTTPD.Response(Status.NOT_FOUND, "", "");
		}
		chunk.set("body", system_action_res );
		chunk.set("host", "http://" + session.getHeaders().get("host") );	
		Response r = new Response(chunk.toString() );
        return r;
    }

    private String getEtag(String key) {
		/*
		String hashtext = DigestUtils.md5Hex(md5);
		byte[] md5 = Files.getDigest(data, md);
		*/
		MessageDigest messageDigest;
		try {
			messageDigest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
		byte[] dig = messageDigest.digest(key.getBytes());
		return new String(bytArrayToHex(dig));
	}
	private String move_x_page(IHTTPSession session, Chunk chunk) {
		Arduino ar		= Arduino.getInstance();
		ArduinoQueue q	= new ArduinoQueue();
		int posx		= virtualComponents.getInt("POSX", 0 );
		virtualComponents.moveZDown( q );
		virtualComponents.moveX( q, ( posx +1000));
		ar.send(q);
		return default_page(session, chunk);
	}

    String bytArrayToHex(byte[] a) {
    	   StringBuilder sb = new StringBuilder();
    	   for(byte b: a)
    	      sb.append(String.format("%02x", b&0xff));
    	   return sb.toString();
    	}
    
	private String default_page(IHTTPSession session, Chunk chunk) {
		Chunk action_chunk			= theme.makeChunk("main#body");
		Map<String, List<String>> decodedQueryParameters =decodeParameters(session.getQueryParameterString());

    	  StringBuilder sb = new StringBuilder(); 
          sb.append("<p><blockquote><b>URI</b> = ").append(
              String.valueOf(session.getUri())).append("<br />");

          sb.append("<b>Method</b> = ").append(
              String.valueOf(session.getMethod())).append("</blockquote></p>");

          sb.append("<h3>Headers</h3><p><blockquote>").
              append(toString(session.getHeaders())).append("</blockquote></p>");

          sb.append("<h3>Parms</h3><p><blockquote>").
              append(toString(session.getParms())).append("</blockquote></p>");

          sb.append("<h3>Parms (multi values?)</h3><p><blockquote>").
              append(toString(decodedQueryParameters)).append("</blockquote></p>");

          try {
              Map<String, String> files = new HashMap<String, String>();
              session.parseBody(files);
              sb.append("<h3>Files</h3><p><blockquote>").
                  append(toString(files)).append("</blockquote></p>");
          } catch (Exception e) {
              e.printStackTrace();
          }
        action_chunk.set("body2", sb.toString() );
    	return action_chunk.toString();
    }

    private String toString(Map<String, ? extends Object> map) {
        if (map.size() == 0) {
            return "";
        }
        return unsortedList(map);
    }

    private String unsortedList(Map<String, ? extends Object> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("<ul>");
        for (Map.Entry entry : map.entrySet()) {
            listItem(sb, entry);
        }
        sb.append("</ul>");
        return sb.toString();
    }

    private void listItem(StringBuilder sb, Map.Entry entry) {
        sb.append("<li><code><b>").append(entry.getKey()).
            append("</b> = ").append(entry.getValue()).append("</code></li>");
	}
 
    private static final Map<String, String> MIME_TYPES = new HashMap<String, String>() {
		private static final long serialVersionUID = -5637166427995938617L;
	{
        put("css", "text/css");
        put("htm", "text/html");
        put("html", "text/html");
        put("xml", "text/xml");
        put("java", "text/x-java-source, text/java");
        put("md", "text/plain");
        put("txt", "text/plain");
        put("asc", "text/plain");
        put("gif", "image/gif");
        put("jpg", "image/jpeg");
        put("jpeg", "image/jpeg");
        put("png", "image/png");
        put("mp3", "audio/mpeg");
        put("ico", "image/x-icon");
        put("m3u", "audio/mpeg-url");
        put("mp4", "video/mp4");
        put("ogv", "video/ogg");
        put("flv", "video/x-flv");
        put("mov", "video/quicktime");
        put("swf", "application/x-shockwave-flash");
        put("js", "text/javascript");
        put("pdf", "application/pdf");
        put("doc", "application/msword");
        put("ogg", "application/x-ogg");
        put("zip", "application/octet-stream");
        put("exe", "application/octet-stream");
        put("class", "application/octet-stream");
    }};

	public static SofaServer getInstance() {
		// TODO Auto-generated method stub
		if( ins == null){
			ins = new SofaServer();
		}
		return ins;
	}
	public void setBaseContext(Context baseContext) {
        AndroidTemplates loader = new AndroidTemplates(baseContext);
        am = baseContext.getAssets();
		theme = new Theme(loader);
	}
}
/*
FileInputStream fis = null;
try {
    fis = new FileInputStream(Environment.getExternalStorageDirectory()+ "/music/musicfile.mp3");
    return new NanoHTTPD.Response(Status.OK, "audio/mpeg", fis);
} catch (FileNotFoundException e) {
    e.printStackTrace();
}*/