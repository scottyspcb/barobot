package com.barobot.debug;

import java.util.HashMap;
import java.util.Map;

import yuku.ambilwarna.AmbilWarnaDialog;
import yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;

import com.barobot.R;
import com.barobot.activity.DebugActivity;
import com.barobot.common.Initiator;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.hardware.devices.i2c.I2C_Device_Imp;


public class DebugTabLeds extends Fragment {
	public int tab_id	= -1 ;
	private int lastcolor = 0xff000000;
	private View rootView;

	public DebugTabLeds(Activity debugActivity, int tabCommandsId) {
    	Initiator.logger.i("DebugTabLeds", "init");
    	this.tab_id = tabCommandsId;
	}
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
		//Integer.toString(getArguments().getInt(DebugActivity.ARG_SECTION_NUMBER))
   // 	Initiator.logger.i("DebugTabLeds", "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
    }	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		Initiator.logger.i("DebugTabLeds", "onCreateView");

		int lay = DebugActivity.layouts[tab_id];
		//View rootView = inflater.inflate( R.layout.fragment_device_list_dummy, container, false);
		this.rootView = inflater.inflate( lay, container, false);

		Switch xb6 = (Switch) rootView.findViewById(R.id.all_lights_on);	
		final BarobotConnector barobot = Arduino.getInstance().barobot;
		xb6.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
		    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		        if (isChecked) {
		        	barobot.lightManager.setAllLeds( barobot.main_queue, "ff",255, 255, 255,255 );
		        } else {
		        	barobot.lightManager.turnOffLeds(barobot.main_queue);
		        }
		    }
		});

		Button xb1 = (Button) rootView.findViewById(R.id.main_color);
		xb1.setOnClickListener( new OnClickListener(){
			@Override
			public void onClick(View v) {
				Initiator.logger.i("DebugTabLeds","setOnClickListener main_color" );
				changeColorOf("ff", 0);
			}
		});
		Button xb2 = (Button) rootView.findViewById(R.id.top_color);
		xb2.setOnClickListener( new OnClickListener(){
			@Override
			public void onClick(View v) {
				Initiator.logger.i("DebugTabLeds","setOnClickListener top_color" );
				changeColorOf("0f", 3);
			}
		});
		Button xb3 = (Button) rootView.findViewById(R.id.bottom_color);
		xb3.setOnClickListener( new OnClickListener(){
			@Override
			public void onClick(View v) {
				changeColorOf("f0", 4);
			}
		});

		Button cc = (Button) rootView.findViewById(R.id.carret_color);
		cc.setOnClickListener( new OnClickListener(){
			@Override
			public void onClick(View v) {
				changeColorOf("f0", 1);
			}
		});

		SeekBar light_scale = (SeekBar) rootView.findViewById( R.id.light_scale );
		if(light_scale!=null){
			light_scale.setProgress(I2C_Device_Imp.level);	
		}
		Map<Integer, String> buttonToCommand = new HashMap<Integer, String>();
		buttonToCommand.put( R.id.led_off, "led_off" );
		buttonToCommand.put( R.id.scann_leds, "scann_leds" );
		buttonToCommand.put( R.id.led_green_on, "led_green_on" );
		buttonToCommand.put( R.id.led_blue_on, "led_blue_on" );
		buttonToCommand.put( R.id.led_red_on, "led_red_on" );

		View rootView = inflater.inflate( lay, container, false);
		DebugTabCommands.assignButtons(buttonToCommand, rootView );
		return rootView;
	}

	private void changeColorOf(final String string, final int device) {
		AmbilWarnaDialog dialog = new AmbilWarnaDialog(rootView.getContext(), lastcolor, 
				new OnAmbilWarnaListener() {
		        @Override
		        public void onOk(AmbilWarnaDialog dialog, int color) {
		        	int red		= Color.red(color);
		        	int green	= Color.green(color);
		    		int blue	= Color.blue(color);
		    		BarobotConnector barobot = Arduino.getInstance().barobot;
		    		if(device == 0 || device == 3 || device == 4 ){
		    			barobot.lightManager.setAllLeds(barobot.main_queue, string, 100, red, green, blue);
		    		}else if(device == 1 ){
		    			barobot.lightManager.carret_color(barobot.main_queue, red, green, blue);
		    		}
					lastcolor = color;
		        }
		        @Override
		        public void onCancel(AmbilWarnaDialog dialog) {
		        	Log.i("OnAmbilWarnaListener", "onCancel");
		        }
		});
		dialog.show();
	};
}
