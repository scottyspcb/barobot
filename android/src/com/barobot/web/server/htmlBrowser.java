package com.barobot.web.server;

import com.barobot.activity.BarobotMain;

public class htmlBrowser {
	private BarobotMain m	=null;
	public htmlBrowser(BarobotMain barobotMain) {
		this.m=barobotMain;
	}
	//console.log("zmienionych elementow przez ("+ selector +"): " + affected.length);
	public void startPage() {
	//	String aa = htmlBrowser.readRawTextFile(this.m, R.raw.main_page);
	//	this.m.webview.loadUrl("http://google.pl");
	//	this.m.webview.loadUrl("file:///android_asset/test.txt");
		this.m.webview.loadUrl("http://localhost:8000");
	//	String tplcc = this.fetchTplVel("scroll_view", context );
	//	String tplcc = this.fetchTplVel("main_page_test", context );
	//	Log.d("+HTML",tplcc);
//		this.m.webview.loadDataWithBaseURL("file:///android_asset/", tplcc, "text/html", "UTF-8", null );
	}
}
