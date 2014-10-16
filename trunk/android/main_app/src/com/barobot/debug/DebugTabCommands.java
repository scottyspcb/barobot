package com.barobot.debug;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.barobot.R;
import com.barobot.activity.DebugActivity;
import com.barobot.common.Initiator;
import com.barobot.common.constant.Constant;
import com.barobot.common.interfaces.HardwareState;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.sofa.route.CommandRoute;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

public class DebugTabCommands extends Fragment {
	public int tab_id	= -1 ;
	private DebugActivity cc;

    public DebugTabCommands(DebugActivity debugActivity, int tabCommandsId) {
    	this.tab_id = tabCommandsId;
    	this.cc=debugActivity;
	}
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

	private static Map<Integer, String> buttonToCommand = new HashMap<Integer, String>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
	//	Initiator.logger.i("DebugTabCommands", "onCreateView");

		int lay = DebugActivity.layouts[tab_id];
		//View rootView = inflater.inflate( R.layout.fragment_device_list_dummy, container, false);
		View rootView = inflater.inflate( lay, container, false);
		
		buttonToCommand.put( R.id.max_z, "max_z" );
		buttonToCommand.put( R.id.min_z, "min_z" );
		buttonToCommand.put( R.id.max_x, "max_x" );
		buttonToCommand.put( R.id.max_y, "max_y" );
		buttonToCommand.put( R.id.min_x, "min_x" );
		buttonToCommand.put( R.id.min_y, "min_y" );
		buttonToCommand.put( R.id.unlock, "unlock" );
		buttonToCommand.put( R.id.pacpac, "pacpac" );
		buttonToCommand.put( R.id.kalibrujx, "kalibrujx" );
		buttonToCommand.put( R.id.disablez, "disablez" );
		buttonToCommand.put( R.id.disabley, "disabley" );	
		buttonToCommand.put( R.id.disablex, "disablex" );	

		buttonToCommand.put( R.id.set_robot_id, "set_robot_id" );
		buttonToCommand.put( R.id.reset1, "reset1" );
		buttonToCommand.put( R.id.reset, "reset" );
		buttonToCommand.put( R.id.reset2, "reset2" );
		buttonToCommand.put( R.id.reset3, "reset3" );
		buttonToCommand.put( R.id.reset4, "reset4" );

		
		buttonToCommand.put( R.id.find_bottles, "find_bottles" );
		buttonToCommand.put( R.id.reset_margin, "reset_margin" );
		buttonToCommand.put( R.id.analog_temp, "analog_temp" );
		buttonToCommand.put( R.id.clear_queue, "clear_queue" );
		buttonToCommand.put( R.id.reset_serial, "reset_serial" );
		buttonToCommand.put( R.id.wait_for_cup, "wait_for_cup" );
		
		buttonToCommand.put( R.id.rb, "rb" );
		buttonToCommand.put( R.id.rb2, "rb2" );
		buttonToCommand.put( R.id.scann_leds, "scann_leds" );
		buttonToCommand.put( R.id.led_green_on, "led_green_on" );
		buttonToCommand.put( R.id.led_blue_on, "led_blue_on" );
		buttonToCommand.put( R.id.led_red_on, "led_red_on" );
		buttonToCommand.put( R.id.set_x_1000, "set_x_1000" );
		buttonToCommand.put( R.id.set_x_100, "set_x_100" );
		buttonToCommand.put( R.id.set_x_10, "set_x_10" );
		buttonToCommand.put( R.id.set_x10, "set_x10" );
		buttonToCommand.put( R.id.set_x100, "set_x100" );
		buttonToCommand.put( R.id.set_x1000, "set_x1000" );
		buttonToCommand.put( R.id.set_y_600, "set_y_600" );
		buttonToCommand.put( R.id.set_y_100, "set_y_100" );
		buttonToCommand.put( R.id.set_y_10, "set_y_10" );
		buttonToCommand.put( R.id.set_y10, "set_y10" );
		buttonToCommand.put( R.id.set_y100, "set_y100" );
		buttonToCommand.put( R.id.set_y600, "set_y600" );
		buttonToCommand.put( R.id.goToNeutralY, "goToNeutralY" );
		buttonToCommand.put( R.id.kalibrujy, "kalibrujy" );
		buttonToCommand.put( R.id.machajx, "machajx" );
		buttonToCommand.put( R.id.machajy, "machajy" );
		buttonToCommand.put( R.id.machajz, "machajz" );
		buttonToCommand.put( R.id.wznow, "wznow" );
		buttonToCommand.put( R.id.losujx, "losujx" );
		buttonToCommand.put( R.id.losujy, "losujy" );
		buttonToCommand.put( R.id.fill5000, "fill5000" );
		buttonToCommand.put( R.id.index_names, "index_names" );
		buttonToCommand.put( R.id.auto_repair, "auto_repair" );

		for (Entry<Integer, String> entry : buttonToCommand.entrySet()) {
			int id =  entry.getKey();
			final String command =  entry.getValue();
			View w = rootView.findViewById( id );
			if( w == null){
				Initiator.logger.i(Constant.TAG,"pomijam: "+ id );
			}else{
				String classname = w.getClass().getName();
				if( "android.widget.Button".equals( classname ) || "android.widget.ToggleButton".equals( classname )){
					Button xb1 = (Button) rootView.findViewById(id);	
					xb1.setOnClickListener( new OnClickListener() {
						@Override
						public void onClick(View v) {

							Log.i("button click","click");
							new Thread( new Runnable(){
								@Override
								public void run() {
									Log.i("button click1","exec "+ command + " start");
									CommandRoute.runCommand2(command);	
									Log.i("button click1","exec "+ command + " end");
								}}).start();
						}
					});			
				}
			}
		}

		button_click bc = new button_click( this.cc );
		int[] buttons = {
				R.id.download_database,
				R.id.reset_database,
				R.id.firmware_download,
				R.id.firmware_download_manual,
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
