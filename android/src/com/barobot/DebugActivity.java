package com.barobot;

import java.util.Locale;
import java.util.Vector;

import com.barobot.debug.DebugTabBottles;
import com.barobot.debug.DebugTabCommands;
import com.barobot.debug.DebugTabDevices;
import com.barobot.debug.DebugTabGraph;
import com.barobot.debug.DebugTabLeds;
import com.barobot.debug.DebugTabLog;
import com.barobot.drinks.RunnableWithData;
import com.barobot.utils.Constant;

import android.app.ActionBar;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

public class DebugActivity extends FragmentActivity implements	ActionBar.TabListener {
	private static DebugActivity instance;
	public static final int INTENT_NAME = 14;
	public static final String ARG_SECTION_NUMBER = "section_number";

	public static final int TAB_COMMANDS_ID	= 0;
	public static final int TAB_DEVICES_ID	= 1;
	public static final int TAB_LEDS_ID		= 2;
	public static final int TAB_BOTTLES_ID	= 3;
	public static final int TAB_GRAPH_ID	= 4;	
	public static final int TAB_LOG_ID		= 5;

	public static int[] layouts= {
		R.layout.debug_tab_commands,
		R.layout.debug_tab_devices,
		R.layout.debug_tab_leds,
		R.layout.debug_tab_bottles,
		R.layout.debug_tab_graph,
		R.layout.debug_tab_log
	};
	
	public Vector<Fragment> objVector = new Vector<Fragment>(10);

	public DebugActivity(){
		Constant.log("DebugActivity", "init");
		
	}
	
	public static DebugActivity getInstance() {
		return instance;
	}

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		objVector.add(DebugActivity.TAB_COMMANDS_ID,  	new DebugTabCommands( this, DebugActivity.TAB_COMMANDS_ID ));
		objVector.add(DebugActivity.TAB_DEVICES_ID,  	new DebugTabDevices( this, DebugActivity.TAB_DEVICES_ID ));
		objVector.add(DebugActivity.TAB_LEDS_ID,  		new DebugTabLeds( this, DebugActivity.TAB_LEDS_ID ));
		objVector.add(DebugActivity.TAB_BOTTLES_ID,  	new DebugTabBottles( this, DebugActivity.TAB_BOTTLES_ID ));
		objVector.add(DebugActivity.TAB_GRAPH_ID,  		new DebugTabGraph( this, DebugActivity.TAB_GRAPH_ID ));
		objVector.add(DebugActivity.TAB_LOG_ID,  		new DebugTabLog( this, DebugActivity.TAB_LOG_ID ));
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_debug);
		DebugActivity.instance = this;

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		menu.clear();
		getMenuInflater().inflate(R.menu.device_list, menu);
		Constant.log("Debug", "onPrepareOptionsMenu");
		int position = mViewPager.getCurrentItem();
		Fragment fff = null;
		Constant.log("Debug", "onPrepareOptionsMenu " + position);
		try {
			fff = objVector.get(position);
			fff.onCreateOptionsMenu(menu, getMenuInflater());
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		}
		return true;
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//return false;
		return true;		// wszystko dzieje sie w onPrepareOptionsMenu
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
		FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {
		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {// position zaczyna od 0
			// getItem is called to instantiate the fragment for the given page.
			// Return a DebugTabGraph (defined as a static inner class
			// below) with the page number as its lone argument.
			Bundle args = new Bundle();
			args.putInt(DebugActivity.ARG_SECTION_NUMBER, position + 1);
			Constant.log("DebugActivity", "pos " + position);
			Fragment fff = null;
			try {
				fff = objVector.get(position);
				fff.setArguments(args);
				return fff;
			} catch (ArrayIndexOutOfBoundsException e) {
				// TODO: handle exception
			}
			return fff;		//todo jakies zabezpieczenia
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 6;
		}

		@Override
		public CharSequence getPageTitle(int position) {	// position zaczyna od 0
			Locale l = Locale.getDefault();
			switch (position) {
			case DebugActivity.TAB_COMMANDS_ID:
				return getString(R.string.title_section1).toUpperCase(l);
			case DebugActivity.TAB_DEVICES_ID:
				return getString(R.string.title_section2).toUpperCase(l);
			case DebugActivity.TAB_LEDS_ID:
				return getString(R.string.title_section3).toUpperCase(l);
			case DebugActivity.TAB_BOTTLES_ID:
				return getString(R.string.title_section4).toUpperCase(l);
			case DebugActivity.TAB_GRAPH_ID:
				return getString(R.string.title_section5).toUpperCase(l);
			case DebugActivity.TAB_LOG_ID:
				return getString(R.string.title_section6).toUpperCase(l);
			}
			return null;
		}
	}

	public void update(String name, String value) {
		if( "LENGTHX".equals(name)){
			setText( R.id.dlugosc_x, value, false );
		}else if( "LENGTHY".equals(name)){
			setText( R.id.dlugosc_y, value, false );
		}else if("LENGTHZ".equals(name)){
			setText( R.id.dlugosc_z, value, false );
		}else if("POSX".equals(name)){
			setText( R.id.position_x, value, false );
		}else if("POSY".equals(name) ){
			setText( R.id.position_y, value, false );
		}else if("POSZ".equals(name) ){
			setText( R.id.position_z, value, false );		
		}else if("LED1".equals(name) ){
			setChecked( R.id.led1, "ON".equals(value) );
		}else if("LED2".equals(name) ){
			setChecked( R.id.led2, "ON".equals(value) );
		}else if("LED3".equals(name) ){
			setChecked( R.id.led3, "ON".equals(value) );
		}else if("LED4".equals(name) ){
			setChecked( R.id.led4, "ON".equals(value) );
		}else if("LED5".equals(name) ){
			setChecked( R.id.led5, "ON".equals(value) );
		}else if("LED6".equals(name) ){
			setChecked( R.id.led6, "ON".equals(value) );
		}else if("LED7".equals(name) ){
			setChecked( R.id.led7, "ON".equals(value) );
		}else if("LED8".equals(name) ){
			setChecked( R.id.led8, "ON".equals(value) );
		}else if("LED9".equals(name) ){
			setChecked( R.id.led9, "ON".equals(value) );
		}else if("LED10".equals(name) ){
			setChecked( R.id.led10, "ON".equals(value) );
		}
	}

	// Any update to UI can not be carried out in a non UI thread like the one
	// used
	// for Server. Hence runOnUIThread is used.
	public void setText( int target, final String result, boolean now ) {
		if (result != null) {
			final TextView text_field = (TextView) findViewById(target);
			if(now){
				if (text_field != null) {
					text_field.setText(result);
				}
			}else{
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
		// 	if (result != null) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				ToggleButton tb			= (ToggleButton) findViewById(target);
				if (tb != null) {
					tb.setChecked(equals);
				}
			}
		});
	}
}

/*
 * 
 * 
	public View getObject(String namespace, String mDrawableName) {
		int resID = getResources().getIdentifier(mDrawableName, namespace,getPackageName());
		return findViewById(resID);
	}

	public int getResource(String namespace, String mDrawableName) {
		int resID = getResources().getIdentifier(mDrawableName, namespace,getPackageName());
		return resID;
	}*/
