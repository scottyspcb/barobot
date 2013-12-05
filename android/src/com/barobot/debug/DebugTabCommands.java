package com.barobot.debug;

import com.barobot.DebugActivity;
import com.barobot.R;
import com.barobot.R.id;
import com.barobot.hardware.virtualComponents;
import com.barobot.utils.Constant;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

public class DebugTabCommands extends Fragment {
	public int tab_id	= -1 ;
	private DebugActivity cc;

    public DebugTabCommands(DebugActivity debugActivity, int tabCommandsId) {
    	Constant.log("DebugTabCommands", "init");
    	this.tab_id = tabCommandsId;
    	this.cc=debugActivity;
	}
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
		//Integer.toString(getArguments().getInt(DebugActivity.ARG_SECTION_NUMBER))
    	Constant.log("DebugTabCommands", "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
    }
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		Constant.log("DebugTabCommands", "onCreateView");

		int lay = DebugActivity.layouts[tab_id];
		//View rootView = inflater.inflate( R.layout.fragment_device_list_dummy, container, false);
		View rootView = inflater.inflate( lay, container, false);

		button_click bc = new button_click( this.cc );
		int[] buttons = {
				R.id.kalibrujx,
				R.id.kalibrujy,
				R.id.kalibrujz,
				R.id.max_x,
				R.id.min_x,		
				R.id.max_y,
				R.id.min_y,
				R.id.max_z,
				R.id.min_z,			
				R.id.machajx,
				R.id.machajy,
				R.id.machajz,
				R.id.losujx,
				R.id.losujy,
				R.id.set_x1000,
				R.id.set_x100,
				R.id.set_x10,
				R.id.set_x_1000,
				R.id.set_x_100,
				R.id.set_x_10,
				R.id.set_y_600,
				R.id.set_y_100,
				R.id.set_y_10,
				R.id.set_y10,
				R.id.set_y100,
				R.id.set_y600,
				R.id.fill5000,
				R.id.set_bottle,
				R.id.set_neutral_y,
				R.id.goToNeutralY,
				R.id.unlock,
				R.id.disablez,
				R.id.disabley,
				R.id.enabley,
				R.id.reset_carret,
				R.id.smile,
				R.id.find_bottles,
				R.id.pacpac
			};
		for(int i =0; i<buttons.length;i++){
			View w = rootView.findViewById(buttons[i]);
			if( w == null){
				Constant.log(Constant.TAG,"pomijam: "+ buttons[i] );
				continue;
			}
			String classname = w.getClass().getName();
			if( "android.widget.Button".equals( classname )){
				Button xb1 = (Button) rootView.findViewById(buttons[i]);	
				xb1.setOnClickListener(bc);			
			}
			if( "android.widget.ToggleButton".equals( classname )){
				Button xb1 = (Button) rootView.findViewById(buttons[i]);	
				xb1.setOnClickListener(bc);			
			}
		}
		button_toggle bt = new button_toggle();
		int[] togglers = {
				R.id.wagi_live,
				R.id.need_glass,
				R.id.auto_fill
		};
		for(int i =0; i<togglers.length;i++){
			View w = rootView.findViewById(togglers[i]);
			String classname = w.getClass().getName();
			if( "android.widget.ToggleButton".equals( classname )){
				Button xb3 = (ToggleButton) rootView.findViewById(togglers[i]);	
				xb3.setOnClickListener(bt);
			}	
		}
		cc.setText( R.id.position_z, virtualComponents.get( "POSZ","0"), true );
		cc.setText( R.id.position_y, virtualComponents.get( "POSY","0"), true );
		cc.setText( R.id.position_x, virtualComponents.get( "POSX","0"), true );

		return rootView;
	}
}