package com.barobot.debug;

import com.barobot.R;
import com.barobot.activity.DebugActivity;
import com.barobot.common.Initiator;
import com.barobot.common.constant.Constant;
import com.barobot.common.interfaces.HardwareState;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;

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
    //	Initiator.logger.i("DebugTabCommands", "init");
    	this.tab_id = tabCommandsId;
    	this.cc=debugActivity;
	}
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
		//Integer.toString(getArguments().getInt(DebugActivity.ARG_SECTION_NUMBER))
    //	Initiator.logger.i("DebugTabCommands", "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
    }
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
	//	Initiator.logger.i("DebugTabCommands", "onCreateView");

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
				R.id.rb,
				R.id.wznow,
				R.id.rb2,
				R.id.led_red_on,
				R.id.scann_leds,		
				R.id.led_green_on,
				R.id.led_blue_on,
				R.id.i2c_test,
				R.id.clear_queue,		
				R.id.scann_i2c,
				R.id.firmware_download,		
				R.id.firmware_burn,
				R.id.reset_margin,
				R.id.analog_temp,
				R.id.reset_serial,
				R.id.download_database,
				R.id.smile,
				R.id.find_bottles,
				R.id.pacpac,
				R.id.reset1,
				R.id.reset2,
				R.id.reset3,		
				R.id.reset4,		
				R.id.goto_max_x,
				R.id.goto_min_x,
			};

		for(int i =0; i<buttons.length;i++){
			View w = rootView.findViewById(buttons[i]);
			if( w == null){
				Initiator.logger.i(Constant.TAG,"pomijam: "+ buttons[i] );
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
				R.id.auto_fill_on_ready
		};
		for(int i =0; i<togglers.length;i++){
			View w = rootView.findViewById(togglers[i]);
			String classname = w.getClass().getName();
			if( "android.widget.ToggleButton".equals( classname )){
				Button xb3 = (ToggleButton) rootView.findViewById(togglers[i]);	
				xb3.setOnClickListener(bt);
			}	
		}
		ToggleButton toggle = (ToggleButton)rootView.findViewById(R.id.auto_fill_on_ready);		
		toggle.setChecked(true);

		TextView ttt = (TextView)rootView.findViewById(R.id.position_z);
		
		BarobotConnector barobot = Arduino.getInstance().barobot;
		HardwareState state = barobot.state;
		if(ttt!=null){
			String posz = "" + state.getInt( "POSZ",0);
			ttt.setText(posz);
		}
		ttt = (TextView)rootView.findViewById(R.id.position_y);
		if(ttt!=null){
			String posy = "" + state.get( "POSY","0");
			ttt.setText(posy);
		}
		
		ttt = (TextView)rootView.findViewById(R.id.position_x);
		if(ttt!=null){
			String posx = "" + barobot.driver_x.getSPos();
			ttt.setText(posx);
		}
		return rootView;
	}
}
