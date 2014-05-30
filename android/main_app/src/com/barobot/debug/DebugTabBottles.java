package com.barobot.debug;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.barobot.R;
import com.barobot.activity.DebugActivity;
import com.barobot.common.Initiator;
import com.barobot.common.constant.Constant;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;

public class DebugTabBottles extends Fragment {
	public int tab_id	= -1 ;
	private Activity cc;
    public DebugTabBottles(Activity debugActivity, int tabCommandsId) {
    //	Initiator.logger.i("DebugTabBottles", "init");
    	this.tab_id = tabCommandsId;
    	this.cc=debugActivity;
	}
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
		//Integer.toString(getArguments().getInt(DebugActivity.ARG_SECTION_NUMBER))
    //	Initiator.logger.i("DebugTabBottles", "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
    }
	public Point getScreenSize( ) {
		Display display = cc.getWindowManager().getDefaultDisplay();
		//display.getRotation()
		Point size = new Point();
		display.getSize(size);
		return size;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		//Initiator.logger.i("DebugTabBottles", "onCreateView");
		int lay = DebugActivity.layouts[tab_id];
		//View rootView = inflater.inflate( R.layout.fragment_device_list_dummy, container, false);
		View rootView = inflater.inflate( lay, container, false);
		Point size = getScreenSize();

		button_zajedz bz = new button_zajedz();
		int[] nalejs = {
				R.id.start_pos,
				R.id.nalej1,
				R.id.nalej2,
				R.id.nalej3,
				R.id.nalej4,
				R.id.nalej5,
				R.id.nalej6,
				R.id.nalej7,
				R.id.nalej8,
				R.id.nalej9,
				R.id.nalej10,
				R.id.nalej11,
				R.id.nalej12
				};
		int button_width = (size.x -100) / nalejs.length * 2;
		for(int i =0; i<nalejs.length;i++){
			View w = rootView.findViewById(nalejs[i]);
			String classname = w.getClass().getName();
			if( "android.widget.Button".equals( classname )){
				Button xb1 = (Button) rootView.findViewById(nalejs[i]);	
				xb1.setOnClickListener(bz);
				LinearLayout.LayoutParams params = (LayoutParams) xb1.getLayoutParams();	// powiększ
				params.width = button_width;
				xb1.setLayoutParams(params);
			}
		}
		int[] wagi = {
				R.id.waga1,
				R.id.waga2,
				R.id.waga3,
				R.id.waga4,
				R.id.waga5,
				R.id.waga6,
				R.id.waga7,
				R.id.waga8,
				R.id.waga9,
				R.id.waga10,
				R.id.waga11,
				R.id.waga12
				};
/*
		OnClickListener list1 = new OnClickListener() {
		    @Override
		    public void onClick(View v) {
		    	 Arduino.getInstance().send("GET WEIGHT");
		    }
		};*/

		BarobotConnector barobot = Arduino.getInstance().barobot;
		for(int i =0; i<wagi.length;i++){
			TextView waga1 = (TextView) rootView.findViewById(wagi[i]);
			int x		=  barobot.getBottlePosX( i );
			int y		=  barobot.getBottlePosY( i );
			String pos = "" + x +"/"+ y;
		//	waga1.setOnClickListener( list1 );
			LinearLayout.LayoutParams params = (LayoutParams) waga1.getLayoutParams();
			params.width = button_width;
			waga1.setLayoutParams(params);
			waga1.setText(pos);
		}
		int[] wagi2 = {
				R.id.waga_start,
				R.id.waga0,
		};
		for(int i =0; i<wagi2.length;i++){
			TextView waga1 = (TextView) rootView.findViewById(wagi2[i]);
			LinearLayout.LayoutParams params = (LayoutParams) waga1.getLayoutParams();
			params.width = button_width;
			waga1.setLayoutParams(params);
		}
		return rootView;
	}
	public void refreshPos() {
		Initiator.logger.i(Constant.TAG,"reload pozycje na stronie glownej");
		int[] wagi = {R.id.waga1,
				R.id.waga2,
				R.id.waga3,
				R.id.waga4,
				R.id.waga5,
				R.id.waga6,
				R.id.waga7,
				R.id.waga8,
				R.id.waga9,
				R.id.waga10,
				R.id.waga11,
				R.id.waga12
		};

		BarobotConnector barobot = Arduino.getInstance().barobot;
		for(int i =0; i<wagi.length;i++){
			TextView waga1 = (TextView) cc.findViewById(wagi[i]);
			int x	=  barobot.getBottlePosX( i );
			int y	=  barobot.getBottlePosY( i );
			String pos = "" + x +"/"+ y;			
			waga1.setText(pos);
		}
	}

}