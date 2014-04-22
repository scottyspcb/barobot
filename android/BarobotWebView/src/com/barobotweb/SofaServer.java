package com.barobotweb;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
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
	public static InputStream assetExists(AssetManager am, String path) {
	    try {
	        InputStream stream = am.open(path);
		    return stream;
	    } catch (FileNotFoundException e) {
	    	return null;
	    } catch (IOException e) {
	    	return null;
	    }
	}
    @Override public Response serve(IHTTPSession session) {
		String uri					= session.getUri();
//		Log.i("---otwieram--- ", uri );
		EmptyRoute route = doRoutes( uri );
		if( route == null ){
			String path				= uri.substring(1);
			InputStream mbuffer		= assetExists(am, path);
			if( mbuffer != null ){
	//			String data =Android.readAsset(am, path);
				String etag = getEtag(path);
				int dotpos = path.lastIndexOf(".");
	        	String ext = path.substring(dotpos+1);
	        	if(MIME_TYPES.containsKey(ext)){
	        		String mime = MIME_TYPES.get(ext) + ";encoding=utf-8;charset=UTF-8";
	        		try {
						mbuffer		= am.open(uri.substring(1));
						Response r	= new NanoHTTPD.Response(Status.OK, mime, mbuffer);
	//					r.addHeader("Cache-Control", "no-transform,public,max-age=3000,s-maxage=900");
				//		r.addHeader("Expires", "Thu, 03 Jan 2019 14:42:16 GMT");
				//		r.addHeader("Age", "10000");
				//		r.addHeader("Connection", "keep-alive");
				//		r.addHeader("Keep-Alive", "timeout=2, max=99");
				//		r.addHeader("Vary", "Accept-Encoding,User-Agent");
						r.addHeader("Server", "Apache/2.2.16");
						r.addHeader("X-Cache", "HIT");
	//	    			r.addHeader("Last-Modified", "Wed, 05 Dec 2012 13:23:44 GMT");
				//		Log.i("etag :", path +" " + etag );
						r.addHeader("Etag", "\""+ etag + "\"");	
						return r;
					} catch (IOException e) {
						e.printStackTrace();
					}
	    		//	Response r = new NanoHTTPD.Response(Status.OK, mime, data);
	    		//	Response r = new NanoHTTPD.Response(data);
	    		//	return r;
	        	}
			}else{
				Log.i("SofaServer nie istnieje:", path );
			}
			return new NanoHTTPD.Response(Status.NOT_FOUND, "", "");
		}else{
			String system_action_res = route.run( this, theme, session);
			Response r = null;
			if(route.use_raw_output){
		//		Log.i("SofaServer use_raw_output true", route.getClass().toString() );
				r = new Response( system_action_res );
			}else{
		//		Log.i("SofaServer use_raw_output false", route.getClass().toString() );
				Chunk chunk				= theme.makeChunk("main#header");
				chunk.set("body", system_action_res );
				chunk.set("host", "http://" + session.getHeaders().get("host") );	
				r = new Response( chunk.toString() );
			}
	        return r;
		}
    }
    public Map<String, List<String>> decodeParameters(String queryString) {
        Map<String, List<String>> parms = new HashMap<String, List<String>>();
        if (queryString != null) {
            StringTokenizer st = new StringTokenizer(queryString, "&");
            while (st.hasMoreTokens()) {
                String e = st.nextToken();
                int sep = e.indexOf('=');
                String propertyName = (sep >= 0) ? decodePercent(e.substring(0, sep)).trim() : decodePercent(e).trim();
                if (!parms.containsKey(propertyName)) {
                    parms.put(propertyName, new ArrayList<String>());
                }
                String propertyValue = (sep >= 0) ? decodePercent(e.substring(sep + 1)) : null;
                if (propertyValue != null) {
                    parms.get(propertyName).add(propertyValue);
                }
            }
        }
        return parms;
    }
    private EmptyRoute doRoutes(String uri) {
    	if( uri.matches(MainPage.regex) ){
    		return new MainPage( uri );
    	}
    	if( uri.matches(RPCPage.regex) ){
    		return new RPCPage( uri );
    	}
		return null;
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

    String bytArrayToHex(byte[] a) {
    	   StringBuilder sb = new StringBuilder();
    	   for(byte b: a)
    	      sb.append(String.format("%02x", b&0xff));
    	   return sb.toString();
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
        put("ttf", "application/octet-stream");
        put("woff", "application/font-woff");
        put("ico", "image/x-icon");
        put("js", "text/javascript");
        put("jpg", "image/jpeg");
        put("jpeg", "image/jpeg");
        put("png", "image/png");
        put("ttf", "application/octet-stream");
        put("woff", "application/font-woff");
        put("ico", "image/x-icon");
		put("m3u", "audio/mpeg-url");
        put("mp4", "video/mp4");
        put("ogv", "video/ogg");
        put("flv", "video/x-flv");
        put("mov", "video/quicktime");
        put("swf", "application/x-shockwave-flash");
        put("pdf", "application/pdf");
        put("doc", "application/msword");
        put("ogg", "application/x-ogg");
        put("zip", "application/octet-stream");
        put("exe", "application/octet-stream");
        put("class", "application/octet-stream");
        put("mp3", "audio/mpeg");
        put("md", "text/plain");
        put("txt", "text/plain");
        put("asc", "text/plain");
    }};

	public static SofaServer getInstance() {
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
