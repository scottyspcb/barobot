package com.barobot.debug;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.barobot.R;
import com.barobot.activity.DebugActivity;
import com.barobot.common.Initiator;
import com.barobot.common.constant.Constant;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;

public class DebugTabDevices extends Fragment {
	public int tab_id	= -1 ;
	private Activity cc;
	
    public DebugTabDevices(Activity debugActivity, int tabCommandsId) {
    	this.tab_id = tabCommandsId;
    	this.cc=debugActivity;
	}
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
		//Integer.toString(getArguments().getInt(DebugActivity.ARG_SECTION_NUMBER))
        super.onActivityCreated(savedInstanceState);
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		int lay = DebugActivity.layouts[tab_id];
		final BarobotConnector barobot = Arduino.getInstance().barobot;
		final View rootView = inflater.inflate( lay, container, false);
		final int[] buttons = {
				R.id.editb0,
				R.id.editb1,
				R.id.editb2,
				R.id.editb3,
				R.id.editb4,		
				R.id.editb5,
				R.id.editb6,
				R.id.editb7,		
				R.id.editb8,
				R.id.editb9,
				R.id.editb10,
				R.id.editb11	
		};
		Button xb1 = (Button) rootView.findViewById(R.id.save_poss);
		xb1.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				for(int i=0;i<12;i++){
					EditText editb0 = (EditText) rootView.findViewById( buttons[i] );
					int a = Integer.parseInt(editb0.getText().toString());
					barobot.setSlotMarginX(i, a );
				}
			}
		});
		for(int i=0;i<12;i++){
			int xpos = barobot.getSlotMarginX( i );
			EditText editb0 = (EditText) rootView.findViewById( buttons[i] );
			editb0.setText(""+xpos );
		}
		return rootView;
	}
}

/*
editb0.addTextChangedListener(new TextWatcher() {
       public void afterTextChanged(Editable s) {

       }
       public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
       public void onTextChanged(CharSequence s, int start, int before, int count) {}
    });
*/