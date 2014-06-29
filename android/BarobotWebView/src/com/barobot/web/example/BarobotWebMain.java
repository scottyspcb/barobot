package com.barobot.web.example;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.barobot.web.route.MainPage;
import com.barobot.web.route.RPCPage;
import com.barobot.web.route.TemplateRendering;
import com.barobot.web.server.SofaServer;
import com.barobotweb.R;

import android.webkit.WebViewClient;

public class BarobotWebMain extends Activity {
	public WebView webview	=null;
	SofaServer ss = null;

	@Override
    public void onCreate(Bundle savedInstanceState) {
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

//		requestWindowFeature(Window.FEATURE_NO_TITLE);
//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

    	ss =  SofaServer.getInstance();
		ss.setBaseContext(getBaseContext());
		ss.addRoute( new RPCPage() );
		ss.addRoute( new TemplateRendering() );
		ss.addRoute( new MainPage() );
        try {
			ss.start();
		} catch (IOException e) {
			e.printStackTrace();
		}

    	//	getWindow().requestFeature(Window.FEATURE_PROGRESS);
	//	StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	//	StrictMode.setThreadPolicy(policy); 
		initUI( savedInstanceState);  
		int mUIFlag = View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
	    getWindow().getDecorView().setSystemUiVisibility(mUIFlag);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
       Log.i("onKeyDown","" + keyCode);
       if(keyCode == KeyEvent.KEYCODE_HOME){
    	   Log.i("onKeyDown","KEYCODE_HOME");
    	   return true;
       }
       if(keyCode==KeyEvent.KEYCODE_BACK){
    	   Log.i("onKeyDown","KEYCODE_BACK");
           //finish();
    	   return true;
       }
       return super.onKeyDown( keyCode, event);
    }
    
    @Override
    public void onStart() {
        super.onStart();
    }
	@Override
	protected void onPause() {
		super.onPause();
	}
    @Override
    public void onResume() {
        super.onResume();
    }
    @Override
    public void onDestroy() {
    	SofaServer.getInstance().stop();
        super.onDestroy();
    }

	@Override
	public void onBackPressed() {
	    new AlertDialog.Builder(this)
	        .setIcon(android.R.drawable.ic_dialog_alert)
	        .setTitle("Koniec?")
	        .setMessage("Czy na pewno zamknąć aplikację przerwać pracę robota?")
	        .setPositiveButton("Yes", new DialogInterface.OnClickListener(){
	        @Override
	        public void onClick(DialogInterface dialog, int which) {
	            finish();
	        }
	    })
	    .setNegativeButton("No", null).show();
	}

	protected void initUI(Bundle savedInstanceState) {
		this.webview = (WebView) findViewById(R.id.webview);
       
		webview.setWebViewClient(new WebViewClient() {
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				Log.d("CONSOLE.LOG", description + " -- in "+ failingUrl );
			}
	   	    public boolean shouldOverrideUrlLoading(WebView view, String url) {
	   	    	return false;
	   	    	/*
		        if (Uri.parse(url).getHost().equals("www.example.com")) {
		            // This is my web site, so do not override; let my WebView load the page
		            return false;
		        }
		        // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
		        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		        activity.startActivity(intent);
		        return true;*/
		    }
	   		});	 
		
		 webview.setWebChromeClient(new WebChromeClient() {
				public boolean onConsoleMessage(ConsoleMessage cm) {
					String sourceID = cm.sourceId();
					if(sourceID == null || sourceID.startsWith("data:text/html")){
						sourceID ="raw source";
					}
					Log.d("CONSOL E.LOG", cm.message() + " -- From line "+ cm.lineNumber() + " of "+ sourceID );
					return true;
				}
			});
    //	webview.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
    //	webview.setScrollbarFadingEnabled(true);
    //	webview.setHorizontalScrollBarEnabled(false);
    //	webview.setVerticalScrollBarEnabled(false);
/*
	   		this.webview.setWebChromeClient(new WebChromeClient() {
	   		   public void onProgressChanged(WebView view, int progress) {
	   		     // Activities and WebViews measure progress with different scales.
	   		     // The progress meter will automatically disappear when we reach 100%
	   		     activity.setProgress(progress * 1000);
	   		   }
	   		});
	*/

		//webview.setBackgroundColor(0x00000000);
		webview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

		int port = ss.getListeningPort();
    	webview.loadUrl("http://localhost:" + port);
    	WebSettings webSettings = this.webview.getSettings();
    	webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH); 
    //	webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

//    	webSettings.setLoadsImagesAutomatically(true);
//   	webSettings.setBuiltInZoomControls(true);
//   	webSettings.setSupportZoom(true);
   		webSettings.setJavaScriptEnabled(true);
	}
}
