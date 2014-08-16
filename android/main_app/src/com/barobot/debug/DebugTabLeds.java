package com.barobot.debug;

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
import android.widget.ToggleButton;

import com.barobot.AppInvoker;
import com.barobot.R;
import com.barobot.activity.DebugActivity;
import com.barobot.common.Initiator;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.hardware.devices.i2c.I2C_Device_Imp;
import com.barobot.other.Audio;
import com.barobot.parser.Queue;

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
	//	Initiator.logger.i("DebugTabLeds", "onCreateView");

		int lay = DebugActivity.layouts[tab_id];
		//View rootView = inflater.inflate( R.layout.fragment_device_list_dummy, container, false);
		this.rootView = inflater.inflate( lay, container, false);

		
		button_toggle bt = new button_toggle();
		int[] togglers = {
			
		};
		for(int i =0; i<togglers.length;i++){
			View w = rootView.findViewById(togglers[i]);
			String classname = w.getClass().getName();
			if( "android.widget.ToggleButton".equals( classname )){
				Button xb3 = (ToggleButton) rootView.findViewById(togglers[i]);	
				xb3.setOnClickListener(bt);	
			}	
		}

		Switch xb5 = (Switch) rootView.findViewById(R.id.light_show);	
		xb5.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
		    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		    	Audio a = getAudio();
		    	
		    	if(a.isRunning()){
		    		System.out.println("getAudio stop1");
		        	a.stop();
		    	}else if (isChecked ) {
		    		System.out.println("getAudio start");
		    		final BarobotConnector barobot = Arduino.getInstance().barobot;
		        	a.start(barobot);
		        } else {
		        	System.out.println("getAudio stop2");
		        	a.stop();
		        }
		    }
		});
		Audio a= getAudio();
		if(a.isRunning()){
			xb5.setChecked(true);
		}else{
			xb5.setChecked(false);
		}

		Switch xb6 = (Switch) rootView.findViewById(R.id.all_lights_on);	
		final BarobotConnector barobot = Arduino.getInstance().barobot;
		xb6.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
		    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		        if (isChecked) {
		        	barobot.setLeds( barobot.main_queue,"ff", 255 );
		        } else {
		        	barobot.setLeds( barobot.main_queue,"ff", 0 );
		        }
		    }
		});

		Button xb1 = (Button) rootView.findViewById(R.id.main_color);
		xb1.setOnClickListener( new OnClickListener(){
			@Override
			public void onClick(View v) {
				changeColorOf("ff");
			}
		});
		Button xb2 = (Button) rootView.findViewById(R.id.top_color);
		xb2.setOnClickListener( new OnClickListener(){
			@Override
			public void onClick(View v) {
				changeColorOf("0f");
			}
		});
		Button xb3 = (Button) rootView.findViewById(R.id.bottom_color);
		xb3.setOnClickListener( new OnClickListener(){
			@Override
			public void onClick(View v) {
				changeColorOf("f0");
			}
		});
		
		Switch whto = (Switch) rootView.findViewById(R.id.white_top_on);	
		whto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
		    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		        if (isChecked) {
		        	barobot.setLeds( barobot.main_queue,"08", 255 );
		        } else {
		        	barobot.setLeds( barobot.main_queue,"08", 0 );
		        }
		    }
		});
		Switch whbo = (Switch) rootView.findViewById(R.id.white_bottom_on);	
		whbo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
		    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		        if (isChecked) {
		        	barobot.setLeds( barobot.main_queue,"80", 255 );
		        } else {
		        	barobot.setLeds( barobot.main_queue,"80", 0 );
		        }
		    }
		});
		Button cc = (Button) rootView.findViewById(R.id.carret_color);
		cc.setOnClickListener( new OnClickListener(){
			@Override
			public void onClick(View v) {
				changeCarretColor();
			}
		});

		SeekBar light_scale = (SeekBar) rootView.findViewById( R.id.light_scale );
		if(light_scale!=null){
			light_scale.setProgress(I2C_Device_Imp.level);	
		}
		SeekBar white_scale = (SeekBar) rootView.findViewById( R.id.white_scale );
		if(white_scale!=null){
			
		}
		
		
		
		
		
		return rootView;
	}

	private Audio getAudio() {
		Audio a = (Audio) AppInvoker.container.get("Audio");
    	if(a == null ){
    		a = new Audio();
    		AppInvoker.container.put("Audio", a );
    	}
    	return a;
	}

	private void changeCarretColor() {
		AmbilWarnaDialog dialog = new AmbilWarnaDialog(rootView.getContext(), lastcolor, 
				new OnAmbilWarnaListener() {
		        @Override
		        public void onOk(AmbilWarnaDialog dialog, int color) {
		    		int blue	= Color.blue(color);
		        	int red		= Color.red(color);
		        	int green	= Color.green(color);
		        	int white	= 0;
		    		Queue q1	= new Queue();
		    		BarobotConnector barobot = Arduino.getInstance().barobot;
		  		  	barobot.i2c.carret.setRgbw(q1, red,green,blue,white);
		  		  	barobot.main_queue.add(q1);
					lastcolor = color;
		        }
		        @Override
		        public void onCancel(AmbilWarnaDialog dialog) {
		        	Log.i("OnAmbilWarnaListener", "onCancel");
		        }
		});
		dialog.show();
	};	
	private void changeColorOf(final String string) {
		AmbilWarnaDialog dialog = new AmbilWarnaDialog(rootView.getContext(), lastcolor, 
				new OnAmbilWarnaListener() {
		        @Override
		        public void onOk(AmbilWarnaDialog dialog, int color) {
		        	int red		= Color.red(color);
		        	int green	= Color.green(color);
		    		int blue	= Color.blue(color);
		    		BarobotConnector barobot = Arduino.getInstance().barobot;
		        	barobot.setColor( barobot.main_queue, string, red, green, blue, 0 );
					lastcolor = color;
		        }
		        @Override
		        public void onCancel(AmbilWarnaDialog dialog) {
		        	Log.i("OnAmbilWarnaListener", "onCancel");
		        }
		});
		dialog.show();
	};
	

    public int getDipsFromPixel(float pixels) {
        // Get the screen's density scale
        final float scale = getResources().getDisplayMetrics().density;
        // Convert the dps to pixels, based on density scale
        return (int) (pixels * scale + 0.5f);
    } 
}
