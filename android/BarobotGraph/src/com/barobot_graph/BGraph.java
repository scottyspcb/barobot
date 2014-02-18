package com.barobot_graph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import com.barobot_graph.R;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


// android:theme="@android:style/Theme.NoTitleBar.Fullscreen"

public class BGraph extends Activity {
	// Layout Viewsd
	private static BGraph instance;

	public WebView webview = null;
	private FrameLayout webViewPlaceholder;


	public static int graph_speed = 20;
	public static int graph_repeat = 2;
	public static int graph_source = 2;
	public static int graph_xsize = 4;
	public static int graph_fps = 10;

	private AJS jsInterface;
	private boolean firstTime = true;


	public static final int TAB_GRAPH_ID = 0;
	public static int[] layouts = { R.layout.debug_tab_graph, };
	public Vector<Fragment> objVector = new Vector<Fragment>(10);
	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		instance = this; // Set up the window layout
		if (getIntent().hasExtra("bundle") && savedInstanceState == null) {
			savedInstanceState = getIntent().getExtras().getBundle("bundle");
		}
		 this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		 WindowManager.LayoutParams.FLAG_FULLSCREEN);
		 requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		WindowManager.LayoutParams.FLAG_FULLSCREEN);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.debug_tab_graph);

		Arduino.getInstance().onStart(this);

		if (firstTime) {
			// Constant.log("DebugTabGraph", "firstTime");
			ToggleButton xb1 = (ToggleButton) this
					.findViewById(R.id.graph_lines);
			xb1.setChecked(true);
			ToggleButton xb2 = (ToggleButton) this
					.findViewById(R.id.graph_scale);
			xb2.setChecked(true);
			firstTime = false;
		}
		graph_button_click bc = new graph_button_click(this);
		int[] buttons = {
				 R.id.analog0, 
				 R.id.analog1,
				 R.id.analog2,
				 R.id.analog3,
				 R.id.analog4,
				 R.id.analog5,
				 R.id.analog6,
				 R.id.analog7,
				 R.id.analog8,
				 R.id.perf,
				 R.id.pfinished,
				 R.id.pother,
				R.id.graph_repeat, R.id.graph_xsize, R.id.graph_random,
				R.id.graph_fps, R.id.graph_reverse, R.id.graph_scale,
				R.id.graph_points, R.id.graph_lines, R.id.graph_columns,
				R.id.graph_high_speed};
		for (int i = 0; i < buttons.length; i++) {
			View w = this.findViewById(buttons[i]);
			if (w == null) {
				// Constant.log(Constant.TAG,"pomijam: "+ buttons[i] );
				continue;
			}
			String classname = w.getClass().getName();
			if ("android.widget.Button".equals(classname)) {
				Button xb5 = (Button) this.findViewById(buttons[i]);
				xb5.setOnClickListener(bc);
			} else if ("android.widget.ToggleButton".equals(classname)) {
				ToggleButton xb5 = (ToggleButton) this
						.findViewById(buttons[i]);
				xb5.setOnClickListener(bc);
			}
		}
		
	//	enableUI(uiEnabled);
		initUI( savedInstanceState);

	}


	@Override
	public synchronized void onResume() {
		super.onResume();
		Arduino.getInstance().resume();
	}

	public ArrayList<interval> inters = new ArrayList<interval>();

	@Override
	public void onDestroy() {
		Arduino.getInstance().destroy();
		Iterator<interval> it = this.inters.iterator();
		while (it.hasNext()) {
			it.next().cancel();
		}
		if (webViewPlaceholder != null && webview != null) {
			webViewPlaceholder.removeView(webview);
		}
		super.onDestroy();
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Constant.log(Constant.TAG, "onActivityResult " + resultCode);
		switch (requestCode) {
		case BTListActivity.INTENT_NAME:
			Constant.log(Constant.TAG, "REQUEST_CONNECT_DEVICE_SECURE");
			// When BTListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				String address = data.getExtras().getString(
						Constant.EXTRA_DEVICE_ADDRESS); // Get the device MAC
														// address
		//		Arduino.getInstance().connectId(address);
			}
			break;
		case Constant.REQUEST_ENABLE_BT:
			Constant.log(Constant.TAG, "REQUEST_ENABLE_BT " + resultCode);
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so set up session
	//			Arduino.getInstance().setupBT(this);
			} else {
				// User did not enable Bluetooth or an error occurred
				Constant.log(Constant.TAG, "BT not enabled");
				Toast.makeText(this, R.string.bt_not_enabled_leaving,
						Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}

	// Any update to UI can not be carried out in a non UI thread like the one
	// used
	// for Server. Hence runOnUIThread is used.
	public void setText(final int target, final String result) {
		if (result != null) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					TextView bt = (TextView) findViewById(target);
					bt.setText(result);
				}
			});
		}
	}

	public static BGraph getInstance() {
		return instance;
	}

	protected void initUI(Bundle savedInstanceState) {
		// Retrieve UI elements
		this.webViewPlaceholder = ((FrameLayout) findViewById(R.id.webViewPlaceholder2));
		// Initialize the WebView if necessary
		if (this.webview == null) {
			Log.d("+NEW", "webview");
			webview = new WebView(this);
			webview.setLayoutParams(new ViewGroup.LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
			webview.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
			webview.setScrollbarFadingEnabled(true);
			webview.setWebChromeClient(new WebChromeClient() {
				public boolean onConsoleMessage(ConsoleMessage cm) {
					String sourceID = cm.sourceId();
					if (sourceID == null
							|| sourceID.startsWith("data:text/html")) {
						sourceID = "raw source";
					}
					Log.d("CONSOL E.LOG",
							cm.message() + " -- From line " + cm.lineNumber()
									+ " of " + sourceID);
					return true;
				}
			});

			final Activity activity = this;
			// webview.setWebViewClient(new MyWebViewClient());
			webview.setWebViewClient(new WebViewClient());
			webview.setWebViewClient(new WebViewClient() {
				public void onReceivedError(WebView view, int errorCode,
						String description, String failingUrl) {
					Log.d("CONSOLE.LOG", description + " -- in " + failingUrl);
				}

				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
					return false;
					/*
					 * if (Uri.parse(url).getHost().equals("www.example.com")) {
					 * // This is my web site, so do not override; let my
					 * WebView load the page return false; } // Otherwise, the
					 * link is not for a page on my site, so launch another
					 * Activity that handles URLs Intent intent = new
					 * Intent(Intent.ACTION_VIEW, Uri.parse(url));
					 * activity.startActivity(intent); return true;
					 */
				}
			});

			webview.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
			webview.setScrollbarFadingEnabled(true);
			webview.setHorizontalScrollBarEnabled(false);
			webview.setVerticalScrollBarEnabled(false);

			this.webview.setWebChromeClient(new WebChromeClient() {
				public void onProgressChanged(WebView view, int progress) {
					// Activities and WebViews measure progress with different
					// scales.
					// The progress meter will automatically disappear when we
					// reach 100%
					activity.setProgress(progress * 1000);
				}
			});

			jsInterface = new AJS(this, this.webview);
			webview.addJavascriptInterface(jsInterface, "AJS");
			webview.loadUrl("file:///android_asset/oscyloskop.htm");
			// webview.loadUrl("http://www.onet.pl");

			webview.addJavascriptInterface(new AJS(this, this.webview), "AJS");
			WebSettings webSettings = this.webview.getSettings();
			webSettings.setLoadsImagesAutomatically(true);
			// webSettings.setBuiltInZoomControls(true);
			// webSettings.setSupportZoom(true);
			webSettings.setJavaScriptEnabled(true);
			this.webViewPlaceholder.addView(this.webview);// Attach the WebView to
		} else {
			if (savedInstanceState == null) {
				Log.d("+NEW2", "webview");
				// webview.loadUrl(URLData);
			} else {
				Log.d("+NEW4", "webview");
				webview.restoreState(savedInstanceState);
				//
			}
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		if (webview != null) {
			// Remove the WebView from the old placeholder
			webViewPlaceholder.removeView(webview);
		}
		super.onConfigurationChanged(newConfig);
		// Load the layout resource for the new configuration

		// Checks the orientation of the screen
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
		}
		// Reinitialize the UI
		initUI(null);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// Constant.log("DebugTabGraph", "onSaveInstanceState");
		if (webview != null && outState != null) {
			webview.saveState(outState);
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		// Restore the state of the WebView
		if (webview != null) {
			webview.restoreState(savedInstanceState);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent serverIntent = null;
		switch (item.getItemId()) {
		case R.id.secure_connect_scan:
			/*
			if (Arduino.getInstance().checkBT() == false) {
				Toast.makeText(this, "Bluetooth jest niedostępny",
						Toast.LENGTH_LONG).show();
				finish();
			}
			serverIntent = new Intent(this, BTListActivity.class);*/
			break;
		}
		if (serverIntent != null) {
			serverIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(serverIntent);
		}
		return false;
	}
	public void port_enabled( final int num, final boolean value  ){
		int[] btns = {
				R.id.analog0,
				R.id.analog1,
				R.id.analog2,
				R.id.analog3,
				R.id.analog4,
				R.id.analog5,
				R.id.analog6,
				R.id.analog7,
				R.id.analog8,
				0,			//9
				0,			//10
				0,			//11
				0,			//12
				R.id.pother,		//13
				R.id.pfinished,		//14
				R.id.perf,			//15
				
		};
		final int btnid	= btns[num];
		if( btnid > 0){
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					ToggleButton tb = (ToggleButton) findViewById(btnid);
					if (tb != null) {
						Log.e(Constant.TAG, "SET port " + num + " to "  + (value ? "ON" : "OFF"));
						tb.setChecked(value);
					}
				}
			});
		}
	}
	// Any update to UI can not be carried out in a non UI thread like the one
	// used
	// for Server. Hence runOnUIThread is used.
	public void setText(int target, final String result, boolean now) {
		if (result != null) {
			final TextView text_field = (TextView) findViewById(target);
			if (now) {
				if (text_field != null) {
					text_field.setText(result);
				} else {
					// Constant.log("nie ma setText",result + " / " +target) ;
				}
			} else {
				if (text_field != null) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							text_field.setText(result);
						}
					});
				}
			}
		}
	}

	public void setChecked(final int target, final boolean equals) {
		// if (result != null) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				ToggleButton tb = (ToggleButton) findViewById(target);
				if (tb != null) {
					tb.setChecked(equals);
				}
			}
		});
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	


	public BGraph onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		return this;
	}


	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// Constant.log("DebugTabGraph", "onContextItemSelected");
		return super.onContextItemSelected(item);
	}

	@Override
	public void onOptionsMenuClosed(Menu menu) {
		// Constant.log("DebugTabGraph", "onOptionsMenuClosed");
		super.onOptionsMenuClosed(menu);
	}

	@Override
	public void onPause() {
		// Constant.log("DebugTabGraph", "onPause");
		super.onPause();
	}

	@Override
	public void onStart() {
		// Constant.log("DebugTabGraph", "onStart");
		super.onStart();
	}

	@Override
	public void onStop() {
		// Constant.log("DebugTabGraph", "onStop");
		super.onStop();
	}
	@Override
	public void startActivity(Intent intent) {
		// Constant.log("DebugTabGraph", "startActivity");
		super.startActivity(intent);
	}

	private void clearUI() {
		if (webview != null) {
			webViewPlaceholder.removeView(webview);
			this.webview = null;
		}
		if (jsInterface != null) {
			AJS.clearInstance();
			this.jsInterface = null;
		}
	}

	private int toInt(String str, int def) {
		int res;
		try {
			res = Integer.parseInt(str);
		} catch (NumberFormatException e) {
			return def;
		}
		return res;
	}

	private class graph_button_click implements OnClickListener,
			OnCheckedChangeListener {
		private Context dbw;

		
		public graph_button_click(Context debugWindow) {
			dbw = debugWindow;
		}
		@Override
		public void onClick(View v) {
			Arduino ar = Arduino.getInstance();
			
			boolean isChecked = false;
			String classname = v.getClass().getName();
			if ("android.widget.Button".equals(classname)) {
//				Button xb5 = (Button) findViewById(v.getId());
			} else if ("android.widget.ToggleButton".equals(classname)) {
				ToggleButton xb5 = (ToggleButton) findViewById(v.getId());
				isChecked = xb5.isChecked();
			}

			switch (v.getId()) {
			case R.id.graph_repeat:
				if (jsInterface != null) {
					show_dialog(new RunnableWithData() {
						@Override
						public void run() {
							graph_repeat = toInt(this.data, 1);	
							Arduino ar = Arduino.getInstance();
							ar.send("r" + graph_repeat);
						}
					}, "" + graph_repeat);
				}
				break;
			case R.id.graph_xsize:
				if (jsInterface != null) {
					show_dialog(new RunnableWithData() {
						@Override
						public void run() {
							graph_xsize = toInt(this.data, 4);
							jsInterface.runJs("changex", ""
									+ graph_xsize);
						}
					}, "" + graph_xsize);
				}
				break;

			case R.id.graph_fps:
				if (jsInterface != null) {
					show_dialog(new RunnableWithData() {
						@Override
						public void run() {
							graph_fps = toInt(this.data, 10);
							
							Arduino ar = Arduino.getInstance();
							ar.send("t" + graph_fps);

							jsInterface.runJs("changefps", ""
									+ graph_fps);
						}
					}, "" + graph_fps);
				}
				break;
				
				
				
				
				
				
				
				
				
				
				
				
				case R.id.graph_random:
					int graph_source5 = graph_source;
					disable_analog(ar, graph_source5);
					int graph_speed2 = graph_speed;
					if (jsInterface != null) {
						jsInterface.runJs("show_random", "" + graph_speed2);
					}
					break;

				case R.id.graph_reverse:
					if (jsInterface != null) {
						jsInterface.runJs("reverseY", isChecked ? "1" : "0");
					}
					break;
				case R.id.graph_scale:
					if (jsInterface != null) {
						jsInterface.runJs("toggleLocalMin", isChecked ? "1" : "0");
					}
					break;
				case R.id.graph_points:
					if (jsInterface != null) {
						jsInterface.runJs("dots", isChecked ? "1" : "0");
					}
					break;
				case R.id.graph_lines:
					if (jsInterface != null) {
						jsInterface.runJs("lines", isChecked ? "1" : "0");
					}
					break;
				case R.id.graph_columns:
					if (jsInterface != null) {
						jsInterface.runJs("column", isChecked ? "1" : "0");
					}
					break;
				case R.id.graph_high_speed:
					if (jsInterface != null) {
						jsInterface.runJs("sethighspeed", isChecked ? "1" : "0");
					}
					break;
		
				case R.id.analog0:
					if(isChecked){
						ar.send("+0");
					}else{
						ar.send("-0");
					}
					if (jsInterface != null) {
			//			jsInterface.runJs("sethighspeed", isChecked ? "1" : "0");
					}
					break;
				case  R.id.analog1:
					if(isChecked){
						ar.send("+1");
					}else{
						ar.send("-1");
					}
					if (jsInterface != null) {
			//			jsInterface.runJs("sethighspeed", isChecked ? "1" : "0");
					}
					break;
				case R.id.analog2:
					if(isChecked){
						ar.send("+2");
					}else{
						ar.send("-2");
					}
					if (jsInterface != null) {
			//			jsInterface.runJs("sethighspeed", isChecked ? "1" : "0");
					}
					break;
				case  R.id.analog3:
					if(isChecked){
						ar.send("+3");
					}else{
						ar.send("-3");
					}
					if (jsInterface != null) {
			//			jsInterface.runJs("sethighspeed", isChecked ? "1" : "0");
					}
					break;
				case  R.id.analog4:
					if(isChecked){
						ar.send("+4");
					}else{
						ar.send("-4");
					}
					if (jsInterface != null) {
				//		jsInterface.runJs("sethighspeed", isChecked ? "1" : "0");
					}
					break;
				case  R.id.analog5:
					if(isChecked){
						ar.send("+5");
					}else{
						ar.send("-5");
					}
					if (jsInterface != null) {
				//		jsInterface.runJs("sethighspeed", isChecked ? "1" : "0");
					}
					break;
				case  R.id.analog6:
					if(isChecked){
						ar.send("+6");
					}else{
						ar.send("-6");
					}
					if (jsInterface != null) {
				//		jsInterface.runJs("sethighspeed", isChecked ? "1" : "0");
					}
					break;
				case  R.id.analog7:
					if(isChecked){
						ar.send("+7");
					}else{
						ar.send("-7");
					}
					if (jsInterface != null) {
				//		jsInterface.runJs("sethighspeed", isChecked ? "1" : "0");
					}
					break;
				case  R.id.analog8:
					if(isChecked){
						ar.send("+8");
					}else{
						ar.send("-8");
					}
					if (jsInterface != null) {
				//		jsInterface.runJs("sethighspeed", isChecked ? "1" : "0");
					}
					break;
				case  R.id.perf:
					if(isChecked){
						ar.send("+p");
					}else{
						ar.send("-p");
					}
					if (jsInterface != null) {
				//		jsInterface.runJs("sethighspeed", isChecked ? "1" : "0");
					}
					break;
					
					
				case  R.id.pfinished:
					if(isChecked){
						ar.send("+f");
					}else{
						ar.send("-f");
					}
					if (jsInterface != null) {
				//		jsInterface.runJs("sethighspeed", isChecked ? "1" : "0");
					}
					break;
				case  R.id.pother:
					if(isChecked){
						ar.send("+o");
					}else{
						ar.send("-o");
					}
					if (jsInterface != null) {
				//		jsInterface.runJs("sethighspeed", isChecked ? "1" : "0");
					}
					break;				
				}	
				
				
				
				

		}

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			Arduino ar = Arduino.getInstance();
			switch (buttonView.getId()) {
			
			}
		}
	
		public void show_dialog(final RunnableWithData onfinish,
				String defaultValue) {
			// custom dialog
			final Dialog dialog = new Dialog(dbw);
			dialog.setContentView(R.layout.dialog_int);
			dialog.setTitle("Podaj...");

			// set the custom dialog components - text, image and button
			final EditText text = (EditText) dialog
					.findViewById(R.id.dialog_int_input);
			text.setText(defaultValue);

			Button dialogButton = (Button) dialog
					.findViewById(R.id.dialog_int_dialogButtonOK);
			// if button is clicked, close the custom dialog
			dialogButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					String res = text.getText().toString();
					onfinish.sendData(res);
					onfinish.run();
					dialog.dismiss();
				}
			});
			dialog.show();
		}
	}
	
	public static void enable_analog(Arduino ar, int pin, int time, int repeat) {
		ar.send("LIVE A " + pin + "," + time + "," + repeat); // repeat pomiary													// porcie pin
	}

	public static void disable_analog(Arduino ar, int analogWaga) {
		ar.send("LIVE A OFF");
	}

}
