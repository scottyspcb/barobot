package com.barobot.debug;

import com.barobot.DebugActivity;
import com.barobot.R;
import com.barobot.utils.Constant;
import com.barobot.webview.AJS;
import android.app.Activity;
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
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ToggleButton;

/**
 * A dummy fragment representing a section of the app
 */
public class DebugTabGraph extends Fragment {

	public int tab_id	= -1 ;
	private Activity cc;
	public WebView webview	=null;
	public FrameLayout webViewPlaceholder;
	private int live_analog_num		= 0;
	private int live_analog_time	= 10;
	private int live_analog_repeat	= 2;
	private View rootView;

    public DebugTabGraph(Activity debugActivity, int tabCommandsId) {
    	Constant.log("DebugTabGraph", "init");
    	this.tab_id = tabCommandsId;
    	this.cc=debugActivity;
	}
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	Constant.log("DebugTabGraph", "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState!=null && !savedInstanceState.isEmpty()){
        	 webview.restoreState(savedInstanceState);
        }
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		Constant.log("DebugTabGraph", "onCreateView");
		//View rootView = inflater.inflate( R.layout.fragment_device_list_dummy, container, false);
		View rootView	= inflater.inflate(  DebugActivity.layouts[tab_id], container, false);
		this.rootView	= rootView;
		initUI();

		button_click bc = new button_click( this.cc );
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
				Constant.log(Constant.TAG,"pomijam: "+ buttons[i] );
				continue;
			}
			String classname = w.getClass().getName();
			if( "android.widget.Button".equals( classname ) || "android.widget.ToggleButton".equals( classname ) ){
				Button xb1 = (Button) rootView.findViewById(buttons[i]);
				xb1.setOnClickListener(bc);			
			}
		}
		ToggleButton xb1 = (ToggleButton) rootView.findViewById(R.id.graph_lines);
		xb1.setChecked(true);
		
		ToggleButton xb2 = (ToggleButton) rootView.findViewById(R.id.graph_scale);
		xb2.setChecked(true);

		return rootView;
	}

    public void onDestroy() {
    	Constant.log("DebugTabGraph", "onDestroy");
    	super.onDestroy();
    }

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
	}
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		Constant.log("DebugTabGraph", "onConfigurationChanged");
		super.onConfigurationChanged(newConfig);
		if (webview != null)	{
		  // Remove the WebView from the old placeholder
			webViewPlaceholder.removeView(webview);
		}
		initUI();
	}
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		Constant.log("DebugTabGraph", "onContextItemSelected");
		return super.onContextItemSelected(item);
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Constant.log("DebugTabGraph", "onCreate");
		super.onCreate(savedInstanceState);
	}
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		Constant.log("DebugTabGraph", "onCreateOptionsMenu");
		super.onCreateOptionsMenu(menu, inflater);
		cc.getMenuInflater().inflate(R.menu.web, menu);

	}
	@Override
	public void onDestroyOptionsMenu() {
		// TODO Auto-generated method stub
		Constant.log("DebugTabGraph", "onDestroyOptionsMenu");
		super.onDestroyOptionsMenu();
	}
	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		Constant.log("DebugTabGraph", "onDestroyView");
		if (webview != null){
			webViewPlaceholder.removeView(webview);	  // Remove the WebView from the old placeholder
		}
		super.onDestroyView();
	}
	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		Constant.log("DebugTabGraph", "onDetach");
		super.onDetach();
	}
	@Override
	public void onHiddenChanged(boolean hidden) {
		// TODO Auto-generated method stub
		super.onHiddenChanged(hidden);
		Constant.log("DebugTabGraph", "onHiddenChanged");
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		Constant.log("DebugTabGraph", "onOptionsItemSelected");
		return super.onOptionsItemSelected(item);
	}
	@Override
	public void onOptionsMenuClosed(Menu menu) {
		// TODO Auto-generated method stub
		Constant.log("DebugTabGraph", "onOptionsMenuClosed");
		super.onOptionsMenuClosed(menu);
	}
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		Constant.log("DebugTabGraph", "onPause");
		super.onPause();
	}
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		Constant.log("DebugTabGraph", "onPrepareOptionsMenu");
		super.onPrepareOptionsMenu(menu);
	}
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		Constant.log("DebugTabGraph", "onResume");
		super.onResume();
	}
	@Override
	public void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		Constant.log("DebugTabGraph", "onSaveInstanceState");
		webview.saveState(outState);
	}
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		Constant.log("DebugTabGraph", "onStart");
		super.onStart();
	}
	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		Constant.log("DebugTabGraph", "onStop");
		super.onStop();
	}
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Constant.log("DebugTabGraph", "onViewCreated");
		super.onViewCreated(view, savedInstanceState);
	}
	@Override
	public void onViewStateRestored(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Constant.log("DebugTabGraph", "onViewStateRestored");
		super.onViewStateRestored(savedInstanceState);
	}

	@Override
	public void setMenuVisibility(boolean menuVisible) {
		// TODO Auto-generated method stub
		Constant.log("DebugTabGraph", "setMenuVisibility");
		super.setMenuVisibility(menuVisible);
	}
	@Override
	public void startActivity(Intent intent) {
		// TODO Auto-generated method stub
		Constant.log("DebugTabGraph", "startActivity");
		super.startActivity(intent);
	}

	protected void initUI() {
		// Retrieve UI elements
	    // Initialize the WebView if necessary
	    if (this.webview == null) {
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
	   		this.webview.addJavascriptInterface(new AJS(cc, this.webview), "AJS");
	   		this.webview.loadUrl("file:///android_asset/oscyloskop.htm");
	    }
	    if(this.webview == null){
	    	Constant.log(Constant.TAG,"webview null" );
	    }

	    View bbb = cc.findViewById(R.id.webViewPlaceholder2);
	    if(bbb == null){
	    	Constant.log(Constant.TAG,"bbb null" );
	    }
	    this.webViewPlaceholder = (FrameLayout) this.rootView.findViewById(R.id.webViewPlaceholder2);

	    if(this.webViewPlaceholder == null){
	    	Constant.log(Constant.TAG,"webViewPlaceholder null" );
	    }else{
	    	this.webViewPlaceholder.addView(this.webview);// Attach the WebView to its placeholder	
	    }
	}

}