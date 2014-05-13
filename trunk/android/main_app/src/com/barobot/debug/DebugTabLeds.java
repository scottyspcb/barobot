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
import android.widget.Switch;
import android.widget.ToggleButton;

import com.barobot.R;
import com.barobot.activity.DebugActivity;
import com.barobot.common.Initiator;
import com.barobot.hardware.virtualComponents;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.hardware.devices.i2c.Upanel;
import com.barobot.parser.Queue;

public class DebugTabLeds extends Fragment {
	public int tab_id	= -1 ;
	private Activity cc;
	private int lastcolor = 0xff000000;
	private View rootView;

    public DebugTabLeds(Activity debugActivity, int tabCommandsId) {
    	Initiator.logger.i("DebugTabLeds", "init");
    	this.tab_id = tabCommandsId;
    	this.cc=debugActivity;
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
		        if (isChecked) {
		        	
		        } else {
		        	
		        }
		    }
		});

		Switch xb6 = (Switch) rootView.findViewById(R.id.all_lights_on);	
		xb6.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
		    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		        if (isChecked) {
		        	virtualComponents.barobot.setLeds( "ff", 255 );
		        } else {
		        	virtualComponents.barobot.setLeds( "ff", 0 );
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
		        	virtualComponents.barobot.setLeds( "08", 255 );
		        } else {
		        	virtualComponents.barobot.setLeds( "08", 0 );
		        }
		    }
		});
		Switch whbo = (Switch) rootView.findViewById(R.id.white_bottom_on);	
		whbo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
		    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		        if (isChecked) {
		        	virtualComponents.barobot.setLeds( "80", 255 );
		        } else {
		        	virtualComponents.barobot.setLeds( "80", 0 );
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
		
	
		/*
		light_scale
		white_scale
		*/
		
		return rootView;
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
		    		Queue q		= virtualComponents.barobot.main_queue;
		    		q.add(q1);
		  		  	virtualComponents.barobot.i2c.carret.setRgbw(q1, red,green,blue,white);
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
		        	virtualComponents.setColor( string, color );
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
