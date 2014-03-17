package com.barobot.debug;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ToggleButton;

import com.barobot.R;
import com.barobot.activity.DebugActivity;

public class DebugTabLeds extends Fragment {
	public int tab_id	= -1 ;
	private Activity cc;

    public DebugTabLeds(Activity debugActivity, int tabCommandsId) {
    //	Constant.log("DebugTabLeds", "init");
    	this.tab_id = tabCommandsId;
    	this.cc=debugActivity;
	}
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
		//Integer.toString(getArguments().getInt(DebugActivity.ARG_SECTION_NUMBER))
   // 	Constant.log("DebugTabLeds", "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
    }
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
	//	Constant.log("DebugTabLeds", "onCreateView");

		int lay = DebugActivity.layouts[tab_id];
		//View rootView = inflater.inflate( R.layout.fragment_device_list_dummy, container, false);
		View rootView = inflater.inflate( lay, container, false);

		button_toggle bt = new button_toggle();
		int[] togglers = {
				R.id.led1,
				R.id.led2,
				R.id.led3,
				R.id.led4,
				R.id.led5,
				R.id.led6,
				R.id.led7,
				R.id.led8,
				R.id.led9,
				R.id.led10
		};
		for(int i =0; i<togglers.length;i++){
			View w = rootView.findViewById(togglers[i]);
			String classname = w.getClass().getName();
			if( "android.widget.ToggleButton".equals( classname )){
				Button xb3 = (ToggleButton) rootView.findViewById(togglers[i]);	
				xb3.setOnClickListener(bt);			
			}	
		}
		ListView  led_list_box = (ListView ) rootView.findViewById(R.id.led_list);
		if( led_list_box == null){
	//		Constant.log("DebugTabDevices", "null2");
		}

		return rootView;
	}
    public int getDipsFromPixel(float pixels) {
        // Get the screen's density scale
        final float scale = getResources().getDisplayMetrics().density;
        // Convert the dps to pixels, based on density scale
        return (int) (pixels * scale + 0.5f);
    }
 
}
