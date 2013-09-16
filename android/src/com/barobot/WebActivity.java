package com.barobot;

import com.barobot.drinks.RunnableWithData;
import com.barobot.hardware.ArduinoQueue;
import com.barobot.hardware.virtualComponents;
import com.barobot.utils.Arduino;
import com.barobot.webview.AJS;
import com.barobot.webview.htmlBrowser;

import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnClickListener;

public class WebActivity extends Activity {

	public static final int INTENT_NAME = 11;

	htmlBrowser wb =null;
	public WebView webview	=null;
	public FrameLayout webViewPlaceholder;
	private int live_analog_num		= 0;
	private int live_analog_time	= 10;
	private int live_analog_repeat	= 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	//	getWindow().requestFeature(Window.FEATURE_PROGRESS);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web);
	//	StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	//	StrictMode.setThreadPolicy(policy); 
		initUI();
	}

	  @Override
	  protected void onPause() {
	    super.onPause();
	  }
	  @Override
	  protected void onResume() {
		  super.onResume();
	  }

	protected void initUI() {
	    // Retrieve UI elements
	    this.webViewPlaceholder = ((FrameLayout)findViewById(R.id.webViewPlaceholder));
	    // Initialize the WebView if necessary
	    if (this.webview == null) {
	    	Log.d("+NEW", "webview" );
	    	webview = new WebView(this);
	        webview.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
	        webview.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
	        webview.setScrollbarFadingEnabled(true);
	        
	   		WebSettings webSettings = this.webview.getSettings();
	   		webSettings.setLoadsImagesAutomatically(true);
	   		webSettings.setBuiltInZoomControls(true);
	   		webSettings.setSupportZoom(true);
	   		webSettings.setJavaScriptEnabled(true);

	        this.webview.setWebChromeClient(new WebChromeClient() {
				public boolean onConsoleMessage(ConsoleMessage cm) {
					String sourceID = cm.sourceId();
					if(sourceID == null || sourceID.startsWith("data:text/html")){
						sourceID ="raw source";
					}
					Log.d("CONSOLE.LOG", cm.message() + " -- From line "
					+ cm.lineNumber() + " of "
					+ sourceID );
					return true;
				}
			});

	   		final Activity activity = this;
	   //		webview.setWebViewClient(new MyWebViewClient());
	   		webview.setWebViewClient(new WebViewClient());
	   		webview.setWebViewClient(new WebViewClient() {
				public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
					Toast.makeText(activity, "Oh no! " + description, Toast.LENGTH_SHORT).show();
				}
		   	    @Override
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

	    	webview.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
	    	webview.setScrollbarFadingEnabled(true);

	   		this.webview.setWebChromeClient(new WebChromeClient() {
	   		   public void onProgressChanged(WebView view, int progress) {
	   		     // Activities and WebViews measure progress with different scales.
	   		     // The progress meter will automatically disappear when we reach 100%
	   		     activity.setProgress(progress * 1000);
	   		   }
	   		});
	   		webview.addJavascriptInterface(new AJS(this, this.webview), "AJS");

	   		wb = new htmlBrowser( this );
   			wb.startPage();	        // Load a page
	    }
	    this.webViewPlaceholder.addView(this.webview);// Attach the WebView to its placeholder
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.web, menu);
		return true;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig){
		if (webview != null)	{
		  // Remove the WebView from the old placeholder
			webViewPlaceholder.removeView(webview);
		}
		super.onConfigurationChanged(newConfig);
		// Load the layout resource for the new configuration
		setContentView(R.layout.activity_web);

	    // Checks the orientation of the screen
	    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
	        Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
	    } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
	        Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
	    }
		// Reinitialize the UI
		initUI();
	}

	public void onBackPressed() {
		Arduino ar = Arduino.getInstance();
		virtualComponents.disable_analog(ar, live_analog_num);
		super.onBackPressed();
		//moveTaskToBack(true);
	//	return;
	}
	@Override
	  protected void onSaveInstanceState(Bundle outState){
	    super.onSaveInstanceState(outState);
	    // Save the state of the WebView
	    webview.saveState(outState);
	  }
	  @Override
	  protected void onRestoreInstanceState(Bundle savedInstanceState) {
	    super.onRestoreInstanceState(savedInstanceState);
	    // Restore the state of the WebView
	    webview.restoreState(savedInstanceState);
	  }	

	 public void show_dialog( final RunnableWithData onfinish, String defaultValue ){
		// custom dialog
		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.dialog_int);
		dialog.setTitle("Title...");
 
		// set the custom dialog components - text, image and button
		final EditText text = (EditText) dialog.findViewById(R.id.dialog_int_input);
		text.setText(defaultValue);

		Button dialogButton = (Button) dialog.findViewById(R.id.dialog_int_dialogButtonOK);
		// if button is clicked, close the custom dialog
		dialogButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String res =  text.getText().toString();
				onfinish.sendData(res);
				onfinish.run();
				dialog.dismiss();
			}
		});
 
		dialog.show();
	 }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent serverIntent = null;
        Arduino ar = Arduino.getInstance();
		ArduinoQueue q = new ArduinoQueue();

        switch (item.getItemId()) {
        case R.id.web_machaj_x:
			virtualComponents.moveZDown( q );
			q.add("SET Y " + virtualComponents.get("NEUTRAL_POS_Y", "0" ), true );
			long lengthx4	=  virtualComponents.getInt("LENGTHX", 600 );
			for( int i =0; i<10;i++){
				//q.add("SET X " + (lengthx4/4), true );
				//q.add("SET X " + (lengthx4/4 * 3) , true );
				q.add("SET X 0", true );
				q.add("SET X " + (lengthx4) , true );
			}
			ar.send(q);
            break;
        case R.id.web_machaj_y:
			virtualComponents.moveZDown( q );
			long lengthy4	=  virtualComponents.getInt("LENGTHY", 600 );
			for( int i =0; i<10;i++){
				q.add("SET Y " + (lengthy4/4), true );
				q.add("SET Y " + (lengthy4/4 * 3) , true );
			}
			ar.send(q);
            break;
        case R.id.select_timeout:
        	show_dialog( new RunnableWithData(){
				@Override
				public void run() {
					// TODO Auto-generated method stub
					live_analog_time	= Integer.parseInt(this.data);
					Arduino ar			= Arduino.getInstance();
		        	virtualComponents.enable_analog(ar, live_analog_num, live_analog_time, live_analog_repeat);	
				}
        	}, ""+live_analog_time);

            break;            
        case R.id.select_repeat:
        	show_dialog( new RunnableWithData(){
				@Override
				public void run() {
					// TODO Auto-generated method stub
					live_analog_repeat	= Integer.parseInt(this.data);
					Arduino ar			= Arduino.getInstance();
		        	virtualComponents.enable_analog(ar, live_analog_num, live_analog_time, live_analog_repeat);	
				}
        	}, ""+live_analog_repeat);
            break;          
        case R.id.menu_settings:
        	serverIntent = new Intent(this, MainSettingsActivity.class);
            startActivityForResult(serverIntent, MainSettingsActivity.INTENT_NAME);
            break;

        case R.id.analog0:
        	live_analog_num		= 0;
        	virtualComponents.enable_analog(ar, live_analog_num, live_analog_time, live_analog_repeat);	
            break;            
        case R.id.analog1:
        	live_analog_num		= 1;
        	virtualComponents.enable_analog(ar, live_analog_num, live_analog_time, live_analog_repeat);	
            break;            
        case R.id.analog2:
        	live_analog_num		= 2;
        	virtualComponents.enable_analog(ar, live_analog_num, live_analog_time, live_analog_repeat);	
            break;            
        case R.id.analog3:
        	live_analog_num		= 3;
        	virtualComponents.enable_analog(ar, live_analog_num, live_analog_time, live_analog_repeat);	
            break;            
        case R.id.analog4:
        	live_analog_num		= 4;
        	virtualComponents.enable_analog(ar, live_analog_num, live_analog_time, live_analog_repeat);	
            break;
            /*
        case R.id.analog5:
        	live_analog_num		= 5;
        	virtualComponents.enable_analog(ar, live_analog_num, live_analog_time, live_analog_repeat);	
            break;            
        case R.id.analog6:
        	live_analog_num		= 6;
        	virtualComponents.enable_analog(ar, live_analog_num, live_analog_time, live_analog_repeat);	
            break;            
        case R.id.analog7:
        	live_analog_num		= 7;
        	virtualComponents.enable_analog(ar, live_analog_num, live_analog_time, live_analog_repeat);	
            break;            
        case R.id.analog8:
        	live_analog_num		= 8;
        	virtualComponents.enable_analog(ar, live_analog_num, live_analog_time, live_analog_repeat);	
            break;            
        case R.id.analog9:
        	live_analog_num		= 9;
        	virtualComponents.enable_analog(ar, live_analog_num, live_analog_time, live_analog_repeat);	
            break;            
        case R.id.analog10:
        	live_analog_num		= 10;
        	virtualComponents.enable_analog(ar, live_analog_num, live_analog_time, live_analog_repeat);	
            break;            
        case R.id.analog11:
        	live_analog_num		= 11;
        	virtualComponents.enable_analog(ar, live_analog_num, live_analog_time, live_analog_repeat);	
            break;            
        case R.id.analog12:
        	live_analog_num		= 12;
        	virtualComponents.enable_analog(ar, live_analog_num, live_analog_time, live_analog_repeat);	
            break;            
        case R.id.analog13:
        	live_analog_num		= 13;
        	virtualComponents.enable_analog(ar, live_analog_num, live_analog_time, live_analog_repeat);	
            break;            
        case R.id.analog14:
        	live_analog_num		= 14;
        	virtualComponents.enable_analog(ar, live_analog_num, live_analog_time, live_analog_repeat);	
            break;            
        case R.id.analog15:
        	live_analog_num		= 15;
        	virtualComponents.enable_analog(ar, live_analog_num, live_analog_time, live_analog_repeat);	
            break;*/            
        case R.id.analog20:
        	live_analog_num		= 20;
        	virtualComponents.enable_analog(ar, live_analog_num, live_analog_time, live_analog_repeat);	
            break;            
        case R.id.analog21:
        	live_analog_num		= 21;
        	virtualComponents.enable_analog(ar, live_analog_num, live_analog_time, live_analog_repeat);	
            break;            
            
            
            
            
	    }        

        return false;
    }
}
