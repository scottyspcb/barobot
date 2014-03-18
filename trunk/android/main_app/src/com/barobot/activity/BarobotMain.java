package com.barobot.activity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.barobot.AppInvoker;
import com.barobot.R;
import com.barobot.common.Initiator;
import com.barobot.common.constant.Constant;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.virtualComponents;
import com.barobot.hardware.serial.BluetoothChatService;

// android:theme="@android:style/Theme.NoTitleBar.Fullscreen"

public class BarobotMain extends BarobotActivity {
    // Layout Viewsd
	private static BarobotMain instance;
	public WebView webview	=null;
	private FrameLayout webViewPlaceholder;
    @Override
    public void onCreate(Bundle savedInstanceState) {
		instance = this;	        // Set up the window layout
		if (getIntent().hasExtra("bundle") && savedInstanceState==null){
		   savedInstanceState = getIntent().getExtras().getBundle("bundle");
		}
	//	this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
	//	this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
	//	requestWindowFeature(Window.FEATURE_NO_TITLE);

//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.barobot_main);
        AppInvoker.createInstance( this ).onCreate();

    	//	getWindow().requestFeature(Window.FEATURE_PROGRESS);
	//	StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	//	StrictMode.setThreadPolicy(policy); 
	//	initUI( savedInstanceState);  
		int mUIFlag = View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
	    getWindow().getDecorView().setSystemUiVisibility(mUIFlag);
	    
/*
	    final UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
	    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
	                public void uncaughtException(Thread thread, Throwable ex) {
	                  Intent launchIntent = new Intent(BarobotMain.this.getIntent());
	                  PendingIntent pending = PendingIntent.getActivity(BarobotMain.this, 0,
	                        launchIntent, BarobotMain.this.getIntent().getFlags());

	                  getAlarmManager().set(AlarmManager.RTC, System.currentTimeMillis() + 2000, pending);
	                  defaultHandler.uncaughtException(thread, ex);
	                }

	    }); */  
    }
	private AlarmManager getAlarmManager() {
		AlarmManager mgr=(AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
		return mgr;
	}
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
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
		AppInvoker.getInstance().onPause();
		super.onPause();
	}
    @Override
    public synchronized void onResume() {
        super.onResume();
        AppInvoker.getInstance().onResume();
    }
    @Override
    public void onDestroy() {
    	AppInvoker.getInstance().onDestroy();
    	if( webViewPlaceholder !=null && webview != null){
    		webViewPlaceholder.removeView(webview);
    	}
        super.onDestroy();
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Initiator.logger.i(Constant.TAG, "onActivityResult " + resultCode);
        switch (requestCode){
        case UpdateActivity.INTENT_NAME:
        	Initiator.logger.i(Constant.TAG, "END OF UpdateActivity");
            break;
        case AboutActivity.INTENT_NAME:
        	Initiator.logger.i(Constant.TAG, "END OF AboutActivity");
            break;
        case MainSettingsActivity.INTENT_NAME:
        	Initiator.logger.i(Constant.TAG, "END OF SETTINGS");
            break;
        case DebugActivity.INTENT_NAME:
        	Initiator.logger.i(Constant.TAG, "END OF DebugActivity");
            break;
        case BTListActivity.INTENT_NAME:
        	Initiator.logger.i(Constant.TAG, "REQUEST_CONNECT_DEVICE_SECURE");
            // When BTListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                String address = data.getExtras().getString(BluetoothChatService.EXTRA_DEVICE_ADDRESS);           // Get the device MAC address
                Arduino.getInstance().connectId(address);
            }
            break;
        case BluetoothChatService.REQUEST_ENABLE_BT:
        	Initiator.logger.i(Constant.TAG, "REQUEST_ENABLE_BT " + resultCode);
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up session
                Arduino.getInstance().setupBT( this );
            } else {
                // User did not enable Bluetooth or an error occurred
                Initiator.logger.i(Constant.TAG, "BT not enabled");
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

	//Any update to UI can not be carried out in a non UI thread like the one used
	//for Server. Hence runOnUIThread is used.
	public void setText(final int target, final String result) {
		if(result!=null){
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					TextView bt = (TextView) findViewById(target);	    	
					bt.setText(result);
				}
			});
		}
	}
	public static BarobotMain getInstance(){
		return instance;
	}

	@Override
	public void onBackPressed() {
	    new AlertDialog.Builder(this)
	        .setIcon(android.R.drawable.ic_dialog_alert)
	        .setTitle("Koniec?")
	        .setMessage("Czy na pewno zamknąć aplikację i przerwać pracę robota?")
	        .setPositiveButton("Yes", new DialogInterface.OnClickListener(){
	        @Override
	        public void onClick(DialogInterface dialog, int which) {
	            finish();
	        }
	    })
	    .setNegativeButton("No", null).show();
	}

	protected void initUI(Bundle savedInstanceState) {
	    /*// Retrieve UI elements
	    this.webViewPlaceholder = ((FrameLayout)findViewById(R.id.webViewPlaceholder));
	    // Initialize the WebView if necessary
	    if (this.webview == null) {
	    	Log.d("+NEW", "webview" );
	    	webview = new WebView(this);
	        webview.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
	        webview.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
	        webview.setScrollbarFadingEnabled(true);
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

	   		final Activity activity = this;
	   //		webview.setWebViewClient(new MyWebViewClient());
	   		webview.setWebViewClient(new WebViewClient());
	   		webview.setWebViewClient(new WebViewClient() {
				public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
					Log.d("CONSOLE.LOG", description + " -- in "+ failingUrl );
				}
		   	    @Override
			    public boolean shouldOverrideUrlLoading(WebView view, String url) {
		   	    	return false;
		   	    	
			        if (Uri.parse(url).getHost().equals("www.example.com")) {
			            // This is my web site, so do not override; let my WebView load the page
			            return false;
			        }
			        // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
			        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			        activity.startActivity(intent);
			        return true;
			    }
		   		});	    	

	    	webview.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
	    	webview.setScrollbarFadingEnabled(true);
	    	webview.setHorizontalScrollBarEnabled(false);
	    	webview.setVerticalScrollBarEnabled(false);

	   		this.webview.setWebChromeClient(new WebChromeClient() {
	   		   public void onProgressChanged(WebView view, int progress) {
	   		     // Activities and WebViews measure progress with different scales.
	   		     // The progress meter will automatically disappear when we reach 100%
	   		     activity.setProgress(progress * 1000);
	   		   }
	   		});
	        webview.loadUrl("http://localhost:8000");
	       //webview.loadUrl("http://www.onet.pl");

	        webview.addJavascriptInterface(new AJS(this, this.webview), "AJS");
	   		WebSettings webSettings = this.webview.getSettings();
	   		webSettings.setLoadsImagesAutomatically(true);
	//   		webSettings.setBuiltInZoomControls(true);
	//   		webSettings.setSupportZoom(true);
	   		webSettings.setJavaScriptEnabled(true);

	    }else{
			 if (savedInstanceState == null){
				 Log.d("+NEW2", "webview" );
			//	 webview.loadUrl(URLData);
			 } else{
				 Log.d("+NEW4", "webview" );
				 webview.restoreState(savedInstanceState);
			 //  
			 }	
	    }
	    this.webViewPlaceholder.addView(this.webview);// Attach the WebView to its placeholder
*/	}
	@Override
	public void onConfigurationChanged(Configuration newConfig){
		if (webview != null){
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
	//	initUI(null);
	}
/*
	@Override
	  protected void onSaveInstanceState(Bundle outState){
	    super.onSaveInstanceState(outState);
	    // Save the state of the WebView
	    if(webview != null){
	    	webview.saveState(outState);
	    }
	  }
	  @Override
	  protected void onRestoreInstanceState(Bundle savedInstanceState) {
	    super.onRestoreInstanceState(savedInstanceState);
	    // Restore the state of the WebView
	    if(webview != null){
	    	webview.restoreState(savedInstanceState);
	    }
	  }*/
	  public void showError(){
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					 Builder show = new AlertDialog.Builder(BarobotMain.this)
					    .setTitle("Kalibracja")
					    .setMessage("Kalibracja się nie udała. Czy spróbować ponownie?")
					    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					        public void onClick(DialogInterface dialog, int which) { 
					            // continue with delete
					        	virtualComponents.kalibrcja();
					        }
					     })
					    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
					        public void onClick(DialogInterface dialog, int which) { 
					            // do nothing
					        }
					     });
					 show.show();    
				}
			});
	  } 
}

/*
new AlertDialog.Builder(this)
.setTitle("Delete entry")
.setMessage("Are you sure you want to delete this entry?")
.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
    public void onClick(DialogInterface dialog, int which) { 
        // continue with delete
    }
 })
.setNegativeButton("No", new DialogInterface.OnClickListener() {
    public void onClick(DialogInterface dialog, int which) { 
        // do nothing
    }
 })
 .show();
*/