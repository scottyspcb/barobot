package com.barobot.debug;

import com.barobot.R;
import com.barobot.activity.DebugActivity;
import com.barobot.constant.Constant;
import com.barobot.hardware.virtualComponents;
import com.barobot.utils.Arduino;
import com.barobot.utils.RunnableWithData;
import com.barobot.utils.AJS;

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
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ToggleButton;

/**
 * A dummy fragment representing a section of the app
 */
public class DebugTabGraph extends Fragment {

	public static int graph_speed	= 20;
	public static int graph_repeat	= 2;
	public static int graph_source	= 2;
	public static int graph_xsize	= 4;
	public static int graph_fps		= 10;
	
	
	public int tab_id	= -1 ;
	private Activity cc;
	private WebView webview	=null;
	private FrameLayout webViewPlaceholder;
	private View rootView;
	private AJS jsInterface;
	private boolean uiEnabled = false;
	private boolean firstTime = true;

    public DebugTabGraph(Activity debugActivity, int tabCommandsId) {
    //	Constant.log("DebugTabGraph", "init");
    	this.tab_id = tabCommandsId;
    	this.cc=debugActivity;
	}
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
  //  	Constant.log("DebugTabGraph", "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState!=null && !savedInstanceState.isEmpty()){
        	 webview.restoreState(savedInstanceState);
        }
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
	//	Constant.log("DebugTabGraph", "onCreateView");
		//View rootView = inflater.inflate( R.layout.fragment_device_list_dummy, container, false);
		View rootView	= inflater.inflate(  DebugActivity.layouts[tab_id], container, false);
		this.rootView	= rootView;
		if(firstTime){
		//	Constant.log("DebugTabGraph", "firstTime");
			ToggleButton xb1 = (ToggleButton) rootView.findViewById(R.id.graph_lines);
			xb1.setChecked(true);
			ToggleButton xb2 = (ToggleButton) rootView.findViewById(R.id.graph_scale);
			xb2.setChecked(true);
			firstTime = false;
		}
		graph_button_click bc	= new graph_button_click( this.cc );
		int[] buttons = {
				R.id.graph_source,
				R.id.graph_speed,
				R.id.graph_repeat,
				R.id.graph_xsize,
				R.id.graph_random,
				R.id.graph_fps,
				R.id.graph_reverse,
				R.id.graph_scale,
				R.id.graph_points,
				R.id.graph_lines,
				R.id.graph_columns,
				R.id.graph_high_speed,
				R.id.graph_active
			};
		for(int i =0; i<buttons.length;i++){
			View w = rootView.findViewById(buttons[i]);
			if( w == null){
	//			Constant.log(Constant.TAG,"pomijam: "+ buttons[i] );
				continue;
			}
			String classname = w.getClass().getName();
			if( "android.widget.Button".equals( classname )){
				Button xb5 = (Button) rootView.findViewById(buttons[i]);
				xb5.setOnClickListener(bc);		
			}else if("android.widget.ToggleButton".equals( classname ) ){
				ToggleButton xb5 = (ToggleButton) rootView.findViewById(buttons[i]);
				xb5.setOnCheckedChangeListener(bc);			
			}
		}
		enableUI(uiEnabled);
		initUI( uiEnabled );
		return rootView;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
