package com.barobot.debug;

import com.barobot.DebugActivity;
import com.barobot.R;
import com.barobot.button_toggle;
import com.barobot.utils.Constant;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ToggleButton;

public class DebugTabLeds extends Fragment {
	public int tab_id	= -1 ;
	private Activity cc;

    public DebugTabLeds(Activity debugActivity, int tabCommandsId) {
    	Constant.log("DebugTabLeds", "init");
    	this.tab_id = tabCommandsId;
    	this.cc=debugActivity;
	}
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
		//Integer.toString(getArguments().getInt(DebugActivity.ARG_SECTION_NUMBER))
    	Constant.log("DebugTabLeds", "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
    }
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		Constant.log("DebugTabLeds", "onCreateView");

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
		return rootView;
	}
}
