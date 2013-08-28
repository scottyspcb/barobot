package com.barobot.webview;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

import com.collector.model.RemoteData;
import com.collector.model.RunnableWithData;
import com.eclipsesource.json.ParseException;

public class WebAppInterface {
	Context mContext;
	public static Map<String, String> ready2send = new HashMap<String, String>();
	 WebView webview;
	    WebAppInterface(Context c, WebView w) {
	        mContext = c;
	        webview = w;
	    }
	    public void showToast(String toast) {
	        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
	    }

	    public String getReady(String index) {
	       if(ready2send.containsKey(index)){
	    	   String s =  ready2send.get(index);
	    	   ready2send.remove(index);
	    	   return s;
	       }else{
	    	   return "";
	       }
	    }
	    public void saveReady(String index, String data ) {
	    	ready2send.put(index, data);
	    }
	    /*
	    public void loadUrl(final String url, final String data, final String onready, final String onerror ){
	    	final WebAppInterface appi = this;
	    	
	    	
	    	
	    	
		    RemoteData.doDownload(url, new RunnableWithData() {
		    	@Override
				public void run() {
					long start = System.nanoTime();
					String key = "item_"+ start +"/"+ready2send.size();
			    	try {
						DataParser.parseJson( this.data );
						saveReady(key, this.data);
						appi.runJs( onready, key );
					} catch (ParseException e) {
						e.printStackTrace();
						appi.runJs( onerror, key );
					}
				}
			});
	    }*/
	    public void loadMore(final String url, final String data, final String onready, final String onerror ){
	    	final WebAppInterface appi = this;
	    	
	    	if(url == "z1"){
	    		RemoteData.doLoad(url, new RunnableWithData() {
				    @Override
					public void run() {
						long start = System.nanoTime();
						String key = "item_"+ start +"/"+ready2send.size();
				    	try {
					//		DataParser.parseJson( this.data );
							saveReady(key, this.data);
							appi.runJs( onready, key );
						} catch (ParseException e) {
							e.printStackTrace();
							appi.runJs( onerror, key );
						}
					}
				});
	    	}else{
		    	RemoteData rd = new RemoteData("element_get");
		    	rd.query("page", data);
		    	rd.query("element_type", "exhibit");

		    	rd.load( new RunnableWithData() {
				    @Override
					public void run() {
						long start = System.nanoTime();
						String key = "item_"+ start +"/"+ready2send.size();
				    	try {
					//		DataParser.parseJson( this.data );
							saveReady(key, this.data);
							appi.runJs( onready, key );
						} catch (ParseException e) {
							e.printStackTrace();
					    	String msg = e.getMessage();
					    	appi.runJs( onerror, msg, key );
						}
					}
				}, new RunnableWithData() {
				    @Override
					public void run() {
						// blad - wyslij komunikat
				    	String msg = this.error.getMessage();
				    	appi.runJs( onerror, msg, "" );
					}
				});
	    	}
	    }
	    public void onLoad() {
	    //	this.runJs( "putIn", "#main_title","jeah!" );
	    }

		public void  doPhoto() {
			//mContext.cm.doPhoto();
			/*
			Intent intent = new Intent();
		    intent.setType("image/*");
		    intent.setAction(Intent.ACTION_GET_CONTENT);
		    intent.addCategory(Intent.CATEGORY_OPENABLE);
		    startActivityForResult(intent, mContext.PHOTO_REQUEST_CODE);*/
		}

	    
	    
	    
	    
	    
		private void runJs(final String method, final String... args) {
	    	final WebAppInterface appi = this;
	    	appi.webview.post(new Runnable() {
	            @Override
	            public void run() { 
	    			String callargs = "";
	    			for (int i = 0; i < args.length; ++i) {
	    				callargs += "'"+ addSlashes( args[i] ) +"'";
	    				if( i < args.length -1 ){
	    					callargs += ",";
	    				}
	    			}
	    			String url = "javascript:"+method+"("+ callargs +");void(0);";
	    			Log.d("+URL", url );
	            	appi.webview.loadUrl(url);
	            }
	        }); 
		}

	   private static String addSlashes(String str) {
		  if (str == null){
		     return "";
		  }
		  StringBuffer buffer = new StringBuffer();
		  for (int i = 0; i < str.length(); i++) {
		     switch (str.charAt(i)) {
		      case '\'': buffer.append("\\'");  break;
	//	      case '"':  buffer.append("\\\""); break;
	//	      case '\\': buffer.append("\\\\"); break;
		      default:   buffer.append(str.charAt(i));
		     }
		  }
		  return buffer.toString();
	   }
	public void setOrientation(String string) {
		this.runJs( "setOrientation", string );
	}
}
