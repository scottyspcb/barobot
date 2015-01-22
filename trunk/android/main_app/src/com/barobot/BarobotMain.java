package com.barobot;

import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.barobot.android.Android;
import com.barobot.android.InternetHelpers;
import com.barobot.common.Initiator;
import com.barobot.hardware.Arduino;
import com.barobot.other.LangTool;
import com.barobot.other.StartupException;

public class BarobotMain extends Activity {
	private static BarobotMain instance;
	public static boolean canStart				= true;
	public static Exception lastException		= null;
	public static BarobotMain getInstance() {
		return instance;
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if( Arduino.getInstance() != null ){
		// 	barobot = Arduino.getInstance().barobot;
		}
		if(instance == null){
			instance = this; // Set up the window layout
		}
		startUp();
	}

	protected void setTextViewText(String text, int id){
		TextView tView = (TextView) findViewById(id);
		tView.setText(text);
	}

    protected void ButtonEnabled(boolean enabled, int id){
		Button okButton = (Button) findViewById(id);
		okButton.setEnabled(enabled);
	}

	@Override
	protected void onStart() {			// start this activity
		super.onStart();
		AppInvoker.getInstance().onStart();
	}

	public void setFullScreen() {
		int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		if (currentapiVersion >= 19 ){	// kitkat
			getWindow().getDecorView().setSystemUiVisibility(
			          View.SYSTEM_UI_FLAG_LAYOUT_STABLE
			        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
			        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
			        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
			        | View.SYSTEM_UI_FLAG_FULLSCREEN
			        | View.SYSTEM_UI_FLAG_IMMERSIVE);			
		}
	}

	public void changeLanguage(String langCode) {
		Resources res = this.getBaseContext().getResources();
		DisplayMetrics dm = res.getDisplayMetrics();
		android.content.res.Configuration conf = res.getConfiguration();
		conf.locale = new Locale(langCode);
		res.updateConfiguration(conf, dm);
		//String langCode = Locale.getDefault().getLanguage();	// system alng i.e. "pl"
		LangTool.setLanguage(langCode);
	}
	private void showRaportError(final Exception lastException2) {
		final String msg = Android.tttt( lastException2 );
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Exception Occured");
        dialog.setMessage(msg);
        dialog.setPositiveButton("Raport Error", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				InternetHelpers.raportError( lastException2, msg );
			}
		});
        dialog.setNegativeButton("Close", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});
        dialog.create().show();
	}

	private void startUp(){
		/*
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
		    @Override
		    public void uncaughtException(Thread thread, Throwable ex)
		    {
		        new Thread() {
		            @Override
		            public void run() {
		                Looper.prepare();
		                Toast.makeText(getApplicationContext(), "Application crashed", Toast.LENGTH_LONG).show();
		                Looper.loop();
		            }
		        }.start();

		        try
		        {
		            Thread.sleep(4000); // Let the Toast display before app will get shutdown
		        }
		        catch (InterruptedException e)
		        {
		            // Ignored.
		        }
		    }
		});
		*/
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		try {
			AppInvoker.createInstance(this);
			AppInvoker.getInstance().onCreate();
		} catch (StartupException e) {
			Initiator.logger.e("BarobotMain.onCreate", "StartupException", e);
			canStart = false;	
			lastException = e;
		} catch (NullPointerException e) {
			Initiator.logger.e("BarobotMain.onCreate", "NullPointerException", e);
			canStart = false;
			lastException = e;
		} catch (RuntimeException e) {
			Initiator.logger.e("RuntimeException.onCreate", "NullPointerException", e);
			canStart = false;
			lastException = e;
		} catch (Exception e) {
			Initiator.logger.e("BarobotMain.onCreate", "Exception", e);
			canStart = false;
			lastException = e;
		}
		if(canStart){
			AppInvoker.getInstance().onResume();
		}else{
			showRaportError( lastException );
		}
	}
}

/*
List<Translated_name> l = Model.fetchQuery(ModelQuery.select().from(Translated_name.class).where(
		C.and(
				C.eq("element_id", id),
				C.eq("table_name", table_name)
			)).getQuery(),Translated_name.class);

for( Translated_name tn : l){
	Log.e("translateName1", "translated " + tn.translated );
	Log.e("translateName1", "translated " + tn.language_id );
}
Log.e("translateName1", "------------------------");


Query query = new Query("SELECT * FROM Translated_name WHERE `element_id` ='"+id+"' and `table_name`='"+table_name+"' " + order);

Query query = ModelQuery.select().from(Translated_name.class).where(
		C.and(
				C.eq("element_id", id),
				C.eq("table_name", table_name)
			)).orderBy(order).getQuery();

List<Translated_name> l2 = Model.fetchQuery(query,Translated_name.class);

for( Translated_name tn : l2){
	Log.e("translateName2", "translated " + tn.translated );
	Log.e("translateName2", "translated " + tn.language_id );
}		
Log.e("translateName2", "------------------------");
*/

