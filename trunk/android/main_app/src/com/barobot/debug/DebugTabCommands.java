package com.barobot.debug;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ToggleButton;

import com.barobot.BarobotMain;
import com.barobot.R;
import com.barobot.activity.DebugActivity;
import com.barobot.common.Initiator;
import com.barobot.common.constant.Constant;
import com.barobot.common.interfaces.HardwareState;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.sofa.route.CommandRoute;

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

//	public static List<Map<String,String>> data = new ArrayList<Map<String,String>>();
	private static LinkedHashMap<String, String> analog_list = new LinkedHashMap<String, String>();
	private static KeyValueAdapter kva = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
	//	Initiator.logger.i("DebugTabCommands", "onCreateView");
		BarobotConnector barobot = Arduino.getInstance().barobot;
		HardwareState state = barobot.state;
		int lay = DebugActivity.layouts[tab_id];
		//View rootView = inflater.inflate( R.layout.fragment_device_list_dummy, container, false);
		View rootView = inflater.inflate( lay, container, false);

		Map<Integer, String> buttonToCommand = new HashMap<Integer, String>();


		buttonToCommand.put( R.id.max_z, "z_up" );
		buttonToCommand.put( R.id.min_z, "z_down" );
		buttonToCommand.put( R.id.max_x, "max_x" );
		buttonToCommand.put( R.id.max_y, "y_back" );
		buttonToCommand.put( R.id.min_x, "min_x" );
		buttonToCommand.put( R.id.min_y, "y_front" );
		buttonToCommand.put( R.id.unlock, "unlock" );
		buttonToCommand.put( R.id.kalibrujx, "kalibrujx" );
		buttonToCommand.put( R.id.disablez, "disablez" );
		buttonToCommand.put( R.id.disabley, "disabley" );	
		buttonToCommand.put( R.id.disablex, "disablex" );	

		buttonToCommand.put( R.id.reset1, "reset1" );
		buttonToCommand.put( R.id.reset, "reset" );
		buttonToCommand.put( R.id.reset2, "reset2" );
		buttonToCommand.put( R.id.reset3, "reset3" );
		buttonToCommand.put( R.id.reset4, "reset4" );

		buttonToCommand.put( R.id.find_bottles, "find_bottles" );
		buttonToCommand.put( R.id.reset_margin, "reset_margin" );
		buttonToCommand.put( R.id.analog_temp, "analogs" );
		buttonToCommand.put( R.id.clear_queue, "clear_queue" );
	//	buttonToCommand.put( R.id.reset_serial, "reset_serial" );
		buttonToCommand.put( R.id.reset_serial, "command_renew_serial" );

		buttonToCommand.put( R.id.wait_for_cup, "wait_for_cup" );
		buttonToCommand.put( R.id.set_x_1000, "set_x_1000" );
		buttonToCommand.put( R.id.set_x_100, "set_x_100" );
		buttonToCommand.put( R.id.set_x_10, "set_x_10" );
		buttonToCommand.put( R.id.set_x10, "set_x10" );
		buttonToCommand.put( R.id.set_x100, "set_x100" );
		buttonToCommand.put( R.id.set_x1000, "set_x1000" );
		buttonToCommand.put( R.id.kalibrujy, "kalibrujy" );
		buttonToCommand.put( R.id.machajx, "machajx" );
		buttonToCommand.put( R.id.machajy, "machajy" );
		buttonToCommand.put( R.id.machajz, "machajz" );
		buttonToCommand.put( R.id.fill5000, "fill5000" );
		buttonToCommand.put( R.id.index_names, "index_names" );
		buttonToCommand.put( R.id.auto_repair, "auto_repair" );

		assignButtons(buttonToCommand, rootView);
		
		int autofill		= state.getInt("AUTOFILL", 1 );

		ToggleButton tbng2 = (ToggleButton)rootView.findViewById(R.id.auto_fill_on_ready);		
		tbng2.setChecked( (autofill == 1) ? true : false);

	

		button_click bc = new button_click( this.cc );
		int[] buttons = {
				R.id.download_database,
				R.id.reset_database,
				R.id.firmware_download,
				R.id.firmware_download_manual,
				R.id.new_robot_id,
				R.id.force_app_update
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

		analog_list.put("POSX", ""+barobot.x.getSPos());
		analog_list.put("POSY", ""+state.get( "POSY","0"));
		analog_list.put("POSZ", ""+state.getInt( "POSZ",0));		
	
        kva = new KeyValueAdapter(rootView.getContext(), analog_list);
        ListView analog_spinner = (ListView) rootView.findViewById(R.id.analog_spinner);
        analog_spinner.setAdapter(kva);
		return rootView;
	}

	public static void assignButtons(Map<Integer, String> buttonToCommand2, View rootView) {
		for (Entry<Integer, String> entry : buttonToCommand2.entrySet()) {
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
	}
	public synchronized static void updateValue( final String name, final String value ){
		if(kva !=null){
			BarobotMain.getInstance().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if(kva !=null){
						synchronized(kva){
							analog_list.put(name, value);
							kva.notifyDataSetChanged();
						}
					}
				}
			});
		}
	}
}