//		Constant.log("DebugTabGraph", "onConfigurationChanged");
		super.onConfigurationChanged(newConfig);
		if (webview != null)	{
		  // Remove the WebView from the old placeholder
			webViewPlaceholder.removeView(webview);
		}
	//	ToggleButton gab = (ToggleButton) rootView.findViewById(R.id.graph_active);
		initUI( uiEnabled );
	}
	@Override
	public boolean onContextItemSelected(MenuItem item) {
	//	Constant.log("DebugTabGraph", "onContextItemSelected");
		return super.onContextItemSelected(item);
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
	//	Constant.log("DebugTabGraph", "onCreate");
		super.onCreate(savedInstanceState);
	}
	@Override
	public void onDestroyOptionsMenu() {
//		Constant.log("DebugTabGraph", "onDestroyOptionsMenu");
		super.onDestroyOptionsMenu();
	}
	@Override
	public void onDestroyView() {
	//	Constant.log("DebugTabGraph", "onDestroyView");
		if (webview != null){
			webViewPlaceholder.removeView(webview);	  // Remove the WebView from the old placeholder
		}
		super.onDestroyView();
	}
	@Override
	public void onDetach() {
	//	Constant.log("DebugTabGraph", "onDetach");
		super.onDetach();
	}
	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
	//	Constant.log("DebugTabGraph", "onHiddenChanged");
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	//	Constant.log("DebugTabGraph", "onOptionsItemSelected");
		return super.onOptionsItemSelected(item);
	}
	@Override
	public void onOptionsMenuClosed(Menu menu) {
	//	Constant.log("DebugTabGraph", "onOptionsMenuClosed");
		super.onOptionsMenuClosed(menu);
	}
	@Override
	public void onPause() {
	//	Constant.log("DebugTabGraph", "onPause");
		super.onPause();
	}
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
	//	Constant.log("DebugTabGraph", "onPrepareOptionsMenu");
		super.onPrepareOptionsMenu(menu);
	}
	@Override
	public void onResume() {
	//	Constant.log("DebugTabGraph", "onResume");
		super.onResume();
	}
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	//	Constant.log("DebugTabGraph", "onSaveInstanceState");
		if(webview!=null && outState!= null){
			webview.saveState(outState);
		}
	}
	@Override
	public void onStart() {
	//	Constant.log("DebugTabGraph", "onStart");
		super.onStart();
	}
	@Override
	public void onStop() {
	//	Constant.log("DebugTabGraph", "onStop");
		super.onStop();
	}
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
	//	Constant.log("DebugTabGraph", "onViewCreated");
		super.onViewCreated(view, savedInstanceState);
	}
	@Override
	public void onViewStateRestored(Bundle savedInstanceState) {
	//	Constant.log("DebugTabGraph", "onViewStateRestored");
		super.onViewStateRestored(savedInstanceState);
	}

	@Override
	public void setMenuVisibility(boolean menuVisible) {
	//	Constant.log("DebugTabGraph", "setMenuVisibility");
		super.setMenuVisibility(menuVisible);
	}
	@Override
	public void startActivity(Intent intent) {
	//	Constant.log("DebugTabGraph", "startActivity");
		super.startActivity(intent);
	}

	private void clearUI() {
		if (webview != null)	{
			webViewPlaceholder.removeView(webview);
			this.webview = null;
		}
		if (jsInterface != null)	{
			AJS.clearInstance();
			this.jsInterface = null;
		}
		uiEnabled = false;
	}
	protected void initUI( boolean activated ) {
		// Retrieve UI elements
	    // Initialize the WebView if necessary
	    if (this.webview == null && activated) {
	    	Log.d("+NEW", "webview" );
	    	this.webview = new WebView(cc);
	    	this.webview.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
	        this.webview.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
	        this.webview.setScrollbarFadingEnabled(true);

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

	   		final Activity activity = cc;
	   //		webview.setWebViewClient(new MyWebViewClient());
	   		this.webview.setWebViewClient(new WebViewClient());
	   		this.webview.setWebViewClient(new WebViewClient() {
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

	   		this.webview.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
	   		this.webview.setScrollbarFadingEnabled(true);

	   		this.webview.setWebChromeClient(new WebChromeClient() {
	   		   public void onProgressChanged(WebView view, int progress) {
	   		     // Activities and WebViews measure progress with different scales.
	   		     // The progress meter will automatically disappear when we reach 100%
	   		     activity.setProgress(progress * 1000);
	   		   }
	   		});
	   		jsInterface = new AJS(cc, this.webview);
	   		this.webview.addJavascriptInterface( jsInterface, "AJS");
	   		this.webview.loadUrl("file:///android_asset/oscyloskop.htm");
	    }
	    if(activated){
		    this.webViewPlaceholder = (FrameLayout) this.rootView.findViewById(R.id.webViewPlaceholder2);
		    if(this.webViewPlaceholder == null){
		  //  	Constant.log(Constant.TAG,"webViewPlaceholder null" );
		    }else{
		    	int cc = this.webViewPlaceholder.getChildCount();
		    	if(cc == 0){
		    		this.webViewPlaceholder.addView(this.webview);// Attach the WebView to its placeholder	
		    	}
		    }
		    uiEnabled = true;
	    }

	}

	// gdy zamkne panel serwisowy
    public void onDestroy() {
    	uiEnabled =false;
  //  	Constant.log("DebugTabGraph", "onDestroy");
    	super.onDestroy();
    }

	private void enableUI(boolean active ){
		((ToggleButton) rootView.findViewById(R.id.graph_reverse)).setEnabled(active);
		((ToggleButton) rootView.findViewById(R.id.graph_scale)).setEnabled(active);
		((ToggleButton) rootView.findViewById(R.id.graph_points)).setEnabled(active);
		((ToggleButton) rootView.findViewById(R.id.graph_columns)).setEnabled(active);
		((ToggleButton) rootView.findViewById(R.id.graph_lines)).setEnabled(active);
		((ToggleButton) rootView.findViewById(R.id.graph_high_speed)).setEnabled(active);
		((ToggleButton) rootView.findViewById(R.id.graph_reverse)).setEnabled(active);
		((Button) rootView.findViewById(R.id.graph_fps)).setEnabled(active);
		((Button) rootView.findViewById(R.id.graph_xsize)).setEnabled(active);		
	}	
	
	private class graph_button_click implements OnClickListener, OnCheckedChangeListener{
		private Context dbw;

		public graph_button_click(Context debugWindow){
			dbw = debugWindow;
		}
		
		private int toInt(String str, int def ){
			int res;
			try {
				res = Integer.parseInt(str);
			} catch (NumberFormatException e) {
				return def;
			}
			return res;
		}

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.graph_source:
	  	  		((ToggleButton) rootView.findViewById(R.id.graph_active)).setChecked(true);
				int graph_source	= DebugTabGraph.graph_source;
				show_dialog( new RunnableWithData(){
					@Override
					public void run() {
						DebugTabGraph.graph_source		= toInt(this.data,0);	
						Arduino ar							= Arduino.getInstance();
						virtualComponents.enable_analog(ar, DebugTabGraph.graph_source, DebugTabGraph.graph_speed, DebugTabGraph.graph_repeat);	
					}
	        	}, ""+graph_source);

				/*
	        	deviceManager dm = deviceManager.getInstance();
	        	dm.getDevices( devices.DEVICE_ANALOG );
	        	dm.getDevices( devices.DEVICE_ULTRA );
	           	live_analog_num		= 20;
	        	.getDevices( devices.DEVICE_ANALOG );
	        	virtualComponents.getDevices( ANALOG );
	        	device.liveEnable();
	*/
				break;
			case R.id.graph_speed:
	  	  		((ToggleButton) rootView.findViewById(R.id.graph_active)).setChecked(true);
				show_dialog( new RunnableWithData(){
					@Override
					public void run() {
						DebugTabGraph.graph_speed		= toInt(this.data, 50 );
						Arduino ar							= Arduino.getInstance();
						virtualComponents.enable_analog(ar, DebugTabGraph.graph_source, DebugTabGraph.graph_speed, DebugTabGraph.graph_repeat);
					}
	        	}, ""+DebugTabGraph.graph_speed);

				break;
			case R.id.graph_repeat:
	  	  		((ToggleButton) rootView.findViewById(R.id.graph_active)).setChecked(true);
				if(jsInterface!=null){
					show_dialog( new RunnableWithData(){
						@Override
						public void run() {

							DebugTabGraph.graph_repeat	= toInt(this.data, 1 );
							Arduino ar						= Arduino.getInstance();
				        	virtualComponents.enable_analog(ar, DebugTabGraph.graph_source, DebugTabGraph.graph_speed, DebugTabGraph.graph_repeat);	
						}
		        	}, ""+DebugTabGraph.graph_repeat);
				}
				break;
			case R.id.graph_xsize:
				if(jsInterface!=null){
					show_dialog( new RunnableWithData(){
						@Override
						public void run() {
							DebugTabGraph.graph_xsize		= toInt(this.data, 4);
							jsInterface.runJs("changex", ""+DebugTabGraph.graph_xsize);
						}
		        	}, ""+DebugTabGraph.graph_xsize);
				}
				break;

			case R.id.graph_fps:
				if(jsInterface!=null){
					show_dialog( new RunnableWithData(){
						@Override
						public void run() {
							DebugTabGraph.graph_fps		= toInt(this.data,10);
							jsInterface.runJs("changefps", ""+DebugTabGraph.graph_fps);
						}
		        	}, ""+DebugTabGraph.graph_fps);
				}
				break;
			}
		}

		@Override
		public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
			Arduino ar			= Arduino.getInstance();
			switch (buttonView.getId()) {
			case R.id.graph_active:
				Constant.log("graph_active","isChecked: " + isChecked );
				if(isChecked){
					initUI( true );
					enableUI(true);
				}else{
					enableUI(false);
					int graph_source9	= DebugTabGraph.graph_source;
					virtualComponents.disable_analog(ar, graph_source9 );
					if(jsInterface!=null){
						jsInterface.runJs("show_random", "0");
					}
					clearUI();
				}
				break;
			case R.id.graph_random:
	  	  		ToggleButton xb1 = (ToggleButton) rootView.findViewById(R.id.graph_active);
		  	  	if(isChecked){
		  	  		xb1.setChecked(true);
					int graph_source5	= DebugTabGraph.graph_source;
					virtualComponents.disable_analog(ar, graph_source5 );
					int graph_speed2 = DebugTabGraph.graph_speed;
					if(jsInterface!=null){
						jsInterface.runJs("show_random", ""+graph_speed2);	
					}
				}else{
		  	  		xb1.setChecked(false);
					if(jsInterface!=null){
						jsInterface.runJs("show_random", "0");
					}
				}
				break;

			case R.id.graph_reverse:
				if(jsInterface!=null){
					jsInterface.runJs("reverseY", isChecked? "1" : "0");
				}
				break;
			case R.id.graph_scale:
				if(jsInterface!=null){
					jsInterface.runJs("toggleLocalMin", isChecked? "1" : "0");
				}
				break;
			case R.id.graph_points:
				if(jsInterface!=null){
					jsInterface.runJs("dots", isChecked? "1" : "0" );
				}
				break;
			case R.id.graph_lines:
				if(jsInterface!=null){
					jsInterface.runJs("lines", isChecked? "1" : "0");
				}
				break;
			case R.id.graph_columns:
				if(jsInterface!=null){
					jsInterface.runJs("column", isChecked? "1" : "0");
				}
				break;
			case R.id.graph_high_speed:
				if(jsInterface!=null){
					jsInterface.runJs("sethighspeed", isChecked? "1" : "0");
				}
				break;
			}
		}
		public void show_dialog( final RunnableWithData onfinish, String defaultValue ){
			// custom dialog
			final Dialog dialog = new Dialog(dbw);
			dialog.setContentView(R.layout.dialog_int);
			dialog.setTitle("Podaj...");

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
	}
}